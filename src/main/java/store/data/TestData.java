package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TestData {

  public final int foo;
  public final String bar;
  public final List<Integer> baz;

  public TestData(ResultSet response) throws SQLException {
    this.foo = response.getInt("foo");
    this.bar = response.getString("bar");
    this.baz =
      List
        .of(response.getString("baz").split(","))
        .stream()
        .map(entry -> Integer.valueOf(entry))
        .toList();
  }
}
