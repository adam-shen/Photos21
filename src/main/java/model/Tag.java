/**
 * Represents a tag associated with a photo.
 *
 * <p>
 * A Tag is defined by a name and a corresponding value (for example, "location"="Paris" or "person"="Alice").
 * Two Tag objects are considered equal if both their names and values match (ignoring case differences).
 * This class implements Serializable to allow tags to be persisted as part of the photo data.
 * </p>
 *
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
package model;

import java.io.Serializable;
import java.util.Objects;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) o;
        return name.equalsIgnoreCase(tag.getName()) && value.equalsIgnoreCase(tag.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase(), value.toLowerCase());
    }

}