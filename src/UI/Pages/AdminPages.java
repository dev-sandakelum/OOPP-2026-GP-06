package UI.Pages;

import UI.Kit.FrameData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;

public class AdminPages extends JFrame implements FrameData{
    private final String[] tabs = {"Overview", "User Profiles", "Courses", "Notices", "Timetables", "My Profile"};

    public AdminPages(){
        setSize(width ,height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(width , height));

        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/assets/img.png"))).getImage());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sideBar(), BorderLayout.WEST);
        add(container());
//        this.add(sideBar());

    }
    public JPanel sideBar(){
        JPanel sp = new JPanel(new BorderLayout());
        sp.setPreferredSize(new Dimension(sp_width , 0));
        sp.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        sp.add(logoSection() ,BorderLayout.NORTH);
        sp.add(navSection());
        sp.add(accountSection() ,BorderLayout.SOUTH);
        return sp;
    }
    public JPanel logoSection(){
        JPanel lp = new JPanel(new BorderLayout());
        lp.setPreferredSize(new Dimension(sp_width, lp_height));
        lp.setBackground(new Color(221, 157, 17));
        return lp;
    }
    public JPanel navSection(){
        JPanel np = new JPanel();
        np.setLayout(new BoxLayout(np ,BoxLayout.Y_AXIS));
        np.setPreferredSize(new Dimension(0 , 0));
        np.setBackground(new Color(241, 240, 234));
        np.add(navItem(adminTabs[0],null , true ));
        np.add(Box.createVerticalStrut(4));
        np.add(navItem(adminTabs[1],null , false ));
        np.add(Box.createVerticalStrut(4));
        np.add(navItem(adminTabs[2],null , false ));
        np.add(Box.createVerticalStrut(4));
        np.add(navItem(adminTabs[3],null , false ));
        return np;
    }
    public JPanel accountSection(){
        JPanel ap = new JPanel(new BorderLayout());
        ap.setPreferredSize(new Dimension(sp_width , lp_height));
        ap.setBackground(new Color(61, 17, 221));
        return ap;
    }

    public JButton navItem(String text, String icon, boolean selected) {

        JButton item = new JButton();

        item.setLayout(new BorderLayout(12, 0));
        item.setPreferredSize(new Dimension(260, 42));
        item.setMaximumSize(new Dimension(260, 42));

        item.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        item.setForeground(selected
                ? new Color(30, 30, 30)
                : new Color(110, 105, 95));

        item.setBackground(selected ? Color.WHITE : new Color(243, 242, 237));

        item.setFocusPainted(false);
        item.setBorderPainted(false);
        item.setContentAreaFilled(false);
        item.setOpaque(false);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 19));
        iconLabel.setForeground(selected
                ? new Color(35, 35, 35)
                : new Color(125, 120, 108));

        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        textLabel.setForeground(item.getForeground());

        JPanel content = new JPanel(new FlowLayout(
                FlowLayout.LEFT, 0, 0
        ));
        content.setOpaque(false);
        content.add(iconLabel);
        content.add(Box.createHorizontalStrut(14));
        content.add(textLabel);
        content.setBorder(new EmptyBorder(0, 22, 0, 0));

        item.add(content, BorderLayout.CENTER);

        // Custom rounded background and selected indicator
        item = new JButton() {
            {
                setLayout(new BorderLayout());
                add(content, BorderLayout.CENTER);
                setPreferredSize(new Dimension(260, 42));
                setMaximumSize(new Dimension(260, 42));
                setBorderPainted(false);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setOpaque(false);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON
                );

                if (selected) {
                    g2.setColor(Color.WHITE);
                    g2.fill(new RoundRectangle2D.Float(
                            0, 0, getWidth(), getHeight(), 28, 28
                    ));

                    g2.setColor(new Color(215, 85, 55));
                    g2.fillRoundRect(8, 11, 3, 20, 3, 3);
                }

                g2.dispose();
                super.paintComponent(g);
            }
        };

        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setAlignmentX(Component.LEFT_ALIGNMENT);

        return item;
    }
    public JScrollPane container(){
        JScrollPane sp = new JScrollPane();
        sp.setPreferredSize(new Dimension(0 ,6000));
        sp.getViewport().setBackground(new Color(202, 253, 235));
        return sp;
    }


}
