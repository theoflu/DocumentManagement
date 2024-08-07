package com.theoflu.Document.Management.user.repository;

import com.theoflu.Document.Management.user.entity.Role;
import com.theoflu.Document.Management.user.entity.TeamEntity;
import com.theoflu.Document.Management.user.entity.UserEntity;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<TeamEntity, Long> {

    TeamEntity findTeamEntityByTeamname(String team_name);
    TeamEntity findTeamEntityByUserEntities (UserEntity user);
}
