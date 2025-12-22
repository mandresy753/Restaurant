import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection {
    private final Dotenv dotenv = Dotenv.load();
    private String URL = dotenv.get("URL");
    private String USER = dotenv.get("USER");
    private String PASSWORD = dotenv.get("PASSWORD");
    private Connection connection;

    public Connection getDBConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }} catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
