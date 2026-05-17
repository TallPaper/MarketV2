import controller.MainController;
import view.MainView;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Modern bir görünüm için sistem temasını kullanalım
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        db.DatabaseConnection.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            MainView loginView = new MainView();
            new MainController(loginView);
            loginView.setVisible(true);
        });
    }
}
