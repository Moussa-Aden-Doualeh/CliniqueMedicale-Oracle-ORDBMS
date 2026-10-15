package clinique.ui;

import clinique.dao.FactureDAO;
import clinique.dao.MedecinDAO;
import clinique.dao.PatientDAO;
import clinique.model.ComboItem;
import clinique.model.Facture;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class FactureView extends JPanel {

    private final FactureDAO factureDAO = new FactureDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JTextField txtFacNum, txtFacDate, txtFacMontant, txtFacPat, txtFacVille, txtFacMed;
    private JTable tblFactures;
    private DefaultTableModel modelFactures;

    public FactureView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 5, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlForm.setPreferredSize(new Dimension(300, 0));

        txtFacNum = creerChampLecture();
        txtFacDate = creerChampLecture();
        txtFacMontant = creerChampLecture();
        txtFacPat = creerChampLecture();
        txtFacVille = creerChampLecture();
        txtFacMed = creerChampLecture();

        pnlForm.add(creerLabel("Invoice N° :"));           pnlForm.add(txtFacNum);
        pnlForm.add(creerLabel("Date Facture :"));         pnlForm.add(txtFacDate);
        pnlForm.add(creerLabel("Montant Total :"));        pnlForm.add(txtFacMontant);
        pnlForm.add(creerLabel("Patient Débiteur :"));     pnlForm.add(txtFacPat);
        pnlForm.add(creerLabel("Ville Facturation :"));    pnlForm.add(txtFacVille);
        pnlForm.add(creerLabel("Doctor Prescripteur :"));  pnlForm.add(txtFacMed);

        add(pnlForm, BorderLayout.WEST);

        // Tableau + Actions
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelFactures = new DefaultTableModel(new String[]{"N° Facture", "Date", "Montant", "Patient", "Ville", "Médecin"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblFactures = new JTable(modelFactures);
        tblFactures.setRowHeight(24);
        tblFactures.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblFactures.getSelectedRow() != -1) {
                int r = tblFactures.getSelectedRow();
                txtFacNum.setText(modelFactures.getValueAt(r, 0).toString());
                txtFacDate.setText(modelFactures.getValueAt(r, 1).toString());
                txtFacMontant.setText(modelFactures.getValueAt(r, 2).toString());
                txtFacPat.setText(modelFactures.getValueAt(r, 3).toString());
                txtFacVille.setText(modelFactures.getValueAt(r, 4).toString());
                txtFacMed.setText(modelFactures.getValueAt(r, 5).toString());
            }
        });
        pnlTable.add(new JScrollPane(tblFactures), BorderLayout.CENTER);

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 5));
        pnlActions.setOpaque(false);

        JButton btnAdd = creerBouton(" Créer Facture", new Color(39, 174, 96), "add.png");
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
        modelFactures.setRowCount(0);
        try {
            List<Facture> liste = factureDAO.getToutes();
            for (Facture f : liste) {
                modelFactures.addRow(new Object[]{
                    f.getNumFacture(), f.getDateFacture(), f.getMontant() + " DT",
                    f.getNomPatient(), f.getVilleFacturation(), f.getNomMedecin()
                });
            }
            if (tblFactures.getRowCount() > 0) tblFactures.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur Factures : " + ex.getMessage());
        }
    }

    private void dialogueAjouter() {
        JTextField numF = new JTextField(String.valueOf(System.currentTimeMillis() % 10000));
        JTextField montant = new JTextField("120");
        JComboBox<ComboItem> comboP = new JComboBox<>();
        JComboBox<ComboItem> comboM = new JComboBox<>();

        try {
            for (ComboItem it : patientDAO.getComboItems()) comboP.addItem(it);
            for (ComboItem it : medecinDAO.getComboItems()) comboM.addItem(it);
        } catch (SQLException ignored) {}

        Object[] fields = {"N° Facture :", numF, "Montant Total (DT) :", montant, "Patient Débiteur (REF) :", comboP, "Médecin Prescripteur (REF) :", comboM};

        if (JOptionPane.showConfirmDialog(this, fields, "Créer Facture (Table Non Typée)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                ComboItem p = (ComboItem) comboP.getSelectedItem();
                ComboItem m = (ComboItem) comboM.getSelectedItem();
                factureDAO.ajouter(Integer.parseInt(numF.getText().trim()), Double.parseDouble(montant.getText().trim()), p.getId(), m.getId());
                JOptionPane.showMessageDialog(this, "Facture enregistrée dans la table non typée !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void dialogueModifier() {
        int r = tblFactures.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une facture !"); return; }
        int num = (int) modelFactures.getValueAt(r, 0);

        JTextField montant = new JTextField(txtFacMontant.getText().replace(" DT", ""));
        JComboBox<ComboItem> comboP = new JComboBox<>();
        JComboBox<ComboItem> comboM = new JComboBox<>();

        try {
            for (ComboItem it : patientDAO.getComboItems()) comboP.addItem(it);
            for (ComboItem it : medecinDAO.getComboItems()) comboM.addItem(it);
        } catch (SQLException ignored) {}

        Object[] fields = {"N° Facture : " + num, "Nouveau Montant (DT) :", montant, "Patient (REF) :", comboP, "Médecin (REF) :", comboM};

        if (JOptionPane.showConfirmDialog(this, fields, "Modifier Facture", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                ComboItem p = (ComboItem) comboP.getSelectedItem();
                ComboItem m = (ComboItem) comboM.getSelectedItem();
                factureDAO.modifier(num, Double.parseDouble(montant.getText().trim()), p.getId(), m.getId());
                JOptionPane.showMessageDialog(this, "Facture mise à jour !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void actionSupprimer() {
        int r = tblFactures.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une facture !"); return; }
        int num = (int) modelFactures.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer la facture n° " + num + " ?", "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                factureDAO.supprimer(num);
                JOptionPane.showMessageDialog(this, "Facture supprimée !");
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