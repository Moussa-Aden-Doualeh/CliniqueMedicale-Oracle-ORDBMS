package clinique.ui;

import clinique.dao.StatistiqueDAO;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class StatistiqueView extends JPanel {

    private final StatistiqueDAO statistiqueDAO = new StatistiqueDAO();
    private static final Color COLOR_PRIMARY = new Color(194, 24, 91);
    private static final Color COLOR_BG = new Color(245, 247, 250);

    // Cartes KPIs professionnelles
    private JLabel lblValRecettes;
    private JLabel lblValSoins;
    private JLabel lblValRDV;

    // Tableaux métier
    private JTable tblStatsMedecins;
    private DefaultTableModel modelStatsMedecins;
    private JTable tblStatsDepartements;
    private DefaultTableModel modelStatsDepartements;

    public StatistiqueView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. HAUT : 3 Cartes de Gestion Clinique
        JPanel pnlKPIs = new JPanel(new GridLayout(1, 3, 10, 10));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setPreferredSize(new Dimension(0, 75));

        lblValRecettes = new JLabel("0 DT", SwingConstants.CENTER);
        lblValSoins    = new JLabel("0", SwingConstants.CENTER);
        lblValRDV      = new JLabel("0", SwingConstants.CENTER);

        pnlKPIs.add(creerCarteKPI("RECETTES TOTALES (C.A.)", lblValRecettes, new Color(39, 174, 96))); // Vert financier
        pnlKPIs.add(creerCarteKPI("SOINS & ACTES RÉALISÉS", lblValSoins, new Color(41, 128, 185)));   // Bleu médical
        pnlKPIs.add(creerCarteKPI("RENDEZ-VOUS PLANIFIÉS", lblValRDV, new Color(230, 126, 34)));     // Orange agenda

        add(pnlKPIs, BorderLayout.NORTH);

        // 2. CENTRE : Deux onglets d'analyse d'activité
        JTabbedPane tabStats = new JTabbedPane();
        tabStats.setFont(new Font("Segoe UI", Font.BOLD, 11));

        // Tableau 1 : Médecins
        modelStatsMedecins = new DefaultTableModel(new String[]{
            "Praticien", "Spécialité", "Patients Suivis", "Actes Réalisés", "C.A. Généré"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStatsMedecins = new JTable(modelStatsMedecins);
        tblStatsMedecins.setRowHeight(24);
        tabStats.addTab("Activité & Rentabilité par Médecin", new JScrollPane(tblStatsMedecins));

        // Tableau 2 : Départements
        modelStatsDepartements = new DefaultTableModel(new String[]{
            "Service Médical", "Étage", "Effectif Médical", "Recettes du Service"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblStatsDepartements = new JTable(modelStatsDepartements);
        tblStatsDepartements.setRowHeight(24);
        tabStats.addTab("Bilan par Service / Département", new JScrollPane(tblStatsDepartements));

        add(tabStats, BorderLayout.CENTER);

        // 3. BAS : Bouton d'actualisation
        JPanel pnlBas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlBas.setOpaque(false);

        JButton btnActualiser = new JButton("Actualiser le Tableau de Bord");
        btnActualiser.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnActualiser.setBackground(Color.WHITE);
        btnActualiser.setForeground(COLOR_PRIMARY);
        btnActualiser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_PRIMARY, 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btnActualiser.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualiser.addActionListener(e -> charger());

        pnlBas.add(btnActualiser);
        add(pnlBas, BorderLayout.SOUTH);

        // Chargement initial
        charger();
    }

    private JPanel creerCarteKPI(String titre, JLabel lblValeur, Color couleur) {
        JPanel carte = new JPanel(new BorderLayout(2, 2));
        carte.setBackground(Color.WHITE);
        carte.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 224, 230), 1),
            new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitre.setForeground(new Color(100, 116, 139));

        lblValeur.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblValeur.setForeground(couleur);

        carte.add(lblTitre, BorderLayout.NORTH);
        carte.add(lblValeur, BorderLayout.CENTER);
        return carte;
    }

    public void charger() {
        try {
            // Mise à jour des cartes
            double recettes = statistiqueDAO.getTotalRecettes();
            int soins = statistiqueDAO.getNbActesTermines();
            int rdv = statistiqueDAO.getNbRDVPlanifies();

            lblValRecettes.setText(recettes + " DT");
            lblValSoins.setText(soins + " actes");
            lblValRDV.setText(rdv + " RDV");

            // Remplissage du tableau Médecins
            modelStatsMedecins.setRowCount(0);
            List<Object[]> listeMeds = statistiqueDAO.getRapportMedecins();
            for (Object[] row : listeMeds) {
                modelStatsMedecins.addRow(row);
            }

            // Remplissage du tableau Départements
            modelStatsDepartements.setRowCount(0);
            List<Object[]> listeDeps = statistiqueDAO.getRapportDepartements();
            for (Object[] row : listeDeps) {
                modelStatsDepartements.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur Statistiques : " + ex.getMessage());
        }
    }
}