package infrastructure.csv.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WriteCSV {
    public static Path write(
            Path uploadFolder,
            String filename,
            String content
    ){
        Path uploadFile = uploadFolder.resolve(filename);

        try(BufferedWriter writer =
                    Files.newBufferedWriter(uploadFile, StandardCharsets.UTF_8)){
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            System.out.println(e);
        }
        return uploadFile;
    }

    public static Path bindColumns(
            Path uploadFolder,
            String filename,
            String content
    ) {
        Path original = uploadFolder.resolve(filename);
        Path temp = uploadFolder.resolve(filename+"_temp" + ".csv");

        try (
                BufferedReader originalReader = Files.newBufferedReader(original, StandardCharsets.UTF_8);
                BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)
        ) {
            List<String> contentLines = new ArrayList<>(List.of(content.split("\\r?\\n+")));

            if(System.getenv("removeFirstLineCSV").equalsIgnoreCase("true")){
                contentLines.removeFirst();
            }

            String originalLine;
            int i = 0;

            while ((originalLine = originalReader.readLine()) != null) {
                String merged;

                if (i < contentLines.size()) {
                    merged = originalLine + "," + contentLines.get(i);
                    writer.write(merged);
                    writer.newLine();
                    i++;
                }
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        try {
            Files.deleteIfExists(original);
            Files.move(temp, original);
        } catch (IOException e) {
            System.out.println(e);
        }

        return original;
    }
}
