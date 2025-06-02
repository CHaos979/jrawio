package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.net.URL;
import java.util.ResourceBundle;

public class Shape implements Initializable {

    @FXML
    private Canvas shapeCanvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        GraphicsContext gc = shapeCanvas.getGraphicsContext2D();
        gc.strokeOval(4, 4, 40, 40); // 只绘制外圈
    }
}
