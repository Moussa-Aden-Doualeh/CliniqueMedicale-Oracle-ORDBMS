package clinique.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    // L'URL standard d'Oracle (seul l'hôte/port est ici, AUCUN mot de passe !)
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:XE";

    // Mémorise la session active après validation du formulaire
    private static String sessionUser = "";
    private static String sessionPassword = "";

    // Méthode de vérification dynamique appelée par le formulaire de Login
    public static boolean verifierConnexion(String user, String password) throws SQLException {
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);

        // Si c'est le compte sys, on active le privilège sysdba
        if (user.equalsIgnoreCase("sys")) {
            props.put("internal_logon", "sysdba");
        }

        // Tente la connexion directement auprès du moteur Oracle
        try (Connection conn = DriverManager.getConnection(DB_URL, props)) {
            if (conn != null && !conn.isClosed()) {
                // Si la connexion a réussi, on mémorise pour la suite de l'application
                sessionUser = user;
                sessionPassword = password;
                return true;
            }
        }
        return false;
    }

    // Fournit la connexion validée à toute l'application
    public static Connection getConnection() throws SQLException {
        if (sessionUser.isEmpty() || sessionPassword.isEmpty()) {
            throw new SQLException("Aucun utilisateur connecté ! Veuillez vous authentifier.");
        }

        Properties props = new Properties();
        props.put("user", sessionUser);
        props.put("password", sessionPassword);

        if (sessionUser.equalsIgnoreCase("sys")) {
            props.put("internal_logon", "sysdba");
        }

        return DriverManager.getConnection(DB_URL, props);
    }

    public static String getSessionUser() {
        return sessionUser;
    }
}