package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShowData extends MediaData {

  public final List<SeasonData> seasons = new ArrayList<>();

  public ShowData(ResultSet response) throws SQLException {
    super(response);
    var connection = response.getStatement().getConnection();

    try (
      var _response = connection
        .createStatement()
        .executeQuery(
          "SELECT * from seasons WHERE show_id = " + "'" + this.id + "'"
        )
    ) {
      while (_response.next()) {
        this.seasons.add(new SeasonData(_response));
      }
    } catch (Exception error) {
      error.printStackTrace();
    }
  }
}
