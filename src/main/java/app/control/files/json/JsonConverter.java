package app.control.files.json;

public interface JsonConverter {
    <T> T fromJson(String value, Class<T> tClass);
    <T> String toJson(T value);
}
