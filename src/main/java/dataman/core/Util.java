package dataman.core;

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
}
