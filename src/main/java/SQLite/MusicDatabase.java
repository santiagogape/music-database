package SQLite;

import java.sql.*;

public class MusicDatabase {
    private Connection connect() throws SQLException {
        String url = "jdbc:sqlite:C:\\Users\\santi\\Desktop\\music.db";
        return DriverManager.getConnection(url);
    }

    public void createTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS files (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                relative_path TEXT NOT NULL,
                title TEXT,
                album TEXT,
                track_number TEXT,
                artists TEXT
            );
        """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void insertFile(String path, String title, String album, String track, String artists) throws SQLException {
        String sql = "INSERT INTO files (relative_path, title, album, track_number, artists) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, path);
            pstmt.setString(2, title);
            pstmt.setString(3, album);
            pstmt.setString(4, track);
            pstmt.setString(5, artists);
            pstmt.executeUpdate();
        }
    }
}
