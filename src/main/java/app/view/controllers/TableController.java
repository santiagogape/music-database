package app.view.controllers;

import app.model.utilities.database.Database;

import java.util.List;

public interface TableController<T> extends RefreshableView {
    void add(List<T> data);
    void delete(T data);
    void all(List<T> data);
    void setTable(Database.Table<T> table);
}
