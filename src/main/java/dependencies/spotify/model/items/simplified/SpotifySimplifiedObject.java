package dependencies.spotify.model.items.simplified;

import dependencies.spotify.model.items.SpotifyExternalUrls;

public class SpotifySimplifiedObject {
    private final String name;
    private final String type;
    private final String uri;
    private final String id;



    private final SpotifyExternalUrls external_urls;

    /*
        type: string // album, artist, track
        uri: string //spotify:type:id
         */
    public SpotifySimplifiedObject (String id, SpotifyExternalUrls external_urls,
                                    String name,
                                    String type,
                                    String uri){
        this.name = name;
        this.type = type;
        this.uri = uri;
        this.id = id;
        this.external_urls = external_urls;
    }

    @Override
    public String toString() {
        return "SpotifySimplifiedObject{" +
                "name='" + name + '\'' +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public SpotifyExternalUrls getExternal_urls() {
        return external_urls;
    }
}
