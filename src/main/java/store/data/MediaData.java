package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaData {

  public final String id;
  public final String showId;
  public final String type;
  public final String url;
  public final String imagePath;
  public final String mediaPath;
  public final int season;
  public final String title;
  public final String content;
  public final String productionYear;
  public final List<String> tags;
  public final float rank;

  public MediaData(ResultSet response) throws SQLException {
    this.id = response.getString("id");
    this.showId = response.getString("show_id");
    this.type = response.getString("type");
    this.url = response.getString("url");
    this.imagePath = response.getString("image_path");
    this.mediaPath = response.getString("media_path");
    this.season = response.getInt("season");
    this.title = response.getString("title");
    this.content = response.getString("content");
    this.productionYear = response.getString("production_year");
    this.tags = new ArrayList<>(List.of(response.getString("tags").split(",")));
    this.rank = response.getFloat("ranking");
  }

  public MediaData(SearchData data) {
    this.id = data.id;
    this.showId = data.showId;
    this.type = data.type;
    this.url = data.url;
    this.imagePath = data.imagePath;
    this.mediaPath = null;
    this.season = -1;
    this.title = data.title;
    this.content = data.content;
    this.productionYear = data.productionYear;
    this.tags = data.tags;
    this.rank = data.rank;
  }
}
