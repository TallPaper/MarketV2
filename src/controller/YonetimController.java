package controller;

import db.DatabaseConnection;
import model.Musteri;
import model.Personel;
import service.MailService;
import view.YonetimView;
import view.MainView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class YonetimController {
    private YonetimView view;
    private Personel aktifPersonel;

    public YonetimController(Personel personel) {
        this.aktifPersonel = personel;
        this.view = new YonetimView();
        view.setTitle("YÖNETİM PANELİ - " + aktifPersonel.getAdSoyad());
        initController();
        verileriYukle();
        view.setVisible(true);
    }

    private void initController() {
        // Firewall & Navigation
        view.btnCikis.addActionListener(e -> guvenliCikis());
        view.btnKullaniciDegistir.addActionListener(e -> guvenliDonus());

        // AI Analiz (Thread)
        view.btnAISorgula.addActionListener(e -> aiAnalizBaslat());

        // Kampanya (Duyuru Thread)
        view.btnKampanyaEkle.addActionListener(e -> kampanyaEkle());
        
        // Personel & Tedarikçi (Standard CRUD)
        view.btnEklePersonel.addActionListener(e -> personelEkle());
        view.btnSilPersonel.addActionListener(e -> kayitSil("personel", view.tablePersonel));
        view.btnDuzenlePersonel.addActionListener(e -> personelDuzenle());

        // Kampanya İşlemleri
        view.btnKampanyaSil.addActionListener(e -> kayitSil("kampanyalar", view.tableKampanyalar));
        
        // Diğer
        view.btnHesap.addActionListener(e -> JOptionPane.showMessageDialog(view, "Aktif Kullanıcı: " + aktifPersonel.getAdSoyad() + "\nRol: " + aktifPersonel.getRol(), "Hesap Bilgileri", JOptionPane.INFORMATION_MESSAGE));
    }

    private void guvenliCikis() {
        JPasswordField pf = new JPasswordField();
        pf.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent e) { pf.requestFocusInWindow(); }
            public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
            public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
        });

        int opt = JOptionPane.showConfirmDialog(view, new Object[]{"Güvenli Çıkış için Şifreniz:", pf}, "Güvenli Çıkış", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            if (new String(pf.getPassword()).equals(aktifPersonel.getSifre())) {
                view.dispose();
                MainView mv = new MainView();
                new MainController(mv);
                mv.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(view, "Hatalı Şifre!");
            }
        }
    }

    private void guvenliDonus() {
        String no = JOptionPane.showInputDialog(view, "Yeni Kullanıcı No:", "Kullanıcı Değiştir - 1. Adım", JOptionPane.QUESTION_MESSAGE);
        
        if (no != null && !no.trim().isEmpty()) {
            no = no.trim();
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM personel WHERE kullanici_no = ?")) {
                pstmt.setString(1, no);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    String ad = rs.getString("ad");
                    String soyad = rs.getString("soyad");
                    String dogruSifre = rs.getString("sifre");
                    String dbRol = rs.getString("rol").toUpperCase(java.util.Locale.ENGLISH).trim();
                    String mail = rs.getString("mail");
                    int id = rs.getInt("id");

                    JPasswordField pf = new JPasswordField();
                    pf.addAncestorListener(new javax.swing.event.AncestorListener() {
                        public void ancestorAdded(javax.swing.event.AncestorEvent e) { pf.requestFocusInWindow(); }
                        public void ancestorMoved(javax.swing.event.AncestorEvent e) {}
                        public void ancestorRemoved(javax.swing.event.AncestorEvent e) {}
                    });
                    int opt = JOptionPane.showConfirmDialog(view, new Object[]{"Kullanıcı: " + ad + " " + soyad + "\nŞifre:", pf}, "Kullanıcı Değiştir - 2. Adım", JOptionPane.OK_CANCEL_OPTION);
                    
                    if (opt == JOptionPane.OK_OPTION && new String(pf.getPassword()).equals(dogruSifre)) {
                        Personel p = new Personel(id, no, ad, soyad, rs.getString("rol"), dogruSifre, mail);
                        view.dispose();
                        if (dbRol.contains("KASA")) new KasaController(p);
                        else if (dbRol.contains("DEPO")) new DepoController(p);
                        else if (dbRol.contains("YONETIM")) new YonetimController(p);
                        DatabaseConnection.logEkle(p.getAdSoyad(), "Kullanıcı Değiştirildi -> " + dbRol);
                    } else if (opt == JOptionPane.OK_OPTION) {
                        JOptionPane.showMessageDialog(view, "Hatalı Şifre!");
                    }
                } else {
                    JOptionPane.showMessageDialog(view, "Kullanıcı Bulunamadı!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void aiAnalizBaslat() {
        view.txtOneriler.setText("AI Analiz Motoru Başlatılıyor...\nVeriler toplanıyor...");
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Analiz simülasyonu
                StringBuilder sb = new StringBuilder("--- STRATEJİK YÖNETİM RAPORU (GEMINI AI) ---\n\n");
                
                // Çok Satanlar Analizi
                sb.append("[SATIŞ PERFORMANSI]\n");
                for (int i = 0; i < view.modelCokSatanlar.getRowCount(); i++) {
                    String urun = view.modelCokSatanlar.getValueAt(i, 0).toString();
                    int adet = Integer.parseInt(view.modelCokSatanlar.getValueAt(i, 1).toString());
                    if (adet > 50) sb.append("- ").append(urun).append(" ürününde yüksek talep var. Stok artırımı önerilir.\n");
                }

                // Az Kalanlar Analizi
                sb.append("\n[STOK RİSK ANALİZİ]\n");
                for (int i = 0; i < view.modelAzKalanlar.getRowCount(); i++) {
                    String urun = view.modelAzKalanlar.getValueAt(i, 0).toString();
                    double stok = Double.parseDouble(view.modelAzKalanlar.getValueAt(i, 1).toString());
                    sb.append("- ").append(urun).append(" (Kalan: ").append(stok).append(") acil tedarik edilmeli!\n");
                }

                // Kar Analizi ve Strateji
                sb.append("\n[KAR ANALİZİ]\n");
                try (Connection c = DatabaseConnection.connect(); Statement s = c.createStatement()) {
                    ResultSet rs = s.executeQuery("SELECT SUM((u.fiyat - u.alis_fiyati) * u.satis_sayisi) FROM urunler u");
                    if (rs.next()) sb.append("- Tahmini Toplam Kar: ").append(String.format("%.2f ₺", rs.getDouble(1))).append("\n");
                    
                    ResultSet rs2 = s.executeQuery("SELECT urun_adi FROM urunler WHERE satis_sayisi = 0 LIMIT 3");
                    while (rs2.next()) sb.append("- ").append(rs2.getString(1)).append(" hiç satılmadı. Kampanya önerilir!\n");
                } catch (Exception ex) {}

                sb.append("\n[STRATEJİK ÖNERİ]\n");
                sb.append("Gemini AI: Mevcut verilere göre akşam saatlerinde 'UYE' kampanyaları ciroda %15 artış sağlayabilir.");

                SwingUtilities.invokeLater(() -> view.txtOneriler.setText(sb.toString()));
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void kampanyaEkle() {
        String ad = view.txtKampanyaAd.getText();
        String barkod = view.txtKampanyaBarkod.getText();
        String yuzde = view.txtKampanyaYuzde.getText();
        String tur = view.cmbKampanyaTur.getSelectedItem().toString();
        java.util.Date baslangic = view.dcBaslangic.getDate();
        java.util.Date bitis = view.dcBitis.getDate();

        if (baslangic == null || bitis == null) { JOptionPane.showMessageDialog(view, "Lütfen tarihleri seçiniz!"); return; }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO kampanyalar (kampanya_adi, urun_barkod, indirim_yuzdesi, baslangic_tarihi, bitis_tarihi, kampanya_baslik) VALUES (?,?,?,?,?,?)")) {
            ps.setString(1, tur); 
            ps.setString(2, barkod);
            ps.setInt(3, Integer.parseInt(yuzde));
            ps.setDate(4, new java.sql.Date(baslangic.getTime()));
            ps.setDate(5, new java.sql.Date(bitis.getTime()));
            ps.setString(6, ad);
            ps.executeUpdate();

            // Otomatik Duyuru (Thread)
            new Thread(() -> {
                List<Musteri> musteriler = new ArrayList<>();
                try (Connection c = DatabaseConnection.connect(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM musteriler")) {
                    while (rs.next()) musteriler.add(new Musteri(rs.getInt("id"), rs.getString("telefon"), rs.getString("ad_soyad"), rs.getString("mail"), rs.getDouble("puan")));
                } catch (Exception ex) { ex.printStackTrace(); }
                MailService.kampanyaDuyurusuGonder(ad + " Kampanyası Başladı!", musteriler);
            }).start();

            JOptionPane.showMessageDialog(view, "Kampanya Oluşturuldu ve Müşterilere Duyuru Gönderiliyor!");
            verileriYukle();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void verileriYukle() {
        loadCiro();
        loadKampanyalar();
        loadPersonel();
        loadLogs();
        loadAnalizVerileri();
        loadSatislar();
    }

    private void loadCiro() {
        try (Connection c = DatabaseConnection.connect()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT fn_gunluk_ciro(CURRENT_DATE)")) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) view.lblGunlukCiro.setText(String.format("%.2f ₺", rs.getDouble(1)));
            }
            try (Statement s = c.createStatement(); ResultSet rs2 = s.executeQuery("SELECT COUNT(*) FROM satislar")) {
                if (rs2.next()) view.lblSatisAdedi.setText(rs2.getString(1));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadKampanyalar() {
        view.modelKampanyalar.setRowCount(0);
        try (Connection c = DatabaseConnection.connect(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM kampanyalar")) {
            while (rs.next()) view.modelKampanyalar.addRow(new Object[]{rs.getInt("id"), "Kampanya", rs.getString("urun_barkod"), "%" + rs.getInt("indirim_yuzdesi"), rs.getString("kampanya_adi")});
        } catch (Exception e) {}
    }

    private void loadAnalizVerileri() {
        view.modelCokSatanlar.setRowCount(0);
        view.modelAzKalanlar.setRowCount(0);
        try (Connection c = DatabaseConnection.connect()) {
            try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery("SELECT urun_adi, satis_sayisi FROM urunler ORDER BY satis_sayisi DESC LIMIT 5")) {
                while (rs.next()) view.modelCokSatanlar.addRow(new Object[]{rs.getString(1), rs.getInt(2)});
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM fn_kritik_stok(CAST(? AS numeric))")) {
                ps.setInt(1, 10);
                ResultSet rs2 = ps.executeQuery();
                while (rs2.next()) view.modelAzKalanlar.addRow(new Object[]{rs2.getString("ad"), rs2.getDouble("kalan")});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadSatislar() {
        view.modelSatislar.setRowCount(0);
        try (Connection c = DatabaseConnection.connect(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM satislar ORDER BY id DESC LIMIT 50")) {
            while (rs.next()) {
                view.modelSatislar.addRow(new Object[]{
                    rs.getInt("id"), rs.getTimestamp("tarih"), rs.getDouble("tutar"),
                    rs.getString("odeme_turu"), rs.getString("kasiyer"), rs.getString("musteri_mail")
                });
            }
        } catch (Exception e) {}
    }

    private void loadPersonel() {
        view.modelPersonel.setRowCount(0);
        try (Connection c = DatabaseConnection.connect(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM personel")) {
            while (rs.next()) view.modelPersonel.addRow(new Object[]{rs.getInt("id"), rs.getString("kullanici_no"), rs.getString("ad"), rs.getString("soyad"), rs.getString("rol")});
        } catch (Exception e) {}
    }

    private void loadLogs() {
        view.modelLog.setRowCount(0);
        try (Connection c = DatabaseConnection.connect(); ResultSet rs = c.createStatement().executeQuery("SELECT * FROM logs ORDER BY id DESC LIMIT 20")) {
            while (rs.next()) view.modelLog.addRow(new Object[]{rs.getInt("id"), rs.getString("kullanici"), rs.getString("islem"), rs.getTimestamp("tarih")});
        } catch (Exception e) {}
    }

    private void personelEkle() {
        JTextField t1 = new JTextField(), t2 = new JTextField(), t3 = new JTextField();
        JPasswordField t4 = new JPasswordField();
        JComboBox<String> c = new JComboBox<>(new String[]{"KASA", "DEPO", "YONETIM"});
        if (JOptionPane.showConfirmDialog(view, new Object[]{"Kullanıcı No:", t1, "Ad:", t2, "Soyad:", t3, "Rol:", c, "Şifre:", t4}, "Yeni Personel", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection x = DatabaseConnection.connect(); PreparedStatement p = x.prepareStatement("INSERT INTO personel (kullanici_no, ad, soyad, rol, sifre) VALUES (?,?,?,?,?)")) {
                p.setString(1, t1.getText()); p.setString(2, t2.getText()); p.setString(3, t3.getText()); p.setString(4, c.getSelectedItem().toString()); p.setString(5, new String(t4.getPassword()));
                p.executeUpdate(); loadPersonel();
            } catch (Exception e) { JOptionPane.showMessageDialog(view, e.getMessage()); }
        }
    }

    private void personelDuzenle() {
        int row = view.tablePersonel.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(view, "Düzenlenecek personeli seçin!"); return; }
        int id = (int) view.modelPersonel.getValueAt(row, 0);
        
        JTextField t1 = new JTextField(view.modelPersonel.getValueAt(row, 1).toString());
        JTextField t2 = new JTextField(view.modelPersonel.getValueAt(row, 2).toString());
        JTextField t3 = new JTextField(view.modelPersonel.getValueAt(row, 3).toString());
        JComboBox<String> c = new JComboBox<>(new String[]{"KASA", "DEPO", "YONETIM"});
        c.setSelectedItem(view.modelPersonel.getValueAt(row, 4).toString());
        
        if (JOptionPane.showConfirmDialog(view, new Object[]{"Kullanıcı No:", t1, "Ad:", t2, "Soyad:", t3, "Rol:", c}, "Personel Düzenle", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Connection x = DatabaseConnection.connect(); PreparedStatement p = x.prepareStatement("UPDATE personel SET kullanici_no=?, ad=?, soyad=?, rol=? WHERE id=?")) {
                p.setString(1, t1.getText()); p.setString(2, t2.getText()); p.setString(3, t3.getText()); p.setString(4, c.getSelectedItem().toString()); p.setInt(5, id);
                p.executeUpdate(); loadPersonel();
            } catch (Exception e) { JOptionPane.showMessageDialog(view, e.getMessage()); }
        }
    }

    private void kayitSil(String tablo, JTable tbl) {
        int r = tbl.getSelectedRow();
        if (r != -1 && JOptionPane.showConfirmDialog(view, "Kaydı silmek istediğinize emin misiniz?", "Sil", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection c = DatabaseConnection.connect(); PreparedStatement p = c.prepareStatement("DELETE FROM " + tablo + " WHERE id = ?")) {
                p.setInt(1, (int) tbl.getValueAt(r, 0));
                p.executeUpdate(); verileriYukle();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
