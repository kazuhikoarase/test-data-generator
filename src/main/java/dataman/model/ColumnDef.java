package dataman.model;

/**
 * ColumnDef
 * @author kazuhiko arase
 */
public class ColumnDef {

  private String tableName;
  private String columnName;
  private String typeName;
  private int columnSize;
  private int decimalDigits;
  private boolean nullable;
  private boolean primaryKey;

  public ColumnDef() {
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getColumnName() {
    return columnName;
  }

  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  public String getTypeName() {
    return typeName;
  }

  public void setTypeName(String typeName) {
    this.typeName = typeName;
  }

  public int getColumnSize() {
    return columnSize;
  }

  public void setColumnSize(int columnSize) {
    this.columnSize = columnSize;
  }

  public int getDecimalDigits() {
    return decimalDigits;
  }

  public void setDecimalDigits(int decimalDigits) {
    this.decimalDigits = decimalDigits;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getTypeDesc() {
    StringBuilder buf = new StringBuilder();
    buf.append(getTypeName() );
    buf.append('(');
    buf.append(getColumnSize() );
    int digits = getDecimalDigits();
    if (digits > 0) {
      buf.append(',');
      buf.append(digits);
    }
    buf.append(')');
    return buf.toString();
  }
}
