package UI.Pages.Components;

import java.awt.*;
import java.awt.geom.*;

// Line icons drawn in code. Each icon is designed in a 20x20 box.
// Roles: ADM = Admin, LEC = Lecturer, TO = Technical Officer, UG = Undergraduate.
public enum NavIcon {

    // ---------- Shared by several roles ----------
    GRID,         // Overview                                   | ADM, LEC, TO, UG
    BOOK,         // Courses / My courses                       | ADM, LEC, UG
    MEGA,         // Notices                                    | ADM, LEC, TO, UG
    CAL,          // Timetables                                 | ADM, TO, UG
    USER,         // My Profile                                 | ADM, LEC, TO, UG
    MEDICAL,      // Medicals                                   | LEC, TO, UG
    ATTENDANCE,   // Attendance summary / My attendance         | LEC, UG
    GRADES,       // Grades & GPA / Marks & GPA / Results       | LEC, UG

    // ---------- Single-role icons ----------
    USERS,        // User Profiles (CRUD)                       | ADM only
    MARKS,        // Marks: CA / Final / Summary                | LEC only
    STUDENTS,     // Undergraduates directory                   | LEC only
    REGISTER,     // Attendance register (take P/M/A)           | TO only

    // ---------- Not a nav tab ----------
    LOGOUT;       // Logout button on the account card          | ADM, LEC, TO, UG

    public static final int SIZE = 20;

    // Draws this icon with its top-left corner at (x, y).
    public void draw(Graphics2D base, int x, int y, Color c) {
        Graphics2D g = (Graphics2D) base.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.translate(x, y);
        g.setColor(c);
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        switch (this) {
            case GRID -> {
                g.draw(new RoundRectangle2D.Float(2, 2, 6, 8, 2, 2));
                g.draw(new RoundRectangle2D.Float(12, 2, 6, 5, 2, 2));
                g.draw(new RoundRectangle2D.Float(2, 13, 6, 5, 2, 2));
                g.draw(new RoundRectangle2D.Float(12, 10, 6, 8, 2, 2));
            }
            case USERS -> {
                g.draw(new Ellipse2D.Float(4, 3, 6, 6));
                g.draw(new Arc2D.Float(1.5f, 12, 11, 10, 0, 180, Arc2D.OPEN));
                g.draw(new Arc2D.Float(11, 3, 6, 6, 90, -180, Arc2D.OPEN));
                g.draw(new Arc2D.Float(12, 12, 7, 8, 90, -90, Arc2D.OPEN));
            }
            case BOOK -> {
                Path2D p = new Path2D.Float();
                p.moveTo(10, 5);
                p.curveTo(8, 3, 4, 3, 2, 4);
                p.lineTo(2, 16);
                p.curveTo(4, 15, 8, 15, 10, 17);
                p.lineTo(10, 5);
                p.curveTo(12, 3, 16, 3, 18, 4);
                p.lineTo(18, 16);
                p.curveTo(16, 15, 12, 15, 10, 17);
                g.draw(p);
            }
            case MEGA -> {
                Path2D p = new Path2D.Float();
                p.moveTo(2, 8);
                p.lineTo(6, 8);
                p.lineTo(15, 3);
                p.lineTo(15, 15);
                p.lineTo(6, 12);
                p.lineTo(2, 12);
                p.closePath();
                g.draw(p);
                Path2D handle = new Path2D.Float();
                handle.moveTo(5, 12);
                handle.lineTo(6, 17);
                handle.lineTo(9, 17);
                handle.lineTo(8, 13);
                g.draw(handle);
                g.draw(new Line2D.Float(18, 7, 18, 11));
            }
            case CAL -> {
                g.draw(new RoundRectangle2D.Float(2, 4, 16, 14, 4, 4));
                g.draw(new Line2D.Float(2, 8, 18, 8));
                g.draw(new Line2D.Float(6, 2, 6, 5));
                g.draw(new Line2D.Float(14, 2, 14, 5));
                float[][] dots = {{6, 11}, {10, 11}, {14, 11}, {6, 14}, {10, 14}};
                for (float[] d : dots) {
                    g.fill(new Ellipse2D.Float(d[0] - 0.8f, d[1] - 0.8f, 1.6f, 1.6f));
                }
            }
            case USER -> {
                g.draw(new Ellipse2D.Float(6, 2.5f, 8, 8));
                g.draw(new Arc2D.Float(3, 13, 14, 10, 0, 180, Arc2D.OPEN));
            }
            case MARKS -> {   // document with text lines
                Path2D p = new Path2D.Float();
                p.moveTo(5, 2);
                p.lineTo(12, 2);
                p.lineTo(16, 6);
                p.lineTo(16, 18);
                p.lineTo(5, 18);
                p.closePath();
                g.draw(p);
                Path2D fold = new Path2D.Float();
                fold.moveTo(12, 2);
                fold.lineTo(12, 6);
                fold.lineTo(16, 6);
                g.draw(fold);
                g.draw(new Line2D.Float(8, 10, 13, 10));
                g.draw(new Line2D.Float(8, 13.5f, 13, 13.5f));
            }
            case STUDENTS -> {   // graduation cap
                Path2D top = new Path2D.Float();
                top.moveTo(10, 3);
                top.lineTo(18, 7);
                top.lineTo(10, 11);
                top.lineTo(2, 7);
                top.closePath();
                g.draw(top);
                Path2D base2 = new Path2D.Float();
                base2.moveTo(5, 9.5f);
                base2.lineTo(5, 13);
                base2.curveTo(5, 15.5f, 15, 15.5f, 15, 13);
                base2.lineTo(15, 9.5f);
                g.draw(base2);
                g.draw(new Line2D.Float(18, 7, 18, 12));
            }
            case ATTENDANCE -> {   // person with a check mark
                g.draw(new Ellipse2D.Float(3.5f, 3, 7, 7));
                g.draw(new Arc2D.Float(1, 12, 12, 10, 0, 180, Arc2D.OPEN));
                Path2D check = new Path2D.Float();
                check.moveTo(12, 10);
                check.lineTo(14, 12);
                check.lineTo(18, 8);
                g.draw(check);
            }
            case REGISTER -> {   // clipboard with list
                g.draw(new RoundRectangle2D.Float(3, 4, 14, 14, 3, 3));
                g.draw(new RoundRectangle2D.Float(7, 2, 6, 4, 2, 2));
                g.fill(new Ellipse2D.Float(6.2f, 9.7f, 1.6f, 1.6f));
                g.fill(new Ellipse2D.Float(6.2f, 13.7f, 1.6f, 1.6f));
                g.draw(new Line2D.Float(9.5f, 10.5f, 13.5f, 10.5f));
                g.draw(new Line2D.Float(9.5f, 14.5f, 13.5f, 14.5f));
            }
            case GRADES -> {   // bar chart
                Path2D axes = new Path2D.Float();
                axes.moveTo(3, 3);
                axes.lineTo(3, 17);
                axes.lineTo(17, 17);
                g.draw(axes);
                g.draw(new Line2D.Float(7, 14, 7, 10));
                g.draw(new Line2D.Float(11, 14, 11, 6));
                g.draw(new Line2D.Float(15, 14, 15, 9));
            }
            case MEDICAL -> {   // medical cross
                Path2D p = new Path2D.Float();
                p.moveTo(8, 3);
                p.lineTo(12, 3);
                p.lineTo(12, 8);
                p.lineTo(17, 8);
                p.lineTo(17, 12);
                p.lineTo(12, 12);
                p.lineTo(12, 17);
                p.lineTo(8, 17);
                p.lineTo(8, 12);
                p.lineTo(3, 12);
                p.lineTo(3, 8);
                p.lineTo(8, 8);
                p.closePath();
                g.draw(p);
            }
            case LOGOUT -> {   // door bracket with arrow
                Path2D door = new Path2D.Float();
                door.moveTo(8, 3);
                door.lineTo(4, 3);
                door.lineTo(4, 17);
                door.lineTo(8, 17);
                g.draw(door);
                g.draw(new Line2D.Float(8, 10, 17, 10));
                Path2D head = new Path2D.Float();
                head.moveTo(14, 7);
                head.lineTo(17, 10);
                head.lineTo(14, 13);
                g.draw(head);
            }
        }
        g.dispose();
    }
}