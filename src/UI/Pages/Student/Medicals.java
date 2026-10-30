package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static UI.Kit.Palette.*;

public class Medicals extends Page {

    // ---------- Medical Records Data ----------
    // Format: {Date, Status, Title, Modules (comma separated), Footer info}
    private final List<String[]> medicals = new ArrayList<>(List.of(
            new String[]{
                    "2026-08-24",
                    "Approved",
                    "Migraine — clinic observation",
                    "ICT2131-T#12,ICT2132-T#12,ICT2133-T#12,ICT2134-T#15",
                    "leave 24 Aug - 15 Sep · submitted 16 Sep · med_ug01_1.pdf"
            },
            new String[]{
                    "2026-06-29",
                    "Approved",
                    "Fractured wrist — follow-up",
                    "ICT2135-T#07,ICT2135-P#13,ICT2136-T#04",
                    "leave 29 Jun - 01 Sep · submitted 02 Sep · med_ug01_2.pdf"
            }
    ));

    public Medicals(User user) {
        super("Medicals", "View your medical leave submissions and approvals", null);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (String[] m : medicals) {
            list.add(medicalCard(m));
            list.add(Box.createVerticalStrut(15));
        }

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 0, 0));
        body.add(list, BorderLayout.NORTH);

        setScrollBody(body);
    }

    private JComponent medicalCard(String[] m) {
        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 25, 18, 25));

        // 1. Date Block (Left Side)
        LocalDate date = LocalDate.parse(m[0]);
        String day = String.format("%02d", date.getDayOfMonth());
        String month = date.getMonth().name().substring(0, 3).toUpperCase();

        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);

        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 22));
        dayLbl.setForeground(new Color(2, 48, 71));

        JLabel monthLbl = new JLabel(month, SwingConstants.CENTER);
        monthLbl.setFont(font(Font.PLAIN, 11));
        monthLbl.setForeground(Color.GRAY);

        dateBlock.add(dayLbl);
        dateBlock.add(monthLbl);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);
        dateContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(235, 235, 235)),
                new EmptyBorder(0, 5, 0, 25)
        ));
        dateContainer.add(dateBlock, BorderLayout.NORTH);

        // 2. Info Block (Right Side)
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(new BoxLayout(infoBlock, BoxLayout.Y_AXIS));
        infoBlock.setOpaque(false);
        infoBlock.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Top Row: Status Chip + Title
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        Color approvedColor = new Color(46, 125, 50); // Green
        titleRow.add(new Chip("● " + m[1], approvedColor)); // Dot added to match design

        JLabel titleLbl = new JLabel(m[2]);
        titleLbl.setFont(font(Font.BOLD, 15));
        titleLbl.setForeground(Color.BLACK);
        titleRow.add(titleLbl);

        // Middle Row: Module Chips
        JPanel modulesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        modulesRow.setOpaque(false);
        modulesRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        modulesRow.setBorder(new EmptyBorder(8, 0, 8, 0));

        String[] modules = m[3].split(",");
        for (String mod : modules) {
            modulesRow.add(new Chip(mod.trim(), Color.GRAY)); // Custom background
        }

        // Bottom Row: Footer Details
        JLabel footerLbl = new JLabel(m[4]);
        footerLbl.setFont(font(Font.PLAIN, 12));
        footerLbl.setForeground(Color.GRAY);
        footerLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerLbl.setBorder(new EmptyBorder(0, 5, 0, 0));

        infoBlock.add(titleRow);
        infoBlock.add(modulesRow);
        infoBlock.add(footerLbl);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(infoBlock, BorderLayout.CENTER);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        return card;
    }
}