package com.theoflu.Document.Management.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "files")
@Builder
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToMany
    @Column(name = "users_who_approve")
    private List<UserEntity> userEntity;
    @Column(name = "is_Approved")
    private boolean  is_Approved;
    @Column(name = "checked_by_whom")//ONAYLAMA SIRASINA GÖRE TAMAMLANIRSA ÜSTTEKİ TRUE OLUR YOKSA FALSE
    private ERole checked_by_whom;
    @Column(name = "files")
    private String file;
    @Column(name = "file_size")
    private Float  file_size;
    @Column(name = "report")
    private String  report;
    @Column(name = "reported_by_whom")
    private ERole  reported_by_whom;
    @ManyToMany
    private List<TeamEntity>  team;
    @ManyToOne
    private UserEntity sender;

}