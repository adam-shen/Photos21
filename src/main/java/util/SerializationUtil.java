/**
 * Utility class for handling serialization and deserialization of objects.
 *
 * <p>
 * This class provides static methods to save any serializable object
 * to a file and load it back from a file using Java's built-in serialization
 * mechanism. The save method writes the object to the specified file path,
 * while the load method reads and returns the deserialized object from the given file.
 * </p>
 *
 * @author Adam Student
 * @author Neer Patel
 * @version 1.0
 */
package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Utility class for handling serialization and deserialization of objects.
 * Provides methods to save and load objects to/from files.
 */
public class SerializationUtil {

    /**
     * Saves an object to the specified file.
     *
     * @param obj      the object to save (must implement Serializable)
     * @param filePath the file path where the object will be saved
     */
    public static void save(Object obj, String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
            System.out.println("Data successfully saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving data to " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads an object from the specified file.
     *
     * @param <T>      the type of the object to load
     * @param filePath the file path from which the object will be loaded
     * @return the deserialized object, or null if an error occurs or the file does
     *         not exist
     */
    public static <T> T load(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("No saved data found at " + filePath);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            T obj = (T) ois.readObject();
            System.out.println("Data successfully loaded from " + filePath);
            return obj;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data from " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
