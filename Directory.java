import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a directory in the NinjaFiles file system.
 * Directories can contain other files and directories as children.
 * Uses a HashMap for hash-table-based lookup of children by name.
 */
public class Directory extends Node {
    // Hash table storing children: key = child name (String), value = Node
    // This is the hash-table-based lookup required by the project
    private HashMap<String, Node> children;
    
    /**
     * Constructor for Directory.
     * @param name The name of the directory
     * @param parent The parent directory (null for root directory)
     */
    public Directory(String name, Directory parent) {
        super(name, parent);
        this.children = new HashMap<String, Node>();
    }
    
    /**
     * Overrides isDirectory() to return true for directories.
     * @return true
     */
    @Override
    public boolean isDirectory() {
        return true;
    }
    
    /**
     * Gets a child node by name using hash-table lookup.
     * This provides O(1) average-case access time, which is the hash-table-based
     * lookup required by the project specification.
     * @param name The name of the child to retrieve
     * @return The child node if found, null otherwise
     */
    public Node getChild(String name) {
        return children.get(name);
    }
    
    /**
     * Adds a child node to this directory using hash-table storage.
     * The child is stored in the HashMap with its name as the key, enabling
     * fast O(1) average-case lookups by name.
     * @param child The child node to add
     */
    public void addChild(Node child) {
        children.put(child.getName(), child);
        child.setParent(this);
    }
    
    /**
     * Removes a child node from this directory by name.
     * @param name The name of the child to remove
     * @return The removed node if found, null otherwise
     */
    public Node removeChild(String name) {
        Node removed = children.remove(name);
        if (removed != null) {
            removed.setParent(null);
        }
        return removed;
    }
    
    /**
     * Gets all children of this directory.
     * @return A collection of all child nodes
     */
    public Collection<Node> getChildren() {
        return children.values();
    }
    
    /**
     * Calculates the total size of this directory recursively.
     * Sums the sizes of all children (files and subdirectories).
     * @return The total size of the directory
     */
    @Override
    public int getSize() {
        int totalSize = 0;
        for (Node child : children.values()) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
}

