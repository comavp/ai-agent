package ru.comavp;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailClient {

    private Session session;
    private String emailAddress;

    private static String CONTENT_TYPE = "text/html; charset=utf-8";

    public EmailClient() {
        EmailPropertiesLoader propertiesLoader = new EmailPropertiesLoader();
        session = Session.getInstance(propertiesLoader.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(propertiesLoader.getUserName(), propertiesLoader.getPassword());
            }
        });
        emailAddress = propertiesLoader.getUserName() + "@yandex.ru";
    }

    public void sendEmail(String recipient, String content) {
        try {
            Transport.send(buildMessage(recipient, content));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private Message buildMessage(String recipient, String content) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailAddress));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(content, CONTENT_TYPE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        return message;
    }
}
