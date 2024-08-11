package com.theoflu.Document.Management.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMailReq {
    private String filename;
    private String sendTo;
    private String whoSend;
    private String message;
    private String subject;
}
