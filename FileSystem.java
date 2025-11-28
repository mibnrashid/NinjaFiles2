import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Core class that powers the NinjaFiles file system simulator.
 * Manages the root directory, current working directory, and all file system operations.
 */
public class FileSystem {
    // The root directory of the file system (always "/")
    private Directory root;
    
    // The current working directory
    private Directory current;
    
    /**
     * Constructor for FileSystem.
     * Creates the root directory "/" with parent = null and sets it as current.
     */
    public FileSystem() {
        this.root = new Directory("/", null);
        this.current = root;
    }
    
    /**
     * Gets the root directory.
     * @return The root directory
     */
    public Directory getRoot() {
        return root;
    }
    
    /**
     * Gets the current working directory.
     * @return The current directory
     */
    public Directory getCurrent() {
        return current;
    }
    
    /**
     * Sets the current working directory.
     * @param current The new current directory
     */
    public void setCurrent(Directory current) {
        this.current = current;
    }
    
    /**
     * Builds and returns the absolute path of the current directory.
     * Uses parent links to traverse up to the root.
     * @return The absolute path as a string (e.g., "/", "/home", "/a/b/c")
     */
    public String getCurrentPath() {
        if (current == root) {
            return "/";
        }
        
        // Build path by traversing up the parent chain
        StringBuilder path = new StringBuilder();
        buildPath(current, path);
        return path.toString();
    }
    
    /**
     * Helper method to recursively build the path from a node to root.
     * @param node The current node
     * @param path The StringBuilder to append to
     */
    private void buildPath(Node node, StringBuilder path) {
        if (node.getParent() == null) {
            // Reached root
            path.insert(0, "/");
        } else {
            // Insert this node's name at the beginning
            path.insert(0, node.getName());
            path.insert(0, "/");
            buildPath(node.getParent(), path);
        }
    }
    
    // ========== Path Resolution Helper Methods ==========
    
    /**
     * Resolves a path string to a Directory node.
     * Uses hash-table-based lookups in each directory for O(1) average access.
     * @param path The path to resolve (absolute starting with /, or relative)
     * @return The Directory if found, null otherwise
     */
    private Directory resolveDirectory(String path) {
        Node node = resolveNode(path);
        if (node != null && node.isDirectory()) {
            return (Directory) node;
        }
        return null;
    }
    
    /**
     * Resolves a path string to a Node (file or directory).
     * Uses hash-table-based lookups in each directory for O(1) average access.
     * @param path The path to resolve (absolute starting with /, or relative)
     * @return The Node if found, null otherwise
     */
    private Node resolveNode(String path) {
        if (path == null || path.isEmpty()) {
            return current;
        }
        
        // Handle special cases
        if (path.equals("/")) {
            return root;
        }
        if (path.equals(".")) {
            return current;
        }
        if (path.equals("..")) {
            return current.getParent() != null ? current.getParent() : root;
        }
        
        // Start from root if absolute path, otherwise from current
        Directory startDir = path.startsWith("/") ? root : current;
        
        // Split path into components
        String[] components = path.split("/");
        Node current = startDir;
        
        // Skip empty first component if path starts with /
        int startIndex = path.startsWith("/") ? 1 : 0;
        
        for (int i = startIndex; i < components.length; i++) {
            String component = components[i];
            
            // Skip empty components
            if (component.isEmpty()) {
                continue;
            }
            
            // Handle . and .. in path
            if (component.equals(".")) {
                continue;
            }
            if (component.equals("..")) {
                if (current.isDirectory()) {
                    Directory dir = (Directory) current;
                    current = dir.getParent() != null ? dir.getParent() : root;
                } else {
                    return null; // Can't go up from a file
                }
                continue;
            }
            
            // Use hash table lookup in current directory
            if (current.isDirectory()) {
                Directory dir = (Directory) current;
                Node child = dir.getChild(component);
                if (child == null) {
                    return null; // Path component not found
                }
                current = child;
            } else {
                return null; // Trying to traverse into a file
            }
        }
        
        return current;
    }
    
    /**
     * Resolves the parent directory of a given path.
     * @param path The path whose parent directory to find
     * @return The parent Directory, or null if path is invalid
     */
    private Directory resolveParentDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return current;
        }
        
        // Handle root case
        if (path.equals("/")) {
            return null; // Root has no parent
        }
        
        // Find the last slash
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            // No slash, parent is current directory
            return current;
        }
        
        if (lastSlash == 0) {
            // Path is like "/something", parent is root
            return root;
        }
        
        // Get parent path
        String parentPath = path.substring(0, lastSlash);
        if (parentPath.isEmpty()) {
            return root;
        }
        
        return resolveDirectory(parentPath);
    }
    
    // ========== Command Methods ==========
    
    /**
     * Creates new directories. Supports multiple directory names and -p flag.
     * @param args Array of arguments (may include -p flag and directory names/paths)
     */
    public void mkdir(String[] args) {
        if (args.length == 0) {
            return;
        }
        
        boolean createParents = false;
        int startIndex = 0;
        
        // Check for -p flag
        if (args[0].equals("-p")) {
            createParents = true;
            startIndex = 1;
        }
        
        // Process each directory name/path
        for (int i = startIndex; i < args.length; i++) {
            String dirPath = args[i];
            
            if (createParents) {
                // Create path with parent directories
                createDirectoryPath(dirPath);
            } else {
                // Create directory directly in current directory
                createDirectory(dirPath, current);
            }
        }
    }
    
    /**
     * Helper method to create a single directory in a parent directory.
     * @param name The name of the directory to create
     * @param parent The parent directory
     */
    private void createDirectory(String name, Directory parent) {
        // Check if already exists using hash table lookup
        if (parent.getChild(name) != null) {
            System.out.println("Error: '" + name + "' already exists.");
            return;
        }
        
        Directory newDir = new Directory(name, parent);
        parent.addChild(newDir);
    }
    
    /**
     * Helper method to create a directory path, creating parent directories as needed.
     * @param path The full path to create
     */
    private void createDirectoryPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        
        // Determine start directory
        Directory startDir = path.startsWith("/") ? root : current;
        
        // Split path into components
        String[] components = path.split("/");
        int startIndex = path.startsWith("/") ? 1 : 0;
        
        Directory currentDir = startDir;
        
        for (int i = startIndex; i < components.length; i++) {
            String component = components[i];
            if (component.isEmpty()) {
                continue;
            }
            
            // Check if component exists using hash table lookup
            Node existing = currentDir.getChild(component);
            
            if (existing == null) {
                // Create the directory
                Directory newDir = new Directory(component, currentDir);
                currentDir.addChild(newDir);
                currentDir = newDir;
            } else if (existing.isDirectory()) {
                // Directory already exists, continue
                currentDir = (Directory) existing;
            } else {
                // A file with this name exists
                System.out.println("Error: '" + component + "' already exists.");
                return;
            }
        }
    }
    
    /**
     * Creates a new empty file with the given size.
     * @param name The name of the file to create
     * @param size The size of the file
     */
    public void touch(String name, int size) {
        // Check if already exists using hash table lookup
        Node existing = current.getChild(name);
        
        if (existing == null) {
            // Create new file
            File newFile = new File(name, current, size);
            current.addChild(newFile);
        } else if (existing.isDirectory()) {
            System.out.println("Error: '" + name + "' is a directory.");
        } else {
            // File exists, update its size and clear content
            File file = (File) existing;
            file.setSize(size);
            file.setContent("");
        }
    }
    
    /**
     * Writes content to a file (creates if doesn't exist).
     * @param content The content to write
     * @param filePath The path to the file (can be relative or absolute)
     */
    public void echo(String content, String filePath) {
        // Resolve the parent directory and filename
        Directory parentDir;
        String fileName;
        
        if (filePath.contains("/")) {
            // Path contains slashes, need to resolve parent
            parentDir = resolveParentDirectory(filePath);
            if (parentDir == null) {
                System.out.println("Error: Path '" + filePath + "' not found.");
                return;
            }
            fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        } else {
            // Simple filename in current directory
            parentDir = current;
            fileName = filePath;
        }
        
        // Check if file exists using hash table lookup
        Node existing = parentDir.getChild(fileName);
        
        if (existing == null) {
            // Create new file with content
            File newFile = new File(fileName, parentDir, content);
            parentDir.addChild(newFile);
        } else if (existing.isDirectory()) {
            System.out.println("Error: '" + fileName + "' is a directory.");
        } else {
            // File exists, update content
            File file = (File) existing;
            file.setContent(content);
        }
    }
    
    /**
     * Lists the contents of the current directory or a specified directory.
     * @param path Optional path to list (null for current directory)
     */
    public void ls(String path) {
        Directory targetDir;
        
        if (path == null) {
            targetDir = current;
        } else {
            Node node = resolveNode(path);
            if (node == null) {
                System.out.println("Error: Path '" + path + "' not found.");
                return;
            }
            if (!node.isDirectory()) {
                System.out.println("Error: '" + path + "' is not a directory.");
                return;
            }
            targetDir = (Directory) node;
        }
        
        // Get all children and sort by name
        List<Node> children = new ArrayList<Node>(targetDir.getChildren());
        Collections.sort(children, (a, b) -> a.getName().compareTo(b.getName()));
        
        // Format output: directories as name/, files as name (sizeB)
        if (children.isEmpty()) {
            // Empty directory - no output (just next prompt)
            return;
        }
        
        StringBuilder output = new StringBuilder();
        for (Node child : children) {
            if (output.length() > 0) {
                output.append(" ");
            }
            if (child.isDirectory()) {
                output.append(child.getName()).append("/");
            } else {
                File file = (File) child;
                output.append(child.getName()).append(" (").append(file.getSize()).append("B)");
            }
        }
        System.out.println(output.toString());
    }
    
    /**
     * Changes the current working directory.
     * @param path The path to change to
     */
    public void cd(String path) {
        if (path == null || path.isEmpty()) {
            current = root;
            return;
        }
        
        // Handle special case: cd . means go up one level (per spec samples)
        if (path.equals(".")) {
            if (current.getParent() != null) {
                current = current.getParent();
            }
            return;
        }
        
        // Resolve the path
        Node target = resolveNode(path);
        
        if (target == null) {
            System.out.println("Error: Path '" + path + "' not found.");
            return;
        }
        
        if (!target.isDirectory()) {
            System.out.println("Error: '" + path + "' is not a directory.");
            return;
        }
        
        current = (Directory) target;
    }
    
    /**
     * Prints the current working directory path.
     */
    public void pwd() {
        System.out.println(getCurrentPath());
    }
    
    /**
     * Removes a file or empty directory from the current directory.
     * @param name The name of the file/directory to remove
     */
    public void rm(String name) {
        // Check if exists using hash table lookup
        Node target = current.getChild(name);
        
        if (target == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (target.isDirectory()) {
            Directory dir = (Directory) target;
            // Check if directory is empty
            if (!dir.getChildren().isEmpty()) {
                System.out.println("Error: Cannot remove directory '" + name + "'. It is not empty.");
                return;
            }
        }
        
        // Remove from hash table
        current.removeChild(name);
    }
    
    /**
     * Recursively removes a directory and all its contents.
     * @param name The name of the directory to remove
     */
    public void rmRecursive(String name) {
        // Check if exists using hash table lookup
        Node target = current.getChild(name);
        
        if (target == null) {
            System.out.println("Error: '" + name + "' not found.");
            return;
        }
        
        if (!target.isDirectory()) {
            // Not a directory, use regular rm
            rm(name);
            return;
        }
        
        // Recursively remove directory and all contents
        removeDirectoryRecursive((Directory) target);
        
        // Remove from parent's hash table
        current.removeChild(name);
    }
    
    /**
     * Helper method to recursively remove a directory and all its contents.
     * Uses hash-table-based lookups to access children.
     * @param dir The directory to remove
     */
    private void removeDirectoryRecursive(Directory dir) {
        // Recursively remove all children
        List<Node> children = new ArrayList<Node>(dir.getChildren());
        for (Node child : children) {
            if (child.isDirectory()) {
                removeDirectoryRecursive((Directory) child);
            }
            // Remove child from hash table
            dir.removeChild(child.getName());
        }
    }
    
    /**
     * Displays the directory tree structure.
     * Uses recursion to traverse the directory tree and hash-table lookups to access children.
     * @param path Optional path to display tree for (null for current directory)
     */
    public void tree(String path) {
        Directory targetDir;
        
        if (path == null) {
            targetDir = current;
        } else {
            Node node = resolveNode(path);
            if (node == null) {
                System.out.println("Error: Path '" + path + "' not found.");
                return;
            }
            if (!node.isDirectory()) {
                System.out.println("Error: '" + path + "' is not a directory.");
                return;
            }
            targetDir = (Directory) node;
        }
        
        // Print . representing the current directory
        System.out.println(".");
        
        // Recursively print the tree structure
        // Get children from hash table and sort for stable order
        List<Node> children = new ArrayList<Node>(targetDir.getChildren());
        Collections.sort(children, (a, b) -> a.getName().compareTo(b.getName()));
        
        // Print each child with proper tree formatting
        for (int i = 0; i < children.size(); i++) {
            boolean isLast = (i == children.size() - 1);
            printTree(children.get(i), "", isLast);
        }
    }
    
    /**
     * Recursive helper method to print the tree structure.
     * Uses hash-table-based lookups in each directory to access children efficiently.
     * @param node The current node to print
     * @param prefix The prefix string for tree formatting (├── or └──)
     * @param isLast Whether this is the last child in its parent
     */
    private void printTree(Node node, String prefix, boolean isLast) {
        // Print the current node with appropriate formatting
        String connector = isLast ? "└── " : "├── ";
        System.out.print(prefix + connector);
        
        // Print node name with appropriate suffix
        if (node.isDirectory()) {
            System.out.println(node.getName() + "/");
        } else {
            File file = (File) node;
            System.out.println(node.getName() + " (" + file.getSize() + "B)");
        }
        
        // If it's a directory, recursively print its children
        if (node.isDirectory()) {
            Directory dir = (Directory) node;
            List<Node> children = new ArrayList<Node>(dir.getChildren());
            Collections.sort(children, (a, b) -> a.getName().compareTo(b.getName()));
            
            // Build prefix for children (│ for continuation, spaces for last)
            String childPrefix = prefix + (isLast ? "    " : "│   ");
            
            for (int i = 0; i < children.size(); i++) {
                boolean childIsLast = (i == children.size() - 1);
                printTree(children.get(i), childPrefix, childIsLast);
            }
        }
    }
    
    /**
     * Searches for a pattern in file contents using the KMP (Knuth-Morris-Pratt) algorithm.
     * Uses hash-table-based path resolution to locate the file.
     * @param pattern The pattern to search for
     * @param filePath The path to the file to search in
     */
    public void grep(String pattern, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        // Resolve the file path using hash-table lookups
        Node node = resolveNode(filePath);
        
        if (node == null) {
            System.out.println("Error: '" + filePath + "' not found.");
            return;
        }
        
        if (node.isDirectory()) {
            System.out.println("Error: '" + filePath + "' is not a file.");
            return;
        }
        
        // Get file content
        File file = (File) node;
        String content = file.getContent();
        
        // Use KMP algorithm to search for pattern
        boolean found = kmpSearch(content, pattern);
        
        if (found) {
            System.out.println("Pattern \"" + pattern + "\" found in " + filePath + ".");
        } else {
            System.out.println("Pattern \"" + pattern + "\" not found in " + filePath + ".");
        }
    }
    
    /**
     * Builds the LPS (Longest Proper Prefix which is also Suffix) array for KMP algorithm.
     * The LPS array helps skip unnecessary comparisons during pattern matching.
     * @param pattern The pattern string to build LPS for
     * @return The LPS array
     */
    private int[] buildLPS(String pattern) {
        int m = pattern.length();
        int[] lps = new int[m];
        int len = 0; // Length of the previous longest prefix suffix
        int i = 1;
        
        lps[0] = 0; // LPS[0] is always 0
        
        // Build LPS array by comparing characters
        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    // Don't match lps[len-1] characters, they will match anyway
                    len = lps[len - 1];
                } else {
                    lps[i] = 0;
                    i++;
                }
            }
        }
        
        return lps;
    }
    
    /**
     * KMP (Knuth-Morris-Pratt) string matching algorithm.
     * Uses the LPS array to perform linear-time pattern matching without backtracking.
     * Algorithm: Build LPS array, then scan text once while using LPS to skip comparisons.
     * @param text The text to search in
     * @param pattern The pattern to search for
     * @return true if pattern is found in text, false otherwise
     */
    private boolean kmpSearch(String text, String pattern) {
        if (pattern.isEmpty()) {
            return false;
        }
        
        int n = text.length();
        int m = pattern.length();
        
        // Build LPS array for the pattern
        int[] lps = buildLPS(pattern);
        
        int i = 0; // Index for text
        int j = 0; // Index for pattern
        
        // Linear scan through text
        while (i < n) {
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
            }
            
            // Pattern found
            if (j == m) {
                return true;
            }
            
            // Mismatch after j matches
            if (i < n && text.charAt(i) != pattern.charAt(j)) {
                if (j != 0) {
                    // Use LPS to skip unnecessary comparisons
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Displays disk usage (size) of directories.
     * Uses recursion to compute total size of all files in the subtree.
     * @param path Optional path to check (null for current directory)
     */
    public void du(String path) {
        Directory targetDir;
        
        if (path == null) {
            targetDir = current;
        } else {
            Node node = resolveNode(path);
            if (node == null) {
                System.out.println("Error: Path '" + path + "' not found.");
                return;
            }
            if (!node.isDirectory()) {
                System.out.println("Error: '" + path + "' is not a directory.");
                return;
            }
            targetDir = (Directory) node;
        }
        
        // Recursively compute total size using hash-table lookups
        int totalSize = computeSize(targetDir);
        System.out.println("Total size: " + totalSize + "B");
    }
    
    /**
     * Recursively computes the total size of all files in a directory subtree.
     * Uses hash-table-based lookups to access children efficiently.
     * Recursion: For each child, if it's a file, add its size; if it's a directory, recursively compute its size.
     * @param dir The directory to compute size for
     * @return The total size in bytes
     */
    private int computeSize(Directory dir) {
        int totalSize = 0;
        
        // Iterate through all children using hash table
        for (Node child : dir.getChildren()) {
            if (child.isDirectory()) {
                // Recursively compute size of subdirectory
                totalSize += computeSize((Directory) child);
            } else {
                // Add file size directly
                totalSize += child.getSize();
            }
        }
        
        return totalSize;
    }
}

