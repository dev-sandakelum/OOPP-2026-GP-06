package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static UI.Kit.Palette.*;

// Shows a JOptionPane the way the login spinner is shown: a frosted wash over the parent
// window with one glass card in the middle. There is no title bar, so the only way out is
// one of the pane's buttons (or Esc, which counts as Cancel / close).
//
// Implemented as an undecorated, translucent, modal JDialog that covers the parent window's
// content area, so the usual blocking "show and return the answer" style still works.
public final class GlassDialog {
    private GlassDialog() { }

    /** Client property set on the pane so GlassOptionPaneUI paints only the card. */
    public static final String OVERLAY = "glass.overlay";

    private static final Color WASH = Glass.alpha(BG_2, 150);

    /** Shows the pane over the parent's window, blocks until it is closed, and returns pane.getValue(). */
    public static Object show(Component parent, String title, JOptionPane pane) {
        pane.putClientProperty(OVERLAY, Boolean.TRUE);
        if (title != null && !title.isBlank()) pane.setMessage(withHeading(title, pane.getMessage()));

        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        boolean translucent = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);

        JPanel wash = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(translucent ? WASH : BG_2);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wash.setOpaque(false);
        wash.add(pane);
        dialog.setContentPane(wash);
        if (translucent) dialog.setBackground(new Color(0, 0, 0, 0));

        dialog.setBounds(area(parent, owner, pane));

        // close as soon as a button sets the pane's value (same rule JOptionPane.createDialog uses)
        pane.addPropertyChangeListener(e -> {
            if (dialog.isVisible() && e.getSource() == pane
                    && JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName())
                    && e.getNewValue() != null && e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                dialog.dispose();
            }
        });
        dialog.addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { pane.selectInitialValue(); }
        });

        dialog.setVisible(true);          // blocks until disposed
        return pane.getValue();
    }

    /** The parent window's content area, or a box centred on the screen when there is no parent. */
    private static Rectangle area(Component parent, Window owner, JOptionPane pane) {
        try {
            JRootPane root = parent == null ? null : SwingUtilities.getRootPane(parent);
            if (root != null && root.isShowing()) {
                return new Rectangle(root.getLocationOnScreen(), root.getSize());
            }
            if (owner != null && owner.isShowing()) return owner.getBounds();
        } catch (IllegalComponentStateException ignored) { }

        Dimension d = pane.getPreferredSize();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int w = d.width + 120, h = d.height + 120;
        return new Rectangle(screen.x + (screen.width - w) / 2, screen.y + (screen.height - h) / 2, w, h);
    }

    /** Puts a bold heading above the message (which may be a String or a Component). */
    private static JComponent withHeading(String title, Object message) {
        JLabel heading = new JLabel(title);
        heading.setFont(font(Font.BOLD, 17));
        heading.setForeground(INK);

        JComponent body;
        if (message instanceof Component c) {
            body = c instanceof JComponent jc ? jc : wrap(c);
        } else {
            JPanel lines = new JPanel();
            lines.setOpaque(false);
            lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
            for (String line : String.valueOf(message).split("\n")) {
                JLabel l = new JLabel(line);
                l.setFont(font(Font.PLAIN, 14));
                l.setForeground(INK);
                l.setAlignmentX(Component.LEFT_ALIGNMENT);
                lines.add(l);
            }
            body = lines;
        }

        JPanel p = new JPanel(new BorderLayout(0, 14));
        p.setOpaque(false);
        p.add(heading, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private static JComponent wrap(Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        return p;
    }
}
