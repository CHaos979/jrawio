package org.jrawio.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class CenterPanelController {
    @FXML
    private ScrollPane centerScrollPane;

    @FXML
    public void initialize() {
        // 只在初始化时设置一次滚动条位置到正中间
        Platform.runLater(() -> {
            centerScrollPane.setHvalue(0.5);
            centerScrollPane.setVvalue(0.5);
        });

        // 禁止ScrollPane的拖拽滚动（pannable）
        centerScrollPane.setPannable(false);
    }
}