package UI.Pages.Officer;

import UI.Pages.Components.Card;
import UI.Pages.Components.SectionCard;
import UI.Pages.Components.SimpleDocListener;
import model.Course;
import model.Department;
import model.Lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// The "Schedule a Course" + "Evaluation Criteria" form, shown in the Add and Edit windows
// opened from the Courses tab. Validates live: the owner is told through onChange whenever
// something changes, and isComplete() says whether it can be saved.
public class CourseForm extends JPanel {

    public static final String[] CA_NAMES = {"Quizzes", "Assignments", "Project", "Mid"};
    private static final String NONE = "-- Select --";
    private static final String[] CREDIT_OPTIONS = {NONE, "0", "1", "2", "3", "4"};

    private final List<Lecturer> lecturers;
    private final List<Department> departments;

    private final JTextField codeF = field(20), nameF = field(20), batchF = field(20);
    private final JRadioButton theory = radio("Theory", true), practical = radio("Practical", false);
    private final JRadioButton gpa = radio("GPA", true), ngpa = radio("NGPA", false);
    private final JComboBox<String> lecturerCb = new JComboBox<>();
    private final JComboBox<String> deptCb = new JComboBox<>();
    private final JComboBox<String> creditsCb = new JComboBox<>(CREDIT_OPTIONS);

    private final JTextField endF = percent(), caF = percent();
    private final JCheckBox[] caBox = new JCheckBox[CA_NAMES.length];
    private final JTextField[] caParts = new JTextField[CA_NAMES.length];
    private final JLabel totalLbl = new JLabel(), caLbl = new JLabel();

    private Runnable onChange = () -> { };
    private boolean complete;

    public CourseForm(List<Lecturer> lecturers, List<Department> departments) {
        this.lecturers = lecturers;
        this.departments = departments;
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        lecturerCb.addItem(NONE);
        for (Lecturer l : lecturers) lecturerCb.addItem(lecturerText(l));
        deptCb.addItem(NONE);
        for (Department d : departments) deptCb.addItem(d.getDep_name());
        for (JComboBox<String> cb : List.of(lecturerCb, deptCb, creditsCb)) cb.setFont(font(Font.PLAIN, 14));

        for (int i = 0; i < CA_NAMES.length; i++) {
            caBox[i] = check(CA_NAMES[i]);
            caParts[i] = percent();
        }

        add(left(new SectionCard("Course Details", null, details())));
        add(Box.createVerticalStrut(16));
        add(left(new SectionCard("Evaluation Criteria", null, evaluation())));

        wire();
        recalc();
    }

    public void setOnChange(Runnable r) { onChange = r == null ? () -> { } : r; }

    /** True when every required field is filled and the weights add up. */
    public boolean isComplete() { return complete; }

    // -----------------------------------------------------------------
    //  Layout
    // -----------------------------------------------------------------

    private JComponent details() {
        ButtonGroup t = new ButtonGroup();
        t.add(theory);
        t.add(practical);
        ButtonGroup g = new ButtonGroup();
        g.add(gpa);
        g.add(ngpa);

        JPanel p = new JPanel(new GridLayout(0, 2, 20, 10));
        p.setOpaque(false);
        p.add(labelled("Course ID", hint(codeF, "e.g. ICT2152")));
        p.add(labelled("Course Name", hint(nameF, "e.g. Object Oriented Programming")));
        p.add(labelled("Course Type", radios(theory, practical)));
        p.add(labelled("Lecturer", lecturerCb));
        p.add(labelled("Department", deptCb));
        p.add(labelled("Batch", hint(batchF, "e.g. 2023/2024")));
        p.add(labelled("GPA / NGPA", radios(gpa, ngpa)));
        p.add(labelled("Credits (0 – 4)", creditsCb));
        return p;
    }

    // PDF layout: hint, End card, CA card (weight, dashed rule, components), totals line
    private JComponent evaluation() {
        JLabel hint = new JLabel("End + CA must total 100 %. The CA components must total the CA weight.");
        hint.setFont(font(Font.PLAIN, 12));
        hint.setForeground(INK_2);

        JPanel end = column();
        end.add(heading("End Examination"));
        end.add(weightRow(boldLabel("Weight (%)"), endF));

        JPanel ca = column();
        ca.add(heading("Continuous Assessment (CA)"));
        ca.add(weightRow(boldLabel("Weight (%)"), caF));
        ca.add(dashedRule());
        for (int i = 0; i < CA_NAMES.length; i++) ca.add(weightRow(caBox[i], caParts[i]));

        totalLbl.setFont(font(Font.PLAIN, 13));
        caLbl.setFont(font(Font.PLAIN, 13));
        JPanel totals = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        totals.setOpaque(false);
        totals.setBorder(new EmptyBorder(4, 12, 0, 0));
        totals.add(totalLbl);
        totals.add(caLbl);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(left(hint));
        p.add(Box.createVerticalStrut(12));
        p.add(left(inner(end)));
        p.add(Box.createVerticalStrut(12));
        p.add(left(inner(ca)));
        p.add(Box.createVerticalStrut(8));
        p.add(left(totals));
        return p;
    }

    private static JPanel column() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JComponent inner(JComponent c) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(14, 20, 16, 20));
        card.add(c, BorderLayout.CENTER);
        return card;
    }

    private static JComponent heading(String s) {
        JLabel l = boldLabel(s);
        l.setFont(font(Font.BOLD, 15));
        l.setBorder(new EmptyBorder(0, 0, 8, 0));
        return left(l);
    }

    private static JComponent weightRow(JComponent label, JTextField f) {
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
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return left(row);
    }

    private static JComponent dashedRule() {
        JComponent rule = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(LINE_2);
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{4, 4}, 0));
                g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                g2.dispose();
            }
        };
        rule.setPreferredSize(new Dimension(10, 14));
        rule.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        return left(rule);
    }

    private static JComponent labelled(String text, JComponent c) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        p.add(label(text), BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private static JTextField hint(JTextField f, String hint) {
        f.setToolTipText(hint);
        return f;
    }

    private static JComponent radios(JRadioButton a, JRadioButton b) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 6));
        p.setOpaque(false);
        p.add(a);
        p.add(b);
        return p;
    }

    private static <T extends JComponent> T left(T c) {
        c.setAlignmentX(LEFT_ALIGNMENT);
        return c;
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
        f.setPreferredSize(new Dimension(80, 36));
        return f;
    }

    // -----------------------------------------------------------------
    //  Live validation
    // -----------------------------------------------------------------

    private void wire() {
        SimpleDocListener doc = new SimpleDocListener(this::recalc);
        for (JTextField f : new JTextField[]{codeF, nameF, batchF, endF, caF}) f.getDocument().addDocumentListener(doc);
        for (int i = 0; i < CA_NAMES.length; i++) {
            final int k = i;
            caParts[i].getDocument().addDocumentListener(doc);
            caBox[i].addActionListener(e -> {
                if (!caBox[k].isSelected()) caParts[k].setText("0");
                recalc();
            });
        }
        for (JComboBox<String> cb : List.of(lecturerCb, deptCb, creditsCb)) cb.addActionListener(e -> recalc());
    }

    private static int num(JTextField f) {
        try {
            int v = Integer.parseInt(f.getText().trim());
            return v < 0 ? -1 : v;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void recalc() {
        for (int i = 0; i < CA_NAMES.length; i++) caParts[i].setEnabled(caBox[i].isSelected());

        int end = num(endF), ca = num(caF);
        boolean bad = end < 0 || ca < 0;
        int sum = 0;
        for (int i = 0; i < CA_NAMES.length; i++) {
            int v = caBox[i].isSelected() ? num(caParts[i]) : 0;
            if (v < 0) bad = true; else sum += v;
        }
        int total = Math.max(end, 0) + Math.max(ca, 0);
        boolean totalOk = !bad && total == 100;
        boolean caOk = !bad && sum == ca;

        totalLbl.setText("<html>End + CA: <b>" + total + " / 100</b></html>");
        totalLbl.setForeground(totalOk ? OK : BAD);
        caLbl.setText("<html>CA components: <b>" + sum + " / " + Math.max(ca, 0) + "</b></html>");
        caLbl.setForeground(caOk ? OK : BAD);

        boolean filled = !codeF.getText().isBlank() && !nameF.getText().isBlank() && !batchF.getText().isBlank()
                && deptCb.getSelectedIndex() > 0 && creditsCb.getSelectedIndex() > 0;
        complete = totalOk && caOk && filled;
        onChange.run();
    }

    // -----------------------------------------------------------------
    //  Model <-> form
    // -----------------------------------------------------------------

    public static String lecturerText(Lecturer l) {
        return l.getName() + "  (" + lecturerCode(l.getUser_id()) + ")";
    }

    public static String lecturerCode(Integer id) {
        return id == null ? "-" : String.format("LEC%03d", id);
    }

    /** Copies the form into the given course (a fresh one when editing a new course). */
    public Course fill(Course c) {
        c.setCourse_code(codeF.getText().trim().toUpperCase());
        c.setCourse_name(nameF.getText().trim());
        c.setCredit(Double.parseDouble((String) creditsCb.getSelectedItem()));
        c.setDep_id(departments.get(deptCb.getSelectedIndex() - 1).getDep_id());
        int lec = lecturerCb.getSelectedIndex();
        c.setLecturer_id(lec > 0 ? lecturers.get(lec - 1).getUser_id() : null);
        c.setCourse_type(theory.isSelected() ? "Theory" : "Practical");
        c.setBatch(batchF.getText().trim());
        c.setGpa_type(gpa.isSelected() ? "GPA" : "NGPA");
        c.setEnd_weight(num(endF));
        c.setCa_weight(num(caF));
        c.setQuiz_weight(part(0));
        c.setAssignment_weight(part(1));
        c.setProject_weight(part(2));
        c.setMid_weight(part(3));
        return c;
    }

    private int part(int i) { return caBox[i].isSelected() ? num(caParts[i]) : 0; }

    public void load(Course c) {
        codeF.setText(c.getCourse_code());
        nameF.setText(c.getCourse_name());
        ("Practical".equals(c.getCourse_type()) ? practical : theory).setSelected(true);
        lecturerCb.setSelectedIndex(0);
        for (int i = 0; i < lecturers.size(); i++)
            if (c.getLecturer_id() != null && lecturers.get(i).getUser_id() == c.getLecturer_id())
                lecturerCb.setSelectedIndex(i + 1);
        deptCb.setSelectedIndex(0);
        for (int i = 0; i < departments.size(); i++)
            if (c.getDep_id() != null && departments.get(i).getDep_id() == c.getDep_id())
                deptCb.setSelectedIndex(i + 1);
        batchF.setText(c.getBatch() == null ? "" : c.getBatch());
        ("NGPA".equals(c.getGpa_type()) ? ngpa : gpa).setSelected(true);
        int credits = (int) Math.round(c.getCredit());
        creditsCb.setSelectedItem(credits >= 0 && credits <= 4 ? String.valueOf(credits) : NONE);
        endF.setText(String.valueOf(c.getEnd_weight()));
        caF.setText(String.valueOf(c.getCa_weight()));
        int[] w = {c.getQuiz_weight(), c.getAssignment_weight(), c.getProject_weight(), c.getMid_weight()};
        for (int i = 0; i < w.length; i++) {
            caBox[i].setSelected(w[i] > 0);
            caParts[i].setText(String.valueOf(w[i]));
        }
        recalc();
    }

    public void reset() {
        for (JTextField f : new JTextField[]{codeF, nameF, batchF}) f.setText("");
        theory.setSelected(true);
        gpa.setSelected(true);
        lecturerCb.setSelectedIndex(0);
        deptCb.setSelectedIndex(0);
        creditsCb.setSelectedIndex(0);
        endF.setText("0");
        caF.setText("0");
        for (int i = 0; i < CA_NAMES.length; i++) {
            caBox[i].setSelected(true);
            caParts[i].setText("0");
        }
        recalc();
    }
}
