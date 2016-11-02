package dataman.model;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.Stack;

/**
 * DsTreeNode
 * @author kazuhiko arase
 */
public class DsTreeNode extends ListTreeNode {

  private final String name;
  private final String driverClassName;
  private final String url;
  private final Properties info;

  public DsTreeNode(String name,
      String driverClassName, String url, Properties info) {
    this.name = name;
    this.driverClassName = driverClassName;
    this.url = url;
    this.info = info;
  }

  @Override
  public String toString() {
    return name;
  }

  public Connection getConnection() throws Exception {
    Connection conn;
    if (connPool.isEmpty() ) {
      Class.forName(driverClassName);
      conn = DriverManager.getConnection(url, info);
      conn.setAutoCommit(false);
      conn.setTransactionIsolation(
          Connection.TRANSACTION_READ_COMMITTED);
    } else {
      conn = connPool.pop();
    } 
    return (Connection)Proxy.newProxyInstance(
        getClass().getClassLoader(),
        new Class<?>[]{ Connection.class },
        new ConnHandler(conn) );
  }

  private Stack<Connection> connPool = new Stack<Connection>();

  class ConnHandler implements InvocationHandler {
    private Connection conn;
    public ConnHandler(Connection conn) {
      this.conn = conn;
    }
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
      if (method.getName().equals("close") &&
          method.getParameterTypes().length == 0) {
        conn.rollback();
        connPool.push(conn);
        return null;
      }
      return method.invoke(conn, args);
    }
  }
}
