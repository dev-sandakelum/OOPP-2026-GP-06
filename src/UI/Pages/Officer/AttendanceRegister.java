package UI.Pages.Officer;

import UI.Pages.Components.Page;

import javax.swing.*;
import java.awt.*;

public class AttendanceRegister extends Page{
    public AttendanceRegister() {
        super("Attendance Register" ,"ffffgg" ,null);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(new JLabel("Attendance Register"));
        setScrollBody(body);
    }
}
