package store;

import com.google.common.hash.Hashing;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Store {

  private final String path;
  private final String dbPath;
  private final Connection connection;

  public QueryBuilder from(String table) {
    return new QueryBuilder(this.connection, table);
  }

  public void commit() {
    try {
      this.connection.commit();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  public void close() {
    try {
      this.commit();
      this.connection.close();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private String[] newClauseParams(int length, String value) {
    var arr = new String[length];

    Arrays.fill(arr, value);

    return arr;
  }

  private String newClause(String clause, String[] fields) {
    return clause + "(" + String.join(",", fields) + ")\n";
  }

  private String newInsertClause(String clause, String[] fields) {
    return (
      this.newClause(clause, fields) +
      this.newClause("VALUES", this.newClauseParams(fields.length, "?"))
    );
  }

  private void initializeTables() {
    try (var statement = this.connection.createStatement()) {
      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS tags",
            new String[] { "id integer PRIMARY KEY", "title text" }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS media",
            new String[] {
              "id text PRIMARY KEY",
              "type text",
              "show_id text",
              "url text",
              "image_path text",
              "media_path text",
              "season integer",
              "title text",
              "content text",
              "production_year text",
              "tags text",
              "ranking real",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE VIRTUAL TABLE IF NOT EXISTS search_table USING FTS5",
            new String[] {
              "id UNINDEXED",
              "type",
              "show_id UNINDEXED",
              "url UNINDEXED",
              "image_path UNINDEXED",
              "title",
              "content",
              "production_year",
              "tags",
              "ranking",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS seasons",
            new String[] {
              "id text PRIMARY KEY",
              "show_id text",
              "title text",
              "season integer",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS sessions",
            new String[] {
              "id integer PRIMARY KEY",
              "user_id integer",
              "favorites text",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS watching",
            new String[] {
              "id integer PRIMARY KEY",
              "session_id integer",
              "media_id text",
              "watching_id",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS progress",
            new String[] {
              "id integer PRIMARY KEY",
              "session_id integer",
              "media_id text",
              "progress real",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "CREATE TABLE IF NOT EXISTS users",
            new String[] {
              "id integer PRIMARY KEY",
              "email text",
              "password text",
              "name text",
              "country text",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "INSERT INTO users(email, password, name, country) VALUES",
            new String[] {
              "'user@domain.com'",
              "'" +
              Hashing
                .sha256()
                .hashString("password", StandardCharsets.UTF_8)
                .toString() +
              "'",
              "'Arthur Fonzarelli'",
              "'Denmark'",
            }
          )
      );

      statement.addBatch(
        this.newClause(
            "INSERT INTO sessions(user_id) VALUES",
            new String[] { "1" }
          )
      );

      statement.executeBatch();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private final Set<String> tags = new HashSet<>();

  private void insertTagsData() {
    try (var tagInfo = connection.createStatement()) {
      var query =
        "INSERT INTO tags(title)\n" +
        "VALUES\n" +
        String.join(
          ",\n",
          this.tags.stream().map(tag -> "('" + tag + "')").toList()
        );

      tagInfo.executeUpdate(query);
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private ZipInputStream newInputStream() throws IOException {
    return new ZipInputStream(
      this.getClass().getResourceAsStream("/public/data.zip"),
      Charset.forName("ISO-8859-1")
    );
  }

  private DecimalFormat newDecimalFormatter() {
    var seperator = new DecimalFormatSymbols();

    seperator.setDecimalSeparator(',');

    var decimalFormatter = new DecimalFormat();

    decimalFormatter.setDecimalFormatSymbols(seperator);

    return decimalFormatter;
  }

  private void insertMovieData(String[] fileEntries) {
    try (
      var mediaData = connection.prepareStatement(
        this.newInsertClause(
            "INSERT INTO media",
            new String[] {
              "id",
              "type",
              "url",
              "image_path",
              "media_path",
              "title",
              "content",
              "production_year",
              "tags",
              "ranking",
            }
          )
      )
    ) {
      var decimalFormatter = this.newDecimalFormatter();
      var i = 0;

      for (var entry : fileEntries) {
        var data = entry.split(";");
        var id = "movie-" + i++;
        var title = data[0];
        var productionYear = data[1].replaceAll("\\s", "");
        var tags = "Movie," + data[2].replaceAll("\\s", "");
        var rank = decimalFormatter
          .parse(data[3].replaceAll("\\s", ""))
          .floatValue();

        mediaData.setString(1, id);
        mediaData.setString(2, "movie");
        mediaData.setString(3, "/watch/movie/" + id);
        mediaData.setString(4, this.path + "/images/movies/" + title + ".jpg");
        mediaData.setString(5, this.path + "/placeholder-media.mp4");
        mediaData.setString(6, title);
        mediaData.setString(7, "No description.");
        mediaData.setString(8, productionYear);
        mediaData.setString(9, tags);
        mediaData.setFloat(10, rank);
        mediaData.addBatch();

        this.tags.addAll(Set.of(tags.replaceAll("\\s", "").split(",")));
      }

      mediaData.executeBatch();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private final Runnable getMovieData = () -> {
    try (var inputStream = this.newInputStream()) {
      ZipEntry file;

      while ((file = inputStream.getNextEntry()) != null) {
        if (!file.getName().equals("movies.txt")) {
          continue;
        }

        this.insertMovieData(
            new String(inputStream.readAllBytes()).split("\n")
          );

        break;
      }

      inputStream.closeEntry();
    } catch (Exception error) {
      error.printStackTrace();
    }
  };

  private void insertEpisodeData(
    String showId,
    String[] showData,
    int season,
    int episodes
  ) {
    try (
      var mediaData = this.connection.prepareStatement(
          this.newInsertClause(
              "INSERT INTO media",
              new String[] {
                "id",
                "type",
                "url",
                "show_id",
                "image_path",
                "media_path",
                "season",
                "title",
                "content",
                "production_year",
                "tags",
                "ranking",
              }
            )
        )
    ) {
      var decimalFormatter = this.newDecimalFormatter();
      var i = -1;

      while (++i < episodes) {
        var id = showId + "-season-" + season + "-episode-" + i;
        var title =
          showData[0] + "\nSeason " + (season + 1) + " - Episode " + (i + 1);
        var showTitle = showData[0];
        var productionYear = showData[1].replaceAll("\\s", "");
        var tags = "Show," + showData[2].replaceAll("\\s", "");
        var rank = decimalFormatter
          .parse(showData[3].replaceAll("\\s", ""))
          .floatValue();

        mediaData.setString(1, id);
        mediaData.setString(2, "episode");
        mediaData.setString(3, "/watch/show/" + showId + "/" + id);
        mediaData.setString(4, showId);
        mediaData.setString(
          5,
          this.path + "/images/shows/" + showTitle + ".jpg"
        );
        mediaData.setString(6, this.path + "/placeholder-media.mp4");
        mediaData.setInt(7, season);
        mediaData.setString(8, title);
        mediaData.setString(9, "No description.");
        mediaData.setString(10, productionYear);
        mediaData.setString(11, tags);
        mediaData.setFloat(12, rank);
        mediaData.addBatch();

        this.tags.addAll(Set.of(tags.replaceAll("\\s", "").split(",")));
      }

      mediaData.executeBatch();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private void insertSeasonData(
    String showId,
    String[] showData,
    String[] seasonEntries
  ) {
    try (
      var seasonData = this.connection.prepareStatement(
          this.newInsertClause(
              "INSERT INTO seasons",
              new String[] { "id", "show_id", "title", "season" }
            )
        )
    ) {
      var i = 0;
      for (var seasonEntry : seasonEntries) {
        var data = seasonEntry.split("-");
        var id = showId + "-season-" + i++;
        var season = Integer.parseInt(data[0]) - 1;
        var episodes = Integer.parseInt(data[1]);

        seasonData.setString(1, id);
        seasonData.setString(2, showId);
        seasonData.setString(3, "Season " + (season + 1));
        seasonData.setInt(4, season);
        seasonData.addBatch();

        this.insertEpisodeData(showId, showData, season, episodes);
      }

      seasonData.executeBatch();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private void insertShowData(String[] fileEntries) {
    try (
      var mediaData = connection.prepareStatement(
        this.newInsertClause(
            "INSERT INTO media",
            new String[] {
              "id",
              "type",
              "url",
              "image_path",
              "title",
              "content",
              "production_year",
              "tags",
              "ranking",
            }
          )
      )
    ) {
      var decimalFormatter = this.newDecimalFormatter();
      var i = 0;

      for (var fileEntry : fileEntries) {
        var data = fileEntry.split(";");
        var id = "show-" + i++;
        var title = data[0];
        var productionYear = data[1].replaceAll("\\s", "");
        var tags = "Show," + data[2].replaceAll("\\s", "");
        var rank = decimalFormatter
          .parse(data[3].replaceAll("\\s", ""))
          .floatValue();
        var seaonEntries = data[4].replaceAll("\\s", "").split(",");

        mediaData.setString(1, id);
        mediaData.setString(2, "show");
        mediaData.setString(
          3,
          "/watch/show/" + id + "/" + id + "-season-0-episode-0"
        );
        mediaData.setString(4, this.path + "/images/shows/" + title + ".jpg");
        mediaData.setString(5, title);
        mediaData.setString(6, "No description.");
        mediaData.setString(7, productionYear);
        mediaData.setString(8, tags);
        mediaData.setFloat(9, rank);
        mediaData.addBatch();

        this.insertSeasonData(id, data, seaonEntries);

        this.tags.addAll(Set.of(tags.replaceAll("\\s", "").split(",")));
      }

      mediaData.executeBatch();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private final void insertSearchData() {
    try (var searchData = this.connection.createStatement()) {
      searchData.executeUpdate(
        "INSERT INTO search_table\n" +
        "SELECT id, type, show_id, url, image_path, title, content, production_year, tags, ranking\n" +
        "FROM media WHERE type IN ('movie', 'show')"
      );
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  private final Runnable getShowData = () -> {
    try (var inputStream = this.newInputStream()) {
      ZipEntry zipEntry;

      while ((zipEntry = inputStream.getNextEntry()) != null) {
        if (!zipEntry.getName().equals("shows.txt")) {
          continue;
        }

        this.insertShowData(new String(inputStream.readAllBytes()).split("\n"));

        break;
      }

      inputStream.closeEntry();
    } catch (Exception error) {
      error.printStackTrace();
    }
  };

  private void insertData() {
    new Thread(() -> {
      this.initializeTables();

      var executor = Executors.newFixedThreadPool(2);

      executor.submit(this.getMovieData);
      executor.submit(this.getShowData);
      executor.shutdown();

      try {
        executor.awaitTermination(60, TimeUnit.SECONDS);
        TimeUnit.MILLISECONDS.sleep(100);
      } catch (Exception error) {
        error.printStackTrace();
      }

      this.insertSearchData();
      this.insertTagsData();

      try {
        this.connection.commit();
      } catch (Exception error) {
        error.printStackTrace();
      }
    })
      .start();
  }

  private void createExternalData() {
    try (var inputStream = this.newInputStream()) {
      var destination = new File(this.path);
      var buffer = new byte[1024];
      ZipEntry zipEntry;

      while ((zipEntry = inputStream.getNextEntry()) != null) {
        var file = new File(destination, zipEntry.getName());

        if (zipEntry.isDirectory()) {
          file.mkdirs();

          continue;
        }

        if (zipEntry.getName().contains(".txt")) {
          continue;
        }

        var outputStream = new FileOutputStream(file);
        var i = 0;

        while ((i = inputStream.read(buffer)) > 0) {
          outputStream.write(buffer, 0, i);
        }

        outputStream.close();
      }

      inputStream.closeEntry();
    } catch (Exception error) {
      error.printStackTrace();
    }
  }

  public Store(String path) {
    this.path = path;
    this.dbPath = "jdbc:sqlite:" + this.path + "/.db";

    var flag = new File(this.path).exists();

    if (!flag) {
      System.out.println("creating data");
      this.createExternalData();
    }

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(this.dbPath);
      connection.setAutoCommit(false);
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.connection = connection;

    if (!flag) {
      System.out.println("creating database");
      this.insertData();
    }
  }
}
