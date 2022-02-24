package store.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchData {

  public final String id;
  public final String type;
  public final String showId;
  public final String url;
  public final String imagePath;
  public final String title;
  public final String content;
  public final String productionYear;
  public final List<String> tags;
  public final float rank;

  public SearchData(ResultSet response) throws SQLException {
    this.id = response.getString("id");
    this.type = response.getString("type");
    this.showId = response.getString("show_id");
    this.url = response.getString("url");
    this.imagePath = response.getString("image_path");
    this.title = response.getString("title");
    this.content = response.getString("content");
    this.productionYear = response.getString("production_year");
    this.tags = new ArrayList<>(List.of(response.getString("tags").split(",")));
    this.rank = response.getFloat("ranking");
  }
}
