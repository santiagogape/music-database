package jaudiotagger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Mp3Reader {
    public static List<Mp3Metadata> readFromFolder(String folderPath) {
        List<Mp3Metadata> result = new ArrayList<>();
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3")))) {
                try {
                    AudioFile audioFile = AudioFileIO.read(file);
                    Tag tag = audioFile.getTag();

                    String title = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE) : "";
                    String album = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM) : "";
                    String artists = tag != null ? tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST) : "";
                    result.add(new Mp3Metadata(file.getName(), title, album, artists));
                } catch (Exception e) {
                    System.err.println("❌ Error leyendo: " + file.getName() + " -> " + e.getMessage());
                }
            }
        } else {
            System.err.println("❌ La ruta no es una carpeta válida.");
        }

        return result;
    }

    public static void saveAsJson(FolderMetadata folderMetadata, String outputPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(folderMetadata, writer);
            System.out.println("✅ JSON generado en: " + outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error guardando JSON: " + e.getMessage());
        }
    }

    public static void saveAsJson(List<String> folderMetadata, String outputPath) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(outputPath)) {
            gson.toJson(folderMetadata, writer);
            System.out.println("✅ JSON generado en: " + outputPath);
        } catch (IOException e) {
            System.err.println("❌ Error guardando JSON: " + e.getMessage());
        }
    }

    public static void mainTest(String[] args) {
        String inputFolder = "C:\\Users\\santi\\Desktop\\musica";
        String outputFile = "C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\temp\\db.original.json";

        List<Mp3Metadata> metadataList = readFromFolder(inputFolder);
        FolderMetadata folderMetadata = new FolderMetadata(inputFolder, metadataList);

        saveAsJson(folderMetadata, outputFile);
    }

    public static void main(String[] args) {
        String inputFolder = "C:\\Users\\santi\\Desktop\\musica";
        String outputFile = "C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\temp\\db.original-formated.json";

        List<Mp3Metadata> metadataList = readFromFolder(inputFolder);
        List<String> list = Mp3Processor.processMetadata(metadataList);
        saveAsJson(list, outputFile);
    }
}
