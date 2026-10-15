package clinique.ui;

import clinique.config.DatabaseConnection;
import java.awt.*;
import java.net.URL;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {

    // Même charte graphique que MainFrame
    private static final Color COLOR_PRIMARY      = new Color(194, 24, 91);   // Rose / Magenta principal
    private static final Color COLOR_PRIMARY_DARK = new Color(136, 14, 79);   // Teinte sombre pour bandeau
    private static final Color COLOR_BG           = new Color(245, 247, 250); // Fond gris perle épuré
    private static final Color COLOR_TEXT_LABEL   = new Color(194, 24, 91);   // Couleur des labels de formulaire

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnConnecter;
    private JButton btnFermer;
    private JLabel lblMessage;
    private JProgressBar progressBar;

    public LoginFrame() {
        setTitle("Clinique Medical Center - Authentification");
        setSize(480, 390);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // 1. Bandeau supérieur (Même style que MainFrame)
        add(creerTopBanner(), BorderLayout.NORTH);

        // 2. Panneau central (Fond clair + Formulaire soigné)
        JPanel panelCentre = new JPanel(new BorderLayout(15, 15));
        panelCentre.setBackground(COLOR_BG);
        panelCentre.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Formulaire
        JPanel pnlForm = new JPanel(new GridLayout(2, 2, 12, 18));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 190, 231), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblUser = new JLabel("Identifiant :");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(COLOR_TEXT_LABEL);

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lblPass = new JLabel("Mot de passe :");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(COLOR_TEXT_LABEL);

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        pnlForm.add(lblUser);
        pnlForm.add(txtUsername);
        pnlForm.add(lblPass);
        pnlForm.add(txtPassword);

        panelCentre.add(pnlForm, BorderLayout.CENTER);

        // 3. Bas de page (Barre de chargement + Message + Boutons)
        JPanel pnlBas = new JPanel(new BorderLayout(10, 10));
        pnlBas.setOpaque(false);

        // Zone d'état
        JPanel pnlStatus = new JPanel(new GridLayout(2, 1, 5, 5));
        pnlStatus.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Connexion en cours, veuillez patienter...");
        progressBar.setStringPainted(true);
        progressBar.setForeground(COLOR_PRIMARY);
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setVisible(false);

        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMessage.setForeground(new Color(192, 57, 43));

        pnlStatus.add(progressBar);
        pnlStatus.add(lblMessage);
        pnlBas.add(pnlStatus, BorderLayout.NORTH);

        // Boutons
        JPanel pnlBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        pnlBoutons.setOpaque(false);

        btnConnecter = new JButton("Se connecter");
        btnConnecter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConnecter.setBackground(COLOR_PRIMARY);
        btnConnecter.setForeground(Color.WHITE);
        btnConnecter.setFocusPainted(false);
        btnConnecter.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY_DARK, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btnConnecter.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnFermer = new JButton("Fermer");
        btnFermer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFermer.setBackground(Color.WHITE);
        btnFermer.setForeground(COLOR_PRIMARY);
        btnFermer.setFocusPainted(false);
        btnFermer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 1),
            new EmptyBorder(8, 16, 8, 16)
        ));
        btnFermer.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlBoutons.add(btnConnecter);
        pnlBoutons.add(btnFermer);
        pnlBas.add(pnlBoutons, BorderLayout.SOUTH);

        panelCentre.add(pnlBas, BorderLayout.SOUTH);
        add(panelCentre, BorderLayout.CENTER);

        // Actions
        btnConnecter.addActionListener(e -> tenterConnexion());
        btnFermer.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(btnConnecter); // Touche Entrée
    }

    private JPanel creerTopBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(COLOR_PRIMARY_DARK);
        banner.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel title = new JLabel("Clinique Medical Center - Authentification", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        // Ajout du logo si disponible
        ImageIcon icon = chargerIcone("heart-center.png");
        if (icon == null) {
            icon = chargerIcone("medicine.png");
        }
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
            title.setIcon(new ImageIcon(img));
            title.setIconTextGap(10);
        }

        banner.add(title, BorderLayout.CENTER);
        return banner;
    }

    private ImageIcon chargerIcone(String nomFichier) {
        try {
            URL url = getClass().getResource("/resources/icons/" + nomFichier);
            if (url == null) {
                url = getClass().getClassLoader().getResource("resources/icons/" + nomFichier);
            }
            if (url != null) {
                return new ImageIcon(url);
            }

            java.io.File codeSource = new java.io.File(LoginFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            java.io.File dossierProjet = codeSource.getParentFile();
            if (dossierProjet != null && dossierProjet.getName().equalsIgnoreCase("classes")) {
                dossierProjet = dossierProjet.getParentFile();
            }
            if (dossierProjet != null && dossierProjet.getName().equalsIgnoreCase("build")) {
                dossierProjet = dossierProjet.getParentFile();
            }

            if (dossierProjet != null && dossierProjet.exists()) {
                java.io.File imageTrouvee = scannerFichier(dossierProjet, nomFichier);
                if (imageTrouvee != null && imageTrouvee.exists()) {
                    return new ImageIcon(imageTrouvee.getAbsolutePath());
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private java.io.File scannerFichier(java.io.File dossier, String nomFichier) {
        if (dossier == null || !dossier.exists()) return null;
        java.io.File[] fichiers = dossier.listFiles();
        if (fichiers == null) return null;
        for (java.io.File f : fichiers) {
            if (f.isDirectory() && !f.getName().equals(".git")) {
                java.io.File trouve = scannerFichier(f, nomFichier);
                if (trouve != null) return trouve;
            } else if (f.getName().equalsIgnoreCase(nomFichier)) {
                return f;
            }
        }
        return null;
    }

    private void tenterConnexion() {
        String user = txtUsername.getText().trim();
        char[] passChars = txtPassword.getPassword();

        if (user.isEmpty() || passChars.length == 0) {
            lblMessage.setText("Veuillez renseigner l'identifiant et le mot de passe !");
            return;
        }

        String password = new String(passChars);
        Arrays.fill(passChars, '0');

        lblMessage.setText(" ");
        progressBar.setVisible(true);
        btnConnecter.setEnabled(false);
        txtUsername.setEnabled(false);
        txtPassword.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return DatabaseConnection.verifierConnexion(user, password);
            }

            @Override
            protected void done() {
                try {
                    boolean succes = get();
                    if (succes) {
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        afficherErreur();
                    }
                } catch (Exception ex) {
                    afficherErreur();
                }
            }

            private void afficherErreur() {
                progressBar.setVisible(false);
                btnConnecter.setEnabled(true);
                txtUsername.setEnabled(true);
                txtPassword.setEnabled(true);
                lblMessage.setText("Nom d'utilisateur ou mot de passe incorrect.");
            }
        };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}