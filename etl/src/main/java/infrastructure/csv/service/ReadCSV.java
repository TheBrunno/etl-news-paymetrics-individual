package infrastructure.csv.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class ReadCSV {
    public static String readSpecificColumn(
            Path uploadFolder,
            String filename,
            int indexColumn
    ){
        Path uploadFile = uploadFolder.resolve(filename);

        try(Scanner sc = new Scanner(uploadFile)){
            boolean header = true;
            String[] line;

            StringBuilder result = new StringBuilder();

            while(sc.hasNextLine()){
                if(header){
                    header = false;
                    sc.nextLine();
                }else{
                    line = sc.nextLine().split(",");
                    try{
                        result.append(line[indexColumn]).append("\n");
                    }catch (ArrayIndexOutOfBoundsException e){
                        return result.toString();
                    }
                }
            }

        } catch (IOException e) {
            System.out.println(e);
        }
        return "";
    }
}
