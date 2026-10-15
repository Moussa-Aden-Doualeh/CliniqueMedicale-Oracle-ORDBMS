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

public class PatientView extends JPanel {

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
        pnlForm.add(creerLabel("Âge (PL/SQL) :"));pnlForm.add(txtPatAge);
        pnlForm.add(creerLabel("Ville (TAD) :")); pnlForm.add(txtPatVille);
        pnlForm.add(creerLabel("Code Postal :")); pnlForm.add(txtPatCP);
        pnlForm.add(creerLabel("Phones (VARRAY):"));pnlForm.add(comboPatTels);
        pnlForm.add(creerLabel("Médecin (REF) :"));pnlForm.add(txtPatMedecin);

        add(pnlForm, BorderLayout.WEST);

        // Tableau Droite + Actions
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelPatients = new DefaultTableModel(new String[]{"ID", "Nom", "Sexe", "Âge", "Ville", "Médecin (REF)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblPatients = new JTable(modelPatients);
        tblPatients.setRowHeight(24);
        tblPatients.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPatients.getSelectedRow() != -1) {
                int idPat = (int) modelPatients.getValueAt(tblPatients.getSelectedRow(), 0);
                remplirFiche(idPat);
            }
        });
        pnlTable.add(new JScrollPane(tblPatients), BorderLayout.CENTER);

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

    private void dialogueAjouter() {
        JTextField id = new JTextField(String.valueOf(System.currentTimeMillis() % 10000));
        JTextField nom = new JTextField();
        JTextField dateN = new JTextField("15/04/1998");
        JComboBox<String> comboSexe = new JComboBox<>(new String[]{"M", "F"});
        JTextField rue = new JTextField("Avenue Habib Bourguiba");
        JTextField ville = new JTextField("Tunis");
        JTextField cp = new JTextField("1001");
        JTextField tel1 = new JTextField("98111222");
        JTextField tel2 = new JTextField("55444333");
        JComboBox<ComboItem> comboMeds = new JComboBox<>();

        try {
            for (ComboItem it : medecinDAO.getComboItems()) comboMeds.addItem(it);
        } catch (SQLException ignored) {}

        Object[] fields = {
            "ID :", id, "Nom & Prénom :", nom, "Date Naissance :", dateN, "Sexe :", comboSexe,
            "-- TAD ADRESSE_T --", new JSeparator(),
            "Rue :", rue, "Ville :", ville, "Code Postal :", cp,
            "-- VARRAY (Max 3) --", new JSeparator(),
            "Téléphone 1 :", tel1, "Téléphone 2 :", tel2,
            "-- MÉDECIN (REF) --", new JSeparator(),
            "Médecin Traitant :", comboMeds
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Nouveau Patient (TAD + VARRAY + REF)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Vector<String> tels = new Vector<>();
                if (!tel1.getText().trim().isEmpty()) tels.add("'" + tel1.getText().trim() + "'");
                if (!tel2.getText().trim().isEmpty()) tels.add("'" + tel2.getText().trim() + "'");
                String varraySql = "telephones_va(" + String.join(", ", tels) + ")";

                ComboItem med = (ComboItem) comboMeds.getSelectedItem();

                patientDAO.ajouter(Integer.parseInt(id.getText().trim()), nom.getText().trim(), dateN.getText().trim(),
                        comboSexe.getSelectedItem().toString(), rue.getText().trim(), ville.getText().trim(),
                        Integer.parseInt(cp.getText().trim()), varraySql, med.getId());

                JOptionPane.showMessageDialog(this, "Patient créé avec Nested Table vide par défaut !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void dialogueModifier() {
        int r = tblPatients.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un patient !"); return; }
        int id = (int) modelPatients.getValueAt(r, 0);

        try {
            Patient p = patientDAO.getFicheComplete(id);
            if (p == null) return;

            JTextField nom = new JTextField(p.getNomPat());
            JTextField dateN = new JTextField(p.getDateNaissance());
            JComboBox<String> comboSexe = new JComboBox<>(new String[]{"M", "F"});
            comboSexe.setSelectedItem(p.getSexe());
            JTextField rue = new JTextField(p.getRue());
            JTextField ville = new JTextField(p.getVille());
            JTextField cp = new JTextField(String.valueOf(p.getCodePostal()));
            JTextField tel1 = new JTextField(p.getTels().size() > 0 ? p.getTels().get(0) : "");
            JTextField tel2 = new JTextField(p.getTels().size() > 1 ? p.getTels().get(1) : "");
            JComboBox<ComboItem> comboMeds = new JComboBox<>();
            for (ComboItem it : medecinDAO.getComboItems()) comboMeds.addItem(it);

            Object[] fields = {
                "ID : " + id, "Nom :", nom, "Date Naissance :", dateN, "Sexe :", comboSexe,
                "-- TAD ADRESSE_T --", new JSeparator(),
                "Rue :", rue, "Ville :", ville, "Code Postal :", cp,
                "-- VARRAY --", new JSeparator(),
                "Téléphone 1 :", tel1, "Téléphone 2 :", tel2,
                "-- MÉDECIN (REF) --", new JSeparator(),
                "Médecin Traitant :", comboMeds
            };

            if (JOptionPane.showConfirmDialog(this, fields, "Modifier Patient", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                Vector<String> tels = new Vector<>();
                if (!tel1.getText().trim().isEmpty()) tels.add("'" + tel1.getText().trim() + "'");
                if (!tel2.getText().trim().isEmpty()) tels.add("'" + tel2.getText().trim() + "'");
                String varraySql = "telephones_va(" + String.join(", ", tels) + ")";

                ComboItem med = (ComboItem) comboMeds.getSelectedItem();
                patientDAO.modifier(id, nom.getText().trim(), dateN.getText().trim(), comboSexe.getSelectedItem().toString(),
                        rue.getText().trim(), ville.getText().trim(), Integer.parseInt(cp.getText().trim()), varraySql, med.getId());

                JOptionPane.showMessageDialog(this, "Patient mis à jour avec son TAD et son VARRAY !");
                charger();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void actionSupprimer() {
        int r = tblPatients.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un patient !"); return; }
        int id = (int) modelPatients.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le patient n° " + id + " ?\nAttention : Toute sa Nested Table de consultations sera détruite en cascade simultanément !", "Suppression en Cascade", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                patientDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Patient et dossier médical détruits avec succès !");
                charger();
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