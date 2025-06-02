package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;

public class CanvasComponentController {
    @FXML
    private Canvas gridCanvas;

    @FXML
    private Pane canvasPane;

    // 框选相关成员变量
    private Rectangle selectionRect = new Rectangle();
    private double startX, startY;

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

        // 让gridCanvas不响应鼠标事件
        gridCanvas.setMouseTransparent(true);

        // 添加阴影效果
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(50, 50, 50, 0.4));
        gridCanvas.setEffect(dropShadow);

        // 允许拖拽进入
        canvasPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasString() && db.getString().equals("circle")) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // 拖拽放下时创建Shape
        canvasPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && db.getString().equals("circle")) {
                // 创建Shape对象
                Shape shape = new Shape(40, 40);
                // 设置放置位置
                shape.setLayoutX(event.getX() - shape.getWidth() / 2);
                shape.setLayoutY(event.getY() - shape.getHeight() / 2);
                canvasPane.getChildren().add(shape);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        // 初始化选区框
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.web("rgba(100,100,255,0.2)"));
        selectionRect.setVisible(false);
        canvasPane.getChildren().add(selectionRect);

        // 框选鼠标事件
        canvasPane.setOnMousePressed(event -> {
            // 只响应鼠标左键且不是拖拽Shape时
            if (event.isPrimaryButtonDown() && event.getTarget() == canvasPane) {
                startX = event.getX();
                startY = event.getY();
                selectionRect.setX(startX);
                selectionRect.setY(startY);
                selectionRect.setWidth(0);
                selectionRect.setHeight(0);
                selectionRect.setVisible(true);
            }
        });

        canvasPane.setOnMouseDragged(event -> {
            if (selectionRect.isVisible()) {
                double x = Math.min(startX, event.getX());
                double y = Math.min(startY, event.getY());
                double w = Math.abs(event.getX() - startX);
                double h = Math.abs(event.getY() - startY);
                selectionRect.setX(x);
                selectionRect.setY(y);
                selectionRect.setWidth(w);
                selectionRect.setHeight(h);
            }
        });

        canvasPane.setOnMouseReleased(event -> {
            if (selectionRect.isVisible()) {
                // 框选结束，判断哪些Shape在选区内
                for (javafx.scene.Node node : canvasPane.getChildren()) {
                    if (node instanceof Shape) {
                        Shape shape = (Shape) node;
                        double sx = shape.getLayoutX();
                        double sy = shape.getLayoutY();
                        double sw = shape.getWidth();
                        double sh = shape.getHeight();
                        if (selectionRect.getBoundsInParent().intersects(sx, sy, sw, sh)) {
                            shape.setSelected(true);
                        } else {
                            shape.setSelected(false);
                        }
                    }
                }
                selectionRect.setVisible(false);
            }
        });

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
