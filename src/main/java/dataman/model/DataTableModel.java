package dataman.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import dataman.core.Util;

/**
 * DataTableModel
 * @author kazuhiko arase
 */
public class DataTableModel implements TableModel {

  private TableDef tableDef;

  private List<DataObjectRow> data;

  private List<TableModelListener> listeners =
      new ArrayList<TableModelListener>();

  private String[] columnExtras = { "", "" };

  public DataTableModel(
    TableDef tableDef,
    List<DataObjectRow> data
  ) {
    this.tableDef = tableDef;
    this.data = data;
  }

  public int getNumColumnExtras() {
    return columnExtras.length;
  }

  public TableDef getTableDef() {
    return tableDef;
  }

  public DataObjectRow getDataRowAt(int row) {
    return data.get(row);
  }

  @Override
  public String getColumnName(int columnIndex) {
    if (columnIndex < columnExtras.length) {
      return columnExtras[columnIndex];
    } else {
      return tableDef.getColumns().
          get(columnIndex - columnExtras.length).getColumnName();
    }
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return Object.class;
  }

  @Override
  public int getColumnCount() {
    return columnExtras.length + tableDef.getColumns().size();
  }

  @Override
  public int getRowCount() {
    return data.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex < columnExtras.length) {
      if (columnIndex == 0) {
        return Integer.valueOf(rowIndex + 1);
      } else if (columnIndex == 1) {
        String cmt = Util.trim(getDataRowAt(rowIndex).getComment() );
        return Util.isEmpty(cmt)? null : cmt;
      }
      return "@";
    } else {
      Object value = getDataRowAt(rowIndex).
          get(columnIndex - columnExtras.length).getValue();
      return (value != null)? value : "(null)";
    }
  }

  @Override
  public void setValueAt(Object value, int rowIndex, int columnIndex) {
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    listeners.remove(l);
  }
}
