package app.model.utilities.database;

import java.util.List;
import java.util.Optional;

public interface Database {

    void open();
    void close();
    void commit();
    String url();
    String folder();
    String name();

    void createTable(String definition);
    void deleteTable(String name);
    List<String> tables();
    void initialize();

    interface Table<T> {
        T insert(T item);
        void delete(Integer id);
        T update(Integer id, T item);
        Optional<T> get(Integer id);
        List<T> query(String sql);
        List<T> all();
        List<T> allWithOffset(Integer offset);
    }

    enum Tables {
        FILES("""
                CREATE TABLE IF NOT EXISTS FILES (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    NAME TEXT NOT NULL,
                    PATH TEXT UNIQUE NOT NULL,
                    CREATION NOT NULL,
                    TITLE TEXT,
                    ALBUM TEXT,
                    ARTISTS TEXT,
                    REQUEST TEXT NOT NULL
                );
                """),
        RESPONSES("""
                CREATE TABLE IF NOT EXISTS RESPONSES (
                    ID INTEGER PRIMARY KEY,
                    NAME TEXT NOT NULL,
                    PATH TEXT UNIQUE NOT NULL,
                    CREATION TEXT NOT NULL,
                    FOREIGN KEY (ID) REFERENCES FILES(ID) ON DELETE CASCADE
                );
                """),
        OBJECTS("""
                CREATE TABLE IF NOT EXISTS OBJECTS (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    TYPE TEXT NOT NULL,
                    SPOTIFY TEXT NOT NULL UNIQUE
                );
                """),
        IMAGES("""
                CREATE TABLE IF NOT EXISTS IMAGES (
                    OBJECT INTEGER NOT NULL,
                    NUMBER INT NOT NULL,
                    PATH TEXT UNIQUE NOT NULL,
                    WIDTH INT NOT NULL,
                    HEIGHT INT NOT NULL,
                    FOREIGN KEY (OBJECT) REFERENCES OBJECTS(ID) ON DELETE CASCADE,
                    PRIMARY KEY (OBJECT, NUMBER)
                );
                """),
        GENRES("""
                CREATE TABLE IF NOT EXISTS GENRES (
                    GENRE TEXT PRIMARY KEY
                );
                """),
        OBJECT_GENRES("""
                CREATE TABLE IF NOT EXISTS OBJECT_GENRES (
                    OBJECT INTEGER NOT NULL,
                    GENRE TEXT NOT NULL,
                    PRIMARY KEY (OBJECT, GENRE),
                    FOREIGN KEY (OBJECT) REFERENCES OBJECTS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (GENRE) REFERENCES GENRES(GENRE) ON DELETE CASCADE
                );
                """),
        ARTISTS("""
                CREATE TABLE IF NOT EXISTS ARTISTS (
                    ID INTEGER PRIMARY KEY,
                    NAME TEXT NOT NULL,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID) ON DELETE CASCADE
                );
                """),
        ALBUMS("""
                CREATE TABLE IF NOT EXISTS ALBUMS (
                    ID INTEGER PRIMARY KEY,
                    NAME TEXT NOT NULL,
                    TYPE TEXT NOT NULL,
                    TRACKS INT NOT NULL,
                    RELEASE TEXT NOT NULL,
                    RELEASE_PRECISION TEXT NOT NULL,
                    LABEL TEXT,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID) ON DELETE CASCADE
                );
                """),
        TRACKS("""
                CREATE TABLE IF NOT EXISTS TRACKS (
                    ID INTEGER PRIMARY KEY,
                    NAME TEXT NOT NULL,
                    ALBUM INTEGER NOT NULL,
                    NUMBER INT NOT NULL,
                    PATH TEXT UNIQUE NOT NULL,
                    CREATION TEXT NOT NULL,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (ID) REFERENCES ALBUMS(ID)
                );
                """),
        TRACK_ARTISTS("""
                CREATE TABLE IF NOT EXISTS TRACK_ARTISTS (
                    TRACK INTEGER NOT NULL,
                    ARTIST INTEGER NOT NULL,
                    PRIMARY KEY (TRACK, ARTIST),
                    FOREIGN KEY (TRACK) REFERENCES TRACKS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (ARTIST) REFERENCES ARTISTS(ID) ON DELETE CASCADE
                );
                """),
        ALBUM_ARTISTS("""
                CREATE TABLE IF NOT EXISTS ALBUM_ARTISTS (
                    ALBUM INTEGER NOT NULL,
                    ARTIST INTEGER NOT NULL,
                    PRIMARY KEY (ALBUM, ARTIST),
                    FOREIGN KEY (ALBUM) REFERENCES ALBUMS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (ARTIST) REFERENCES ARTISTS(ID) ON DELETE CASCADE
                );
                """);


        private final String definition;

        Tables(String definition){
            this.definition = definition;
        }

        public String definition() {
            return definition;
        }
    }


}
