package store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryBuilder {

  private final Connection connection;
  private final String table;
  private List<String> fields;
  private Map<String, String> values;
  private String query = "";
  private String where = "";
  private String not = "";
  private String sort = "";

  public QueryBuilder select() {
    if (!this.query.isEmpty()) {
      return this;
    }

    this.query = String.join(" ", "SELECT", "*", "FROM", this.table);

    return this;
  }

  public QueryBuilder select(String... fields) {
    if (!this.query.isEmpty()) {
      return this;
    }

    this.query =
      String.join(" ", "SELECT", String.join(",", fields), "FROM", this.table);

    return this;
  }

  public QueryBuilder insert(String... fields) {
    if (!this.query.isEmpty()) {
      return this;
    }

    this.fields = new ArrayList<>(List.of(fields));
    this.values = new HashMap<>();

    this.query =
      String.join(
        " ",
        "INSERT INTO",
        this.table,
        "(" + String.join(",", this.fields) + ")"
      );

    return this;
  }

  public QueryBuilder setValue(String field, String value) {
    this.values.put(field, "'" + value + "'");

    return this;
  }

  public QueryBuilder setValue(String field, Number value) {
    this.values.put(field, String.valueOf(value));

    return this;
  }

  public <T> QueryBuilder setValue(String field, Collection<T> array) {
    if (array == null || array.size() == 0) {
      this.values.put(field, "null");

      return this;
    }

    this.values.put(
        field,
        "'" +
        array
          .stream()
          .map(entry -> {
            if (entry instanceof String) {
              return (String) entry;
            }

            if (entry instanceof Number) {
              return String.valueOf(entry);
            }

            return entry.toString();
          })
          .collect(Collectors.joining(",")) +
        "'"
      );

    return this;
  }

  public QueryBuilder setValues(Map<String, ?> values) {
    values.forEach((key, value) -> {
      if (value instanceof String) {
        this.setValue(key, (String) value);

        return;
      }

      if (value instanceof Number) {
        this.setValue(key, (Number) value);

        return;
      }

      if (value instanceof Collection) {
        this.setValue(key, (Collection<?>) value);

        return;
      }

      this.values.put(key, value.toString());
    });

    return this;
  }

  public QueryBuilder not() {
    if (!this.not.isEmpty()) {
      return this;
    }

    this.not = "NOT ";

    return this;
  }

  public QueryBuilder where(String field, int value) {
    this.where += this.where.isEmpty() ? "WHERE " : "\nAND ";

    this.where += String.join(" ", field, "=", String.valueOf(value));

    return this;
  }

  public QueryBuilder where(String field, String value) {
    this.where += this.where.isEmpty() ? "WHERE " : "\nAND ";

    this.where += String.join(" ", field, "=", "'" + value + "'");

    return this;
  }

  public QueryBuilder where(String field, String... values) {
    this.where += this.where.isEmpty() ? "WHERE " : "\nAND ";

    this.where +=
      String.join(
        " ",
        field,
        this.not + "IN",
        "('" + String.join("','", values) + "')"
      );

    return this;
  }

  public QueryBuilder like(String field, String value) {
    this.where += this.where.isEmpty() ? "WHERE " : "\nAND ";

    this.where += String.join(" ", field, "LIKE", "'%" + value + "%'");

    return this;
  }

  public QueryBuilder textSearch(String value) {
    this.where += this.where.isEmpty() ? "WHERE " : "\nAND ";

    var terms = List
      .of(value.split(" "))
      .stream()
      .map(entry -> "\"" + entry + "\"*")
      .collect(Collectors.joining(" "));

    this.where += "search_table MATCH " + "'" + terms + "'";
    this.sort = "ORDER BY rank";

    return this;
  }

  public QueryBuilder sort(String... fields) {
    if (!this.sort.isEmpty()) {
      return this;
    }

    this.sort =
      "ORDER BY " +
      List
        .of(fields)
        .stream()
        .map(entry -> entry + " DESC")
        .collect(Collectors.joining(","));

    return this;
  }

  private final String build() {
    var list = new ArrayList<String>();

    list.add(this.query);

    if (this.values != null) {
      list.add(
        "VALUES " +
        String.join(
          "",
          "(",
          this.fields.stream()
            .map(entry -> this.values.get(entry))
            .collect(Collectors.joining(",")),
          ")"
        )
      );
      list.add("ON CONFLICT(id) DO UPDATE SET");
      list.add(
        this.fields.stream()
          .filter(entry -> !entry.equals("id"))
          .map(entry -> entry + "=" + this.values.get(entry))
          .collect(Collectors.joining(",\n"))
      );
      list.add("RETURNING *");
    }

    if (!this.where.isEmpty()) {
      list.add(this.where);
    }

    if (!this.sort.isEmpty()) {
      list.add(this.sort);
    }

    return String.join("\n", list);
  }

  public ResultSet execute() {
    try {
      return connection.createStatement().executeQuery(this.build());
    } catch (SQLException error) {
      error.printStackTrace();
    }

    return null;
  }

  public void delete() {
    var list = new ArrayList<String>();

    list.add("DELETE FROM " + this.table);

    if (!this.where.isEmpty()) {
      list.add(this.where);
    }

    try {
      connection.createStatement().executeUpdate(String.join("\n", list));
    } catch (SQLException error) {
      error.printStackTrace();
    }
  }

  public <T> ArrayList<T> asArrayList(Class<T> type) {
    var list = new ArrayList<T>();

    try (var response = this.execute()) {
      while (response.next()) {
        list.add(
          type.getDeclaredConstructor(ResultSet.class).newInstance(response)
        );
      }
    } catch (Exception error) {
      error.printStackTrace();
    }

    return list;
  }

  public <T> T asSingle(Class<T> type) {
    return this.asArrayList(type).stream().findFirst().orElse(null);
  }

  public QueryBuilder(Connection connection, String table) {
    this.connection = connection;
    this.table = table;
  }

  @Override
  public String toString() {
    return this.getClass().getName() + "=" + this.build();
  }
}
