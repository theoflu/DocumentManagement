package com.theoflu.Document.Management.user.controller;

import com.theoflu.Document.Management.user.configs.JwtUtils;
import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.request.ApproveReq;
import com.theoflu.Document.Management.user.request.ReportReq;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600,allowedHeaders = "*")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;
    private final JwtUtils jwtUtils;
    //TODO  e-imza yı bağlayalım
    //upload
    //download
    //approve
    //denied
    //checkfile
    //sign
    private PermCheckerResponse PermChecker(String username, ERolePerm Perm){
        UserEntity user=userService.findUser(username);
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
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestHeader("Authorization") String token,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "fileName", required = false) String fileName) {
       PermCheckerResponse permCheckerResponse= PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.UPLOAD );
        if (file.isEmpty()) {
            return new ResponseEntity<>("Dosya boş", HttpStatus.BAD_REQUEST);
        }
        if(permCheckerResponse.isVal()) {
            try {

                // Geçici dizinde dosyayı al
                Path tempDir = Files.createTempDirectory("");
                File tempFile = new File(tempDir.toFile(), file.getOriginalFilename());
                file.transferTo(tempFile);

                // Dosyanın kaydedileceği yeri belirliyoruz
                String uploadDir = "uploads/";
                File uploadDirectory = new File(uploadDir);

                if (!uploadDirectory.exists()) {
                    uploadDirectory.mkdirs();  // Dizinleri oluştur
                }

                // Dosya adını alıyoruz
                if (fileName == null || fileName.isEmpty()) {
                    fileName = file.getOriginalFilename();  // Dosya adı sağlanmadıysa orijinal dosya adını kullan
                }
                String filePath = uploadDir + fileName;
                File dest = new File(filePath);
                // find user

                UserEntity user = userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));

                // Geçici dosyayı belirli bir dizine kopyala
                Files.copy(tempFile.toPath(), dest.toPath());
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFile(fileName);
                fileEntity.set_Approved(false);
                fileEntity.setSender(user);
                fileEntity.setUserEntity(user);
                fileEntity.setChecked_by_whom(ERole.ROLE_Content_Creator);
                fileEntity.setReported_by_whom(ERole.ROLE_Reviewer);
                userService.saveFile(fileEntity);

                return new ResponseEntity<>("Dosya başarıyla yüklendi: " + filePath, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Dosya yüklenirken bir hata oluştu", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else
            return new ResponseEntity<>(permCheckerResponse.getMessage(),HttpStatus.FORBIDDEN);

    }

    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(FileEntity fileEntity){
        return  null;
    }
    @PostMapping("/approve")
    public ResponseEntity<?> approveFile(@RequestHeader("Authorization") String token, @RequestBody ApproveReq req){
        FileEntity file= userService.findFile(req.getFilename());
        UserEntity user=userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));
        List<Role>dene=user.getRoles().stream().toList();
        //rolu aldık
       // List<ERolePerm> a= dene.get(0).getPerms();
        PermCheckerResponse permCheckerResponse= PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.APPROVE );
        if(permCheckerResponse.isVal()){
            if (file.getChecked_by_whom().ordinal()>=dene.get(0).getName().ordinal()){
                user.getRoles().forEach(role -> file.setChecked_by_whom(role.getName()));
                file.set_Approved(req.isApprove());
                file.setUserEntity(user);
                userService.saveFile(file);
                return  ResponseEntity.ok("İstek Başarılı Şekilde Gerçekleşti.");

            }
            else{
                return new ResponseEntity<>("Bunu yapmaya yetkin yok!", HttpStatus.FORBIDDEN);
            }
        }
        else
            return new ResponseEntity<>(permCheckerResponse.getMessage(),HttpStatus.FORBIDDEN);
    }
    @PostMapping("/check")
    public ResponseEntity<?> checkFile(FileEntity fileEntity){

        return  null;

    }
    @PostMapping("/report")
    public ResponseEntity<?> reportFile(@RequestHeader("Authorization") String token, @RequestBody ReportReq req){

            PermCheckerResponse permCheckerResponse = PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)), ERolePerm.REPORT);
            UserEntity user = userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));
            FileEntity file = userService.findFile(req.getFilename());
            List<Role> dene = user.getRoles().stream().toList();
            if (file.getReported_by_whom().ordinal() >= dene.get(0).getName().ordinal()) {
                if (permCheckerResponse.isVal()) {
                    file.setReport(req.getReport_message());
                    file.set_Approved(false);
                    file.setReported_by_whom(dene.get(0).getName());
                    userService.saveFile(file);
                    return ResponseEntity.ok("İşlem tamamdır yavri");
                } else return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
            }else return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);


    }
    @PostMapping("/sign")
    public ResponseEntity<?> signFile(FileEntity fileEntity){


        return  null;
    }


}
