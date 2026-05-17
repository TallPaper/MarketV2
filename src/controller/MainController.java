package controller;

import db.DatabaseConnection;
import model.Personel;
import view.MainView;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;

public class MainController {
    private MainView view;

    public MainController(MainView view) {
        this.view = view;
        this.view.addLoginListener(new LoginListener());
    }

    class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton tiklananButon = (JButton) e.getSource();
            String butonMetni = tiklananButon.getText();

            String hedefBolum = butonMetni.toUpperCase(Locale.ENGLISH)
                    .replace("Ö", "O")
                    .replace("Ç", "C")
                    .replace("Ş", "S")
                    .replace("İ", "I")
                    .replace("Ğ", "G")
                    .replace("Ü", "U")
                    .trim();

            girisIslemiBaslat(hedefBolum);
        }
    }

    private void girisIslemiBaslat(String hedefBolum) {
        String kullaniciNo = JOptionPane.showInputDialog(view, "Kullanıcı No Giriniz:", "Sisteme Giriş - 1. Adım", JOptionPane.QUESTION_MESSAGE);
        
        if (kullaniciNo != null && !kullaniciNo.trim().isEmpty()) {
            kullaniciNo = kullaniciNo.trim();
            // Kullanıcı Var mı Kontrol Et
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("SELECT ad, soyad FROM personel WHERE kullanici_no = ?")) {
                ps.setString(1, kullaniciNo);
                ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String isim = rs.getString("ad") + " " + rs.getString("soyad");
                    JPasswordField pf = new JPasswordField();
                    pf.addAncestorListener(new javax.swing.event.AncestorListener() {
                        public void ancestorAdded(javax.swing.event.AncestorEvent e) { pf.requestFocusInWindow(); }
                        public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
                        public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
                    });
                    
                    int ok = JOptionPane.showConfirmDialog(view, new Object[]{"Hoşgeldiniz " + isim + "\nŞifreniz:", pf}, "Sisteme Giriş - 2. Adım", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    
                    if (ok == JOptionPane.OK_OPTION) {
                        String sifre = new String(pf.getPassword());
                        dogrulaVeGirisYap(kullaniciNo, sifre, hedefBolum);
                    }
                } else {
                    JOptionPane.showMessageDialog(view, "Kullanıcı Bulunamadı!");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void dogrulaVeGirisYap(String no, String sifre, String hedefRol) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM personel WHERE kullanici_no = ? AND sifre = ?")) {

            pstmt.setString(1, no);
            pstmt.setString(2, sifre);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String dbRol = rs.getString("rol").toUpperCase(Locale.ENGLISH).trim();

                if (dbRol.equals(hedefRol) || dbRol.equals("YONETIM")) {
                    String ad = rs.getString("ad");
                    String soyad = rs.getString("soyad");
                    String mail = rs.getString("mail");
                    
                    Personel personel = new Personel(rs.getInt("id"), rs.getString("kullanici_no"), ad, soyad, rs.getString("rol"), rs.getString("sifre"), mail);

                    view.dispose();

                    if (hedefRol.contains("KASA")) {
                        new KasaController(personel);
                    } else if (hedefRol.contains("DEPO")) {
                        new DepoController(personel);
                    } else if (hedefRol.contains("YONETIM")) {
                        new YonetimController(personel);
                    }
                    DatabaseConnection.logEkle(personel.getAdSoyad(), "Sisteme Giriş Yapıldı: " + hedefRol);
                } else {
                    JOptionPane.showMessageDialog(view, "Yetkiniz Yok! Bölüm: " + hedefRol + ", Sizin Rolünüz: " + dbRol, "Yetki Hatası", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(view, "Hatalı Kullanıcı No veya Şifre!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(view, "Bağlantı Hatası: " + ex.getMessage());
        }
    }
}
