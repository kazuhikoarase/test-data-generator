package dataman.ui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import dataman.core.Console;
import dataman.model.ColumnDef;
import dataman.model.DataObject;
import dataman.model.DataObjectRow;
import dataman.model.DataTableModel;
import dataman.model.TableDef;

/**
 * DataTableUI
 * @author kazuhiko arase
 */
@SuppressWarnings("serial")
public class DataTableUI {

  private final TableDef tableDef;

  private final List<DataObjectRow> values;

  private final Console infoConsole;

  private final Console snippetConsole;

  private final JScrollPane ui;

  private boolean hideDefaultValueColumns = false;

  public DataTableUI(
    final TableDef tableDef,
    final List<DataObjectRow> values,
    final boolean hideDefaultValueColumns,
    final Console infoConsole,
    final Console snippetConsole
  ) {
    this.tableDef = tableDef;
    this.values = values;
    this.hideDefaultValueColumns = hideDefaultValueColumns;
    this.infoConsole = infoConsole;
    this.snippetConsole = snippetConsole;

    ui = new JScrollPane(createTable() );
  }

  public TableDef getTableDef() {
    return tableDef;
  }

  public void setHideDefaultValueColumns(boolean b) {
    if (hideDefaultValueColumns != b) {
      hideDefaultValueColumns = b;
      ui.setViewportView(createTable() );
    }
  }

  public JComponent getUI() {
    return ui;
  }

  private JTable createTable() {

    if (!hideDefaultValueColumns) {
      return createTable(tableDef, values);
    }

    boolean[] visibleCols = new boolean[tableDef.getColumns().size()];
    Arrays.fill(visibleCols, false);
    for (DataObjectRow row : values) {
      for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
        if (tableDef.getUniqKeys().contains(
            tableDef.getColumns().get(i).getColumnName() ) ) {
          visibleCols[i] = true;
        } else if (row.get(i).getType() != DataObject.TYPE_DEFAULT) {
          visibleCols[i] = true;
        }
      }
    }

    TableDef subTableDef = new TableDef();
    subTableDef.setTableName(tableDef.getTableName() );
    subTableDef.getUniqKeys().addAll(tableDef.getUniqKeys() );
    List<DataObjectRow> subValues = new ArrayList<DataObjectRow>();
    for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
      if (visibleCols[i]) {
        subTableDef.getColumns().add(tableDef.getColumns().get(i) );
      }
    }

    for (DataObjectRow row : values) {

      DataObjectRow subRow = new DataObjectRow(
          subTableDef.getColumns().size() );
      subRow.setDeleted(row.isDeleted() );
      subRow.setComment(row.getComment() );

      int r = 0;
      for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
        if (visibleCols[i]) {
          subRow.set(r, row.get(i) );
          r += 1;
        }
      }
      subValues.add(subRow);
    }
    return createTable(subTableDef, subValues);
  }

  private JTable createTable(
    final TableDef tableDef,
    final List<DataObjectRow> values
  ) {
    final DataTableModel tableModel = new DataTableModel(tableDef, values);
    final JTable table = new JTable() {
      @Override
      public Component prepareRenderer(TableCellRenderer renderer,
            int row, int column) {
        Component comp = super.prepareRenderer(renderer, row, column);
        int rendererWidth = comp.getPreferredSize().width;
        TableColumn tableColumn = getColumnModel().getColumn(column);
        tableColumn.setPreferredWidth(Math.max(
            rendererWidth + getIntercellSpacing().width,
            tableColumn.getPreferredWidth() ) );
        return comp;
       }
    };
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    table.setModel(tableModel);
    table.setDefaultRenderer(Object.class, new DataTableCellRenderer() );
    table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setDefaultRenderer(
        new DataTableHeaderCellRenderer(
            table.getTableHeader().getDefaultRenderer(), tableDef) );
    table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
      
      @Override
      public void columnSelectionChanged(ListSelectionEvent e) {
        if (table.getSelectedColumn() != -1) {
          if (table.getSelectedColumn() < tableModel.getNumColumnExtras() ) {
            
          } else {
            ColumnDef columnDef =
                tableDef.getColumns().get(table.getSelectedColumn() -
                    tableModel.getNumColumnExtras() );
            infoConsole.clear();
            infoConsole.appendInfo(columnDef.getColumnName() );
            infoConsole.appendInfo(columnDef.getTypeDesc() );
          }
        }
      }
      @Override
      public void columnRemoved(TableColumnModelEvent e) {
      }
      @Override
      public void columnMoved(TableColumnModelEvent e) {
      }
      @Override
      public void columnMarginChanged(ChangeEvent e) {
      }
      @Override
      public void columnAdded(TableColumnModelEvent e) {
      }
    });

    table.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        if (table.getSelectedRow() == -1 ||
            table.getSelectedColumn() == -1) {
          return;
        }
        if (table.getSelectedColumn() < tableModel.getNumColumnExtras() ) {
          
        } else {
          ColumnDef columnDef = tableDef.getColumns().get(
              table.getSelectedColumn() - tableModel.getNumColumnExtras() );
          String tableName = tableDef.getTableName();
          String columnName = columnDef.getColumnName();
          if (e.isAltDown() ) {
            snippetConsole.appendInfo(
                "userData." + columnName + " = null;");
          } else {
            snippetConsole.appendInfo("'" +
                tableName + "." + columnName + "',");
          }
        }
      }
    });

    for (int c = 0; c < table.getColumnModel().getColumnCount(); c += 1) {
      TableColumn column = table.getColumnModel().getColumn(c);
      Component header = table.getTableHeader().getDefaultRenderer().
          getTableCellRendererComponent(
              table, column.getHeaderValue(), false, false, -1, c);
      int width = header.getPreferredSize().width;
      for (int r = 0; r < tableModel.getRowCount(); r += 1) {
        Component cell = table.getDefaultRenderer(Object.class).
            getTableCellRendererComponent(table,
            tableModel.getValueAt(r, c), false, false, r, c);
        width = Math.max(width, cell.getPreferredSize().width);
      }
      column.setPreferredWidth(width + 8);
    }
    return table;
  }
}
