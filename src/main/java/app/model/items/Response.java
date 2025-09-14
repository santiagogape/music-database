package app.model.items;

public interface Response extends FileReference {
    Status status();

    enum Status {
        not_checked, checked_contained, checked_not_contained, added
    }
}
