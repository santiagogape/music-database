package main;

import app.model.items.SimpleItem;
import app.model.utilities.database.Database;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class littleTest {
    private static List<SimpleItem.ItemUri> results;

    public static void main(String[] args) {
        results = new ArrayList<>();
        for (Database.ItemSource source : Database.ItemSource.values()) {
            for (SimpleItem.ItemType type: SimpleItem.ItemType.values()) {
                IntStream.range(0,10).mapToObj(i->new SimpleItem.ItemUri(i,source,type,String.valueOf(i))).forEach(results::add);
            }
        }
        System.out.println(results.stream().collect(
                Collectors.groupingBy(SimpleItem.ItemUri::source,
                        Collectors.groupingBy(
                                SimpleItem.ItemUri::type
                        ))));

        Map<Database.ItemSource, Map<SimpleItem.ItemType, List<SimpleItem.ItemUri>>> collect = results.stream().collect(
                Collectors.groupingBy(SimpleItem.ItemUri::source,
                        Collectors.groupingBy(
                                SimpleItem.ItemUri::type
                        )));

        collect.get(Database.ItemSource.spotify).get(SimpleItem.ItemType.artist).add(new SimpleItem.ItemUri(10, Database.ItemSource.spotify, SimpleItem.ItemType.artist,"10"));
        System.out.println(collect.get(Database.ItemSource.spotify).get(SimpleItem.ItemType.artist));
    }
}
