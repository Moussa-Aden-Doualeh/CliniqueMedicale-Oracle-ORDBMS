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

    public ConsultationView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panneau supérieur : choix du patient
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

        // Fiche de détails à gauche (Lecture seule)
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
        pnlForm.add(creerLabel("Ordonnance (VARRAY):"));pnlForm.add(comboConsMedicaments);

        add(pnlForm, BorderLayout.WEST);

        // Tableau à droite
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelConsultations = new DefaultTableModel(new String[]{"N°", "Date", "Motif", "Statut", "Tarif"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblConsultations = new JTable(modelConsultations);
        tblConsultations.setRowHeight(24);
        tblConsultations.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblConsultations.getSelectedRow() != -1) {
                int r = tblConsultations.getSelectedRow();
                txtConsNum.setText(modelConsultations.getValueAt(r, 0).toString());
                txtConsDate.setText(modelConsultations.getValueAt(r, 1).toString());
                txtConsMotif.setText(modelConsultations.getValueAt(r, 2).toString());
                txtConsStatut.setText(modelConsultations.getValueAt(r, 3).toString());
                txtConsTarif.setText(modelConsultations.getValueAt(r, 4).toString());

                int numCons = (int) modelConsultations.getValueAt(r, 0);
                chargerMedicaments(numCons);
            }
        });
        pnlTable.add(new JScrollPane(tblConsultations), BorderLayout.CENTER);

        // Barre de boutons CRUD en bas
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

    // BOÎTE DE DIALOGUE : AJOUTER UN ACTE
    private void dialogueAjouter() {
        // Liste des patients pour choisir
        JComboBox<ComboItem> comboChoixPat = new JComboBox<>();
        try {
            for (ComboItem it : patientDAO.getComboItems()) comboChoixPat.addItem(it);
        } catch (SQLException ignored) {}

        if (comboConsPatient.getSelectedItem() != null) {
            comboChoixPat.setSelectedItem(comboConsPatient.getSelectedItem());
        }

        // Affichage du médecin traitant
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

        JTextField dateC = new JTextField("15/10/2026");
        JTextField motif = new JTextField("Consultation de routine");
        JComboBox<String> comboStatut = new JComboBox<>(new String[]{"Terminé", "Planifié"});
        JTextField tarif = new JTextField("80");
        JTextField medicament = new JTextField("Doliprane 1000mg");

        Object[] fields = {
            "Patient :", comboChoixPat,
            "Médecin Traitant (REF) :", txtMedInfo,
            new JSeparator(),
            "Date (JJ/MM/AAAA) :", dateC,
            "Motif / Diagnostic :", motif,
            "Statut (Acte / RDV) :", comboStatut,
            "Tarif (DT) :", tarif,
            "-- ORDONNANCE (VARRAY) --",
            "Médicaments prescrits :", medicament
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Ajouter dans la Nested Table", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem patChoisi = (ComboItem) comboChoixPat.getSelectedItem();
            if (patChoisi == null) return;

            try {
                consultationDAO.ajouterActe(
                    patChoisi.getId(),
                    dateC.getText().trim(),
                    motif.getText().trim(),
                    comboStatut.getSelectedItem().toString(),
                    Double.parseDouble(tarif.getText().trim()),
                    medicament.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "Acte médical inséré avec succès dans la Nested Table !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                comboConsPatient.setSelectedItem(patChoisi);
                chargerConsultations();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // BOÎTE DE DIALOGUE : MODIFIER UN ACTE
    private void dialogueModifier() {
        int r = tblConsultations.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un acte médical dans la table !"); return; }
        int numCons = (int) modelConsultations.getValueAt(r, 0);
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();

        JTextField motif = new JTextField(txtConsMotif.getText());
        JComboBox<String> comboStatut = new JComboBox<>(new String[]{"Terminé", "Planifié"});
        comboStatut.setSelectedItem(txtConsStatut.getText());
        JTextField tarif = new JTextField(txtConsTarif.getText().replace(" DT", ""));
        JTextField medicament = new JTextField(comboConsMedicaments.getItemCount() > 0 ? comboConsMedicaments.getItemAt(0) : "");

        Object[] fields = {
            "Patient : " + pat.getLabel(),
            "Acte N° : " + numCons,
            "Nouveau Motif :", motif,
            "Nouveau Statut :", comboStatut,
            "Nouveau Tarif (DT) :", tarif,
            "-- ORDONNANCE (VARRAY) --",
            "Médicaments :", medicament
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Modifier Acte dans la Nested Table", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                consultationDAO.modifierActe(
                    pat.getId(),
                    numCons,
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

    // ACTION : SUPPRIMER UN ACTE
    private void actionSupprimer() {
        int r = tblConsultations.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un acte médical !"); return; }
        int numCons = (int) modelConsultations.getValueAt(r, 0);
        ComboItem pat = (ComboItem) comboConsPatient.getSelectedItem();

        if (JOptionPane.showConfirmDialog(this, "Supprimer l'acte N° " + numCons + " de la Nested Table de ce patient ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                consultationDAO.supprimerActe(pat.getId(), numCons);
                JOptionPane.showMessageDialog(this, "Acte retiré de la Nested Table avec succès !");
                chargerConsultations();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
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

        ImageIcon icon = chargerIconeBouton(iconFile);
        if (icon != null) {
            b.setIcon(icon);
            b.setIconTextGap(5);
        }
        return b;
    }

    private ImageIcon chargerIconeBouton(String nom) {
        try {
            java.net.URL url = getClass().getResource("/resources/icons/" + nom);
            if (url == null) url = getClass().getClassLoader().getResource("resources/icons/" + nom);
            if (url != null) return new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));

            java.io.File dossier = new java.io.File(System.getProperty("user.dir"));
            java.io.File f = scannerFichier(dossier, nom);
            if (f != null) return new ImageIcon(new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH));
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