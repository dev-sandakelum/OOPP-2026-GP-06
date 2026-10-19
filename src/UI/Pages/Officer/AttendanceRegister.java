package UI.Pages.Officer;

import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

public class AttendanceRegister extends Page {

    private static final String[] COLS = {"#", "Undergraduate", "Medical on File", "Mark"};

    private static final String[] COURSES = {
            "ICT2131 — Programming with Java",
            "ICT2132 — Database Management Systems",
            "ICT2133 — Operating Systems",
            "ICT2134 — Computer Networks"
    };

    private static final String[] TYPES = {"Theory", "Practical"};

    private static final String[] SESSIONS = {
            "Session 01", "Session 02", "Session 03", "Session 04", "Session 05", "Session 06"
    };

    // ---------- Placeholder data matching the UI mockup ----------
    private static final String[][] SEED = {
            {"01", "W. A. N. Silva (FOT/2023/061)", "—", "P"},
            {"02", "D. M. R. Fernando (FOT/2023/062)", "—", "P"},
            {"03", "H. P. K. Perera (FOT/2023/063)", "—", "P"},
            {"04", "K. T. Jayasuriya (FOT/2023/064)", "—", "A"},
            {"05", "S. B. Bandara (FOT/2023/065)", "—", "P"},
            {"06", "N. P. Weerasinghe (FOT/2023/066)", "—", "A"},
            {"07", "A. G. Gunasekara (FOT/2023/067)", "—", "P"},
            {"08", "R. K. Rathnayake (FOT/2022/034)", "—", "P"}
    };
    // -------------------------------------------------------------

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final JComboBox<String> coursePicker = new JComboBox<>(COURSES);
    private final JComboBox<String> typePicker = new JComboBox<>(TYPES);
    private final JComboBox<String> sessionPicker = new JComboBox<>(SESSIONS);
    private final JLabel sessionNotice = new JLabel();

    public AttendanceRegister() {
        super("Attendance Register", "Mark and update session attendance", null);

        for (String[] row : SEED) {
            model.addRow(row);
        }

        // P -> OK (Green), M -> WARN (Orange/Yellow), A -> BRAND (Red/Accent)
        table = table(model, Map.of(
                "P", OK,
                "M", WARN,
                "A", BRAND
        ), 3);

        table.setRowHeight(44);

        // Adjust column widths and text alignments
        configureTableColumns();

        // Click on the 'Mark' column (index 3) cycles: P -> A -> M -> P
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    toggleAttendanceMark(row);
                }
            }
        });

        // Main layout container
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel topSection = new JPanel(new BorderLayout(0, 10));
        topSection.setOpaque(false);
        topSection.add(createToolbar(), BorderLayout.NORTH);
        topSection.add(createNoticeBanner(), BorderLayout.SOUTH);

        body.add(topSection, BorderLayout.NORTH);
        body.add(tableCard(table), BorderLayout.CENTER);

        setBody(body);
        updateNotice();
    }

    private void configureTableColumns() {
        TableColumnModel colModel = table.getColumnModel();

        // Column 0: "#" (Compact, center-aligned)
        colModel.getColumn(0).setPreferredWidth(60);
        colModel.getColumn(0).setMaxWidth(80);
        colModel.getColumn(0).setMinWidth(50);
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        colModel.getColumn(0).setCellRenderer(centerRender);

        // Column 1: "Undergraduate" (Wide, left-aligned with clean padding)
        colModel.getColumn(1).setPreferredWidth(360);
        colModel.getColumn(1).setMinWidth(280);
        DefaultTableCellRenderer leftPadRender = new DefaultTableCellRenderer();
        leftPadRender.setHorizontalAlignment(SwingConstants.LEFT);
        leftPadRender.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        colModel.getColumn(1).setCellRenderer(leftPadRender);

        // Column 2: "Medical on File" (Center-aligned)
        colModel.getColumn(2).setPreferredWidth(140);
        colModel.getColumn(2).setCellRenderer(centerRender);

        // Column 3: "Mark" (Compact, center-aligned)
        colModel.getColumn(3).setPreferredWidth(100);
        colModel.getColumn(3).setMaxWidth(120);
        colModel.getColumn(3).setMinWidth(80);
    }

    private JComponent createToolbar() {
        coursePicker.setPreferredSize(new Dimension(280, 36));

        typePicker.addActionListener(e -> updateNotice());

        sessionPicker.setSelectedItem("Session 04");
        sessionPicker.addActionListener(e -> updateNotice());

        RoundedButton allPresentBtn = new RoundedButton("✓ All present", false);
        allPresentBtn.addActionListener(e -> markAllPresent());

        RoundedButton saveBtn = new RoundedButton("💾 Save register", true);
        saveBtn.addActionListener(e -> saveRegister());

        // Left controls: Course, Type (Theory/Practical), Session
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(coursePicker);
        left.add(typePicker);
        left.add(sessionPicker);

        // Right controls: Action buttons
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(allPresentBtn);
        right.add(saveBtn);

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JComponent createNoticeBanner() {
        sessionNotice.setFont(sessionNotice.getFont().deriveFont(12.5f));
        sessionNotice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createDashedBorder(null, 2, 4),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return sessionNotice;
    }

    private void updateNotice() {
        String type = ((String) typePicker.getSelectedItem()).toLowerCase();
        String session = (String) sessionPicker.getSelectedItem();
        sessionNotice.setText(String.format(
                "<html><b>%s</b> · %s · 29 Jun. Undergraduates with a medical covering this session are pre-set to <b>medical leave</b>; approved leave counts separately from attendance.</html>",
                session, type
        ));
    }

    private void toggleAttendanceMark(int row) {
        String current = (String) model.getValueAt(row, 3);
        String next;
        if ("P".equals(current)) {
            next = "A";
        } else if ("A".equals(current)) {
            next = "M";
        } else {
            next = "P";
        }
        model.setValueAt(next, row, 3);
    }

    private void markAllPresent() {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt("P", i, 3);
        }
    }

    private void saveRegister() {
        int presentCount = 0;
        int absentCount = 0;
        int medicalCount = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            String mark = (String) model.getValueAt(i, 3);
            if ("P".equals(mark)) presentCount++;
            else if ("A".equals(mark)) absentCount++;
            else if ("M".equals(mark)) medicalCount++;
        }

        String summary = String.format(
                "Attendance saved successfully!\n\nSession: %s (%s)\nPresent: %d\nAbsent: %d\nMedical: %d",
                sessionPicker.getSelectedItem(),
                typePicker.getSelectedItem(),
                presentCount,
                absentCount,
                medicalCount
        );
        info(this, summary);
    }
}