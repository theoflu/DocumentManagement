package com.theoflu.Document.Management.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignReq {
    private String username;
    private String filename;
}
