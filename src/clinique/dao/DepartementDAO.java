package clinique.dao;

import clinique.config.DatabaseConnection;
import clinique.model.ComboItem;
import clinique.model.Departement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class DepartementDAO {

    public List<Departement> getTous() throws SQLException {
        List<Departement> liste = new ArrayList<>();
        String sql = "SELECT d.idDep, d.nomDep, d.etage, "
                   + "       (SELECT COUNT(*) FROM Medecins m WHERE m.dept.idDep = d.idDep) "
                   + "FROM Departements d ORDER BY d.idDep";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Departement(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
            }
        }
        return liste;
    }

    public Vector<ComboItem> getComboItems() throws SQLException {
        Vector<ComboItem> list = new Vector<>();
        String sql = "SELECT idDep, nomDep FROM Departements ORDER BY idDep";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return list;
    }
    
    // Calcule automatiquement le prochain ID département 
    public int getNextId() throws SQLException {
        String sql = "SELECT NVL(MAX(d.idDep), 0) + 1 FROM Departements d";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    public void ajouter(int id, String nom, int etage) throws SQLException {
        String sql = "INSERT INTO Departements VALUES (departement_t(?, ?, ?))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nom);
            ps.setInt(3, etage);
            ps.executeUpdate();
            
        }
    }
    
    public void modifier(int id, String nom, int etage) throws SQLException {
        String sql = "UPDATE Departements d SET d.nomDep = ?, d.etage = ? WHERE d.idDep = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setInt(2, etage);
            ps.setInt(3, id);
            ps.executeUpdate();
           
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Departements WHERE idDep = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
           
        }
    }
}