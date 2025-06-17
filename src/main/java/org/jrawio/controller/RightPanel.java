package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import java.util.Set;

public class RightPanel {
    private static RightPanel instance;

    @FXML
    private VBox rightPanelRoot;

    @FXML
    private void initialize() {
        instance = this;
    }

    public static RightPanel getInstance() {
        return instance;
    }

    // 响应Shape选中变化，更新右侧面板内容
    public void onShapeSelectionChanged(Set<Shape> selectedShapes) {
        rightPanelRoot.getChildren().clear();
        if (selectedShapes == null || selectedShapes.isEmpty()) {
            rightPanelRoot.getChildren().add(new Label("未选中任何图形"));
        } else {
            for (Shape shape : selectedShapes) {
                Label label = new Label("属性：");
                TextField input = new TextField(shape.getText());
                input.setPrefWidth(120);
                input.setOnAction(e -> {
                    shape.setText(input.getText());
                });
                input.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        shape.setText(input.getText());
                    }
                });
                rightPanelRoot.getChildren().addAll(label, input);
            }
        }
    }

    // 在这里添加右侧面板的逻辑和事件处理方法
}
