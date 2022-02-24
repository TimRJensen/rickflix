package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProgressData {

  public final int id;
  public final int sessionId;
  public final String mediaId;
  public final Double progress;

  public ProgressData(ResultSet response) throws SQLException {
    this.id = response.getInt("id");
    this.sessionId = response.getInt("session_id");
    this.mediaId = response.getString("media_id");
    this.progress = response.getDouble("progress");
  }

  public ProgressData(ProgressData data, double progress) {
    this.id = data.id;
    this.sessionId = data.sessionId;
    this.mediaId = data.mediaId;
    this.progress = progress;
  }
}
