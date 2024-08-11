package com.theoflu.Document.Management.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddPermReq {
    private  int rolid;
    private String permname;
}
