package clinique.ui;

import clinique.dao.DepartementDAO;
import clinique.dao.MedecinDAO;
import clinique.model.ComboItem;
import clinique.model.Medecin;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class MedecinView extends JPanel {

    private final MedecinDAO medecinDAO = new MedecinDAO();
    private final DepartementDAO departementDAO = new DepartementDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JTextField txtMedId, txtMedNom, txtMedSpec, txtMedSal, txtMedDep, txtMedHoraires, txtMedEmail, txtMedPrimeTaux;
    private JTable tblMedecins;
    private DefaultTableModel modelMedecins;

    public MedecinView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlForm = new JPanel(new GridLayout(9, 2, 5, 5));
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        pnlForm.setPreferredSize(new Dimension(300, 0));

        txtMedId = creerChampLecture();
        txtMedNom = creerChampLecture();
        txtMedSpec = creerChampLecture();
        txtMedSal = creerChampLecture();
        txtMedDep = creerChampLecture();
        txtMedHoraires = creerChampLecture();
        txtMedEmail = creerChampLecture();
        txtMedPrimeTaux = new JTextField("0.15");

        pnlForm.add(creerLabel("ID Médecin :"));      pnlForm.add(txtMedId);
        pnlForm.add(creerLabel("Nom :"));             pnlForm.add(txtMedNom);
        pnlForm.add(creerLabel("Spécialité :"));      pnlForm.add(txtMedSpec);
        pnlForm.add(creerLabel("Salaire Base :"));    pnlForm.add(txtMedSal);
        pnlForm.add(creerLabel("Service (REF) :"));   pnlForm.add(txtMedDep);
        pnlForm.add(creerLabel("Horaires :"));        pnlForm.add(txtMedHoraires);
        pnlForm.add(creerLabel("Email :"));           pnlForm.add(txtMedEmail);
        pnlForm.add(creerLabel("Taux Prime :"));      pnlForm.add(txtMedPrimeTaux);

        JButton btnPrime = new JButton("Calculer Prime");
        btnPrime.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPrime.setBackground(new Color(230, 126, 34));
        btnPrime.setForeground(Color.WHITE);
        btnPrime.addActionListener(e -> calculerPrime());
        pnlForm.add(btnPrime);
        pnlForm.add(new JLabel(""));

        add(pnlForm, BorderLayout.WEST);

        // Tableau + Actions
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelMedecins = new DefaultTableModel(new String[]{"ID", "Nom", "Spécialité", "Salaire", "Service (REF)", "Horaires"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblMedecins = new JTable(modelMedecins);
        tblMedecins.setRowHeight(24);
        tblMedecins.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblMedecins.getSelectedRow() != -1) {
                int r = tblMedecins.getSelectedRow();
                txtMedId.setText(modelMedecins.getValueAt(r, 0).toString());
                txtMedNom.setText(modelMedecins.getValueAt(r, 1).toString());
                txtMedSpec.setText(modelMedecins.getValueAt(r, 2).toString());
                txtMedSal.setText(modelMedecins.getValueAt(r, 3).toString());
                txtMedDep.setText(modelMedecins.getValueAt(r, 4).toString());
                txtMedHoraires.setText(modelMedecins.getValueAt(r, 5).toString());
                txtMedEmail.setText("dr." + txtMedNom.getText().toLowerCase().replaceAll("[^a-z]", "") + "@clinique.tn");
            }
        });
        pnlTable.add(new JScrollPane(tblMedecins), BorderLayout.CENTER);

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
        modelMedecins.setRowCount(0);
        try {
            List<Medecin> liste = medecinDAO.getTous();
            for (Medecin m : liste) {
                modelMedecins.addRow(new Object[]{
                    m.getIdMed(), m.getNomMed(), m.getSpecialite(),
                    m.getSalaireBase() + " DT", m.getNomDepartement(), m.getHoraires()
                });
            }
            if (tblMedecins.getRowCount() > 0) tblMedecins.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
        }
    }

    private void dialogueAjouter() {
        JTextField id = new JTextField(String.valueOf(System.currentTimeMillis() % 1000));
        JTextField nom = new JTextField();
        JTextField spec = new JTextField();
        JTextField sal = new JTextField("3200");
        JComboBox<ComboItem> comboDeps = new JComboBox<>();
        try {
            for (ComboItem it : departementDAO.getComboItems()) comboDeps.addItem(it);
        } catch (SQLException ignored) {}

        Object[] fields = {"ID :", id, "Nom :", nom, "Spécialité :", spec, "Salaire :", sal, "Département (REF) :", comboDeps};

        if (JOptionPane.showConfirmDialog(this, fields, "Ajouter un Médecin", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                ComboItem dep = (ComboItem) comboDeps.getSelectedItem();
                medecinDAO.ajouter(Integer.parseInt(id.getText().trim()), nom.getText().trim(), spec.getText().trim(), Double.parseDouble(sal.getText().trim()), dep.getId());
                JOptionPane.showMessageDialog(this, "Médecin créé avec référence OID département !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void dialogueModifier() {
        int r = tblMedecins.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un médecin !"); return; }
        int id = (int) modelMedecins.getValueAt(r, 0);

        JTextField nom = new JTextField(txtMedNom.getText());
        JTextField spec = new JTextField(txtMedSpec.getText());
        JTextField sal = new JTextField(txtMedSal.getText().replace(" DT", ""));
        JTextField hor = new JTextField(txtMedHoraires.getText());
        JTextField mail = new JTextField(txtMedEmail.getText());
        JComboBox<ComboItem> comboDeps = new JComboBox<>();
        try {
            for (ComboItem it : departementDAO.getComboItems()) comboDeps.addItem(it);
        } catch (SQLException ignored) {}

        Object[] fields = {
            "ID : " + id, "Nom :", nom, "Spécialité :", spec, "Salaire (DT) :", sal,
            "Département (REF) :", comboDeps, "Horaires :", hor, "Email :", mail
        };

        if (JOptionPane.showConfirmDialog(this, fields, "Modifier Médecin", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                ComboItem dep = (ComboItem) comboDeps.getSelectedItem();
                medecinDAO.modifier(id, nom.getText().trim(), spec.getText().trim(), Double.parseDouble(sal.getText().trim()), dep.getId(), hor.getText().trim(), mail.getText().trim());
                JOptionPane.showMessageDialog(this, "Médecin mis à jour !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void actionSupprimer() {
        int r = tblMedecins.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un médecin !"); return; }
        int id = (int) modelMedecins.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le Dr. n° " + id + " ?\nAttention : les patients suivis par ce médecin auront une référence orpheline (DANGLING) !", "Alerte de cohérence", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                medecinDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Médecin supprimé d'Oracle !");
                charger();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void calculerPrime() {
        if (txtMedId.getText().isEmpty()) return;
        try {
            int id = Integer.parseInt(txtMedId.getText());
            double taux = Double.parseDouble(txtMedPrimeTaux.getText().trim());
            double prime = medecinDAO.calculerPrime(id, taux);
            JOptionPane.showMessageDialog(this, "Prime calculée (m.calculer_prime) = " + prime + " DT", "Méthode PL/SQL", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
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