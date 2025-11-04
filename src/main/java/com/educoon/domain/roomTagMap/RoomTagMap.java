package com.educoon.domain.roomTagMap;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.tag.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "tag_id"})
        },
        name = "room_tag_maps"
)
public class RoomTagMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private StudyRoom studyRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;
}
