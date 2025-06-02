import javax.swing.text.Highlighter.HighlightPainter;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class SwingEditor extends JFrame {
    private JTextArea textArea;
    private JLabel statusLabel;
    private UndoManager undoManager;

    public SwingEditor() {
        setTitle("Advanced File Editor");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        undoManager = new UndoManager();

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.getDocument().addUndoableEditListener(undoManager);
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Top panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton openBtn = new JButton("Open");
        JButton saveBtn = new JButton("Save");
        JButton writeBtn = new JButton("Write");
        JButton searchBtn = new JButton("Search");
        JButton undoBtn = new JButton("Undo");
        JButton redoBtn = new JButton("Redo");
        JToggleButton themeToggle = new JToggleButton("Dark Mode");

        topPanel.add(openBtn);
        topPanel.add(saveBtn);
        topPanel.add(writeBtn);
        topPanel.add(searchBtn);
        topPanel.add(undoBtn);
        topPanel.add(redoBtn);
        topPanel.add(themeToggle);
        add(topPanel, BorderLayout.NORTH);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Words: 0 | Chars: 0 | Line: 1");
        JSlider fontSlider = new JSlider(10, 30, 14);
        fontSlider.setMajorTickSpacing(5);
        fontSlider.setPaintTicks(true);
        fontSlider.setPaintLabels(true);
        JTextField searchField = new JTextField(10);

        bottomPanel.add(statusLabel);
        bottomPanel.add(new JLabel("Font:"));
        bottomPanel.add(fontSlider);
        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(searchField);
        add(bottomPanel, BorderLayout.SOUTH);

        // Event listeners
        openBtn.addActionListener(e -> openFile());
        saveBtn.addActionListener(e -> saveFile());
        writeBtn.addActionListener(e -> writeFile());
        searchBtn.addActionListener(e -> searchWord(searchField.getText()));
        undoBtn.addActionListener(e -> { if (undoManager.canUndo()) undoManager.undo(); });
        redoBtn.addActionListener(e -> { if (undoManager.canRedo()) undoManager.redo(); });

        themeToggle.addActionListener(e -> {
            boolean dark = themeToggle.isSelected();
            textArea.setBackground(dark ? Color.BLACK : Color.WHITE);
            textArea.setForeground(dark ? Color.GREEN : Color.BLACK);
            themeToggle.setText(dark ? "Light Mode" : "Dark Mode");
        });

        fontSlider.addChangeListener(e -> {
            int size = fontSlider.getValue();
            textArea.setFont(new Font("Monospaced", Font.PLAIN, size));
        });

        textArea.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                updateStatus();
            }
        });

        setVisible(true);
    }

    private void updateStatus() {
        String text = textArea.getText();
        int words = text.trim().isEmpty() ? 0 : text.split("\\s+").length;
        int chars = text.length();
        int line = 1;
        try {
            line = textArea.getLineOfOffset(textArea.getCaretPosition()) + 1;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        statusLabel.setText("Words: " + words + " | Chars: " + chars + " | Line: " + line);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    textArea.append(line + "\n");
                }
                updateStatus();
            } catch (IOException e) {
                showError("Failed to read file.");
            }
        }
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(textArea.getText());
                updateStatus();
            } catch (IOException e) {
                showError("Failed to save file.");
            }
        }
    }

    private void writeFile() {
        String input = JOptionPane.showInputDialog(this, "Enter text to append:");
        if (input != null && !input.isEmpty()) {
            textArea.append(input + "\n");
            updateStatus();
        }
    }

    private void searchWord(String word) {
        removeHighlights();
        if (word == null || word.isEmpty()) return;

        Highlighter highlighter = textArea.getHighlighter();
        HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        String text = textArea.getText().toLowerCase();
        word = word.toLowerCase();

        int index = text.indexOf(word);
        while (index >= 0) {
            try {
                highlighter.addHighlight(index, index + word.length(), painter);
                index = text.indexOf(word, index + word.length());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeHighlights() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingEditor());
    }
}