import java.util.Scanner;

/**
 * Main entry point for the NinjaFiles file system simulator.
 * Provides a command-line interface for interacting with the in-memory file system.
 */
public class NinjaFiles {
    /**
     * Main method that runs the NinjaFiles simulator.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create the file system instance
        FileSystem fs = new FileSystem();
        
        // Scanner for reading user input
        Scanner scanner = new Scanner(System.in);
        
        // Main command loop
        while (true) {
            // Print prompt: current path followed by $
            System.out.print(fs.getCurrentPath() + "$ ");
            
            // Read a full line from user input
            String input = scanner.nextLine().trim();
            
            // Skip empty lines
            if (input.isEmpty()) {
                continue;
            }
            
            // Parse command and arguments (simple split for now)
            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();
            
            // Handle commands
            switch (command) {
                case "mkdir":
                    if (parts.length > 1) {
                        // Extract all arguments (including -p and directory names)
                        String[] mkdirArgs = new String[parts.length - 1];
                        System.arraycopy(parts, 1, mkdirArgs, 0, mkdirArgs.length);
                        fs.mkdir(mkdirArgs);
                    }
                    break;
                    
                case "touch":
                    if (parts.length >= 2) {
                        String fileName = parts[1];
                        int size = 0;
                        // Check if size is provided
                        if (parts.length > 2) {
                            try {
                                size = Integer.parseInt(parts[2]);
                            } catch (NumberFormatException e) {
                                size = 0;
                            }
                        }
                        fs.touch(fileName, size);
                    }
                    break;
                    
                case "echo":
                    // Parse: echo "content" > filename
                    // Extract quoted content and file path
                    String line = input.substring(4).trim(); // Skip "echo"
                    
                    // Find the first quote
                    int firstQuote = line.indexOf('"');
                    if (firstQuote == -1) {
                        break; // No quote found
                    }
                    
                    // Find the matching closing quote
                    int secondQuote = line.indexOf('"', firstQuote + 1);
                    if (secondQuote == -1) {
                        break; // No closing quote
                    }
                    
                    // Extract content between quotes
                    String content = line.substring(firstQuote + 1, secondQuote);
                    
                    // Find the > symbol after the quotes
                    String afterQuotes = line.substring(secondQuote + 1).trim();
                    if (!afterQuotes.startsWith(">")) {
                        break; // Invalid format
                    }
                    
                    // Extract file path after >
                    String filePath = afterQuotes.substring(1).trim();
                    if (filePath.isEmpty()) {
                        break; // No file path
                    }
                    
                    fs.echo(content, filePath);
                    break;
                    
                case "ls":
                    if (parts.length > 1) {
                        fs.ls(parts[1]);
                    } else {
                        fs.ls(null);
                    }
                    break;
                    
                case "cd":
                    if (parts.length > 1) {
                        fs.cd(parts[1]);
                    } else {
                        fs.cd("/");
                    }
                    break;
                    
                case "pwd":
                    fs.pwd();
                    break;
                    
                case "rm":
                    if (parts.length > 1) {
                        // Check for -r flag
                        if (parts.length > 2 && parts[1].equals("-r")) {
                            fs.rmRecursive(parts[2]);
                        } else {
                            fs.rm(parts[1]);
                        }
                    } else {
                        System.out.println("Usage: rm <file_name> or rm -r <directory_name>");
                    }
                    break;
                    
                case "tree":
                    if (parts.length > 1) {
                        fs.tree(parts[1]);
                    } else {
                        fs.tree(null);
                    }
                    break;
                    
                case "grep":
                    // Parse: grep "pattern" file_path
                    // Extract quoted pattern and file path
                    String grepLine = input.substring(4).trim(); // Skip "grep"
                    
                    // Find the first quote
                    int grepFirstQuote = grepLine.indexOf('"');
                    if (grepFirstQuote == -1) {
                        break; // No quote found
                    }
                    
                    // Find the matching closing quote
                    int grepSecondQuote = grepLine.indexOf('"', grepFirstQuote + 1);
                    if (grepSecondQuote == -1) {
                        break; // No closing quote
                    }
                    
                    // Extract pattern between quotes
                    String grepPattern = grepLine.substring(grepFirstQuote + 1, grepSecondQuote);
                    
                    // Extract file path after the quotes
                    String grepFilePath = grepLine.substring(grepSecondQuote + 1).trim();
                    if (grepFilePath.isEmpty()) {
                        break; // No file path
                    }
                    
                    fs.grep(grepPattern, grepFilePath);
                    break;
                    
                case "du":
                    if (parts.length > 1) {
                        fs.du(parts[1]);
                    } else {
                        fs.du(null);
                    }
                    break;
                    
                case "exit":
                case "quit":
                    System.out.println("Exiting NinjaFiles. Goodbye!");
                    scanner.close();
                    return;
                    
                default:
                    System.out.println("Unknown command: " + command);
                    System.out.println("Available commands: mkdir, touch, echo, ls, cd, pwd, rm, tree, grep, du, exit");
                    break;
            }
        }
    }
}

