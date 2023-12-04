package service.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class WordsCountService {
    public static void main(String[] args) {
        try {
            String conteudo = new String(Files.readAllBytes(Paths.get("/tmp/data.txt")));
            String[] palavras = conteudo.split("\\s+");

            Map<String, Integer> contagemPalavras = new HashMap<>();

            for (String palavra : palavras) {
                contagemPalavras.put(palavra, contagemPalavras.getOrDefault(palavra, 0) + 1);
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/data_counter.txt"))) {
                for (Map.Entry<String, Integer> entry : contagemPalavras.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
                System.out.println("O arquivo de contagem foi gerado e está disponível em sua pasta tmp!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}