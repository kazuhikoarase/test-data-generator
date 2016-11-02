package dataman.core;
import java.sql.Connection;

/**
 * DataSource
 * @author kazuhiko arase
 */
public interface DataSource {
  Connection getConnection(String dataSourceName) throws Exception;
}
