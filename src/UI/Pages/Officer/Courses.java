package UI.Pages.Officer;

import UI.Pages.Components.Card;
import UI.Pages.Components.Page;
import UI.Pages.Components.ReadOnlyTableModel;
import UI.Pages.Components.RoundedButton;
import UI.Pages.Components.SectionCard;
import UI.Pages.Components.SimpleDocListener;
import data.CourseData;
import data.DepartmentData;
import data.LecturerData;
import model.Course;
import model.Department;
import model.Lecturer;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// Officer "Courses" tab: schedule a course (type, lecturer, batch, GPA, credits and
// evaluation weights) and list / edit / delete the scheduled courses.
public class Courses extends Page {

    private static final String[] COLS = {"Course ID", "Name", "Type", "Lecturer", "Dept", "Batch",
            "GPA", "Credits", "End / CA", "Actions"};
    private static final int COL_TYPE = 2, COL_ACTIONS = 9;
    private static final String SELECT = "-- Select --";

    private final CourseData courseData = new CourseData();
    private final DepartmentData departmentData = new DepartmentData();
    private final LecturerData lecturerData = new LecturerData();

    private final Map<Integer, Lecturer> lecturers = new HashMap<>();   // user_id -> lecturer
    private final List<Department> departments = new ArrayList<>();
    private final List<Course> courses = new ArrayList<>();             // one per table row (model order)

    // ----- form widgets -----
    private final JTextField codeF = field(20), nameF = field(20), lecturerF = field(20),
            batchF = field(20), creditsF = field(20);
    private final JRadioButton theory = radio("Theory", true), practical = radio("Practical", false);
    private final JRadioButton gpa = radio("GPA", true), ngpa = radio("NGPA", false);
    private final JLabel lecturerName = new JLabel("Lecturer name will appear here");
    private final JComboBox<String> deptCb = new JComboBox<>();

    private final JTextField endF = percent(), caF = percent();
    private final JCheckBox[] caBox = {check("Quizzes"), check("Assignments"), check("Project"), check("Mid")};
    private final JTextField[] caF4 = {percent(), percent(), percent(), percent()};
    private final JLabel totalLbl = new JLabel(), caLbl = new JLabel();

    private final RoundedButton submit = new RoundedButton("Schedule Course", true);
    private final RoundedButton clear = new RoundedButton("Clear", false);

    private final ReadOnlyTableModel model = new ReadOnlyTableModel(COLS);
    private final JTable table;
    private final Card tableHolder;

    private Course editing;   // null = scheduling a new course

    public Courses(User user) {
        super("Courses", "Logged in as: " + (user == null ? "Officer" : user.getName()), null);

        table = table(model, Map.of("Theory", BRAND, "Practical", OK), COL_TYPE);
        table.getColumnModel().getColumn(COL_ACTIONS).setCellRenderer(new ActionsRenderer());
        table.getColumnModel().getColumn(COL_ACTIONS).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onTableClick(e); }
        });
        tableHolder = (Card) tableCard(table);

        loadLookups();

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(left(new SectionCard("Schedule a Course", null, buildDetails())));
        body.add(Box.createVerticalStrut(16));
        body.add(left(new SectionCard("Evaluation Criteria", null, buildEvaluation())));
        body.add(Box.createVerticalStrut(14));
        body.add(left(buildButtons()));
        body.add(Box.createVerticalStrut(16));
        body.add(left(new SectionCard("Scheduled Courses", null, tableHolder)));
        setScrollBody(wrapNorth(body));

        wireEvents();
        reload();
        recalc();
    }

    // -----------------------------------------------------------------
    //  Layout
    // -----------------------------------------------------------------

    private JComponent buildDetails() {
        lecturerName.setFont(font(Font.PLAIN, 12));
        lecturerName.setForeground(INK_2);
        deptCb.setFont(font(Font.PLAIN, 14));

        ButtonGroup t = new ButtonGroup();
        t.add(theory);
        t.add(practical);
        ButtonGroup g = new ButtonGroup();
        g.add(gpa);
        g.add(ngpa);

        JPanel p = new JPanel(new GridLayout(0, 2, 20, 10));
        p.setOpaque(false);
        p.add(labelled("Course ID", codeF, "e.g. ICT2152"));
        p.add(labelled("Course Name", nameF, "e.g. Object Oriented Programming"));
        p.add(labelledC("Course Type", radios(theory, practical)));
        JPanel lec = new JPanel(new BorderLayout(0, 3));
        lec.setOpaque(false);
        lec.add(labelled("Lecturer ID", lecturerF, "e.g. LEC001"), BorderLayout.CENTER);
        lec.add(lecturerName, BorderLayout.SOUTH);
        p.add(lec);
        p.add(labelledC("Department", deptCb));
        p.add(labelled("Batch", batchF, "e.g. 2023/2024"));
        p.add(labelledC("GPA / NGPA", radios(gpa, ngpa)));
        p.add(labelled("Credits", creditsF, "e.g. 3"));
        return p;
    }

    private JComponent buildEvaluation() {
        JLabel hint = new JLabel("End + CA must total 100 %. The CA components must total the CA weight.");
        hint.setFont(font(Font.PLAIN, 12));
        hint.setForeground(INK_2);

        JPanel end = new JPanel(new BorderLayout());
        end.setOpaque(false);
        end.add(bold("End Examination"), BorderLayout.NORTH);
        end.add(weightRow(boldLabel("Weight (%)"), endF), BorderLayout.CENTER);

        JPanel ca = new JPanel();
        ca.setOpaque(false);
        ca.setLayout(new BoxLayout(ca, BoxLayout.Y_AXIS));
        ca.add(bold("Continuous Assessment (CA)"));
        ca.add(weightRow(boldLabel("Weight (%)"), caF));
        ca.add(Box.createVerticalStrut(4));
        for (int i = 0; i < caBox.length; i++) ca.add(weightRow(caBox[i], caF4[i]));

        totalLbl.setFont(font(Font.PLAIN, 13));
        caLbl.setFont(font(Font.PLAIN, 13));
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        totals.setOpaque(false);
        totals.add(totalLbl);
        totals.add(caLbl);

        JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
        grid.setOpaque(false);
        grid.add(inner(end));
        grid.add(inner(ca));

        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        p.add(hint, BorderLayout.NORTH);
        p.add(grid, BorderLayout.CENTER);
        p.add(totals, BorderLayout.SOUTH);
        return p;
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        p.setOpaque(false);
        p.add(clear);
        p.add(submit);
        return p;
    }

    private JComponent inner(JComponent c) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.add(c, BorderLayout.CENTER);
        return card;
    }

    private JComponent weightRow(JComponent label, JTextField f) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 0, 5, 0));
        row.add(label, BorderLayout.CENTER);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(f);
        JLabel pc = new JLabel("%");
        pc.setForeground(INK_2);
        right.add(pc);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JComponent labelled(String text, JTextField f, String hint) {
        f.setToolTipText(hint);
        f.putClientProperty("hint", hint);
        return labelledC(text, f);
    }

    private JComponent labelledC(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(label(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JComponent radios(JRadioButton a, JRadioButton b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return p;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
    }

    private static JComponent wrapNorth(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.NORTH);
        return p;
    }

    private static JLabel bold(String s) {
        JLabel l = boldLabel(s);
        l.setBorder(new EmptyBorder(0, 0, 6, 0));
        return l;
    }

    private static JLabel boldLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(font(Font.BOLD, 14));
        l.setForeground(INK);
        return l;
    }

    private static JRadioButton radio(String s, boolean sel) {
        JRadioButton r = new JRadioButton(s, sel);
        r.setOpaque(false);
        r.setFont(font(Font.PLAIN, 14));
        r.setForeground(INK);
        r.setFocusPainted(false);
        return r;
    }

    private static JCheckBox check(String s) {
        JCheckBox c = new JCheckBox(s, true);
        c.setOpaque(false);
        c.setFont(font(Font.BOLD, 14));
        c.setForeground(INK);
        c.setFocusPainted(false);
        return c;
    }

    private static JTextField percent() {
        JTextField f = field(4);
        f.setText("0");
        f.setHorizontalAlignment(SwingConstants.RIGHT);
        f.setPreferredSize(new Dimension(70, 36));
        return f;
    }

    // -----------------------------------------------------------------
    //  Events
    // -----------------------------------------------------------------

    private void wireEvents() {
        Runnable recalc = this::recalc;
        for (JTextField f : new JTextField[]{endF, caF, caF4[0], caF4[1], caF4[2], caF4[3]})
            f.getDocument().addDocumentListener(new SimpleDocListener(recalc));
        for (int i = 0; i < caBox.length; i++) {
            final int k = i;
            caBox[i].addActionListener(e -> {
                if (!caBox[k].isSelected()) caF4[k].setText("0");
                recalc();
            });
        }
        lecturerF.getDocument().addDocumentListener(new SimpleDocListener(this::updateLecturerName));
        codeF.getDocument().addDocumentListener(new SimpleDocListener(recalc));
        nameF.getDocument().addDocumentListener(new SimpleDocListener(recalc));
        batchF.getDocument().addDocumentListener(new SimpleDocListener(recalc));
        creditsF.getDocument().addDocumentListener(new SimpleDocListener(recalc));
        deptCb.addActionListener(e -> recalc());

        submit.addActionListener(e -> save());
        clear.addActionListener(e -> resetForm());
    }

    // Edit / Delete links in the Actions column
    private void onTableClick(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        int col = table.columnAtPoint(e.getPoint());
        if (row < 0) return;
        int m = table.convertRowIndexToModel(row);
        if (col == COL_ACTIONS) {
            Rectangle r = table.getCellRect(row, col, false);
            if (e.getX() - r.x < r.width / 2) startEdit(courses.get(m));
            else delete(courses.get(m));
        }
    }

    // -----------------------------------------------------------------
    //  Validation (live)
    // -----------------------------------------------------------------

    private static int num(JTextField f) {
        try {
            int v = Integer.parseInt(f.getText().trim());
            return Math.max(v, 0);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean weightsOk;

    private void recalc() {
        for (int i = 0; i < caBox.length; i++) caF4[i].setEnabled(caBox[i].isSelected());

        int end = num(endF), ca = num(caF);
        int sum = 0;
        boolean bad = end < 0 || ca < 0;
        for (int i = 0; i < caBox.length; i++) {
            int v = caBox[i].isSelected() ? num(caF4[i]) : 0;
            if (v < 0) bad = true; else sum += v;
        }
        int total = Math.max(end, 0) + Math.max(ca, 0);
        boolean totalOk = !bad && total == 100;
        boolean caOk = !bad && sum == ca;
        weightsOk = totalOk && caOk;

        totalLbl.setText("<html>End + CA: <b>" + total + " / 100</b></html>");
        totalLbl.setForeground(totalOk ? OK : BAD);
        caLbl.setText("<html>CA components: <b>" + sum + " / " + Math.max(ca, 0) + "</b></html>");
        caLbl.setForeground(caOk ? OK : BAD);

        boolean filled = !codeF.getText().isBlank() && !nameF.getText().isBlank()
                && !batchF.getText().isBlank() && !creditsF.getText().isBlank()
                && deptCb.getSelectedIndex() > 0;
        submit.setEnabled(weightsOk && filled);
    }

    private static int idDigits(String s) {
        String d = s.replaceAll("\\D", "");
        return d.isEmpty() ? -1 : Integer.parseInt(d.length() > 9 ? d.substring(0, 9) : d);
    }

    private void updateLecturerName() {
        Lecturer l = lecturers.get(idDigits(lecturerF.getText()));
        lecturerName.setText(l != null ? l.getName()
                : lecturerF.getText().isBlank() ? "Lecturer name will appear here" : "No lecturer with this ID");
    }

    // -----------------------------------------------------------------
    //  Data
    // -----------------------------------------------------------------

    private void loadLookups() {
        try {
            for (Lecturer l : lecturerData.findAll()) lecturers.put(l.getUser_id(), l);
            departments.addAll(departmentData.findAll());
        } catch (Exception ex) {
            info(this, "Could not load lecturers / departments: " + ex.getMessage());
        }
        deptCb.addItem(SELECT);
        for (Department d : departments) deptCb.addItem(d.getDep_name());
    }

    private void reload() {
        model.setRowCount(0);
        courses.clear();
        try {
            for (Course c : courseData.findAll()) {
                courses.add(c);
                model.addRow(new Object[]{
                        c.getCourse_code(), c.getCourse_name(),
                        c.getCourse_type() == null ? "" : c.getCourse_type(),
                        c.getLecturer_id() == null ? "-" : String.format("LEC%03d", c.getLecturer_id()),
                        deptShort(c.getDep_id()),
                        c.getBatch() == null ? "" : c.getBatch(),
                        c.getGpa_type() == null ? "" : c.getGpa_type(),
                        creditText(c.getCredit()),
                        c.getEnd_weight() + " / " + c.getCa_weight(),
                        ""});
            }
        } catch (Exception ex) {
            info(this, "Could not load courses: " + ex.getMessage());
        }
        int rows = Math.max(model.getRowCount(), 2);
        tableHolder.setPreferredSize(new Dimension(100, rows * table.getRowHeight() + 80));
        tableHolder.revalidate();
        tableHolder.repaint();
    }

    private static String creditText(double c) {
        return c == Math.rint(c) ? String.valueOf((int) c) : String.valueOf(c);
    }

    // "Information and Communication Technology" -> ICT, "Bio-system Technology" -> BST
    private String deptShort(Integer depId) {
        if (depId == null) return "-";
        for (Department d : departments) {
            if (d.getDep_id() == depId) {
                StringBuilder sb = new StringBuilder();
                for (String w : d.getDep_name().split("[\\s-]+"))
                    if (!w.isEmpty() && !w.equalsIgnoreCase("and")) sb.append(Character.toUpperCase(w.charAt(0)));
                return sb.toString();
            }
        }
        return "-";
    }

    // -----------------------------------------------------------------
    //  Actions
    // -----------------------------------------------------------------

    private void save() {
        String code = codeF.getText().trim().toUpperCase();
        String name = nameF.getText().trim();
        String batch = batchF.getText().trim();

        double credits;
        try {
            credits = Double.parseDouble(creditsF.getText().trim());
            if (credits <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            info(this, "Credits must be a positive number.");
            return;
        }

        Integer lecId = null;
        if (!lecturerF.getText().isBlank()) {
            int id = idDigits(lecturerF.getText());
            if (!lecturers.containsKey(id)) {
                info(this, "No lecturer found with ID \"" + lecturerF.getText().trim() + "\".");
                return;
            }
            lecId = id;
        }

        Course c = new Course();
        if (editing != null) c.setCourse_id(editing.getCourse_id());
        c.setCourse_code(code);
        c.setCourse_name(name);
        c.setCredit(credits);
        c.setDep_id(departments.get(deptCb.getSelectedIndex() - 1).getDep_id());
        c.setCourse_type(theory.isSelected() ? "Theory" : "Practical");
        c.setLecturer_id(lecId);
        c.setBatch(batch);
        c.setGpa_type(gpa.isSelected() ? "GPA" : "NGPA");
        c.setEnd_weight(num(endF));
        c.setCa_weight(num(caF));
        c.setQuiz_weight(caBox[0].isSelected() ? num(caF4[0]) : 0);
        c.setAssignment_weight(caBox[1].isSelected() ? num(caF4[1]) : 0);
        c.setProject_weight(caBox[2].isSelected() ? num(caF4[2]) : 0);
        c.setMid_weight(caBox[3].isSelected() ? num(caF4[3]) : 0);

        try {
            if (editing != null) courseData.updateFull(c);
            else courseData.insertFull(c);
        } catch (Exception ex) {
            info(this, "Could not save the course: " + ex.getMessage());
            return;
        }
        resetForm();
        reload();
    }

    private void startEdit(Course c) {
        editing = c;
        codeF.setText(c.getCourse_code());
        nameF.setText(c.getCourse_name());
        (("Practical".equals(c.getCourse_type())) ? practical : theory).setSelected(true);
        lecturerF.setText(c.getLecturer_id() == null ? "" : String.format("LEC%03d", c.getLecturer_id()));
        deptCb.setSelectedIndex(0);
        for (int i = 0; i < departments.size(); i++)
            if (c.getDep_id() != null && departments.get(i).getDep_id() == c.getDep_id()) deptCb.setSelectedIndex(i + 1);
        batchF.setText(c.getBatch() == null ? "" : c.getBatch());
        ("NGPA".equals(c.getGpa_type()) ? ngpa : gpa).setSelected(true);
        creditsF.setText(creditText(c.getCredit()));
        endF.setText(String.valueOf(c.getEnd_weight()));
        caF.setText(String.valueOf(c.getCa_weight()));
        int[] w = {c.getQuiz_weight(), c.getAssignment_weight(), c.getProject_weight(), c.getMid_weight()};
        for (int i = 0; i < w.length; i++) {
            caBox[i].setSelected(w[i] > 0);
            caF4[i].setText(String.valueOf(w[i]));
        }
        submit.setText("Update Course");
        recalc();
        scrollRectToVisible(new Rectangle(0, 0, 1, 1));
    }

    private void delete(Course c) {
        if (!confirm(this, "Delete " + c.getCourse_code() + "?")) return;
        try {
            courseData.delete(c.getCourse_id());
        } catch (Exception ex) {
            info(this, "Could not delete the course: " + ex.getMessage());
            return;
        }
        if (editing != null && editing.getCourse_id() == c.getCourse_id()) resetForm();
        reload();
    }

    private void resetForm() {
        editing = null;
        for (JTextField f : new JTextField[]{codeF, nameF, lecturerF, batchF, creditsF}) f.setText("");
        theory.setSelected(true);
        gpa.setSelected(true);
        deptCb.setSelectedIndex(0);
        endF.setText("0");
        caF.setText("0");
        for (int i = 0; i < caBox.length; i++) {
            caBox[i].setSelected(true);
            caF4[i].setText("0");
        }
        submit.setText("Schedule Course");
        recalc();
    }

    // -----------------------------------------------------------------
    //  "Edit | Delete" cell
    // -----------------------------------------------------------------

    private static final class ActionsRenderer extends JPanel implements TableCellRenderer {
        private final RoundedButton edit = new RoundedButton("Edit", false);
        private final RoundedButton del = new RoundedButton("Delete", false);

        ActionsRenderer() {
            super(new GridLayout(1, 2, 6, 0));
            setOpaque(false);
            setBorder(new EmptyBorder(4, 0, 2, 4));
            edit.setBorder(new EmptyBorder(4, 10, 6, 10));
            del.setBorder(new EmptyBorder(4, 10, 6, 10));
            add(edit);
            add(del);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }
}
