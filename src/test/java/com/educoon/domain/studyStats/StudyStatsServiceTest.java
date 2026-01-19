package com.educoon.domain.studyStats;

import com.educoon.domain.studyRecord.repository.StudyRecordRepository;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class StudyStatsServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private StudyRecordRepository studyRecordRepository;

    @InjectMocks
    private StudyStatsService studyStatsService;

    @Test
    @DisplayName("일일 총 학습 시간 조회 성공")
    void getDailyTotalDuration_Success() {
        // given
        String kakaoId = "12345678";
        LocalDate date = LocalDate.of(2024, 1, 1);
        User user = User.builder().userId(1L).kakaoId(kakaoId).build();
        
        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));
        given(studyRecordRepository.findDurationSumByUserAndPeriod(eq(user), any(), any())).willReturn(3600L);

        // when
        StudyStatsTotalDurationResponse result = studyStatsService.getDailyTotalDuration(kakaoId, date);

        // then
        assertThat(result.getTotalDuration()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("주간 총 학습 시간 조회 성공")
    void getWeeklyTotalDuration_Success() {
        // given
        String kakaoId = "12345678";
        LocalDate date = LocalDate.of(2024, 1, 1);
        User user = User.builder().userId(1L).kakaoId(kakaoId).build();

        given(userRepository.findByKakaoId(kakaoId)).willReturn(Optional.of(user));
        given(studyRecordRepository.findDurationSumByUserAndPeriod(eq(user), any(), any())).willReturn(7200L);

        // when
        StudyStatsTotalDurationResponse result = studyStatsService.getWeeklyTotalDuration(kakaoId, date);

        // then
        assertThat(result.getTotalDuration()).isEqualTo(7200L);
    }
}
