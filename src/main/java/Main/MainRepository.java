package Main;

import app.model.repositories.FilesResponsesRepository;
import app.model.repositories.GenresRepository;
import app.model.repositories.ImagesRepository;
import app.model.repositories.ItemsRepository;

import java.util.List;

public class MainRepository {

    private FilesResponsesRepository filesResponsesRepository;
    private ItemsRepository itemsRepository;
    private GenresRepository genresRepository;
    private ImagesRepository imagesRepository;

    public MainRepository() {}


    public ImagesRepository getImagesRepository() {
        return imagesRepository;
    }

    public void setImagesRepository(ImagesRepository imagesRepository) {
        this.imagesRepository = imagesRepository;
    }

    private List<String> directories;

    public FilesResponsesRepository getFilesResponsesRepository() {
        return filesResponsesRepository;
    }

    public ItemsRepository getItemsRepository() {
        return itemsRepository;
    }

    public GenresRepository getGenresRepository() {
        return genresRepository;
    }

    public void setFilesResponsesRepository(FilesResponsesRepository filesResponsesRepository) {
        this.filesResponsesRepository = filesResponsesRepository;
    }

    public void setItemsRepository(ItemsRepository itemsRepository) {
        this.itemsRepository = itemsRepository;
    }

    public void setGenresRepository(GenresRepository genresRepository) {
        this.genresRepository = genresRepository;
    }

    public List<String> getDirectories() {
        return directories;
    }

    public void setDirectories(List<String> directories) {
        this.directories = directories;
    }


}
