package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DepoView extends JFrame {

    public JTabbedPane tabbedPane;
    public JTable tableUrunler;
    public DefaultTableModel modelUrunler;

    public JTextField txtBarkod, txtUrunAdi, txtStok, txtAlisFiyati, txtSatisFiyati;
    public JComboBox<String> cmbKategori, cmbTedarikci;
    public JRadioButton rbAdet, rbKg;
    public ButtonGroup bgBirim;

    public JButton btnCikis, btnKullaniciDegistir, btnHesap;
    public JButton btnKaydet, btnGuncelle, btnSil, btnTemizle;

    private final Color COLOR_DARK_BG = new Color(33, 47, 61);
    private final Color COLOR_ACCENT = new Color(46, 204, 113);
    private final Color COLOR_BTN_UPDATE = new Color(52, 152, 219);
    private final Color COLOR_BTN_DELETE = new Color(231, 76, 60);

    public DepoView() {
        setTitle("YILDIZ MARKET DEPO YÖNETİMİ");
        setSize(1350, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_DARK_BG);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("DEPO YÖNETİM MERKEZİ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlHeaderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlHeaderButtons.setOpaque(false);
        btnHesap = createHeaderButton("HESAP", new Color(142, 68, 173));
        btnKullaniciDegistir = createHeaderButton("KULLANICI DEĞİŞTİR", new Color(52, 152, 219));
        btnCikis = createHeaderButton("ÇIKIŞ", new Color(192, 57, 43));
        pnlHeaderButtons.add(btnHesap); pnlHeaderButtons.add(btnKullaniciDegistir); pnlHeaderButtons.add(btnCikis);
        pnlHeader.add(pnlHeaderButtons, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // FORM
        JPanel pnlMain = new JPanel(new BorderLayout(10, 10));
        pnlMain.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setPreferredSize(new Dimension(350, 0));
        pnlForm.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Ürün Detayları", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.gridy = 0;

        pnlForm.add(new JLabel("Barkod:"), gbc); gbc.gridy++; txtBarkod = new JTextField(); pnlForm.add(txtBarkod, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Ürün Adı:"), gbc); gbc.gridy++; txtUrunAdi = new JTextField(); pnlForm.add(txtUrunAdi, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Kategori:"), gbc); gbc.gridy++; cmbKategori = new JComboBox<>(new String[]{"Gıda", "Manav", "Temizlik", "İçecek", "Diğer"}); pnlForm.add(cmbKategori, gbc); gbc.gridy++;
        
        JPanel pnlRadio = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rbAdet = new JRadioButton("Adet", true); rbKg = new JRadioButton("Kg");
        bgBirim = new ButtonGroup(); bgBirim.add(rbAdet); bgBirim.add(rbKg);
        pnlRadio.add(rbAdet); pnlRadio.add(rbKg); pnlForm.add(pnlRadio, gbc); gbc.gridy++;

        pnlForm.add(new JLabel("Stok Miktarı:"), gbc); gbc.gridy++; txtStok = new JTextField(); pnlForm.add(txtStok, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Alış Fiyatı:"), gbc); gbc.gridy++; txtAlisFiyati = new JTextField(); pnlForm.add(txtAlisFiyati, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Satış Fiyatı:"), gbc); gbc.gridy++; txtSatisFiyati = new JTextField(); pnlForm.add(txtSatisFiyati, gbc); gbc.gridy++;
        
        JPanel pnlBtns = new JPanel(new GridLayout(2, 2, 5, 5));
        btnKaydet = createFlatButton("KAYDET", COLOR_ACCENT);
        btnGuncelle = createFlatButton("GÜNCELLE", COLOR_BTN_UPDATE);
        btnSil = createFlatButton("SİL", COLOR_BTN_DELETE);
        btnTemizle = createFlatButton("TEMİZLE", Color.GRAY);
        pnlBtns.add(btnKaydet); pnlBtns.add(btnGuncelle); pnlBtns.add(btnSil); pnlBtns.add(btnTemizle);
        gbc.insets = new Insets(20, 10, 10, 10); pnlForm.add(pnlBtns, gbc);

        // TABLE
        String[] cols = {"ID", "Barkod", "Ürün Adı", "Kategori", "Miktar", "Birim", "Alış", "Satış"};
        modelUrunler = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        tableUrunler = new JTable(modelUrunler);
        decorateTable(tableUrunler);
        
        pnlMain.add(pnlForm, BorderLayout.WEST);
        pnlMain.add(new JScrollPane(tableUrunler), BorderLayout.CENTER);
        add(pnlMain, BorderLayout.CENTER);
    }

    private void decorateTable(JTable table) {
        table.setRowHeight(30); table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {{
            setBackground(COLOR_DARK_BG); setForeground(Color.WHITE); setHorizontalAlignment(JLabel.CENTER);
        }});
    }

    private JButton createHeaderButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12)); btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(140, 35)); return btn;
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text); btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14)); btn.setFocusPainted(false); btn.setOpaque(true); btn.setBorderPainted(false);
        return btn;
    }
}
