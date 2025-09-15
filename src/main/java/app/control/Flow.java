package app.control;

public enum Flow {
    NO_RESPONSE("0: files which haven't been requested"),
    NOT_CHECKED("1: files whose response haven't been checked"),
    NOT_CONTAINED("2: files whose response doesn't contain the track searched"),
    CONTAINED_NOT_ADDED("3: files whose response has been checked but haven't been added");

    public String description() {
        return description;
    }

    public final String description;

    Flow(String description) {
        this.description = description;
    }
}
