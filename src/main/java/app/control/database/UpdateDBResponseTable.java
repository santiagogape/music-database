package app.control.database;

import app.model.items.FileSong;
import app.model.items.Response;
import app.model.utilities.database.Database;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static app.io.FileToModel.FileToResponse;

/*
after receiving an API response, the json is stored temporarily in the storage,
so the database is updated with said changes
 */
public class UpdateDBResponseTable {
    private final Database db;
    private final Database.Table<Response> responses;

    public UpdateDBResponseTable(Database db, Database.Table<Response> responses) {
        this.db = db;
        this.responses = responses;
    }

    public List<Response> updateWith(Map<FileSong, File> input){
        List<Response> list = input.entrySet()
                .stream()
                .map(pair ->
                        responses.insert(FileToResponse(pair.getKey(), pair.getValue())))
                .toList();
        db.commit();
        return list;
    }

}
