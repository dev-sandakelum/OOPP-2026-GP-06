package UI.Pages.Officer;

import UI.Kit.Glass;
import UI.Pages.Components.Card;
import UI.Pages.Components.SectionCard;
import UI.Pages.Components.SimpleDocListener;
import model.Course;
import model.Department;
import model.Lecturer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static UI.Kit.Palette.*;
import static UI.Pages.Components.Ui.*;

// The "Schedule a Course" + "Evaluation Criteria" form, shown in the Add and Edit windows
// opened from the Courses tab. Validates live: the owner is told through onChange whenever
// something changes, and isComplete() says whether it can be saved.
//
// Course code = department prefix + year + semester + course no. + credits, e.g. ICT1132:
//   ICT -> Information and Communication Technology, 1 -> first year, 1 -> first semester,
//   3 -> third course of that semester, 2 -> two credits.
// The "Course Code Breakdown" strip shows those parts as drop-downs. Picking a part rewrites
// the code; typing a code re-selects the parts. The course no. is only edited through the code.
public class CourseForm extends JPanel {

    public static final String[] CA_NAMES = {"Quizzes", "Assignments", "Project", "Mid"};
    private static final String NONE = "-- Select --";
    private static final String[] YEARS = {"First Year", "Second Year", "Third Year", "Fourth Year"};
    private static final String[] SEMESTERS = {"First Semester", "Second Semester"};
    private static final String[] CREDITS = {"0 Credits", "1 Credit", "2 Credits", "3 Credits", "4 Credits"};
    private static final String DEFAULT_NO = "1";          // course no. used when the code has none yet

    private static final int YEAR = 0, SEM = 1, NO = 2, CREDIT = 3;   // digit positions in the code
    private static final Pattern CODE = Pattern.compile("^([A-Za-z]*)(\\d*)(.*)$");
    private static final Pattern FULL_CODE = Pattern.compile("^[A-Z]{2,6}\\d{4}$");

    private final List<Lecturer> lecturers;
    private final List<Department> departments;
    private final List<String> deptCodes = new ArrayList<>();   // "ICT", "ET", ... parallel to departments

    private final JTextField codeF = field(20), nameF = field(20), batchF = field(20);
    private final JRadioButton theory = radio("Theory", true), practical = radio("Practical", false);
    private final JRadioButton gpa = radio("GPA", true), ngpa = radio("NGPA", false);
    private final JComboBox<String> lecturerCb = new JComboBox<>();

    // the four code indicators
    private final JComboBox<String> deptCb = new JComboBox<>();
    private final JComboBox<String> yearCb = combo(YEARS);
    private final JComboBox<String> semCb = combo(SEMESTERS);
    private final JComboBox<String> creditsCb = combo(CREDITS);
    private final JLabel deptCap = caption(), yearCap = caption(), semCap = caption(), creditsCap = caption();
    private final Segment[] segments = {
            new Segment("Dept", BRAND), new Segment("Year", OK), new Segment("Sem", WARN),
            new Segment("No.", INK_2), new Segment("Credits", BRAND_LT)};
    private boolean syncing;   // true while code <-> indicators are being copied, to stop echo

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
        lecturerCb.setFont(font(Font.PLAIN, 14));
        deptCb.addItem(NONE);
        for (Department d : departments) {
            deptCodes.add(deptCode(d));
            deptCb.addItem(deptCode(d));
        }

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

        JPanel grid = new JPanel(new GridLayout(0, 2, 20, 10));
        grid.setOpaque(false);
        grid.add(labelled("Course Code", hint(codeF, "e.g. ICT1132 = ICT · 1st year · 1st semester · course 3 · 2 credits")));
        grid.add(labelled("Course Name", hint(nameF, "e.g. Object Oriented Programming")));
        grid.add(labelled("Course Type", radios(theory, practical)));
        grid.add(labelled("Lecturer", lecturerCb));
        grid.add(labelled("Batch", hint(batchF, "e.g. 2023/2024")));
        grid.add(labelled("GPA / NGPA", radios(gpa, ngpa)));

        JPanel p = column();
        p.add(left(grid));
        p.add(Box.createVerticalStrut(16));
        p.add(left(inner(breakdown())));
        return p;
    }

    // "Course Code Breakdown": title + live segmented preview on top, the four drop-downs below
    private JComponent breakdown() {
        JLabel title = boldLabel("Course Code Breakdown");
        title.setFont(font(Font.BOLD, 15));
        JLabel sub = new JLabel("Pick parts to build the code, or type it.");
        sub.setFont(font(Font.PLAIN, 12));
        sub.setForeground(INK_2);

        JPanel text = column();
        text.add(left(title));
        text.add(Box.createVerticalStrut(3));
        text.add(left(sub));

        JPanel preview = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        preview.setOpaque(false);
        for (Segment s : segments) preview.add(s);

        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);
        head.add(text, BorderLayout.CENTER);
        head.add(preview, BorderLayout.EAST);

        JPanel strip = new JPanel(new GridLayout(1, 4, 14, 0));
        strip.setOpaque(false);
        strip.add(indicator("Department", deptCb, deptCap));
        strip.add(indicator("Academic Year", yearCb, yearCap));
        strip.add(indicator("Semester", semCb, semCap));
        strip.add(indicator("Credits", creditsCb, creditsCap));

        JPanel p = column();
        p.add(left(head));
        p.add(Box.createVerticalStrut(16));
        p.add(left(strip));
        return p;
    }

    // one indicator: small caps label, drop-down, caption with the meaning
    private static JComponent indicator(String name, JComboBox<String> cb, JLabel cap) {
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, cb.getPreferredSize().height));
        JPanel p = column();
        p.add(left(label(name.toUpperCase())));
        p.add(Box.createVerticalStrut(6));
        p.add(left(cb));
        p.add(Box.createVerticalStrut(6));
        p.add(left(cap));
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

        JPanel p = column();
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

    private static JLabel caption() {
        JLabel l = new JLabel("Not set");
        l.setFont(font(Font.PLAIN, 11));
        l.setForeground(INK_2);
        // fixed height, any width: long department names are cut with "..." (full name in the tooltip)
        l.setPreferredSize(new Dimension(10, 16));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        return l;
    }

    private static JComboBox<String> combo(String[] items) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(NONE);
        for (String s : items) cb.addItem(s);
        return cb;
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

    // One piece of the code preview: tinted rounded value with a tiny name under it ("?" when missing)
    private static final class Segment extends JPanel {
        private final JLabel value = new JLabel("?", SwingConstants.CENTER);
        private final Color color;

        Segment(String name, Color color) {
            this.color = color;
            setOpaque(false);
            setLayout(new BorderLayout(0, 3));
            value.setFont(font(Font.BOLD, 15));
            value.setForeground(INK_2);
            value.setBorder(new EmptyBorder(4, 12, 4, 12));
            JLabel cap = new JLabel(name, SwingConstants.CENTER);
            cap.setFont(font(Font.PLAIN, 10));
            cap.setForeground(INK_2);
            add(value, BorderLayout.CENTER);
            add(cap, BorderLayout.SOUTH);
        }

        void set(String s) {
            boolean empty = s == null || s.isEmpty();
            value.setText(empty ? "?" : s);
            value.setForeground(empty ? INK_2 : color);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = Glass.aa(g);
            Rectangle r = value.getBounds();
            g2.setColor(Glass.alpha(value.getForeground(), 34));
            g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 12, 12));
            g2.dispose();
        }
    }

    // -----------------------------------------------------------------
    //  Code <-> indicators
    // -----------------------------------------------------------------

    private void wire() {
        SimpleDocListener doc = new SimpleDocListener(this::recalc);
        for (JTextField f : new JTextField[]{nameF, batchF, endF, caF}) f.getDocument().addDocumentListener(doc);
        codeF.getDocument().addDocumentListener(new SimpleDocListener(this::codeTyped));
        for (int i = 0; i < CA_NAMES.length; i++) {
            final int k = i;
            caParts[i].getDocument().addDocumentListener(doc);
            caBox[i].addActionListener(e -> {
                if (!caBox[k].isSelected()) caParts[k].setText("0");
                recalc();
            });
        }
        lecturerCb.addActionListener(e -> recalc());
        for (JComboBox<String> cb : List.of(deptCb, yearCb, semCb, creditsCb))
            cb.addActionListener(e -> indicatorPicked(cb));
    }

    private String[] parts() {   // [prefix, digits] of what is typed
        Matcher m = CODE.matcher(codeF.getText().trim());
        return m.matches() ? new String[]{m.group(1).toUpperCase(), m.group(2)} : new String[]{"", ""};
    }

    private static int digit(String digits, int pos) {
        return digits.length() > pos ? digits.charAt(pos) - '0' : -1;
    }

    // typed code -> re-select the four indicators
    private void codeTyped() {
        if (syncing) return;
        syncing = true;
        try {
            selectFromCode();
        } finally {
            syncing = false;
        }
        recalc();
    }

    private void selectFromCode() {
        String[] p = parts();
        deptCb.setSelectedIndex(deptCodes.indexOf(p[0]) + 1);
        int y = digit(p[1], YEAR), s = digit(p[1], SEM), c = digit(p[1], CREDIT);
        yearCb.setSelectedIndex(y >= 1 && y <= YEARS.length ? y : 0);
        semCb.setSelectedIndex(s >= 1 && s <= SEMESTERS.length ? s : 0);
        creditsCb.setSelectedIndex(c >= 0 && c < CREDITS.length ? c + 1 : 0);
    }

    // indicator picked -> rewrite the code, then re-read it so every indicator agrees with it
    private void indicatorPicked(JComboBox<String> source) {
        if (syncing) return;
        syncing = true;
        try {
            codeF.setText(buildCode(source.getSelectedIndex() > 0));
            selectFromCode();
        } finally {
            syncing = false;
        }
        recalc();
    }

    // The digits read left to right, so a later part needs the earlier ones. When a part is picked
    // (fill = true) any missing earlier part defaults to 1; when one is cleared the parts after it go.
    private String buildCode(boolean fill) {
        String digits = parts()[1];
        String no = digits.length() > NO ? digits.substring(NO, NO + 1) : "";
        int year = yearCb.getSelectedIndex(), sem = semCb.getSelectedIndex(), credits = creditsCb.getSelectedIndex() - 1;
        if (fill) {
            if (credits >= 0 && no.isEmpty()) no = DEFAULT_NO;
            if (!no.isEmpty() && sem == 0) sem = 1;
            if (sem > 0 && year == 0) year = 1;
        }

        StringBuilder sb = new StringBuilder(deptCb.getSelectedIndex() > 0 ? deptCodes.get(deptCb.getSelectedIndex() - 1) : "");
        if (year == 0) return sb.toString();
        sb.append(year);
        if (sem == 0) return sb.toString();
        sb.append(sem).append(no);
        if (credits >= 0) sb.append(credits);
        return sb.toString();
    }

    // preview chips + captions under the drop-downs
    private void refreshIndicators() {
        String[] p = parts();
        segments[0].set(p[0]);
        for (int i = 0; i < 4; i++) segments[i + 1].set(digit(p[1], i) < 0 ? "" : p[1].substring(i, i + 1));

        int d = deptCb.getSelectedIndex();
        String deptName = d > 0 ? departments.get(d - 1).getDep_name() : null;
        boolean deptMismatch = d > 0 && !p[0].isEmpty() && !p[0].equals(deptCodes.get(d - 1));
        caption(deptCap, deptMismatch ? "Code prefix " + p[0] + " is not a department" : deptName, deptMismatch);
        caption(yearCap, yearCb.getSelectedIndex() > 0 ? "1st digit = " + yearCb.getSelectedIndex() : null, false);
        caption(semCap, semCb.getSelectedIndex() > 0 ? "2nd digit = " + semCb.getSelectedIndex() : null, false);
        int credits = creditsCb.getSelectedIndex() - 1, codeCredits = digit(p[1], CREDIT);
        boolean creditMismatch = credits >= 0 && codeCredits >= 0 && credits != codeCredits;
        caption(creditsCap, creditMismatch ? "Code says " + codeCredits + ", stored " + credits
                : credits >= 0 ? "Last digit = " + credits : null, creditMismatch);
    }

    private static void caption(JLabel l, String text, boolean warn) {
        l.setText(text == null ? "Not set" : text);
        l.setToolTipText(text);
        l.setForeground(warn ? WARN : INK_2);
    }

    // -----------------------------------------------------------------
    //  Live validation
    // -----------------------------------------------------------------

    private static int num(JTextField f) {
        try {
            int v = Integer.parseInt(f.getText().trim());
            return v < 0 ? -1 : v;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void recalc() {
        refreshIndicators();
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

        boolean codeOk = FULL_CODE.matcher(codeF.getText().trim().toUpperCase()).matches()
                && deptCb.getSelectedIndex() > 0 && yearCb.getSelectedIndex() > 0
                && semCb.getSelectedIndex() > 0 && creditsCb.getSelectedIndex() > 0;
        boolean filled = codeOk && !nameF.getText().isBlank() && !batchF.getText().isBlank();
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

    /** "Information and Communication Technology" -> ICT, "Bio-system Technology" -> BST. */
    public static String deptCode(Department d) {
        StringBuilder sb = new StringBuilder();
        for (String w : d.getDep_name().split("[\\s-]+"))
            if (!w.isEmpty() && !w.equalsIgnoreCase("and")) sb.append(Character.toUpperCase(w.charAt(0)));
        return sb.toString();
    }

    /** Copies the form into the given course (a fresh one when editing a new course). */
    public Course fill(Course c) {
        c.setCourse_code(codeF.getText().trim().toUpperCase());
        c.setCourse_name(nameF.getText().trim());
        c.setCredit(creditsCb.getSelectedIndex() - 1);
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
        syncing = true;
        try {
            codeF.setText(c.getCourse_code() == null ? "" : c.getCourse_code());
            selectFromCode();
            // the stored department and credits win over what the code says (older rows may differ);
            // the caption flags the difference and the code is rewritten once a part is picked
            for (int i = 0; i < departments.size(); i++)
                if (c.getDep_id() != null && departments.get(i).getDep_id() == c.getDep_id())
                    deptCb.setSelectedIndex(i + 1);
            int credits = (int) Math.round(c.getCredit());
            if (credits >= 0 && credits < CREDITS.length) creditsCb.setSelectedIndex(credits + 1);
        } finally {
            syncing = false;
        }
        nameF.setText(c.getCourse_name());
        ("Practical".equals(c.getCourse_type()) ? practical : theory).setSelected(true);
        lecturerCb.setSelectedIndex(0);
        for (int i = 0; i < lecturers.size(); i++)
            if (c.getLecturer_id() != null && lecturers.get(i).getUser_id() == c.getLecturer_id())
                lecturerCb.setSelectedIndex(i + 1);
        batchF.setText(c.getBatch() == null ? "" : c.getBatch());
        ("NGPA".equals(c.getGpa_type()) ? ngpa : gpa).setSelected(true);
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
        syncing = true;
        try {
            codeF.setText("");
            selectFromCode();
        } finally {
            syncing = false;
        }
        for (JTextField f : new JTextField[]{nameF, batchF}) f.setText("");
        theory.setSelected(true);
        gpa.setSelected(true);
        lecturerCb.setSelectedIndex(0);
        endF.setText("0");
        caF.setText("0");
        for (int i = 0; i < CA_NAMES.length; i++) {
            caBox[i].setSelected(true);
            caParts[i].setText("0");
        }
        recalc();
    }
}
