package clinique.ui;

import clinique.config.DatabaseConnection;
import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

    private static final Color COLOR_PRIMARY      = new Color(194, 24, 91);
    private static final Color COLOR_PRIMARY_DARK = new Color(136, 14, 79);

    private JPanel pnlContentCards;
    private CardLayout cardLayout;
    private JButton btnNavPatients, btnNavMedecins, btnNavDepartements, btnNavConsultations, btnNavFactures, btnNavStats;

    // Les 6 vues modulaires
    private PatientView patientView;
    private MedecinView medecinView;
    private DepartementView departementView;
    private ConsultationView consultationView;
    private FactureView factureView;
    private StatistiqueView statistiqueView;

    public MainFrame() {
        setTitle("Clinique Medical Center - Gestion Relationnel-Objet (v1.0)");
        setSize(1200, 600);             
        setResizable(false);         
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);  
        setLayout(new BorderLayout());

        // 1. Bandeau supérieur
        add(creerTopBanner(), BorderLayout.NORTH);

        // 2. Barre latérale
        add(creerSidebar(), BorderLayout.WEST);

        // 3. Zone centrale (Assemblage des 6 vues indépendantes)
        cardLayout = new CardLayout();
        pnlContentCards = new JPanel(cardLayout);

        patientView      = new PatientView();
        medecinView      = new MedecinView();
        departementView  = new DepartementView();
        consultationView = new ConsultationView();
        factureView      = new FactureView();
        statistiqueView  = new StatistiqueView();

        pnlContentCards.add(patientView, "PATIENTS");
        pnlContentCards.add(medecinView, "MEDECINS");
        pnlContentCards.add(departementView, "DEPARTEMENTS");
        pnlContentCards.add(consultationView, "CONSULTATIONS");
        pnlContentCards.add(factureView, "FACTURES");
        pnlContentCards.add(statistiqueView, "STATS");

        add(pnlContentCards, BorderLayout.CENTER);
        activerBoutonNav(btnNavPatients);
    }

    private JPanel creerTopBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(COLOR_PRIMARY_DARK);
        banner.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Clinique Medical Center Management System Version 1.0");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);
        banner.add(title, BorderLayout.WEST);

        JLabel user = new JLabel("User : " + DatabaseConnection.getSessionUser());
        user.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        user.setForeground(Color.YELLOW);
        banner.add(user, BorderLayout.EAST);
        return banner;
    }

    private JPanel creerSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(COLOR_PRIMARY);
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel pnlLogo = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlLogo.setOpaque(false);
        pnlLogo.setBorder(new EmptyBorder(22, 15, 22, 15));

        JLabel lblIcon = new JLabel(" Medical Center", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblIcon.setForeground(Color.WHITE);
        ImageIcon iconLogo = chargerIcone("heart-center.png");
        if (iconLogo != null) lblIcon.setIcon(new ImageIcon(iconLogo.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH)));

        JLabel lblSub = new JLabel("Relationnel-Objet", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(248, 187, 208));

        pnlLogo.add(lblIcon); pnlLogo.add(lblSub);
        sidebar.add(pnlLogo, BorderLayout.NORTH);

        JPanel pnlMenu = new JPanel(new GridLayout(7, 1, 0, 8));
        pnlMenu.setOpaque(false);
        pnlMenu.setBorder(new EmptyBorder(10, 10, 10, 10));

        btnNavPatients      = creerBoutonMenu("Patients", "patient.png");
        btnNavMedecins      = creerBoutonMenu("Médecins", "doctor.png");
        btnNavDepartements  = creerBoutonMenu("Départements", "department.png");
        btnNavConsultations = creerBoutonMenu("Consultations", "consultation.png");
        btnNavFactures      = creerBoutonMenu("Factures", "invoice.png");
        btnNavStats         = creerBoutonMenu("Statistique", "statistics.png");

        btnNavPatients.addActionListener(e -> { cardLayout.show(pnlContentCards, "PATIENTS"); activerBoutonNav(btnNavPatients); patientView.charger(); });
        btnNavMedecins.addActionListener(e -> { cardLayout.show(pnlContentCards, "MEDECINS"); activerBoutonNav(btnNavMedecins); medecinView.charger(); });
        btnNavDepartements.addActionListener(e -> { cardLayout.show(pnlContentCards, "DEPARTEMENTS"); activerBoutonNav(btnNavDepartements); departementView.charger(); });
        btnNavConsultations.addActionListener(e -> { cardLayout.show(pnlContentCards, "CONSULTATIONS"); activerBoutonNav(btnNavConsultations); consultationView.rafraichirPatients(); });
        btnNavFactures.addActionListener(e -> { cardLayout.show(pnlContentCards, "FACTURES"); activerBoutonNav(btnNavFactures); factureView.charger(); });
        btnNavStats.addActionListener(e -> { cardLayout.show(pnlContentCards, "STATS"); activerBoutonNav(btnNavStats); statistiqueView.charger(); });

        pnlMenu.add(btnNavPatients); pnlMenu.add(btnNavMedecins); pnlMenu.add(btnNavDepartements);
        pnlMenu.add(btnNavConsultations); pnlMenu.add(btnNavFactures); pnlMenu.add(btnNavStats);
        sidebar.add(pnlMenu, BorderLayout.CENTER);

        JButton btnLogout = new JButton("Déconnexion");
        ImageIcon iconLogout = chargerIcone("logout.png");
        if (iconLogout != null) btnLogout.setIcon(iconLogout);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setBackground(COLOR_PRIMARY_DARK);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new EmptyBorder(12, 10, 12, 10));
        btnLogout.addActionListener(e -> { dispose(); new LoginFrame().setVisible(true); });

        sidebar.add(btnLogout, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton creerBoutonMenu(String texte, String iconFile) {
        JButton btn = new JButton(texte);
        ImageIcon icon = chargerIcone(iconFile);
        if (icon != null) btn.setIcon(icon);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(COLOR_PRIMARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void activerBoutonNav(JButton actif) {
        JButton[] btns = {btnNavPatients, btnNavMedecins, btnNavDepartements, btnNavConsultations, btnNavFactures, btnNavStats};
        for (JButton b : btns) { b.setBackground(COLOR_PRIMARY); b.setForeground(Color.WHITE); }
        actif.setBackground(Color.WHITE); actif.setForeground(COLOR_PRIMARY);
    }

    // La méthode exacte qui fonctionne dans LoginFrame avec le scanner automatique
    private ImageIcon chargerIcone(String nomFichier) {
        try {
            URL url = getClass().getResource("/resources/icons/" + nomFichier);
            if (url == null) {
                url = getClass().getClassLoader().getResource("resources/icons/" + nomFichier);
            }
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }

            java.io.File codeSource = new java.io.File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
                    Image img = new ImageIcon(imageTrouvee.getAbsolutePath()).getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    return new ImageIcon(img);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Le scanner indispensable qui explore le dossier du projet
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
}