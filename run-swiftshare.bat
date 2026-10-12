@echo off
setlocal enabledelayedexpansion

:: ====================================================================
:: SwiftShare - One-Click Windows Launcher
:: Peer-to-Peer Desktop File Transfer Application (Java 17 / JavaFX)
:: Repository: https://github.com/SanyamRajSingh/SwiftShare.git
:: ====================================================================

title SwiftShare Launcher

:: Step 1: Establish project root directory (handles paths containing spaces)
cd /d "%~dp0"

echo ====================================================================
echo   SwiftShare - Peer-to-Peer File Transfer Launcher
echo ====================================================================
echo.

:: Step 2: Check Java Availability and Version (Java 17+ Required)
echo [1/3] Checking Java runtime environment...
where java >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo.
    echo ====================================================================
    echo [ERROR] Java is not installed or not available on your PATH.
    echo.
    echo SwiftShare requires Java Development Kit 17 or higher.
    echo.
    echo How to resolve:
    echo   1. Download JDK 17 from: https://adoptium.net/temurin/releases/?version=17
    echo   2. Run installer and enable options: Set JAVA_HOME and Add to PATH.
    echo   3. Re-open this launcher or restart your terminal.
    echo ====================================================================
    echo.
    pause
    exit /b 1
)

:: Verify Java version is 17 or higher
set "RAW_VER="
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "RAW_VER=%%g"
)

if defined RAW_VER (
    set "JAVA_VER=!RAW_VER:"=!"
    for /f "tokens=1 delims=." %%v in ("!JAVA_VER!") do (
        set "JAVA_MAJOR=%%v"
    )
)

if defined JAVA_MAJOR (
    if !JAVA_MAJOR! LSS 17 (
        echo.
        echo ====================================================================
        echo [ERROR] Incompatible Java version detected: !JAVA_VER!
        echo.
        echo SwiftShare requires Java 17 or higher to run JavaFX components.
        echo Please install JDK 17 and update your PATH and JAVA_HOME.
        echo ====================================================================
        echo.
        pause
        exit /b 1
    )
    echo       Found Java !JAVA_MAJOR! [!JAVA_VER!] - OK
) else (
    echo [WARNING] Could not parse Java version. Proceeding with launch...
)

:: Step 3: Resolve Maven Build Tool
echo [2/3] Checking build status and packaging...
set "MVN_CMD="

if exist "%~dp0mvnw.cmd" (
    set "MVN_CMD="%~dp0mvnw.cmd""
) else (
    where mvn >nul 2>&1
    if %ERRORLEVEL% equ 0 (
        set "MVN_CMD=mvn"
    ) else (
        :: Check IntelliJ IDEA embedded Maven fallback
        if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd" (
            set "MVN_CMD="C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.1\plugins\maven\lib\maven3\bin\mvn.cmd""
        )
    )
)

:: Step 4: Build Executable JAR if Missing or Requested
set "FORCE_REBUILD=0"
if /i "%~1"=="--build" set "FORCE_REBUILD=1"
if /i "%~1"=="--rebuild" set "FORCE_REBUILD=1"

if not exist "target\SwiftShare.jar" set "FORCE_REBUILD=1"

if !FORCE_REBUILD! equ 1 (
    if "!MVN_CMD!"=="" (
        echo.
        echo ====================================================================
        echo [ERROR] Packaged JAR not found and Apache Maven was not found on PATH.
        echo.
        echo A one-time initial build is required on fresh repository clones:
        echo   1. Install Apache Maven 3.8+ from: https://maven.apache.org/download.cgi
        echo   2. Add Maven bin directory to your Windows PATH environment variable.
        echo   3. Alternatively, compile and package from your IDE.
        echo ====================================================================
        echo.
        pause
        exit /b 1
    )

    echo       Building SwiftShare executable JAR with Maven...
    echo       Executing: !MVN_CMD! -B -DskipTests package
    echo.
    call !MVN_CMD! -B -DskipTests package
    if %ERRORLEVEL% neq 0 (
        echo.
        echo ====================================================================
        echo [ERROR] Maven build failed with exit code %ERRORLEVEL%.
        echo Please inspect the Maven output above for compiler or dependency errors.
        echo ====================================================================
        echo.
        pause
        exit /b %ERRORLEVEL%
    )
    echo.
    echo       Build completed successfully.
) else (
    echo       Existing packaged JAR found: target\SwiftShare.jar - Skipping rebuild.
    echo       [To force a rebuild, pass --build flag: run-swiftshare.bat --build]
)

:: Verify Target Artifact Exists
if not exist "target\SwiftShare.jar" (
    echo.
    echo ====================================================================
    echo [ERROR] Build output target\SwiftShare.jar is missing.
    echo ====================================================================
    echo.
    pause
    exit /b 1
)

:: Step 5: Launch SwiftShare Application
echo [3/3] Launching SwiftShare desktop interface...
echo.
echo ====================================================================
echo   Application running. Close the application window to exit.
echo ====================================================================
echo.

java -jar "target\SwiftShare.jar"
set "APP_EXIT_CODE=%ERRORLEVEL%"

if %APP_EXIT_CODE% neq 0 (
    echo.
    echo ====================================================================
    echo [ERROR] SwiftShare exited with non-zero exit code: %APP_EXIT_CODE%
    echo ====================================================================
    echo.
    pause
    exit /b %APP_EXIT_CODE%
)

echo.
echo SwiftShare closed cleanly.
exit /b 0
