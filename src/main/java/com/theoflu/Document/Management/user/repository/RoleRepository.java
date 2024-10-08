package com.theoflu.Document.Management.user.repository;

import com.theoflu.Document.Management.user.entity.ERole;
import com.theoflu.Document.Management.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
