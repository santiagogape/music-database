package app.control.items;

import app.model.items.FileSong;
import app.model.items.Response;

import java.time.LocalDateTime;

import static app.model.items.SimpleItem.ItemType.response;
import static java.time.LocalDateTime.now;

public class ResponseCreator {
    public ResponseCreator() {}
    public Response createFrom(FileSong fileSong){
        return new Response(){
            @Override
            public Status status() {
                return Status.not_checked;
            }

            @Override
            public String directory() {
                return "responses";
            }

            @Override
            public LocalDateTime creation() {
                return now();
            }

            @Override
            public String nameWithExtension() {
                return name()+".json";
            }

            @Override
            public Integer id() {
                return fileSong.id();
            }

            @Override
            public String name() {
                return fileSong.name();
            }

            @Override
            public ItemType type() {
                return response;
            }
        };
    }
}
