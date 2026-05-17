package controller;

import db.DatabaseConnection;
import model.Personel;
import view.DepoView;
import view.MainView;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class DepoController {
    private DepoView view;
    private Personel aktifPersonel;

    public DepoController(Personel personel) {
        this.aktifPersonel = personel;
        this.view = new DepoView();
        view.setTitle("DEPO - Kullanıcı: " + aktifPersonel.getAdSoyad());
        initController();
        verileriYukle();
        view.setVisible(true);
    }

    private void initController() {
        // Firewall & Navigation
        view.btnCikis.addActionListener(e -> guvenliCikis());
        view.btnKullaniciDegistir.addActionListener(e -> guvenliDonus());

        // Ürün İşlemleri
        view.btnKaydet.addActionListener(e -> urunKaydet());
        view.btnGuncelle.addActionListener(e -> urunGuncelle());
        view.btnSil.addActionListener(e -> urunSil());
        view.btnTemizle.addActionListener(e -> formuTemizle());

        view.tableUrunler.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = view.tableUrunler.getSelectedRow();
                if (row != -1) formuDoldur(row);
            }
        });
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

    private void verileriYukle() {
        view.modelUrunler.setRowCount(0);
        try (Connection conn = DatabaseConnection.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT * FROM urunler ORDER BY id DESC")) {
            while (rs.next()) {
                view.modelUrunler.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("barkod"), rs.getString("urun_adi"),
                        rs.getString("kategori"), rs.getDouble("stok"), rs.getString("birim"),
                        rs.getDouble("alis_fiyati"), rs.getDouble("fiyat")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void urunKaydet() {
        if (view.txtBarkod.getText().isEmpty() || view.txtUrunAdi.getText().isEmpty() || view.txtStok.getText().isEmpty() || view.txtSatisFiyati.getText().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Lütfen zorunlu alanları doldurun!"); return;
        }
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT fn_urun_kaydet(?, ?, ?, CAST(? AS numeric), ?, CAST(? AS numeric), CAST(? AS numeric))")) {
            ps.setString(1, view.txtBarkod.getText());
            ps.setString(2, view.txtUrunAdi.getText());
            ps.setString(3, view.cmbKategori.getSelectedItem().toString());
            ps.setDouble(4, Double.parseDouble(view.txtStok.getText().replace(",", ".")));
            ps.setString(5, view.rbAdet.isSelected() ? "Adet" : "Kg");
            ps.setDouble(6, Double.parseDouble(view.txtAlisFiyati.getText().replace(",", ".").isEmpty() ? "0" : view.txtAlisFiyati.getText().replace(",", ".")));
            ps.setDouble(7, Double.parseDouble(view.txtSatisFiyati.getText().replace(",", ".").isEmpty() ? "0" : view.txtSatisFiyati.getText().replace(",", ".")));
            ps.executeQuery();
            verileriYukle(); formuTemizle();
            JOptionPane.showMessageDialog(view, "Ürün Kaydedildi/Güncellendi!");
        } catch (NumberFormatException nfe) { JOptionPane.showMessageDialog(view, "Geçersiz sayı formatı!"); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void urunGuncelle() {
        urunKaydet();
    }

    private void urunSil() {
        int row = view.tableUrunler.getSelectedRow();
        if (row != -1) {
            String barkod = view.modelUrunler.getValueAt(row, 1).toString();
            if (JOptionPane.showConfirmDialog(view, "Ürünü silmek istediğinize emin misiniz?", "Sil", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.connect(); PreparedStatement ps = conn.prepareStatement("DELETE FROM urunler WHERE barkod=?")) {
                    ps.setString(1, barkod); ps.executeUpdate(); verileriYukle(); formuTemizle();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }

    private void formuDoldur(int row) {
        try {
            Object b = view.modelUrunler.getValueAt(row, 1);
            Object a = view.modelUrunler.getValueAt(row, 2);
            Object k = view.modelUrunler.getValueAt(row, 3);
            Object s = view.modelUrunler.getValueAt(row, 4);
            Object bi = view.modelUrunler.getValueAt(row, 5);
            Object al = view.modelUrunler.getValueAt(row, 6);
            Object sa = view.modelUrunler.getValueAt(row, 7);

            view.txtBarkod.setText(b != null ? b.toString() : "");
            view.txtUrunAdi.setText(a != null ? a.toString() : "");
            view.cmbKategori.setSelectedItem(k != null ? k.toString() : "Diğer");
            view.txtStok.setText(s != null ? s.toString() : "0");
            if (bi != null && bi.toString().equals("Kg")) view.rbKg.setSelected(true); else view.rbAdet.setSelected(true);
            view.txtAlisFiyati.setText(al != null ? al.toString() : "0");
            view.txtSatisFiyati.setText(sa != null ? sa.toString() : "0");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void formuTemizle() {
        view.txtBarkod.setText(""); view.txtUrunAdi.setText(""); view.txtStok.setText("");
        view.txtAlisFiyati.setText(""); view.txtSatisFiyati.setText(""); view.rbAdet.setSelected(true);
    }
}
