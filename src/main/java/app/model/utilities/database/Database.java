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

    List<String> tables();
    void initialize();

    interface Table<T> {
        T insert(T item);
        List<T> query(String sql);
        List<T> all();
        List<T> allWithOffset(Integer offset);
    }

    interface TableIntID<T> extends Table<T>{
        void delete(Integer id);
        Optional<T> get(Integer id);
    }

    interface TableStringID<T> extends Table<T>{
        void delete(String id);
        Optional<T> get(String id);
    }

    interface UpdateTableIntID<T> extends TableIntID<T>{
        T update(T item);
    }

    interface UpdateTableStringID<T> extends TableStringID<T>{
        T update(T item);
    }

    enum ItemSource {
        spotify,youtube,tiktok,other
    }

    enum Tables {
        DIRECTORIES("""
                CREATE TABLE IF NOT EXISTS DIRECTORIES (
                    NAME TEXT PRIMARY KEY
                );
                """),
        FILES("""
                CREATE TABLE IF NOT EXISTS FILES (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    NAME TEXT NOT NULL,
                    DIRECTORY TEXT NOT NULL,
                    CREATION TEXT NOT NULL,
                    TITLE TEXT,
                    ALBUM TEXT,
                    ARTISTS TEXT,
                    REQUEST TEXT NOT NULL,
                    UNIQUE (DIRECTORY, NAME),
                    FOREIGN KEY (DIRECTORY) REFERENCES DIRECTORIES(NAME)
                );
                """),
        RESPONSES("""
                CREATE TABLE IF NOT EXISTS RESPONSES (
                    ID INTEGER PRIMARY KEY,
                    NAME TEXT NOT NULL,
                    PATH TEXT NOT NULL,
                    CREATION TEXT NOT NULL,
                    STATUS TEXT NOT NULL DEFAULT 'not_checked',
                    FOREIGN KEY (ID) REFERENCES FILES(ID) ON DELETE CASCADE
                );
                """),
        SOURCES("""
                CREATE TABLE IF NOT EXISTS SOURCES (
                    ID INTEGER PRIMARY KEY,
                    FOREIGN KEY (ID) REFERENCES FILES(ID) ON DELETE CASCADE
                );
                """),
        OBJECTS("""
                CREATE TABLE IF NOT EXISTS OBJECTS (
                    ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    TYPE TEXT NOT NULL,
                    SOURCE TEXT NOT NULL DEFAULT 'spotify',
                    ID_SOURCE TEXT NOT NULL UNIQUE
                );
                """),
        WEB_IMAGES("""
                CREATE TABLE IF NOT EXISTS WEB_IMAGES(
                    OBJECT INTEGER NOT NULL,
                    SOURCE TEXT UNIQUE NOT NULL,
                    WIDTH INT NOT NULL,
                    HEIGHT INT NOT NULL,
                    FOREIGN KEY (OBJECT) REFERENCES OBJECTS(ID) ON DELETE CASCADE,
                    PRIMARY KEY (OBJECT, SOURCE)
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
                    PATH TEXT NOT NULL,
                    CREATION TEXT NOT NULL,
                    FOREIGN KEY (ID) REFERENCES OBJECTS(ID) ON DELETE CASCADE,
                    FOREIGN KEY (ALBUM) REFERENCES ALBUMS(ID)
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
