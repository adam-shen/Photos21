
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Photo {

    private final String filepath;
    private String caption;
    private final LocalDateTime dateTaken;
    private Set<Tag> tags;

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
    }

    public void removeTag(Tag tag) {
        if (tags.contains(tag))
            tags.remove(tag);
    }

    public LocalDateTime getDateTaken() {
        return dateTaken;
    }

    public String getFilepath() {
        return this.filepath;
    }

    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Set<Tag> getTags() {
        return this.tags;
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

}
