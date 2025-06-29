package norax.dev.Mcleaner.Ui;

import norax.dev.Mcleaner.anvil.Chunk;
import norax.dev.Mcleaner.anvil.Parser;
import norax.dev.Mcleaner.anvil.Types.Status;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Window {
    public static void init() {
        JFrame frame = newMainframe();
        Color panelColor = new Color(64, 68, 75);
        Color textColor = new Color(220, 221, 222);
        Color borderColor = new Color(79, 84, 92);
        Color buttonColor = new Color(88, 101, 242);

        JTextField pathField = newPath(frame, panelColor, textColor, borderColor, buttonColor);
        JTextField timeField = newTime(frame, panelColor, textColor, borderColor);
        JButton startButton = startButton(frame, pathField, timeField, buttonColor, textColor, borderColor);
        JButton experimentalButton = newExp(frame, buttonColor, textColor, borderColor);



        frame.add(startButton);
        frame.add(experimentalButton);
        frame.setVisible(true);

        UIManager.put("ToolTip.background", new Color(64, 68, 75));
        UIManager.put("ToolTip.foreground", new Color(220, 221, 222));
        UIManager.put("ToolTip.border", BorderFactory.createLineBorder(new Color(79, 84, 92)));
        UIManager.put("ToolTip.font", new Font("SansSerif", Font.PLAIN, 12));

    }

    static boolean deleteProtoChunks;

    private static JFrame newMainframe() {
        JFrame f = new JFrame("Mcleaner");
        f.setResizable(false);
        f.setSize(450, 200);
        f.setLayout(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setBackground(new Color(54, 57, 63));
        return f;
    }

    private static JTextField newPath(JFrame f, Color panelColor, Color textColor, Color borderColor, Color buttonColor) {
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

        return pathField;
    }

    private static JTextField newTime(JFrame f, Color panelColor, Color textColor, Color borderColor) {
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

        return timeField;
    }

    private static JButton startButton(JFrame f, JTextField pathField, JTextField timeField, Color buttonColor, Color textColor, Color borderColor) {
        JButton startButton = new JButton("Start");
        startButton.setBounds(120, 130, 100, 30);
        startButton.setBackground(buttonColor);
        startButton.setForeground(textColor);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(borderColor));

        startButton.addActionListener(e -> {
            String worldPath = pathField.getText();
            String timeText = timeField.getText();

            if (worldPath.isEmpty() || timeText.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(f, "Have you made a backup?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) return;

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

            File[] mcaFiles = getRegionFiles(regionDir);

            if (mcaFiles == null || mcaFiles.length == 0) {
                JOptionPane.showMessageDialog(f, "No region files found in the selected directory or its /region subfolder.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            long totalOriginalSize = 0;
            for (File file : mcaFiles) totalOriginalSize += file.length();

            int processed = 0;
            for (File mca : mcaFiles) {
                try {
                    List<Chunk> chunks = Parser.processRegionFile(mca);


                    if (deleteProtoChunks) {
                        chunks.removeIf(chunk -> chunk.getStatus() != Status.FULL);
                    }

                    chunks.removeIf(chunk -> chunk.getInhabitedTime().getSeconds() <= minInhabited);
                    Parser.saveRegionFile(mca, chunks);
                    processed++;
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(f, "Error processing file " + mca.getName() + ": " + ex.getMessage(),
                            "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }

            long totalNewSize = 0;
            for (File file : mcaFiles) totalNewSize += file.length();
            double reductionPercent = 100.0 * (totalOriginalSize - totalNewSize) / totalOriginalSize;

            JOptionPane.showMessageDialog(f,
                    String.format("Processing complete.\nRegion files saved: %d\nSize reduced by: %.2f%%", processed, reductionPercent),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        return startButton;
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

    private static JButton newExp(JFrame parent, Color buttonColor, Color textColor, Color borderColor) {
        JButton experimentalButton = new JButton("Experimental");
        experimentalButton.setBounds(230, 130, 120, 30);
        experimentalButton.setBackground(buttonColor);
        experimentalButton.setForeground(textColor);
        experimentalButton.setFocusPainted(false);
        experimentalButton.setBorder(BorderFactory.createLineBorder(borderColor));

        experimentalButton.addActionListener(e -> showExp(parent));

        return experimentalButton;
    }

    private static void showExp(JFrame parent) {
        JDialog dialog = new JDialog(parent, "Experimental Settings", true);
        dialog.setSize(300, 120);
        dialog.setLayout(null);
        dialog.getContentPane().setBackground(new Color(54, 57, 63));

        JLabel label = new JLabel("Delete proto-chunks:");
        label.setForeground(new Color(220, 221, 222));
        label.setBounds(20, 20, 150, 25);
        dialog.add(label);

        JCheckBox checkbox = new JCheckBox();
        checkbox.setBackground(new Color(54, 57, 63));
        checkbox.setForeground(new Color(220, 221, 222));
        checkbox.setFocusPainted(false);
        checkbox.setBounds(180, 20, 25, 25);
        checkbox.setSelected(deleteProtoChunks);
        checkbox.addActionListener(e -> deleteProtoChunks = checkbox.isSelected());
        dialog.add(checkbox);

        JLabel tooltipLabel = new JLabel("?");
        tooltipLabel.setBounds(210, 20, 25, 25);
        tooltipLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tooltipLabel.setToolTipText("Deletes non fully generated chunks");
        tooltipLabel.setBorder(BorderFactory.createLineBorder(new Color(79, 84, 92)));
        tooltipLabel.setOpaque(true);
        tooltipLabel.setBackground(new Color(64, 68, 75));
        tooltipLabel.setForeground(new Color(220, 221, 222));
        dialog.add(tooltipLabel);

        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

}
