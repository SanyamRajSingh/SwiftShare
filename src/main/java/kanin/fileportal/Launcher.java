package kanin.fileportal;

/**
 * Launcher class acts as the true entry point for the JavaFX application.
 * Some build tools (like Maven and Java 17+) require this extra launcher
 * because JavaFX applications cannot always be started directly from the main Application class.
 *
 * This class simply calls the main() method of the Main.java file.
 */
public class Launcher {
    public static void main(String[] args) { 
        Main.main(args); // Delegates control to the JavaFX Application class
    }
}
