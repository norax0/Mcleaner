package norax.dev.Mcleaner;

import norax.dev.Mcleaner.Ui.Window;
import norax.dev.Mcleaner.anvil.Parser;
import norax.dev.Mcleaner.anvil.Chunk;
import norax.dev.Mcleaner.anvil.Types.Status;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        if (args.length == 2) {
            String worldPath = args[0];
            String timeText = args[1];

            File regionDir = new File(worldPath);
            if (!regionDir.isDirectory()) {
                System.err.println("Invalid directory: " + worldPath);
                System.exit(1);
            }

            int minInhabited;
            try {
                minInhabited = Integer.parseInt(timeText);
            } catch (NumberFormatException ex) {
                System.err.println("Minimum Time must be an integer.");
                System.exit(1);
                return;
            }

            File[] mcaFiles = getRegionFiles(regionDir);

            if (mcaFiles == null || mcaFiles.length == 0) {
                System.err.println("No region files found in the selected directory or its /region subfolder.");
                System.exit(1);
            }

            long totalOriginalSize = 0;
            for (File file : mcaFiles) totalOriginalSize += file.length();

            int processed = 0;
            for (File mca : mcaFiles) {
                try {
                    List<Chunk> chunks = Parser.processRegionFile(mca);
                    chunks.removeIf(chunk -> chunk.getInhabitedTime().getSeconds() <= minInhabited);
                    Parser.saveRegionFile(mca, chunks);
                    processed++;
                } catch (IOException ex) {
                    System.err.println("Error processing file " + mca.getName() + ": " + ex.getMessage());
                }
            }

            long totalNewSize = 0;
            for (File file : mcaFiles) totalNewSize += file.length();
            double reductionPercent = 100.0 * (totalOriginalSize - totalNewSize) / totalOriginalSize;

            System.out.printf("Processing complete.\nRegion files saved: %d\nSize reduced by: %.2f%%%n", processed, reductionPercent);
            System.exit(0);
        } else {
            Window.init();        }
    }

    private static File[] getRegionFiles(File regionDir) {
        File[] mcaFiles = regionDir.listFiles((dir, name) -> name.endsWith(".mca"));
        if (mcaFiles == null || mcaFiles.length == 0) {
            File regionSubDir = new File(regionDir, "region");
            if (regionSubDir.isDirectory()) {
                mcaFiles = regionSubDir.listFiles((dir, name) -> name.endsWith(".mca"));
            }
        }
        return mcaFiles;
    }
}
