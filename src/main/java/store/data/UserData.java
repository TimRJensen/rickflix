package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserData {

  public final int id;
  public final String email;
  public final String password;
  public final String name;
  public final String country;
  public final SessionData session;

  public UserData(ResultSet response) throws SQLException {
    this.id = response.getInt("id");
    this.email = response.getString("email");
    this.password = response.getString("password");
    this.name = response.getString("name");
    this.country = response.getString("country");

    var connection = response.getStatement().getConnection();

    SessionData session = null;

    try (
      var _response = connection
        .createStatement()
        .executeQuery(
          "SELECT * FROM sessions WHERE user_id = " + "'" + this.id + "'"
        )
    ) {
      while (_response.next()) {
        session = new SessionData(_response);
      }
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.session = session;
  }
}
