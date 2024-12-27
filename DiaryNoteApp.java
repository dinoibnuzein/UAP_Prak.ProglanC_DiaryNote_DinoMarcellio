import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class DiaryNoteApp {
    private static ArrayList<String> diaryEntries = new ArrayList<>();
    private static String savedUsername = null;
    private static String savedPassword = null;

    public static void main(String[] args) {
        // Apply Retro Theme
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception e) {
            System.err.println("Failed to apply theme: " + e.getMessage());
        }

        while (!createOrLogin()) {
            JOptionPane.showMessageDialog(null, "You must create an account or login to proceed.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JFrame frame = new JFrame("Diary Note App");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        LocalDate currentDate = LocalDate.now();
        JLabel dateLabel = new JLabel("Today's Date: " + currentDate.toString());
        dateLabel.setFont(new Font("Courier New", Font.BOLD, 16));
        dateLabel.setForeground(Color.YELLOW);
        dateLabel.setBackground(Color.DARK_GRAY);
        dateLabel.setOpaque(true);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(dateLabel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setCaretColor(Color.WHITE);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBackground(Color.DARK_GRAY);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);

        JButton saveButton = new JButton("Save Note");
        styleButton(saveButton);
        saveButton.addActionListener(e -> {
            try {
                String text = textArea.getText();
                if (text.isEmpty()) {
                    throw new Exception("Diary entry cannot be empty!");
                }
                String entryWithDate = "Date: " + currentDate.toString() + "\n" + text;
                diaryEntries.add(entryWithDate);
                saveToFile(entryWithDate);
                JOptionPane.showMessageDialog(frame, "Entry saved successfully.");
                textArea.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton viewButton = new JButton("View Notes");
        styleButton(viewButton);
        viewButton.addActionListener(e -> {
            try {
                StringBuilder entries = new StringBuilder();
                for (String entry : diaryEntries) {
                    entries.append(entry).append("\n\n");
                }

                if (entries.length() == 0) {
                    throw new Exception("No entries to display.");
                }

                // Membuat panel untuk menampilkan entri dengan gambar
                JPanel entriesPanel = new JPanel();
                entriesPanel.setLayout(new BoxLayout(entriesPanel, BoxLayout.Y_AXIS));
                entriesPanel.setBackground(Color.DARK_GRAY);

                // Membuat label untuk menampilkan entri teks
                JTextArea entriesTextArea = new JTextArea(entries.toString());
                entriesTextArea.setFont(new Font("Courier New", Font.PLAIN, 14));
                entriesTextArea.setBackground(Color.BLACK);
                entriesTextArea.setForeground(Color.GREEN);
                entriesTextArea.setCaretColor(Color.WHITE);
                entriesTextArea.setEditable(false);

                // Menambahkan teks entri ke panel
                entriesPanel.add(entriesTextArea);

                // Memeriksa apakah ada gambar di setiap entri
                for (String entry : diaryEntries) {
                    if (entry.contains("[Image:")) {
                        // Ekstrak path gambar
                        String imagePath = entry.substring(entry.indexOf("[Image:") + 7, entry.indexOf("]"));
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            ImageIcon imageIcon = new ImageIcon(imageFile.getAbsolutePath());
                            JLabel imageLabel = new JLabel(imageIcon);
                            entriesPanel.add(imageLabel);  // Menambahkan gambar ke panel
                        }
                    }
                }

                // Menampilkan panel dengan entri dan gambar
                JOptionPane.showMessageDialog(frame, new JScrollPane(entriesPanel), "Diary Entries", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton uploadImageButton = new JButton("Upload Image");
        styleButton(uploadImageButton);
        uploadImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image Files", "jpg", "png", "jpeg", "gif"));
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    if (selectedFile != null && selectedFile.exists()) {
                        String text = textArea.getText();
                        if (text.isEmpty()) {
                            throw new Exception("Please write a diary entry before adding an image.");
                        }
                        String entryWithImage = "Date: " + currentDate.toString() + "\n" + text + "\n[Image: " + selectedFile.getAbsolutePath() + "]";
                        diaryEntries.add(entryWithImage);
                        saveToFile(entryWithImage);
                        JOptionPane.showMessageDialog(frame, "Image added and entry saved successfully.");
                        textArea.setText("");
                    } else {
                        throw new Exception("Invalid file selected.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JButton updateButton = new JButton("Update Note");
        styleButton(updateButton);
        updateButton.addActionListener(e -> {
            String[] entriesArray = diaryEntries.toArray(new String[0]);
            String selectedEntry = (String) JOptionPane.showInputDialog(frame, "Select an entry to update", "Update Entry",
                    JOptionPane.QUESTION_MESSAGE, null, entriesArray, entriesArray[0]);

            if (selectedEntry != null) {
                String newText = JOptionPane.showInputDialog(frame, "Enter new text for the entry:", selectedEntry);
                if (newText != null && !newText.trim().isEmpty()) {
                    diaryEntries.set(diaryEntries.indexOf(selectedEntry), "Date: " + currentDate.toString() + "\n" + newText);
                    saveAllEntriesToFile();
                    JOptionPane.showMessageDialog(frame, "Entry updated successfully.");
                }
            }
        });

        JButton deleteButton = new JButton("Delete Note");
        styleButton(deleteButton);
        deleteButton.addActionListener(e -> {
            String[] entriesArray = diaryEntries.toArray(new String[0]);
            String selectedEntry = (String) JOptionPane.showInputDialog(frame, "Select an entry to delete", "Delete Entry",
                    JOptionPane.QUESTION_MESSAGE, null, entriesArray, entriesArray[0]);

            if (selectedEntry != null) {
                int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this entry?", "Delete Entry", JOptionPane.YES_NO_OPTION);
                if (confirmation == JOptionPane.YES_OPTION) {
                    diaryEntries.remove(selectedEntry);
                    saveAllEntriesToFile();
                    JOptionPane.showMessageDialog(frame, "Entry deleted successfully.");
                }
            }
        });

        JButton logoutButton = new JButton("Logout");
        styleButton(logoutButton);
        logoutButton.addActionListener(e -> {
            int confirmation = JOptionPane.showConfirmDialog(frame, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirmation == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(frame, "You have been logged out.", "Logout", JOptionPane.INFORMATION_MESSAGE);
                frame.dispose();
                main(null);
            }
        });

        buttonPanel.add(saveButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(uploadImageButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(logoutButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Courier New", Font.BOLD, 14));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.GREEN);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.YELLOW));
    }

    private static boolean createOrLogin() {
        while (true) {
            JPanel panel = new JPanel(new GridLayout(3, 1));
            panel.setBackground(Color.DARK_GRAY);

            JLabel label = new JLabel("Welcome! Choose an option:");
            label.setFont(new Font("Courier New", Font.BOLD, 14));
            label.setForeground(Color.GREEN);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(label);

            Object[] options = {"Create Account", "Login"};
            int choice = JOptionPane.showOptionDialog(null, panel, "Welcome",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (choice == 0) {
                return createAccount();
            } else if (choice == 1) {
                return login();
            } else {
                System.exit(0);
            }
        }
    }

    private static boolean createAccount() {
        while (true) {
            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.setBackground(Color.DARK_GRAY);

            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setFont(new Font("Courier New", Font.BOLD, 12));
            usernameLabel.setForeground(Color.GREEN);
            JTextField usernameField = new JTextField();
            usernameField.setBackground(Color.BLACK);
            usernameField.setForeground(Color.GREEN);

            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setFont(new Font("Courier New", Font.BOLD, 12));
            passwordLabel.setForeground(Color.GREEN);
            JPasswordField passwordField = new JPasswordField();
            passwordField.setBackground(Color.BLACK);
            passwordField.setForeground(Color.GREEN);

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Create Account", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // Validasi username dan password
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Username and Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (password.length() < 8) {
                    JOptionPane.showMessageDialog(null, "Password must be at least 8 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!password.matches(".*[A-Z].*")) {
                    JOptionPane.showMessageDialog(null, "Password must contain at least one uppercase letter.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!password.matches(".*[a-z].*")) {
                    JOptionPane.showMessageDialog(null, "Password must contain at least one lowercase letter.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!password.matches(".*\\d.*")) {
                    JOptionPane.showMessageDialog(null, "Password must contain at least one number.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                // Menyimpan username dan password
                savedUsername = username;
                savedPassword = password;
                JOptionPane.showMessageDialog(null, "Account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                break; // Jika pengguna membatalkan, keluar dari loop
            }
        }
        return false; // Mengembalikan false jika proses pembuatan akun dibatalkan
    }


    private static boolean login() {
        while (true) {
            if (savedUsername == null || savedPassword == null) {
                JOptionPane.showMessageDialog(null, "No account exists. Please create an account first.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.setBackground(Color.DARK_GRAY);

            JLabel usernameLabel = new JLabel("Username:");
            usernameLabel.setFont(new Font("Courier New", Font.BOLD, 12));
            usernameLabel.setForeground(Color.GREEN);
            JTextField usernameField = new JTextField();
            usernameField.setBackground(Color.BLACK);
            usernameField.setForeground(Color.GREEN);

            JLabel passwordLabel = new JLabel("Password:");
            passwordLabel.setFont(new Font("Courier New", Font.BOLD, 12));
            passwordLabel.setForeground(Color.GREEN);
            JPasswordField passwordField = new JPasswordField();
            passwordField.setBackground(Color.BLACK);
            passwordField.setForeground(Color.GREEN);

            panel.add(usernameLabel);
            panel.add(usernameField);
            panel.add(passwordLabel);
            panel.add(passwordField);

            int result = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (username.equals(savedUsername) && password.equals(savedPassword)) {
                    JOptionPane.showMessageDialog(null, "Login successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    return true;
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
            } else {
                break;
            }
        }
        return false;
    }

    private static void saveToFile(String entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("diary_entries.txt", true))) {
            writer.write(entry);
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void saveAllEntriesToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("diary_entries.txt"))) {
            for (String entry : diaryEntries) {
                writer.write(entry);
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
