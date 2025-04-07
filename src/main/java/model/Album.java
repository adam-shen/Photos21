package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private ArrayList<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public void addPhoto(Photo p) {

        if (!photos.contains(p))
            photos.add(p);

    }

    public void deletePhoto(Photo p) {

        photos.remove(p);

    }

    public void renameAlbum(String newName) {
        name = newName;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Photo> getPhotos() {
        return photos;
    }

    public String getDateRange() {

        if (photos.isEmpty()) {
            return "No photos";
        }
        LocalDateTime earliest = photos.get(0).getDateTaken();
        LocalDateTime latest = photos.get(0).getDateTaken();

        for (Photo photo : photos) {
            LocalDateTime date = photo.getDateTaken();
            if (date.isBefore(earliest)) {
                earliest = date;
            }
            if (date.isAfter(latest)) {
                latest = date;
            }
        }

        return "From " + earliest.toString() + " to " + latest.toString();
    }

}