package org.jrawio.controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class Shape extends Canvas {
    private boolean selected = false;

    public Shape(double width, double height) {
        super(width, height);
        draw();

        this.setOnMouseClicked(this::handleClick);

        // 支持拖动
        this.setOnMousePressed(event -> {
            this.toFront(); // 拖动时置顶
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgTranslateX = getLayoutX();
            orgTranslateY = getLayoutY();
        });

        this.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            setLayoutX(orgTranslateX + offsetX);
            setLayoutY(orgTranslateY + offsetY);
        });
    }

    // 添加成员变量
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    private void handleClick(MouseEvent event) {
        selected = !selected;
        draw();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setStroke(selected ? Color.RED : Color.BLACK);
        gc.setLineWidth(selected ? 3 : 1);
        double padding = 4;
        double diameter = Math.min(getWidth(), getHeight()) - 2 * padding;
        double x = (getWidth() - diameter) / 2;
        double y = (getHeight() - diameter) / 2;
        gc.strokeOval(x, y, diameter, diameter);
    }
}
