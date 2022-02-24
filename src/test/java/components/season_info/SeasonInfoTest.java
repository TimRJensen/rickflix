package components.season_info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import store.Session;
import store.data.SeasonData;
import store.data.SessionData;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ApplicationExtension.class)
public class SeasonInfoTest extends App {

  private SeasonInfo seasonInfo;

  @Start
  public void start(Stage stage) {
    try {
      var seasonData = new ArrayList<SeasonData>();
      var i = -1;
      while (++i < 10) {
        var connection = mock(Connection.class);
        var statement = mock(Statement.class);
        var response = mock(ResultSet.class);

        when(response.getString("id"))
          .thenReturn("show-0", "media-" + i, "media-" + (i + 1));
        when(response.getString("show_id")).thenReturn("show-0");
        when(response.getString("type")).thenReturn(null);
        when(response.getString("url"))
          .thenReturn("/foo/media-" + i, "/foo/media-" + (i + 1));
        when(response.getString("image_path"))
          .thenReturn(
            this.getClass()
              .getResource("/public/placeholder-image.png")
              .getPath()
          );
        when(response.getInt("season")).thenReturn(i % 2);
        when(response.getString("title"))
          .thenReturn(
            "season-" + (i / 2),
            "season-" + (i / 2) + "-episode-1",
            "season-" + (i / 2) + "-episode-2"
          );
        when(response.getString("content")).thenReturn("content");
        when(response.getString("tags")).thenReturn("foo,bar");
        when(response.getStatement()).thenReturn(statement);
        when(response.next()).thenReturn(true, true, false);

        when(statement.getConnection()).thenReturn(connection);
        when(statement.executeQuery(anyString())).thenReturn(response);

        when(connection.createStatement()).thenReturn(statement);

        seasonData.add(new SeasonData(response));

        if (i == 0) {
          new Session(new SessionData(response));
        }

        i++;
      }

      this.seasonInfo =
        new SeasonInfo(seasonData.get(0).episodes.get(0), seasonData);
      this.seasonInfo.getStyleClass().add("season-info");
    } catch (SQLException e) {
      e.printStackTrace();
    }

    var root = new StackPane();

    root.getChildren().add(this.seasonInfo);
    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName(
    "Assert that new SeasonInfo displays Links & MediaCardContainer, & first Link is toggled"
  )
  void newInstance(FxRobot robot) {
    var linkGroup = robot.lookup("#linkGroup").queryAs(Pane.class);
    var mediaCardContainer = robot
      .lookup("#mediaCardContainer")
      .queryAs(Pane.class);
    var firstLink = robot.lookup("season-0").queryAs(Label.class);

    assertEquals(5, linkGroup.getChildren().size());
    assertEquals(2, mediaCardContainer.getChildren().size());
    assertEquals("season-0", firstLink.getText());
    assertEquals(true, firstLink.getStyleClass().contains("toggled"));
  }

  @Test
  @DisplayName(
    "Assert that #handleClick toggles Link on/off & MediaCardContainer contains next MediaCards"
  )
  void handleClick(FxRobot robot) throws InterruptedException {
    var nextLink = robot.lookup("season-2").queryAs(Label.class);

    robot.clickOn(nextLink);

    var mediaCardContainer = robot
      .lookup("#mediaCardContainer")
      .queryAs(Pane.class);

    assertEquals(2, mediaCardContainer.getChildren().size());
    assertEquals(true, nextLink.getStyleClass().contains("toggled"));
    assertNotNull(robot.lookup("season-2-episode-1"));
  }
}
