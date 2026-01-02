package com.educoon.domain.token.repository;

import com.educoon.domain.token.entity.RefreshToken;
import com.educoon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

//    토큰 값으로 RefreshToken 엔티티 조회(검증 시 사용)
    Optional<RefreshToken> findByTokenValue(String tokenValue);

//    신규 사용자로 RefreshToken 엔티티 조회(로그인 시 업데이트/생성 시 사용)
    Optional<RefreshToken> findByUser(User user);
}
