package UI.Pages.Officer;

import UI.Pages.Components.Page;

import javax.swing.*;
import java.awt.*;

public class Medicals extends Page{
    public Medicals() {
        super("Medicals" ,"ffffgg" ,null);
        JPanel body = new JPanel(new BorderLayout(0, 20));
        body.setOpaque(false);
        body.add(new JLabel("Medicals"));
        setScrollBody(body);
    }
}
