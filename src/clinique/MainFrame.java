package clinique;

import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MainFrame extends JFrame {

    // Paramètres Oracle validés
    private static final String DB_URL  = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "sys";
    private static final String DB_PASS = "8lXmA8";

    // Barre d'état
    private JLabel lblStatusConnexion;
    private JLabel lblStatusLignes;

    // Onglet 1 : Départements
    private JTable tblDepartements;
    private DefaultTableModel modelDepartements;

    // Onglet 2 : Médecins
    private JTable tblMedecins;
    private DefaultTableModel modelMedecins;

    // Onglet 3 : Patients
    private JTable tblPatients;
    private DefaultTableModel modelPatients;
    private TableRowSorter<DefaultTableModel> sorterPatients;

    // Onglet 4 : Consultations
    private JComboBox<ComboItem> comboPatientsCons;
    private JTable tblConsultations;
    private DefaultTableModel modelConsultations;
    private JLabel lblTotalTarifsPatient;

    // Onglet 5 : Factures
    private JTable tblFactures;
    private DefaultTableModel modelFactures;

    // Onglet 6 : Statistiques
    private JTextArea txtStats;

    public MainFrame() {
        setTitle("Clinique Médicale - Système de Gestion Relationnel-Objet Oracle");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Test de connexion immédiat au démarrage
        verifierConnexionInitiale();

        // Structure principale
        JPanel root = new JPanel(new BorderLayout());

        // Système d'onglets
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        tabs.addTab("1. Départements (Table typée)", creerOngletDepartements());
        tabs.addTab("2. Médecins (Méthode + REF)", creerOngletMedecins());
        tabs.addTab("3. Patients (TAD, VARRAY, REF)", creerOngletPatients());
        tabs.addTab("4. Consultations (Nested Table)", creerOngletConsultations());
        tabs.addTab("5. Factures (Non typée + REF)", creerOngletFactures());
        tabs.addTab("6. Statistiques Globales", creerOngletStatistiques());

        root.add(tabs, BorderLayout.CENTER);

        // Barre d'état inférieure
        root.add(creerBarreEtat(), BorderLayout.SOUTH);

        add(root);

        // Chargement initial des données
        chargerDepartements();
    }

    private Connection getConnection() throws SQLException {
        java.util.Properties props = new java.util.Properties();
        props.put("user", DB_USER);
        props.put("password", DB_PASS);
        props.put("internal_logon", "sysdba");
        return DriverManager.getConnection(DB_URL, props);
    }

    private void verifierConnexionInitiale() {
        try (Connection conn = getConnection()) {
            // Connexion réussie
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Échec critique de connexion à Oracle :\n" + ex.getMessage(),
                "Erreur de Connexion", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel creerBarreEtat() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            new EmptyBorder(5, 10, 5, 10)
        ));
        lblStatusConnexion = new JLabel("Connexion : Connecté à Oracle (sys as sysdba)");
        lblStatusConnexion.setForeground(new Color(39, 174, 96));
        lblStatusConnexion.setFont(new Font("Segoe UI", Font.BOLD, 11));

        lblStatusLignes = new JLabel("Lignes chargées : 0");
        lblStatusLignes.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        bar.add(lblStatusConnexion, BorderLayout.WEST);
        bar.add(lblStatusLignes, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // MODULE 1 : DÉPARTEMENTS (Table Typée)
    // =========================================================================
    private JPanel creerOngletDepartements() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        modelDepartements = new DefaultTableModel(new String[]{"ID Département", "Nom du Service", "Étage", "Nb Médecins affectés (Règle 1,n)"}, 0);
        tblDepartements = new JTable(modelDepartements);
        tblDepartements.setRowHeight(24);

        panel.add(new JScrollPane(tblDepartements), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAct = new JButton("Actualiser");
        JButton btnAdd = new JButton("Ajouter Département");
        JButton btnDel = new JButton("Supprimer Département");

        btnAct.addActionListener(e -> chargerDepartements());
        btnAdd.addActionListener(e -> ajouterDepartement());
        btnDel.addActionListener(e -> supprimerDepartement());

        pnlActions.add(btnAct);
        pnlActions.add(btnAdd);
        pnlActions.add(btnDel);
        panel.add(pnlActions, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerDepartements() {
        modelDepartements.setRowCount(0);
        String sql = "SELECT d.idDep, d.nomDep, d.etage, "
                   + "       (SELECT COUNT(*) FROM Medecins m WHERE m.dept.idDep = d.idDep) "
                   + "FROM Departements d ORDER BY d.idDep";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelDepartements.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4) + " médecin(s)"});
            }
            lblStatusLignes.setText("Départements : " + modelDepartements.getRowCount() + " ligne(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void ajouterDepartement() {
        JTextField id = new JTextField();
        JTextField nom = new JTextField();
        JTextField etage = new JTextField();
        Object[] fields = {"ID :", id, "Nom :", nom, "Étage :", etage};

        if (JOptionPane.showConfirmDialog(this, fields, "Nouveau Département", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String sql = "INSERT INTO Departements VALUES (departement_t(" + id.getText() + ", '" + nom.getText() + "', " + etage.getText() + "))";
            executerDML(sql, "Département ajouté avec succès !");
            chargerDepartements();
        }
    }

    private void supprimerDepartement() {
        int row = tblDepartements.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une ligne !"); return; }
        int id = (int) modelDepartements.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le département " + id + " ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            executerDML("DELETE FROM Departements WHERE idDep = " + id, "Département supprimé !");
            chargerDepartements();
        }
    }

    // =========================================================================
    // MODULE 2 : MÉDECINS (Méthode + REF)
    // =========================================================================
    private JPanel creerOngletMedecins() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        modelMedecins = new DefaultTableModel(new String[]{"ID", "Nom Médecin", "Spécialité", "Salaire Base (DT)", "Département (m.dept.nomDep)", "Patients Suivis"}, 0);
        tblMedecins = new JTable(modelMedecins);
        tblMedecins.setRowHeight(24);

        panel.add(new JScrollPane(tblMedecins), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAct = new JButton("Actualiser");
        JButton btnAdd = new JButton("Ajouter Médecin (REF)");
        JButton btnDel = new JButton("Supprimer");
        JButton btnPrime = new JButton("Calculer Prime (Méthode PL/SQL)");
        btnPrime.setBackground(new Color(230, 126, 34));
        btnPrime.setForeground(Color.WHITE);

        btnAct.addActionListener(e -> chargerMedecins());
        btnAdd.addActionListener(e -> ajouterMedecin());
        btnDel.addActionListener(e -> supprimerMedecin());
        btnPrime.addActionListener(e -> calculerPrimeMedecin());

        pnlActions.add(btnAct);
        pnlActions.add(btnAdd);
        pnlActions.add(btnDel);
        pnlActions.add(btnPrime);
        panel.add(pnlActions, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerMedecins() {
        modelMedecins.setRowCount(0);
        String sql = "SELECT m.idMed, m.nomMed, m.specialite, m.salaireBase, m.dept.nomDep, "
                   + "       (SELECT COUNT(*) FROM Patients p WHERE p.medecinTraitant.idMed = m.idMed) "
                   + "FROM Medecins m ORDER BY m.idMed";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelMedecins.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4) + " DT", rs.getString(5), rs.getInt(6)});
            }
            lblStatusLignes.setText("Médecins : " + modelMedecins.getRowCount() + " ligne(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void ajouterMedecin() {
        JTextField id = new JTextField();
        JTextField nom = new JTextField();
        JTextField spec = new JTextField();
        JTextField sal = new JTextField();
        JComboBox<ComboItem> comboDeps = new JComboBox<>(chargerItemsSQL("SELECT idDep, nomDep FROM Departements ORDER BY idDep"));

        Object[] fields = {"ID Médecin :", id, "Nom :", nom, "Spécialité :", spec, "Salaire :", sal, "Département (REF) :", comboDeps};

        if (JOptionPane.showConfirmDialog(this, fields, "Nouveau Médecin", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem dep = (ComboItem) comboDeps.getSelectedItem();
            String sql = "INSERT INTO Medecins VALUES (medecin_t(" + id.getText() + ", '" + nom.getText() + "', '" + spec.getText() + "', "
                       + sal.getText() + ", (SELECT REF(d) FROM Departements d WHERE d.idDep = " + dep.id + ")))";
            executerDML(sql, "Médecin inséré avec sa référence département !");
            chargerMedecins();
        }
    }

    private void supprimerMedecin() {
        int row = tblMedecins.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un médecin !"); return; }
        int id = (int) modelMedecins.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le médecin " + id + " ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            executerDML("DELETE FROM Medecins WHERE idMed = " + id, "Médecin supprimé !");
            chargerMedecins();
        }
    }

    private void calculerPrimeMedecin() {
        int row = tblMedecins.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez d'abord un médecin dans la table !"); return; }
        int id = (int) modelMedecins.getValueAt(row, 0);
        String nom = modelMedecins.getValueAt(row, 1).toString();

        String tauxStr = JOptionPane.showInputDialog(this, "Entrez le taux de la prime (ex: 0.15 pour 15%, 0.20 pour 20%) :", "0.15");
        if (tauxStr == null || tauxStr.trim().isEmpty()) return;

        try {
            double taux = Double.parseDouble(tauxStr);
            String sql = "SELECT m.calculer_prime(" + taux + ") FROM Medecins m WHERE m.idMed = " + id;
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                        "Appel de la méthode membre m.calculer_prime(" + taux + ") :\n\n"
                        + "Médecin : " + nom + "\n"
                        + "Prime calculée : " + rs.getDouble(1) + " DT",
                        "Résultat Méthode PL/SQL", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    // =========================================================================
    // MODULE 3 : PATIENTS (TAD Imbriqué, VARRAY, REF, Méthode)
    // =========================================================================
    private JPanel creerOngletPatients() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Barre supérieure : Recherche
        JPanel pnlHaut = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlHaut.add(new JLabel("Recherche rapide (Nom ou Ville) :"));
        JTextField txtSearch = new JTextField(18);
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                sorterPatients.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + q));
            }
        });
        pnlHaut.add(txtSearch);
        panel.add(pnlHaut, BorderLayout.NORTH);

        modelPatients = new DefaultTableModel(new String[]{
            "ID", "Nom Patient", "Ville (p.adr.ville)", "Médecin Traitant (REF)", "Âge (p.calculer_age())"
        }, 0);
        tblPatients = new JTable(modelPatients);
        tblPatients.setRowHeight(24);
        sorterPatients = new TableRowSorter<>(modelPatients);
        tblPatients.setRowSorter(sorterPatients);

        panel.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAct = new JButton("Actualiser");
        JButton btnFiche = new JButton("Fiche Détaillée & Téléphones (TABLE(p.tels))");
        JButton btnAdd = new JButton("Ajouter Patient (TAD + VARRAY + REF)");
        JButton btnDel = new JButton("Supprimer Patient");

        btnAct.addActionListener(e -> chargerPatients());
        btnFiche.addActionListener(e -> afficherFicheDetailleePatient());
        btnAdd.addActionListener(e -> ajouterPatient());
        btnDel.addActionListener(e -> supprimerPatient());

        pnlActions.add(btnAct);
        pnlActions.add(btnFiche);
        pnlActions.add(btnAdd);
        pnlActions.add(btnDel);
        panel.add(pnlActions, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerPatients() {
        modelPatients.setRowCount(0);
        String sql = "SELECT p.idPat, p.nomPat, p.adr.ville, p.medecinTraitant.nomMed, p.calculer_age() "
                   + "FROM Patients p ORDER BY p.idPat";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelPatients.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getInt(5) + " ans"});
            }
            lblStatusLignes.setText("Patients : " + modelPatients.getRowCount() + " ligne(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void afficherFicheDetailleePatient() {
        int row = tblPatients.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un patient !"); return; }
        int modelRow = tblPatients.convertRowIndexToModel(row);
        int idPat = (int) modelPatients.getValueAt(modelRow, 0);

        String sql = "SELECT p.nomPat, TO_CHAR(p.dateNaissance, 'DD/MM/YYYY'), p.calculer_age(), "
                   + "       p.adr.rue, p.adr.ville, p.adr.codePostal, "
                   + "       p.medecinTraitant.nomMed, p.medecinTraitant.dept.nomDep, "
                   + "       (SELECT NVL(SUM(c.tarif), 0) FROM TABLE(p.historique) c) "
                   + "FROM Patients p WHERE p.idPat = " + idPat;

        String sqlTels = "SELECT COLUMN_VALUE FROM Patients p, TABLE(p.tels) WHERE p.idPat = " + idPat;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                StringBuilder sb = new StringBuilder("=== FICHE PATIENT DÉTAILLÉE ===\n\n");
                sb.append("Nom : ").append(rs.getString(1)).append("\n");
                sb.append("Date Naissance : ").append(rs.getString(2)).append(" (").append(rs.getInt(3)).append(" ans)\n\n");
                sb.append("ADRESSE (TAD composite adresse_t) :\n");
                sb.append("  ").append(rs.getString(4)).append(", ").append(rs.getString(5)).append(" (").append(rs.getInt(6)).append(")\n\n");
                sb.append("MÉDECIN TRAITANT (Navigation REF) :\n");
                sb.append("  ").append(rs.getString(7)).append(" [Département : ").append(rs.getString(8)).append("]\n\n");

                // Téléphones via TABLE(p.tels)
                sb.append("TÉLÉPHONES D'URGENCE (Déroulés du VARRAY via TABLE(p.tels)) :\n");
                ResultSet rsT = stmt.executeQuery(sqlTels);
                int count = 1;
                while (rsT.next()) {
                    sb.append("  Contact ").append(count++).append(" : ").append(rsT.getString(1)).append("\n");
                }

                sb.append("\nTOTAL DES CONSULTATIONS : ").append(rs.getDouble(9)).append(" DT\n");

                JTextArea area = new JTextArea(sb.toString(), 18, 45);
                area.setEditable(false);
                JOptionPane.showMessageDialog(this, new JScrollPane(area), "Fiche Patient", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void ajouterPatient() {
        JTextField id = new JTextField();
        JTextField nom = new JTextField();
        JTextField dateN = new JTextField("15/04/1995");
        JTextField rue = new JTextField("Rue de la Liberté");
        JTextField ville = new JTextField("Tunis");
        JTextField cp = new JTextField("1001");
        JTextField tel1 = new JTextField("98111222");
        JTextField tel2 = new JTextField("55444333");
        JTextField tel3 = new JTextField();
        JComboBox<ComboItem> comboMeds = new JComboBox<>(chargerItemsSQL("SELECT idMed, nomMed FROM Medecins ORDER BY idMed"));

        Object[] fields = {
            "ID Patient :", id, "Nom :", nom, "Date Naissance (JJ/MM/AAAA) :", dateN,
            "-- TAD ADRESSE_T --", new JSeparator(),
            "Rue :", rue, "Ville :", ville, "Code Postal :", cp,
            "-- VARRAY TELEPHONES (Max 3) --", new JSeparator(),
            "Téléphone 1 (Obligatoire) :", tel1,
            "Téléphone 2 (Optionnel) :", tel2,
            "Téléphone 3 (Optionnel) :", tel3,
            "-- MÉDECIN TRAITANT (REF) --", new JSeparator(),
            "Médecin référent :", comboMeds
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Nouveau Patient Objet", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            // Construction propre du VARRAY
            Vector<String> tels = new Vector<>();
            if (!tel1.getText().trim().isEmpty()) tels.add("'" + tel1.getText().trim() + "'");
            if (!tel2.getText().trim().isEmpty()) tels.add("'" + tel2.getText().trim() + "'");
            if (!tel3.getText().trim().isEmpty()) tels.add("'" + tel3.getText().trim() + "'");

            String varraySql = "telephones_va(" + String.join(", ", tels) + ")";
            ComboItem med = (ComboItem) comboMeds.getSelectedItem();

            String sql = "INSERT INTO Patients VALUES (patient_t("
                       + id.getText() + ", '" + nom.getText() + "', TO_DATE('" + dateN.getText() + "', 'DD/MM/YYYY'), "
                       + "adresse_t('" + rue.getText() + "', '" + ville.getText() + "', " + cp.getText() + "), "
                       + varraySql + ", consultations_nt(), "
                       + "(SELECT REF(m) FROM Medecins m WHERE m.idMed = " + med.id + ")))";

            executerDML(sql, "Patient créé avec TAD, VARRAY et REF !");
            chargerPatients();
        }
    }

    private void supprimerPatient() {
        int row = tblPatients.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un patient !"); return; }
        int modelRow = tblPatients.convertRowIndexToModel(row);
        int id = (int) modelPatients.getValueAt(modelRow, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le patient " + id + " et tout son historique médical ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            executerDML("DELETE FROM Patients WHERE idPat = " + id, "Patient et historique supprimés !");
            chargerPatients();
        }
    }

    // =========================================================================
    // MODULE 4 : CONSULTATIONS (Nested Table)
    // =========================================================================
    private JPanel creerOngletConsultations() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Barre supérieure : Sélection patient + Total
        JPanel pnlHaut = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlHaut.add(new JLabel("Choisir un Patient :"));
        comboPatientsCons = new JComboBox<>();
        comboPatientsCons.setPreferredSize(new Dimension(220, 26));
        comboPatientsCons.addActionListener(e -> chargerHistoriquePatientSelectionne());
        pnlHaut.add(comboPatientsCons);

        lblTotalTarifsPatient = new JLabel("Total Dépenses : 0 DT");
        lblTotalTarifsPatient.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalTarifsPatient.setForeground(new Color(142, 68, 173));
        pnlHaut.add(lblTotalTarifsPatient);

        panel.add(pnlHaut, BorderLayout.NORTH);

        modelConsultations = new DefaultTableModel(new String[]{"N° Acte", "Date Consultation", "Diagnostic / Motif", "Tarif (DT)"}, 0);
        tblConsultations = new JTable(modelConsultations);
        tblConsultations.setRowHeight(24);
        panel.add(new JScrollPane(tblConsultations), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAct = new JButton("Actualiser");
        JButton btnAdd = new JButton("Ajouter Acte Médical (INSERT INTO TABLE)");
        JButton btnDel = new JButton("Supprimer Acte (DELETE FROM TABLE)");

        btnAct.addActionListener(e -> rafraichirComboPatients());
        btnAdd.addActionListener(e -> ajouterConsultation());
        btnDel.addActionListener(e -> supprimerConsultation());

        pnlActions.add(btnAct);
        pnlActions.add(btnAdd);
        pnlActions.add(btnDel);
        panel.add(pnlActions, BorderLayout.SOUTH);

        return panel;
    }

    private void rafraichirComboPatients() {
        comboPatientsCons.removeAllItems();
        Vector<ComboItem> items = chargerItemsSQL("SELECT idPat, nomPat FROM Patients ORDER BY idPat");
        for (ComboItem it : items) comboPatientsCons.addItem(it);
        chargerHistoriquePatientSelectionne();
    }

    private void chargerHistoriquePatientSelectionne() {
        modelConsultations.setRowCount(0);
        ComboItem pat = (ComboItem) comboPatientsCons.getSelectedItem();
        if (pat == null) return;

        String sql = "SELECT c.numCons, TO_CHAR(c.dateCons, 'DD/MM/YYYY'), c.motif, c.tarif "
                   + "FROM Patients p, TABLE(p.historique) c "
                   + "WHERE p.idPat = " + pat.id + " ORDER BY c.numCons";

        String sqlTotal = "SELECT NVL(SUM(c.tarif), 0) FROM Patients p, TABLE(p.historique) c WHERE p.idPat = " + pat.id;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                modelConsultations.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4) + " DT"});
            }

            ResultSet rsTot = stmt.executeQuery(sqlTotal);
            if (rsTot.next()) {
                lblTotalTarifsPatient.setText("Total Dépenses : " + rsTot.getDouble(1) + " DT");
            }
            lblStatusLignes.setText("Consultations du patient : " + modelConsultations.getRowCount() + " acte(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void ajouterConsultation() {
        ComboItem pat = (ComboItem) comboPatientsCons.getSelectedItem();
        if (pat == null) return;

        JTextField dateC = new JTextField("10/02/2026");
        JTextField motif = new JTextField();
        JTextField tarif = new JTextField();

        Object[] fields = {"Date (JJ/MM/AAAA) :", dateC, "Motif / Diagnostic :", motif, "Tarif (DT) :", tarif};

        if (JOptionPane.showConfirmDialog(this, fields, "Nouvel Acte Médical (Nested Table)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String sql = "INSERT INTO TABLE(SELECT p.historique FROM Patients p WHERE p.idPat = " + pat.id + ") "
                       + "VALUES (consultation_t( "
                       + "  NVL((SELECT MAX(c.numCons) FROM Patients pt, TABLE(pt.historique) c), 0) + 1, "
                       + "  TO_DATE('" + dateC.getText() + "', 'DD/MM/YYYY'), '" + motif.getText() + "', " + tarif.getText() + "))";

            executerDML(sql, "Acte inséré directement dans la Nested Table !");
            chargerHistoriquePatientSelectionne();
        }
    }

    private void supprimerConsultation() {
        int row = tblConsultations.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un acte médical !"); return; }
        int numCons = (int) modelConsultations.getValueAt(row, 0);
        ComboItem pat = (ComboItem) comboPatientsCons.getSelectedItem();

        String sql = "DELETE FROM TABLE(SELECT p.historique FROM Patients p WHERE p.idPat = " + pat.id + ") c "
                   + "WHERE c.numCons = " + numCons;

        executerDML(sql, "Acte supprimé de la Nested Table !");
        chargerHistoriquePatientSelectionne();
    }

    // =========================================================================
    // MODULE 5 : FACTURES (Table Non Typée + REF)
    // =========================================================================
    private JPanel creerOngletFactures() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        modelFactures = new DefaultTableModel(new String[]{
            "N° Facture", "Date Facture", "Montant (DT)", "Patient (REF)", "Ville Facturation (REF)", "Médecin Émetteur (REF)", "N° Acte"
        }, 0);
        tblFactures = new JTable(modelFactures);
        tblFactures.setRowHeight(24);

        panel.add(new JScrollPane(tblFactures), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAct = new JButton("Actualiser");
        JButton btnAdd = new JButton("Créer Facture (Génération Auto)");
        JButton btnDel = new JButton("Supprimer Facture");

        btnAct.addActionListener(e -> chargerFactures());
        btnAdd.addActionListener(e -> creerFactureAuto());
        btnDel.addActionListener(e -> supprimerFacture());

        pnlActions.add(btnAct);
        pnlActions.add(btnAdd);
        pnlActions.add(btnDel);
        panel.add(pnlActions, BorderLayout.SOUTH);

        return panel;
    }

    private void chargerFactures() {
        modelFactures.setRowCount(0);
        String sql = "SELECT f.numFacture, TO_CHAR(f.dateFacture, 'DD/MM/YYYY'), f.montant, "
                   + "       f.patientConcerne.nomPat, f.patientConcerne.adr.ville, "
                   + "       f.medecinEmetteur.nomMed, f.numCons "
                   + "FROM Factures f ORDER BY f.numFacture";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelFactures.addRow(new Object[]{
                    rs.getInt(1), rs.getString(2), rs.getDouble(3) + " DT",
                    rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7)
                });
            }
            lblStatusLignes.setText("Factures : " + modelFactures.getRowCount() + " ligne(s)");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void creerFactureAuto() {
        JTextField numF = new JTextField();
        JComboBox<ComboItem> comboP = new JComboBox<>(chargerItemsSQL("SELECT idPat, nomPat FROM Patients ORDER BY idPat"));
        JComboBox<ComboItem> comboM = new JComboBox<>(chargerItemsSQL("SELECT idMed, nomMed FROM Medecins ORDER BY idMed"));
        JTextField txtMontant = new JTextField();

        JButton btnCalculer = new JButton("Calculer à partir des actes du patient");
        btnCalculer.addActionListener(e -> {
            ComboItem p = (ComboItem) comboP.getSelectedItem();
            if (p != null) {
                String sqlTot = "SELECT NVL(SUM(c.tarif), 0) FROM Patients pt, TABLE(pt.historique) c WHERE pt.idPat = " + p.id;
                try (Connection conn = getConnection(); Statement s = conn.createStatement(); ResultSet r = s.executeQuery(sqlTot)) {
                    if (r.next()) txtMontant.setText(String.valueOf(r.getDouble(1)));
                } catch (Exception ignored) {}
            }
        });

        Object[] fields = {"N° Facture :", numF, "Patient Débiteur (REF) :", comboP, "Médecin (REF) :", comboM, btnCalculer, "Montant Total (DT) :", txtMontant};

        if (JOptionPane.showConfirmDialog(this, fields, "Nouvelle Facture", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem p = (ComboItem) comboP.getSelectedItem();
            ComboItem m = (ComboItem) comboM.getSelectedItem();
            String sql = "INSERT INTO Factures VALUES (" + numF.getText() + ", SYSDATE, " + txtMontant.getText() + ", "
                       + "(SELECT REF(pt) FROM Patients pt WHERE pt.idPat = " + p.id + "), "
                       + "(SELECT REF(md) FROM Medecins md WHERE md.idMed = " + m.id + "), 1)";

            executerDML(sql, "Facture générée dans la table non typée !");
            chargerFactures();
        }
    }

    private void supprimerFacture() {
        int row = tblFactures.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une facture !"); return; }
        int num = (int) modelFactures.getValueAt(row, 0);

        executerDML("DELETE FROM Factures WHERE numFacture = " + num, "Facture supprimée !");
        chargerFactures();
    }

    // =========================================================================
    // MODULE 6 : STATISTIQUES GLOBALES
    // =========================================================================
    private JPanel creerOngletStatistiques() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtStats = new JTextArea();
        txtStats.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtStats.setEditable(false);
        panel.add(new JScrollPane(txtStats), BorderLayout.CENTER);

        JButton btnCalculer = new JButton("Calculer & Rafraîchir les Indicateurs Statistiques");
        btnCalculer.setBackground(new Color(41, 128, 185));
        btnCalculer.setForeground(Color.WHITE);
        btnCalculer.addActionListener(e -> genererRapportStatistique());
        panel.add(btnCalculer, BorderLayout.SOUTH);

        return panel;
    }

    private void genererRapportStatistique() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================================================\n");
        sb.append("                     RAPPORT STATISTIQUE DE LA CLINIQUE MÉDICALE                  \n");
        sb.append("==================================================================================\n\n");

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // 1. Âge moyen des patients
            ResultSet rs = stmt.executeQuery("SELECT ROUND(AVG(p.calculer_age()), 1) FROM Patients p");
            if (rs.next()) sb.append("1. ÂGE MOYEN DES PATIENTS (Calculé via p.calculer_age()) : ").append(rs.getDouble(1)).append(" ans\n\n");

            // 2. Nombre de médecins par département
            sb.append("2. NOMBRE DE MÉDECINS PAR DÉPARTEMENT :\n");
            rs = stmt.executeQuery("SELECT d.nomDep, COUNT(m.idMed) FROM Departements d LEFT JOIN Medecins m ON m.dept.idDep = d.idDep GROUP BY d.nomDep ORDER BY 2 DESC");
            while (rs.next()) {
                sb.append("   - ").append(String.format("%-20s", rs.getString(1))).append(" : ").append(rs.getInt(2)).append(" médecin(s)\n");
            }
            sb.append("\n");

            // 3. Nombre de patients par médecin
            sb.append("3. NOMBRE DE PATIENTS SUIVIS PAR MÉDECIN :\n");
            rs = stmt.executeQuery("SELECT m.nomMed, COUNT(p.idPat) FROM Medecins m LEFT JOIN Patients p ON p.medecinTraitant.idMed = m.idMed GROUP BY m.nomMed ORDER BY 2 DESC");
            while (rs.next()) {
                sb.append("   - ").append(String.format("%-20s", rs.getString(1))).append(" : ").append(rs.getInt(2)).append(" patient(s)\n");
            }
            sb.append("\n");

            // 4. Chiffre d'affaires par médecin
            sb.append("4. CHIFFRE D'AFFAIRES PAR MÉDECIN (Cumul des Factures) :\n");
            rs = stmt.executeQuery("SELECT m.nomMed, NVL(SUM(f.montant), 0) FROM Medecins m LEFT JOIN Factures f ON f.medecinEmetteur.idMed = m.idMed GROUP BY m.nomMed ORDER BY 2 DESC");
            while (rs.next()) {
                sb.append("   - ").append(String.format("%-20s", rs.getString(1))).append(" : ").append(rs.getDouble(2)).append(" DT\n");
            }
            sb.append("\n");

            // 5. Les 3 actes médicaux les plus chers
            sb.append("5. ACTES MÉDICAUX LES PLUS COÛTEUX (Extraction Nested Table) :\n");
            rs = stmt.executeQuery("SELECT p.nomPat, c.motif, c.tarif FROM Patients p, TABLE(p.historique) c WHERE ROWNUM <= 5 ORDER BY c.tarif DESC");
            while (rs.next()) {
                sb.append("   - Patient : ").append(String.format("%-18s", rs.getString(1)))
                  .append(" | Acte : ").append(String.format("%-22s", rs.getString(2)))
                  .append(" | Tarif : ").append(rs.getDouble(3)).append(" DT\n");
            }

            txtStats.setText(sb.toString());
            lblStatusLignes.setText("Statistiques calculées avec succès.");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur Stats : " + ex.getMessage());
        }
    }

    // =========================================================================
    // UTILITAIRES DML ET COMBOS
    // =========================================================================
    private void executerDML(String sql, String messageSucces) {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            conn.commit();
            JOptionPane.showMessageDialog(this, messageSucces, "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Vector<ComboItem> chargerItemsSQL(String sql) {
        Vector<ComboItem> list = new Vector<>();
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        } catch (SQLException ignored) {}
        return list;
    }

    private static class ComboItem {
        int id;
        String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label + " (ID: " + id + ")"; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}