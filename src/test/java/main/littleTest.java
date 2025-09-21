package main;

import app.model.items.SimpleItem;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class littleTest {

    //tod -> this works -> refactor to flowTest as a nother flow (added -> tagged), keep filename -> nameWithExtension, change title -> name
    public static void main(String[] args) {
        File file = new File("C:\\Users\\santi\\Desktop\\musica\\test\\Ado - Crime and Punishment, sung.mp3");
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            throw new RuntimeException(e);
        }
        Tag tag = audioFile.getTagOrCreateAndSetDefault();

        try {
            tag.setField(FieldKey.TITLE, "シャルル");
            tag.setField(FieldKey.ALBUM, "シャルル");
            tag.setField(FieldKey.ARTISTS, "Ado");
            tag.setField(FieldKey.YEAR, "2025");
            tag.setField(FieldKey.TRACK, "1");
        } catch (FieldDataInvalidException e) {
            throw new RuntimeException(e);
        }

        try {
            audioFile.commit();
        } catch (CannotWriteException e) {
            throw new RuntimeException(e);
        }
    }
}
