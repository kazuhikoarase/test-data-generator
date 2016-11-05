package dataman.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import dataman.model.ColumnDef;
import dataman.model.DataObject;
import dataman.model.DataObjectRow;
import dataman.model.DataTableModel;
import dataman.model.SqlName;

/**
 * DataTableCellRenderer
 * @author kazuhiko arase
 */
@SuppressWarnings("serial")
public class DataTableCellRenderer extends DefaultTableCellRenderer {

  public DataTableCellRenderer() {
  }

  private void setupLabel(
      JLabel label,
      ColumnDef columnDef,
      DataObject value,
      boolean isSelected
  ) {

    label.setToolTipText(columnDef.getTypeDesc() );

    if (value.getValue() instanceof Number) {
      label.setHorizontalAlignment(JLabel.RIGHT);
    } else {
      label.setHorizontalAlignment(JLabel.LEFT);
    }

    if (value.getType() == DataObject.TYPE_GENERATED) {
      Color c = Styles.getGeneratedColor(value);
      if (!isSelected) {
        label.setBackground(c);
      } else {
        label.setForeground(c);
      } 
    }
    if (value.getValue() == null) {
      label.setForeground(Styles.getNullFgColor() );
    } else if (value.getValue() instanceof SqlName) {
      label.setForeground(Styles.getSqlNameFgColor() );
    }

    if (value.getType() == DataObject.TYPE_USER) {
      label.setForeground(Styles.getUserFgColor() );
    }
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table,
      Object o,
      boolean isSelected,
      boolean hasFocus,
      int rowIndex,
      int columnIndex
  ) {

    setForeground(Color.BLACK);
    setBackground(Color.WHITE);
    setHorizontalAlignment(JLabel.LEFT);
    setToolTipText(null);

    JLabel label = (JLabel)super.getTableCellRendererComponent(
        table, o, isSelected, hasFocus, rowIndex, columnIndex);

    DataTableModel model = (DataTableModel)table.getModel();
    DataObjectRow row = model.getDataRowAt(rowIndex);
    if (columnIndex < model.getNumColumnExtras() ) {
      if (columnIndex == 0) {
        label.setHorizontalAlignment(JLabel.RIGHT);
      } else if (columnIndex == 1) {
      }
    } else {
      DataObject value = row.get(columnIndex - model.getNumColumnExtras() );
      ColumnDef columnDef = model.getTableDef().getColumns().
          get(columnIndex - model.getNumColumnExtras() );
      setupLabel(label, columnDef, value, isSelected);
    }

    if (row.isDeleted() ) {
      label.setForeground(Styles.getDeletedFgColor() );
      label.setBackground(Styles.getDeletedBgColor() );
    }

    return label;
  }
}
