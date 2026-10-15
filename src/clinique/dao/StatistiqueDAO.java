package clinique.dao;

import clinique.config.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatistiqueDAO {

    // 1. Chiffre d'Affaires Global de la clinique
    public double getTotalRecettes() throws SQLException {
        String sql = "SELECT NVL(SUM(f.montant), 0) FROM Factures f";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0;
    }

    // 2. Nombre d'actes médicaux terminés (Soins réalisés dans la Nested Table)
    public int getNbActesTermines() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Patients p, TABLE(p.historique) c WHERE c.statut = 'Terminé'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // 3. Nombre de Rendez-vous planifiés à venir (Charge de travail future)
    public int getNbRDVPlanifies() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Patients p, TABLE(p.historique) c WHERE c.statut = 'Planifié'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // 4. Performance et charge de travail par Médecin (Liaison REF + Nested Table + Factures)
    public List<Object[]> getRapportMedecins() throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT m.nomMed, m.specialite, "
                   + "       (SELECT COUNT(*) FROM Patients p WHERE p.medecinTraitant.idMed = m.idMed) AS nbPatients, "
                   + "       (SELECT COUNT(*) FROM Patients p, TABLE(p.historique) c WHERE p.medecinTraitant.idMed = m.idMed AND c.statut = 'Terminé') AS nbActes, "
                   + "       NVL((SELECT SUM(f.montant) FROM Factures f WHERE f.medecinEmetteur.idMed = m.idMed), 0) AS ca "
                   + "FROM Medecins m ORDER BY ca DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getInt(3) + " patient(s)",
                    rs.getInt(4) + " acte(s)",
                    rs.getDouble(5) + " DT"
                });
            }
        }
        return liste;
    }

    // 5. Rentabilité et ressources par Département Médical
    public List<Object[]> getRapportDepartements() throws SQLException {
        List<Object[]> liste = new ArrayList<>();
        String sql = "SELECT d.nomDep, 'Étage ' || d.etage, "
                   + "       (SELECT COUNT(*) FROM Medecins m WHERE m.dept.idDep = d.idDep) AS nbMedecins, "
                   + "       NVL((SELECT SUM(f.montant) FROM Factures f, Medecins m WHERE f.medecinEmetteur.idMed = m.idMed AND m.dept.idDep = d.idDep), 0) AS caService "
                   + "FROM Departements d ORDER BY caService DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(new Object[]{
                    rs.getString(1),
                    rs.getString(2),
                    rs.getInt(3) + " médecin(s)",
                    rs.getDouble(4) + " DT"
                });
            }
        }
        return liste;
    }
}