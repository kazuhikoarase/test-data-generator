package dataman.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Config
 * @author kazuhiko arase
 */
public class Config {

  public static final String PROP_LAST_FILE = "PROP_LAST_FILE";

  private static Config instance;

  public static Config getInstance() {
    if (instance == null) {
      instance = new Config();
      instance.load();
    }
    return instance;
  }

  private Properties props = new Properties();

  private Config() {
  }

  public void setProperty(String name, String value) {
    props.setProperty(name, value);
  }

  public String getProperty(String name) {
    return props.getProperty(name);
  }

  private File getConfigFile() {
    return new File(System.getProperty("user.home"),
        ".test_data_generator.properties");
  }

  public void load() {
    try {
      File file= getConfigFile();
      if (!file.exists() ) {
        return;
      }
      InputStream in = new BufferedInputStream(
          new FileInputStream(file) );
      try {
        props.load(in);
      } finally {
        in.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void save() {
    try {
      OutputStream out = new BufferedOutputStream(
          new FileOutputStream(getConfigFile() ) );
      try {
        props.store(out, "");
      } finally {
        out.close();
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
