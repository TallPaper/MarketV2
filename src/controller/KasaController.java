package controller;

import db.DatabaseConnection;
import model.Musteri;
import model.Personel;
import view.KasaView;
import view.MainView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class KasaController {
    private KasaView view;
    private double genelToplam = 0.0;
    private Personel aktifPersonel;
    private Musteri aktifMusteri = null;

    public KasaController(Personel personel) {
        this.aktifPersonel = personel;
        this.view = new KasaView();
        view.setTitle("KASA - Kasiyer: " + aktifPersonel.getAdSoyad());
        initController();
        hizliMenuYukle();
        view.setVisible(true);
        SwingUtilities.invokeLater(() -> view.txtBarkod.requestFocusInWindow());
    }

    private void hizliMenuYukle() {
        String[][] urunTanımları = {
            {"EKMEK", "🍞"},
            {"SÜT", "🥛"},
            {"SU", "💧"},
            {"DOMATES", "🍅"},
            {"ELMA", "🍎"},
            {"YUMURTA", "🥚"},
            {"GAZETE", "📰"},
            {"POŞET", "🛍️"}
        };

        for (String[] tanim : urunTanımları) {
            String urunAdi = tanim[0];
            String ikon = tanim[1];
            
            String btnText = "<html><div style='width: 55px; text-align: center; font-family: Segoe UI;'>" +
                             "<div style='font-size: 20px; line-height: 1;'>" + ikon + "</div>" +
                             "<div style='font-size: 9px; font-weight: bold; color: #2c3e50; margin-top: 2px;'>" + urunAdi + "</div>" +
                             "</div></html>";
            
            JButton btn = new JButton(btnText);
            Dimension size = new Dimension(75, 75);
            btn.setPreferredSize(size);
            btn.setMinimumSize(size);
            btn.setMaximumSize(size);
            
            btn.setVerticalAlignment(SwingConstants.CENTER);
            btn.setHorizontalAlignment(SwingConstants.CENTER);
            btn.setMargin(new Insets(2, 2, 2, 2));
            
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(44, 62, 80));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
            ));
            
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                String bulunanBarkod = barkodBulByName(urunAdi);
                if (bulunanBarkod != null) {
                    view.txtBarkod.setText(bulunanBarkod);
                    urunEkle();
                } else {
                    JOptionPane.showMessageDialog(view, urunAdi + " ürünü sistemde bulunamadı!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            // Hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(new Color(245, 245, 245));
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(39, 174, 96), 2),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                }
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(Color.WHITE);
                    btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(2, 2, 2, 2)
                    ));
                }
            });

            view.pnlQuickItems.add(btn);
            view.pnlQuickItems.add(Box.createRigidArea(new Dimension(10, 0)));
        }
    }

    private String barkodBulByName(String name) {
        String query = "SELECT barkod FROM urunler WHERE urun_adi ILIKE ? LIMIT 1";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("barkod");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initController() {
        // Numpad & Input
        view.txtBarkod.addActionListener(e -> urunEkle());
        for (JButton btn : view.btnNumpad) {
            btn.addActionListener(e -> {
                String tus = btn.getText().trim().toUpperCase(java.util.Locale.ENGLISH);
                if (tus.equals("C")) { view.txtBarkod.setText(""); }
                else if (tus.startsWith("GİR") || tus.startsWith("GIR")) { urunEkle(); }
                else { view.txtBarkod.setText(view.txtBarkod.getText() + btn.getText()); }
                view.txtBarkod.requestFocus();
            });
        }

        // Güvenli Çıkış & Kullanıcı Değiştirme (Firewall)
        view.btnCikis.addActionListener(e -> guvenliCikis());
        view.btnKullaniciDegistir.addActionListener(e -> guvenliDonus());

        // Müşteri İşlemleri (Loyalty Engine)
        view.btnMusteriBul.addActionListener(e -> musteriSorgula());

        // Satış & İade
        view.btnSatisNakit.addActionListener(e -> satisBitir("NAKİT"));
        view.btnSatisKart.addActionListener(e -> satisBitir("KREDİ KARTI"));
        view.btnUrunCikar.addActionListener(e -> {
            int row = view.tableSepet.getSelectedRow();
            if (row != -1) { view.model.removeRow(row); hesaplaGenelToplam(); }
        });
        view.btnIptal.addActionListener(e -> sepetiTemizle());
        view.btnIade.addActionListener(e -> iadeIslemiBaslat());
        view.btnHesap.addActionListener(e -> JOptionPane.showMessageDialog(view, "Kasiyer: " + aktifPersonel.getAdSoyad() + "\nBölüm: KASA", "Kullanıcı Bilgisi", JOptionPane.INFORMATION_MESSAGE));

        view.tableSepet.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) miktarGuncelle(); }
        });
    }

    // --- KATMAN 1: GÜVENLİ NAVİGASYON ---
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

    // --- KATMAN 2: MÜŞTERİ SADAKAT VE PUAN SİSTEMİ ---
    private void musteriSorgula() {
        String tel = view.txtMusteriTelefon.getText().replaceAll("[^0-9]", "");
        if (tel.length() < 10) { JOptionPane.showMessageDialog(view, "Geçersiz Telefon Formatı!"); return; }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM musteriler WHERE telefon = ?")) {
            ps.setString(1, tel);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                aktifMusteri = new Musteri(rs.getInt("id"), rs.getString("telefon"), rs.getString("ad_soyad"), rs.getString("mail"), rs.getDouble("puan"));
                view.lblMusteriAd.setText("Müşteri: " + aktifMusteri.getAdSoyad() + " (Puan: " + String.format("%.2f", aktifMusteri.getPuan()) + ")");
                view.lblMusteriAd.setForeground(new Color(39, 174, 96));
                sepetiYenidenHesapla();
            } else {
                musteriKayitPenceresi(tel);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sepetiYenidenHesapla() {
        if (view.model.getRowCount() == 0) return;

        try (Connection conn = DatabaseConnection.connect()) {
            for (int i = 0; i < view.model.getRowCount(); i++) {
                String barkod = view.model.getValueAt(i, 0).toString();
                double miktar = Double.parseDouble(view.model.getValueAt(i, 2).toString());

                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM urunler WHERE barkod = ?")) {
                    ps.setString(1, barkod);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String ad = rs.getString("urun_adi");
                        double fiyat = rs.getDouble("fiyat");

                        try (PreparedStatement psK = conn.prepareStatement("SELECT * FROM kampanyalar WHERE urun_barkod = ? AND CURRENT_DATE BETWEEN baslangic_tarihi AND bitis_tarihi")) {
                            psK.setString(1, barkod);
                            ResultSet rsK = psK.executeQuery();
                            if (rsK.next()) {
                                String tur = rsK.getString("kampanya_adi");
                                double indirim = rsK.getDouble("indirim_yuzdesi") / 100.0;

                                if (tur.equals("UYE") && aktifMusteri != null) {
                                    fiyat -= (fiyat * indirim);
                                    ad += " (Uye Ind.)";
                                } else if (tur.equals("IKISI") && aktifMusteri != null) {
                                    fiyat -= (fiyat * indirim * 1.5);
                                    ad += " (Sadakat İnd.)";
                                } else if (!tur.equals("UYE") && !tur.equals("IKISI")) {
                                    fiyat -= (fiyat * indirim);
                                    ad += " (Kampanyalı)";
                                }
                                fiyat = Math.round(fiyat * 100.0) / 100.0;
                            }
                        }
                        
                        view.model.setValueAt(ad, i, 1);
                        view.model.setValueAt(fiyat, i, 4);
                        view.model.setValueAt(miktar * fiyat, i, 5);
                    }
                }
            }
            hesaplaGenelToplam();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void musteriKayitPenceresi(String tel) {
        JTextField txtAd = new JTextField();
        JTextField txtMail = new JTextField();
        Object[] fields = { "Numara Bulunamadı. Yeni Kayıt:", "Ad Soyad:", txtAd, "E-Mail:", txtMail };
        
        int opt = JOptionPane.showConfirmDialog(view, fields, "Müşteri Kayıt", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String ad = txtAd.getText().trim();
            String mail = txtMail.getText().trim();
            
            // Regex Mail Kontrolü
            String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
            if (ad.isEmpty() || !Pattern.matches(regex, mail) || !mail.contains(".")) {
                JOptionPane.showMessageDialog(view, "Hata: Geçersiz Ad veya Mail formatı!");
                return;
            }

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO musteriler (telefon, ad_soyad, mail, puan) VALUES (?,?,?,0) RETURNING id")) {
                ps.setString(1, tel); ps.setString(2, ad); ps.setString(3, mail);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    aktifMusteri = new Musteri(rs.getInt("id"), tel, ad, mail, 0);
                    view.lblMusteriAd.setText("Müşteri: " + ad + " (Puan: 0.00)");
                    sepetiYenidenHesapla();
                    JOptionPane.showMessageDialog(view, "Müşteri Başarıyla Kaydedildi!");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // --- KATMAN 3: KASA VE TRANSAKSİYON GÜVENLİĞİ ---
    private void urunEkle() {
        String barkod = view.txtBarkod.getText().trim();
        double miktar = 1.0;
        try { miktar = Double.parseDouble(view.txtMiktar.getText().replace(",", ".")); } catch (Exception e) {}

        if (barkod.isEmpty()) return;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM urunler WHERE barkod = ?")) {
            ps.setString(1, barkod);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String ad = rs.getString("urun_adi");
                double fiyat = rs.getDouble("fiyat");
                double mevcutStok = rs.getDouble("stok");

                if (miktar > mevcutStok) {
                    JOptionPane.showMessageDialog(view, "Yetersiz Stok! Mevcut: " + mevcutStok);
                    return;
                }
                
                // Üçlü Kampanya Mantığı
                PreparedStatement psK = conn.prepareStatement("SELECT * FROM kampanyalar WHERE urun_barkod = ? AND CURRENT_DATE BETWEEN baslangic_tarihi AND bitis_tarihi");
                psK.setString(1, barkod);
                ResultSet rsK = psK.executeQuery();
                if (rsK.next()) {
                    String tur = rsK.getString("kampanya_adi");
                    double indirim = rsK.getDouble("indirim_yuzdesi") / 100.0;
                    
                    if (tur.equals("UYE") && aktifMusteri == null) { /* İndirim yok */ }
                    else if (tur.equals("IKISI") && aktifMusteri != null) { 
                        fiyat -= (fiyat * indirim * 1.5);
                        ad += " (Sadakat İnd.)";
                    } else {
                        fiyat -= (fiyat * indirim);
                        ad += " (Kampanyalı)";
                    }
                    fiyat = Math.round(fiyat * 100.0) / 100.0; // Yuvarlama
                }
                
                view.model.addRow(new Object[]{barkod, ad, miktar, rs.getString("birim"), fiyat, miktar * fiyat});
                hesaplaGenelToplam();
                view.txtBarkod.setText(""); view.txtMiktar.setText("1");
                view.txtBarkod.requestFocus();
            } else {
                JOptionPane.showMessageDialog(view, "Ürün Bulunamadı!");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void satisBitir(String tur) {
        if (view.model.getRowCount() == 0) return;

        double nakitAlinan = 0.0;
        double paraUstu = 0.0;
        double kullanilanPuan = 0.0;

        // Puan Kullanma Seçeneği
        if (aktifMusteri != null && aktifMusteri.getPuan() > 0) {
            int secim = JOptionPane.showConfirmDialog(view, "Müşterinin " + String.format("%.2f", aktifMusteri.getPuan()) + " puanı var. İndirim olarak kullanılsın mı?", "Puan Kullanımı", JOptionPane.YES_NO_OPTION);
            if (secim == JOptionPane.YES_OPTION) {
                kullanilanPuan = Math.min(genelToplam, aktifMusteri.getPuan());
                genelToplam -= kullanilanPuan;
                JOptionPane.showMessageDialog(view, String.format("%.2f TL puan indirimi uygulandı.", kullanilanPuan));
            }
        }

        if (tur.equals("NAKİT")) {
            String girilen = JOptionPane.showInputDialog(view, "Toplam: " + String.format("%.2f", genelToplam) + " TL\nAlınan Nakit:");
            if (girilen == null) return;
            try {
                nakitAlinan = Double.parseDouble(girilen.replace(",", "."));
                if (nakitAlinan < genelToplam) { JOptionPane.showMessageDialog(view, "HATA: Alınan miktar toplamdan küçük!"); return; }
                paraUstu = nakitAlinan - genelToplam;
            } catch (Exception e) { return; }
        }

        // DB İşlemleri
        try (Connection conn = DatabaseConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement psS = conn.prepareStatement("INSERT INTO satislar (tutar, odeme_turu, kasiyer, musteri_mail) VALUES (?,?,?,?) RETURNING id");
                psS.setDouble(1, genelToplam); psS.setString(2, tur); psS.setString(3, aktifPersonel.getAdSoyad());
                psS.setString(4, aktifMusteri != null ? aktifMusteri.getMail() : "");
                ResultSet rsS = psS.executeQuery();
                int satisId = 0; if (rsS.next()) satisId = rsS.getInt(1);

                for (int i = 0; i < view.model.getRowCount(); i++) {
                    String barkod = view.model.getValueAt(i, 0).toString();
                    double miktar = Double.parseDouble(view.model.getValueAt(i, 2).toString());
                    double toplam = Double.parseDouble(view.model.getValueAt(i, 5).toString());

                    PreparedStatement psD = conn.prepareStatement("INSERT INTO satis_detay (satis_id, barkod, urun_adi, miktar, fiyat, toplam) VALUES (?,?,?,?,?,?)");
                    psD.setInt(1, satisId);
                    psD.setString(2, barkod);
                    psD.setString(3, view.model.getValueAt(i, 1).toString());
                    psD.setDouble(4, miktar);
                    psD.setDouble(5, Double.parseDouble(view.model.getValueAt(i, 4).toString()));
                    psD.setDouble(6, toplam);
                    psD.executeUpdate();

                }

                // Puan Güncelleme (%1 Kazanım)
                if (aktifMusteri != null) {
                    double kazanilanPuan = genelToplam * 0.01;
                    double yeniPuan = aktifMusteri.getPuan() - kullanilanPuan + kazanilanPuan;
                    PreparedStatement psP = conn.prepareStatement("UPDATE musteriler SET puan = ? WHERE id = ?");
                    psP.setDouble(1, yeniPuan); psP.setInt(2, aktifMusteri.getId());
                    psP.executeUpdate();
                    aktifMusteri.setPuan(yeniPuan);
                }

                conn.commit();
                fisGoster(tur, nakitAlinan, paraUstu, satisId);
                sepetiTemizle();
            } catch (Exception ex) { conn.rollback(); throw ex; }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void iadeIslemiBaslat() {
        String fisNo = JOptionPane.showInputDialog(view, "İade edilecek Fiş No:");
        if (fisNo == null || fisNo.isEmpty()) return;

        int islemId;
        try {
            islemId = Integer.parseInt(fisNo.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Geçersiz Fiş Numarası!");
            return;
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM satis_detay WHERE satis_id = ? AND iade_edildi = FALSE")) {
            ps.setInt(1, islemId);
            ResultSet rs = ps.executeQuery();
            
            DefaultTableModel iadeModel = new DefaultTableModel(new String[]{"ID", "Barkod", "Ürün", "Miktar", "Toplam"}, 0);
            while (rs.next()) {
                iadeModel.addRow(new Object[]{rs.getInt("id"), rs.getString("barkod"), rs.getString("urun_adi"), rs.getDouble("miktar"), rs.getDouble("toplam")});
            }

            if (iadeModel.getRowCount() == 0) { JOptionPane.showMessageDialog(view, "İade edilecek ürün bulunamadı veya zaten iade edilmiş."); return; }

            JTable table = new JTable(iadeModel);
            int secim = JOptionPane.showConfirmDialog(view, new JScrollPane(table), "İade Edilecek Ürünü Seçin", JOptionPane.OK_CANCEL_OPTION);
            if (secim == JOptionPane.OK_OPTION && table.getSelectedRow() != -1) {
                int id = (int) iadeModel.getValueAt(table.getSelectedRow(), 0);
                String barkod = iadeModel.getValueAt(table.getSelectedRow(), 1).toString();
                double miktar = (double) iadeModel.getValueAt(table.getSelectedRow(), 3);
                double tutar = (double) iadeModel.getValueAt(table.getSelectedRow(), 4);

                // Mükerrer İade Engeli (Zaten sorguda engelledik ama garantiye alıyoruz)
                PreparedStatement psU = conn.prepareStatement("UPDATE satis_detay SET iade_edildi = TRUE WHERE id = ?");
                psU.setInt(1, id);
                psU.executeUpdate();

                JOptionPane.showMessageDialog(view, String.format("%.2f TL İade İşlemi Başarılı!", tutar));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void fisGoster(String tur, double alinan, double paraUstu, int id) {
        StringBuilder sb = new StringBuilder();
        sb.append("      YILDIZ MARKET FİŞİ      \n");
        sb.append("==============================\n");
        sb.append("Tarih: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())).append("\n");
        sb.append("Fiş No: ").append(id).append("\n");
        sb.append("Kasiyer: ").append(aktifPersonel.getAdSoyad()).append("\n");
        if (aktifMusteri != null) sb.append("Müşteri: ").append(aktifMusteri.getAdSoyad()).append("\n");
        sb.append("------------------------------\n");
        sb.append(String.format("%-15s %10s\n", "ÜRÜN", "TUTAR"));
        for (int i = 0; i < view.model.getRowCount(); i++) {
            sb.append(String.format("%-15s %10.2f\n", view.model.getValueAt(i, 1), (double)view.model.getValueAt(i, 5)));
        }
        sb.append("==============================\n");
        sb.append(String.format("GENEL TOPLAM: %14.2f TL\n", genelToplam));
        if (tur.equals("NAKİT")) {
            sb.append(String.format("ALINAN:       %14.2f TL\n", alinan));
            sb.append(String.format("PARA ÜSTÜ:    %14.2f TL\n", paraUstu));
        } else {
            sb.append("ÖDEME: KREDİ KARTI\n");
        }
        sb.append("==============================\n");
        sb.append("   BİZİ TERCİH ETTİĞİNİZ İÇİN \n        TEŞEKKÜRLER!   \n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JOptionPane.showMessageDialog(view, new JScrollPane(area), "Termal Fiş Çıktısı", JOptionPane.PLAIN_MESSAGE);
    }

    private void hesaplaGenelToplam() {
        genelToplam = 0;
        for (int i = 0; i < view.model.getRowCount(); i++) genelToplam += (double) view.model.getValueAt(i, 5);
        view.lblGenelToplam.setText(String.format("%.2f ₺", genelToplam));
    }

    private void sepetiTemizle() {
        view.model.setRowCount(0); hesaplaGenelToplam();
        aktifMusteri = null; view.lblMusteriAd.setText("Müşteri: Kayıtlı Değil"); view.txtMusteriTelefon.setText("");
    }

    private void miktarGuncelle() { /* Opsiyonel miktar güncelleme */ }
}
