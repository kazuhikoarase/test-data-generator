package dataman.model;

/**
 * CasePattern
 * @author kazuhiko arase
 */
public class CasePattern {

  private final String id;

  private final String name;

  public CasePattern(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return id + ": " + name;
  }
}
