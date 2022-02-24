open module rickflix {
  requires java.sql;
  requires javafx.controls;
  requires javafx.fxml;
  requires transitive javafx.media;
  requires transitive javafx.graphics;
  requires com.google.common;

  exports app ;
}
