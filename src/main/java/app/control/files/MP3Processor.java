package app.control.files;

import app.model.items.FileSong;
import app.model.utilities.filesMetadata.Mp3Metadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MP3Processor {
    private final MP3MetadataReader reader;
    private final ProcessMetadataToRequestString processor;

    public MP3Processor(MP3MetadataReader reader, ProcessMetadataToRequestString processor) {
        this.reader = reader;
        this.processor = processor;
    }

    public List<FileSong> process(Path folder){
        if (!Files.exists(folder)) throw new RuntimeException("doesn't exists");
        if (!Files.isDirectory(folder)) throw new RuntimeException("not a folder");
        List<Mp3Metadata> mp3Metadata = reader.fromFolder(folder);
        List<String> strings = processor.fromMetadata(mp3Metadata);
        List<FileSong> result = new ArrayList<>();
        IntStream.range(0, strings.size()).forEach(n -> addFileSongToResult(folder, mp3Metadata.get(n), strings.get(n), result));
        return result;
    }

    private static void addFileSongToResult(Path folder, Mp3Metadata meta, String request, List<FileSong> result) {
        result.add(new FileSong() {
            @Override
            public Integer id() {
                return 0;
            }

            @Override
            public String name() {
                return meta.fileName();
            }

            @Override
            public ItemType type() {
                return ItemType.track;
            }

            @Override
            public String path() {
                return folder.resolve(name()).toString();
            }

            @Override
            public LocalDateTime creation() {
                return LocalDateTime.now();
            }

            @Override
            public String title() {
                return meta.title();
            }

            @Override
            public String album() {
                return meta.album();
            }

            @Override
            public String artists() {
                return meta.artists();
            }

            @Override
            public String request() {
                return request;
            }
        });
    }
}
