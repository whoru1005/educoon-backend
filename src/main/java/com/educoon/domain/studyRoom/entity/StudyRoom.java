package com.educoon.domain.studyRoom.entity;

import com.educoon.domain.roomParticipant.entity.RoomParticipant;
import com.educoon.domain.roomTagMap.entity.RoomTagMap;
import com.educoon.domain.tag.entity.Tag;
import com.educoon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "study_rooms")
@EntityListeners(AuditingEntityListener.class)
public class StudyRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Setter
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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "studyRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RoomTagMap> roomTagMaps = new HashSet<>();

    public void addParticipant(User user){
        RoomParticipant participant = RoomParticipant.builder()
                .user(user)
                .studyRoom(this)
                .build();

        this.participants.add(participant);
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
        this.password = password;
        this.maxCapacity = maxCapacity;
        this.description = description;
        this.isPublic = (password == null);
    }

    public void updateTags(List<Tag> newTags){

        Set<Long> newTagIds = newTags.stream()
                .map(Tag::getTagId)
                .collect(Collectors.toSet());

        this.roomTagMaps.removeIf(rtm ->
                !newTagIds.contains(rtm.getTag().getTagId())
        );

        Set<Long> currentTagIds = this.roomTagMaps.stream()
                .map(rtm -> rtm.getTag().getTagId())
                .collect(Collectors.toSet());

        newTags.forEach(newTag -> {
            if (!currentTagIds.contains(newTag.getTagId())) {
                this.addTag(newTag);
            }
        });
    }

}
