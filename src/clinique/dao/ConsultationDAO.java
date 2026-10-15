package clinique.dao;

import clinique.config.DatabaseConnection;
import clinique.model.Consultation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {

    // 1. LIRE les actes d'un patient
    public List<Consultation> getParPatient(int idPat) throws SQLException {
        List<Consultation> liste = new ArrayList<>();
        String sql = "SELECT c.numCons, TO_CHAR(c.dateCons, 'DD/MM/YYYY'), c.motif, c.statut, c.tarif "
                   + "FROM Patients p, TABLE(p.historique) c "
                   + "WHERE p.idPat = ? ORDER BY c.numCons";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPat);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Consultation(
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getDouble(5), new ArrayList<>()
                    ));
                }
            }
        }
        return liste;
    }

    // 2. Calculer le total des dépenses du patient
    public double getTotalTarifs(int idPat) throws SQLException {
        String sql = "SELECT NVL(SUM(c.tarif), 0) FROM Patients p, TABLE(p.historique) c WHERE p.idPat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // 3. Lire les médicaments du VARRAY d'un acte
    public List<String> getMedicamentsActe(int idPat, int numCons) throws SQLException {
        List<String> meds = new ArrayList<>();
        String sql = "SELECT m.COLUMN_VALUE "
                   + "FROM Patients p, TABLE(p.historique) c, TABLE(c.medicaments) m "
                   + "WHERE p.idPat = ? AND c.numCons = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPat);
            ps.setInt(2, numCons);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    meds.add(rs.getString(1));
                }
            }
        }
        return meds;
    }

    // 4. AJOUTER directement dans la Nested Table (avec médicaments VARRAY)
    public void ajouterActe(int idPat, String dateC, String motif, String statut, double tarif, String medicament) throws SQLException {
        String varrayMed = (medicament == null || medicament.trim().isEmpty()) 
                ? "prescriptions_va()" 
                : "prescriptions_va('" + medicament.trim().replace("'", "''") + "')";

        String sql = "INSERT INTO TABLE(SELECT p.historique FROM Patients p WHERE p.idPat = " + idPat + ") "
                   + "VALUES (consultation_t("
                   + "  NVL((SELECT MAX(c.numCons) FROM Patients pt, TABLE(pt.historique) c), 0) + 1, "
                   + "  TO_DATE('" + dateC + "', 'DD/MM/YYYY'), "
                   + "  '" + motif.replace("'", "''") + "', "
                   + "  " + tarif + ", "
                   + "  '" + statut + "', "
                   + "  " + varrayMed + "))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // 5. MODIFIER un acte dans la Nested Table
    public void modifierActe(int idPat, int numCons, String motif, String statut, double tarif, String medicament) throws SQLException {
        String varrayMed = (medicament == null || medicament.trim().isEmpty()) 
                ? "prescriptions_va()" 
                : "prescriptions_va('" + medicament.trim().replace("'", "''") + "')";

        String sql = "UPDATE TABLE(SELECT p.historique FROM Patients p WHERE p.idPat = " + idPat + ") c "
                   + "SET c.motif = '" + motif.replace("'", "''") + "', c.statut = '" + statut + "', "
                   + "c.tarif = " + tarif + ", c.medicaments = " + varrayMed + " "
                   + "WHERE c.numCons = " + numCons;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    // 6. SUPPRIMER un acte de la Nested Table
    public void supprimerActe(int idPat, int numCons) throws SQLException {
        String sql = "DELETE FROM TABLE(SELECT p.historique FROM Patients p WHERE p.idPat = " + idPat + ") c "
                   + "WHERE c.numCons = " + numCons;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
}