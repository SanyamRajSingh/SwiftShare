package kanin.fileportal;

/**
 * Launcher
 *
 * Execution bootstrap entry point for SwiftShare.
 *
 * Viva / Technical Explanation:
 * In Java 11 and later (JDK 17), the Java launcher (java.exe) enforces strict checks
 * when executing a class that extends javafx.application.Application. If the class is
 * launched directly from a fat/shaded JAR on the classpath rather than the module-path,
 * the JVM fails immediately with:
 * "Error: JavaFX runtime components are missing, and are required to run this application".
 *
 * By using this separate Launcher class (which does NOT extend Application) as the Main-Class
 * in the JAR manifest, the Java runtime treats it as a standard standalone Java class.
 * Launcher.main() then delegates to Main.main(args), which smoothly initializes the JavaFX
 * runtime without requiring external module flags (--module-path or --add-modules).
 */
public class Launcher {

    // ==================================================
    // JVM Entry Point
    // ==================================================

    public static void main(String[] args) {
        // Delegates control directly to JavaFX Application Main class
        Main.main(args);
    }
}

