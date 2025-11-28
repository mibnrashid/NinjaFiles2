/**
 * Represents a file in the NinjaFiles file system.
 * Files have a size and can contain text content.
 */
public class File extends Node {
    // The size of the file in bytes
    private int size;
    
    // The text content stored in the file
    private String content;
    
    /**
     * Constructor for File that takes size explicitly.
     * @param name The name of the file
     * @param parent The parent directory containing this file
     * @param size The size of the file
     */
    public File(String name, Directory parent, int size) {
        super(name, parent);
        this.size = size;
        this.content = "";
    }
    
    /**
     * Constructor for File that takes content and calculates size.
     * @param name The name of the file
     * @param parent The parent directory containing this file
     * @param content The text content of the file
     */
    public File(String name, Directory parent, String content) {
        super(name, parent);
        this.content = content;
        this.size = content.length();
    }
    
    /**
     * Gets the size of the file.
     * @return The size in bytes
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Sets the size of the file.
     * @param size The new size
     */
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * Gets the content of the file.
     * @return The text content
     */
    public String getContent() {
        return content;
    }
    
    /**
     * Sets the content of the file and updates the size.
     * @param content The new content
     */
    public void setContent(String content) {
        this.content = content;
        this.size = content.length();
    }
}

