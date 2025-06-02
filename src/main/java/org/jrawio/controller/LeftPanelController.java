package org.jrawio.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class LeftPanelController {
    @FXML
    private VBox leftPanel;
    private final List<ShapeBarController> shapeBarControllers = new ArrayList<>();

    @FXML
    public void initialize() throws IOException {
        for (int i = 0; i < 3; i++) {
            addShapeBar();
        }
    }

    private void addShapeBar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/jrawio/components/shape_bar.fxml"));
        Node shapeBar = loader.load();
        leftPanel.getChildren().add(shapeBar);
        // 获取并保存控制器实例
        ShapeBarController controller = loader.getController();
        controller.init("123", false);
        shapeBarControllers.add(controller);
    }
}
