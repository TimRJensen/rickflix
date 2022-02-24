package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionData {

  public final int id;
  public final Set<String> favorites = new HashSet<>();
  public final List<WatchingData> watching = new ArrayList<>();
  public final List<ProgressData> progress = new ArrayList<>();

  public SessionData(ResultSet response) throws SQLException {
    this.id = response.getInt("id");

    var favorites = response.getString("favorites");

    if (favorites != null) {
      this.favorites.addAll(List.of(favorites.split(",")));
    }

    var connection = response.getStatement().getConnection();

    try (
      var _response = connection
        .createStatement()
        .executeQuery(
          "SELECT * from watching WHERE session_id = " + "'" + this.id + "'"
        )
    ) {
      while (_response.next()) {
        this.watching.add(new WatchingData(_response));
      }
    } catch (Exception error) {
      error.printStackTrace();
    }

    try (
      var _response = connection
        .createStatement()
        .executeQuery(
          "SELECT * FROM progress WHERE session_id = " + "'" + this.id + "'"
        )
    ) {
      while (_response.next()) {
        this.progress.add(new ProgressData(_response));
      }
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
