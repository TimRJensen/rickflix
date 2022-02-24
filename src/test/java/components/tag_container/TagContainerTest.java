package components.tag_container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import app.App;
import components.font_icon_button.FontIconButton;
import components.tag.Tag;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import store.data.TagData;

@ExtendWith(MockitoExtension.class)
@ExtendWith(ApplicationExtension.class)
public class TagContainerTest extends App {

  private final int tagLimit = 10;
  private final int persistentTags = 4;

  private TagContainer tagContainer;
  private List<TagData> data;

  @Start
  public void start(Stage stage) {
    this.data = new ArrayList<TagData>();
    var i = -1;

    while (++i < 20) {
      var response = mock(ResultSet.class);

      try {
        when(response.getInt("id")).thenReturn(i);
        when(response.getString("title")).thenReturn("foo-" + i);
        this.data.add(new TagData(response));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    this.tagContainer = new TagContainer(this.data, this.tagLimit);

    var root = new StackPane(this.tagContainer);

    root
      .getStylesheets()
      .add(this.getClass().getResource("/css/index.css").toString());

    stage.setScene(new Scene(root, 1000, 400));
    stage.show();
  }

  // TODO change to robot.lookup
  @Test
  @DisplayName("Assert that #handleNext cycles through children increasingly")
  void handleNext(FxRobot robot) {
    var next = (FontIconButton) this.tagContainer.getChildren().get(2);

    var step = this.tagLimit - this.persistentTags;
    var i = 0;
    var a = step;
    var flag = true;

    while (flag) {
      if (a > this.data.size()) {
        a -= this.data.size();

        if (a == step) {
          i = 0;
        }

        flag = false;
      }

      var children = ((HBox) tagContainer.getChildren().get(1)).getChildren();

      assertEquals("All", ((Tag) children.get(0)).getText());
      assertEquals("Movie", ((Tag) children.get(1)).getText());
      assertEquals("Show", ((Tag) children.get(2)).getText());
      assertEquals("", ((Tag) children.get(3)).getText());
      assertEquals("foo-" + i, ((Tag) children.get(4)).getText());
      assertEquals("foo-" + (a - 1), ((Tag) children.get(9)).getText());

      i += step;
      a = i + step;
      robot.clickOn(next);
    }
  }

  // TODO change to robot.lookup
  @Test
  @DisplayName("Assert that #handlePrev cycles through children decreasingly")
  void handlePrev(FxRobot robot) {
    var prev = (FontIconButton) this.tagContainer.getChildren().get(0);

    robot.clickOn(prev);

    var step = this.tagLimit - this.persistentTags;
    var i = data.size() - step;
    var a = i + step;
    var flag = true;

    while (flag) {
      if (i < 0) {
        i += this.data.size();

        if (i == this.data.size() - step) {
          a = this.data.size() - step;
        }

        flag = false;
      }

      var children = ((HBox) tagContainer.getChildren().get(1)).getChildren();

      assertEquals("All", ((Tag) children.get(0)).getText());
      assertEquals("Movie", ((Tag) children.get(1)).getText());
      assertEquals("Show", ((Tag) children.get(2)).getText());
      assertEquals("", ((Tag) children.get(3)).getText());
      assertEquals("foo-" + i, ((Tag) children.get(4)).getText());
      assertEquals("foo-" + (a - 1), ((Tag) children.get(9)).getText());

      i -= step;
      a = i + step;
      robot.clickOn(prev);
    }
  }
}
