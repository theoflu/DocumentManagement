package com.theoflu.Document.Management.user.repository;

import com.theoflu.Document.Management.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByUsername(String username);
    UserEntity findUserEntityByUsername(String username);
    Optional<UserEntity> findUserByEmail(String userName);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
