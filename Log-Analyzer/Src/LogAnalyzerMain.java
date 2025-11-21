import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class LogAnalyzerMain {

    public static void main(String[] args) throws Exception {

        String folderPath = "logs";
        int N = Runtime.getRuntime().availableProcessors();

        ExecutorService executor = Executors.newFixedThreadPool(N);

        List<String> keywords = List.of("ERROR", "WARN", "INFO", "DEBUG");

        ConcurrentHashMap<String, Integer> globalCounts = new ConcurrentHashMap<>();
        keywords.forEach(k -> globalCounts.put(k, 0));

        long start = System.currentTimeMillis();

        // Collect file paths
        List<Path> files = Files.list(Paths.get(folderPath))
                                .filter(Files::isRegularFile)
                                .collect(Collectors.toList());

        // Submit tasks
        List<Future<Map<String, Integer>>> results = new ArrayList<>();
        for (Path f : files) {
            results.add(executor.submit(new LogFileTask(f, keywords)));
        }

        // Aggregate results
        for (Future<Map<String, Integer>> future : results) {
            Map<String, Integer> local = future.get();
            local.forEach((k, v) -> globalCounts.merge(k, v, Integer::sum));
        }

        executor.shutdown();

        long end = System.currentTimeMillis();

        System.out.println("\n=== Summary ===");
        globalCounts.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("Execution Time (Concurrent): " + (end - start) + " ms");

        // Write output file
        Files.writeString(
            Paths.get("output/result.txt"),
            globalCounts.toString()
        );
    }
}
