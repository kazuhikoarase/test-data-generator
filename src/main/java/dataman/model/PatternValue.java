package dataman.model;

/**
 * PatternValue
 * @author kazuhiko arase
 */
public class PatternValue {

  private final int patternId;

  private final int index;

  private final Object value;

  public PatternValue(int patternId, int index, Object value) {
    this.patternId = patternId;
    this.index = index;
    this.value = value;
  }

  public int getPatternId() {
    return patternId;
  }

  public int getIndex() {
    return index;
  }

  public Object getValue() {
    return value;
  }
}
