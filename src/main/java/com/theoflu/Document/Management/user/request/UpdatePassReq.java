package com.theoflu.Document.Management.user.request;

import lombok.Data;

@Data
public class UpdatePassReq {
    private Long id;
    private String password;
    private String code;
}
