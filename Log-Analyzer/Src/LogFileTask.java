import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;

public class LogFileTask implements Callable<Map<String, Integer>> {

    private final Path filePath;
    private final List<String> keywords;

    public LogFileTask(Path filePath, List<String> keywords) {
        this.filePath = filePath;
        this.keywords = keywords;
    }

    @Override
    public Map<String, Integer> call() throws Exception {
        Map<String, Integer> localCounts = new HashMap<>();

        // Initialize counts
        for (String k : keywords) {
            localCounts.put(k, 0);
        }

        try (var stream = Files.lines(filePath)) {
            stream.forEach(line -> {
                keywords.forEach(keyword -> {
                    if (line.contains(keyword)) {
                        localCounts.put(keyword, localCounts.get(keyword) + 1);
                    }
                });
            });
        }

        System.out.println("Processed: " + filePath.getFileName() 
                           + " by " + Thread.currentThread().getName());
        return localCounts;
    }
}
