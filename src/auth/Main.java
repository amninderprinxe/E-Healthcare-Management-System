package auth;

/**
 * Main entry point for the application.
 * This separate Main class is necessary for JavaFX when building fat JARs.
 */
public class Main {
    public static void main(String[] args) {
        // Calls the main method of your JavaFX Application class
        LoginWindow.main(args);
    }
}