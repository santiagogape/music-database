package app.model.utilities.database;

import java.util.List;
import java.util.stream.Stream;

public interface Database {

    void open();
    void close();
    void commit();
    String url();
    void url(String url);

    interface Table<T> {
        boolean insert(T item);
        boolean delete(Integer id);
        boolean update(Integer id, T item);
        T get(Integer id);
        List<T> query(String sql);
        Stream<T> all();
    }

    enum Tables {
        FILES("""
                CREATE TABLE IF NOT EXISTS FILES (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PATH TEXT NOT NULL,
                    NAME TEXT NOT NULL,
                    TITLE TEXT,
                    ALBUM TEXT,
                    ARTISTS TEXT,
                    REQUEST TEXT
                );
                """),
        RESPONSES("""
                CREATE TABLE IF NOT EXISTS RESPONSES (
                    ID INTEGER PRIMARY KEY
                    FOREIGN KEY (ID) REFERENCES FILES(ID)
                    PATH TEXT
                );
                """),
        OBJECTS("""
                CREATE TABLE IF NOT EXISTS OBJECTS (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    PATH TEXT NOT NULL,
                    NAME TEXT NOT NULL,
                    TYPE TEXT NOT NULL,
                    CREATION TEXT NOT NULL
                );
                """),
        IMAGES("""
                CREATE TABLE IF NOT EXISTS IMAGES (
                    OBJECT INTEGER NOT NULL,
                    NUMBER INT NOT NULL,
                    PATH TEXT NOT NULL,
                    WIDTH INT NOT NULL,
                    HEIGHT INT NOT NULL,
                    FOREIGN KEY (OBJECT) REFERENCES OBJECTS(ID),
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
                    FOREIGN KEY (OBJECT) REFERENCES OBJECTS(ID),
                    FOREIGN KEY (GENRE) REFERENCES GENRES(GENRE)
                );
                """),
        TRACKS("""
                CREATE TABLE IF NOT EXISTS TRACKS (
                    ID INTEGER PRIMARY KEY,
                    TITLE TEXT NOT NULL,
                    ALBUM INTEGER NOT NULL,
                    NUMBER INT NOT NULL,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID)
                    FOREIGN KEY (ID) REFERENCES ALBUMS(ID)
                );
                """),
        TRACK_ARTISTS("""
                CREATE TABLE IF NOT EXISTS TRACK_ARTISTS (
                TRACK INTEGER NOT NULL,
                ARTIST INTEGER NOT NULL,
                PRIMARY KEY (TRACK, ARTIST),
                FOREIGN KEY (TRACK) REFERENCES TRACKS(ID),
                FOREIGN KEY (ARTIST) REFERENCES ARTISTS(ID)
                );
        """),
        ALBUMS("""
                CREATE TABLE IF NOT EXISTS ALBUMS (
                    ID INTEGER PRIMARY KEY,
                    TYPE TEXT NOT NULL,
                    TRACKS INT NOT NULL,
                    RELEASE TEXT NOT NULL,
                    RELEASE_PRECISION TEXT NOT NULL,
                    LABEL TEXT,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID)
                );
                """),
        ALBUM_ARTISTS("""
                CREATE TABLE IF NOT EXISTS ALBUM_ARTISTS (
                    ALBUM INTEGER NOT NULL,
                    ARTIST INTEGER NOT NULL,
                    PRIMARY KEY (ALBUM, ARTIST),
                    FOREIGN KEY (ALBUM) REFERENCES ALBUMS(ID),
                    FOREIGN KEY (ARTIST) REFERENCES ARTISTS(ID)
                );
                """),
        ARTISTS("""
                CREATE TABLE IF NOT EXISTS ARTISTS (
                ID INTEGER PRIMARY KEY,
                FOREIGN KEY (ID) REFERENCES OBJECTS(ID)
                );
                """);


        Tables(String definition){};
    }

}
