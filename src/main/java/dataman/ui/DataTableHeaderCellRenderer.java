package dataman.ui;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import dataman.model.DataTableModel;
import dataman.model.TableDef;

/**
 * DataTableHeaderCellRenderer
 * @author kazuhiko arase
 */
public class DataTableHeaderCellRenderer
implements TableCellRenderer {

  private TableCellRenderer defaltRenderer;

  private TableDef tableDef;

  public DataTableHeaderCellRenderer(
      TableCellRenderer defaltRenderer, TableDef tableDef) {
    this.defaltRenderer = defaltRenderer;
    this.tableDef = tableDef;
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column
  ) {
    JComponent header = (JComponent)defaltRenderer.
        getTableCellRendererComponent(
            table, value, isSelected, hasFocus, row, column);

    DataTableModel model = (DataTableModel)table.getModel();
    if (column < model.getNumColumnExtras() ) {
      header.setForeground(Styles.getSysHeadColor() );
      header.setBackground(Styles.getSysHeadBgColor() );
    } else {
      if (tableDef.getColumns().
          get(column - model.getNumColumnExtras() ).isPrimaryKey() ) {
        header.setForeground(Styles.getPkColor() );
        header.setBackground(Styles.getPkBgColor() );
      }
    }

    return header;
  }
}
