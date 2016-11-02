package dataman.core;

/**
 * Console
 * @author kazuhiko arase
 */
public interface Console {
  void clear();
  void log(Object msg);
  void appendInfo(Object msg);
  void appendError(Object msg);
  void appendThrowable(Throwable t);
}
