package dataman.model;

/**
 * DataObjectRow
 * @author kazuhiko arase
 */
public class DataObjectRow {

  private DataObject[] data;

  private boolean deleted;

  private String comment;

  public DataObjectRow(int size) {
    data = new DataObject[size];
    deleted = false;
    comment = null;
  }

  public DataObject get(int index) {
    return data[index];
  }

  public void set(int index, DataObject value) {
    data[index] = value;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
