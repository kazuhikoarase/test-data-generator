package dataman.core;

import java.awt.Color;
import java.util.regex.Pattern;

/**
 * Util
 * @author kazuhiko arase
 */
public class Util {

  private Util(){
  }

  private static final Pattern trimPat =
      Pattern.compile("^[\\s\\u3000]+|[\\s\\u3000]+$");
  
  public static String trim(String s) {
    return s != null? trimPat.matcher(s).replaceAll("") : s;
  }

  public static boolean isEmpty(String s) {
    return s == null || s.length() == 0;
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
}
