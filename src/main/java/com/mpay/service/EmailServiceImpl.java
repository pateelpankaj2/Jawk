package com.mpay.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${sendgrid.key}")
    private String SENDGRID_API_KEY;

    @Value("${email.fromEmail}")
    private String fromEmail;

    @Async("emailThreadPoolTaskExecutor")
    public void sendTestEmail(String toEmail, String subject, String body) {
        try {
            sendEmail(fromEmail, toEmail, subject, body, null, false);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void sendEmail(String sender, String receipient, String subject, String body, Attachments attachment, boolean addBcc) {
        Email to = new com.sendgrid.helpers.mail.objects.Email();
        Email from = new com.sendgrid.helpers.mail.objects.Email(sender);
        from.setName("Relove");
        Personalization personalization = new Personalization();

        if (receipient.contains(",")) {
            String[] list = receipient.split(",");
            for (String toEmail : list) {
                personalization.addTo(new com.sendgrid.helpers.mail.objects.Email(toEmail));
            }
        } else {
            to.setEmail(receipient);
            personalization.addTo(to);
        }

        // Adding reply-to and bcc
        if (addBcc) {
            com.sendgrid.helpers.mail.objects.Email bcc = new com.sendgrid.helpers.mail.objects.Email();
            //bcc.setEmail(emailBcc);
            personalization.addBcc(bcc);
        }

        //com.sendgrid.helpers.mail.objects.Email replyTo = new com.sendgrid.helpers.mail.objects.Email();
        //replyTo.setEmail(emailReplyTo);

        Content content = new Content("text/plain", body);
        Mail mail = new Mail();
        mail.setFrom(from);
        //mail.setReplyTo(replyTo);
        mail.setSubject(subject);
        mail.addPersonalization(personalization);
        mail.addContent(content);

        if (attachment != null) {
            mail.addAttachments(attachment);
        }

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println(response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

}
