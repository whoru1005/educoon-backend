package com.educoon.domain.user.repository;

import com.educoon.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param kakaoId
     * @return Optinal<User>
     */
    Optional<User> findByKakaoId(String kakaoId);

}
