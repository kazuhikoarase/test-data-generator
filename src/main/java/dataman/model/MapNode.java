package dataman.model;

import java.util.HashMap;
import java.util.Map;

/**
 * MapNode
 * @author kazuhiko arase
 */
public class MapNode extends ListTreeNode {

  protected static String toMetaName(final String name) {
    return name.toUpperCase().intern();
  }

  public static final String TABLE_SCHEM = "TABLE_SCHEM";
  public static final String TABLE_TYPE = "TABLE_TYPE";
  public static final String TABLE_NAME = "TABLE_NAME";

  private final String type;

  private final Map<String,Object> map;

  private final boolean allowsChildren;

  public MapNode(String type, boolean allowsChildren) {
    this.type = toMetaName(type);
    this.allowsChildren = allowsChildren;
    this.map = new HashMap<String, Object>();
  }

  public String getType() {
    return type;
  }

  public Object get(String name) {
    return map.get(toMetaName(name) );
  }

  public void put(String name, Object value) {
    map.put(toMetaName(name), value);
  }

  @Override
  public boolean getAllowsChildren() {
    return allowsChildren;
  }

  @Override
  public boolean isLeaf() {
    return !allowsChildren;
  }

  @Override
  public String toString() {
    Object name = map.get(type);
    return (name != null)? name.toString() : super.toString();
  }
}
