package dataman.model;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.Invocable;

import dataman.core.Console;
import dataman.core.DataSource;
import dataman.ui.DataTableUI;

/**
 * DataSet
 * @author kazuhiko arase
 */
public class DataSet {

  private Map<String,TableDef> metaMap;

  private String dataSourceName;

  private Map<String,Map<String,PatternValues>> patternMap;

  private Map<String,List<DataObjectRow>> dataMap;

  private CasePattern casePattern;

  private final Invocable script;

  private final DataSource ds;

  public DataSet(Invocable script, DataSource ds) {
    this.script = script;
    this.ds = ds;
    metaMap = new HashMap<String,TableDef>();
    patternMap = new LinkedHashMap<String, Map<String,PatternValues>>();
    dataMap = new LinkedHashMap<String, List<DataObjectRow>>();
  }

  public CasePattern getCasePattern() {
    return casePattern;
  }

  public void setCasePattern(CasePattern casePattern) {
    this.casePattern = casePattern;
  }

  public Object invokeFunction(String fn, Object... args)
  throws Exception {
    Object[] _args = new Object[args.length + 1];
    _args[0] = "_intf." + fn;
    for (int i = 0; i < args.length; i += 1) {
      _args[i + 1] = args[i];
    }
    return script.invokeFunction("_invokeFunction", _args);
  }

  public void setDataSource(String dataSourceName) {
    this.dataSourceName = dataSourceName;
  }

  public String getDataSource() {
    return dataSourceName;
  }

  protected void checkValue(Object value) {
    if (value == null) {
    } else if (value instanceof String) {
    } else if (value instanceof Number) {
    } else if (value instanceof SqlName) {
    } else {
      throw new IllegalArgumentException(
           "unsupported type:" + value.getClass().getName() );
    }
  }

  public void addPattern(int patternId, String column, Object value) {

    checkValue(value);

    String[] colDef = column.split("\\.");
    String schemaName;
    String tableName;
    String columnName;
    if (colDef.length == 2) {
      schemaName = null;
      tableName = colDef[0];
      columnName = colDef[1];
    } else if (colDef.length == 3) {
      schemaName = colDef[0];
      tableName = colDef[1];
      columnName = colDef[2];
    } else {
      throw new IllegalArgumentException("invalid column name:" + column);
    }

    Map<String,PatternValues> columnMap = patternMap.get(tableName);
    if (columnMap == null) {
      columnMap = new LinkedHashMap<String, PatternValues>();
      patternMap.put(tableName, columnMap);
    }
    PatternValues patVal = columnMap.get(columnName);
    if (patVal == null) {
      patVal = new PatternValues(patternId);
      columnMap.put(columnName, patVal);
    }
    if (!patVal.getValues().contains(value) ) {
      patVal.getValues().add(value);
    }
  }

  public void commit(Console console) throws Exception {

    invokeFunction("reset");

    for (Entry<String, Map<String,PatternValues>> tablePat :
        patternMap.entrySet() ) {
      final String tableName = tablePat.getKey();
      List<DataObjectRow> rows = new ArrayList<DataObjectRow>();
      dataMap.put(tableName, rows);
 
      Set<Entry<String,PatternValues>> colPat =
          tablePat.getValue().entrySet();
      @SuppressWarnings("unchecked")
      Entry<String,PatternValues>[] colPatArray =
        colPat.toArray(new Entry[colPat.size()]);
      expand(tableName, null, colPatArray, 0);

      int genCount = 0;
      for (DataObjectRow row : rows) {
        if (!row.isDeleted() ) {
          genCount += 1;
        }
      }
      console.log(tableName + " - " + genCount + " record(s) generated.");
    }
  }

  private void expand(
      final String tableName, 
      final Map<String,PatternValue> row,
      final Entry<String,PatternValues>[] colPatArray,
      final int index
  ) throws Exception {
    Entry<String,PatternValues> e = colPatArray[index];
    for (int i = 0; i < e.getValue().getValues().size(); i += 1) {
      Map<String,PatternValue> newRow =
          new LinkedHashMap<String, PatternValue>();
      if (row != null) {
        newRow.putAll(row);
      }
      newRow.put(e.getKey(), new PatternValue(
          e.getValue().getPatternId(), i, 
          e.getValue().getValues().get(i) ) );
      if (index + 1 < colPatArray.length) {
        expand(tableName, newRow, colPatArray, index + 1);
      } else {
        addRow(tableName, newRow);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void addRow(
    String tableName,
    Map<String,PatternValue> row
  ) throws Exception {

    List<DataObjectRow> rows = dataMap.get(tableName);

    TableDef tableDef = getTableDef(tableName);

    Map<String,DataObject> dataMap =
        new HashMap<String, DataObject>();

    for (int i = 0; i < tableDef.getColumns().size(); i += 1) {

      final ColumnDef columnDef = tableDef.getColumns().get(i);
      final String columnName = columnDef.getColumnName();
      final boolean generated = row.containsKey(columnName);

      Object value;
      int index;
      int patternId;
      int type;

      if (generated) {
        PatternValue patternValue = row.get(columnName);
        value = patternValue.getValue();
        index = patternValue.getIndex();
        patternId = patternValue.getPatternId();
        type = DataObject.TYPE_GENERATED;
      } else {
        value = invokeFunction("getDefaultValue", columnDef);
        index = -1;
        patternId = -1;
        type = DataObject.TYPE_DEFAULT;
      }

      dataMap.put(columnName, new DataObject(
          value, type, index, patternId) );
    }

    Map<String,Object> userRowData = (Map<String,Object>)
        invokeFunction("getUserRowData",
            tableDef, rows.size(), dataMap,
            casePattern != null? casePattern.getId() : null);

    DataObjectRow dataRow = new DataObjectRow(
        tableDef.getColumns().size() );

    // meta info
    final Boolean deleted = (Boolean)userRowData.get(".deleted");
    if (deleted != null) {
      dataRow.setDeleted(deleted);
    }
    final String comment = (String)userRowData.get(".comment");
    if (comment != null) {
      dataRow.setComment(comment);
    }

    for (int i = 0; i < tableDef.getColumns().size(); i += 1) {

      final ColumnDef columnDef = tableDef.getColumns().get(i);
      final String columnName = columnDef.getColumnName();
      DataObject data = dataMap.get(columnName);

      Object value = data.getValue();
      int index = data.getIndex();
      int patternId = data.getPatternId();
      int type = data.getType();

      // overwrite default value
      if (type == DataObject.TYPE_DEFAULT &&
          userRowData.containsKey(columnName) ) {
        value = userRowData.get(columnName);
        checkValue(value);
        index = -1;
        patternId = -1;
        type = DataObject.TYPE_USER;
      }

      if (value instanceof Number) {
        BigDecimal dec = new BigDecimal(value.toString() );
        int scale = columnDef.getDecimalDigits();
        if (scale >= 0) {
          dec = dec.setScale(scale, RoundingMode.FLOOR);
        }
        value = dec;
      }

      dataRow.set(i, new DataObject(value, type, index, patternId) );
    }

    rows.add(dataRow);
  }

  private TableDef getTableDef(String tableName)
  throws Exception {
    TableDef tableDef = metaMap.get(tableName);
    if (tableDef == null) {
      Connection conn = ds.getConnection(dataSourceName);
      try {
        tableDef = TableDef.fromMetaData(conn, null, tableName);
        metaMap.put(tableName, tableDef);
      } finally {
        conn.close();
      }
    }
    return tableDef;
  }

  private String getHr() {
    char[] c = new char[76];
    Arrays.fill(c, '-');
    return new String(c);
  }

  public void outputDeleteSql(final PrintWriter out)
  throws Exception {

    final LineHandler h = new LineHandler() {
      @Override
      public void handle(String line) throws Exception {
        out.print(line);
        out.println(';');
      }
    };

    final String hr = getHr();

    for (String tableName : dataMap.keySet() ) {

      TableDef tableDef = getTableDef(tableName);

      out.println();
      out.println(hr);
      out.println("-- " + tableDef.getTableName() );
      out.println(hr);
      out.println();

      fetchDeleteStatements(tableName, h);
    }
  }

  public void outputInsertSql(final PrintWriter out)
  throws Exception {

    final LineHandler h = new LineHandler() {
      @Override
      public void handle(String line) throws Exception {
        out.print(line);
        out.println(';');
      }
    };

    final String hr = getHr();

    if (casePattern != null) {
      out.println(hr);
      out.println("-- " + casePattern);
      out.println(hr);
    }

    for (String tableName : dataMap.keySet() ) {

      TableDef tableDef = getTableDef(tableName);

      out.println();
      out.println(hr);
      out.println("-- " + tableDef.getTableName() );
      out.println(hr);
      out.println();

      fetchInsertStatements(tableName, h);
    }
  }

  public void fetchDeleteStatements(LineHandler h)
  throws Exception {
    for (String tableName : dataMap.keySet() ) {
      fetchDeleteStatements(tableName, h);
    }
  }

  public void fetchInsertStatements(LineHandler h)
  throws Exception {
    for (String tableName : dataMap.keySet() ) {
      fetchInsertStatements(tableName, h);
    }
  }

  public void fetchDeleteStatements(String tableName, LineHandler h)
  throws Exception {

    TableDef tableDef = getTableDef(tableName);

    Object deleteKeys = invokeFunction("getDeleteKeys", tableName);

    if (deleteKeys != null) {
      if (deleteKeys instanceof Set<?>) {
        Set<String> cache = new HashSet<String>();
        Set<?> keySet = (Set<?>)deleteKeys;
        for (DataObjectRow row : dataMap.get(tableName) ) {
          StringBuilder buf = new StringBuilder();
          buf.append("delete from ");
          buf.append(tableDef.getTableName() );
          buf.append(" where ");
          int keyCount = 0;
          for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
            ColumnDef columnDef = tableDef.getColumns().get(i);
            if (keySet.contains(columnDef.getColumnName() ) ) {
              if (keyCount > 0) {
                buf.append(" and ");
              }
              buf.append(columnDef.getColumnName() );
              buf.append("=");
              buf.append(toConstant(row.get(i).getValue() ) );
              keyCount += 1;
            }
          }
          String sql = buf.toString();
          if (cache.contains(sql) ) {
            continue;
          }
          h.handle(sql);
          cache.add(sql);
        }
        return;
      } else if (deleteKeys instanceof Map<?, ?>) {
        StringBuilder buf = new StringBuilder();
        buf.append("delete from ");
        buf.append(tableDef.getTableName() );
        buf.append(" where ");
        int count = 0;
        for (Entry<?, ?> e : ((Map<?, ?>)deleteKeys).entrySet() ) {
          if (count > 0) {
            buf.append(" and ");
          }
          buf.append(e.getKey() );
          buf.append('=');
          buf.append(toConstant(e.getValue() ) );
          count += 1;
        }
        h.handle(buf.toString() );
        return;
      } else if (deleteKeys instanceof String) {
        StringBuilder buf = new StringBuilder();
        buf.append("delete from ");
        buf.append(tableDef.getTableName() );
        buf.append(" where ");
        buf.append(deleteKeys);
        h.handle(buf.toString() );
        return;
      } else {
        throw new IllegalArgumentException("!" + deleteKeys);
      }
    }

    // delete by primary keys
    for (DataObjectRow row : dataMap.get(tableName) ) {
      StringBuilder buf = new StringBuilder();
      buf.append("delete from ");
      buf.append(tableDef.getTableName() );
      buf.append(" where ");
      int keyCount = 0;
      for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
        ColumnDef columnDef = tableDef.getColumns().get(i);
        if (columnDef.isPrimaryKey() ) {
          if (keyCount > 0) {
            buf.append(" and ");
          }
          buf.append(columnDef.getColumnName() );
          buf.append("=");
          buf.append(toConstant(row.get(i).getValue() ) );
          keyCount += 1;
        }
      }
      h.handle(buf.toString() );
    }
  }

  public void fetchInsertStatements(String tableName, LineHandler h)
  throws Exception {

    TableDef tableDef = getTableDef(tableName);

    for (DataObjectRow row : dataMap.get(tableName) ) {
      if (row.isDeleted() ) {
        continue;
      }
      StringBuilder buf = new StringBuilder();
      buf.append("insert into ");
      buf.append(tableDef.getTableName() );
      buf.append(" (");
      for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
        ColumnDef columnDef = tableDef.getColumns().get(i);
        if (i > 0) {
          buf.append(',');
        }
        buf.append(columnDef.getColumnName() );
      }
      buf.append(") values (");
      for (int i = 0; i < tableDef.getColumns().size(); i += 1) {
        if (i > 0) {
          buf.append(',');
        }
        buf.append(toConstant(row.get(i).getValue() ) );
      }
      buf.append(")");
      h.handle(buf.toString() );
    }
  }

  public List<DataTableUI> createUIList(
    Console infoConsole,
    Console snippetConsole,
    boolean hideDefaultValueColumns
  ) throws Exception {
    List<DataTableUI> dataTableUIList = new ArrayList<DataTableUI>();
    for (Entry<String, List<DataObjectRow>> e : dataMap.entrySet() ) {
      DataTableUI dataTable = new DataTableUI(
          getTableDef(e.getKey() ), e.getValue(),
          hideDefaultValueColumns, infoConsole, snippetConsole);
      dataTableUIList.add(dataTable);
    }
    return dataTableUIList;
  }

  // TODO to script side
  private static final char ESCAPE = '\'';
  private static final char QUOT = '\'';
  private static String toConstant(Object value) {

    if (value == null) {
      return "null";
    } else if (value instanceof Number) {
      return value.toString();
    } else if (value instanceof SqlName) {
      return value.toString();
    } else if (value instanceof String) {
      String s = (String)value;
      StringBuilder buf = new StringBuilder();
      buf.append(QUOT);
      for (int i = 0; i < s.length(); i += 1) {
        char c = s.charAt(i);
        if (c == QUOT || c == ESCAPE) {
          buf.append(ESCAPE);
        }
        buf.append(c);
      }
      buf.append(QUOT);
      return buf.toString();
    } else {
      throw new IllegalArgumentException("type:" +
          value.getClass().getName() );
    }
  }

  public interface LineHandler {
    void handle(String line) throws Exception;
  }
}
