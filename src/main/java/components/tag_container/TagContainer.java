package components.tag_container;

import components.font_icon.FontIcon;
import components.tag.Tag;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import store.data.TagData;

final record Category(String label) {}

public class TagContainer extends HBox {

  private final int tagsLimit;
  private final List<TagData> data;
  private final List<Tag> persistentTags;
  private int index = 0;

  @FXML
  private HBox tagGroup;

  @FXML
  public final void handleNext(ActionEvent event) {
    this.update(this.tagsLimit - this.persistentTags.size());
  }

  @FXML
  public final void handlePrev(ActionEvent event) {
    this.update(-(this.tagsLimit - this.persistentTags.size()));
  }

  private final void update(int index) {
    var step = this.tagsLimit - this.persistentTags.size();

    this.index += index;

    if (this.index < 0) {
      this.index += this.data.size();
    }

    if (this.index >= this.data.size()) {
      this.index -= this.data.size();
    }

    var children = this.tagGroup.getChildren();

    if (children.size() == 0) {
      children.addAll(this.persistentTags);
    } else {
      children.retainAll(this.persistentTags);
    }

    var a = this.index;
    var i = -1;

    while (++i < step) {
      if (i + this.index == this.data.size()) {
        a = 0;
      }

      children.add(new Tag(this.data.get(a++).title));
    }
  }

  public TagContainer(
    @NamedArg("data") List<TagData> data,
    @NamedArg(value = "tagsLimit", defaultValue = "10") int tagsLimit
  ) {
    this.data = data;
    this.tagsLimit = tagsLimit;

    var loader = new FXMLLoader(
      this.getClass().getResource("tag_container.fxml")
    );

    loader.setRoot(this);
    loader.setController(this);

    try {
      loader.load();
    } catch (Exception error) {
      error.printStackTrace();
    }

    this.persistentTags = new ArrayList<>();
    this.persistentTags.add(new Tag("All"));
    this.persistentTags.add(new Tag("Movie"));
    this.persistentTags.add(new Tag("Show"));
    this.persistentTags.add(new Tag(new FontIcon("favorite", 14)));

    this.update(0);
  }
}
