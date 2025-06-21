package org.jrawio.controller.shape;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
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
    
    // 缩放相关成员变量
    private boolean resizing = false; // 是否正在缩放
    private ResizeHandle activeHandle = null; // 当前活跃的缩放控制点
    private double originalWidth, originalHeight; // 原始尺寸
    private double originalX, originalY; // 原始位置
    private static final double HANDLE_SIZE = 6; // 控制点大小
    
    // 缩放控制点枚举
    public enum ResizeHandle {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    public Shape(double width, double height) {
        super(width, height);
        draw();

        this.setOnMousePressed(this::handlePressed);
        this.setOnMouseDragged(this::handleDragged);
        this.setOnMouseClicked(this::handleClick);
        this.setOnMouseMoved(this::handleMouseMoved);
        this.setOnMouseReleased(this::handleMouseReleased);
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
        dragging = false;
        resizing = false;

        // 检查是否点击在控制点上
        if (selected) {
            activeHandle = getResizeHandleAt(event.getX(), event.getY());
            if (activeHandle != null) {
                resizing = true;
                originalWidth = getWidth();
                originalHeight = getHeight();
                originalX = getLayoutX();
                originalY = getLayoutY();
                event.consume();
                return;
            }
        }

        // 拖动时如果未选中，则先选中自己
        if (!selected) {
            handleClick(event);
        }
        event.consume();
    }

    private void handleDragged(MouseEvent event) {
        if (resizing && activeHandle != null) {
            handleResize(event);
        } else {
            handleMove(event);
        }
        event.consume();
    }
    
    /**
     * 处理移动操作
     */
    private void handleMove(MouseEvent event) {
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
    }
    
    /**
     * 处理缩放操作
     */
    private void handleResize(MouseEvent event) {
        double deltaX = event.getSceneX() - orgSceneX;
        double deltaY = event.getSceneY() - orgSceneY;
        
        double newWidth = originalWidth;
        double newHeight = originalHeight;
        double newX = originalX;
        double newY = originalY;
        
        switch (activeHandle) {
            case TOP_LEFT:
                newWidth = Math.max(20, originalWidth - deltaX);
                newHeight = Math.max(20, originalHeight - deltaY);
                newX = originalX + (originalWidth - newWidth);
                newY = originalY + (originalHeight - newHeight);
                break;
            case TOP_CENTER:
                newHeight = Math.max(20, originalHeight - deltaY);
                newY = originalY + (originalHeight - newHeight);
                break;
            case TOP_RIGHT:
                newWidth = Math.max(20, originalWidth + deltaX);
                newHeight = Math.max(20, originalHeight - deltaY);
                newY = originalY + (originalHeight - newHeight);
                break;
            case MIDDLE_LEFT:
                newWidth = Math.max(20, originalWidth - deltaX);
                newX = originalX + (originalWidth - newWidth);
                break;
            case MIDDLE_RIGHT:
                newWidth = Math.max(20, originalWidth + deltaX);
                break;
            case BOTTOM_LEFT:
                newWidth = Math.max(20, originalWidth - deltaX);
                newHeight = Math.max(20, originalHeight + deltaY);
                newX = originalX + (originalWidth - newWidth);
                break;
            case BOTTOM_CENTER:
                newHeight = Math.max(20, originalHeight + deltaY);
                break;
            case BOTTOM_RIGHT:
                newWidth = Math.max(20, originalWidth + deltaX);
                newHeight = Math.max(20, originalHeight + deltaY);
                break;
        }
        
        // 应用新的尺寸和位置
        setShapeWidth(newWidth);
        setShapeHeight(newHeight);
        setLayoutX(newX);
        setLayoutY(newY);
        
        // 更新文本框位置
        if (textField != null) {
            textField.setLayoutX(getLayoutX() + 4);
            textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
            textField.setPrefWidth(getWidth() - 8);
        }
    }

    private void handleClick(MouseEvent event) {
        System.out.println("[handleClick]" + this.toString());
        if (event.getClickCount() == 2) {
            startEdit();
            event.consume();
            return;
        }
        if (dragging || resizing) {
            // 如果是拖动或缩放后产生的点击，忽略
            dragging = false;
            resizing = false;
            activeHandle = null;
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
    
    /**
     * 公共方法：用于预览绘制（不包含选中状态和文本）
     */
    public final void drawPreview(GraphicsContext gc, double x, double y, double width, double height) {
        drawShape(gc, x, y, width, height);
    }

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

        // 如果选中，画蓝色方框和控制点
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, shapeWidth, shapeHeight);
            
            // 绘制八个控制点
            drawResizeHandles(gc, x, y, shapeWidth, shapeHeight);
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
    
    /**
     * 绘制缩放控制点
     */
    private void drawResizeHandles(GraphicsContext gc, double shapeX, double shapeY, double shapeWidth, double shapeHeight) {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        
        double halfHandle = HANDLE_SIZE / 2;
        
        // 八个控制点的位置
        double[][] handlePositions = {
            {shapeX - halfHandle, shapeY - halfHandle}, // TOP_LEFT
            {shapeX + shapeWidth / 2 - halfHandle, shapeY - halfHandle}, // TOP_CENTER
            {shapeX + shapeWidth - halfHandle, shapeY - halfHandle}, // TOP_RIGHT
            {shapeX - halfHandle, shapeY + shapeHeight / 2 - halfHandle}, // MIDDLE_LEFT
            {shapeX + shapeWidth - halfHandle, shapeY + shapeHeight / 2 - halfHandle}, // MIDDLE_RIGHT
            {shapeX - halfHandle, shapeY + shapeHeight - halfHandle}, // BOTTOM_LEFT
            {shapeX + shapeWidth / 2 - halfHandle, shapeY + shapeHeight - halfHandle}, // BOTTOM_CENTER
            {shapeX + shapeWidth - halfHandle, shapeY + shapeHeight - halfHandle} // BOTTOM_RIGHT
        };
        
        // 绘制每个控制点
        for (double[] pos : handlePositions) {
            gc.fillRect(pos[0], pos[1], HANDLE_SIZE, HANDLE_SIZE);
            gc.strokeRect(pos[0], pos[1], HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    private void handleMouseMoved(MouseEvent event) {
        if (!selected) {
            setCursor(Cursor.DEFAULT);
            return;
        }
        
        ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
        if (handle != null) {
            setCursor(getCursorForHandle(handle));
        } else {
            setCursor(Cursor.DEFAULT);
        }
    }
    
    /**
     * 检测鼠标位置是否在某个控制点上
     */
    private ResizeHandle getResizeHandleAt(double x, double y) {
        if (!selected) return null;
        
        double padding = 4;
        double shapeWidth = getWidth() - 2 * padding;
        double shapeHeight = getHeight() - 2 * padding;
        double shapeX = padding;
        double shapeY = padding;
        
        // 检测八个控制点
        double halfHandle = HANDLE_SIZE / 2;
        
        // 左上角
        if (isPointInHandle(x, y, shapeX - halfHandle, shapeY - halfHandle)) {
            return ResizeHandle.TOP_LEFT;
        }
        // 上中
        if (isPointInHandle(x, y, shapeX + shapeWidth / 2 - halfHandle, shapeY - halfHandle)) {
            return ResizeHandle.TOP_CENTER;
        }
        // 右上角
        if (isPointInHandle(x, y, shapeX + shapeWidth - halfHandle, shapeY - halfHandle)) {
            return ResizeHandle.TOP_RIGHT;
        }
        // 左中
        if (isPointInHandle(x, y, shapeX - halfHandle, shapeY + shapeHeight / 2 - halfHandle)) {
            return ResizeHandle.MIDDLE_LEFT;
        }
        // 右中
        if (isPointInHandle(x, y, shapeX + shapeWidth - halfHandle, shapeY + shapeHeight / 2 - halfHandle)) {
            return ResizeHandle.MIDDLE_RIGHT;
        }
        // 左下角
        if (isPointInHandle(x, y, shapeX - halfHandle, shapeY + shapeHeight - halfHandle)) {
            return ResizeHandle.BOTTOM_LEFT;
        }
        // 下中
        if (isPointInHandle(x, y, shapeX + shapeWidth / 2 - halfHandle, shapeY + shapeHeight - halfHandle)) {
            return ResizeHandle.BOTTOM_CENTER;
        }
        // 右下角
        if (isPointInHandle(x, y, shapeX + shapeWidth - halfHandle, shapeY + shapeHeight - halfHandle)) {
            return ResizeHandle.BOTTOM_RIGHT;
        }
        
        return null;
    }
    
    /**
     * 检测点是否在控制点区域内
     */
    private boolean isPointInHandle(double x, double y, double handleX, double handleY) {
        return x >= handleX && x <= handleX + HANDLE_SIZE && 
               y >= handleY && y <= handleY + HANDLE_SIZE;
    }
    
    /**
     * 根据控制点获取对应的鼠标光标
     */
    private Cursor getCursorForHandle(ResizeHandle handle) {
        switch (handle) {
            case TOP_LEFT:
            case BOTTOM_RIGHT:
                return Cursor.NW_RESIZE;
            case TOP_CENTER:
            case BOTTOM_CENTER:
                return Cursor.N_RESIZE;
            case TOP_RIGHT:
            case BOTTOM_LEFT:
                return Cursor.NE_RESIZE;
            case MIDDLE_LEFT:
            case MIDDLE_RIGHT:
                return Cursor.W_RESIZE;
            default:
                return Cursor.DEFAULT;
        }
    }
    
    /**
     * 重置缩放状态
     */
    private void resetResizeState() {
        resizing = false;
        activeHandle = null;
        setCursor(Cursor.DEFAULT);
    }

    private void handleMouseReleased(MouseEvent event) {
        if (resizing) {
            resetResizeState();
            // 通知右侧面板更新尺寸信息
            RightPanel rightPanel = RightPanel.getInstance();
            if (rightPanel != null) {
                rightPanel.onShapeSelectionChanged(selectedShapes);
            }
        }
        event.consume();
    }
}
