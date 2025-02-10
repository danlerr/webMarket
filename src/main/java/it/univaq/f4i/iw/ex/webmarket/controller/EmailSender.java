package it.univaq.f4i.iw.ex.webmarket.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

    /**
     * Invia una email HTML semplice con il testo specificato.
     * 
     * @param session La sessione di posta configurata
     * @param toEmail L'indirizzo email del destinatario
     * @param subject L'oggetto della email
     * @param body Il corpo del messaggio (in formato HTML)
     */
    public static void sendEmail(Session session, String toEmail, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            // Imposta gli header della email
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            // Imposta il mittente e il reply-to
            msg.setFrom(new InternetAddress("coquette.webmarket@gmail.com", "coquette"));
            msg.setReplyTo(InternetAddress.parse("coquette.webmarket@gmail.com", false));

            // Imposta l'oggetto e il corpo della email
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");

            // Imposta la data di invio e il destinatario
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

            // Invia il messaggio
            Transport.send(msg);
            System.out.println("Email inviata con successo!");
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }
    
     // Metodo per creare e restituire una sessione email configurata
     public static Session getEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.outlook.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new javax.mail.Authenticator() {
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication("webmarket.univaq@outlook.com", "your_password_here");
            }
        });
    }

    
}
