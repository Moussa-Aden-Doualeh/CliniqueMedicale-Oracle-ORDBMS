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
import javax.swing.table.TableRowSorter;

public class MedecinView extends JPanel {

    private TableRowSorter<DefaultTableModel> sorterMedecins;
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

        // 1. FORMULAIRE DE GAUCHE 
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
        pnlForm.add(creerLabel("Service :"));         pnlForm.add(txtMedDep);
        pnlForm.add(creerLabel("Horaires :"));        pnlForm.add(txtMedHoraires);
        pnlForm.add(creerLabel("Email :"));           pnlForm.add(txtMedEmail);
        pnlForm.add(creerLabel("Taux Prime :"));      pnlForm.add(txtMedPrimeTaux);

        JButton btnPrime = new JButton("Calculer Prime");
        btnPrime.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnPrime.setBackground(new Color(230, 126, 34));
        btnPrime.setForeground(Color.WHITE);
        btnPrime.setFocusPainted(false);
        btnPrime.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrime.addActionListener(e -> calculerPrime());
        pnlForm.add(btnPrime);
        pnlForm.add(new JLabel(""));

        add(pnlForm, BorderLayout.WEST);

        // 2. PANNEAU DE DROITE 
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        modelMedecins = new DefaultTableModel(new String[]{
            "ID", "Nom", "Spécialité", "Salaire", "Service", "Horaires", "Email"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
         
        tblMedecins = new JTable(modelMedecins);
        tblMedecins.setRowHeight(24);

        // Activation du Sorter pour le filtrage
        sorterMedecins = new TableRowSorter<>(modelMedecins);
        tblMedecins.setRowSorter(sorterMedecins);

        // Écouteur de sélection sécurisé avec convertRowIndexToModel
        tblMedecins.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblMedecins.getSelectedRow() != -1) {
                int r = tblMedecins.convertRowIndexToModel(tblMedecins.getSelectedRow());
                txtMedId.setText(modelMedecins.getValueAt(r, 0).toString());
                txtMedNom.setText(modelMedecins.getValueAt(r, 1).toString());
                txtMedSpec.setText(modelMedecins.getValueAt(r, 2).toString());
                txtMedSal.setText(modelMedecins.getValueAt(r, 3).toString());
                txtMedDep.setText(modelMedecins.getValueAt(r, 4).toString());
                txtMedHoraires.setText(modelMedecins.getValueAt(r, 5).toString());
                txtMedEmail.setText(modelMedecins.getValueAt(r, 6).toString());
            }
        });

        // EN-TÊTE PROFESSIONNEL AVEC CAPSULE DE RECHERCHE
        JPanel pnlTableTop = new JPanel(new BorderLayout(10, 5));
        pnlTableTop.setOpaque(false);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel lblTitreTable = new JLabel("Corps Médical Enregistré");
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
        ImageIcon iconSearch = chargerIconeBouton("search.png");
        if (iconSearch != null) lblIconSearch.setIcon(new ImageIcon(iconSearch.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH)));
        else lblIconSearch.setText("🔍");
        pnlSearchBox.add(lblIconSearch, BorderLayout.WEST);

        String placeholder = "Filtrer par nom, spécialité...";
        JTextField txtSearch = new JTextField(placeholder);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearch.setForeground(new Color(148, 163, 184));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        JLabel btnClear = new JLabel("", SwingConstants.CENTER);
        ImageIcon iconClear = chargerIconeBouton("clear.png");
        if (iconClear != null) btnClear.setIcon(new ImageIcon(iconClear.getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH)));
        else btnClear.setText("✕");
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
                    sorterMedecins.setRowFilter(null);
                    btnClear.setVisible(false);
                }
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                if (q.isEmpty() || q.equals(placeholder)) {
                    sorterMedecins.setRowFilter(null);
                    btnClear.setVisible(false);
                } else {
                    btnClear.setVisible(true);
                    sorterMedecins.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                }
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtSearch.setText("");
                sorterMedecins.setRowFilter(null);
                btnClear.setVisible(false);
                txtSearch.requestFocus();
            }
        });

        pnlTableTop.add(pnlSearchBox, BorderLayout.EAST);
        pnlTable.add(pnlTableTop, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblMedecins), BorderLayout.CENTER);

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
        modelMedecins.setRowCount(0);
        try {
            List<Medecin> liste = medecinDAO.getTous();
            for (Medecin m : liste) {
                modelMedecins.addRow(new Object[]{
                    m.getIdMed(), 
                    m.getNomMed(), 
                    m.getSpecialite(),
                    m.getSalaireBase() + " DT", 
                    m.getNomDepartement(), 
                    m.getHoraires(),
                    m.getEmail()
                });
            }
            if (tblMedecins.getRowCount() > 0) tblMedecins.setRowSelectionInterval(0, 0);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur Médecins : " + ex.getMessage());
        }
    }

    // BOÎTE DE DIALOGUE : AJOUTER UN MÉDECIN
    private void dialogueAjouter() {
        int prochainId = 10;
        try {
            prochainId = medecinDAO.getNextId();
        } catch (Exception ignored) {}

        JTextField txtId = new JTextField(String.valueOf(prochainId));
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245));
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtNom = new JTextField();
        JTextField txtSpec = new JTextField();
        JTextField txtSal = new JTextField();
        JTextField txtHoraires = new JTextField("08h00 - 14h00");
        JTextField txtEmail = new JTextField();

        txtNom.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String clean = txtNom.getText().trim().toLowerCase().replaceAll("[^a-z]", "");
                if (!clean.isEmpty()) {
                    txtEmail.setText("dr." + clean + "@clinique.tn");
                }
            }
        });

        JComboBox<ComboItem> comboDeps = new JComboBox<>();
        try {
            for (ComboItem it : departementDAO.getComboItems()) comboDeps.addItem(it);
        } catch (SQLException ignored) {}

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 360));

        ajouterComposant(mainForm, creerLabel("ID Médecin :"));
        ajouterComposant(mainForm, txtId);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Nom du Médecin :"));
        ajouterComposant(mainForm, txtNom);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Spécialité :"));
        ajouterComposant(mainForm, txtSpec);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Salaire de Base (DT) :"));
        ajouterComposant(mainForm, txtSal);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Département d'Affectation :"));
        ajouterComposant(mainForm, comboDeps);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Horaires de Disponibilité :"));
        ajouterComposant(mainForm, txtHoraires);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Email Professionnel :"));
        ajouterComposant(mainForm, txtEmail);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Nouveau Médecin", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (txtNom.getText().trim().isEmpty() || txtSpec.getText().trim().isEmpty() || txtSal.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez renseigner au moins le Nom, la Spécialité et le Salaire !", "Champs obligatoires", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ComboItem dep = (ComboItem) comboDeps.getSelectedItem();
                medecinDAO.ajouter(
                    Integer.parseInt(txtId.getText().trim()),
                    txtNom.getText().trim(),
                    txtSpec.getText().trim(),
                    Double.parseDouble(txtSal.getText().trim()),
                    dep.getId(),
                    txtHoraires.getText().trim(),
                    txtEmail.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "Médecin créé avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'insertion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // BOÎTE DE DIALOGUE : MODIFIER UN MÉDECIN
    private void dialogueModifier() {
        int viewRow = tblMedecins.getSelectedRow();
        if (viewRow == -1) { 
            JOptionPane.showMessageDialog(this, "Veuillez d'abord sélectionner un médecin dans le tableau !"); 
            return; 
        }
        int r = tblMedecins.convertRowIndexToModel(viewRow);
        int id = (int) modelMedecins.getValueAt(r, 0);

        JTextField txtId = new JTextField(String.valueOf(id));
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245));
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtNom = new JTextField(modelMedecins.getValueAt(r, 1).toString());
        JTextField txtSpec = new JTextField(modelMedecins.getValueAt(r, 2).toString());
        JTextField txtSal = new JTextField(modelMedecins.getValueAt(r, 3).toString().replace(" DT", "").trim());
        JTextField txtHoraires = new JTextField(modelMedecins.getValueAt(r, 5) != null ? modelMedecins.getValueAt(r, 5).toString() : "08h00 - 14h00");
        JTextField txtEmail = new JTextField(modelMedecins.getValueAt(r, 6) != null ? modelMedecins.getValueAt(r, 6).toString() : "");

        JComboBox<ComboItem> comboDeps = new JComboBox<>();
        try {
            String depActuel = modelMedecins.getValueAt(r, 4).toString();
            for (ComboItem it : departementDAO.getComboItems()) {
                comboDeps.addItem(it);
                if (it.getLabel().equalsIgnoreCase(depActuel)) {
                    comboDeps.setSelectedItem(it);
                }
            }
        } catch (SQLException ignored) {}

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 360));

        ajouterComposant(mainForm, creerLabel("ID Médecin :"));
        ajouterComposant(mainForm, txtId);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Nom du Médecin :"));
        ajouterComposant(mainForm, txtNom);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Spécialité :"));
        ajouterComposant(mainForm, txtSpec);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Salaire de Base (DT) :"));
        ajouterComposant(mainForm, txtSal);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Département d'Affectation :"));
        ajouterComposant(mainForm, comboDeps);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Horaires de Disponibilité :"));
        ajouterComposant(mainForm, txtHoraires);
        mainForm.add(Box.createVerticalStrut(6));

        ajouterComposant(mainForm, creerLabel("Email Professionnel :"));
        ajouterComposant(mainForm, txtEmail);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Modifier Médecin (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                ComboItem dep = (ComboItem) comboDeps.getSelectedItem();
                medecinDAO.modifier(
                    id,
                    txtNom.getText().trim(),
                    txtSpec.getText().trim(),
                    Double.parseDouble(txtSal.getText().trim()),
                    dep.getId(),
                    txtHoraires.getText().trim(),
                    txtEmail.getText().trim()
                );

                JOptionPane.showMessageDialog(this, "Médecin mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur de mise à jour : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
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
    
    private void actionSupprimer() {
        int viewRow = tblMedecins.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un médecin !"); return; }
        int r = tblMedecins.convertRowIndexToModel(viewRow);
        int id = (int) modelMedecins.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "Supprimer le Dr. n° " + id + " ?\nAttention : les patients suivis par ce médecin perdront leur référence !", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            try {
                medecinDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Médecin supprimé !");
                charger();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
            }
        }
    }

    private void calculerPrime() {
        if (txtMedId.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Sélectionnez un médecin dans le tableau !"); return; }
        try {
            int id = Integer.parseInt(txtMedId.getText());
            double taux = Double.parseDouble(txtMedPrimeTaux.getText().trim());
            double prime = medecinDAO.calculerPrime(id, taux);
            JOptionPane.showMessageDialog(this, "Montant de la prime calculée (m.calculer_prime) = " + prime + " DT", "Résultat PL/SQL", JOptionPane.INFORMATION_MESSAGE);
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