package ru.comavp.tools.functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ListFilesTool implements Function<Map<String, Object>, String> {

    public static Function<Map<String, Object>, String> getListFilesTool() {
        return new ListFilesTool();
    }

    @Override
    public String apply(Map<String, Object> input) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String path = (String) input.get("path");
            Path dir = Paths.get(StringUtils.isNotEmpty(path) ? path : ".");
            List<String> files = new ArrayList<>();
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = dir.relativize(file);
                    files.add(relativePath.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
                    Path relativePath = dir.relativize(directory);
                    if (!relativePath.toString().isEmpty()) {
                        files.add(relativePath + "/");
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
            Collections.sort(files);
            return mapper.writeValueAsString(files);
        } catch (Exception e) {
            return "Error during listing files: " + e.getMessage();
        }
    }
}
