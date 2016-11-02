package dataman.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TableDef
 * @author kazuhiko arase
 */
public class TableDef {

  private String tableName;

  private List<ColumnDef> columns = new ArrayList<ColumnDef>();

  private Set<String> uniqKeys = new HashSet<String>();

  public TableDef() {
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getTableName() {
    return tableName;
  }

  public List<ColumnDef> getColumns() {
    return columns;
  }

  public Set<String> getUniqKeys() {
    return uniqKeys;
  }

  public static TableDef fromMetaData(
      Connection conn,
      String schema,
      String table
  ) throws Exception {

    TableDef tableDef = new TableDef();

    tableDef.setTableName(table);

    DatabaseMetaData metaData = conn.getMetaData();
    {
      ResultSet rs = metaData.getPrimaryKeys(null, schema, table);
      try {
        while (rs.next() ) {
          tableDef.getUniqKeys().add(rs.getString("COLUMN_NAME") );
        }
      } finally {
        rs.close();
      }
    }

    {
      ResultSet rs = metaData.getColumns(null, schema, table, null);
      try {
        while (rs.next() ) {
          ColumnDef columnDef = new ColumnDef();
          columnDef.setTableName(rs.getString("TABLE_NAME") );
          columnDef.setColumnName(rs.getString("COLUMN_NAME") );
          columnDef.setTypeName(rs.getString("TYPE_NAME") );
          columnDef.setColumnSize(rs.getInt("COLUMN_SIZE") );
          columnDef.setDecimalDigits(rs.getInt("DECIMAL_DIGITS") );
          columnDef.setNullable("YES".equals(rs.getString("IS_NULLABLE") ) );
          if (tableDef.getUniqKeys().contains(columnDef.getColumnName() ) ) {
            columnDef.setPrimaryKey(true);
          }
          tableDef.getColumns().add(columnDef);
        }
      } finally {
        rs.close();
      }
    }

    return tableDef;
  }
}
