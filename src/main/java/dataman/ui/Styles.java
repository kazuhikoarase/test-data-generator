package dataman.ui;

import java.awt.Color;

import dataman.model.DataObject;

/**
 * Styles
 * @author kazuhiko arase
 */
public class Styles {
  private Styles() {
  }
  public static Color getSysHeadColor() {
    return new Color(0xffffff);
  }
  public static Color getSysHeadBgColor() {
    return new Color(0x666666);
  }
  public static Color getPkColor() {
    return new Color(0xffffff);
  }
  public static Color getPkBgColor() {
    return new Color(0x0000cc);
  }
  public static Color getNullFgColor() {
    return Color.LIGHT_GRAY;
  }
  public static Color getUserFgColor() {
    return new Color(0xcc0066);
  }
  public static Color getSqlNameFgColor() {
    return Color.BLUE;
  }
  public static Color getDeletedFgColor() {
    return Color.LIGHT_GRAY;
  }
  public static Color getDeletedBgColor() {
    return Color.GRAY;
  }
  private static final int[] mod = { 0, 1, 3, 2 };
  public static Color getGeneratedColor(DataObject value) {
    return new Color(Color.HSBtoRGB(
        ( (value.getPatternId() * 3f) % 32) / 32, 0.4f,
        1 - mod[value.getIndex() % mod.length] * 0.1f) );
  }
}
