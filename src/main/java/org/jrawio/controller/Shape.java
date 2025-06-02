package org.jrawio.controller;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import java.util.HashMap;
import java.util.Map;

public class Shape extends Canvas {
    private boolean selected = false;
    private String text = "";
    private TextField textField;

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

        // 双击进入编辑
        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                startEdit();
                event.consume();
            } else {
                handleClick(event);
            }
        });
    }

    private void startEdit() {
        if (textField != null) return;
        Pane parent = (Pane) getParent();
        textField = new TextField(text);
        textField.setPrefWidth(getWidth() - 8);
        textField.setLayoutX(getLayoutX() + 4);
        textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
        parent.getChildren().add(textField);
        textField.requestFocus();

        textField.setOnAction(e -> finishEdit());
        textField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) finishEdit();
        });
    }

    private void finishEdit() {
        if (textField == null) return;
        text = textField.getText();
        Pane parent = (Pane) getParent();
        parent.getChildren().remove(textField);
        textField = null;
        draw();
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
                    selectedShapesOrigin.put(shape, new double[] { shape.getLayoutX(), shape.getLayoutY() });
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
            // 同步移动文本框
            if (shape.textField != null) {
                shape.textField.setLayoutX(shape.getLayoutX() + 4);
                shape.textField.setLayoutY(shape.getLayoutY() + shape.getHeight() / 2 - 12);
            }
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

        // 画圆
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        double padding = 4;
        double diameter = Math.min(getWidth(), getHeight()) - 2 * padding;
        double x = (getWidth() - diameter) / 2;
        double y = (getHeight() - diameter) / 2;
        gc.strokeOval(x, y, diameter, diameter);

        // 如果选中，画蓝色方框
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, diameter, diameter);
        }

        // 画文本
        if (text != null && !text.isEmpty() && textField == null) {
            gc.setFill(Color.BLACK);
            javafx.scene.text.Font font = javafx.scene.text.Font.font(14);
            gc.setFont(font);

            // 用 Text 类测量文本宽度
            javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
            tempText.setFont(font);
            double textWidth = tempText.getLayoutBounds().getWidth();

            double textX = getWidth() / 2 - textWidth / 2;
            double textY = getHeight() / 2 + 6;
            gc.fillText(text, textX, textY);
        }
    }
}
