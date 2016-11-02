package dataman.model;

/**
 * SqlName
 * @author kazuhiko arase
 */
public class SqlName {

  private final String name;

  public SqlName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return name;
  }
}
