package app.control.files;

import app.model.utilities.filesMetadata.Mp3Metadata;

import java.util.List;
import java.util.stream.Collectors;

public final class ProcessMetadataToRequestString {
    public List<String> fromMetadata(List<Mp3Metadata> metadataList) {
        return metadataList.stream()
                .map(meta -> {
                    boolean hasAll = meta.title() != null && !meta.title().isBlank()
                            && meta.album() != null && !meta.album().isBlank()
                            && meta.artists() != null && !meta.artists().isBlank();

                    if (hasAll) {
                        String cleanArtists = meta.artists().replaceAll(" - Topic$", "").trim();

                        boolean artistInTitle = meta.title().toLowerCase().contains(cleanArtists.toLowerCase());

                        if (meta.title().equalsIgnoreCase(meta.album())) {
                            return artistInTitle
                                    ? meta.title()
                                    : String.join(",", meta.title(), cleanArtists);
                        } else {
                            return artistInTitle
                                    ? String.join(",", meta.title(), meta.album())
                                    : String.join(",", meta.title(), meta.album(), cleanArtists);
                        }

                    } else {
                        return meta.fileName().replaceAll("(?i)\\.mp3$", "");
                    }
                })
                .collect(Collectors.toList());
    }
}
