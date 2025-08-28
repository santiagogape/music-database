package Main;

import app.model.items.FileSong;
import app.model.utilities.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MainDatabase {

    private final String folder;
    private final String name;

    public MainDatabase(String folder, String name) {
        this.folder = folder;
        this.name = name;
    }

    private final Database database = defineDatabase();


    private Database defineDatabase(){
        return new Database() {
            String folder = MainDatabase.this.folder;
            String name = MainDatabase.this.name;
            Connection connection;

            private void execute(String sql){
                try (Statement st = connection.createStatement()) {
                    st.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void initialize() {
                for (Tables t: Tables.values()){
                    System.out.println("executing: " + t.definition());
                    execute(t.definition());
                }
            }

            @Override
            public String url() {
                return folder + "\\" + name;
            }

            @Override
            public String folder() {
                return folder;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public void createTable(String definition) {
                execute(definition);
            }

            @Override
            public void deleteTable(String name) {
                execute("DROP TABLE IF EXISTS " + name + ";");
            }

            @Override
            public List<String> tables() {
                List<String> tables = new ArrayList<>();
                String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";

                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        tables.add(rs.getString("name"));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return tables;
            }


            @Override
            public void open() {
                try {
                    connection = DriverManager.getConnection(this.url());
                    try (Statement st = connection.createStatement()) {
                        st.execute("PRAGMA foreign_keys = ON;");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void commit() {
                try {
                    connection.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void close() {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


}
