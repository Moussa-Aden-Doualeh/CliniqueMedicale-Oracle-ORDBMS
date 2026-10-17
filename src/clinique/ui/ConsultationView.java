package clinique.ui;

import clinique.dao.ConsultationDAO;
import clinique.dao.PatientDAO;
import clinique.model.ComboItem;
import clinique.model.Consultation;
import clinique.model.Patient;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class ConsultationView extends JPanel {

    private final ConsultationDAO consultationDAO = new ConsultationDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JComboBox<ComboItem> comboConsPatient;
    private JTextField txtConsNum, txtConsDate, txtConsMotif, txtConsStatut, txtConsTarif;
    private JComboBox<String> comboConsMedicaments;
    private JLabel lblConsTotal;
    private JTable tblConsultations;
    private DefaultTableModel modelConsultations;
    private TableRowSorter<DefaultTableModel> sorterConsultations;

    public ConsultationView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // EN-TÊTE SUPÉRIEUR : CHOIX DU PATIENT + TOTAL DÉPENSES
        JPanel pnlHaut = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlHaut.setBackground(Color.WHITE);
        pnlHaut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));

        comboConsPatient = new JComboBox<>();
        comboConsPatient.setPreferredSize(new Dimension(220, 26));
        comboConsPatient.addActionListener(e -> chargerConsultations());

        lblConsTotal = new JLabel("Total : 0 DT");
        lblConsTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConsTotal.setForeground(COLOR_TEXT_LABEL);

        pnlHaut.add(creerLabel("Sélectionner un Patient :"));
        pnlHaut.add(comboConsPatient);
        pnlHaut.add(lblConsTotal);
        add(pnlHaut, BorderLayout.NORTH);

       
        // 1. FICHE DE DÉTAILS À GAUCHE
        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlForm.setPreferredSize(new Dimension(280, 0));

        txtConsNum = creerChampLecture();
        txtConsDate = creerChampLecture();
        txtConsMotif = creerChampLecture();
        txtConsStatut = creerChampLecture();
        txtConsTarif = creerChampLecture();
        comboConsMedicaments = new JComboBox<>();
        comboConsMedicaments.setBackground(Color.WHITE);

        pnlForm.add(creerLabel("N° Acte :"));        pnlForm.add(txtConsNum);
        pnlForm.add(creerLabel("Date :"));           pnlForm.add(txtConsDate);
        pnlForm.add(creerLabel("Motif :"));          pnlForm.add(txtConsMotif);
        pnlForm.add(creerLabel("Statut :"));         pnlForm.add(txtConsStatut);
        pnlForm.add(creerLabel("Tarif :"));          pnlForm.add(txtConsTarif);
        pnlForm.add(creerLabel("Ordonnance :"));     pnlForm.add(comboConsMedicaments);

        add(pnlForm, BorderLayout.WEST);

        // 2. PANNEAU DE DROITE : RECHERCHE MODERNE + TABLEAU + ACTIONS
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        // Modèle et tableau
        modelConsultations = new DefaultTableModel(new String[]{"N°", "Date", "Motif", "Statut", "Tarif"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblConsultations = new JTable(modelConsultations);
        tblConsultations.setRowHeight(24);

        // Sorter pour le filtrage
        sorterConsultations = new TableRowSorter<>(modelConsultations);
        tblConsultations.setRowSorter(sorterConsultations);

        // Sélection sécurisée avec convertRowIndexToModel
        tblConsultations.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblConsultations.getSelectedRow() != -1) {
                int r = tblConsultations.convertRowIndexToModel(tblConsultations.getSelectedRow());
                txtConsNum.setText(modelConsultations.getValueAt(r, 0).toString());
                txtConsDate.setText(modelConsultations.getValueAt(r, 1).toString());
                txtConsMotif.setText(modelConsultations.getValueAt(r, 2).toString());
                txtConsStatut.setText(modelConsultations.getValueAt(r, 3).toString());
                txtConsTarif.setText(modelConsultations.getValueAt(r, 4).toString());

                int numCons = (int) modelConsultations.getValueAt(r, 0);
                chargerMedicaments(numCons);
            }
        });

        // BARRE DE RECHERCHE EN CAPSULE MODERNE AVEC VRAIES ICÔNES
        JPanel pnlTableTop = new JPanel(new BorderLayout(10, 5));
        pnlTableTop.setOpaque(false);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel lblTitreTable = new JLabel("Dossier Médical (Historique des Soins)");
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

        String placeholder = "Filtrer motif, statut, date...";
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

        // Interactions Placeholder & Filtrage
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
                    sorterConsultations.setRowFilter(null);
                    btnClear.setVisible(false);
                }
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                if (q.isEmpty() || q.equals(placeholder)) {
                    sorterConsultations.setRowFilter(null);
                    btnClear.setVisible(false);
                } else {
                    btnClear.setVisible(true);
                    sorterConsultations.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                }
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtSearch.setText("");
                sorterConsultations.setRowFilter(null);
                btnClear.setVisible(false);
                txtSearch.requestFocus();
            }
        });

        pnlTableTop.add(pnlSearchBox, BorderLayout.EAST);
        pnlTable.add(pnlTableTop, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblConsultations), BorderLayout.CENTER);

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
        btnAct.addActionListener(e -> rafraichirPatients());

        pnlActions.add(btnAdd);
        pnlActions.add(btnMod);
        pnlActions.add(btnDel);
        pnlActions.add(btnAct);
        pnlTable.add(pnlActions, BorderLayout.SOUTH);

        add(pnlTable, BorderLayout.CENTER);
        rafraichirPatients();
    }

    public void rafraichirPatients() {
        comboConsPatient.removeAllItems();
        try {
            Vector<ComboItem> items = patientDAO.getComboItems();
            for (ComboItem it : items) comboConsPatient.addItem(it);
            chargerConsultations();
        } catch (SQLException ignored) {}
    }

    private void chargerConsultations() {
        modelConsultations.setRowCount(0);
        comboConsMedicaments.removeAllItems();
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();
        if (pat == null) return;

        try {
            List<Consultation> liste = consultationDAO.getParPatient(pat.getId());
            for (Consultation c : liste) {
                modelConsultations.addRow(new Object[]{
                    c.getNumCons(), c.getDateCons(), c.getMotif(), c.getStatut(), c.getTarif() + " DT"
                });
            }
            double total = consultationDAO.getTotalTarifs(pat.getId());
            lblConsTotal.setText("Total : " + total + " DT");

            if (tblConsultations.getRowCount() > 0) tblConsultations.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void chargerMedicaments(int numCons) {
        comboConsMedicaments.removeAllItems();
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();
        if (pat == null) return;

        try {
            List<String> meds = consultationDAO.getMedicamentsActe(pat.getId(), numCons);
            if (!meds.isEmpty()) {
                for (String m : meds) comboConsMedicaments.addItem(m);
            } else {
                comboConsMedicaments.addItem("Aucun médicament (RDV)");
            }
        } catch (SQLException ignored) {}
    }

    // =========================================================================
    // BOÎTE DE DIALOGUE : AJOUTER UN ACTE / RDV (SÉLECTEUR JOUR/MOIS/ANNÉE)
    // =========================================================================
    private void dialogueAjouter() {
        JComboBox<ComboItem> comboChoixPat = new JComboBox<>();
        try {
            for (ComboItem it : patientDAO.getComboItems()) comboChoixPat.addItem(it);
        } catch (SQLException ignored) {}

        if (comboConsPatient.getSelectedItem() != null) {
            comboChoixPat.setSelectedItem(comboConsPatient.getSelectedItem());
        }

        JTextField txtMedInfo = new JTextField();
        txtMedInfo.setEditable(false);
        txtMedInfo.setBackground(new Color(245, 245, 245));

        Runnable actualiserMed = () -> {
            ComboItem sel = (ComboItem) comboChoixPat.getSelectedItem();
            if (sel != null) {
                try {
                    Patient p = patientDAO.getFicheComplete(sel.getId());
                    if (p != null) txtMedInfo.setText(p.getNomMedecin());
                } catch (Exception ignored) {}
            }
        };
        comboChoixPat.addActionListener(e -> actualiserMed.run());
        actualiserMed.run();

        // Sélecteur Jour / Mois / Année
        String[] jours = new String[31];
        for (int i = 1; i <= 31; i++) jours[i - 1] = String.format("%02d", i);
        JComboBox<String> cbJour = new JComboBox<>(jours);
        cbJour.setSelectedItem("15");

        String[] mois = {
            "01 - Janvier", "02 - Février", "03 - Mars", "04 - Avril", "05 - Mai", "06 - Juin",
            "07 - Juillet", "08 - Août", "09 - Septembre", "10 - Octobre", "11 - Novembre", "12 - Décembre"
        };
        JComboBox<String> cbMois = new JComboBox<>(mois);
        cbMois.setSelectedIndex(9);

        String[] annees = {"2024", "2025", "2026", "2027", "2028"};
        JComboBox<String> cbAnnee = new JComboBox<>(annees);
        cbAnnee.setSelectedItem("2026");

        JPanel pnlDate = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlDate.setOpaque(false);
        pnlDate.add(cbJour);
        pnlDate.add(cbMois);
        pnlDate.add(cbAnnee);

        // Champs vierges
        JTextField motif = new JTextField();
        JComboBox<String> comboStatut = new JComboBox<>(new String[]{"Terminé", "Planifié"});
        JTextField tarif = new JTextField();
        JTextField medicament = new JTextField();

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 360));

        JLabel sec1 = new JLabel("ORDONNANCE");
        sec1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sec1.setForeground(new Color(100, 116, 139));

        ajouterComposant(mainForm, creerLabel("Sélectionner le Patient :"));
        ajouterComposant(mainForm, comboChoixPat);
        mainForm.add(Box.createVerticalStrut(5));

        ajouterComposant(mainForm, creerLabel("Médecin Référent :"));
        ajouterComposant(mainForm, txtMedInfo);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Date de la Consultation / RDV :"));
        ajouterComposant(mainForm, pnlDate);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Motif / Diagnostic :"));
        ajouterComposant(mainForm, motif);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Statut :"));
        ajouterComposant(mainForm, comboStatut);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Honoraires / Tarif (DT) :"));
        ajouterComposant(mainForm, tarif);
        mainForm.add(Box.createVerticalStrut(10));

        ajouterComposant(mainForm, sec1);
        ajouterComposant(mainForm, new JSeparator());
        ajouterComposant(mainForm, creerLabel("Médicaments Prescrits :"));
        ajouterComposant(mainForm, medicament);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Nouvel Acte dans la Nested Table", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem patChoisi = (ComboItem) comboChoixPat.getSelectedItem();
            if (patChoisi == null) return;
            if (motif.getText().trim().isEmpty() || tarif.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez renseigner au moins le Motif et le Tarif !", "Champs obligatoires", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String moisNum = cbMois.getSelectedItem().toString().substring(0, 2);
                String dateFinale = cbJour.getSelectedItem() + "/" + moisNum + "/" + cbAnnee.getSelectedItem();

                consultationDAO.ajouterActe(
                    patChoisi.getId(),
                    dateFinale,
                    motif.getText().trim(),
                    comboStatut.getSelectedItem().toString(),
                    Double.parseDouble(tarif.getText().trim()),
                    medicament.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "Acte inséré avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                comboConsPatient.setSelectedItem(patChoisi);
                chargerConsultations();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // BOÎTE DE DIALOGUE : MODIFIER UN ACTE
    // =========================================================================
    private void dialogueModifier() {
        int viewRow = tblConsultations.getSelectedRow();
        if (viewRow == -1) { 
            JOptionPane.showMessageDialog(this, "Veuillez d'abord sélectionner un acte médical dans la table !"); 
            return; 
        }
        int r = tblConsultations.convertRowIndexToModel(viewRow);
        int numCons = (int) modelConsultations.getValueAt(r, 0);
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();

        // Date existante découpée
        String dateExistante = modelConsultations.getValueAt(r, 1).toString();
        String[] jours = new String[31];
        for (int i = 1; i <= 31; i++) jours[i - 1] = String.format("%02d", i);
        JComboBox<String> cbJour = new JComboBox<>(jours);

        String[] mois = {
            "01 - Janvier", "02 - Février", "03 - Mars", "04 - Avril", "05 - Mai", "06 - Juin",
            "07 - Juillet", "08 - Août", "09 - Septembre", "10 - Octobre", "11 - Novembre", "12 - Décembre"
        };
        JComboBox<String> cbMois = new JComboBox<>(mois);

        String[] annees = {"2024", "2025", "2026", "2027", "2028"};
        JComboBox<String> cbAnnee = new JComboBox<>(annees);

        try {
            String[] parts = dateExistante.split("/");
            if (parts.length == 3) {
                cbJour.setSelectedItem(parts[0]);
                int mIdx = Integer.parseInt(parts[1]) - 1;
                if (mIdx >= 0 && mIdx < 12) cbMois.setSelectedIndex(mIdx);
                cbAnnee.setSelectedItem(parts[2]);
            }
        } catch (Exception ignored) {}

        JPanel pnlDate = new JPanel(new GridLayout(1, 3, 5, 0));
        pnlDate.setOpaque(false);
        pnlDate.add(cbJour);
        pnlDate.add(cbMois);
        pnlDate.add(cbAnnee);

        JTextField motif = new JTextField(txtConsMotif.getText());
        JComboBox<String> comboStatut = new JComboBox<>(new String[]{"Terminé", "Planifié"});
        comboStatut.setSelectedItem(txtConsStatut.getText());
        JTextField tarif = new JTextField(txtConsTarif.getText().replace(" DT", "").trim());

        String medExistant = "";
        if (comboConsMedicaments.getItemCount() > 0 && !comboConsMedicaments.getItemAt(0).contains("Aucun médicament")) {
            medExistant = comboConsMedicaments.getItemAt(0);
        }
        JTextField medicament = new JTextField(medExistant);

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 360));

        JLabel sec1 = new JLabel("ORDONNANCE (VARRAY)");
        sec1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sec1.setForeground(new Color(100, 116, 139));

        ajouterComposant(mainForm, creerLabel("Patient : " + pat.getLabel()));
        ajouterComposant(mainForm, creerLabel("Acte N° : " + numCons ));
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Date de la Consultation / RDV :"));
        ajouterComposant(mainForm, pnlDate);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Motif / Diagnostic :"));
        ajouterComposant(mainForm, motif);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Statut :"));
        ajouterComposant(mainForm, comboStatut);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Honoraires / Tarif (DT) :"));
        ajouterComposant(mainForm, tarif);
        mainForm.add(Box.createVerticalStrut(10));

        ajouterComposant(mainForm, sec1);
        ajouterComposant(mainForm, new JSeparator());
        ajouterComposant(mainForm, creerLabel("Médicaments Prescrits :"));
        ajouterComposant(mainForm, medicament);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Modifier Acte !", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String moisNum = cbMois.getSelectedItem().toString().substring(0, 2);
                String dateFinale = cbJour.getSelectedItem() + "/" + moisNum + "/" + cbAnnee.getSelectedItem();

                consultationDAO.modifierActe(
                    pat.getId(),
                    numCons,
                    dateFinale,
                    motif.getText().trim(),
                    comboStatut.getSelectedItem().toString(),
                    Double.parseDouble(tarif.getText().trim()),
                    medicament.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "Acte médical mis à jour dans la collection !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerConsultations();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionSupprimer() {
        int viewRow = tblConsultations.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un acte médical !"); return; }
        int r = tblConsultations.convertRowIndexToModel(viewRow);
        int numCons = (int) modelConsultations.getValueAt(r, 0);
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();

        if (JOptionPane.showConfirmDialog(this, "Supprimer l'acte N° " + numCons + " de la Nested Table de ce patient ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                consultationDAO.supprimerActe(pat.getId(), numCons);
                JOptionPane.showMessageDialog(this, "Acte retiré avec succès !");
                chargerConsultations();
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

    private JLabel creerLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(COLOR_TEXT_LABEL);
        return l;
    }

    private JButton creerBouton(String text, Color bg, String iconFile) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 11));
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