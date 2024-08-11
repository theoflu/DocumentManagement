package com.theoflu.Document.Management.user.repository;

import com.theoflu.Document.Management.user.entity.FileEntity;
import com.theoflu.Document.Management.user.entity.UserEntity;
import com.theoflu.Document.Management.user.entity.UserFavFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavRepository extends JpaRepository<UserFavFileEntity,Long> {

    UserFavFileEntity findByUser(UserEntity user);
}
