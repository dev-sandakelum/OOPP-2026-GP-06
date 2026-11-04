package UI.Pages.Components;

import javax.swing.table.DefaultTableModel;

// Table model whose cells cannot be edited by typing.
public class ReadOnlyTableModel extends DefaultTableModel {

    public ReadOnlyTableModel(String[] columns) {
        super(columns, 0);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
