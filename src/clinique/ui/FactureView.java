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
import javax.swing.table.TableRowSorter;

public class FactureView extends JPanel {

    private final FactureDAO factureDAO = new FactureDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final MedecinDAO medecinDAO = new MedecinDAO();
    private static final Color COLOR_BG = new Color(245, 247, 250);
    private static final Color COLOR_TEXT_LABEL = new Color(194, 24, 91);

    private JTextField txtFacNum, txtFacDate, txtFacMontant, txtFacPat, txtFacVille, txtFacMed;
    private JTable tblFactures;
    private DefaultTableModel modelFactures;
    private TableRowSorter<DefaultTableModel> sorterFactures;

    public FactureView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. FORMULAIRE DE GAUCHE 
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

        // 2. PANNEAU DE DROITE : RECHERCHE MODERNE + TABLEAU + ACTIONS
        JPanel pnlTable = new JPanel(new BorderLayout(5, 5));
        pnlTable.setOpaque(false);

        // Modèle et tableau
        modelFactures = new DefaultTableModel(new String[]{"N° Facture", "Date", "Montant", "Patient", "Ville", "Médecin"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblFactures = new JTable(modelFactures);
        tblFactures.setRowHeight(24);

        // Activation du Sorter pour le filtrage
        sorterFactures = new TableRowSorter<>(modelFactures);
        tblFactures.setRowSorter(sorterFactures);

        // Écouteur de sélection sécurisé avec convertRowIndexToModel
        tblFactures.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblFactures.getSelectedRow() != -1) {
                int r = tblFactures.convertRowIndexToModel(tblFactures.getSelectedRow());
                txtFacNum.setText(modelFactures.getValueAt(r, 0).toString());
                txtFacDate.setText(modelFactures.getValueAt(r, 1).toString());
                txtFacMontant.setText(modelFactures.getValueAt(r, 2).toString());
                txtFacPat.setText(modelFactures.getValueAt(r, 3).toString());
                txtFacVille.setText(modelFactures.getValueAt(r, 4).toString());
                txtFacMed.setText(modelFactures.getValueAt(r, 5).toString());
            }
        });

        // EN-TÊTE PROFESSIONNEL AVEC CAPSULE DE RECHERCHE
        JPanel pnlTableTop = new JPanel(new BorderLayout(10, 5));
        pnlTableTop.setOpaque(false);
        pnlTableTop.setBorder(new EmptyBorder(0, 0, 6, 0));

        JLabel lblTitreTable = new JLabel("Facturation");
        lblTitreTable.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitreTable.setForeground(new Color(30, 41, 59));
        pnlTableTop.add(lblTitreTable, BorderLayout.WEST);

        // Capsule de recherche unifiée
        JPanel pnlSearchBox = new JPanel(new BorderLayout(8, 0));
        pnlSearchBox.setBackground(Color.WHITE);
        pnlSearchBox.setPreferredSize(new Dimension(230, 28));
        pnlSearchBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(2, 8, 2, 6)
        ));

        // 1. Vraie icône Loupe PNG
        JLabel lblIconSearch = new JLabel();
        ImageIcon iconSearch = chargerIcone("search.png", 14, 14);
        if (iconSearch != null) lblIconSearch.setIcon(iconSearch); else lblIconSearch.setText("🔍");
        pnlSearchBox.add(lblIconSearch, BorderLayout.WEST);

        // 2. Champ avec Placeholder
        String placeholder = "Filtrer facture, patient, ville...";
        JTextField txtSearch = new JTextField(placeholder);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtSearch.setForeground(new Color(148, 163, 184));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        pnlSearchBox.add(txtSearch, BorderLayout.CENTER);

        // 3. Vraie icône Effacer PNG
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
                    sorterFactures.setRowFilter(null);
                    btnClear.setVisible(false);
                }
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String q = txtSearch.getText().trim();
                if (q.isEmpty() || q.equals(placeholder)) {
                    sorterFactures.setRowFilter(null);
                    btnClear.setVisible(false);
                } else {
                    btnClear.setVisible(true);
                    sorterFactures.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                }
            }
        });

        btnClear.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                txtSearch.setText("");
                sorterFactures.setRowFilter(null);
                btnClear.setVisible(false);
                txtSearch.requestFocus();
            }
        });

        pnlTableTop.add(pnlSearchBox, BorderLayout.EAST);
        pnlTable.add(pnlTableTop, BorderLayout.NORTH);
        pnlTable.add(new JScrollPane(tblFactures), BorderLayout.CENTER);

        // Barre d'actions CRUD en bas
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

    // BOÎTE DE DIALOGUE : CRÉER FACTURE 
    private void dialogueAjouter() {
        int prochainNum = 5001;
        try {
            prochainNum = factureDAO.getNextNumFacture();
        } catch (Exception ignored) {}

        JTextField txtNum = new JTextField(String.valueOf(prochainNum));
        txtNum.setEditable(false);
        txtNum.setBackground(new Color(245, 245, 245));
        txtNum.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JComboBox<ComboItem> comboP = new JComboBox<>();
        JComboBox<ComboItem> comboM = new JComboBox<>();

        try {
            for (ComboItem it : patientDAO.getComboItems()) comboP.addItem(it);
            for (ComboItem it : medecinDAO.getComboItems()) comboM.addItem(it);
        } catch (SQLException ignored) {}

        JTextField txtMontant = new JTextField();
        txtMontant.setFont(new Font("Segoe UI", Font.BOLD, 12));

        Runnable actualiserMontantEtMedecin = () -> {
            ComboItem patSel = (ComboItem) comboP.getSelectedItem();
            if (patSel != null) {
                try {
                    double totalSoins = new clinique.dao.ConsultationDAO().getTotalTarifs(patSel.getId());
                    txtMontant.setText(String.valueOf(totalSoins > 0 ? totalSoins : 80.0));

                    clinique.model.Patient p = patientDAO.getFicheComplete(patSel.getId());
                    if (p != null) {
                        for (int i = 0; i < comboM.getItemCount(); i++) {
                            if (comboM.getItemAt(i).getLabel().contains(p.getNomMedecin())) {
                                comboM.setSelectedIndex(i);
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        };

        comboP.addActionListener(e -> actualiserMontantEtMedecin.run());
        actualiserMontantEtMedecin.run();

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 260));

        ajouterComposant(mainForm, creerLabel("N° Facture :"));
        ajouterComposant(mainForm, txtNum);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Patient Débiteur :"));
        ajouterComposant(mainForm, comboP);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Médecin Prescripteur :"));
        ajouterComposant(mainForm, comboM);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Montant Total à Payer (DT) :"));
        ajouterComposant(mainForm, txtMontant);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Créer Facture ", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem p = (ComboItem) comboP.getSelectedItem();
            ComboItem m = (ComboItem) comboM.getSelectedItem();
            if (p == null || m == null || txtMontant.getText().trim().isEmpty()) return;

            try {
                factureDAO.ajouter(
                    Integer.parseInt(txtNum.getText().trim()),
                    Double.parseDouble(txtMontant.getText().trim()),
                    p.getId(),
                    m.getId()
                );
                JOptionPane.showMessageDialog(this, "Facture enregistrée avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur d'insertion : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // BOÎTE DE DIALOGUE : MODIFIER FACTURE
    private void dialogueModifier() {
        int viewRow = tblFactures.getSelectedRow();
        if (viewRow == -1) { 
            JOptionPane.showMessageDialog(this, "Veuillez d'abord sélectionner une facture dans le tableau !"); 
            return; 
        }
        int r = tblFactures.convertRowIndexToModel(viewRow);
        int numFacture = (int) modelFactures.getValueAt(r, 0);
        String patientActuel = modelFactures.getValueAt(r, 3).toString();
        String medecinActuel = modelFactures.getValueAt(r, 5).toString();

        JTextField txtNum = new JTextField(String.valueOf(numFacture));
        txtNum.setEditable(false);
        txtNum.setBackground(new Color(245, 245, 245));
        txtNum.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JTextField txtMontant = new JTextField(txtFacMontant.getText().replace(" DT", "").trim());

        JComboBox<ComboItem> comboP = new JComboBox<>();
        JComboBox<ComboItem> comboM = new JComboBox<>();

        try {
            for (ComboItem it : patientDAO.getComboItems()) {
                comboP.addItem(it);
                if (it.getLabel().equalsIgnoreCase(patientActuel)) {
                    comboP.setSelectedItem(it);
                }
            }
            for (ComboItem it : medecinDAO.getComboItems()) {
                comboM.addItem(it);
                if (it.getLabel().contains(medecinActuel)) {
                    comboM.setSelectedItem(it);
                }
            }
        } catch (SQLException ignored) {}

        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setPreferredSize(new Dimension(340, 260));

        ajouterComposant(mainForm, creerLabel("N° Facture : " + numFacture));
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Patient Débiteur :"));
        ajouterComposant(mainForm, comboP);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Médecin Prescripteur :"));
        ajouterComposant(mainForm, comboM);
        mainForm.add(Box.createVerticalStrut(8));

        ajouterComposant(mainForm, creerLabel("Nouveau Montant Total (DT) :"));
        ajouterComposant(mainForm, txtMontant);

        if (JOptionPane.showConfirmDialog(this, mainForm, "Modifier Facture N° " + numFacture, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ComboItem p = (ComboItem) comboP.getSelectedItem();
            ComboItem m = (ComboItem) comboM.getSelectedItem();

            try {
                factureDAO.modifier(
                    numFacture,
                    Double.parseDouble(txtMontant.getText().trim()),
                    p.getId(),
                    m.getId()
                );
                JOptionPane.showMessageDialog(this, "Facture mise à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur de modification : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actionSupprimer() {
        int viewRow = tblFactures.getSelectedRow();
        if (viewRow == -1) { JOptionPane.showMessageDialog(this, "Sélectionnez une facture !"); return; }
        int r = tblFactures.convertRowIndexToModel(viewRow);
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