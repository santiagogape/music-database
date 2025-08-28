package app.io;

import app.model.items.FileSong;
import app.model.items.ItemImage;
import app.model.items.Response;
import app.model.items.SimpleItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileToModel {
    public FileToModel() {}


    public static ItemImage FileToItemImage(Map.Entry<SimpleItem, File> entry) {
        int height;
        int width;
        try {
            BufferedImage img = ImageIO.read(entry.getValue());
            height = img.getHeight();
            width = img.getWidth();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new ItemImage() {
            @Override
            public Integer item() {
                return entry.getKey().id();
            }

            @Override
            public int number() {
                return 0;
            }

            @Override
            public String path() {
                try {
                    return entry.getValue().getCanonicalPath();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public int width() {
                return width;
            }

            @Override
            public int height() {
                return height;
            }
        };
    }

    public static Response FileToResponse(FileSong key, File value) {
        return new Response() {
            @Override
            public Integer id() {
                return key.id();
            }

            @Override
            public String path() {
                try {
                    return value.getCanonicalPath();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public String name() {
                return value.getName();
            }
        };
    }

}
