package dataman.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import dataman.core.Console;

/**
 * ConsoleUI
 * @author kazuhiko arase
 */
public class ConsoleUI implements Console {

  private JTextPane textPane;

  public ConsoleUI() {

    final Action copyAction = new AbstractAction() {
      {
        putValue(NAME, "Clear");
      }
      @Override
      public void actionPerformed(ActionEvent e) {
        clear();
      }
    };

    textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setFont(new Font("monospaced", Font.PLAIN, 12) );
    textPane.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent event) {
        if (event.isPopupTrigger() &&
            event.getComponent() instanceof JTextPane) {
            JPopupMenu popup = new JPopupMenu();
            popup.add(new JMenuItem(copyAction) );
            popup.show(event.getComponent(),
                event.getX(), event.getY());
        }
      }
    });
  }

  public JComponent getUI() {
    return textPane;
  }

  public void log(Object msg) {
    final String timestamp =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS").
        format(new Date() );
    appendInfo(timestamp + " - " + msg);
  }

  public void appendInfo(Object log) {
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setForeground(set, Color.BLACK);
    Document doc = textPane.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), log + "\n", set);
    } catch(BadLocationException e) {
      e.printStackTrace();
    }
  }

  public void appendError(Object log) {
    SimpleAttributeSet set = new SimpleAttributeSet();
    StyleConstants.setForeground(set, Color.RED);
    Document doc = textPane.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), log + "\n", set);
    } catch(BadLocationException e) {
      e.printStackTrace();
    }
  }

  public void appendThrowable(Throwable t) {
    StringWriter sout = new StringWriter();
    PrintWriter pout = new PrintWriter(sout);
    try {
      t.printStackTrace(pout);
    } finally {
      pout.close();
    }
    appendError(sout.toString() );
  }

  public void clear() {
    Document doc = textPane.getStyledDocument();
    try {
      doc.remove(0, doc.getLength() );
    } catch(BadLocationException e) {
      e.printStackTrace();
    }
  }
}
