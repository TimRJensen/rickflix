package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SeasonData {

  public final String id;
  public final String title;
  public final int season;
  public final List<MediaData> episodes = new ArrayList<>();

  public SeasonData(ResultSet response) throws SQLException {
    this.id = response.getString("id");
    this.title = response.getString("title");
    this.season = response.getInt("season");

    var connection = response.getStatement().getConnection();

    try (
      var _response = connection
        .createStatement()
        .executeQuery(
          String.join(
            " ",
            List.of(
              "SELECT * FROM media WHERE id LIKE",
              "'%" + id + "%'",
              "AND type = 'episode'"
            )
          )
        )
    ) {
      while (_response.next()) {
        this.episodes.add(new MediaData(_response));
      }
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
