import com.paranoiaworks.sse.Encryptor;
import com.paranoiaworks.sse.MessageHandler;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DecryptPTE {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java DecryptPTE <input.enc> <output_dir> <password>");
            System.out.println("  input.enc  - Paranoia encrypted .enc file");
            System.out.println("  output_dir - Directory to extract decrypted files");
            System.out.println("  password   - Decryption password");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputDir = args[1];
        String password = args[2];

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.err.println("Error: Input file not found: " + inputPath);
            System.exit(1);
        }

        // Create output directory
        new File(outputDir).mkdirs();

        try {
            System.out.println("Input: " + inputPath);
            System.out.println("Output: " + outputDir);
            System.out.println("Decrypting...");

            char[] passwordChars = password.toCharArray();

            // Algorithm code 0 = AES-256, purpose 3 = file encryption
            Encryptor encryptor = new Encryptor(passwordChars, 0, 3, true);

            MessageHandler mh = new MessageHandler();
            List<String> logs = new ArrayList<>();

            encryptor.unzipAndDecryptFile(inputFile, mh, new File(outputDir), logs);

            System.out.println("Decryption completed successfully!");
            System.out.println("Output directory: " + outputDir);

            // Print logs
            for (String log : logs) {
                System.out.println("  " + log);
            }

        } catch (Exception e) {
            System.err.println("Decryption failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
