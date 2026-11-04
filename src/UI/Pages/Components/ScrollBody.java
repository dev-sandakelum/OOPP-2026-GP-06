package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;

// Panel for scroll panes: follows the viewport width, grows in height.
public class ScrollBody extends JPanel implements Scrollable {

    public ScrollBody() {
        super(new BorderLayout());
        setOpaque(false);
    }

    // Children (e.g. read-only JTextAreas used as wrapped labels) ask to be scrolled into
    // view when their caret moves, which drags a freshly built page away from the top.
    // Only honour such requests while the keyboard focus is inside this body.
    @Override
    public void scrollRectToVisible(Rectangle r) {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focus == null || !SwingUtilities.isDescendingFrom(focus, this)) return;
        super.scrollRectToVisible(r);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
