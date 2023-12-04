package service.file;

import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;

public class FileWriterService {
    public static void writeFile(String data) {
        try {
            FileWriter write = new FileWriter("/tmp/data.txt", true);
            write.write(
                    Normalizer
                            .normalize(data, Normalizer.Form.NFD)
                            .replaceAll("[^\\p{ASCII}]", "")
                            .toLowerCase()
            );
            write.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}