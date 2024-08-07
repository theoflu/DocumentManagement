package com.theoflu.Document.Management.user.service.impl;


import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.repository.FileRepository;
import com.theoflu.Document.Management.user.repository.RoleRepository;
import com.theoflu.Document.Management.user.repository.TeamRepository;
import com.theoflu.Document.Management.user.repository.UserRepository;
import com.theoflu.Document.Management.user.request.CreateTeamReq;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.response.statusProcesses;
import com.theoflu.Document.Management.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private  final UserRepository userepo;
    private  final FileRepository fileRepository;
    private  final TeamRepository teamRepository;
    private  final RoleRepository roleRepository;
    @Override
    public UserEntity findUser(String username){
        return userepo.findUserEntityByUsername(username);

    }
    @Override
    public statusProcesses saveFile(FileEntity file){
        try {
            fileRepository.save(file);
            return new statusProcesses("DOSYA KAYDEDİLDİ");
        }
        catch (Exception e){
            return new statusProcesses("DOSYA Kaydedilmedi HATA!  "+ e.getMessage());
        }


    }

    @Override
    public FileEntity findFile(String filename) {
        return fileRepository.findFileEntityByFile(filename);
    }

    @Override
    public TeamEntity findTeam(String team_name) {
      return  teamRepository.findTeamEntityByTeamname(team_name);


    }
    @Override
    public TeamEntity findUserTeam(UserEntity entity) {

        return teamRepository.findTeamEntityByUserEntities(entity);
    }

    @Override
    public List<FileEntity> listNonApproveFiles() { // Onaylanmamış belgeri ve kimden onay alması gerektiğini gösteren fonk

        List<FileEntity> unapprovedFiles = fileRepository.findAll().stream()
                .filter(file -> !file.is_Approved())
                .toList();
        return unapprovedFiles;
    }

    @Override
    public List<UserEntity> onaylamasigerekenler() {
        return userepo.findAll();
    }
    @Override
    public Role getHighestRole(UserEntity user) { // Kullanıcıyı verip rolü aldığım fonk
        return user.getRoles().stream()
                .max(Comparator.comparing(Role::getName))
                .orElseThrow(() -> new RuntimeException("Kullanıcının rolü yok!"));
    }

    @Override
    public String getNextLowerRoleName(ERole currentRole) {// Hangi kullanıcıdan izin beklendiğini hesaplamak için
        ERole[] roles = ERole.values();
        int currentIndex = currentRole.ordinal();
        if (currentIndex > 0) {
            return roles[currentIndex - 1].name();
        }
        return "Bir alt rol bulunamadı";
    }
    @Override
    public ERole getRol(int i) {
        ERole[] roles = ERole.values();
        if (i >= 0 && i < roles.length) {
            return roles[i];
        } else {
            throw new IllegalArgumentException("Invalid index: " + i);
        }
    }
    @Override
    public String approveChecker(int eroleNo, String filename) {// Bir belgeyi onaylaması gerekenlerin kontrolü
        FileEntity file = findFile(filename);
        List<UserEntity> approvers = file.getUserEntity();
      //  List<UserEntity> users = onaylamasigerekenler();
        List<UserEntity> users = file.getTeam().get(0).getUserEntities();


        List<UserEntity> requiredUsersWithRole = users.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().ordinal() == eroleNo))
                .toList();

        List<UserEntity> nonMatchingUsers = requiredUsersWithRole.stream()
                .filter(requiredUser -> approvers.stream()
                        .noneMatch(approver -> approver.getRoles().stream()
                                .anyMatch(role -> role.getName().ordinal() == eroleNo && approver.equals(requiredUser))))
                .toList();

        if (!nonMatchingUsers.isEmpty()) {
            return "110"; // Onaylaması gereken kullanıcılar arasında rolü olan ancak onaylayanlarda bulunmayanlar var
        } else {
            // Uncomment and complete this section if necessary
            // file.setApproved(true);
            // userService.saveFile(file);
            return "Tüm onaylaması gereken roller onaylandı.";
        }
    }

    @Override
    public PermCheckerResponse PermChecker(String username, ERolePerm Perm){ // Kullanıcının permlerini kontrol etme okuma yazma vs
        UserEntity user=findUser(username);
        List<Role>dene=user.getRoles().stream().toList();
        //rolu aldık
        List<ERolePerm> a= dene.get(0).getPerms();

        for (ERolePerm eRolePerm : a) {
            if (eRolePerm == Perm) {
                return new PermCheckerResponse("You have perm " + Perm.name(), true);
            }
        }
        return new PermCheckerResponse("You have not perm "+Perm.name(),false);

    }

    @Override
    public statusProcesses createTeam(CreateTeamReq createTeamReq) {
        TeamEntity team= new TeamEntity();
        List<UserEntity> user= new ArrayList<>();
        team.setTeamname(createTeamReq.getTeam_name());
        for (int i=0;i<createTeamReq.getUsername().size();i++){
            user.add(findUser(createTeamReq.getUsername().get(i)));
        }
        team.setUserEntities(user);
        teamRepository.save(team);
        return new statusProcesses("TAKIM OLUŞTURULDU");
    }




}
