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
import java.util.List;
import java.util.ArrayList;

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
                
                // Check if product already exists in the cart (sepet)
                int existingRow = -1;
                for (int i = 0; i < view.model.getRowCount(); i++) {
                    if (view.model.getValueAt(i, 0).toString().equals(barkod)) {
                        existingRow = i;
                        break;
                    }
                }

                if (existingRow != -1) {
                    Object existingMiktarObj = view.model.getValueAt(existingRow, 2);
                    double eskiMiktar = (existingMiktarObj instanceof Number) ? ((Number) existingMiktarObj).doubleValue() : Double.parseDouble(existingMiktarObj.toString());
                    double yeniMiktar = eskiMiktar + miktar;

                    if (yeniMiktar > mevcutStok) {
                        JOptionPane.showMessageDialog(view, "Yetersiz Stok! Mevcut: " + mevcutStok);
                        return;
                    }

                    view.model.setValueAt(yeniMiktar, existingRow, 2);
                    view.model.setValueAt(yeniMiktar * fiyat, existingRow, 5);
                } else {
                    view.model.addRow(new Object[]{barkod, ad, miktar, rs.getString("birim"), fiyat, miktar * fiyat});
                }
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
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM satis_detay WHERE satis_id = ? ORDER BY id ASC")) {
            ps.setInt(1, islemId);
            ResultSet rs = ps.executeQuery();
            
            DefaultTableModel iadeModel = new DefaultTableModel(
                new String[]{"ID", "Barkod", "Ürün", "Satın Alınan", "İade Edilen", "Kalan", "Birim Fiyat", "Kalan Tutar", "İade", "Zatenİade"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 8) return Boolean.class;
                    if (columnIndex == 9) return Boolean.class;
                    return super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                    if (column == 8) {
                        Boolean zatenIade = (Boolean) getValueAt(row, 9);
                        return zatenIade != null && !zatenIade;
                    }
                    return false;
                }
            };

            while (rs.next()) {
                boolean zatenIade = rs.getBoolean("iade_edildi");
                double miktar = rs.getDouble("miktar");
                double fiyat = rs.getDouble("fiyat");
                double iadeMiktari = rs.getDouble("iade_miktari");
                double kalan = miktar - iadeMiktari;
                double kalanTutar = kalan * fiyat;
                
                if (kalan <= 0) {
                    zatenIade = true;
                }
                
                String urunAdi = rs.getString("urun_adi");
                if (zatenIade) {
                    urunAdi += " (İADE EDİLDİ)";
                }
                iadeModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("barkod"),
                    urunAdi,
                    miktar,
                    iadeMiktari,
                    kalan,
                    fiyat,
                    kalanTutar,
                    false,
                    zatenIade
                });
            }

            if (iadeModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(view, "Bu numaraya ait bir satış kaydı bulunamadı.");
                return;
            }

            JTable table = new JTable(iadeModel) {
                @Override
                public java.awt.Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    java.awt.Component c = super.prepareRenderer(renderer, row, column);
                    int modelRow = convertRowIndexToModel(row);
                    Boolean zatenIade = (Boolean) getModel().getValueAt(modelRow, 9);
                    
                    if (zatenIade != null && zatenIade) {
                        c.setForeground(java.awt.Color.GRAY);
                        c.setBackground(new java.awt.Color(240, 240, 240));
                        if (c instanceof javax.swing.JCheckBox) {
                            javax.swing.JCheckBox cb = (javax.swing.JCheckBox) c;
                            cb.setEnabled(false);
                        }
                    } else {
                        c.setForeground(java.awt.Color.BLACK);
                        if (isCellSelected(row, column)) {
                            c.setBackground(getSelectionBackground());
                        } else {
                            c.setBackground(java.awt.Color.WHITE);
                        }
                        if (c instanceof javax.swing.JCheckBox) {
                            javax.swing.JCheckBox cb = (javax.swing.JCheckBox) c;
                            cb.setEnabled(true);
                        }
                    }
                    return c;
                }
            };

            // Style Table
            table.setRowHeight(35);
            table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            table.setSelectionBackground(new Color(220, 230, 240));
            table.setSelectionForeground(Color.BLACK);
            table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override
                public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    label.setBackground(new Color(44, 62, 80));
                    label.setForeground(Color.WHITE);
                    label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    label.setHorizontalAlignment(JLabel.CENTER);
                    return label;
                }
            });

            // Adjust preferred column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(90);  // Barkod
            table.getColumnModel().getColumn(2).setPreferredWidth(160); // Ürün
            table.getColumnModel().getColumn(3).setPreferredWidth(75);  // Satın Alınan
            table.getColumnModel().getColumn(4).setPreferredWidth(75);  // İade Edilen
            table.getColumnModel().getColumn(5).setPreferredWidth(60);  // Kalan
            table.getColumnModel().getColumn(6).setPreferredWidth(70);  // Birim Fiyat
            table.getColumnModel().getColumn(7).setPreferredWidth(80);  // Kalan Tutar
            table.getColumnModel().getColumn(8).setPreferredWidth(50);  // İade

            // Hide the Zatenİade column from view
            try {
                table.getColumnModel().removeColumn(table.getColumn("Zatenİade"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(680, 350));

            Object[] options = {"İade Et", "İptal"};
            int secim = JOptionPane.showOptionDialog(
                view,
                scrollPane,
                "İade Edilecek Ürünleri Seçin",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (secim == 0) { // User clicked "İade Et"
                List<IadeItem> iadeEdilecekler = new ArrayList<>();
                double toplamIadeTutari = 0.0;

                for (int i = 0; i < iadeModel.getRowCount(); i++) {
                    Boolean checked = (Boolean) iadeModel.getValueAt(i, 8);
                    Boolean zatenIade = (Boolean) iadeModel.getValueAt(i, 9);

                    if (checked != null && checked && (zatenIade == null || !zatenIade)) {
                        int id = (Integer) iadeModel.getValueAt(i, 0);
                        String barkod = iadeModel.getValueAt(i, 1).toString();
                        String urunAdi = iadeModel.getValueAt(i, 2).toString();
                        double kalan = (Double) iadeModel.getValueAt(i, 5);
                        double fiyat = (Double) iadeModel.getValueAt(i, 6);

                        double iadeEdilecekAdet = kalan;
                        if (kalan > 1.0) {
                            String cleanName = urunAdi.endsWith(" (İADE EDİLDİ)") ? urunAdi.substring(0, urunAdi.length() - " (İADE EDİLDİ)".length()) : urunAdi;
                            String input = JOptionPane.showInputDialog(view, 
                                cleanName + " ürününden " + kalan + " adet iade edilebilir.\nKaç adet iade etmek istiyorsunuz?", 
                                kalan);
                            if (input == null || input.trim().isEmpty()) {
                                return;
                            }
                            try {
                                iadeEdilecekAdet = Double.parseDouble(input.trim().replace(",", "."));
                                if (iadeEdilecekAdet <= 0 || iadeEdilecekAdet > kalan) {
                                    JOptionPane.showMessageDialog(view, "Geçersiz miktar girildi!", "Hata", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            } catch (NumberFormatException nfe) {
                                JOptionPane.showMessageDialog(view, "Geçersiz miktar formatı!", "Hata", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        double toplam = iadeEdilecekAdet * fiyat;
                        iadeEdilecekler.add(new IadeItem(id, barkod, urunAdi, iadeEdilecekAdet, toplam));
                        toplamIadeTutari += toplam;
                    }
                }

                if (iadeEdilecekler.isEmpty()) {
                    JOptionPane.showMessageDialog(view, "İade edilmek üzere hiçbir ürün seçilmedi!");
                    return;
                }

                // Process database updates inside transaction
                try (Connection conn2 = DatabaseConnection.connect()) {
                    conn2.setAutoCommit(false);
                    try {
                        for (IadeItem item : iadeEdilecekler) {
                            try (PreparedStatement psU = conn2.prepareStatement("UPDATE satis_detay SET iade_miktari = iade_miktari + ? WHERE id = ?")) {
                                psU.setDouble(1, item.miktar);
                                psU.setInt(2, item.id);
                                psU.executeUpdate();
                            }
                        }
                        conn2.commit();
                        
                        JOptionPane.showMessageDialog(view, String.format("Seçilen ürünlerin iade işlemi başarıyla tamamlandı!\nToplam İade Tutarı: %.2f TL", toplamIadeTutari));
                        
                        // Print refund receipt
                        iadeFisiGoster(islemId, iadeEdilecekler, toplamIadeTutari);
                        
                    } catch (Exception ex) {
                        conn2.rollback();
                        throw ex;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(view, "İade işlemi sırasında hata oluştu: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Bir veritabanı hatası oluştu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
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

    private void iadeFisiGoster(int satisId, List<IadeItem> items, double toplamIade) {
        StringBuilder sb = new StringBuilder();
        sb.append("      YILDIZ MARKET İADE FİŞİ      \n");
        sb.append("===================================\n");
        sb.append("Tarih: ").append(new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date())).append("\n");
        sb.append("İade Edilen Fiş No: ").append(satisId).append("\n");
        sb.append("Kasiyer: ").append(aktifPersonel.getAdSoyad()).append("\n");
        sb.append("-----------------------------------\n");
        sb.append(String.format("%-18s %6s %10s\n", "ÜRÜN", "ADET", "TUTAR"));
        for (IadeItem item : items) {
            String ad = item.urunAdi;
            if (ad.endsWith(" (İADE EDİLDİ)")) {
                ad = ad.substring(0, ad.length() - " (İADE EDİLDİ)".length());
            }
            sb.append(String.format("%-18s %6.1f %10.2f\n", ad, item.miktar, item.toplam));
        }
        sb.append("===================================\n");
        sb.append(String.format("TOPLAM İADE TUTARI: %14.2f TL\n", toplamIade));
        sb.append("===================================\n");
        sb.append("      İADE İŞLEMİ TAMAMLANMIŞTIR   \n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);
        JOptionPane.showMessageDialog(view, new JScrollPane(area), "İade Fiş Çıktısı", JOptionPane.PLAIN_MESSAGE);
    }

    private static class IadeItem {
        int id;
        String barkod;
        String urunAdi;
        double miktar;
        double toplam;
        
        IadeItem(int id, String barkod, String urunAdi, double miktar, double toplam) {
            this.id = id;
            this.barkod = barkod;
            this.urunAdi = urunAdi;
            this.miktar = miktar;
            this.toplam = toplam;
        }
    }
}
