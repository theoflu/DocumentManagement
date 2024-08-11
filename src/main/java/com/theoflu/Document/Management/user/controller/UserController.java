package com.theoflu.Document.Management.user.controller;

import com.theoflu.Document.Management.user.FileSigner.ApiReqs;
import com.theoflu.Document.Management.user.configs.JwtUtils;
import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.request.*;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.service.UserService;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*", maxAge = 3600,allowedHeaders = "*")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private  final UserService userService;
    private final JwtUtils jwtUtils;
    private final ApiReqs apiReqs;
    //TODO  onaylayanlarda tüm onaylaması gerekenler yoksa üsttekilere gitmesin
    //upload
    //download
    //approve
    //denied
    //checkfile
    //sign

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestHeader("Authorization") String token,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "fileName", required = false) String fileName) {
       PermCheckerResponse permCheckerResponse= userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.UPLOAD );
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
                List<UserEntity> list= new ArrayList<>();
                list.add(user);
                TeamEntity team= userService.findUserTeam(user);
                List<TeamEntity> list1=new ArrayList<>();
                list1.add(team);
                // Geçici dosyayı belirli bir dizine kopyala
                Files.copy(tempFile.toPath(), dest.toPath());
                FileEntity fileEntity = new FileEntity();
                fileEntity.setFile(fileName);
                fileEntity.set_Approved(false);
                fileEntity.setReport("");
                fileEntity.setSender(user);
                fileEntity.setChecked_by_whom(userService.getHighestRole(user).getName());
                fileEntity.setReported_by_whom(userService.getRol(userService.getHighestRole(user).getId().intValue()-1));
                fileEntity.setUserEntity(list);

                fileEntity.setTeam(list1);
                userService.saveFile(fileEntity);

                return new ResponseEntity<>("Dosya başarıyla yüklendi: " + filePath, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Dosya yüklenirken bir hata oluştu " +e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else
            return new ResponseEntity<>(permCheckerResponse.getMessage(),HttpStatus.FORBIDDEN);

    }
    /*
    @PostMapping("/updateFile")
    public ResponseEntity<String> updateFile(@RequestHeader("Authorization") String token,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "fileName", required = false) String fileName) {
        PermCheckerResponse permCheckerResponse = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)), ERolePerm.UPLOAD);
        UserEntity user = userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));
        fileName=file.getOriginalFilename();

        if (file.isEmpty()) {
            return new ResponseEntity<>("Dosya boş", HttpStatus.BAD_REQUEST);
        }

        if (permCheckerResponse.isVal()) {
            try {

                FileEntity existingFile = userService.findFile(file.getOriginalFilename());
                List<UserEntity> userEntities = existingFile.getTeam().get(0).getUserEntities();
                if (existingFile == null) {
                    return new ResponseEntity<>("Dosya bulunamadı", HttpStatus.NOT_FOUND);
                }
                if (!userEntities.contains(user)) {
                    return ResponseEntity.ok("Sen Bu grupta değilsin ");
                }
                if (existingFile.getUserEntity().contains(user)) {
                    return ResponseEntity.ok("Sen zaten onayladın.");
                }
                Path tempDir = Files.createTempDirectory("");
                File tempFile = new File(tempDir.toFile(), file.getOriginalFilename());
                file.transferTo(tempFile);
                File oldFile = new File("uploads/" + fileName);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                List<UserEntity> users= existingFile.getUserEntity();
                users.add(user);
                String uploadDir = "uploads/";
                File dest = new File(uploadDir + fileName);
                Files.copy(tempFile.toPath(), dest.toPath());
                existingFile.setFile(fileName);
                existingFile.set_Approved(false);
                existingFile.setReport("");
                existingFile.setReported_by_whom(userService.getRol(userService.getHighestRole(user).getId().intValue()-1));
                existingFile.setUserEntity(users);
                userService.saveFile(existingFile);
                return new ResponseEntity<>("Dosya başarıyla güncellendi: " + fileName, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>("Dosya güncellenirken bir hata oluştu: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
        }
    }


     */

    @PostMapping("/updateFile")
    public ResponseEntity<String> updateFile(@RequestHeader("Authorization") String token,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam(value = "fileName", required = false) String fileName) {
        String username = userService.extractUsernameFromToken(token);
        PermCheckerResponse permCheckerResponse = userService.PermChecker(username, ERolePerm.UPLOAD);
        UserEntity user = userService.findUser(username);
        fileName = file.getOriginalFilename();

        // Dosya boş mu?
        if (file.isEmpty()) {
            return new ResponseEntity<>("Dosya boş", HttpStatus.BAD_REQUEST);
        }

        // Yetki kontrolü
        if (!permCheckerResponse.isVal()) {
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
        }
        try {
            // Dosyayı bul
            FileEntity existingFile = userService.findFile(fileName);
            if (existingFile == null) {
                return new ResponseEntity<>("Dosya bulunamadı", HttpStatus.NOT_FOUND);
            }

            // Kullanıcının dosya ile ilişkili takıma ait olup olmadığını kontrol et
            if (!userService.isUserInFileTeam(existingFile, user)) {
                return ResponseEntity.ok("Sen bu grupta değilsin");
            }

            // Kullanıcının dosyayı daha önce onaylayıp onaylamadığını kontrol et
            if (userService.hasUserAlreadyApproved(existingFile, user)) {
                return ResponseEntity.ok("Sen zaten onayladın.");
            }

            // Dosyayı güncelle
            userService.updateExistingFile(existingFile, file, user, fileName);

            return new ResponseEntity<>("Dosya başarıyla güncellendi: " + fileName, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Dosya güncellenirken bir hata oluştu: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(FileEntity fileEntity){
        return  null;
    }
    /*
    @PostMapping("/approve")
    public ResponseEntity<?> approveFile(@RequestHeader("Authorization") String token, @RequestBody ApproveReq req) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(6));
        FileEntity file = userService.findFile(req.getFilename());
        UserEntity user = userService.findUser(username);
        PermCheckerResponse permCheckerResponse = userService.PermChecker(username, ERolePerm.APPROVE);
        if (!permCheckerResponse.isVal()) {
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
        }

        Role highestRole = userService.getHighestRole(user);
        if(!file.getReport().isEmpty()){
          return ResponseEntity.ok("Raporu düzenledin mi ?");
        }
        if (file.getUserEntity().contains(user)) {
            return ResponseEntity.ok("Sen zaten onayladın.");
        }
        if (file.getChecked_by_whom().ordinal() >= highestRole.getName().ordinal()) {
          String a =  userService.approveChecker(file.getChecked_by_whom().ordinal(),file.getFile());
          if(!a.equals("110") && (file.getChecked_by_whom().ordinal()-1 == highestRole.getId()-1)){
              file.setChecked_by_whom(highestRole.getName());
              if(highestRole.getId()==1)
                file.set_Approved(true);
              file.getUserEntity().add(user);
              file.setReport("");
              userService.saveFile(file);
              return ResponseEntity.ok(highestRole.getName() +" herkes onayladı.");
          }
          else if(file.getChecked_by_whom().ordinal()==highestRole.getId()-1 )
          {
              file.setReport("");
              file.getUserEntity().add(user);
              userService.saveFile(file);
              return ResponseEntity.ok("İstek başarılı şekilde gerçekleşti.");
          }

          return ResponseEntity.ok("Sizden önce onaylaması gerekenler var.");
        } else {
            return new ResponseEntity<>("Bunu yapmaya yetkin yok!", HttpStatus.FORBIDDEN);
        }
    }


     */



    @PostMapping("/approve")
    public ResponseEntity<?> approveFile(@RequestHeader("Authorization") String token, @RequestBody ApproveReq req) {
        String username = userService.extractUsernameFromToken(token);
        FileEntity file = userService.findFile(req.getFilename());
        UserEntity user = userService.findUser(username);

        PermCheckerResponse permCheckerResponse = userService.PermChecker(username, ERolePerm.APPROVE);
        if (!permCheckerResponse.isVal()) {
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
        }

        Role highestRole = userService.getHighestRole(user);

        if (!file.getReport().isEmpty()) {
            return ResponseEntity.ok("Raporu düzenledin mi?");
        }

        if (userService.hasUserAlreadyApproved(file, user)) {
            return ResponseEntity.ok("Sen zaten onayladın.");
        }
        if (!userService.canUserApproveFile(file, highestRole)) {
            return new ResponseEntity<>("Bunu yapmaya yetkin yok!", HttpStatus.FORBIDDEN);
        }
        return userService.approveFileAndRespond(file, user, highestRole);
    }


    @PostMapping("/report")
    public ResponseEntity<?> reportFile(@RequestHeader("Authorization") String token, @RequestBody ReportReq req) {
        String username = userService.extractUsernameFromToken(token);
        FileEntity file = userService.findFile(req.getFilename());
        UserEntity user = userService.findUser(username);

        PermCheckerResponse permCheckerResponse = userService.PermChecker(username, ERolePerm.REPORT);
        if (!permCheckerResponse.isVal()) {
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
        }

        if (!userService.hasApprovers(file)) {
            return new ResponseEntity<>("Kimse Onaylamamış", HttpStatus.FORBIDDEN);
        }
        userService.removeLastApprover(file);

        if (!userService.isUserInTeam(file, user)) {
            return new ResponseEntity<>("Bunu yapmaya yetkin yok! Bu takımda değilsin", HttpStatus.FORBIDDEN);
        }

        Role highestRole = userService.getHighestRole(user);
        if (!userService.canUserReportFile(file, highestRole)) {
            return new ResponseEntity<>("Sizden önce onaylaması gerekenler var.", HttpStatus.FORBIDDEN);
        }

        userService.reportFile(file, user, req.getReport_message());
        return ResponseEntity.ok("Dosya reportlandı.");
    }
     /*
    @PostMapping("/report")
    public ResponseEntity<?> reportFile(@RequestHeader("Authorization") String token, @RequestBody ReportReq req) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(6));
        FileEntity file = userService.findFile(req.getFilename());
        UserEntity user = userService.findUser(username);
        PermCheckerResponse permCheckerResponse = userService.PermChecker(username, ERolePerm.REPORT);
        List<UserEntity> approvers= file.getUserEntity();
        if(approvers.size()-1<0){
            return new ResponseEntity<>("Kimse Onaylamamış ", HttpStatus.FORBIDDEN);
        }
        UserEntity lastApprover= approvers.get(approvers.size()-1);
        approvers.remove(lastApprover);
        file.setUserEntity(approvers);
        //reportlanan dosya o takıma ait mi ?
        List<UserEntity> users = file.getTeam().get(0).getUserEntities(); // dosyanın verildiği takımdaki kullanıcılar
        Role highestRole = userService.getHighestRole(user);
        if (permCheckerResponse.isVal())
        { if(users.contains(user)){
            if (file.getReported_by_whom().ordinal() >= highestRole.getName().ordinal()) {
                if(file.getReported_by_whom().ordinal()==highestRole.getId() )
                {
                    file.setReport(req.getReport_message());
                    file.setReported_by_whom(userService.getRol(userService.getHighestRole(user).getId().intValue()));
                    file.setChecked_by_whom(userService.getRol(userService.getHighestRole(user).getId().intValue()));
                    userService.saveFile(file);
                    return ResponseEntity.ok("Dosya Reportlandı gerçekleşti.");
                }

                return ResponseEntity.ok("Sizden önce onaylaması gerekenler var.");
            } else {
                return new ResponseEntity<>("Bunu yapmaya yetkin yok!", HttpStatus.FORBIDDEN);
            }
        }
        else
            return new ResponseEntity<>("Bunu yapmaya yetkin yok! Bu takımda değilsin", HttpStatus.FORBIDDEN);

        }
        else
            return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);

    }
     */

    @PostMapping("/createTeam")
    public ResponseEntity<?> createTeam(@RequestHeader("Authorization") String token,@RequestBody CreateTeamReq createTeamReq){
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(6));
        UserEntity user = userService.findUser(username);
        if(userService.getHighestRole(user).getId()==1){
            userService.createTeam(createTeamReq);
            return new ResponseEntity<>("BAŞARILI",HttpStatus.OK);
        }
        return new ResponseEntity<>("BAŞARISIZ",HttpStatus.FORBIDDEN);
    }


    @GetMapping("/listNonApproveFiles")
    public ResponseEntity<?> listNonApproveFiles(@RequestHeader("Authorization") String token) {
        String username = jwtUtils.getUserNameFromJwtToken(token.substring(6));
        UserEntity user = userService.findUser(username);
        Role highestRole = userService.getHighestRole(user);

        List<FileEntity> nonApprovedFiles = userService.listNonApproveFiles().stream()
                .filter(file -> file.getChecked_by_whom().ordinal() > highestRole.getName().ordinal())
                .toList();


        List<String> fileEntityDetails = nonApprovedFiles.stream()
                .map(fileEntity -> " Dosya : " + fileEntity.getFile() + "  Yükleyen :  " + fileEntity.getSender().getUsername() +"  Kimden onay bekliyor : " + userService.getNextLowerRoleName(fileEntity.getChecked_by_whom()) )
                .toList();

        return ResponseEntity.ok(fileEntityDetails);
    }
    @GetMapping("/check/{filename}")
    public ResponseEntity<?> checkFile(@RequestHeader("Authorization") String token ,@PathVariable String filename) {

        FileEntity file = userService.findFile(filename);
        List<String> userDetails = file.getUserEntity().stream()
                .map(user -> user.getUsername() + " - " + user.getRoles().stream().map(rol->rol.getName().name()).toList())
                .toList();
        return new ResponseEntity<>("Onaylayanlar tam liste: " + userDetails, HttpStatus.OK);
    }


    @PostMapping("/signfile")
    ResponseEntity<?> signfile(@RequestHeader("Authorization") String token, @RequestBody SignReq signReq){
        PermCheckerResponse result = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.EXECUTE);
        if(result.isVal()){
            return  apiReqs.signfile(token,signReq);
        }
        else
            return new ResponseEntity<>(result.getMessage(),HttpStatus.FORBIDDEN);
    }
    @PostMapping("/addPerm")

    ResponseEntity<?> addPerm(@RequestHeader("Authorization") String token, @RequestBody AddPermReq req){
        PermCheckerResponse result = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.EXECUTE);
        if(result.isVal()){
            return new ResponseEntity<>(userService.rolePermEkle(req.getRolid(), req.getPermname()).getMessage(),HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(result.getMessage(),HttpStatus.FORBIDDEN);
    }
    @PostMapping("/delPerm")

    ResponseEntity<?> delPerm(@RequestHeader("Authorization") String token, @RequestBody AddPermReq req){
        PermCheckerResponse result = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.EXECUTE);
        if(result.isVal()){
            return new ResponseEntity<>(userService.rolePermCikar(req.getRolid(), req.getPermname()).getMessage(),HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(result.getMessage(),HttpStatus.FORBIDDEN);
    }

    @PostMapping("/addRole")
    ResponseEntity<?> addRole(@RequestHeader("Authorization") String token, @RequestBody AddRoleReq req){
        PermCheckerResponse result = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.EXECUTE);
        if(result.isVal()){
            return new ResponseEntity<>(userService.addRole(req).getMessage(),HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(result.getMessage(),HttpStatus.FORBIDDEN);
    }

    @PostMapping("/delRole")

    ResponseEntity<?> delRole(@RequestHeader("Authorization") String token, @RequestBody AddRoleReq req){
        PermCheckerResponse result = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.EXECUTE);
        if(result.isVal()){
            return new ResponseEntity<>(userService.delRole(req).getMessage(),HttpStatus.OK);
        }
        else
            return new ResponseEntity<>(result.getMessage(),HttpStatus.FORBIDDEN);
    }

    @GetMapping("/find/{word}")
    ResponseEntity<?> find(@RequestHeader("Authorization") String token, @PathVariable String word
    ){
       return  new ResponseEntity<>(" AHANDA BURADA : "+ userService.searchInFolder("uploads/",word,jwtUtils.getUserNameFromJwtToken(token.substring(6))),HttpStatus.OK);

    }



}
