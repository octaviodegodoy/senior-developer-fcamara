package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileProcessor {
    private static List<String> lines = Collections.synchronizedList(new ArrayList<>());
    public static void main(String[] args) throws Exception {
        Path filePath = Paths.get("data.txt");
        // Verifica se o arquivo existe
        if (!Files.exists(filePath)) {
            System.err.println("Arquivo nao encontrado: " + filePath.toAbsolutePath());
            return;
        }

        // Le o arquivo apenas uma vez
        List<String> fileLines = Files.readAllLines(filePath);

        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {
            // Distribui o processamento por threads
            int chunkSize = Math.max(1, (fileLines.size() + 9) / 10);
            for (int i = 0; i < 10; i++) {
                final int start = i * chunkSize;
                final int end = Math.min(start + chunkSize, fileLines.size());

                if (start < fileLines.size()) {
                    executor.submit(() -> {
                        for (int j = start; j < end; j++) {
                            lines.add(fileLines.get(j).toUpperCase());
                        }
                    });
                }
            }

            // Espera pelo termino do processamento
            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.err.println("Tasks did not finish in time");
                executor.shutdownNow();
            }

            System.out.println("Lines processed: " + lines.size());

        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }


    }
}
