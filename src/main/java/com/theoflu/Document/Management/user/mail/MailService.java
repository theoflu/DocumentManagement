package com.theoflu.Document.Management.user.mail;

import com.theoflu.Document.Management.user.request.SendMailReq;
import jakarta.mail.MessagingException;

import java.io.File;

public interface MailService {
    //String sendMail( String whoTO, String code) ;
    String sendMail( String whoTO, SendMailReq req) ;
    //String sendMultiMediaMail(File send) throws MessagingException;
    String sendMultiMediaMail(File send, SendMailReq req) throws MessagingException;
}
