package org.jrawio.controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import java.util.HashMap;
import java.util.Map;

public class Shape extends Canvas {
    private boolean selected = false;

    // 用于记录每个选中Shape的初始位置
    private Map<Shape, double[]> selectedShapesOrigin = new HashMap<>();

    // 拖动相关成员变量
    private double orgSceneX, orgSceneY;

    public Shape(double width, double height) {
        super(width, height);
        draw();

        this.setOnMouseClicked(this::handleClick);
        this.setOnMousePressed(this::handlePressed);
        this.setOnMouseDragged(this::handleDragged);
    }

    private void handlePressed(MouseEvent event) {
        this.toFront();
        orgSceneX = event.getSceneX();
        orgSceneY = event.getSceneY();


        // 记录所有被选中Shape的初始位置
        selectedShapesOrigin.clear();
        for (Node node : getParent().getChildrenUnmodifiable()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                if (shape.isSelected()) {
                    selectedShapesOrigin.put(shape, new double[]{shape.getLayoutX(), shape.getLayoutY()});
                }
            }
        }
        event.consume();
    }

    private void handleDragged(MouseEvent event) {
        double offsetX = event.getSceneX() - orgSceneX;
        double offsetY = event.getSceneY() - orgSceneY;
        // 同步移动所有被选中的Shape
        for (Map.Entry<Shape, double[]> entry : selectedShapesOrigin.entrySet()) {
            Shape shape = entry.getKey();
            double[] origin = entry.getValue();
            shape.setLayoutX(origin[0] + offsetX);
            shape.setLayoutY(origin[1] + offsetY);
        }
        event.consume();
    }

    // 只有点按空白处解除选中
    private void handleClick(MouseEvent event) {
        if (!selected) {
            selected = true;
        }
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
