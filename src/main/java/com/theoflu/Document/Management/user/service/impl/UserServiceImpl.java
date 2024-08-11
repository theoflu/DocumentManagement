package com.theoflu.Document.Management.user.service.impl;


import com.theoflu.Document.Management.user.configs.JwtUtils;
import com.theoflu.Document.Management.user.entity.*;
import com.theoflu.Document.Management.user.repository.*;
import com.theoflu.Document.Management.user.request.AddRoleReq;
import com.theoflu.Document.Management.user.request.CreateTeamReq;
import com.theoflu.Document.Management.user.request.UserFavRequest;
import com.theoflu.Document.Management.user.response.FavResponse;
import com.theoflu.Document.Management.user.response.PermCheckerResponse;
import com.theoflu.Document.Management.user.response.statusProcesses;
import com.theoflu.Document.Management.user.service.FileTextSearcher;
import com.theoflu.Document.Management.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private  final FileTextSearcher fileTextSearcher;
    private  final FavRepository favRepository;

    private final JwtUtils jwtUtils;
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
    @Override
    public List<String> searchInFolder(String folderPath, String searchTerm, String username) {
        List<String> list=fileTextSearcher.searchInFolder(folderPath, searchTerm);
        List<String> iznli=new ArrayList<>();
        UserEntity user= findUser(username);
        TeamEntity team= findUserTeam(user);
        for(int i=0;i< list.size();i++){
            FileEntity file= findFile(list.get(0));
            if(file.getTeam().get(0)==team)
            {
                iznli.add(list.get(i));

            }
        }

        //TODO gelen dosyalara kullanıcının erişimi var mı?
        return  iznli;
    }
    @Override
    public statusProcesses rolePermEkle(int roleId, String permName) {
        try {
            ERole erole = getRol(roleId);
            ERolePerm newPerm = ERolePerm.valueOf(permName);
            Role role= roleRepository.findByName(erole).orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));;
            role.getPerms().add(newPerm);
            roleRepository.save(role);
            return new statusProcesses("Perm Başarı ile Eklendi");
        }
        catch (Exception e){
            return new statusProcesses("Eklenirken Hata Oluştu : "+e.getMessage());
        }

    }
    @Override
    public statusProcesses rolePermCikar(int roleId, String permName) {
        try {
            ERole erole = getRol(roleId);
            ERolePerm newPerm = ERolePerm.valueOf(permName);
            Role role= roleRepository.findByName(erole).orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));;
            role.getPerms().remove(newPerm);
            roleRepository.save(role);
            return new statusProcesses("Perm Başarı ile Silindi");
        }
        catch (Exception e){
            return new statusProcesses("Eklenirken Hata Oluştu : "+e.getMessage());
        }

    }
    @Override
    public statusProcesses addRole(AddRoleReq addRoleReq) {
        try{
            UserEntity user = findUser(addRoleReq.getUsername());
            ERole newRoleEnum = getRol(addRoleReq.getRoleId());
            Role newRole = roleRepository.findByName(newRoleEnum)
                    .orElseThrow(() -> new IllegalArgumentException("Id bulunamadı: " + addRoleReq.getRoleId()));
            if (user.getRoles().contains(newRole)) {
                throw new IllegalStateException("Zaten bu role sahip: " + newRoleEnum);
            }
            user.getRoles().add(newRole);
            userepo.save(user);
            return new statusProcesses("Başarı ile gerçekleşti");
        }
        catch (Exception e){
            return new statusProcesses("Başarı ile gerçekleşmedi : " + e.getMessage());
        }

    }

    @Override
    public statusProcesses delRole(AddRoleReq addRoleReq) {
        try{
            UserEntity user = findUser(addRoleReq.getUsername());
            ERole newRoleEnum = getRol(addRoleReq.getRoleId());
            Role newRole = roleRepository.findByName(newRoleEnum)
                    .orElseThrow(() -> new IllegalArgumentException("Id bulunamadı: " + addRoleReq.getRoleId()));
            if (user.getRoles().contains(newRole)) {
                user.getRoles().remove(newRole);
                userepo.save(user);
                return new statusProcesses("Başarı ile gerçekleşti");
            }
            else
                return new statusProcesses("Kullanıcı Böyle bir Role Sahip değil");
        }
        catch (Exception e){
            return new statusProcesses("Başarı ile gerçekleşmedi : " + e.getMessage());
        }

    }

    @Override
    public boolean hasUserAlreadyApproved(FileEntity file, UserEntity user) {
        return file.getUserEntity().contains(user);
    }
    @Override
    public boolean canUserApproveFile(FileEntity file, Role highestRole) {
        return file.getChecked_by_whom().ordinal() >= highestRole.getName().ordinal();
    }
    @Override
    public ResponseEntity<String> approveFileAndRespond(FileEntity file, UserEntity user, Role highestRole) {
        String approvalCheck = approveChecker(file.getChecked_by_whom().ordinal(), file.getFile());

        if (!approvalCheck.equals("110") && file.getChecked_by_whom().ordinal() - 1 == highestRole.getId() - 1) {
            file.setChecked_by_whom(highestRole.getName());

            if (highestRole.getId() == 1) {
                file.set_Approved(true);
            }

            file.getUserEntity().add(user);
            file.setReport("");
            saveFile(file);

            return ResponseEntity.ok(highestRole.getName() + " herkes onayladı.");
        } else if (file.getChecked_by_whom().ordinal() == highestRole.getId() - 1) {
            file.setReport("");
            file.getUserEntity().add(user);
            saveFile(file);

            return ResponseEntity.ok("İstek başarılı şekilde gerçekleşti.");
        }

        return ResponseEntity.ok("Sizden önce onaylaması gerekenler var.");
    }



    @Override
    public String extractUsernameFromToken(String token) {
        return jwtUtils.getUserNameFromJwtToken(token.substring(6));
    }
    @Override
    public boolean hasApprovers(FileEntity file) {
        return file.getUserEntity().size() > 0;
    }
    @Override
    public void removeLastApprover(FileEntity file) {
        List<UserEntity> approvers = file.getUserEntity();
        approvers.remove(approvers.size() - 1);
        file.setUserEntity(approvers);
    }
    @Override
    public boolean isUserInTeam(FileEntity file, UserEntity user) {
        List<UserEntity> users = file.getTeam().get(0).getUserEntities();
        return users.contains(user);
    }
    @Override
    public boolean canUserReportFile(FileEntity file, Role highestRole) {
        return file.getReported_by_whom().ordinal() >= highestRole.getName().ordinal() &&
                file.getReported_by_whom().ordinal() == highestRole.getId();
    }
    @Override
    public void reportFile(FileEntity file, UserEntity user, String reportMessage) {
        Role userRole = getHighestRole(user);
        file.setReport(reportMessage);
        file.setReported_by_whom(getRol(userRole.getId().intValue()));
        file.setChecked_by_whom(getRol(userRole.getId().intValue()));
        saveFile(file);
    }

    @Override
    public boolean isUserInFileTeam(FileEntity file, UserEntity user) {
        List<UserEntity> userEntities = file.getTeam().get(0).getUserEntities();
        return userEntities.contains(user);
    }

    @Override
    public void updateExistingFile(FileEntity existingFile, MultipartFile file, UserEntity user, String fileName) throws IOException {
        Path tempDir = Files.createTempDirectory("");
        File tempFile = new File(tempDir.toFile(), file.getOriginalFilename());
        file.transferTo(tempFile);

        deleteOldFile(fileName);

        saveNewFile(tempFile, fileName);

        updateFileEntity(existingFile, user, fileName);
    }
    @Override
    public void deleteOldFile(String fileName) {
        File oldFile = new File("uploads/" + fileName);
        if (oldFile.exists()) {
            oldFile.delete();
        }
    }
    @Override
    public void saveNewFile(File tempFile, String fileName) throws IOException {
        String uploadDir = "uploads/";
        File dest = new File(uploadDir + fileName);
        Files.copy(tempFile.toPath(), dest.toPath());
    }
    @Override
    public void updateFileEntity(FileEntity existingFile, UserEntity user, String fileName) {
        existingFile.setFile(fileName);
        existingFile.set_Approved(false);
        existingFile.setReport("");
        existingFile.setReported_by_whom(getRol(getHighestRole(user).getId().intValue() - 1));

        List<UserEntity> users = existingFile.getUserEntity();
        users.add(user);
        existingFile.setUserEntity(users);

        saveFile(existingFile);
    }
    @Override
    public FavResponse updateFavFile(String username, UserFavRequest userFavRequest) {
        UserEntity user = findUser(username); // takipleyecek adam
        FileEntity fav = findFile(userFavRequest.getFavFileName());
        UserFavFileEntity favs = favRepository.findByUser(user);

        if (favs.getFileEntities().contains(fav)) {
            favs.getFileEntities().remove(fav);
            favRepository.save(favs);
            return new FavResponse("Yayıncı Takipten Çıkarıldı.");
        } else {
            favs.getFileEntities().add(fav);// favori listesine ekledim
            favRepository.save(favs);
            return new FavResponse("Yayıncı Takip Edildi.");

        }
    }
    @Override
  public List<FileEntity> userFavFiles(String token){
        UserEntity user = findUser(extractUsernameFromToken(token)); // takipleyecek adam
        UserFavFileEntity favs = favRepository.findByUser(user);
        return favs.getFileEntities();
    }
    @Override
    public File saveFileToTempDirectory(MultipartFile file) throws IOException {
        Path tempDir = Files.createTempDirectory("");
        File tempFile = new File(tempDir.toFile(), file.getOriginalFilename());
        file.transferTo(tempFile);
        return tempFile;
    }
    @Override
    public String saveFileToUploadDirectory(File tempFile, String fileName) throws IOException {
        String uploadDir = "uploads/";
        File uploadDirectory = new File(uploadDir);

        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();  // Dizinleri oluştur
        }

        if (fileName == null || fileName.isEmpty()) {
            fileName = tempFile.getName();  // Dosya adı sağlanmadıysa orijinal dosya adını kullan
        }

        String filePath = uploadDir + fileName;
        File dest = new File(filePath);
        Files.copy(tempFile.toPath(), dest.toPath());

        return filePath;
    }
    @Override
    public void saveFileEntity(String username, String fileName) {
        UserEntity user = findUser(username);
        TeamEntity team = findUserTeam(user);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFile(fileName);
        fileEntity.set_Approved(false);
        fileEntity.setReport("");
        fileEntity.setSender(user);
        fileEntity.setChecked_by_whom(getHighestRole(user).getName());
        fileEntity.setReported_by_whom(getRol(getHighestRole(user).getId().intValue() - 1));
        fileEntity.setUserEntity(List.of(user));
        fileEntity.setTeam(List.of(team));

        saveFile(fileEntity);
    }






}
