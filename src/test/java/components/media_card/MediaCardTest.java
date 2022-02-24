package components.media_card;

import static javafx.geometry.Pos.TOP_CENTER;
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
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
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
public class MediaCardTest extends App {

  private MediaCard mediaCard;

  @Start
  public void start(Stage stage) {
    var anchor = new StackPane();

    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var response = mock(ResultSet.class);

    try {
      when(response.getString("id")).thenReturn("media-0");
      when(response.getString("image_path"))
        .thenReturn(
          this.getClass().getResource("/public/placeholder-image.png").getPath()
        );
      when(response.getString("title")).thenReturn("title");
      when(response.getString("content")).thenReturn(null);
      when(response.getString("production_year")).thenReturn(null);
      when(response.getString("tags")).thenReturn("foo,bar");
      when(response.getStatement()).thenReturn(statement);

      when(statement.getConnection()).thenReturn(connection);
      when(statement.executeQuery(anyString())).thenReturn(response);

      when(connection.createStatement()).thenReturn(statement);

      new Session(new SessionData(response));

      this.mediaCard = new MediaCard(new MediaData(response), anchor);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    anchor
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());
    anchor.setAlignment(TOP_CENTER);

    stage.setScene(new Scene(anchor, 1000, 400));
    stage.show();

    anchor.getChildren().add(this.mediaCard);
  }

  @Test
  @DisplayName("Assert that new MediaCard loads image and title")
  void newInstance(FxRobot robot) {
    var title = robot.lookup("title").queryAs(Text.class);
    var imageView = robot.lookup("#imageBox").queryAs(ImageView.class);

    assertEquals("title", title.getText());
    assertEquals(
      "file:" +
      this.getClass().getResource("/public/placeholder-image.png").getPath(),
      imageView.getImage().getUrl()
    );
  }

  @Test
  @DisplayName("Assert that #handleClick displays modal")
  void handleClick(FxRobot robot) {
    robot.clickOn(this.mediaCard);

    var mediaCardBounds = this.mediaCard.getBoundsInParent();

    var mediaModal = robot.lookup(".modal").query();
    var mediaModalBounds = mediaModal.getBoundsInParent();

    assertEquals(
      Math.round(mediaCardBounds.getHeight()),
      Math.round(mediaModalBounds.getHeight())
    );
    assertEquals(mediaCardBounds.getMinY(), mediaModalBounds.getMinY());
    assertNotNull(mediaModal);
  }
}
