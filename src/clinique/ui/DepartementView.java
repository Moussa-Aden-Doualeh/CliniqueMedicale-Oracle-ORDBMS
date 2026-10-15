package clinique.ui;

import clinique.dao.DepartementDAO;
import clinique.model.Departement;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class DepartementView extends JPanel {

    private final DepartementDAO departementDAO = new DepartementDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JTextField txtDepId, txtDepNom, txtDepEtage;
    private JTable tblDepartements;
    private DefaultTableModel modelDepartements;

    public DepartementView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Formulaire Gauche
        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 5, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlForm.setPreferredSize(new Dimension(280, 0));

        txtDepId = creerChampLecture();
        txtDepNom = creerChampLecture();
        txtDepEtage = creerChampLecture();

        pnlForm.add(creerLabel("ID Département :")); pnlForm.add(txtDepId);
        pnlForm.add(creerLabel("Nom Service :"));    pnlForm.add(txtDepNom);
        pnlForm.add(creerLabel("Étage :"));          pnlForm.add(txtDepEtage);

        add(pnlForm, BorderLayout.WEST);

        // Tableau Droite
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelDepartements = new DefaultTableModel(new String[]{"ID", "Nom Département", "Étage", "Nb Médecins"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDepartements = new JTable(modelDepartements);
        tblDepartements.setRowHeight(24);
        tblDepartements.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDepartements.getSelectedRow() != -1) {
                int r = tblDepartements.getSelectedRow();
                txtDepId.setText(modelDepartements.getValueAt(r, 0).toString());
                txtDepNom.setText(modelDepartements.getValueAt(r, 1).toString());
                txtDepEtage.setText(modelDepartements.getValueAt(r, 2).toString());
            }
        });
        pnlTable.add(new JScrollPane(tblDepartements), BorderLayout.CENTER);

        // Barre d'actions CRUD
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
        modelDepartements.setRowCount(0);
        try {
            List<Departement> liste = departementDAO.getTous();
            for (Departement d : liste) {
                modelDepartements.addRow(new Object[]{d.getIdDep(), d.getNomDep(), d.getEtage(), d.getNbMedecins() + " médecin(s)"});
            }
            if (tblDepartements.getRowCount() > 0) tblDepartements.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void dialogueAjouter() {
        JTextField id = new JTextField(String.valueOf(System.currentTimeMillis() % 100));
        JTextField nom = new JTextField();
        JTextField etage = new JTextField("1");
        Object[] fields = {"ID :", id, "Nom du Service :", nom, "Étage :", etage};

        if (JOptionPane.showConfirmDialog(this, fields, "Ajouter un Département", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                departementDAO.ajouter(Integer.parseInt(id.getText().trim()), nom.getText().trim(), Integer.parseInt(etage.getText().trim()));
                JOptionPane.showMessageDialog(this, "Département créé avec succès !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void dialogueModifier() {
        int r = tblDepartements.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une ligne !"); return; }
        int id = (int) modelDepartements.getValueAt(r, 0);

        JTextField nom = new JTextField(modelDepartements.getValueAt(r, 1).toString());
        JTextField etage = new JTextField(modelDepartements.getValueAt(r, 2).toString());
        Object[] fields = {"ID : " + id + " (Non modifiable)", "Nouveau Nom :", nom, "Nouvel Étage :", etage};

        if (JOptionPane.showConfirmDialog(this, fields, "Modifier Département", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                departementDAO.modifier(id, nom.getText().trim(), Integer.parseInt(etage.getText().trim()));
                JOptionPane.showMessageDialog(this, "Département mis à jour !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void actionSupprimer() {
        int r = tblDepartements.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un département !"); return; }
        int id = (int) modelDepartements.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le département n° " + id + " ?\nAttention : les médecins rattachés deviendront orphelins (DANGLING) !", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                departementDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Département supprimé !");
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