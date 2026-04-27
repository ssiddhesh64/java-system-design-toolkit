package Design;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DuplicateFileRemover {

    private static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) throws IOException {

        String folderPath = "path/to/your/folder";
        List<File> files = listtAllFiles(folderPath);

        Map<Long, List<File>> sizeMap = groupBySize(files);

        Map<String, List<File>> hashMap = computeHashes(sizeMap);

        List<List<File>> duplicates = findDuplicates(hashMap);

        removeDuplicates(duplicates);

    }

    private static void removeDuplicates(List<List<File>> duplicates) {

        for(List<File> group : duplicates) {
            for(int i = 1; i < group.size(); i++) {
                File fileToDelete = group.get(i);
                if(fileToDelete.delete()) {
                    System.out.println("Deleted duplicate: " + fileToDelete.getAbsolutePath());
                } else {
                    System.err.println("Failed to delete: " + fileToDelete.getAbsolutePath());
                }
            }
        }
    }

//    private static List<List<File>> findDuplicates(Map<String, List<File>> hashMap) {
//
//        List<List<File>> duplicates = new ArrayList<>();
//        for(List<File> files : hashMap.values()) {
//            if(files.size() > 1) {
//                List<File> verified = verifyDuplicates(files);
//                if(verified.size() > 1) {
//                    duplicates.add(verified);
//                }
//            }
//        }
//        return duplicates;
//    }

private static List<List<File>> findDuplicates(Map<String, List<File>> hashMap) {
    List<List<File>> duplicates = new ArrayList<>();

    for (List<File> files : hashMap.values()) {
        if (files.size() < 2) continue;

        List<List<File>> groups = new ArrayList<>();

        for (File file : files) {
            boolean added = false;

            for (List<File> group : groups) {
                if (areFilesIdentical(file, group.get(0))) {
                    group.add(file);
                    added = true;
                    break;
                }
            }

            if (!added) {
                List<File> newGroup = new ArrayList<>();
                newGroup.add(file);
                groups.add(newGroup);
            }
        }

        // Only keep duplicate groups
        for (List<File> group : groups) {
            if (group.size() > 1) {
                duplicates.add(group);
            }
        }
    }

    return duplicates;
}

    private static boolean areFilesIdentical(File file1, File file2) {

        try(FileInputStream fis1 = new FileInputStream(file1);
            FileInputStream fis2 = new FileInputStream(file2)) {

            byte[] buffer1 = new byte[8192];
            byte[] buffer2 = new byte[8192];

            int bytesRead1, bytesRead2;
            while ((bytesRead1 = fis1.read(buffer1)) != -1) {
                bytesRead2 = fis2.read(buffer2);
                if(bytesRead1 != bytesRead2 || !Arrays.equals(buffer1, buffer2)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static Map<String, List<File>> computeHashes(Map<Long, List<File>> sizeMap) {

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        Map<String, List<File>> hashMap = new ConcurrentHashMap<>();

        List<Future<?>> futures = new ArrayList<>();
        for(List<File> files : sizeMap.values()) {
            if(files.size() < 2) continue;

            for(File file : files) {
                futures.add(executor.submit(() -> {
                    try {
                        String hash = computeHash(file);
                        hashMap.computeIfAbsent(hash, k -> Collections.synchronizedList(new ArrayList<>()))
                                .add(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }));
            }
        }

        for(Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return hashMap;
    }

    private static String computeHash(File file) throws Exception {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        try(InputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = md.digest();
        return bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] hashBytes) {
        StringBuilder sb = new StringBuilder();
        for(byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static List<File> listtAllFiles(String folderPath) throws IOException {
        List<File> files = new ArrayList<>();
        Files.walk(Paths.get(folderPath))
                .filter(Files::isRegularFile)
                .forEach(path -> files.add(path.toFile()));
        return files;
    }

    private static Map<Long, List<File>> groupBySize(List<File> files) {
        Map<Long, List<File>> sizeMap = new HashMap<>();
        for(File file : files) {
            sizeMap.computeIfAbsent(file.length(), k -> new ArrayList<>()).add(file);
        }
        return sizeMap;
    }
}
