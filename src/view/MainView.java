package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainView extends JFrame {
    private JButton btnKasa, btnYonetim, btnDepo;

    private final Color COLOR_PRIMARY = new Color(52, 152, 219);
    private final Color COLOR_SECONDARY = new Color(44, 62, 80);
    private final Color COLOR_TEXT = Color.WHITE;

    public MainView() {
        setTitle("Yıldız Market Otomasyon Sistemi");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, new Color(43, 50, 60), w, h, new Color(20, 25, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);

        JLabel lblLogo = new JLabel("YILDIZ MARKET", SwingConstants.CENTER);
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 42));
        lblLogo.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(lblLogo, gbc);

        btnKasa = createModernButton("KASA", null);
        btnYonetim = createModernButton("YÖNETİM", null);
        btnDepo = createModernButton("DEPO", null);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(btnKasa, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        mainPanel.add(btnYonetim, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(btnDepo, gbc);
    }

    private JButton createModernButton(String text, String iconPath) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(280, 100));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(COLOR_TEXT);
        btn.setBackground(COLOR_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(41, 128, 185));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_PRIMARY);
            }
        });

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));

        return btn;
    }

    public void addLoginListener(ActionListener listener) {
        btnKasa.addActionListener(listener);
        btnYonetim.addActionListener(listener);
        btnDepo.addActionListener(listener);
    }
}
