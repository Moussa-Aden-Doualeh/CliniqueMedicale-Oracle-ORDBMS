package clinique.dao;

import clinique.config.DatabaseConnection;
import clinique.model.Facture;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FactureDAO {

    public List<Facture> getToutes() throws SQLException {
        List<Facture> liste = new ArrayList<>();
        String sql = "SELECT f.numFacture, TO_CHAR(f.dateFacture, 'DD/MM/YYYY'), f.montant, "
                   + "       f.patientConcerne.nomPat, f.patientConcerne.adr.ville, f.medecinEmetteur.nomMed "
                   + "FROM Factures f ORDER BY f.numFacture";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Facture(
                    rs.getInt(1), rs.getString(2), rs.getDouble(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)
                ));
            }
        }
        return liste;
    }
    
    // Calcule automatiquement le prochain N° de facture (ex: 5011 après 5010)
    public int getNextNumFacture() throws SQLException {
        String sql = "SELECT NVL(MAX(f.numFacture), 5000) + 1 FROM Factures f";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 5001;
    }

    public void ajouter(int numFacture, double montant, int idPat, int idMed) throws SQLException {
        String sql = "INSERT INTO Factures VALUES (?, SYSDATE, ?, "
                   + "(SELECT REF(pt) FROM Patients pt WHERE pt.idPat = ?), "
                   + "(SELECT REF(md) FROM Medecins md WHERE md.idMed = ?), 1)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numFacture);
            ps.setDouble(2, montant);
            ps.setInt(3, idPat);
            ps.setInt(4, idMed);
            ps.executeUpdate();
            
        }
    }
    
    public void modifier(int numFacture, double montant, int idPat, int idMed) throws SQLException {
        String sql = "UPDATE Factures f SET f.montant = ?, "
                   + "f.patientConcerne = (SELECT REF(pt) FROM Patients pt WHERE pt.idPat = ?), "
                   + "f.medecinEmetteur = (SELECT REF(md) FROM Medecins md WHERE md.idMed = ?) "
                   + "WHERE f.numFacture = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, montant);
            ps.setInt(2, idPat);
            ps.setInt(3, idMed);
            ps.setInt(4, numFacture);
            ps.executeUpdate();
           
        }
    }

    public void supprimer(int numFacture) throws SQLException {
        String sql = "DELETE FROM Factures WHERE numFacture = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, numFacture);
            ps.executeUpdate();
            
        }
    }
}