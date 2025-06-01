package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class CanvasComponentController {
    @FXML
    private Canvas gridCanvas;

    @FXML
    private Pane canvasPane;

    @FXML
    public void initialize() {
        // 设置canvasPane的大小
        double paneWidth = gridCanvas.getWidth() + 500;
        double paneHeight = gridCanvas.getHeight() + 500;
        canvasPane.setPrefWidth(paneWidth);
        canvasPane.setPrefHeight(paneHeight);

        // 让gridCanvas居中
        gridCanvas.setLayoutX((paneWidth - gridCanvas.getWidth()) / 2);
        gridCanvas.setLayoutY((paneHeight - gridCanvas.getHeight()) / 2);

        // 添加阴影效果
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(50, 50, 50, 0.4));
        gridCanvas.setEffect(dropShadow);

        drawGrid();
    }

    private void drawGrid() {
        double width = gridCanvas.getWidth();
        double height = gridCanvas.getHeight();
        double gridSize = 20; // 网格间隔

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        // 填充白色背景
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        // 画竖线
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
        // 画横线
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }
    }
}
