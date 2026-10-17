package clinique.dao;

import clinique.config.DatabaseConnection;
import clinique.model.ComboItem;
import clinique.model.Medecin;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MedecinDAO {

    public List<Medecin> getTous() throws SQLException {
        List<Medecin> liste = new ArrayList<>();
        String sql = "SELECT m.idMed, m.nomMed, m.specialite, m.salaireBase, m.dept.nomDep, m.horaires, m.email "
                   + "FROM Medecins m ORDER BY m.idMed";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Medecin(
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getDouble(4), rs.getString(5), rs.getString(6), rs.getString(7)
                ));
            }
        }
        return liste;
    }
    
    // Calcule automatiquement le prochain ID médecin disponible (ex: 110 après 100)
    public int getNextId() throws SQLException {
        String sql = "SELECT NVL(MAX(m.idMed), 0) + 10 FROM Medecins m";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 10;
    }

    public Vector<ComboItem> getComboItems() throws SQLException {
        Vector<ComboItem> list = new Vector<>();
        String sql = "SELECT idMed, nomMed FROM Medecins ORDER BY idMed";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return list;
    }

    public double calculerPrime(int idMed, double taux) throws SQLException {
        String sql = "SELECT m.calculer_prime(?) FROM Medecins m WHERE m.idMed = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, taux);
            ps.setInt(2, idMed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    public void ajouter(int id, String nom, String spec, double salaire, int depId, String horaires, String email) throws SQLException {
        String sql = "INSERT INTO Medecins VALUES (medecin_t(?, ?, ?, ?, "
                   + "(SELECT REF(d) FROM Departements d WHERE d.idDep = ?), ?, ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nom);
            ps.setString(3, spec);
            ps.setDouble(4, salaire);
            ps.setInt(5, depId);
            ps.setString(6, email);
            ps.setString(7, horaires);
            ps.executeUpdate();
        }
    }
    
    public void modifier(int id, String nom, String spec, double salaire, int depId, String horaires, String email) throws SQLException {
        String sql = "UPDATE Medecins m SET m.nomMed = ?, m.specialite = ?, m.salaireBase = ?, "
                   + "m.dept = (SELECT REF(d) FROM Departements d WHERE d.idDep = ?), "
                   + "m.horaires = ?, m.email = ? WHERE m.idMed = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, spec);
            ps.setDouble(3, salaire);
            ps.setInt(4, depId);
            ps.setString(5, horaires);
            ps.setString(6, email);
            ps.setInt(7, id);
            ps.executeUpdate();
            
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Medecins WHERE idMed = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            
        }
    }
}