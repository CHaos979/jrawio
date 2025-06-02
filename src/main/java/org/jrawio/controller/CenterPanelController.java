package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class CenterPanelController {
    @FXML
    private ScrollPane centerScrollPane;

    @FXML
    public void initialize() {
        // 等待布局完成后设置滚动条位置到正中间
        centerScrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            centerScrollPane.setHvalue(0.5);
            centerScrollPane.setVvalue(0.5);
        });
    }
}