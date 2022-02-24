package components.media_modal;

import static javafx.geometry.Pos.TOP_CENTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import components.media_card.MediaCard;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
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
import store.data.MediaData;
import store.data.SessionData;
import util.Route;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ApplicationExtension.class)
public class MediaModalTest extends App {

  private MediaCard mediaCard;
  private MediaModal mediaModal;

  @Start
  public void start(Stage stage) {
    this.router.routes.put("/foo/bar", new Route("/foo/bar", null));

    var anchor = new StackPane();

    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var response = mock(ResultSet.class);

    try {
      when(response.getString("id")).thenReturn("media-0");
      when(response.getString("url")).thenReturn("/foo/bar");
      when(response.getString("image_path"))
        .thenReturn(
          this.getClass().getResource("/public/placeholder-image.png").getPath()
        );
      when(response.getString("title")).thenReturn("title");
      when(response.getString("content")).thenReturn("content");
      when(response.getString("tags")).thenReturn("foo,bar");
      when(response.getStatement()).thenReturn(statement);

      when(statement.getConnection()).thenReturn(connection);
      when(statement.executeQuery(anyString())).thenReturn(response);

      when(connection.createStatement()).thenReturn(statement);

      new Session(new SessionData(response));

      this.mediaCard = new MediaCard(new MediaData(response), anchor);
      this.mediaModal =
        new MediaModal(anchor, this.mediaCard, new MediaData(response));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    anchor
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());
    anchor.setAlignment(TOP_CENTER);

    stage.setScene(new Scene(anchor, 1000, 400));
    stage.show();

    anchor.getChildren().addAll(this.mediaCard, this.mediaModal);
  }

  @Test
  @DisplayName("Assert that new MediaModal loads image")
  void newInstance(FxRobot robot) {
    var imageView = robot
      .lookup("#content > StackPane > #imageBox")
      .queryAs(ImageView.class);

    assertEquals(
      "file:" +
      this.getClass().getResource("/public/placeholder-image.png").getPath(),
      imageView.getImage().getUrl()
    );
  }

  @Test
  @DisplayName("Assert that #handleRedirect navigates to location")
  void handleRedirect(FxRobot robot) {
    var redirectControl = robot
      .lookup("#content > StackPane > FontIconButton")
      .query();

    robot.clickOn(redirectControl);
    assertEquals("/foo/bar", this.router.getLocation().path);
  }

  @Test
  @DisplayName("Assert that #handleClose closses the modal")
  void handleClose(FxRobot robot) {
    var closeControl = robot.lookup(".root MediaInfo #closeControl").query();

    robot.clickOn(closeControl);

    assertNull(this.mediaModal.getScene());
  }
}
