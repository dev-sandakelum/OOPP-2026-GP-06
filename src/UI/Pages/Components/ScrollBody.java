package UI.Pages.Components;

import javax.swing.*;
import java.awt.*;

// Panel for scroll panes: follows the viewport width, grows in height.
public class ScrollBody extends JPanel implements Scrollable {

    public ScrollBody() {
        super(new BorderLayout());
        setOpaque(false);
    }

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
    @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
    @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
    @Override public boolean getScrollableTracksViewportWidth() { return true; }
    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
