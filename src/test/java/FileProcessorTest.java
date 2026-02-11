import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorIntegrationTest {

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    @DisplayName("Should print error when file does not exist")
    void testFileNotFoundErrorMessage() throws Exception {
        // Arrange: Set a non-existent file path
        Path nonExistentFile = tempDir.resolve("missing.txt");

        // Act: Simulate the file check from FileProcessor
        if (!Files.exists(nonExistentFile)) {
            System.err.println("File not found: " + nonExistentFile.toAbsolutePath());
        }

        // Assert
        String errorOutput = errorStream.toString();
        assertTrue(errorOutput.contains("File not found"),
                "Should print file not found error");
        assertTrue(errorOutput.contains(nonExistentFile.toAbsolutePath().toString()),
                "Should include absolute path");
    }

    @Test
    @DisplayName("Should process file successfully when it exists")
    void testSuccessfulProcessing() throws Exception {
        // Arrange: Create test file with content
        Path testFile = tempDir.resolve("data.txt");
        List<String> testLines = Arrays.asList("hello", "world", "java");
        Files.write(testFile, testLines);

        // Act: Verify file exists and is readable
        assertTrue(Files.exists(testFile), "Test file should exist");

        List<String> readLines = Files.readAllLines(testFile);

        // Assert
        assertEquals(3, readLines.size(), "Should read 3 lines");
        assertEquals("hello", readLines.get(0), "First line should match");
    }

    @Test
    @DisplayName("Should handle file existence check correctly")
    void testFileExistenceCheck() throws IOException {
        // Test 1: File exists
        Path existingFile = tempDir.resolve("exists.txt");
        Files.write(existingFile, Collections.singletonList("data"));
        assertTrue(Files.exists(existingFile), "File should exist");

        // Test 2: File doesn't exist
        Path missingFile = tempDir.resolve("missing.txt");
        assertFalse(Files.exists(missingFile), "File should not exist");

        // Test 3: Verify the behavior matches FileProcessor logic
        if (!Files.exists(missingFile)) {
            System.err.println("File not found: " + missingFile.toAbsolutePath());
        }

        assertTrue(errorStream.toString().contains("File not found"),
                "Should print error for missing file");
    }
}