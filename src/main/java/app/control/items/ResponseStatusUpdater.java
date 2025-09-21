package app.control.items;

import app.model.items.Response;

import java.time.LocalDateTime;

public class ResponseStatusUpdater {


    public ResponseStatusUpdater() {}

    public Response updateStatus(Response response, Response.Status newStatus){
        return new Response() {
            @Override
            public Status status() {
                return newStatus;
            }

            @Override
            public String directory() {
                return response.directory();
            }

            @Override
            public LocalDateTime creation() {
                return response.creation();
            }

            @Override
            public Integer id() {
                return response.id();
            }

            @Override
            public String name() {
                return response.name();
            }

            @Override
            public String nameWithExtension() {
                return response.nameWithExtension();
            }

            @Override
            public ItemType type() {
                return response.type();
            }
        };
    }
}
