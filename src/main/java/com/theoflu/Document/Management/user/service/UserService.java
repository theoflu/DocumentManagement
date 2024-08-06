package com.theoflu.Document.Management.user.service;


import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.repository.UserRepository;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.response.statusProcesses;

import java.io.File;
import java.util.List;

public interface UserService {

     UserEntity findUser(String username);
     statusProcesses saveFile(FileEntity file);
     FileEntity findFile(String filename);
     List<FileEntity> listNonApproveFiles();
     List<UserEntity> onaylamasigerekenler();
     Role getHighestRole(UserEntity user);
     String getNextLowerRoleName(ERole currentRole);
     String approveChecker(int eroleNo, String filename);
     PermCheckerResponse PermChecker(String username, ERolePerm Perm);



}
