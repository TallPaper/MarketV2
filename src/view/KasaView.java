package view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;

public class KasaView extends JFrame {

    public JTextField txtBarkod, txtMiktar;
    public JFormattedTextField txtMusteriTelefon;
    public JLabel lblGenelToplam, lblMusteriAd;
    public JTable tableSepet;
    public DefaultTableModel model;

    public ArrayList<JButton> btnNumpad = new ArrayList<>();
    public JButton btnSatisNakit, btnSatisKart, btnIptal, btnIade;
    public JButton btnUrunCikar, btnFisYazdir, btnMusteriBul;

    public JButton btnHesap, btnKullaniciDegistir, btnCikis;
    public JPanel pnlQuickItems;

    private final Color COLOR_DARK_BG = new Color(44, 62, 80);
    private final Color COLOR_ACCENT = new Color(39, 174, 96);
    private final Color COLOR_WARNING = new Color(243, 156, 18);
    private final Color COLOR_DANGER = new Color(192, 57, 43);
    private final Color COLOR_INFO = new Color(41, 128, 185);
    private final Color COLOR_BTN_TEXT = Color.WHITE;

    public KasaView() {
        setTitle("YILDIZ MARKET KASA v6.0 - Profesyonel Ticari Otomasyon");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(COLOR_DARK_BG);
        pnlHeader.setPreferredSize(new Dimension(0, 70));
        pnlHeader.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("YILDIZ MARKET KASA");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlHeaderBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        pnlHeaderBtns.setOpaque(false);
        btnHesap = createFlatButton("HESAP", new Color(142, 68, 173));
        btnKullaniciDegistir = createFlatButton("KULLANICI DEĞİŞTİR", COLOR_INFO);
        btnCikis = createFlatButton("ÇIKIŞ", COLOR_DANGER);

        pnlHeaderBtns.add(btnHesap); pnlHeaderBtns.add(btnKullaniciDegistir); pnlHeaderBtns.add(btnCikis);
        pnlHeader.add(pnlHeaderBtns, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- CENTER ---
        JPanel pnlCenter = new JPanel(new BorderLayout(15, 15));
        pnlCenter.setBorder(new EmptyBorder(15, 15, 15, 15));

        // TABLE
        String[] cols = {"Barkod", "Ürün Adı", "Miktar", "Birim", "Birim Fiyat", "Toplam"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableSepet = new JTable(model);
        decorateTable(tableSepet);
        
        // --- LEFT PANEL (Table + Quick Menu) ---
        JPanel pnlLeft = new JPanel(new BorderLayout(0, 10));
        pnlLeft.setOpaque(false);
        pnlLeft.add(new JScrollPane(tableSepet), BorderLayout.CENTER);

        // QUICK MENU
        JPanel pnlQuickMenu = new JPanel();
        pnlQuickMenu.setLayout(new BoxLayout(pnlQuickMenu, BoxLayout.X_AXIS));
        pnlQuickMenu.setBackground(Color.WHITE);
        pnlQuickMenu.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Hızlı Ürünler"),
            BorderFactory.createEmptyBorder(0, 5, 0, 5)
        ));
        
        JScrollPane scrollQuick = new JScrollPane(pnlQuickMenu);
        scrollQuick.setPreferredSize(new Dimension(0, 120));
        scrollQuick.getViewport().setBackground(Color.WHITE);
        scrollQuick.setBackground(Color.WHITE);
        scrollQuick.setBorder(BorderFactory.createEmptyBorder());
        scrollQuick.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollQuick.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        pnlLeft.add(scrollQuick, BorderLayout.SOUTH);
        
        pnlCenter.add(pnlLeft, BorderLayout.CENTER);
        this.pnlQuickItems = pnlQuickMenu;

        // --- RIGHT PANEL ---
        JPanel pnlRight = new JPanel(new BorderLayout(10, 10));
        pnlRight.setPreferredSize(new Dimension(420, 0));

        // TOTAL
        JPanel pnlTotal = new JPanel(new BorderLayout());
        pnlTotal.setBackground(COLOR_DARK_BG);
        pnlTotal.setBorder(new EmptyBorder(25, 25, 25, 25));
        JLabel lblTotalText = new JLabel("TOPLAM TUTAR:");
        lblTotalText.setForeground(Color.LIGHT_GRAY);
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGenelToplam = new JLabel("0,00 ₺", SwingConstants.RIGHT);
        lblGenelToplam.setForeground(COLOR_ACCENT);
        lblGenelToplam.setFont(new Font("Segoe UI", Font.BOLD, 54));
        pnlTotal.add(lblTotalText, BorderLayout.NORTH);
        pnlTotal.add(lblGenelToplam, BorderLayout.CENTER);
        pnlRight.add(pnlTotal, BorderLayout.NORTH);

        // INPUTS
        JPanel pnlInputContainer = new JPanel(new BorderLayout(5, 5));

        // Müşteri Sorgulama
        JPanel pnlMusteri = new JPanel(new BorderLayout(5, 5));
        pnlMusteri.setBorder(BorderFactory.createTitledBorder("Müşteri Sadakat Sistemi"));
        try {
            MaskFormatter mf = new MaskFormatter("0(###)### ## ##");
            mf.setPlaceholderCharacter('_');
            txtMusteriTelefon = new JFormattedTextField(mf);
        } catch (ParseException e) {
            txtMusteriTelefon = new JFormattedTextField();
        }
        txtMusteriTelefon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnMusteriBul = createFlatButton("BUL", COLOR_INFO);
        lblMusteriAd = new JLabel("Müşteri: Kayıtlı Değil");
        lblMusteriAd.setForeground(COLOR_INFO);
        
        JPanel pnlMusteriInput = new JPanel(new BorderLayout(5, 5));
        pnlMusteriInput.add(txtMusteriTelefon, BorderLayout.CENTER);
        pnlMusteriInput.add(btnMusteriBul, BorderLayout.EAST);
        pnlMusteri.add(pnlMusteriInput, BorderLayout.NORTH);
        pnlMusteri.add(lblMusteriAd, BorderLayout.SOUTH);
        pnlInputContainer.add(pnlMusteri, BorderLayout.NORTH);

        // Ürün Giriş
        JPanel pnlFields = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 0, 5, 5);

        gbc.weightx = 0.3; gbc.gridx = 0;
        txtMiktar = new JTextField("1");
        txtMiktar.setFont(new Font("Segoe UI", Font.BOLD, 32));
        txtMiktar.setHorizontalAlignment(JTextField.CENTER);
        pnlFields.add(txtMiktar, gbc);

        gbc.weightx = 0.7; gbc.gridx = 1;
        txtBarkod = new JTextField();
        txtBarkod.setFont(new Font("Segoe UI", Font.BOLD, 32));
        txtBarkod.setHorizontalAlignment(JTextField.CENTER);
        pnlFields.add(txtBarkod, gbc);
        
        JPanel pnlMidContainer = new JPanel(new BorderLayout());
        pnlMidContainer.add(pnlFields, BorderLayout.NORTH);

        // NUMPAD
        JPanel pnlKeys = new JPanel(new GridLayout(4, 3, 5, 5));
        String[] keys = {"7", "8", "9", "4", "5", "6", "1", "2", "3", "C", "0", "GİRİŞ"};
        for (String key : keys) {
            JButton btn;
            if (key.equals("GİRİŞ")) btn = createFlatButton(key, COLOR_ACCENT);
            else if (key.equals("C")) btn = createFlatButton(key, COLOR_DANGER);
            else {
                btn = new JButton(key);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
                btn.setBackground(Color.WHITE);
                btn.setForeground(COLOR_DARK_BG);
                btn.setFocusPainted(false);
                btn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                
                final JButton finalBtn = btn;
                btn.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { finalBtn.setBackground(new Color(240, 240, 240)); }
                    public void mouseExited(java.awt.event.MouseEvent e) { finalBtn.setBackground(Color.WHITE); }
                });
            }
            btnNumpad.add(btn); pnlKeys.add(btn);
        }
        pnlMidContainer.add(pnlKeys, BorderLayout.CENTER);
        pnlInputContainer.add(pnlMidContainer, BorderLayout.CENTER);

        // ACTIONS
        JPanel pnlActions = new JPanel(new GridLayout(3, 2, 8, 8));
        pnlActions.setPreferredSize(new Dimension(0, 180));
        btnSatisNakit = createFlatButton("NAKİT", COLOR_ACCENT);
        btnSatisKart = createFlatButton("KREDİ KARTI", COLOR_INFO);
        btnFisYazdir = createFlatButton("FİŞ YAZDIR", COLOR_WARNING);
        btnUrunCikar = createFlatButton("SİL", Color.GRAY);
        btnIptal = createFlatButton("İPTAL", COLOR_DANGER);
        btnIade = createFlatButton("İADE İŞLEMİ", new Color(211, 84, 0));

        pnlActions.add(btnSatisNakit); pnlActions.add(btnSatisKart);
        pnlActions.add(btnFisYazdir);  pnlActions.add(btnUrunCikar);
        pnlActions.add(btnIptal);      pnlActions.add(btnIade);
        pnlInputContainer.add(pnlActions, BorderLayout.SOUTH);

        pnlRight.add(pnlInputContainer, BorderLayout.CENTER);
        pnlCenter.add(pnlRight, BorderLayout.EAST);
        add(pnlCenter, BorderLayout.CENTER);
    }

    private JButton createFlatButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(COLOR_BTN_TEXT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setOpaque(true);
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(bg); }
        });
        
        return btn;
    }

    private void decorateTable(JTable table) {
        table.setRowHeight(35); table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setSelectionBackground(new Color(220, 230, 240)); table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(COLOR_DARK_BG); label.setForeground(Color.WHITE);
                label.setFont(new Font("Segoe UI", Font.BOLD, 14)); label.setHorizontalAlignment(JLabel.CENTER);
                return label;
            }
        });
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }
}
