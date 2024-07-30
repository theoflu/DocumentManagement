package com.theoflu.Document.Management.user.service;


import com.theoflu.Document.Management.user.entity.FileEntity;
import com.theoflu.Document.Management.user.entity.UserEntity;
import com.theoflu.Document.Management.user.repository.UserRepository;
import com.theoflu.Document.Management.user.response.statusProcesses;

import java.io.File;

public interface UserService {

     UserEntity findUser(String username);
     statusProcesses saveFile(FileEntity file);
     FileEntity findFile(String filename);



}
