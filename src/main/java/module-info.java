module jrawio {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires lombok;
    
    opens org.jrawio to javafx.fxml;
    exports org.jrawio;
    opens org.jrawio.controller to javafx.fxml;
    exports org.jrawio.controller;
    exports org.jrawio.controller.shape;
}
