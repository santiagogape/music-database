package dependencies.view.JavaFX.controllers.database;

import app.model.items.FileSong;
import app.model.utilities.database.Database;
import app.view.controllers.TableController;

import java.util.List;

public class FilesTableViewController implements TableController<FileSong> {


    private Database.Table<FileSong> table;

    @Override
    public void refresh() {

    }

    @Override
    public void add(List<FileSong> data) {

    }

    @Override
    public void delete(FileSong data) {

    }

    @Override
    public void all(List<FileSong> data) {

    }

    @Override
    public void setTable(Database.Table<FileSong> table) {
        this.table = table;
    }
}
