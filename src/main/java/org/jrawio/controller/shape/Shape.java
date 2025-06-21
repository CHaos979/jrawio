package org.jrawio.controller.shape;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.jrawio.controller.RightPanel;
import java.util.HashSet;
import java.util.Set;

public abstract class Shape extends Canvas {
    private boolean selected = false; // 是否被选中
    private static final Set<Shape> selectedShapes = new HashSet<>(); // 被选中的所有图形

    private String text = this.toString(); // 文本
    private TextField textField; // 文本框控件

    // 拖动相关成员变量
    private double orgSceneX, orgSceneY;
    private boolean dragging = false; // 新增：是否正在拖动

    public Shape(double width, double height) {
        super(width, height);
        draw();

        this.setOnMousePressed(this::handlePressed);
        this.setOnMouseDragged(this::handleDragged);
        this.setOnMouseClicked(this::handleClick);
    }

    private void startEdit() {
        if (textField != null)
            return;
        Pane parent = (Pane) getParent();
        textField = new TextField(text);
        textField.setPrefWidth(getWidth() - 8);
        textField.setLayoutX(getLayoutX() + 4);
        textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
        parent.getChildren().add(textField);
        textField.requestFocus();

        textField.setOnAction(e -> finishEdit());
        textField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV)
                finishEdit();
        });
    }

    private void finishEdit() {
        if (textField == null)
            return;
        text = textField.getText();
        Pane parent = (Pane) getParent();
        parent.getChildren().remove(textField);
        textField = null;
        draw();
    }

    private void handlePressed(MouseEvent event) {
        System.out.println("[handlePressed]"+this.toString());
        this.toFront();
        orgSceneX = event.getSceneX();
        orgSceneY = event.getSceneY();
        dragging = false; // 按下时重置拖动标志

        // 拖动时如果未选中，则先选中自己
        if (!selected) {
            handleClick(event);
        }
        event.consume();
    }

    private void handleDragged(MouseEvent event) {
        double offsetX = event.getSceneX() - orgSceneX;
        double offsetY = event.getSceneY() - orgSceneY;
        // 同步移动所有被选中的Shape
        for (Shape shape : selectedShapes) {
            shape.setLayoutX(shape.getLayoutX() + offsetX);
            shape.setLayoutY(shape.getLayoutY() + offsetY);

            // 同步移动文本框
            if (shape.textField != null) {
                shape.textField.setLayoutX(shape.getLayoutX() + 4);
                shape.textField.setLayoutY(shape.getLayoutY() + shape.getHeight() / 2 - 12);
            }
        }
        orgSceneX = event.getSceneX();
        orgSceneY = event.getSceneY();
        dragging = true; // 拖动时设置为true
        event.consume();
    }

    private void handleClick(MouseEvent event) {
        System.out.println("[handleClick]" + this.toString());
        if (event.getClickCount() == 2) {
            startEdit();
            event.consume();
            return;
        }
        if (dragging) {
            // 如果是拖动后产生的点击，忽略
            dragging = false;
            return;
        }

        boolean multiSelect = event.isShiftDown() || event.isControlDown();

        if (multiSelect) {
            // 多选：切换当前 shape 的选中状态，不影响其他 shape
            setSelected(!selected);
        } else {
            // 单选：取消其他 shape 的选中，只选中当
            System.out.println("[handleClick] cancel select other shape");
            for (Shape shape : selectedShapes.toArray(new Shape[0])) {
                shape.setSelected(false);
            }
            setSelected(true);
        }
        event.consume();
    }

    public void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;
        if (selected) {
            selectedShapes.add(this);
        } else {
            selectedShapes.remove(this);
        }
        draw();
        // 通知右侧面板
        RightPanel rightPanel = RightPanel.getInstance();
        if (rightPanel != null) {
            rightPanel.onShapeSelectionChanged(selectedShapes);
        }
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
        draw();
    }

    public void setShapeWidth(double width) {
        super.setWidth(width);
        draw();
    }
    public void setShapeHeight(double height) {
        super.setHeight(height);
        draw();
    }

    /**
     * 抽象方法：由子类实现具体的图形绘制逻辑
     * @param gc 图形上下文
     * @param x 绘制起始x坐标
     * @param y 绘制起始y坐标
     * @param width 绘制宽度
     * @param height 绘制高度
     */
    protected abstract void drawShape(GraphicsContext gc, double x, double y, double width, double height);

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // 计算绘制区域
        double padding = 4;
        double shapeWidth = getWidth() - 2 * padding;
        double shapeHeight = getHeight() - 2 * padding;
        double x = padding;
        double y = padding;

        // 调用子类实现的图形绘制方法
        drawShape(gc, x, y, shapeWidth, shapeHeight);

        // 如果选中，画蓝色方框
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, shapeWidth, shapeHeight);
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
