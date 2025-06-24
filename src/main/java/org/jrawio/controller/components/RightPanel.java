package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.jrawio.controller.shape.Shape;
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
                Label label = new Label("文本：");
                TextField input = new TextField(shape.getText());
                input.setPrefWidth(120);
                input.setOnAction(e -> shape.setText(input.getText()));
                input.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV)
                        shape.setText(input.getText());
                });
                // 宽度输入框
                Label widthLabel = new Label("宽度：");
                TextField widthInput = new TextField(String.valueOf((int) shape.getWidth()));
                widthInput.setPrefWidth(80);
                widthInput.setOnAction(e -> {
                    try {
                        double w = Double.parseDouble(widthInput.getText());
                        shape.setShapeWidth(w);
                    } catch (Exception ex) {
                    }
                });
                widthInput.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        try {
                            double w = Double.parseDouble(widthInput.getText());
                            shape.setShapeWidth(w);
                        } catch (Exception ex) {
                        }
                    }
                });
                // 高度输入框
                Label heightLabel = new Label("高度：");
                TextField heightInput = new TextField(String.valueOf((int) shape.getHeight()));
                heightInput.setPrefWidth(80);
                heightInput.setOnAction(e -> {
                    try {
                        double h = Double.parseDouble(heightInput.getText());
                        shape.setShapeHeight(h);
                    } catch (Exception ex) {
                    }
                });
                heightInput.focusedProperty().addListener((obs, oldV, newV) -> {
                    if (!newV) {
                        try {
                            double h = Double.parseDouble(heightInput.getText());
                            shape.setShapeHeight(h);
                        } catch (Exception ex) {
                        }
                    }
                });
                rightPanelRoot.getChildren().addAll(label, input, widthLabel, widthInput, heightLabel, heightInput);
            }
        }
    }

    // 在这里添加右侧面板的逻辑和事件处理方法
}
