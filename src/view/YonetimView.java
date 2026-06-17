package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import com.toedter.calendar.JDateChooser;
import java.awt.*;

public class YonetimView extends JFrame {

    public JTabbedPane tabbedPane;
    public DefaultTableModel modelLog, modelTedarikci, modelPersonel, modelCokSatanlar, modelAzKalanlar, modelKampanyalar, modelSatislar;
    public JTable tableLog, tableTedarikci, tablePersonel, tableCokSatanlar, tableAzKalanlar, tableKampanyalar, tableSatislar;
    public JTextArea txtOneriler;

    public JTextField txtKampanyaBarkod, txtKampanyaAd, txtKampanyaYuzde;
    public JDateChooser dcBaslangic, dcBitis;
    public JComboBox<String> cmbKampanyaTur; // HERKES, UYE, IKISI
    public JButton btnKampanyaEkle, btnKampanyaSil, btnAISorgula;

    public JButton btnHesap, btnKullaniciDegistir, btnCikis;
    public JButton btnEkleTedarikci, btnSilTedarikci, btnEklePersonel, btnSilPersonel, btnDuzenlePersonel;
    public JLabel lblGunlukCiro, lblSatisAdedi, lblAylikCiro, lblUrunCesidi;

    private final Color COLOR_DARK_BG = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(52, 152, 219);
    private final Color COLOR_SUCCESS = new Color(39, 174, 96);
    private final Color COLOR_DANGER = new Color(231, 76, 60);
    private final Color COLOR_WARNING = new Color(243, 156, 18);

    public YonetimView() {
        setTitle("YILDIZ MARKET YÖNETİM MERKEZİ");
        setSize(1350, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // HEADER
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_DARK_BG);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("YÖNETİM & AI ANALİZ PANELİ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlHeaderButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlHeaderButtons.setOpaque(false);
        btnHesap = createHeaderButton("HESAP", new Color(142, 68, 173));
        btnKullaniciDegistir = createHeaderButton("KULLANICI DEĞİŞTİR", COLOR_ACCENT);
        btnCikis = createHeaderButton("ÇIKIŞ YAP", COLOR_DANGER);
        pnlHeaderButtons.add(btnHesap); pnlHeaderButtons.add(btnKullaniciDegistir); pnlHeaderButtons.add(btnCikis);
        pnlHeader.add(pnlHeaderButtons, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // TABS
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("ANA SAYFA", createCiroPanel());
        tabbedPane.addTab("SATIŞ RAPORLARI", createSatislarPanel());
        tabbedPane.addTab("AI STRATEJİ", createAnalizPanel());
        tabbedPane.addTab("KAMPANYALAR", createKampanyaPanel());
        tabbedPane.addTab("PERSONEL", createPersonelPanel());
        tabbedPane.addTab("SİSTEM LOGLARI", createLogPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCiroPanel() {
        JPanel p = new JPanel(new GridLayout(2, 2, 20, 20)); p.setBorder(new EmptyBorder(30,30,30,30));
        lblGunlukCiro = new JLabel("0.00 ₺", SwingConstants.CENTER);
        lblSatisAdedi = new JLabel("0", SwingConstants.CENTER);
        lblAylikCiro = new JLabel("0.00 ₺", SwingConstants.CENTER);
        lblUrunCesidi = new JLabel("0", SwingConstants.CENTER);
        
        p.add(createCard("GÜNLÜK CİRO", lblGunlukCiro, COLOR_SUCCESS));
        p.add(createCard("SATIŞ ADEDİ", lblSatisAdedi, COLOR_ACCENT));
        p.add(createCard("AYLIK CİRO", lblAylikCiro, new Color(142, 68, 173)));
        p.add(createCard("ÜRÜN ÇEŞİDİ", lblUrunCesidi, COLOR_WARNING));
        return p;
    }

    private JPanel createCard(String t, JLabel l, Color c) {
        JPanel p = new JPanel(new BorderLayout()); p.setBackground(c);
        JLabel title = new JLabel(t, SwingConstants.CENTER); title.setForeground(Color.WHITE); title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(Color.WHITE); l.setFont(new Font("Segoe UI", Font.BOLD, 42));
        p.add(title, BorderLayout.NORTH); p.add(l, BorderLayout.CENTER); return p;
    }

    private JPanel createKampanyaPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(BorderFactory.createTitledBorder("Yeni Kampanya"));
        pnlForm.setPreferredSize(new Dimension(350, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; gbc.gridx = 0; gbc.gridy = 0;

        txtKampanyaAd = new JTextField();
        txtKampanyaBarkod = new JTextField();
        txtKampanyaYuzde = new JTextField();
        dcBaslangic = new JDateChooser();
        dcBitis = new JDateChooser();
        cmbKampanyaTur = new JComboBox<>(new String[]{"HERKES", "UYE", "IKISI"});
        btnKampanyaEkle = createFlatButton("KAMPANYA OLUŞTUR", COLOR_SUCCESS);
        btnKampanyaSil = createFlatButton("KAMPANYA SİL", COLOR_DANGER);

        pnlForm.add(new JLabel("Kampanya Başlığı:"), gbc); gbc.gridy++; 
        pnlForm.add(txtKampanyaAd, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Ürün Barkod:"), gbc); gbc.gridy++; 
        pnlForm.add(txtKampanyaBarkod, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("İndirim Oranı (%):"), gbc); gbc.gridy++; 
        pnlForm.add(txtKampanyaYuzde, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Başlangıç Tarihi:"), gbc); gbc.gridy++; 
        pnlForm.add(dcBaslangic, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Bitiş Tarihi:"), gbc); gbc.gridy++; 
        pnlForm.add(dcBitis, gbc); gbc.gridy++;
        pnlForm.add(new JLabel("Kampanya Türü:"), gbc); gbc.gridy++; 
        pnlForm.add(cmbKampanyaTur, gbc); gbc.gridy++;
        gbc.insets = new Insets(15, 5, 5, 5);
        pnlForm.add(btnKampanyaEkle, gbc); gbc.gridy++; 
        pnlForm.add(btnKampanyaSil, gbc); gbc.gridy++;
        
        // Pusher component to keep everything at the top
        gbc.weighty = 1.0;
        pnlForm.add(new JPanel(), gbc);

        modelKampanyalar = new DefaultTableModel(new String[]{"ID", "Kampanya Başlığı", "Barkod", "İndirim", "Tür", "Başlangıç Tarihi", "Bitiş Tarihi"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableKampanyalar = decorateTable(new JTable(modelKampanyalar));
        
        panel.add(pnlForm, BorderLayout.WEST);
        panel.add(new JScrollPane(tableKampanyalar), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAnalizPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlTables = new JPanel(new GridLayout(1, 2, 10, 10));
        modelCokSatanlar = new DefaultTableModel(new String[]{"Ürün Adı", "Satış Adedi"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        modelAzKalanlar = new DefaultTableModel(new String[]{"Ürün Adı", "Kalan Stok"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCokSatanlar = decorateTable(new JTable(modelCokSatanlar));
        tableAzKalanlar = decorateTable(new JTable(modelAzKalanlar));

        pnlTables.add(new JScrollPane(tableCokSatanlar));
        pnlTables.add(new JScrollPane(tableAzKalanlar));
        p.add(pnlTables, BorderLayout.CENTER);

        JPanel pnlAI = new JPanel(new BorderLayout(5, 5));
        pnlAI.setPreferredSize(new Dimension(0, 250));
        txtOneriler = new JTextArea(); txtOneriler.setEditable(false);
        txtOneriler.setFont(new Font("Consolas", Font.PLAIN, 14));
        btnAISorgula = createFlatButton("AI STRATEJİK ANALİZ BAŞLAT", new Color(142, 68, 173));
        
        pnlAI.add(new JScrollPane(txtOneriler), BorderLayout.CENTER);
        pnlAI.add(btnAISorgula, BorderLayout.SOUTH);
        p.add(pnlAI, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createSatislarPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        modelSatislar = new DefaultTableModel(new String[]{"ID", "Tarih", "Tutar", "Ödeme", "Kasiyer", "Müşteri"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableSatislar = decorateTable(new JTable(modelSatislar));
        p.add(new JScrollPane(tableSatislar), BorderLayout.CENTER);
        return p;
    }

    private JPanel createPersonelPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        modelPersonel = new DefaultTableModel(new String[]{"ID", "Kullanıcı No", "Ad", "Soyad", "Rol"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablePersonel = decorateTable(new JTable(modelPersonel));
        p.add(new JScrollPane(tablePersonel), BorderLayout.CENTER);
        
        JPanel b = new JPanel();
        btnEklePersonel = createFlatButton("EKLE", COLOR_SUCCESS);
        btnDuzenlePersonel = createFlatButton("DÜZENLE", COLOR_ACCENT);
        btnSilPersonel = createFlatButton("SİL", COLOR_DANGER);
        b.add(btnEklePersonel); b.add(btnDuzenlePersonel); b.add(btnSilPersonel);
        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private JPanel createLogPanel() {
        modelLog = new DefaultTableModel(new String[]{"ID", "Kullanıcı", "İşlem", "Tarih"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tableLog = decorateTable(new JTable(modelLog));
        return new JPanel(new BorderLayout()) {{ add(new JScrollPane(tableLog)); }};
    }

    private JTable decorateTable(JTable t) {
        t.setRowHeight(30); t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {{
            setBackground(COLOR_DARK_BG); setForeground(Color.WHITE); setHorizontalAlignment(JLabel.CENTER);
        }}); return t;
    }

    private JButton createFlatButton(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14)); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true); b.setPreferredSize(new Dimension(180, 40)); return b;
    }

    private JButton createHeaderButton(String t, Color c) {
        JButton b = new JButton(t); b.setBackground(c); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setPreferredSize(new Dimension(150, 35)); return b;
    }
}
