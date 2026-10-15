package clinique.app;

import clinique.ui.LoginFrame;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Démarre l'application par l'écran de connexion sécurisé
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}