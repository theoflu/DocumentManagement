package com.theoflu.Document.Management.user.controller;

import com.theoflu.Document.Management.user.FileSigner.ApiReqs;
import com.theoflu.Document.Management.user.configs.JwtUtils;
import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.request.ApproveReq;
import com.theoflu.Document.Management.user.request.CreateTeamReq;
import com.theoflu.Document.Management.user.request.ReportReq;
import com.theoflu.Document.Management.user.request.SignReq;
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
                fileEntity.setSender(user);
                fileEntity.setChecked_by_whom(userService.getHighestRole(user).getName());
                fileEntity.setUserEntity(list);
                fileEntity.setReported_by_whom(ERole.ROLE_Reviewer);
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

    @PostMapping("/download")
    public ResponseEntity<?> downloadFile(FileEntity fileEntity){
        return  null;
    }
    /*
    *   public ResponseEntity<?> approveFile(@RequestHeader("Authorization") String token, @RequestBody ApproveReq req){
        FileEntity file= userService.findFile(req.getFilename());
        UserEntity user=userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));
        List<Role>dene=user.getRoles().stream().toList();
        //rolu aldık
       // List<ERolePerm> a= dene.get(0).getPerms();
        PermCheckerResponse permCheckerResponse= PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)),ERolePerm.APPROVE );
        if(file.is_Approved())
        {

            String approvedbywho =  file.getUserEntity().getUsername();
            return new ResponseEntity<>("Dosya zaten "+ approvedbywho+ " tarafından onaylanmış!", HttpStatus.FORBIDDEN);

        }
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
    *
    *
    * */
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

        if (file.getUserEntity().contains(user)) {
            return ResponseEntity.ok("Sen zaten onayladın.");
        }
        if (file.getChecked_by_whom().ordinal() >= highestRole.getName().ordinal()) {
          String a =  userService.approveChecker(file.getChecked_by_whom().ordinal(),file.getFile()); //hata
          if(!a.equals("110") && (file.getChecked_by_whom().ordinal()-1 == highestRole.getId()-1)){
              file.setChecked_by_whom(highestRole.getName());
              if(highestRole.getId()==1)
                file.set_Approved(true);
              file.getUserEntity().add(user);
              userService.saveFile(file);
              return ResponseEntity.ok(highestRole.getName() +" herkes onayladı.");
          }
          else if(file.getChecked_by_whom().ordinal()==highestRole.getId()-1 )
          {
              file.getUserEntity().add(user);
              userService.saveFile(file);
              return ResponseEntity.ok("İstek başarılı şekilde gerçekleşti.");
          }

          return ResponseEntity.ok("Sizden önce onaylaması gerekenler var.");
        } else {
            return new ResponseEntity<>("Bunu yapmaya yetkin yok!", HttpStatus.FORBIDDEN);
        }
    }

    private void reportCleaner(String username,String filename){
        FileEntity file =userService.findFile(filename);
        file.setReport("");
        file.setReported_by_whom(userService.getHighestRole(userService.findUser(username)).getName());
        // yetkili olan kullanıcılar görecek reportlu dosyayı amma sorumlu rol düzenleyebilecek
        // reportlu dosyaları listele listelenmişlerden kullanıcıyı ilgilendirenleri listele
        // EKİP EKLENECEK

    }
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
    /*
    private  List<UserEntity> deneme(int erole_no, String filename){
       //e role numarası olup onaylayanda olmayan varsa approved yapma eğer yoksa approved yap ve sonrası için onaylama izini ver yavrucum
       FileEntity file = userService.findFile(filename);
       List<UserEntity> onaylayanlar = file.getUserEntity();
       List<UserEntity> users = userService.onaylamasigerekenler() ;
       List<UserEntity> nonMatchingUsers = users.stream()
                .filter(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName().ordinal() == erole_no))
                .toList();
        return nonMatchingUsers;

        /*
        List<UserEntity> users = userService.onaylamasigerekenler() ; //onaylaması gerekenler
       List<String> rolesList = users.stream()
                .flatMap(user -> user.getRoles().stream())
                .map(role -> role.getName().name())
                .distinct()
                .toList();

    }*/
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
    @PostMapping("/report")
    public ResponseEntity<?> reportFile(@RequestHeader("Authorization") String token, @RequestBody ReportReq req){

            PermCheckerResponse permCheckerResponse = userService.PermChecker(jwtUtils.getUserNameFromJwtToken(token.substring(6)), ERolePerm.REPORT);
            UserEntity user = userService.findUser(jwtUtils.getUserNameFromJwtToken(token.substring(6)));
            FileEntity file = userService.findFile(req.getFilename());
            //List<Role> dene = user.getRoles().stream().toList();
            if (file.getReported_by_whom().ordinal() >= userService.getHighestRole(user).getId()-1) {
                if (permCheckerResponse.isVal()) {
                    file.setReport(req.getReport_message());
                    file.set_Approved(false);
                    file.setReported_by_whom(userService.getHighestRole(user).getName());
                    userService.saveFile(file);
                    return ResponseEntity.ok("İşlem tamamdır ");
                } else return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);
            }else return new ResponseEntity<>(permCheckerResponse.getMessage(), HttpStatus.FORBIDDEN);


    }

    @PostMapping("/signfile")
    ResponseEntity<?> signfile(@RequestHeader("Authorization") String token, @RequestBody SignReq signReq){
        return  apiReqs.signfile(token,signReq);

    }



}
