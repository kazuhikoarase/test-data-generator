package dataman.model;

import java.util.ArrayList;
import java.util.List;

/**
 * PatternValues
 * @author kazuhiko arase
 */
public class PatternValues {

  private final int patternId;

  private final List<Object> values = new ArrayList<Object>();

  public PatternValues(int patternId) {
    this.patternId = patternId;
  }

  public int getPatternId() {
    return patternId;
  }

  public List<Object> getValues() {
    return values;
  }
}
