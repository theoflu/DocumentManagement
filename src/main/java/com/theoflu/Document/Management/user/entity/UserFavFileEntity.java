package com.theoflu.Document.Management.user.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
@Data
@Table(name = "favs")
@Builder
public class UserFavFileEntity  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private UserEntity user;
    private ArrayList<FileEntity> fileEntities;


}
