package org.jrawio.controller;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;

public class ShapeBar extends TitledPane {
    private ShapeBar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/jrawio/components/shape_bar.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Builder {
        private String title = "";
        private boolean expanded = false;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setExpanded(boolean expanded) {
            this.expanded = expanded;
            return this;
        }

        public ShapeBar build() {
            ShapeBar bar = new ShapeBar();
            bar.setText(title);
            bar.setExpanded(expanded);
            return bar;
        }
    }
}
