package com.pawcare;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Reusable database connection manager for PawCare.
 * Loads MySQL JDBC Driver, reads credentials from db.properties or environment variables,
 * and supplies JDBC Connection objects to Data Access Objects (DAOs).
 */
public class DBConnection {

    private static String url;
    private static String user;
    private static String password;
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
        loadDriver();
    }

    /**
     * Loads database configuration from db.properties or environment variables.
     */
    private static void loadConfiguration() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Try reading from project root db.properties
        File configFile = new File("db.properties");
        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                loaded = true;
            } catch (Exception e) {
                System.err.println("[WARN] Could not load db.properties: " + e.getMessage());
            }
        }

        // 2. Try reading from classpath if not found in root
        if (!loaded) {
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception ignored) {
            }
        }

        // Read properties with fallback to environment variables or local defaults
        url = props.getProperty("db.url",
                System.getenv("DB_URL") != null ? System.getenv("DB_URL")
                        : "jdbc:mysql://localhost:3306/pawcare_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

        user = props.getProperty("db.user",
                System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root");

        password = props.getProperty("db.password",
                System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "root");
    }

    /**
     * Explicitly loads the MySQL JDBC Driver into the JVM.
     */
    private static void loadDriver() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("\n[ERROR] MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) was not found!");
            System.err.println("Ensure mysql-connector-j-*.jar is on your classpath or lib folder.\n");
            driverLoaded = false;
        }
    }

    /**
     * Obtains a fresh JDBC Connection from DriverManager.
     * The caller is responsible for closing the connection (preferably using try-with-resources).
     *
     * @return active java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            loadDriver();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests the database connection and displays metadata.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        System.out.println("\n--- Testing MySQL JDBC Database Connection ---");
        System.out.println("JDBC Target URL : " + url);
        System.out.println("Database User   : " + user);

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("[SUCCESS] Successfully connected to MySQL Database!");
                System.out.println("DBMS Product    : " + meta.getDatabaseProductName() + " v" + meta.getDatabaseProductVersion());
                System.out.println("JDBC Driver     : " + meta.getDriverName() + " v" + meta.getDriverVersion());
                System.out.println("Active Schema   : " + conn.getCatalog());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[FAILED] Database connection failed!");
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message   : " + e.getMessage());
            System.err.println("\nTip: Please verify that MySQL Server is running and check credentials in 'db.properties'.");
        }
        return false;
    }

    /**
     * Reload configuration if the user edits db.properties at runtime.
     */
    public static void reloadConfig() {
        loadConfiguration();
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}
