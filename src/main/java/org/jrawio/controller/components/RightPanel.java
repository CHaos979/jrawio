package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jrawio.controller.shape.Shape;
import java.util.Set;
import java.util.List;

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
    } // 响应Shape选中变化，更新右侧面板内容

    public void onShapeSelectionChanged(Set<Shape> selectedShapes) {
        rightPanelRoot.getChildren().clear();

        if (selectedShapes == null || selectedShapes.isEmpty()) {
            rightPanelRoot.getChildren().add(new Label("未选中任何图形"));
            System.out.println("RightPanel: 没有选中的图形");
        } else {
            System.out.println("RightPanel: 选中了 " + selectedShapes.size() + " 个图形");
            for (Shape shape : selectedShapes) {
                // 直接使用Shape的getControlComponents方法
                List<javafx.scene.Node> controls = shape.getControlComponents();
                System.out.println(
                        "RightPanel: 为 " + shape.getClass().getSimpleName() + " 添加 " + controls.size() + " 个控件");
                rightPanelRoot.getChildren().addAll(controls);
            }
        }
    }

    // 在这里添加右侧面板的逻辑和事件处理方法
}
