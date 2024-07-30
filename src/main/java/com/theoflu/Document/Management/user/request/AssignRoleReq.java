package com.theoflu.Document.Management.user.request;

import com.theoflu.Document.Management.user.entity.ERole;
import com.theoflu.Document.Management.user.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AssignRoleReq {
    private String username ;
    private List<ERole> roles;
}