package dataman.ui;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import dataman.core.Console;

/**
 * ConsoleManagerUI
 * @author kazuhiko arase
 */
public class ConsoleManagerUI {

  public static final String CONSOLE_DEFAULT = "DEFAULT";
  public static final String CONSOLE_SQL = "SQL";
  public static final String CONSOLE_INFO = "INFO";
  public static final String CONSOLE_SNIPPET = "SNIPPET";

  private Map<String,ConsoleUI> consoleMap;

  private JTabbedPane tab;

  public ConsoleManagerUI() {
    tab = new JTabbedPane();
    consoleMap = new HashMap<String, ConsoleUI>();
    addConsole(CONSOLE_DEFAULT, "Console", true);
    addConsole(CONSOLE_SQL, "SQL", true);
    addConsole(CONSOLE_INFO, "Info", false);
    addConsole(CONSOLE_SNIPPET, "Snippet", false);
  }

  private void addConsole(
      final String name,
      final String label,
      final boolean autoSelect
  ) {
    final int tabIndex = tab.getTabCount();
    ConsoleUI console = new ConsoleUI() {
      @Override
      public void appendInfo(Object log) {
        if (autoSelect) {
          tab.setSelectedIndex(tabIndex);
        }
        super.appendInfo(log);
      }
      @Override
      public void appendError(Object log) {
        if (autoSelect) {
          tab.setSelectedIndex(tabIndex);
        }
        super.appendError(log);
      }
    };
    consoleMap.put(name, console);
    tab.addTab(label, new JScrollPane(console.getUI() ) );
  }

  public Console getConsole(String name) {
    return consoleMap.get(name);
  }

  public JComponent getUI() {
    return tab;
  }
}
