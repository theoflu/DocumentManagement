package com.theoflu.Document.Management.user.service.impl;


import com.theoflu.Document.Management.user.entity.FileEntity;
import com.theoflu.Document.Management.user.entity.UserEntity;
import com.theoflu.Document.Management.user.repository.FileRepository;
import com.theoflu.Document.Management.user.repository.UserRepository;
import com.theoflu.Document.Management.user.response.statusProcesses;
import com.theoflu.Document.Management.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private  final UserRepository userepo;
    private  final FileRepository fileRepository;
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


}
