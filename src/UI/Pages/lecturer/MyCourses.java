package UI.Pages.lecturer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class MyCourses extends JPanel {

    public MyCourses() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Courses Table
        JPanel tablePanel = createCoursesTable();
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(33, 33, 33));

        panel.add(titleLabel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createCoursesTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // TODO: Connect to MySQL database to fetch courses list
        String[] columns = {"Course ID", "Course Name", "Semester", "Students", "Status"};
        Object[][] data = {
                {"CS101", "Data Structures", "Fall 2024", "45", "Active"},
                {"CS201", "Algorithms", "Fall 2024", "38", "Active"},
                {"CS301", "Database Design", "Spring 2024", "42", "Completed"},
                {"CS102", "Web Development", "Fall 2024", "50", "Active"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        // Action Buttons Panel
        JPanel actionPanel = createActionPanel();
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton viewButton = createStyledButton("View Details", new Color(66, 133, 244));
        JButton editButton = createStyledButton("Edit Course", new Color(251, 188, 4));

        // TODO: Add action listeners to connect with database operations

        panel.add(viewButton);
        panel.add(editButton);

        return panel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
