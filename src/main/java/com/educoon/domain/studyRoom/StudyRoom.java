package com.educoon.domain.studyRoom;

import com.educoon.domain.chatMessage.ChatMessage;
import com.educoon.domain.roomParticipant.RoomParticipant;
import com.educoon.domain.roomTagMap.RoomTagMap;
import com.educoon.domain.studyRecord.StudyRecord;
import com.educoon.domain.tag.Tag;
import com.educoon.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "study_rooms")
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxCapacity = 10;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyRecord> studyRecords = new ArrayList<>();

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChatMessage> chatMessages = new ArrayList<>();

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomTagMap> roomTagMaps = new ArrayList<>();

    public void addParticipant(User user){
        RoomParticipant participant = RoomParticipant.builder()
                .user(user)
                .studyRoom(this)
                .build();

        this.participants.add(participant);
        user.getRoomParticipants().add(participant);
    }

    public void addTag(Tag tag){
        RoomTagMap roomTagMap = RoomTagMap.builder()
                .tag(tag)
                .studyRoom(this)
                .build();

        this.roomTagMaps.add(roomTagMap);
        tag.getRoomTagMaps().add(roomTagMap);
    }

    public void updateDetails(String title, String password, Integer maxCapacity, String description){
        this.title = title;
        this.password = (password != null && !password.isEmpty()) ? password : null;
        this.maxCapacity = maxCapacity;
        this.description = description;
        this.isPublic = password != null && !password.isEmpty();
    }

    public void updateTags(List<Tag> newTags){
        this.roomTagMaps.clear();

        if(newTags != null){
            newTags.forEach(tag ->{
                RoomTagMap newMap = RoomTagMap.builder()
                        .studyRoom(this)
                        .tag(tag)
                        .build();
                this.roomTagMaps.add(newMap);
            });
        }
    }
}
