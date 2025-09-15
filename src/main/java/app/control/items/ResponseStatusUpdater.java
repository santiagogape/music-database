package app.control.items;

import app.model.items.Response;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ResponseStatusUpdater {

    private final Map<Response.Status, Function<Response, Response>> updates;

    public ResponseStatusUpdater() {
        updates = new HashMap<>();
        updates.put(Response.Status.checked_contained, this::markAsCheckedContained);
        updates.put(Response.Status.checked_not_contained, this::markAsCheckedNotContained);
        updates.put(Response.Status.added, this::markAsAdded);
        updates.put(Response.Status.tagged, this::markAsTagged);
    }

    private Response markAsCheckedContained(Response response) {
        return new Response() {
            @Override
            public Status status() {
                return Status.checked_contained;
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

    private Response markAsCheckedNotContained(Response response){
        return new Response() {
            @Override
            public Status status() {
                return Status.checked_not_contained;
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

    private Response markAsAdded(Response response) {
        return new Response() {
            @Override
            public Status status() {
                return Status.added;
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

    private Response markAsTagged(Response response){
        return new Response() {
            @Override
            public Status status() {
                return Status.tagged;
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

    public Response updateStatus(Response.Status newStatus, Response response){
        return updates.get(newStatus).apply(response);
    }
}
