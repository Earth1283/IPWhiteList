package io.github.Earth1283.ipwhitelist;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DatabaseManager {
    private final String url;
    private final Logger logger;
    private Connection connection;

    public DatabaseManager(String dataFolder, Logger logger) {
        this.url = "jdbc:sqlite:" + dataFolder + "/whitelist.db";
        this.logger = logger;
    }

    public void init() throws SQLException {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            connection = DriverManager.getConnection(url);
            createTable();
            checkAndMigrateTable();
        } catch (SQLException e) {
            logger.severe("Could not connect to SQLite database: " + e.getMessage());
            throw e;
        }
    }

    private void createTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS whitelist (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ip TEXT NOT NULL UNIQUE,
                    player_name TEXT,
                    added_by TEXT,
                    timestamp LONG
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void checkAndMigrateTable() {
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(whitelist)")) {
            boolean hasPlayerName = false;
            while (rs.next()) {
                if ("player_name".equalsIgnoreCase(rs.getString("name"))) {
                    hasPlayerName = true;
                    break;
                }
            }
            if (!hasPlayerName) {
                logger.info("Migrating database: Adding player_name column...");
                stmt.execute("ALTER TABLE whitelist ADD COLUMN player_name TEXT");
            }
        } catch (SQLException e) {
            logger.severe("Error migrating table: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("Error closing database connection: " + e.getMessage());
        }
    }

    public boolean addIP(String ip, String addedBy, String playerName) {
        String sql = "INSERT INTO whitelist(ip, added_by, player_name, timestamp) VALUES(?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            pstmt.setString(2, addedBy);
            pstmt.setString(3, playerName); // Can be null
            pstmt.setLong(4, System.currentTimeMillis());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                return false; // Already exists
            }
            logger.severe("Error adding IP: " + e.getMessage());
            return false;
        }
    }

    public boolean removeIP(String ip) {
        String sql = "DELETE FROM whitelist WHERE ip = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.severe("Error removing IP: " + e.getMessage());
            return false;
        }
    }

    public int removeIPsByPlayer(String playerName) {
        String sql = "DELETE FROM whitelist WHERE player_name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error removing IPs by player: " + e.getMessage());
            return 0;
        }
    }

    public List<String> getIPsByPlayer(String playerName) {
        List<String> ips = new ArrayList<>();
        String sql = "SELECT ip FROM whitelist WHERE player_name = ? COLLATE NOCASE";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ips.add(rs.getString("ip"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting IPs by player: " + e.getMessage());
        }
        return ips;
    }

    public boolean isWhitelisted(String ip) {
        String sql = "SELECT 1 FROM whitelist WHERE ip = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ip);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.severe("Error checking whitelist status: " + e.getMessage());
            return false;
        }
    }

    public List<String> getAllIPs() {
        List<String> ips = new ArrayList<>();
        String sql = "SELECT ip, player_name FROM whitelist";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ip = rs.getString("ip");
                String player = rs.getString("player_name");
                if (player != null && !player.isEmpty()) {
                    ips.add(ip + " (" + player + ")");
                } else {
                    ips.add(ip);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting all IPs: " + e.getMessage());
        }
        return ips;
    }
}
