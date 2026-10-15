package clinique.dao;

import clinique.config.DatabaseConnection;
import clinique.model.ComboItem;
import clinique.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PatientDAO {

    public List<Patient> getTous() throws SQLException {
        List<Patient> liste = new ArrayList<>();
        String sql = "SELECT p.idPat, p.nomPat, TO_CHAR(p.dateNaissance, 'DD/MM/YYYY'), "
                   + "       p.sexe, p.calculer_age(), p.adr.ville, p.medecinTraitant.nomMed "
                   + "FROM Patients p ORDER BY p.idPat";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Patient(
                    rs.getInt(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getInt(5), "", rs.getString(6), 0,
                    new ArrayList<>(), rs.getString(7)
                ));
            }
        }
        return liste;
    }

    public Vector<ComboItem> getComboItems() throws SQLException {
        Vector<ComboItem> list = new Vector<>();
        String sql = "SELECT idPat, nomPat FROM Patients ORDER BY idPat";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ComboItem(rs.getInt(1), rs.getString(2)));
            }
        }
        return list;
    }

    public Patient getFicheComplete(int idPat) throws SQLException {
        String sql = "SELECT p.idPat, p.nomPat, TO_CHAR(p.dateNaissance, 'DD/MM/YYYY'), p.sexe, p.calculer_age(), "
                   + "       p.adr.rue, p.adr.ville, p.adr.codePostal, p.medecinTraitant.nomMed "
                   + "FROM Patients p WHERE p.idPat = ?";

        String sqlTels = "SELECT COLUMN_VALUE FROM Patients p, TABLE(p.tels) WHERE p.idPat = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPat);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    List<String> tels = new ArrayList<>();
                    try (PreparedStatement psT = conn.prepareStatement(sqlTels)) {
                        psT.setInt(1, idPat);
                        try (ResultSet rsT = psT.executeQuery()) {
                            while (rsT.next()) {
                                tels.add(rsT.getString(1));
                            }
                        }
                    }

                    return new Patient(
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getInt(5), rs.getString(6), rs.getString(7), rs.getInt(8),
                        tels, rs.getString(9)
                    );
                }
            }
        }
        return null;
    }

    public void ajouter(int id, String nom, String dateN, String sexe, String rue, String ville, int cp, String varraySql, int idMed) throws SQLException {
        String sql = "INSERT INTO Patients VALUES (patient_t(?, ?, TO_DATE(?, 'DD/MM/YYYY'), ?, "
                   + "adresse_t(?, ?, ?), " + varraySql + ", consultations_nt(), "
                   + "(SELECT REF(m) FROM Medecins m WHERE m.idMed = ?)))";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, nom);
            ps.setString(3, dateN);
            ps.setString(4, sexe);
            ps.setString(5, rue);
            ps.setString(6, ville);
            ps.setInt(7, cp);
            ps.setInt(8, idMed);
            ps.executeUpdate();
            
        }
    }
    
    public void modifier(int id, String nom, String dateN, String sexe, String rue, String ville, int cp, String varraySql, int idMed) throws SQLException {
        String sql = "UPDATE Patients p SET p.nomPat = ?, p.dateNaissance = TO_DATE(?, 'DD/MM/YYYY'), "
                   + "p.sexe = ?, p.adr = adresse_t(?, ?, ?), p.tels = " + varraySql + ", "
                   + "p.medecinTraitant = (SELECT REF(m) FROM Medecins m WHERE m.idMed = ?) "
                   + "WHERE p.idPat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, dateN);
            ps.setString(3, sexe);
            ps.setString(4, rue);
            ps.setString(5, ville);
            ps.setInt(6, cp);
            ps.setInt(7, idMed);
            ps.setInt(8, id);
            ps.executeUpdate();
           
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM Patients WHERE idPat = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            
        }
    }
}