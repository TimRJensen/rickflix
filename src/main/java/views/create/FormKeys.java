package views.create;

public enum FormKeys {
  EMAIL("email"),
  PASSWORD("password"),
  CONFIRMED_PASSWORD("confirm password"),
  NAME("name"),
  COUNTRY("country");

  private final String label;

  @Override
  public final String toString() {
    return this.label;
  }

  private FormKeys(String label) {
    this.label = label;
  }
}
