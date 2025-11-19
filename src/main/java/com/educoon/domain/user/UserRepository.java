package com.educoon.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param kakaoId
     * @return Optinal<User>
     * 카카오 ID로 사용자를 찾는 메소드(회원가입 여부 확인 시 사용)
     */
    Optional<User> findByKakaoId(String kakaoId);

    User findByNickname(String nickname);
}
