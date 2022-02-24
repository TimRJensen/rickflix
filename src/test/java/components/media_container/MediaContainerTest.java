package components.media_container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import components.font_icon_button.FontIconButton;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
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
public class MediaContainerTest extends App {

  private MediaContainer mediaContainer;

  @Start
  public void start(Stage stage) {
    this.router.routes.put("/foo", new Route("/foo", null));

    var connection = mock(Connection.class);
    var statement = mock(Statement.class);
    var response = mock(ResultSet.class);

    try {
      when(response.getString("id")).thenReturn("media-0");
      when(response.getString("image_path"))
        .thenReturn(
          this.getClass().getResource("/public/placeholder-image.png").getPath()
        );
      when(response.getString("media_path"))
        .thenReturn(
          this.getClass().getResource("/public/placeholder-media.mp4").getPath()
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

      this.mediaContainer =
        new MediaContainer(400, 400, new MediaData(response));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    var root = new StackPane();

    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();

    root.getChildren().add(this.mediaContainer);
  }

  @Test
  @DisplayName("Assert that new MediaContainer loads media")
  void newInstance(FxRobot robot) {
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);

    assertEquals(
      "file:" +
      this.getClass().getResource("/public/placeholder-media.mp4").getPath(),
      mediaView.getMediaPlayer().getMedia().getSource()
    );
  }

  @Test
  @DisplayName("Assert that #handleMediaControls plays media")
  void handleMediaControlPlay(FxRobot robot) {
    var mediaPlaybackControl = robot
      .lookup(".root #mediaPlaybackAction")
      .queryAs(FontIconButton.class);
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);
    var mediaPlayer = mediaView.getMediaPlayer();

    robot.clickOn(mediaPlaybackControl);

    assertEquals(Status.PLAYING, mediaPlayer.getStatus());
    assertEquals("pause", mediaPlaybackControl.getType());
    mediaPlayer.stop();
  }

  @Test
  @DisplayName("Assert that #handleMediaControls pauses media")
  void handleMediaControlPause(FxRobot robot) {
    var mediaPlaybackControl = robot
      .lookup(".root #mediaPlaybackAction")
      .queryAs(FontIconButton.class);
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);
    var mediaPlayer = mediaView.getMediaPlayer();

    robot.clickOn(mediaPlaybackControl);
    robot.clickOn(mediaPlaybackControl);

    assertEquals(Status.PAUSED, mediaPlayer.getStatus());
    assertEquals("play", mediaPlaybackControl.getType());
    mediaPlayer.stop();
  }

  @Test
  @DisplayName("Assert that #handleMediaControl replays media")
  void handleMediaControlReplay(FxRobot robot) {
    var mediaPlaybackControl = robot
      .lookup(".root #mediaPlaybackAction")
      .queryAs(FontIconButton.class);
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);
    var mediaPlayer = mediaView.getMediaPlayer();

    mediaPlayer.seek(mediaPlayer.getStopTime());

    robot.clickOn(mediaPlaybackControl);

    assertEquals("replay", mediaPlaybackControl.getType());
    assertEquals(Status.STOPPED, mediaPlayer.getStatus());

    robot.clickOn(mediaPlaybackControl);

    assertEquals(Status.PLAYING, mediaPlayer.getStatus());
    assertEquals("pause", mediaPlaybackControl.getType());
    mediaPlayer.stop();
  }

  @Test
  @DisplayName(
    "Assert that #handleMediaProgressChange increases media progress"
  )
  void handleMediaProgressChange(FxRobot robot) {
    var slider = robot.lookup(".root #mediaProgress").queryAs(Slider.class);
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);
    var mediaPlayer = mediaView.getMediaPlayer();
    var bounds = slider.localToScreen(slider.getBoundsInLocal());

    robot.clickOn(bounds);

    assertEquals(true, mediaPlayer.getCurrentTime().toMillis() > 0);
  }

  @Test
  @DisplayName("Assert that #handleMediaViewControl sets fullscreen")
  void handleMediaViewControlFullscreen(FxRobot robot)
    throws InterruptedException {
    var mediaViewControl = robot
      .lookup(".root #mediaViewAction")
      .queryAs(FontIconButton.class);

    robot.clickOn(mediaViewControl);

    assertEquals(true, ((Stage) robot.window(0)).isFullScreen());
    assertEquals("compress", mediaViewControl.getType());

    robot.clickOn(mediaViewControl);

    assertEquals(false, ((Stage) robot.window(0)).isFullScreen());
    assertEquals("expand", mediaViewControl.getType());
  }

  @Test
  @DisplayName("Assert that #handleNavigation stores progress")
  void handleNavigation(FxRobot robot) throws InterruptedException {
    var mediaView = robot.lookup(".root #mediaView").queryAs(MediaView.class);
    var mediaPlayer = mediaView.getMediaPlayer();

    mediaPlayer.seek(Duration.millis(1000));

    this.router.navigate("/foo");

    assertEquals(1, Session.getCurrent().progress.size());
    assertEquals(1000D, Session.getCurrent().progress.get(0).progress);
  }
}
