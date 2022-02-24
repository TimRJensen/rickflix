package components.media_info;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import components.tag.Tag;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import store.Session;
import store.data.MediaData;
import store.data.SessionData;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ApplicationExtension.class)
public class MediaInfoTest extends App {

  private String testString;
  private MediaInfo mediaInfo;

  @Start
  public void start(Stage stage) {
    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var response = mock(ResultSet.class);

    try {
      when(response.getString("id")).thenReturn("media-0");
      when(response.getString("title")).thenReturn("title");
      when(response.getString("content")).thenReturn("content");
      when(response.getString("tags")).thenReturn("foo,bar");
      when(response.getString("favorites")).thenReturn("media-0");
      when(response.getStatement()).thenReturn(statement);

      when(statement.getConnection()).thenReturn(connection);
      when(statement.executeQuery(anyString())).thenReturn(response);

      when(connection.createStatement()).thenReturn(statement);

      new Session(new SessionData(response));

      this.mediaInfo = new MediaInfo(new MediaData(response), true);
      this.mediaInfo.setOnClose(event -> {
          this.testString = "foo";
        });
    } catch (SQLException e) {
      e.printStackTrace();
    }

    var root = new StackPane();

    root.getChildren().add(this.mediaInfo);
    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  @Test
  @DisplayName(
    "Assert that new MediaInfo displays title, content, tags & $favoriteControl is toggled"
  )
  void newInstance(FxRobot robot) {
    assertEquals("foo", robot.lookup("foo").queryAs(Tag.class).getText());
    assertEquals("title", robot.lookup("title").queryAs(Text.class).getText());
    assertEquals(
      "content",
      robot.lookup("content").queryAs(Text.class).getText()
    );
    assertEquals(
      true,
      robot
        .lookup("#favoriteControl")
        .query()
        .getStyleClass()
        .contains("toggled")
    );
  }

  @Test
  @DisplayName("Assert that #handleFavorting toggles #favoriteControl off")
  void handleFavoriting(FxRobot robot) {
    var favoriteControl = robot.lookup("#favoriteControl").query();

    assertEquals(true, Session.getCurrent().favorites.contains("media-0"));

    robot.clickOn(favoriteControl);

    assertEquals(false, Session.getCurrent().favorites.contains("media-0"));
    assertEquals(false, favoriteControl.getStyleClass().contains("toggled"));
  }

  @Test
  @DisplayName("Assert that #handleClose executes callback")
  void handleClose(FxRobot robot) {
    var closeControl = robot.lookup("#closeControl").query();

    robot.clickOn(closeControl);

    assertEquals("foo", this.testString);
  }
}
