package dataman.model;

import java.util.HashMap;
import java.util.Map;

/**
 * MapNode
 * @author kazuhiko arase
 */
public class MapNode extends ListTreeNode {

  private final String type;

  private final Map<String,Object> map;

  private final boolean allowsChildren;

  public MapNode(String type, boolean allowsChildren) {
    this.type = type;
    this.allowsChildren = allowsChildren;
    this.map = new HashMap<String, Object>();
  }

  public String getType() {
    return type;
  }

  public Object get(String name) {
    return map.get(name);
  }

  public void put(String name, Object value) {
    map.put(name, value);
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
