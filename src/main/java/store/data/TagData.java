package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TagData {

  public final int id;
  public final String title;

  public TagData(ResultSet response) throws SQLException {
    this.id = response.getInt("id");
    this.title = response.getString("title");
  }
}
