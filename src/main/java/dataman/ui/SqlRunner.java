package dataman.ui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import dataman.core.Console;
import dataman.core.DataSource;
import dataman.model.DataSet;

/**
 * SqlRunner
 * @author kazuhiko arase
 */
public class SqlRunner {

  public static final int RUN_DELETE = 1;
  public static final int RUN_INSERT = 2;

  private Console console;

  private int deleteCount;
  private int insertCount;
  private int errorCount;

  public SqlRunner(Console console) {
    this.console = console;
  }

  public void run(
      DataSource ds, DataSet dataSet,
      boolean test, int run
  ) throws Exception {

    final boolean runDelete = (run & RUN_DELETE) != 0;
    final boolean runInsert = (run & RUN_INSERT) != 0;

    deleteCount = 0;
    insertCount = 0;
    errorCount = 0;

    console.clear();

    final Connection conn = ds.getConnection(dataSet.getDataSource() );

    try {

      if (runDelete) {
        dataSet.fetchDeleteStatements(new DataSet.LineHandler() {
          @Override
          public void handle(String sql) throws Exception {
            Statement stmt = conn.createStatement();
            try {
              deleteCount += stmt.executeUpdate(sql);
            } catch(SQLException e) {
              console.appendInfo(sql);
              console.appendError(e.getMessage() );
              errorCount += 1;
            } finally {
              stmt.close();
            }
          }
        });
      }  

      if (runInsert) {
        dataSet.fetchInsertStatements(new DataSet.LineHandler() {
          @Override
          public void handle(String sql) throws Exception {
            Statement stmt = conn.createStatement();
            try {
              insertCount += stmt.executeUpdate(sql);
            } catch(SQLException e) {
              console.appendInfo(sql);
              console.appendError(e.getMessage() );
              errorCount += 1;
            } finally {
              stmt.close();
            }
          }
        });
      }

      if (errorCount > 0) {
        console.appendError(errorCount + " error(s)");
      }

      if (test) {
        conn.rollback();
        if (errorCount == 0) {
          console.appendInfo("test run ok.");
        } else {
          console.appendInfo("test run failed.");
        }
      } else {
        conn.commit();
        if (runDelete) {
          console.appendInfo(deleteCount + " record(s) deleted.");
        }
        if (runInsert) {
          console.appendInfo(insertCount + " record(s) inserted.");
        }
        console.appendInfo("update commited.");
      }
    } finally {
      conn.close();
    }
  }
}
