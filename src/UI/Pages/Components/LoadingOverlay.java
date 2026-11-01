package UI.Pages.Components;

import UI.Kit.Glass;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;

import static UI.Kit.Palette.*;

// Frosted wash over a whole window with a small glass card in the middle holding a spinner
// and a message. Install it as the window's glass pane; while it is showing it swallows
// mouse and keyboard input so the page underneath cannot be used.
//
//   LoadingOverlay overlay = LoadingOverlay.install(frame);
//   overlay.start("Signing in…");
//   overlay.finish("Welcome, Nimal", "Opening…");   // tick + message
//   overlay.dismiss();
public class LoadingOverlay extends JComponent {

    private static final int MIN_WIDTH = 240;   // of the text area inside the card

    private final Spinner spinner = new Spinner(44);
    private final JLabel message = new JLabel("", SwingConstants.CENTER);
    private final JLabel detail  = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel card = new JPanel(new BorderLayout(0, 14)) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = Glass.aa(g);
            float w = getWidth() - Card.INSET_X * 2, h = getHeight() - Card.INSET_TOP - Card.INSET_BOTTOM;
            Glass.shadow(g2, Card.INSET_X, Card.INSET_TOP, w, h, RADIUS_CARD);
            Glass.panel(g2, Card.INSET_X, Card.INSET_TOP, w, h, RADIUS_CARD, GLASS_STRONG);
            g2.dispose();
        }
    };

    public static LoadingOverlay install(RootPaneContainer window) {
        LoadingOverlay o = new LoadingOverlay();
        window.setGlassPane(o);
        return o;
    }

    private LoadingOverlay() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setVisible(false);

        // eat every event so the page underneath is frozen while loading
        addMouseListener(new MouseAdapter() { });
        addMouseMotionListener(new MouseAdapter() { });
        addKeyListener(new KeyAdapter() { });
        setFocusable(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        message.setFont(font(Font.BOLD, 15));
        message.setForeground(INK);
        detail.setFont(font(Font.PLAIN, 12));
        detail.setForeground(INK_2);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        message.setAlignmentX(CENTER_ALIGNMENT);
        detail.setAlignmentX(CENTER_ALIGNMENT);
        text.add(message);
        text.add(Box.createVerticalStrut(4));
        text.add(detail);

        JPanel spinnerWrap = new JPanel(new GridBagLayout());
        spinnerWrap.setOpaque(false);
        spinnerWrap.add(spinner);

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(28, 44, 30, 44));
        card.add(spinnerWrap, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        card.add(Box.createHorizontalStrut(MIN_WIDTH), BorderLayout.SOUTH);   // stable width between messages
        add(card);
    }

    /** Shows the overlay with a spinning indicator. */
    public void start(String text) {
        start(text, " ");
    }

    public void start(String text, String subText) {
        spinner.setDone(false);
        message.setText(text);
        detail.setText(subText == null || subText.isBlank() ? " " : subText);
        setVisible(true);
        requestFocusInWindow();
        revalidate();
        repaint();
    }

    /** Swaps the spinner for a tick and updates the text (the overlay stays up). */
    public void finish(String text, String subText) {
        spinner.setDone(true);
        message.setText(text);
        detail.setText(subText == null || subText.isBlank() ? " " : subText);
        setCursor(Cursor.getDefaultCursor());
        revalidate();
        repaint();
    }

    public void dismiss() {
        setVisible(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Glass.aa(g);
        g2.setColor(new Color(BG_2.getRed(), BG_2.getGreen(), BG_2.getBlue(), 150));   // frosted wash
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}
