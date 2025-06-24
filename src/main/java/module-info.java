module jrawio {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.desktop;
    requires static lombok;

    opens org.jrawio to javafx.fxml;

    exports org.jrawio;

    opens org.jrawio.controller.components to javafx.fxml;

    exports org.jrawio.controller.components;
    exports org.jrawio.controller.shape;
}
