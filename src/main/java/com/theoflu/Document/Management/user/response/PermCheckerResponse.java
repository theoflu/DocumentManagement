package com.theoflu.Document.Management.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PermCheckerResponse {
    private String message;
    private boolean val;
}
