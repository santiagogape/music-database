package app.model.items;

import java.time.*;
import java.time.format.DateTimeFormatter;

public interface Album extends SimpleItem {
    AlbumType albumType();
    int tracks();
    LocalDateTime release();
    ReleasePrecision precision();
    String label();

    enum AlbumType {
        single, album, compilation
    }

    enum ReleasePrecision {
        year, month, day;

        public static LocalDateTime toLocalDateTime(ReleasePrecision precision, String string){
            return switch (precision){
                case year -> LocalDateTime.of(Year.parse(string, DateTimeFormatter.ofPattern("yyyy")).getValue(), 1, 1, 0, 0);
                case month -> {
                    YearMonth parse = YearMonth.parse(string, DateTimeFormatter.ofPattern("yyyy-MM"));
                    yield LocalDateTime.of(parse.getYear(), parse.getMonthValue(),1,0,0);
                }
                case day -> {
                    LocalDate parse = LocalDate.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    yield LocalDateTime.of(parse, LocalTime.of(0,0));
                }
            };
        }
    }

    record AlbumArtist(Integer albumId, Integer artistsId){}
}
