package com.educoon.config;

import com.educoon.domain.user.dto.UserLocation;
import com.educoon.domain.user.dto.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionRoomRegistry {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ROOM_KEY_PREFIX = "room:";
    private static final String SESSION_KEY_PREFIX = "session:";

    public void userJoin(Long roomId, UserStatus userStatus){
        String key = ROOM_KEY_PREFIX + roomId;

        redisTemplate.opsForHash().put(key, userStatus.getUserId().toString(), userStatus);

        log.debug("Redis 입장 저장: room={}, user={}", roomId, userStatus.getNickname());
    }

    public Optional<UserStatus> userLeave(Long roomId, Long userId){
        String key = ROOM_KEY_PREFIX + roomId;

        Object rawData = redisTemplate.opsForHash().get(key, userId.toString());

        if(rawData != null){
            redisTemplate.opsForHash().delete(key, userId.toString());
        }

        return Optional.ofNullable((UserStatus) rawData);

    }

    public List<UserStatus> getStudyRoomStatus(Long roomId){
        String key = ROOM_KEY_PREFIX + roomId;

        List<Object> rawList = redisTemplate.opsForHash().values(key);

        if(rawList == null)
            return Collections.emptyList();

        return rawList.stream()
                .map(obj -> (UserStatus)obj)
                .collect(Collectors.toList());
    }

    public Optional<UserStatus> getUserStatus(Long roomId, Long userId){
        String key = ROOM_KEY_PREFIX + roomId;

        Object rawData = redisTemplate.opsForHash().get(key, userId.toString());

        return Optional.ofNullable((UserStatus) rawData);
    }

    public void registerSession(String sessionId, UserLocation location){
        String key = SESSION_KEY_PREFIX + sessionId;

        redisTemplate.opsForValue().set(key, location, Duration.ofHours(24));
    }

    public Optional<UserLocation> unregisterSession(String sessionId){
        String key = SESSION_KEY_PREFIX + sessionId;

        Object rawData = redisTemplate.opsForValue().get(key);
        if(rawData != null)
            redisTemplate.delete(key);

        return Optional.ofNullable((UserLocation) rawData);
    }


}
