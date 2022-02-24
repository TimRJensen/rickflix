package store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DriverManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.data.TestData;

public class QueryBuilderTest {

  private static Store store;

  @BeforeAll
  static void initTest() throws InterruptedException {
    var path = Paths.get("").resolve(".test-data").toAbsolutePath().toString();
    store = new Store(path);

    // TimeUnit.SECONDS.sleep(1);

    try (
      var connection = DriverManager.getConnection(
        "jdbc:sqlite:" + path + "/.db"
      )
    ) {
      connection
        .createStatement()
        .executeUpdate(
          String.join(
            "\n",
            "CREATE TABLE IF NOT EXISTS test_table (",
            "id integer PRIMARY KEY,",
            "foo integer,",
            "bar text,",
            "baz text",
            ")"
          )
        );
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  @AfterAll
  static void cleanAllTests() throws InterruptedException {
    store.close();

    try {
      Files
        .walk(Paths.get("").resolve(".test-data").toAbsolutePath())
        .sorted(Comparator.reverseOrder())
        .map(entry -> entry.toFile())
        .forEach(entry -> entry.delete());
    } catch (Exception never) {
      never.printStackTrace();
    }
  }

  @Test
  @DisplayName(
    "Assert that new instance has initial #toString of #toString + = + ''"
  )
  void newInstance() {
    assertEquals("store.QueryBuilder=", store.from("test_table").toString());
  }

  @Test
  @DisplayName("Assert that #select matches query.")
  void select() {
    assertEquals(
      "store.QueryBuilder=SELECT * FROM test_table",
      store.from("test_table").select().toString()
    );
  }

  @Test
  @DisplayName("Assert that #select with parameters matches query.")
  void selectWithParams() {
    assertEquals(
      "store.QueryBuilder=SELECT foo,bar FROM test_table",
      store.from("test_table").select("foo", "bar").toString()
    );
  }

  @Test
  @DisplayName("Assert that #select#sort with parameters matches query.")
  void selectWithParamsWithSort() {
    assertEquals(
      "store.QueryBuilder=SELECT foo,bar FROM test_table\n" +
      "ORDER BY foo DESC,bar DESC",
      store
        .from("test_table")
        .select("foo", "bar")
        .sort("foo", "bar")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert with parameters matches query.")
  void insert() {
    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar)\n" +
      "VALUES (null,null,null)\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo=null,\n" +
      "bar=null\n" +
      "RETURNING *",
      store.from("test_table").insert("id", "foo", "bar").toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert#setValue with string matches query.")
  void insertStringValue() {
    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar)\n" +
      "VALUES (null,'foo','bar')\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo='foo',\n" +
      "bar='bar'\n" +
      "RETURNING *",
      store
        .from("test_table")
        .insert("id", "foo", "bar")
        .setValue("foo", "foo")
        .setValue("bar", "bar")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert#setValue with number matches query.")
  void insertNumberValue() {
    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar)\n" +
      "VALUES (null,1,10.0)\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo=1,\n" +
      "bar=10.0\n" +
      "RETURNING *",
      store
        .from("test_table")
        .insert("id", "foo", "bar")
        .setValue("foo", 1)
        .setValue("bar", 10.0)
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert#setValue with empty array matches query.")
  void insertEmptyArrayValue() {
    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar)\n" +
      "VALUES (null,null,null)\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo=null,\n" +
      "bar=null\n" +
      "RETURNING *",
      store
        .from("test_table")
        .insert("id", "foo", "bar")
        .setValue("foo", List.of())
        .setValue("bar", List.of())
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert#setValue with array matches query.")
  void insertArrayValue() {
    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar)\n" +
      "VALUES (null,'foo,bar','1,3.14')\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo='foo,bar',\n" +
      "bar='1,3.14'\n" +
      "RETURNING *",
      store
        .from("test_table")
        .insert("id", "foo", "bar")
        .setValue("foo", List.of("foo", "bar"))
        .setValue("bar", List.of(1, 3.14))
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #insert#setValues with map matches query.")
  void inserValues() {
    var map = new HashMap<String, Object>();

    map.put("foo", "foo");
    map.put("bar", 1);
    map.put("baz", 3.14);
    map.put("qux", List.of("foo", "bar"));

    assertEquals(
      "store.QueryBuilder=INSERT INTO test_table (id,foo,bar,baz,qux)\n" +
      "VALUES (null,'foo',1,3.14,'foo,bar')\n" +
      "ON CONFLICT(id) DO UPDATE SET\n" +
      "foo='foo',\n" +
      "bar=1,\n" +
      "baz=3.14,\n" +
      "qux='foo,bar'\n" +
      "RETURNING *",
      store
        .from("test_table")
        .insert("id", "foo", "bar", "baz", "qux")
        .setValues(map)
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #where with string matches query.")
  void whereWithString() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" + "WHERE foo = 'baz'",
      store.from("test_table").select("foo").where("foo", "baz").toString()
    );
  }

  @Test
  @DisplayName("Assert that #where with int matches query.")
  void whereWithInt() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" + "WHERE foo = 1",
      store.from("test_table").select("foo").where("foo", 1).toString()
    );
  }

  @Test
  @DisplayName("Assert that #where with string array matches query.")
  void whereWithMultipleStrings() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo IN ('bar','baz')",
      store
        .from("test_table")
        .select("foo")
        .where("foo", "bar", "baz")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #where#where with string matches query.")
  void whereWithAnd() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo = 'baz'\n" +
      "AND bar = 'baz'\n" +
      "AND baz = 'foo'",
      store
        .from("test_table")
        .select("foo")
        .where("foo", "baz")
        .where("bar", "baz")
        .where("baz", "foo")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #where#not with string matches query.")
  void whereWithNot() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo NOT IN ('foo','bar')",
      store
        .from("test_table")
        .select("foo")
        .not()
        .where("foo", "foo", "bar")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #like with string matches query.")
  void like() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo LIKE '%baz%'",
      store.from("test_table").select("foo").like("foo", "baz").toString()
    );
  }

  @Test
  @DisplayName("Assert that #where#like with string matches query.")
  void whereWithLike() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo = 'baz'\n" +
      "AND foo LIKE '%baz%'",
      store
        .from("test_table")
        .select("foo")
        .where("foo", "baz")
        .like("foo", "baz")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #textSearch with string matches query.")
  void textSearch() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE search_table MATCH '\"baz\"*'\n" +
      "ORDER BY rank",
      store.from("test_table").select("foo").textSearch("baz").toString()
    );
  }

  @Test
  @DisplayName("Assert that #where#textSearch with string matches query.")
  void whereWithTextSearch() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo = 'baz'\n" +
      "AND search_table MATCH '\"baz\"*'\n" +
      "ORDER BY rank",
      store
        .from("test_table")
        .select("foo")
        .where("foo", "baz")
        .textSearch("baz")
        .toString()
    );
  }

  @Test
  @DisplayName("Assert that #where#textSearch#sort with string matches query.")
  void selectWithWhereWithStringWithSort() {
    assertEquals(
      "store.QueryBuilder=SELECT foo FROM test_table\n" +
      "WHERE foo = 'baz'\n" +
      "ORDER BY foo DESC",
      store
        .from("test_table")
        .select("foo")
        .where("foo", "baz")
        .sort("foo")
        .toString()
    );
  }

  @Test
  @DisplayName(
    "Assert that #insert#setValue with number matches inserted value."
  )
  void insertNumber() {
    try (
      var response = store
        .from("test_table")
        .insert("id", "foo")
        .setValue("id", 0)
        .setValue("foo", 1)
        .execute()
    ) {
      while (response.next()) {
        assertEquals(1, response.getInt("foo"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName(
    "Assert that #insert#setValue with string matches inserted value."
  )
  void insertString() {
    try (
      var response = store
        .from("test_table")
        .insert("id", "bar")
        .setValue("id", 0)
        .setValue("bar", "bar")
        .execute()
    ) {
      while (response.next()) {
        assertEquals("bar", response.getString("bar"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName(
    "Assert that #insert#setValue with array matches inserted value."
  )
  void insertArray() {
    try (
      var response = store
        .from("test_table")
        .insert("id", "baz")
        .setValue("id", 0)
        .setValue("baz", List.of(1, 2, 3))
        .execute()
    ) {
      while (response.next()) {
        assertEquals("1,2,3", response.getString("baz"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName(
    "Assert that #insert#setValues with map matches inserted values."
  )
  void insertValues() {
    var values = new HashMap<String, Object>();

    values.put("foo", 1);
    values.put("bar", "bar");
    values.put("baz", List.of(1, 2, 3));

    try (
      var response = store
        .from("test_table")
        .insert("id", "foo", "bar", "baz")
        .setValue("id", 0)
        .setValues(values)
        .execute()
    ) {
      while (response.next()) {
        assertEquals(1, response.getInt("foo"));
        assertEquals("bar", response.getString("bar"));
        assertEquals("1,2,3", response.getString("baz"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName("Assert that #insert#setValues with map matches updated values.")
  void upsert() {
    var values = new HashMap<String, Object>();

    values.put("foo", 1);
    values.put("bar", "bar");
    values.put("baz", List.of(1, 2, 3));

    try (
      var response = store
        .from("test_table")
        .insert("id", "foo", "bar", "baz")
        .setValue("id", 0)
        .setValues(values)
        .execute()
    ) {} catch (Exception never) {}

    values.put("foo", 2);
    values.put("bar", "rab");
    values.put("baz", List.of(3, 2, 1));

    try (
      var response = store
        .from("test_table")
        .insert("id", "foo", "bar", "baz")
        .setValue("id", 0)
        .setValues(values)
        .execute()
    ) {
      while (response.next()) {
        assertEquals(2, response.getInt("foo"));
        assertEquals("rab", response.getString("bar"));
        assertEquals("3,2,1", response.getString("baz"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName("Assert that #delete deletes matched query.")
  void delete() {
    var values = new HashMap<String, Object>();

    values.put("foo", 1);
    values.put("bar", "bar");
    values.put("baz", List.of(1, 2, 3));

    try (
      var responseA = store
        .from("test_table")
        .insert("id", "foo", "bar", "baz")
        .setValue("id", 0)
        .setValues(values)
        .execute();
      var responseB = store
        .from("test_table")
        .insert("id", "foo", "bar", "baz")
        .setValue("id", 1)
        .setValues(values)
        .execute()
    ) {} catch (Exception never) {}

    store.from("test_table").where("id", 0).delete();

    try (
      var responseA = store
        .from("test_table")
        .select()
        .where("id", 0)
        .execute();
      var responseB = store.from("test_table").select().where("id", 1).execute()
    ) {
      while (responseA.next()) {
        assertNull(responseA.getInt("foo"));
      }

      while (responseB.next()) {
        assertEquals(1, responseB.getInt("foo"));
      }
    } catch (Exception never) {}
  }

  @Test
  @DisplayName("Assert that #asArrayList contains query.")
  void asArrayList() {
    var values = new HashMap<String, Object>();

    values.put("foo", 1);
    values.put("bar", "bar");
    values.put("baz", List.of(1, 2, 3));

    for (var i = 0; i < 5; i++) {
      try (
        var response = store
          .from("test_table")
          .insert("foo", "bar", "baz")
          .setValues(values)
          .execute()
      ) {} catch (Exception never) {}
    }

    var testData = store
      .from("test_table")
      .select()
      .asArrayList(TestData.class);

    var first = testData.get(0);

    assertEquals(1, first.foo);
    assertEquals("bar", first.bar);
    assertEquals(3, first.baz.size());
    assertEquals(1, first.baz.get(0));

    var last = testData.get(testData.size() - 1);

    assertEquals(1, last.foo);
    assertEquals("bar", last.bar);
    assertEquals(3, last.baz.size());
    assertEquals(1, first.baz.get(0));
  }

  @Test
  @DisplayName("Assert that #asSingle contains query.")
  void asSingle() {
    var values = new HashMap<String, Object>();

    values.put("foo", 1);
    values.put("bar", "bar");
    values.put("baz", List.of(1, 2, 3));

    try (
      var response = store
        .from("test_table")
        .insert("foo", "bar", "baz")
        .setValues(values)
        .execute()
    ) {} catch (Exception never) {}

    var testData = store.from("test_table").select().asSingle(TestData.class);

    assertEquals(1, testData.foo);
    assertEquals("bar", testData.bar);
    assertEquals(3, testData.baz.size());
    assertEquals(1, testData.baz.get(0));
  }
}
