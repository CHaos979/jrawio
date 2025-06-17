package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
                String info = shape.toString();
                try {
                    java.lang.reflect.Field textField = shape.getClass().getDeclaredField("text");
                    textField.setAccessible(true);
                    Object textValue = textField.get(shape);
                    if (textValue != null) info = textValue.toString();
                } catch (Exception ignore) {}
                rightPanelRoot.getChildren().add(new Label("选中: " + info));
            }
        }
    }

    // 在这里添加右侧面板的逻辑和事件处理方法
}
