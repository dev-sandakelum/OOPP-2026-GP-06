package UI.Pages.lecturer;

// MarksPanel.java
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class Marks extends JPanel {

    public Marks() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header with filters
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Marks table
        JPanel tablePanel = createMarksTable();
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("Marks Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 33, 33));

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setBackground(new Color(245, 245, 245));

        JLabel courseLabel = new JLabel("Course:");
        JComboBox<String> courseCombo = new JComboBox<>(new String[]{"All Courses", "CS101", "CS201"});

        JLabel assessmentLabel = new JLabel("Assessment:");
        JComboBox<String> assessmentCombo = new JComboBox<>(new String[]{"All", "Assignment", "Exam", "Quiz"});

        // TODO: Add action listeners for filtering database queries

        filterPanel.add(courseLabel);
        filterPanel.add(courseCombo);
        filterPanel.add(assessmentLabel);
        filterPanel.add(assessmentCombo);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMarksTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // TODO: Connect to MySQL database to fetch marks data
        String[] columns = {"Student ID", "Student Name", "Course", "Assessment", "Marks", "Out Of", "Percentage", "Grade"};
        Object[][] data = {
                {"S001", "John Doe", "CS101", "Assignment 1", "85", "100", "85%", "A"},
                {"S002", "Jane Smith", "CS101", "Assignment 1", "92", "100", "92%", "A+"},
                {"S003", "Mike Johnson", "CS101", "Exam", "78", "100", "78%", "B"},
                {"S004", "Sarah Williams", "CS201", "Quiz", "88", "100", "88%", "A"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 4; // Allow editing from Marks column onwards
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);

        panel.add(scrollPane, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton saveButton = new JButton("Save Marks");
        JButton uploadButton = new JButton("Upload CSV");

        // TODO: Add MySQL database save operations

        saveButton.setBackground(new Color(52, 168, 84));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        saveButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        uploadButton.setBackground(new Color(66, 133, 244));
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        uploadButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        actionPanel.add(uploadButton);
        actionPanel.add(saveButton);

        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }
}

