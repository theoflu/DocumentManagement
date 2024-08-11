package com.theoflu.Document.Management.user.service;


import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.repository.UserRepository;
import com.theoflu.Document.Management.user.request.AddRoleReq;
import com.theoflu.Document.Management.user.request.CreateTeamReq;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.response.statusProcesses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public interface UserService {

     UserEntity findUser(String username);
     statusProcesses saveFile(FileEntity file);
     FileEntity findFile(String filename);
     TeamEntity findTeam (String teamname);
     List<FileEntity> listNonApproveFiles();
     List<UserEntity> onaylamasigerekenler();
     Role getHighestRole(UserEntity user);
     String getNextLowerRoleName(ERole currentRole);
     String approveChecker(int eroleNo, String filename);
     PermCheckerResponse PermChecker(String username, ERolePerm Perm);

     statusProcesses createTeam(CreateTeamReq createTeamReq);

     TeamEntity findUserTeam (UserEntity entity);
     ERole getRol(int i);
     statusProcesses rolePermEkle(int roleId, String permName);
     List<String> searchInFolder(String folderPath, String searchTerm, String username);

     statusProcesses addRole(AddRoleReq addRoleReq);
     statusProcesses delRole(AddRoleReq addRoleReq);
     statusProcesses rolePermCikar(int roleId, String permName);



     boolean hasUserAlreadyApproved(FileEntity file, UserEntity user);
     boolean canUserApproveFile(FileEntity file, Role highestRole);
     ResponseEntity<String> approveFileAndRespond(FileEntity file, UserEntity user, Role highestRole);


     String extractUsernameFromToken(String token);
     boolean hasApprovers(FileEntity file);
     boolean isUserInTeam(FileEntity file, UserEntity user);
     boolean canUserReportFile(FileEntity file, Role highestRole);
     void reportFile(FileEntity file, UserEntity user, String reportMessage);
     void removeLastApprover(FileEntity file);


      boolean isUserInFileTeam(FileEntity file, UserEntity user) ;
      void updateExistingFile(FileEntity existingFile, MultipartFile file, UserEntity user, String fileName)throws IOException ;
      void deleteOldFile(String fileName);
     void saveNewFile(File tempFile, String fileName) throws IOException;
     void updateFileEntity(FileEntity existingFile, UserEntity user, String fileName);
}
