package org.jrawio.controller.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
import org.jrawio.controller.shape.ShapeType;

public class ShapeBar extends TitledPane {
    
    private ShapeBar(List<ShapeType> shapeTypes) {
        // 创建FlowPane作为内容容器
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(8);
        flowPane.setVgap(8);
        flowPane.setStyle("-fx-padding: 5;");
        
        // 为每个形状类型创建一个ShapeButton
        for (ShapeType shapeType : shapeTypes) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/jrawio/components/shape_button.fxml"));
                javafx.scene.Node buttonNode = loader.load();
                ShapeButton button = loader.getController();
                button.setShapeType(shapeType);
                flowPane.getChildren().add(buttonNode);
            } catch (IOException e) {
                System.err.println("无法加载形状按钮: " + e.getMessage());
            }
        }
        
        setContent(flowPane);
    }

    public static class Builder {
        private String title = "";
        private boolean expanded = false;
        private List<ShapeType> shapeTypes = new ArrayList<>();

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setExpanded(boolean expanded) {
            this.expanded = expanded;
            return this;
        }
        
        public Builder addShapeType(ShapeType shapeType) {
            this.shapeTypes.add(shapeType);
            return this;
        }
        
        public Builder addShapeTypes(ShapeType... shapeTypes) {
            for (ShapeType type : shapeTypes) {
                this.shapeTypes.add(type);
            }
            return this;
        }

        public ShapeBar build() {
            ShapeBar bar = new ShapeBar(shapeTypes);
            bar.setText(title);
            bar.setExpanded(expanded);
            return bar;
        }
    }
}
