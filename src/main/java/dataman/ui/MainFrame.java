package dataman.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import dataman.core.Config;
import dataman.core.Console;
import dataman.model.CasePattern;
import dataman.model.DataSet;

/**
 * MainFrame
 * @author kazuhiko arase
 */
@SuppressWarnings("serial")
public class MainFrame extends JFrame{

  private static final String assetsPrefix = "/dataman/assets";
  private static final String appName = "TestDataGenerator";

  private ScriptEngine se;

  private DsTreeUI dsTree;
  private JTabbedPane tables;

  private ConsoleManagerUI consoleManager;
  private Console console;
  private JCheckBox hideDefaultValueColumns;
  private JComboBox casePatternCombo;

  private List<DataTableUI> dataTableUIList =
      new ArrayList<DataTableUI>();

  private ExecutorService es = Executors.newSingleThreadExecutor();;

  private JFileChooser chooser;
  private File lastFile;

  public MainFrame() throws Exception {

    List<Image> icons = new ArrayList<Image>();
    icons.add(new ImageIcon(
        getClass().getResource(assetsPrefix + "/grid.png") ).getImage() );
    icons.add(new ImageIcon(
        getClass().getResource(assetsPrefix + "/grid32x.png") ).getImage() );
    setIconImages(icons);

    consoleManager = new ConsoleManagerUI();
    console = consoleManager.getConsole(ConsoleManagerUI.CONSOLE_DEFAULT);

    setupDnD();

    setDefaultCloseOperation(EXIT_ON_CLOSE);

    chooser = new JFileChooser();
    chooser.setFileFilter(new FileFilter() {
      @Override
      public String getDescription() {
        return "test data generator script";
      }
      @Override
      public boolean accept(File f) {
        return f.isDirectory() ||
            (f.isFile() && f.getName().endsWith(".js") );
      }
    });

    dsTree = new DsTreeUI(consoleManager.
        getConsole(ConsoleManagerUI.CONSOLE_SNIPPET));

    tables = new JTabbedPane();
    tables.setPreferredSize(new Dimension(600, 400) );

    JSplitPane vpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    vpane.setBorder(new EmptyBorder(0,0,0,0) );
    vpane.setDividerSize(4);
    vpane.setTopComponent(tables);
    vpane.setBottomComponent(consoleManager.getUI() );
    vpane.setDividerLocation(300);

    hideDefaultValueColumns = new JCheckBox("hide default value columns");
    hideDefaultValueColumns.addActionListener(new AbstractAction() {
      {
//        putValue(key, newValue);
      }
      @Override
      public void actionPerformed(ActionEvent e) {
        for (DataTableUI dataTable : dataTableUIList) {
          dataTable.setHideDefaultValueColumns(
              hideDefaultValueColumns.isSelected() );
        }
      }
    });

    casePatternCombo = new JComboBox();
    casePatternCombo.setVisible(false);

    JPanel upperPane = new JPanel();
    upperPane.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0) );
    upperPane.add(hideDefaultValueColumns);
    upperPane.add(casePatternCombo);

    JPanel rPanel = new JPanel();
    rPanel.setLayout(new BorderLayout() );
    rPanel.add(BorderLayout.NORTH, upperPane);
    rPanel.add(BorderLayout.CENTER, vpane);

    JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    pane.setBorder(new EmptyBorder(0,0,0,0) );
    pane.setDividerSize(4);
    pane.setLeftComponent(dsTree.getUI() );
    pane.setRightComponent(rPanel);

    getContentPane().add(BorderLayout.CENTER, pane);

    JMenuBar menubar = new JMenuBar();
    JMenu fileMenu = new JMenu(new AbstractAction() {
      {
        putValue(NAME, "File");
        putValue(MNEMONIC_KEY, (int)'F');
      }
      @Override
      public void actionPerformed(ActionEvent e) {
      }
    });
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Open");
        putValue(MNEMONIC_KEY, (int)'O');
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          open();
        } catch(Exception e) {
          handleException(e);
        }
      }
    });
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Refresh");
        putValue(MNEMONIC_KEY, (int)'R');
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0) );
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          loadFile(lastFile);
        } catch(Exception e) {
          handleException(e);
        }
      }
    });
    fileMenu.addSeparator();
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Test Run");
        putValue(MNEMONIC_KEY, (int)'T');
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER,
            InputEvent.CTRL_DOWN_MASK) );
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        try {
          run(true, SqlRunner.RUN_DELETE | SqlRunner.RUN_INSERT);
        } catch(Exception e) {
          handleException(e);
        }
      }
    });
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Run delete");
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        int ret = confirmRun();
        if (ret != JOptionPane.OK_OPTION) {
          return;
        }
        try {
          run(false, SqlRunner.RUN_DELETE);
        } catch(Exception e) {
          handleException(e);
        }
      }
    });
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Run delete & insert");
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        int ret = confirmRun();
        if (ret != JOptionPane.OK_OPTION) {
          return;
        }
        try {
          run(false, SqlRunner.RUN_DELETE | SqlRunner.RUN_INSERT);
        } catch(Exception e) {
          handleException(e);
        }
      }
    });
    fileMenu.addSeparator();
    fileMenu.add(new AbstractAction() {
      {
        putValue(NAME, "Exit");
        putValue(MNEMONIC_KEY, (int)'X');
      }
      @Override
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
      }
    });
    menubar.add(fileMenu);
    setJMenuBar(menubar);
  }

  public Console getConsole() {
    return console;
  }

  private void setupDnD() {
    setDropTarget(new DropTarget() {

      private List<File> fileList;

      protected boolean checkFravor(DataFlavor[] flavors) {
        for(DataFlavor df : flavors) {
          if (df.isFlavorJavaFileListType() ) {
            return true;
          }
        }
        return false;
      }

      protected boolean checkTran(Transferable tran) {
        try {
          fileList = (List<File>)tran.
              getTransferData(DataFlavor.javaFileListFlavor);
          if (fileList.size() > 0 &&
              fileList.get(0).getName().endsWith(".js") ) {
            return true;
          }
        } catch(Exception e) {
        }
        return false;
      }

      @Override
      public synchronized void dragEnter(DropTargetDragEvent dtde) {
        if (!checkFravor(dtde.getCurrentDataFlavors() ) ) {
          dtde.rejectDrag();
          return;
        }
        if (!checkTran(dtde.getTransferable() ) ) {
          dtde.rejectDrag();
          return;
        }
        dtde.acceptDrag(DnDConstants.ACTION_COPY);
      }

      @Override
      public synchronized void drop(DropTargetDropEvent dtde) {
        if (!checkFravor(dtde.getCurrentDataFlavors() ) ) {
          dtde.rejectDrop();
          return;
        }
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        if (!checkTran(dtde.getTransferable() ) ) {
          return;
        }
        loadFile(fileList.get(0) );
      }
    });
  }

  private int confirmRun() {
    return JOptionPane.showConfirmDialog(this,
        "Do you wish to run?\nThis operation can't cancel.",
        "", JOptionPane.OK_CANCEL_OPTION);
  }

  public void start() throws Exception {

    ScriptEngineManager sem = new ScriptEngineManager();
    se = sem.getEngineByName("javascript");
    se.put("main", this);

    String[] srcList = {
        "/config.js",
        assetsPrefix + "/startup.js"
    };
    for (String src : srcList) {
      eval(src);
    }

    updateTitle(null);
    pack();
    setVisible(true);

    String lastFile = Config.getInstance().getProperty(Config.PROP_LAST_FILE);
    if (lastFile != null) {
      File file = new File(lastFile);
      if (file.exists() && file.isFile() ) {
        loadFile(file);
      }
    }
  }

  public void eval(String src) throws Exception {
    Reader in = new InputStreamReader(getClass().
        getResource(src).openStream(), "UTF-8");
    try {
      se.put(ScriptEngine.FILENAME, src);
      se.eval(in);
    } finally {
      in.close();
    }
  }

  public void evalfile(File path) throws Exception {
    Reader in =  new InputStreamReader(new FileInputStream(path), "UTF-8");
    try {
      se.put(ScriptEngine.FILENAME, path.getPath() );
      se.eval(in);
    } finally {
      in.close();
    }
  }

  private void updateTitle(File file) {
    if (file != null) {
      try {
        setTitle(file.getCanonicalPath() + " - " + appName);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      setTitle(appName);
    }
  }

  public void open() throws Exception {

    if (lastFile != null) {
      chooser.setSelectedFile(lastFile);
      chooser.setCurrentDirectory(lastFile.getParentFile() );
    }

    if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    if (chooser.getSelectedFile() != null) {
      loadFile(chooser.getSelectedFile() );
    }
  }

  public void run(boolean test, int run) throws Exception {

    DataSet dataSet = getDataSet();

    if (dataSet == null) {
      return;
    }

    new SqlRunner(consoleManager.getConsole(ConsoleManagerUI.CONSOLE_SQL) ).
      run(dsTree, dataSet, test, run);
  }

  private File toSqlPath(File path, String suffix) {
    String name= path.getName();
    if (!name.endsWith(".js") ) {
      throw new IllegalArgumentException("not js:" + path);
    }
    return new File(path.getParentFile(),
        name.substring(0, name.length() - 3) + suffix + ".sql");
  }

  private final Object taskLock = new Object();
  private boolean busy = false;

  public DataSet getDataSet() {
    synchronized(taskLock) {
      if (busy) {
        return null;
      }
      return workDataSet;
    }
  }

  private void loadFile(final File file) {
    runTask(new Task() {
      @Override
      public void run() throws Exception {
        MainFrame.this.lastFile = file;
        updateTitle(file);
        if (file == null) {
          return;
        }
        refreshImpl(file);
      }
    });
  }

  protected interface Task {
    void run() throws Exception;
  }

  protected void runTask(final Task task) {

    synchronized(taskLock) {

      if (busy) {
        return;
      }
      busy = true;

      es.execute(new Runnable() {
        @Override
        public void run() {
          try {
            task.run();
          } catch(Exception e) {
            handleException(e);
          } finally {
            synchronized(taskLock) {
              busy = false;
            }
          }
        }
      });
    }
  }

  public void addDataSource(String name,
      String driverClassName, String url, Properties info) {
    dsTree.addDataSource(name, driverClassName, url, info);
  }

  private List<CasePattern> casePatterns;
  private DataSet workDataSet;

  public void setDataSource(String dataSourceName) {
    workDataSet.setDataSource(dataSourceName);
  }

  public void addCasePattern(String name) {
    casePatterns.add(new CasePattern(
        String.valueOf(casePatterns.size() + 1), name) );
  }

  public void addPattern(int patternId, String column, Object value) {
    workDataSet.addPattern(patternId, column, value);
  }

  private void setupCaseCombo() throws Exception {
    final CasePattern selectedCase =
        (CasePattern)casePatternCombo.getSelectedItem();
    final DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
    int selectedCaseIndex = -1;
    for (int i = 0; i < casePatterns.size(); i += 1) {
      CasePattern casePattern = casePatterns.get(i);
      comboModel.addElement(casePattern);
      if (selectedCase != null &&
          selectedCase.getName().equals(casePattern.getName() ) ) {
        selectedCaseIndex = i;
      }
    }
    if (selectedCaseIndex == -1 && comboModel.getSize() > 0) {
      selectedCaseIndex = 0;
    }
    casePatternCombo.setModel(comboModel);
    if (selectedCaseIndex != -1) {
      casePatternCombo.setSelectedIndex(selectedCaseIndex);
    }
    casePatternCombo.setVisible(comboModel.getSize() > 0);
  }

  private void refreshImpl(File srcFile) throws Exception {

    console.clear();
    console.log("Refreshing...");

    casePatterns = new ArrayList<CasePattern>();
    workDataSet = new DataSet( (Invocable)se, dsTree);

    eval("/common.js");

    evalfile(srcFile);

    eval(assetsPrefix + "/loadProject.js");

    setupCaseCombo();

    workDataSet.setCasePattern(
        (CasePattern)casePatternCombo.getSelectedItem() );

    if (workDataSet.getCasePattern() != null) {
      console.log(workDataSet.getCasePattern() );
    }

    workDataSet.commit(console);

    Config.getInstance().setProperty(Config.PROP_LAST_FILE,
        srcFile.getAbsolutePath() );
    Config.getInstance().save();

    final List<DataTableUI> newUIList = workDataSet.createUIList(
        consoleManager.getConsole(ConsoleManagerUI.CONSOLE_INFO),
        consoleManager.getConsole(ConsoleManagerUI.CONSOLE_SNIPPET),
        hideDefaultValueColumns.isSelected() );

    Runnable doRun = new Runnable() {

      @Override
      public void run() {
        final String title = tables.getSelectedIndex() != -1?
            tables.getTitleAt(tables.getSelectedIndex() ) : "";

        int selectedIndex = -1;
        dataTableUIList.clear();
        tables.removeAll();

        for (DataTableUI dataTable : newUIList) {
          String tableName = dataTable.getTableDef().getTableName();
          if (tableName.equals(title) ) {
            selectedIndex = tables.getTabCount();
          }
          dataTableUIList.add(dataTable);
          tables.addTab(tableName, dataTable.getUI() );
        }

        if (selectedIndex != -1) {
          tables.setSelectedIndex(selectedIndex);
        }
      }
    };

    {
      PrintWriter out = new PrintWriter(
          toSqlPath(srcFile, "_del"), "UTF-8");
      try {
        workDataSet.outputDeleteSql(out);
      } finally {
        out.close();
      }
    }
    {
      final String suffix = workDataSet.getCasePattern() != null?
          "_" + workDataSet.getCasePattern().getId() : "";
      PrintWriter out = new PrintWriter(
          toSqlPath(srcFile, suffix + "_ins"), "UTF-8");
      try {
        workDataSet.outputInsertSql(out);
      } finally {
        out.close();
      }
    }

    SwingUtilities.invokeAndWait(doRun);

    console.log("Done.");
  }

  public void handleException(Exception e) {
    e.printStackTrace();
    console.appendThrowable(e);
  }

  public void initSample(String dataSource) throws Exception {

    StringBuilder buf = new StringBuilder();
    BufferedReader in = new BufferedReader(new InputStreamReader(
        getClass().getResourceAsStream(assetsPrefix + "/sample.sql") ) );
    try {
      String line;
      while ( (line = in.readLine() ) != null) {
        buf.append(line);
        buf.append('\n');
      }
    } finally {
      in.close();
    }

    Connection conn = dsTree.getConnection(dataSource);
    try {
      for (String sql : buf.toString().split(";") ) {
        sql = sql.trim();
        if (sql.length() == 0) {
          continue;
        }
        Statement stmt = conn.createStatement();
        try {
          stmt.executeQuery(sql);
        } finally {
          stmt.close();
        }
      }
      conn.commit();
    } finally {
      conn.rollback();
      conn.close();
    }
  }
}
