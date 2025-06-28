package norax.dev.Mcleaner.Ui;

import norax.dev.Mcleaner.anvil.Chunk;
import norax.dev.Mcleaner.anvil.Parser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class Window {
    public static void init() {
        Color bgColor = new Color(54, 57, 63);
        Color panelColor = new Color(64, 68, 75);
        Color textColor = new Color(220, 221, 222);
        Color borderColor = new Color(79, 84, 92);
        Color buttonColor = new Color(88, 101, 242);


        UIManager.put("ToolTip.background", panelColor);
        UIManager.put("ToolTip.foreground", textColor);
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(79, 84, 92)));


        JFrame f = new JFrame("Mcleaner");
        f.setResizable(false);
        f.setSize(450, 200);
        f.setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setBackground(bgColor);

        JLabel pathLabel = new JLabel("World Path:");
        pathLabel.setBounds(20, 20, 100, 25);
        pathLabel.setForeground(textColor);
        f.add(pathLabel);

        JTextField pathField = new JTextField();
        pathField.setBounds(120, 20, 180, 25);
        pathField.setBackground(panelColor);
        pathField.setForeground(textColor);
        pathField.setCaretColor(textColor);
        pathField.setBorder(BorderFactory.createLineBorder(borderColor));
        pathField.setEditable(false);
        f.add(pathField);

        JButton browseButton = new JButton("Browse");
        browseButton.setBounds(310, 20, 90, 25);
        browseButton.setBackground(buttonColor);
        browseButton.setForeground(Color.WHITE);
        browseButton.setFocusPainted(false);
        browseButton.setBorder(BorderFactory.createLineBorder(borderColor));
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(f);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                pathField.setText(selectedFile.getAbsolutePath());
            }
        });
        f.add(browseButton);

        JLabel timeLabel = new JLabel("Minimum Time:");
        timeLabel.setBounds(20, 70, 100, 25);
        timeLabel.setForeground(textColor);
        f.add(timeLabel);

        JTextField timeField = new JTextField();
        timeField.setBounds(120, 70, 100, 25);
        timeField.setBackground(panelColor);
        timeField.setForeground(textColor);
        timeField.setCaretColor(textColor);
        timeField.setBorder(BorderFactory.createLineBorder(borderColor));
        f.add(timeField);

        JLabel helpLabel = new JLabel("?");
        helpLabel.setBounds(225, 70, 25, 25);
        helpLabel.setHorizontalAlignment(SwingConstants.CENTER);
        helpLabel.setToolTipText("The minimum time (in seconds) that chunks need to be habitated for to not be deleted.");
        helpLabel.setBorder(BorderFactory.createLineBorder(borderColor));
        helpLabel.setOpaque(true);
        helpLabel.setBackground(panelColor);
        helpLabel.setForeground(textColor);
        f.add(helpLabel);

        JButton startButton = new JButton("Start");
        startButton.setBounds(150, 130, 100, 30);
        startButton.setBackground(buttonColor);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(borderColor));
        startButton.addActionListener(e -> {
            String worldPath = pathField.getText();
            String timeText = timeField.getText();

            if (worldPath.isEmpty() || timeText.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(f,
                    "Have you made a backup?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            File regionDir = new File(worldPath);
            if (!regionDir.isDirectory()) {
                JOptionPane.showMessageDialog(f, "Invalid directory: " + worldPath, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int minInhabited;
            try {
                minInhabited = Integer.parseInt(timeText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(f, "Minimum Time must be an integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File[] mcaFiles = regionDir.listFiles((dir, name) -> name.endsWith(".mca"));
            if (mcaFiles == null || mcaFiles.length == 0) {
                JOptionPane.showMessageDialog(f, "No region files found in: " + regionDir, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long totalOriginalSize = 0;
            for (File file : mcaFiles) {
                totalOriginalSize += file.length();
            }

            int processed = 0;
            for (File mca : mcaFiles) {
                try {
                    List<Chunk> chunks = Parser.processRegionFile(mca);
                    chunks.removeIf(chunk -> chunk.getInhabitedTime().getSeconds() <= minInhabited);
                    Parser.saveRegionFile(mca, chunks);
                    processed++;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(f, "Error processing file " + mca.getName() + ": " + ex.getMessage(),
                            "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            long totalNewSize = 0;
            for (File file : mcaFiles) {
                totalNewSize += file.length();
            }

            double reductionPercent = 100.0 * (totalOriginalSize - totalNewSize) / totalOriginalSize;

            JOptionPane.showMessageDialog(f,
                    String.format("Processing complete.\nRegion files saved: %d\nSize reduced by: %.2f%%",
                            processed, reductionPercent),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });



        f.add(startButton);

        f.setVisible(true);
    }
}
