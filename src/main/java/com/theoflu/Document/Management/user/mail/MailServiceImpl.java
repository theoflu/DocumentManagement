package com.theoflu.Document.Management.user.mail;

import com.theoflu.Document.Management.user.request.SendMailReq;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {



    private final JavaMailSender mailSender;

    @Override
    public String sendMail( String whoTO,SendMailReq req) {
        SimpleMailMessage message=new SimpleMailMessage();
        message.setFrom("noreply@theoflu.com");
        message.setTo(whoTO);
        message.setText(req.getMessage());
        message.setSubject(req.getSubject());
        mailSender.send(message);
        return "Gönderildi";
    }

    @Override //
    /*
    public String sendMultiMediaMail(File send) throws MessagingException {
        MimeMessage mimeMessage=mailSender.createMimeMessage();
        MimeMessageHelper message=new MimeMessageHelper(mimeMessage,true);
        message.setFrom("noreply@theoflu.com");
        message.setTo("yusuf-oflu61@hotmail.com");
        message.setText("Selamlar Bu Mesaj Size Gönderildi");
        message.setSubject("Sakın Açmaaa!!!!");
        FileSystemResource file=new FileSystemResource(send);
        message.addAttachment("mongo.png",file);
        mailSender.send(mimeMessage);
        return "Gönderildi";
    }


     */
    public String sendMultiMediaMail(File send, SendMailReq req) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true);
        // Gerçek gönderici adresi
        message.setFrom("noreply@theoflu.com");
        // Görünen gönderici adresi (başkası olarak gösterilecek)
        message.setReplyTo(req.getWhoSend()); // Bu, yanıtların gitmesini istediğiniz adres olabilir
        message.setFrom("Theoflu Support <support@theoflu.com>"); // Bu kısımda "Theoflu Support" kısmı görünen isim olacak
        message.setTo(req.getSendTo());
        message.setText(req.getMessage());
        message.setSubject(req.getSubject());
        FileSystemResource file = new FileSystemResource(send);
        message.addAttachment(req.getFilename(), file);
        mailSender.send(mimeMessage);
        return "Gönderildi";
    }
}
