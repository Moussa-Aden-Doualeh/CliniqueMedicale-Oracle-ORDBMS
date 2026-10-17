package clinique.ui;

import clinique.dao.MedecinDAO;
import clinique.dao.PatientDAO;
import clinique.model.ComboItem;
import clinique.model.Patient;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class PatientView extends JPanel {

    private TableRowSorter<DefaultTableModel> sorterPatients;
    private final PatientDAO patientDAO = new PatientDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JTextField txtPatId, txtPatNom, txtPatDateN, txtPatSexe, txtPatAge, txtPatRue, txtPatVille, txtPatCP, txtPatMedecin;
    private JComboBox<String> comboPatTels;
    private JTable tblPatients;
    private DefaultTableModel modelPatients;

    public PatientView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. FORMULAIRE DE GAUCHE
        JPanel pnlForm = new JPanel(new GridLayout(10, 2, 5, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlForm.setPreferredSize(new Dimension(280, 0));

        txtPatId = creerChampLecture();
        txtPatNom = creerChampLecture();
        txtPatDateN = creerChampLecture();
        txtPatSexe = creerChampLecture();
        txtPatAge = creerChampLecture();
        txtPatVille = creerChampLecture();
        txtPatCP = creerChampLecture();
        comboPatTels = new JComboBox<>();
        comboPatTels.setBackground(Color.WHITE);
        txtPatMedecin = creerChampLecture();

        pnlForm.add(creerLabel("ID :"));          pnlForm.add(txtPatId);
        pnlForm.add(creerLabel("Nom :"));         pnlForm.add(txtPatNom);
        pnlForm.add(creerLabel("Date Naiss :"));  pnlForm.add(txtPatDateN);
        pnlForm.add(creerLabel("Sexe :"));        pnlForm.add(txtPatSexe);
        pnlForm.add(creerLabel("Âge :"));         pnlForm.add(txtPatAge);
        pnlForm.add(creerLabel("Ville :"));       pnlForm.add(txtPatVille);
        pnlForm.add(creerLabel("Code Postal :")); pnlForm.add(txtPatCP);
        pnlForm.add(creerLabel("Phones :"));      pnlForm.add(comboPatTels);
        pnlForm.add(creerLabel("Médecin :"));     pnlForm.add(txtPatMedecin);

        add(pnlForm, BorderLayout.WEST);

        // 2. PANNEAU DE DROITE
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        // Modèle et tableau
        modelPatients = new DefaultTableModel(new String[]{"ID", "Nom", "Sexe", "Âge", "Ville", "Médecin traitant"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPatients = new JTable(modelPatients);
        tblPatients.setRowHeight(24);

        // Activation du Sorter pour la recherche en temps réel
        sorterPatients = new TableRowSorter<>(modelPatients);
        tblPatients.setRowSorter(sorterPatients);

        // Écouteur de sélection sécurisé avec convertRowIndexToModel
        tblPatients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPatients.getSelectedRow() != -1) {
                int r = tblPatients.convertRowIndexToModel(tblPatients.getSelectedRow());
                int idPat = (int) modelPatients.getValueAt(r, 0);
                remplirFiche(idPat);
            }
        });

        // EN-TÊTE PROFESSIONNEL AVEC CAPSULE DE RECHERCHE
        JPanel pnlTableTop = new JPanel(new BorderLayout(10, 5));
        pnlTableTop.setOpaque(false);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel lblTitreTable = new JLabel("Dossier Patients (Table Typée OID)");
        lblTitreTable.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitreTable.setForeground(new Color(30, 41, 59));
        pnlTableTop.add(lblTitreTable, BorderLayout.WEST);

        // Capsule
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setPreferredSize(new Dimension(230, 28));
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(2, 8, 2, 6)
        ));

        JLabel lblIconSearch = new JLabel();
        ImageIcon iconSearch = chargerIcone("search.png", 14, 14);
        if (iconSearch != null) lblIconSearch.setIcon(iconSearch); else lblIconSearch.setText("🔍");
        pnlSearchBox.add(lblIconSearch, BorderLayout.WEST);

        String placeholder = "Filtrer nom, ville, médecin...";
        JTextField txtSearch = new JTextField(placeholder);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearch.setForeground(new Color(148, 163, 184));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        JLabel btnClear = new JLabel("", SwingConstants.CENTER);
        ImageIcon iconClear = chargerIcone("clear.png", 12, 12);
        if (iconClear != null) btnClear.setIcon(iconClear); else btnClear.setText("✕");
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setVisible(false);
        pnlSearchBox.add(btnClear, BorderLayout.EAST);

        // Gestion du Placeholder et du Filtrage
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals(placeholder)) {
                    txtSearch.setText("");
                    txtSearch.setForeground(new Color(30, 41, 59));
                }
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().trim().isEmpty()) {
                    txtSearch.setText(placeholder);
                    txtSearch.setForeground(new Color(148, 163, 184));
                    sorterPatients.setRowFilter(null);
                    btnClear.setVisible(false);
                }
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                if (q.isEmpty() || q.equals(placeholder)) {
                    sorterPatients.setRowFilter(null);
                    btnClear.setVisible(false);
                } else {
                    btnClear.setVisible(true);
                    sorterPatients.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                }
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtSearch.setText("");
                sorterPatients.setRowFilter(null);
                btnClear.setVisible(false);
                txtSearch.requestFocus();
            }
        });

        pnlTableTop.add(pnlSearchBox, BorderLayout.EAST);
        pnlTable.add(pnlTableTop, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

        // Barre d'actions CRUD en bas
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 5));
        pnlActions.setOpaque(false);

        JButton btnAdd = creerBouton(" Ajouter", new Color(39, 174, 96), "add.png");
        JButton btnMod = creerBouton(" Modifier", new Color(41, 128, 185), "edit.png");
        JButton btnDel = creerBouton(" Supprimer", new Color(192, 57, 43), "delete.png");
        JButton btnAct = creerBouton(" Actualiser", new Color(100, 116, 139), "refresh.png");

        btnAdd.addActionListener(e -> dialogueAjouter());
        btnMod.addActionListener(e -> dialogueModifier());
        btnDel.addActionListener(e -> actionSupprimer());
        btnAct.addActionListener(e -> charger());

        pnlActions.add(btnAdd);
        pnlActions.add(btnMod);
        pnlActions.add(btnDel);
        pnlActions.add(btnAct);
        pnlTable.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTable, BorderLayout.CENTER);
        charger();
    }

    public void charger() {
        modelPatients.setRowCount(0);
        try {
            List<Patient> liste = patientDAO.getTous();
            for (Patient p : liste) {
                modelPatients.addRow(new Object[]{
                    p.getIdPat(), p.getNomPat(),
                    (p.getSexe().equals("M") ? "M" : "F"),
                    p.getAge() + " ans", p.getVille(), p.getNomMedecin()
                });
            }
            if (tblPatients.getRowCount() > 0) tblPatients.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void remplirFiche(int idPat) {
        try {
            Patient p = patientDAO.getFicheComplete(idPat);
            if (p != null) {
                txtPatId.setText(String.valueOf(p.getIdPat()));
                txtPatNom.setText(p.getNomPat());
                txtPatDateN.setText(p.getDateNaissance());
                txtPatSexe.setText(p.getSexe().equals("M") ? "Masculin" : "Féminin");
                txtPatAge.setText(p.getAge() + " ans");
                txtPatVille.setText(p.getVille());
                txtPatCP.setText(String.valueOf(p.getCodePostal()));
                txtPatMedecin.setText(p.getNomMedecin());

                comboPatTels.removeAllItems();
                for (String t : p.getTels()) comboPatTels.addItem(t);
            }
        } catch (SQLException ignored) {}
    }

    // BOÎTE DE DIALOGUE : AJOUTER UN PATIENT 
    private void dialogueAjouter() {
        int prochainId = 1001;
        try {
            prochainId = patientDAO.getNextId();
        } catch (Exception ignored) {}

        JTextField txtId = new JTextField(String.valueOf(prochainId));
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245));
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtNom = new JTextField();

        // Sélecteur Jour / Mois / Année
        String[] jours = new String[31];
        for (int i = 1; i <= 31; i++) jours[i - 1] = String.format("%02d", i);
        JComboBox<String> cbJour = new JComboBox<>(jours);

        String[] mois = {
            "01 - Janvier", "02 - Février", "03 - Mars", "04 - Avril", "05 - Mai", "06 - Juin",
            "07 - Juillet", "08 - Août", "09 - Septembre", "10 - Octobre", "11 - Novembre", "12 - Décembre"
        };
        JComboBox<String> cbMois = new JComboBox<>(mois);

        int anneeActuelle = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        String[] annees = new String[anneeActuelle - 1930 + 1];
        for (int a = anneeActuelle, idx = 0; a >= 1930; a--, idx++) {
            annees[idx] = String.valueOf(a);
        }
        JComboBox<String> cbAnnee = new JComboBox<>(annees);
        cbAnnee.setSelectedItem("1998");

        JPanel pnlDate = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlDate.setOpaque(false);
        pnlDate.add(cbJour);
        pnlDate.add(cbMois);
        pnlDate.add(cbAnnee);

        JComboBox<String> comboSexe = new JComboBox<>(new String[]{"Masculin", "Féminin"});

        JTextField txtRue = new JTextField();
        String[] villesTunisie = {
            "Tunis", "Ariana", "Ben Arous", "Manouba", "Sousse",
            "Sfax", "Bizerte", "Nabeul", "Monastir", "Kairouan", "Gabès", "Médenine"
        };
        JComboBox<String> comboVille = new JComboBox<>(villesTunisie);
        JTextField txtCP = new JTextField("1001");
        txtCP.setEditable(false);
        txtCP.setBackground(new Color(245, 245, 245));

        comboVille.addActionListener(e -> {
            String v = (String) comboVille.getSelectedItem();
            switch (v) {
                case "Tunis":      txtCP.setText("1001"); break;
                case "Ariana":     txtCP.setText("2080"); break;
                case "Ben Arous":  txtCP.setText("2013"); break;
                case "Manouba":    txtCP.setText("2010"); break;
                case "Sousse":     txtCP.setText("4000"); break;
                case "Sfax":       txtCP.setText("3000"); break;
                case "Bizerte":    txtCP.setText("7000"); break;
                case "Nabeul":     txtCP.setText("8000"); break;
                case "Monastir":   txtCP.setText("5000"); break;
                case "Kairouan":   txtCP.setText("3100"); break;
                case "Gabès":      txtCP.setText("6000"); break;
                case "Médenine":   txtCP.setText("4100"); break;
                default:           txtCP.setText("1000"); break;
            }
        });

        JComboBox<ComboItem> comboMeds = new JComboBox<>();
        try {
            for (ComboItem it : medecinDAO.getComboItems()) comboMeds.addItem(it);
        } catch (SQLException ignored) {}

        // Téléphones
        JTextField txtTel1 = new JTextField();
        JTextField txtTel2 = new JTextField();
        JTextField txtTel3 = new JTextField();

        JLabel lblTel2 = new JLabel("Téléphone 2 (Urgence) :");
        lblTel2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTel2.setForeground(COLOR_TEXT_LABEL);
        lblTel2.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTel2.setVisible(false);
        txtTel2.setVisible(false);

        JLabel lblTel3 = new JLabel("Téléphone 3 (Proche) :");
        lblTel3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTel3.setForeground(COLOR_TEXT_LABEL);
        lblTel3.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTel3.setVisible(false);
        txtTel3.setVisible(false);

        JButton btnPlusTel = new JButton("+ Ajouter");
        btnPlusTel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPlusTel.setBackground(new Color(39, 174, 96));
        btnPlusTel.setForeground(Color.WHITE);
        btnPlusTel.setFocusPainted(false);

        JButton btnMoinsTel = new JButton("- Retirer");
        btnMoinsTel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnMoinsTel.setBackground(new Color(192, 57, 43));
        btnMoinsTel.setForeground(Color.WHITE);
        btnMoinsTel.setFocusPainted(false);
        btnMoinsTel.setEnabled(false);

        JPanel pnlBoutonsTels = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlBoutonsTels.setOpaque(false);
        pnlBoutonsTels.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlBoutonsTels.add(btnPlusTel);
        pnlBoutonsTels.add(btnMoinsTel);

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(360, 500));

        btnPlusTel.addActionListener(e -> {
            if (!txtTel2.isVisible()) {
                lblTel2.setVisible(true);
                txtTel2.setVisible(true);
                btnMoinsTel.setEnabled(true);
            } else if (!txtTel3.isVisible()) {
                lblTel3.setVisible(true);
                txtTel3.setVisible(true);
                btnPlusTel.setEnabled(false);
            }
            Window w = SwingUtilities.getWindowAncestor(mainForm);
            if (w != null) w.pack();
        });

        btnMoinsTel.addActionListener(e -> {
            if (txtTel3.isVisible()) {
                txtTel3.setText("");
                lblTel3.setVisible(false);
                txtTel3.setVisible(false);
                btnPlusTel.setEnabled(true);
            } else if (txtTel2.isVisible()) {
                txtTel2.setText("");
                lblTel2.setVisible(false);
                txtTel2.setVisible(false);
                btnMoinsTel.setEnabled(false);
            }
            Window w = SwingUtilities.getWindowAncestor(mainForm);
            if (w != null) w.pack();
        });

        JLabel sec1 = new JLabel("ADRESSE");
        sec1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sec1.setForeground(new Color(100, 116, 139));

        JLabel sec2 = new JLabel("COLLECTION TÉLÉPHONES (1 à 3 Max)");
        sec2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sec2.setForeground(new Color(100, 116, 139));

        JLabel sec3 = new JLabel("MÉDECIN TRAITANT");
        sec3.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sec3.setForeground(new Color(100, 116, 139));

        ajouterComposant(mainForm, creerLabelForm("ID Patient :"));
        ajouterComposant(mainForm, txtId);
        mainForm.add(Box.createVerticalStrut(5));

        ajouterComposant(mainForm, creerLabelForm("Nom & Prénom du Patient :"));
        ajouterComposant(mainForm, txtNom);
        mainForm.add(Box.createVerticalStrut(5));

        ajouterComposant(mainForm, creerLabelForm("Date de Naissance :"));
        ajouterComposant(mainForm, pnlDate);
        mainForm.add(Box.createVerticalStrut(5));

        ajouterComposant(mainForm, creerLabelForm("Sexe :"));
        ajouterComposant(mainForm, comboSexe);
        mainForm.add(Box.createVerticalStrut(10));

        ajouterComposant(mainForm, sec1);
        ajouterComposant(mainForm, new JSeparator());
        ajouterComposant(mainForm, creerLabelForm("Rue :"));
        ajouterComposant(mainForm, txtRue);
        ajouterComposant(mainForm, creerLabelForm("Ville :"));
        ajouterComposant(mainForm, comboVille);
        ajouterComposant(mainForm, creerLabelForm("Code Postal :"));
        ajouterComposant(mainForm, txtCP);
        mainForm.add(Box.createVerticalStrut(10));

        ajouterComposant(mainForm, sec2);
        ajouterComposant(mainForm, new JSeparator());
        ajouterComposant(mainForm, creerLabelForm("Téléphone 1 (Principal) :"));
        ajouterComposant(mainForm, txtTel1);
        ajouterComposant(mainForm, lblTel2);
        ajouterComposant(mainForm, txtTel2);
        ajouterComposant(mainForm, lblTel3);
        ajouterComposant(mainForm, txtTel3);
        mainForm.add(Box.createVerticalStrut(4));
        ajouterComposant(mainForm, pnlBoutonsTels);
        mainForm.add(Box.createVerticalStrut(10));

        ajouterComposant(mainForm, sec3);
        ajouterComposant(mainForm, new JSeparator());
        ajouterComposant(mainForm, comboMeds);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Nouveau Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (txtNom.getText().trim().isEmpty() || txtTel1.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez renseigner au moins le Nom et le Téléphone principal !", "Champs obligatoires", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String moisNumero = cbMois.getSelectedItem().toString().substring(0, 2);
                String dateFinale = cbJour.getSelectedItem() + "/" + moisNumero + "/" + cbAnnee.getSelectedItem();

                Vector<String> tels = new Vector<>();
                tels.add("'" + txtTel1.getText().trim() + "'");
                if (txtTel2.isVisible() && !txtTel2.getText().trim().isEmpty()) {
                    tels.add("'" + txtTel2.getText().trim() + "'");
                }
                if (txtTel3.isVisible() && !txtTel3.getText().trim().isEmpty()) {
                    tels.add("'" + txtTel3.getText().trim() + "'");
                }
                String varraySql = "telephones_va(" + String.join(", ", tels) + ")";
                String sexeChoisi = comboSexe.getSelectedItem().toString().substring(0, 1);
                ComboItem med = (ComboItem) comboMeds.getSelectedItem();

                patientDAO.ajouter(
                    Integer.parseInt(txtId.getText().trim()),
                    txtNom.getText().trim(),
                    dateFinale,
                    sexeChoisi,
                    txtRue.getText().trim(),
                    comboVille.getSelectedItem().toString(),
                    Integer.parseInt(txtCP.getText().trim()),
                    varraySql,
                    med.getId()
                );

                JOptionPane.showMessageDialog(this, "Patient créé avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'insertion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // BOÎTE DE DIALOGUE : MODIFIER UN PATIENT
    private void dialogueModifier() {
        int viewRow = tblPatients.getSelectedRow();
        if (viewRow == -1) { 
            JOptionPane.showMessageDialog(this, "Veuillez d'abord sélectionner un patient dans le tableau !"); 
            return; 
        }
        int r = tblPatients.convertRowIndexToModel(viewRow);
        int id = (int) modelPatients.getValueAt(r, 0);

        try {
            Patient p = patientDAO.getFicheComplete(id);
            if (p == null) return;

            JTextField txtId = new JTextField(String.valueOf(id));
            txtId.setEditable(false);
            txtId.setBackground(new Color(245, 245, 245));
            txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

            JTextField txtNom = new JTextField(p.getNomPat());

            String[] jours = new String[31];
            for (int i = 1; i <= 31; i++) jours[i - 1] = String.format("%02d", i);
            JComboBox<String> cbJour = new JComboBox<>(jours);

            String[] mois = {
                "01 - Janvier", "02 - Février", "03 - Mars", "04 - Avril", "05 - Mai", "06 - Juin",
                "07 - Juillet", "08 - Août", "09 - Septembre", "10 - Octobre", "11 - Novembre", "12 - Décembre"
            };
            JComboBox<String> cbMois = new JComboBox<>(mois);

            int anneeActuelle = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            String[] annees = new String[anneeActuelle - 1930 + 1];
            for (int a = anneeActuelle, idx = 0; a >= 1930; a--, idx++) {
                annees[idx] = String.valueOf(a);
            }
            JComboBox<String> cbAnnee = new JComboBox<>(annees);

            try {
                String[] partsDate = p.getDateNaissance().split("/");
                if (partsDate.length == 3) {
                    cbJour.setSelectedItem(partsDate[0]);
                    int moisIdx = Integer.parseInt(partsDate[1]) - 1;
                    if (moisIdx >= 0 && moisIdx < 12) cbMois.setSelectedIndex(moisIdx);
                    cbAnnee.setSelectedItem(partsDate[2]);
                }
            } catch (Exception ignored) {}

            JPanel pnlDate = new JPanel(new GridLayout(1, 3, 5, 0));
            pnlDate.setOpaque(false);
            pnlDate.add(cbJour);
            pnlDate.add(cbMois);
            pnlDate.add(cbAnnee);

            JComboBox<String> comboSexe = new JComboBox<>(new String[]{"Masculin", "Féminin"});
            if (p.getSexe() != null && p.getSexe().equalsIgnoreCase("F")) {
                comboSexe.setSelectedIndex(1);
            } else {
                comboSexe.setSelectedIndex(0);
            }

            JTextField txtRue = new JTextField(p.getRue() != null ? p.getRue() : "");
            String[] villesTunisie = {
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Sousse",
                "Sfax", "Bizerte", "Nabeul", "Monastir", "Kairouan", "Gabès", "Médenine"
            };
            JComboBox<String> comboVille = new JComboBox<>(villesTunisie);
            comboVille.setSelectedItem(p.getVille());

            JTextField txtCP = new JTextField(String.valueOf(p.getCodePostal()));
            txtCP.setEditable(false);
            txtCP.setBackground(new Color(245, 245, 245));

            comboVille.addActionListener(e -> {
                String v = (String) comboVille.getSelectedItem();
                switch (v) {
                    case "Tunis":      txtCP.setText("1001"); break;
                    case "Ariana":     txtCP.setText("2080"); break;
                    case "Ben Arous":  txtCP.setText("2013"); break;
                    case "Manouba":    txtCP.setText("2010"); break;
                    case "Sousse":     txtCP.setText("4000"); break;
                    case "Sfax":       txtCP.setText("3000"); break;
                    case "Bizerte":    txtCP.setText("7000"); break;
                    case "Nabeul":     txtCP.setText("8000"); break;
                    case "Monastir":   txtCP.setText("5000"); break;
                    case "Kairouan":   txtCP.setText("3100"); break;
                    case "Gabès":      txtCP.setText("6000"); break;
                    case "Médenine":   txtCP.setText("4100"); break;
                    default:           txtCP.setText("1000"); break;
                }
            });

            JComboBox<ComboItem> comboMeds = new JComboBox<>();
            for (ComboItem it : medecinDAO.getComboItems()) {
                comboMeds.addItem(it);
                if (it.getLabel().contains(p.getNomMedecin())) {
                    comboMeds.setSelectedItem(it);
                }
            }

            List<String> telsExistants = p.getTels();
            JTextField txtTel1 = new JTextField(telsExistants.size() > 0 ? telsExistants.get(0) : "");
            JTextField txtTel2 = new JTextField(telsExistants.size() > 1 ? telsExistants.get(1) : "");
            JTextField txtTel3 = new JTextField(telsExistants.size() > 2 ? telsExistants.get(2) : "");

            JLabel lblTel2 = new JLabel("Téléphone 2 (Urgence) :");
            lblTel2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTel2.setForeground(COLOR_TEXT_LABEL);
            lblTel2.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblTel2.setVisible(telsExistants.size() > 1);
            txtTel2.setVisible(telsExistants.size() > 1);

            JLabel lblTel3 = new JLabel("Téléphone 3 (Proche) :");
            lblTel3.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lblTel3.setForeground(COLOR_TEXT_LABEL);
            lblTel3.setAlignmentX(Component.LEFT_ALIGNMENT);
            lblTel3.setVisible(telsExistants.size() > 2);
            txtTel3.setVisible(telsExistants.size() > 2);

            JButton btnPlusTel = new JButton("+ Ajouter");
            btnPlusTel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnPlusTel.setBackground(new Color(39, 174, 96));
            btnPlusTel.setForeground(Color.WHITE);
            btnPlusTel.setFocusPainted(false);
            btnPlusTel.setEnabled(telsExistants.size() < 3);

            JButton btnMoinsTel = new JButton("- Retirer");
            btnMoinsTel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnMoinsTel.setBackground(new Color(192, 57, 43));
            btnMoinsTel.setForeground(Color.WHITE);
            btnMoinsTel.setFocusPainted(false);
            btnMoinsTel.setEnabled(telsExistants.size() > 1);

            JPanel pnlBoutonsTels = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            pnlBoutonsTels.setOpaque(false);
            pnlBoutonsTels.setAlignmentX(Component.LEFT_ALIGNMENT);
            pnlBoutonsTels.add(btnPlusTel);
            pnlBoutonsTels.add(btnMoinsTel);

            JPanel mainForm = new JPanel();
            mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
            mainForm.setPreferredSize(new Dimension(360, 500));

            btnPlusTel.addActionListener(e -> {
                if (!txtTel2.isVisible()) {
                    lblTel2.setVisible(true);
                    txtTel2.setVisible(true);
                    btnMoinsTel.setEnabled(true);
                } else if (!txtTel3.isVisible()) {
                    lblTel3.setVisible(true);
                    txtTel3.setVisible(true);
                    btnPlusTel.setEnabled(false);
                }
                Window w = SwingUtilities.getWindowAncestor(mainForm);
                if (w != null) w.pack();
            });

            btnMoinsTel.addActionListener(e -> {
                if (txtTel3.isVisible()) {
                    txtTel3.setText("");
                    lblTel3.setVisible(false);
                    txtTel3.setVisible(false);
                    btnPlusTel.setEnabled(true);
                } else if (txtTel2.isVisible()) {
                    txtTel2.setText("");
                    lblTel2.setVisible(false);
                    txtTel2.setVisible(false);
                    btnMoinsTel.setEnabled(false);
                }
                Window w = SwingUtilities.getWindowAncestor(mainForm);
                if (w != null) w.pack();
            });

            JLabel sec1 = new JLabel("ADRESSE");
            sec1.setFont(new Font("Segoe UI", Font.BOLD, 11));
            sec1.setForeground(new Color(100, 116, 139));

            JLabel sec2 = new JLabel("COLLECTION TÉLÉPHONES (1 à 3 Max)");
            sec2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            sec2.setForeground(new Color(100, 116, 139));

            JLabel sec3 = new JLabel("MÉDECIN TRAITANT");
            sec3.setFont(new Font("Segoe UI", Font.BOLD, 11));
            sec3.setForeground(new Color(100, 116, 139));

            ajouterComposant(mainForm, creerLabelForm("ID Patient (Non modifiable) :"));
            ajouterComposant(mainForm, txtId);
            mainForm.add(Box.createVerticalStrut(5));

            ajouterComposant(mainForm, creerLabelForm("Nom & Prénom du Patient :"));
            ajouterComposant(mainForm, txtNom);
            mainForm.add(Box.createVerticalStrut(5));

            ajouterComposant(mainForm, creerLabelForm("Date de Naissance :"));
            ajouterComposant(mainForm, pnlDate);
            mainForm.add(Box.createVerticalStrut(5));

            ajouterComposant(mainForm, creerLabelForm("Sexe :"));
            ajouterComposant(mainForm, comboSexe);
            mainForm.add(Box.createVerticalStrut(10));

            ajouterComposant(mainForm, sec1);
            ajouterComposant(mainForm, new JSeparator());
            ajouterComposant(mainForm, creerLabelForm("Rue :"));
            ajouterComposant(mainForm, txtRue);
            ajouterComposant(mainForm, creerLabelForm("Ville :"));
            ajouterComposant(mainForm, comboVille);
            ajouterComposant(mainForm, creerLabelForm("Code Postal :"));
            ajouterComposant(mainForm, txtCP);
            mainForm.add(Box.createVerticalStrut(10));

            ajouterComposant(mainForm, sec2);
            ajouterComposant(mainForm, new JSeparator());
            ajouterComposant(mainForm, creerLabelForm("Téléphone 1 (Principal) :"));
            ajouterComposant(mainForm, txtTel1);
            ajouterComposant(mainForm, lblTel2);
            ajouterComposant(mainForm, txtTel2);
            ajouterComposant(mainForm, lblTel3);
            ajouterComposant(mainForm, txtTel3);
            mainForm.add(Box.createVerticalStrut(4));
            ajouterComposant(mainForm, pnlBoutonsTels);
            mainForm.add(Box.createVerticalStrut(10));

            ajouterComposant(mainForm, sec3);
            ajouterComposant(mainForm, new JSeparator());
            ajouterComposant(mainForm, comboMeds);

            if (JOptionPane.showConfirmDialog(this, mainForm, "Modifier Patient (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                if (txtNom.getText().trim().isEmpty() || txtTel1.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez renseigner au moins le Nom et le Téléphone principal !", "Champs obligatoires", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String moisNumero = cbMois.getSelectedItem().toString().substring(0, 2);
                String dateFinale = cbJour.getSelectedItem() + "/" + moisNumero + "/" + cbAnnee.getSelectedItem();

                Vector<String> tels = new Vector<>();
                tels.add("'" + txtTel1.getText().trim() + "'");
                if (txtTel2.isVisible() && !txtTel2.getText().trim().isEmpty()) {
                    tels.add("'" + txtTel2.getText().trim() + "'");
                }
                if (txtTel3.isVisible() && !txtTel3.getText().trim().isEmpty()) {
                    tels.add("'" + txtTel3.getText().trim() + "'");
                }
                String varraySql = "telephones_va(" + String.join(", ", tels) + ")";

                String sexeChoisi = comboSexe.getSelectedItem().toString().substring(0, 1);
                ComboItem med = (ComboItem) comboMeds.getSelectedItem();

                patientDAO.modifier(
                    id,
                    txtNom.getText().trim(),
                    dateFinale,
                    sexeChoisi,
                    txtRue.getText().trim(),
                    comboVille.getSelectedItem().toString(),
                    Integer.parseInt(txtCP.getText().trim()),
                    varraySql,
                    med.getId()
                );

                JOptionPane.showMessageDialog(this, "Patient mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur de modification : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actionSupprimer() {
        int viewRow = tblPatients.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un patient !"); return; }
        int r = tblPatients.convertRowIndexToModel(viewRow);
        int id = (int) modelPatients.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le patient n° " + id + " ?\nAttention : toutes les consultations associées à ce patient seront également supprimées définitivement !", "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                patientDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Patient et dossier médical détruits avec succès !");
                charger();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void ajouterComposant(JPanel panel, JComponent comp) {
        comp.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (comp instanceof JTextField || comp instanceof JComboBox) {
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        }
        panel.add(comp);
    }

    private JTextField creerChampLecture() {
        JTextField tf = new JTextField();
        tf.setEditable(false);
        tf.setBackground(new Color(245, 245, 245));
        tf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return tf;
    }

    private JLabel creerLabelForm(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(COLOR_TEXT_LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JLabel creerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(COLOR_TEXT_LABEL);
        return l;
    }

    private JButton creerBouton(String text, Color bg, String iconFile) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
        b.setBackground(b.getBackground());
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ImageIcon icon = chargerIcone(iconFile, 14, 14);
        if (icon != null) {
            b.setIcon(icon);
            b.setIconTextGap(5);
        }
        return b;
    }

    private ImageIcon chargerIcone(String nom, int largeur, int hauteur) {
        try {
            java.net.URL url = getClass().getResource("/resources/icons/" + nom);
            if (url == null) url = getClass().getClassLoader().getResource("resources/icons/" + nom);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH));

            java.io.File dossier = new java.io.File(System.getProperty("user.dir"));
            java.io.File f = scannerFichier(dossier, nom);
            if (f != null) return new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(largeur, hauteur, Image.SCALE_SMOOTH));
        } catch (Exception ignored) {}
        return null;
    }

    private java.io.File scannerFichier(java.io.File d, String nom) {
        if (d == null || !d.exists()) return null;
        java.io.File[] files = d.listFiles();
        if (files == null) return null;
        for (java.io.File f : files) {
            if (f.isDirectory() && !f.getName().equals(".git")) {
                java.io.File res = scannerFichier(f, nom);
                if (res != null) return res;
            } else if (f.getName().equalsIgnoreCase(nom)) {
                return f;
            }
        }
        return null;
    }
}