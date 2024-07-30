package com.theoflu.Document.Management.user.configs;


import com.theoflu.Document.Management.user.entity.ERole;
import com.theoflu.Document.Management.user.entity.ERolePerm;
import com.theoflu.Document.Management.user.entity.Role;
import com.theoflu.Document.Management.user.repository.RoleRepository;
import com.theoflu.Document.Management.user.response.CodeResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@AllArgsConstructor
public class RoleConfigs {
    private  final RoleRepository roleRepository;
    @Bean
    public CodeResponse initRoles() {
        if (roleRepository.count() == 0) { // Check if roles already exist
            Role admin = new Role(1L, ERole.ROLE_Admin, List.of(
                    ERolePerm.WRITE,
                    ERolePerm.READ,
                    ERolePerm.DELETE,
                    ERolePerm.UPLOAD,
                    ERolePerm.APPROVE,
                    ERolePerm.ACCESS,
                    ERolePerm.REPORT,
                    ERolePerm.EXECUTE ));
            Role adminAssistant = new Role(2L, ERole.ROLE_AD, List.of(
                    ERolePerm.WRITE,
                    ERolePerm.READ,
                    ERolePerm.UPLOAD,
                    ERolePerm.APPROVE,
                    ERolePerm.ACCESS,
                    ERolePerm.REPORT,
                    ERolePerm.EXECUTE
            ));
            Role document_Manager = new Role(3L, ERole.ROLE_Document_Manager, List.of(
                    ERolePerm.WRITE,
                    ERolePerm.READ,
                    ERolePerm.UPLOAD,
                    ERolePerm.APPROVE,
                    ERolePerm.ACCESS,
                    ERolePerm.REPORT,
                    ERolePerm.EXECUTE

            ));
            Role content_Creator = new Role(4L, ERole.ROLE_Content_Creator, List.of(
                    ERolePerm.WRITE,
                    ERolePerm.READ,
                    ERolePerm.UPLOAD,
                    ERolePerm.APPROVE,
                    ERolePerm.REPORT,
                    ERolePerm.EXECUTE

            ));
            Role reviewer = new Role(5L, ERole.ROLE_Reviewer, List.of(
                    ERolePerm.READ,
                    ERolePerm.APPROVE,
                    ERolePerm.ACCESS,
                    ERolePerm.REPORT

            ));
            Role reader = new Role(6L, ERole.ROLE_Reader, List.of(
                    ERolePerm.READ
            ));
            roleRepository.saveAll(Arrays.asList(admin, adminAssistant, document_Manager, content_Creator,reviewer,reader)); // Save all roles in one call
            return new CodeResponse("ROLLER EKLENDi");
        } else {
            // Optional: Log a message indicating roles already exist
            // (consider using a logger like SLF4J)
            return new CodeResponse("ROLLER EKLENEMEDİ");
        }
    }
}
