package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WatchingData {

  public final int id;
  public final int sessionId;
  public final String mediaId;
  public final String watchingId;

  public WatchingData(ResultSet response) throws SQLException {
    this.id = response.getInt("id");
    this.sessionId = response.getInt("session_id");
    this.mediaId = response.getString("media_id");
    this.watchingId = response.getString("watching_id");
  }

  public WatchingData(WatchingData data, String watchingId) {
    this.id = data.id;
    this.sessionId = data.sessionId;
    this.mediaId = data.mediaId;
    this.watchingId = watchingId;
  }
}
