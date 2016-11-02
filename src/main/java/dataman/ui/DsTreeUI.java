package dataman.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import dataman.core.Console;
import dataman.core.DataSource;
import dataman.core.Util;
import dataman.model.DsTreeNode;
import dataman.model.ListTreeNode;
import dataman.model.MapNode;

/**
 * DsTreeUI
 * @author kazuhiko arase
 */
public class DsTreeUI implements DataSource {

  public static final String TABLE_SCHEM = "TABLE_SCHEM";
  public static final String TABLE_TYPE = "TABLE_TYPE";
  public static final String TABLE_NAME = "TABLE_NAME";
  public static final String COLUMN_NAME = "COLUMN_NAME";

  private Map<String,DsTreeNode> dsMap;
  private ListTreeNode dsList;
  private JTree tree;
  private JTable table;
  private JComponent ui;

  public DsTreeUI(final Console snippetConsole) {

    dsMap = new HashMap<String, DsTreeNode>();
    dsList = new ListTreeNode();
    tree = new JTree(dsList);
    tree.setRootVisible(false);

    final JTextField search = new JTextField();
    search.addKeyListener(new KeyListener() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          searchKeyword(search.getText(), !e.isShiftDown() );
        }
      }
      @Override
      public void keyReleased(KeyEvent e) {
      }
      @Override
      public void keyTyped(KeyEvent e) {
      }
    });

    table = new JTable();

    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() != 2) {
          return;
        }
        if (table.getSelectedRow() == -1) {
          return;
        }
        if (table.getModel() instanceof DsTableModel) {
          DsTableModel model = (DsTableModel)table.getModel();
          Map<String,Object> columnDef = (Map<String,Object>)model.
              getValueAt(table.getSelectedRow(), 0);
          String tableName = model.getTableName();
          String columnName = (String)columnDef.get("COLUMN_NAME");
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

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout() );
    panel.add(BorderLayout.NORTH, search);
    panel.add(BorderLayout.CENTER, new JScrollPane(tree) );
    
    JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    pane.setBorder(new EmptyBorder(0,0,0,0) );
    pane.setDividerSize(4);
    pane.setTopComponent(panel);
    pane.setBottomComponent(new JScrollPane(table) );
    pane.setPreferredSize(new Dimension(200, 400) );
    pane.setDividerLocation(300);

    ui = pane;

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value,
          boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // TODO Auto-generated method stub
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);
      }
    };
    renderer.setLeafIcon(null);
    tree.setCellRenderer(renderer);

    tree.addTreeWillExpandListener(new TreeWillExpandListener() {
      
      @Override
      public void treeWillExpand(TreeExpansionEvent event)
          throws ExpandVetoException {
        try {
          treeWillExpandHandler(event);
        } catch(Exception e) {
          e.printStackTrace();
        }
      }

      @Override
      public void treeWillCollapse(TreeExpansionEvent event)
          throws ExpandVetoException {
      }
    });
    tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent event) {
        try {
          valueChangedHandler(event);
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void searchKeyword(String keyword, boolean forward) {

    keyword = Util.trim(keyword).toUpperCase();
    Pattern pat = null;
    try {
      pat = Pattern.compile(keyword);
    } catch(Exception e) {
    }

    TreePath startPath = tree.getSelectionPath();

    int index = 0;
    if (startPath != null) {
      if (forward) {
        index = tree.getRowForPath(startPath) + 1;
      } else {
        index = tree.getRowForPath(startPath) - 1;
      }
    }

    for (int i = 0; i < tree.getRowCount(); i += 1) {
      index = (index + tree.getRowCount() ) % tree.getRowCount();
      TreePath path = tree.getPathForRow(index);
      Object comp = path.getLastPathComponent();
      if (comp instanceof MapNode) {
        MapNode node = (MapNode)comp;
        if (node.getType().equals(TABLE_NAME) ) {
          final String text = node.toString().toUpperCase();
          final boolean found = (pat != null)?
              pat.matcher(text).find() :
              (text.indexOf(keyword) != -1);
          if (found) {
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            break;
          }
        }
      }
      if (forward) {
        index += 1;
      } else {
        index -= 1;
      }
    }
  }

  private Set<String> getPkeySet(
      DatabaseMetaData meta, String schema, String table) throws Exception {
    Set<String> pkeySet = new HashSet<String>();
    ResultSet rs = meta.getPrimaryKeys(null, schema, table);
    try {
      while (rs.next() ) {
        pkeySet.add(rs.getString("COLUMN_NAME"));
      }
    } finally {
      rs.close();
    }
    return pkeySet;
  }

  private void valueChangedHandler(TreeSelectionEvent event) throws Exception {
    TreePath path = event.getPath();
    Object comp = path.getLastPathComponent();
    if (comp instanceof MapNode) {
      MapNode node = (MapNode)comp;
      if (node.getType().equals(TABLE_NAME) ) {
        Connection conn = getConnection(event.getPath() );
        try {
          DatabaseMetaData meta = conn.getMetaData();
          final Set<String> pkeySet = getPkeySet(meta,
              getTableSchem(path), getTableName(path));
          ResultSet rs = meta.getColumns(null,
              getTableSchem(path), getTableName(path), null);
          try {
            DsTableModel model = new DsTableModel(getTableName(path) );
            model.addColumn("NAME");
            model.addColumn("TYPE");
            while (rs.next() ) {
              StringBuilder buf = new StringBuilder();
              buf.append(rs.getString("TYPE_NAME") );
              buf.append('(');
              buf.append(rs.getInt("COLUMN_SIZE") );
              int digits = rs.getInt("DECIMAL_DIGITS");
              if (digits > 0) {
                buf.append(',');
                buf.append(digits);
              }
              buf.append(')');

              final String columnName = rs.getString("COLUMN_NAME");
              Map<String,Object> colName = new HashMap<String,Object>() {
                @Override
                public String toString() {
                  return columnName + (pkeySet.contains(columnName)? "*" : "");
                }
              };
              colName.put("COLUMN_NAME", columnName);

              model.addRow(new Object[]{
                colName,
                buf.toString()
              });
            }
            table.setModel(model);
          } finally {
            rs.close();
          }
        } finally {
          conn.close();
        }
      }
    }
  }

  public JComponent getUI() {
    return ui;
  }

  public void addDataSource(String name,
      String driverClassName, String url, Properties info) {
    DsTreeNode ds = new DsTreeNode(name, driverClassName, url, info);
    if (dsMap.containsKey(name) ) {
      throw new IllegalArgumentException(
          "DataSource already defined:" + name);
    }
    dsMap.put(name, ds);
    dsList.addChild(ds);
    tree.updateUI();
  }

  private static class DsTableModel extends DefaultTableModel {
    private String tableName;
    public DsTableModel(String tableName) {
      this.tableName = tableName;
    }
    public String getTableName() {
      return tableName;
    }
    @Override
    public boolean isCellEditable(int row, int column) {
      return false;
    }
  }

  public Connection getConnection(String dataSourceName) throws Exception {
    DsTreeNode dsNode = dsMap.get(dataSourceName);
    if (dsNode == null) {
      throw new IllegalArgumentException(
          "undefined dataSource:" + dataSourceName);
    }
    return dsNode.getConnection();
  }

  private void treeWillExpandHandler(
      TreeExpansionEvent event) throws Exception {

    Object target = event.getPath().getLastPathComponent();
    TreePath path = event.getPath();

    if (target instanceof DsTreeNode) {

      DsTreeNode node = (DsTreeNode)target;
      Connection conn = getConnection(path);
      try {
        ResultSet rs = conn.getMetaData().getSchemas();
        try {
          update(TABLE_SCHEM, true, rs, node);
        } finally {
          rs.close();
        }
      } finally {
        conn.close();
      }

    } else if (target instanceof MapNode) {

      MapNode node = (MapNode)target;
      Connection conn = getConnection(event.getPath() );

      try {

        String type = null;
        ResultSet rs = null;
        boolean allowsChildren = true;
        
        if (node.getType().equals(TABLE_SCHEM) ) {
          type = TABLE_TYPE;
          rs = conn.getMetaData().getTableTypes();
        } else if (node.getType().equals(TABLE_TYPE) ) {
          type = TABLE_NAME;
          rs = conn.getMetaData().getTables(null,
              getTableSchem(path), null,
              new String[]{ getTableType(path) });
          allowsChildren = false;
        } else if (node.getType().equals(TABLE_NAME) ) {
          /*
          type = COLUMN_NAME;
          rs = conn.getMetaData().getColumns(null,
              getTableSchem(path), getTableName(path), null);
          allowsChildren = false;
          */
        }

        try {
          update(type, allowsChildren, rs, node);
        } finally {
          rs.close();
        }

      } finally {
        conn.close();
      }
    }
  }

  protected static Connection getConnection(TreePath path) throws Exception {
    return ((DsTreeNode)path.getPath()[1]).getConnection();
  }

  protected static String getTableSchem(TreePath path) throws Exception {
    MapNode node = (MapNode)path.getPath()[2];
    return String.valueOf(node.get(node.getType() ) );
  }

  protected static String getTableType(TreePath path) throws Exception {
    MapNode node = (MapNode)path.getPath()[3];
    return String.valueOf(node.get(node.getType() ) );
  }

  protected static String getTableName(TreePath path) throws Exception {
    MapNode node = (MapNode)path.getPath()[4];
    return String.valueOf(node.get(node.getType() ) );
  }

  private static void update(
      String type,
      boolean allowsChildren,
      ResultSet rs,
      ListTreeNode node
  ) throws Exception {
    ResultSetMetaData meta = rs.getMetaData();
    String[] columns = new String[meta.getColumnCount()];
    for (int i = 0; i < columns.length; i += 1) {
      columns[i] = meta.getColumnName(i + 1);
    }
    node.removeAllChildern();
    while (rs.next() ) {
      MapNode map = new MapNode(type, allowsChildren);
      for (int i = 0; i < columns.length; i += 1) {
        map.put(columns[i], rs.getObject(i + 1) );
      }
      node.addChild(map);
    }
  }
}
