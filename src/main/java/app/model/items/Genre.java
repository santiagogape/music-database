package app.model.items;

public record Genre(String name) {
    public record ItemGenre(Integer item, Genre genre){}
}
