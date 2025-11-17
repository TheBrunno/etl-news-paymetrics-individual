package infrastructure.csv.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
