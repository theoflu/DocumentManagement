package com.theoflu.Document.Management.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "keys")
@Builder
public class UserKeys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 5096)
    private String privateKey;
    @Column(length = 5096)
    private String publicKey;
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
