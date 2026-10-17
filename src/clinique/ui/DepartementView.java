package clinique.ui;

import clinique.dao.DepartementDAO;
import clinique.model.Departement;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class DepartementView extends JPanel {

    private TableRowSorter<DefaultTableModel> sorterDepartements;
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

        // 1. FORMULAIRE DE GAUCHE
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

        // 2. PANNEAU DE DROITE : RECHERCHE CAPSULE AVEC VRAIES ICÔNES + TABLEAU
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        // A. Création du modèle et du tableau
        modelDepartements = new DefaultTableModel(new String[]{"ID", "Nom Département", "Étage", "Nb Médecins"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblDepartements = new JTable(modelDepartements);
        tblDepartements.setRowHeight(24);

        // B. Activation du Sorter
        sorterDepartements = new TableRowSorter<>(modelDepartements);
        tblDepartements.setRowSorter(sorterDepartements);

        // C. Écouteur de sélection sécurisé
        tblDepartements.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblDepartements.getSelectedRow() != -1) {
                int r = tblDepartements.convertRowIndexToModel(tblDepartements.getSelectedRow());
                txtDepId.setText(modelDepartements.getValueAt(r, 0).toString());
                txtDepNom.setText(modelDepartements.getValueAt(r, 1).toString());
                txtDepEtage.setText(modelDepartements.getValueAt(r, 2).toString());
            }
        });

        // D. EN-TÊTE AVEC SEARCH BAR & VRAIES ICÔNES PNG (search.png et clear.png)
        JPanel pnlTableTop = new JPanel(new BorderLayout(10, 5));
        pnlTableTop.setOpaque(false);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel lblTitreTable = new JLabel("Services Cliniques Enregistrés");
        lblTitreTable.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitreTable.setForeground(new Color(30, 41, 59));
        pnlTableTop.add(lblTitreTable, BorderLayout.WEST);

        // Boîtier de recherche en capsule
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setPreferredSize(new Dimension(230, 28));
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(2, 8, 2, 6)
        ));

        // 1. VRAIE ICÔNE DE RECHERCHE PNG (search.png)
        JLabel lblIconSearch = new JLabel();
        ImageIcon iconSearch = chargerIcone("search.png", 14, 14);
        if (iconSearch != null) {
            lblIconSearch.setIcon(iconSearch);
        } else {
            lblIconSearch.setText("🔍");
        }
        pnlSearchBox.add(lblIconSearch, BorderLayout.WEST);

        // 2. Champ de saisie avec Placeholder
        String placeholder = "Rechercher service, étage...";
        JTextField txtSearch = new JTextField(placeholder);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearch.setForeground(new Color(148, 163, 184));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        // 3. VRAIE ICÔNE BOUTON EFFACER PNG (clear.png)
        JLabel btnClear = new JLabel("", SwingConstants.CENTER);
        ImageIcon iconClear = chargerIcone("clear.png", 12, 12);
        if (iconClear != null) {
            btnClear.setIcon(iconClear);
        } else {
            btnClear.setText("✕");
            btnClear.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btnClear.setForeground(new Color(148, 163, 184));
        }
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.setVisible(false); // Caché au départ
        pnlSearchBox.add(btnClear, BorderLayout.EAST);

        // Gestion du Placeholder
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
                    sorterDepartements.setRowFilter(null);
                    btnClear.setVisible(false);
                }
            }
        });

        // Filtrage en direct
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                if (q.isEmpty() || q.equals(placeholder)) {
                    sorterDepartements.setRowFilter(null);
                    btnClear.setVisible(false);
                } else {
                    btnClear.setVisible(true);
                    sorterDepartements.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                }
            }
        });

        // Clic sur l'icône de suppression pour réinitialiser
        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtSearch.setText("");
                sorterDepartements.setRowFilter(null);
                btnClear.setVisible(false);
                txtSearch.requestFocus();
            }
        });

        pnlTableTop.add(pnlSearchBox, BorderLayout.EAST);
        pnlTable.add(pnlTableTop, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblDepartements), BorderLayout.CENTER);

        // E. Barre d'actions CRUD
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

    // BOÎTE DE DIALOGUE : AJOUTER UN DÉPARTEMENT
    private void dialogueAjouter() {
        int prochainId = 1;
        try {
            prochainId = departementDAO.getNextId();
        } catch (Exception ignored) {}

        JTextField txtId = new JTextField(String.valueOf(prochainId));
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245));
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtNom = new JTextField();
        JComboBox<String> comboEtage = new JComboBox<>(new String[]{"1", "2", "3"});

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(320, 200));

        ajouterComposant(mainForm, creerLabel("ID Département :"));
        ajouterComposant(mainForm, txtId);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Nom du Service Médical :"));
        ajouterComposant(mainForm, txtNom);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Étage du Service :"));
        ajouterComposant(mainForm, comboEtage);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Nouveau Département", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (txtNom.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez renseigner le nom du département !", "Champ obligatoire", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                departementDAO.ajouter(
                    Integer.parseInt(txtId.getText().trim()),
                    txtNom.getText().trim(),
                    Integer.parseInt(comboEtage.getSelectedItem().toString())
                );
                JOptionPane.showMessageDialog(this, "Département créé avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'insertion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // BOÎTE DE DIALOGUE : MODIFIER UN DÉPARTEMENT
    private void dialogueModifier() {
        int viewRow = tblDepartements.getSelectedRow();
        if (viewRow == -1) { 
            JOptionPane.showMessageDialog(this, "Veuillez d'abord sélectionner un département dans le tableau !"); 
            return; 
        }
        int r = tblDepartements.convertRowIndexToModel(viewRow);
        int id = (int) modelDepartements.getValueAt(r, 0);

        JTextField txtId = new JTextField(String.valueOf(id));
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245));
        txtId.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtNom = new JTextField(modelDepartements.getValueAt(r, 1).toString());
        JComboBox<String> comboEtage = new JComboBox<>(new String[]{"1", "2", "3"});
        comboEtage.setSelectedItem(modelDepartements.getValueAt(r, 2).toString());

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(320, 200));

        ajouterComposant(mainForm, creerLabel("ID Département :"));
        ajouterComposant(mainForm, txtId);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Nom du Service Médical :"));
        ajouterComposant(mainForm, txtNom);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Étage du Service :"));
        ajouterComposant(mainForm, comboEtage);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Modifier Département (ID: " + id + ")", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                departementDAO.modifier(
                    id,
                    txtNom.getText().trim(),
                    Integer.parseInt(comboEtage.getSelectedItem().toString())
                );
                JOptionPane.showMessageDialog(this, "Département mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur de mise à jour : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionSupprimer() {
        int viewRow = tblDepartements.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez un département !"); return; }
        int r = tblDepartements.convertRowIndexToModel(viewRow);
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

    // Méthode polyvalente pour charger et redimensionner n'importe quelle icône
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