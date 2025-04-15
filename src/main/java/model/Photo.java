/**
 * Represents a photo within an album in the Photos application.
 *
 * <p>
 * A Photo object stores the file path to an image, a caption, the date the photo was originally taken,
 * and a collection of associated tags. Additionally, it maintains a lastEdited timestamp that is updated
 * whenever the photo’s caption or tags are modified. The file path is used to retrieve and display the image.
 * </p>
 *
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filepath;
    private String caption;
    private final LocalDateTime dateTaken;
    private Set<Tag> tags;
    private LocalDateTime lastEdited;

    public Photo(String filepath, String caption, LocalDateTime dateTaken) {
        this.filepath = filepath;
        this.caption = caption;
        this.dateTaken = dateTaken;
        this.tags = new HashSet<>();
    }

    public void addTag(Tag tag) {
        // Enforce single-value restriction for certain tag types
        if ("location".equalsIgnoreCase(tag.getName())) {
            tags.removeIf(existingTag -> "location".equalsIgnoreCase(existingTag.getName())); // Lamda expression
        }
        tags.add(tag); // Add the tag (Set ensures no duplicates)
        this.lastEdited = LocalDateTime.now();
    }

    public void removeTag(Tag tag) {
        if (tags.contains(tag))
            tags.remove(tag);
        this.lastEdited = LocalDateTime.now();
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
        this.lastEdited = LocalDateTime.now();
    }

    public Set<Tag> getTags() {
        return this.tags;
    }

    public LocalDateTime getLastEdited() {
        return lastEdited;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Photo)) {
            return false;
        }
        Photo photo = (Photo) o;
        return Objects.equals(filepath, photo.getFilepath()) && Objects.equals(caption, photo.getCaption())
                && Objects.equals(dateTaken, photo.getDateTaken()) && Objects.equals(tags, photo.getTags());
    }

    @Override
    public int hashCode() {
        return Objects.hash(filepath, caption, dateTaken, tags);
    }

    @Override
    public String toString() {
        return (caption == null || caption.isEmpty()) ? "Photo: " + filepath : caption;
    }

}
