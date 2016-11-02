package dataman.model;

/**
 * DataObject
 * @author kazuhiko arase
 */
public class DataObject {

  public static final int TYPE_GENERATED = 1;

  public static final int TYPE_DEFAULT = 2;

  public static final int TYPE_USER = 3;

  private final int type;

  private final int index;

  private final int patternId;

  private final Object value;

  public DataObject(Object value, int type,
      int index, int patternId) {
    this.value = value;
    this.type = type;
    this.index = index;
    this.patternId = patternId;
  }

  public Object getValue() {
    return value;
  }

  public int getType() {
    return type;
  }

  public int getIndex() {
    return index;
  }

  public int getPatternId() {
    return patternId;
  }
}
