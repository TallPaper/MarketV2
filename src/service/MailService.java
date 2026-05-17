package service;

import model.Musteri;
import java.util.List;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailService {
    
    // SMTP AYARLARI (.env dosyasından yüklenir)
    private static final String SMTP_HOST = util.EnvLoader.getOrDefault("MAIL_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = util.EnvLoader.getOrDefault("MAIL_PORT", "587");
    private static final String SENDER_EMAIL = util.EnvLoader.get("MAIL_SENDER");
    private static final String APP_PASSWORD = util.EnvLoader.get("MAIL_PASSWORD");

    public static void kampanyaDuyurusuGonder(String kampanyaAdi, List<Musteri> musteriler) {
        new Thread(() -> {
            System.out.println("--- Kampanya Duyuru Sistemi Başlatıldı ---");
            int basarili = 0;
            int hatali = 0;

            for (Musteri m : musteriler) {
                if (m.getMail() != null && m.getMail().contains("@")) {
                    boolean sonuc = sendEmail(m.getMail(), kampanyaAdi, 
                        "Sayın " + m.getAdSoyad() + ",\n\n" + 
                        "Yıldız Market'te yeni bir fırsat var: " + kampanyaAdi + "\n" +
                        "Puanlarınızla indirim kazanmayı unutmayın!\n\nİyi alışverişler dileriz.");
                    
                    if (sonuc) basarili++; else hatali++;
                    
                    try { Thread.sleep(1000); } catch (InterruptedException e) {} // Spam filtresine takılmamak için
                }
            }
            System.out.println("--- Duyuru Tamamlandı | Başarılı: " + basarili + " | Hatalı: " + hatali + " ---");
        }).start();
    }
    
    public static void tekilMailGonder(String email, String konu, String icerik) {
        new Thread(() -> {
            sendEmail(email, konu, icerik);
        }).start();
    }

    private static boolean sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Yıldız Market"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Mail Başarıyla Gönderildi: " + to);
            return true;
        } catch (Exception e) {
            System.err.println("Mail Gönderim Hatası (" + to + "): " + e.getMessage());
            return false;
        }
    }
}
