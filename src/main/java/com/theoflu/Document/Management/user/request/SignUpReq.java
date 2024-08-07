package com.theoflu.Document.Management.user.request;

import lombok.Data;

@Data

public class SignUpReq {
    private String username;
    private String password;
    private String email;

    private int rol;
}
