package com.educoon.config;

import com.educoon.domain.user.UserLocation;
import com.educoon.domain.user.UserStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SessionRoomRegistry {

//    방별 현재 상태 저장소
//    Key: roomId, Value: Map<UserId, UserStatus>
    private final Map<Long, Map<Long, UserStatus>> roomStates = new ConcurrentHashMap<>();

//    세션-위치 매핑(Disconnect 처리용)
//    Key: sessionId, Value: UserLocation(userId, nickname, roomId)
    private final Map<String, UserLocation> sessionLocations = new ConcurrentHashMap<>();

    public void userJoin(Long roomId, UserStatus userStatus){

        Map<Long, UserStatus> userMap = roomStates.computeIfAbsent(
                roomId, k -> new ConcurrentHashMap<>()
        );

        userMap.put(userStatus.getUserId(), userStatus);
    }

    public Optional<UserStatus> userLeave(Long roomId, Long userId){
        Map<Long, UserStatus> userMap = roomStates.get(roomId);
        if(userMap != null){
            return Optional.ofNullable(userMap.remove(userId));
        }
        return Optional.empty();
    }

    public List<UserStatus> getStudyRoomStatus(Long roomId){
        Map<Long, UserStatus> userMap = roomStates.getOrDefault(roomId, new ConcurrentHashMap<>());

        return new ArrayList<>(userMap.values());
    }

    public Optional<UserStatus> getUserStatus(Long roomId, Long userId){
        Map<Long, UserStatus> userMap = roomStates.get(roomId);

        if(userMap != null){
            return Optional.ofNullable(userMap.get(userId));
        }

        return Optional.empty();
    }

    public void registerSession(String sessionId, UserLocation location){
        sessionLocations.put(sessionId, location);
    }

    public Optional<UserLocation> unregisterSession(String sessionId){
        return Optional.ofNullable(sessionLocations.remove(sessionId));
    }


}
