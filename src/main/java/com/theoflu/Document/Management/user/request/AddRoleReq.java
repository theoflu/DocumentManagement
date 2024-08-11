package com.theoflu.Document.Management.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddRoleReq {

    private String username;
    private int roleId;
}
