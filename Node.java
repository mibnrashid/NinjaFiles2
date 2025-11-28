/**
 * Abstract base class representing a node in the NinjaFiles file system.
 * Both files and directories are nodes in the tree structure.
 */
public abstract class Node {
    // The name of this node (file or directory)
    protected String name;
    
    // The parent directory that contains this node
    protected Directory parent;
    
    /**
     * Constructor for Node.
     * @param name The name of the node
     * @param parent The parent directory (null for root directory)
     */
    public Node(String name, Directory parent) {
        this.name = name;
        this.parent = parent;
    }
    
    /**
     * Gets the name of this node.
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the parent directory of this node.
     * @return The parent directory (null if this is the root)
     */
    public Directory getParent() {
        return parent;
    }
    
    /**
     * Sets the parent directory of this node.
     * @param parent The new parent directory
     */
    public void setParent(Directory parent) {
        this.parent = parent;
    }
    
    /**
     * Abstract method to get the size of this node.
     * Files return their size, directories return the sum of their children's sizes.
     * @return The size of the node
     */
    public abstract int getSize();
    
    /**
     * Checks if this node is a directory.
     * @return false by default (overridden in Directory class)
     */
    public boolean isDirectory() {
        return false;
    }
}

