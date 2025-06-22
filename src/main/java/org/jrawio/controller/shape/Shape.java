package org.jrawio.controller.shape;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import org.jrawio.controller.components.RightPanel;
import org.jrawio.controller.shape.Shape.ShapeStateMachine.InteractionState;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;

public abstract class Shape extends Canvas {
    /** 是否被选中 */
    protected boolean selected = false;

    /** 被选中的所有图形 */
    protected static final Set<Shape> selectedShapes = new HashSet<>();

    /** 文本内容 */
    protected String text;

    /** 文本框控件 */
    protected TextField textField;

    /** 操作状态机 */
    protected final ShapeStateMachine stateMachine = new ShapeStateMachine();

    /**
     * 内部状态机类 - 管理Shape的交互状态
     */
    @Data
    public static class ShapeStateMachine {
        /**
         * 互斥的交互状态枚举
         */
        public enum InteractionState {
            /** 空闲状态 */
            IDLE,
            /** 拖动状态 */
            DRAGGING,
            /** 缩放状态 */
            RESIZING
        }

        /** 当前交互状态 */
        private InteractionState currentState = InteractionState.IDLE;

        /** 状态相关数据 - 原始场景坐标 */
        private double orgSceneX, orgSceneY;

        /** 当前活动的控制点 */
        private ResizeHandleManager.ResizeHandle activeHandle = null;

        /** 原始尺寸和位置 */
        private double originalWidth, originalHeight;
        private double originalX, originalY;

        /**
         * 切换到空闲状态
         */
        public void toIdle() {
            this.currentState = InteractionState.IDLE;
            this.activeHandle = null;
        }

        /**
         * 切换到拖动状态
         * 
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         */
        public void toDragging(double sceneX, double sceneY) {
            this.currentState = InteractionState.DRAGGING;
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
            this.activeHandle = null;
        }

        /**
         * 切换到缩放状态
         * 
         * @param handle 活动控制点
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         * @param width  原始宽度
         * @param height 原始高度
         * @param x      原始X位置
         * @param y      原始Y位置
         */
        public void toResizing(ResizeHandleManager.ResizeHandle handle, double sceneX, double sceneY,
                double width, double height, double x, double y) {
            this.currentState = InteractionState.RESIZING;
            this.activeHandle = handle;
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
            this.originalWidth = width;
            this.originalHeight = height;
            this.originalX = x;
            this.originalY = y;
        }

        /**
         * 更新原始场景坐标
         * 
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         */
        public void updateOrgScene(double sceneX, double sceneY) {
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
        }

        /**
         * 为交互准备初始坐标
         * 
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         */
        public void prepareForInteraction(double sceneX, double sceneY) {
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
        }
    }

    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public Shape(double width, double height) {
        super(width, height);
        draw();

        this.setOnMousePressed(this::handlePressed);
        this.setOnMouseDragged(this::handleDragged);
        this.setOnMouseClicked(this::handleClick);
        this.setOnMouseMoved(this::handleMouseMoved);
        this.setOnMouseReleased(this::handleMouseReleased);
        this.setOnMouseEntered(this::handleMouseEntered);
        this.setOnMouseExited(this::handleMouseExited);
    }

    /**
     * 处理鼠标按下事件 - 基础实现，子类可重写
     * 
     * @param event 鼠标事件
     */
    protected void handlePressed(MouseEvent event) {
        System.out.println("[Shape.handlePressed]" + this.toString());
        this.toFront();
        stateMachine.prepareForInteraction(event.getSceneX(), event.getSceneY());

        // 拖动时如果未选中，则先选中自己
        if (!selected) {
            handleClick(event);
        }
        event.consume();
    }

    /**
     * 处理鼠标拖拽事件 - 基础实现，子类可重写
     * 
     * @param event 鼠标事件
     */
    protected void handleDragged(MouseEvent event) {
        // 如果当前不是拖动状态，先切换到拖动状态
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.IDLE) {
            stateMachine.toDragging(stateMachine.getOrgSceneX(), stateMachine.getOrgSceneY());
        }
        handleMove(event);
        event.consume();
    }

    /**
     * 处理鼠标移动事件 - 基础实现，子类可重写
     * 
     * @param event 鼠标事件
     */
    protected void handleMouseMoved(MouseEvent event) {
        // 基础实现：什么都不做
    }

    /**
     * 处理鼠标释放事件 - 基础实现，子类可重写
     * 
     * @param event 鼠标事件
     */
    protected void handleMouseReleased(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.DRAGGING) {
            setCursor(Cursor.HAND);
        }
        event.consume();
    }

    /**
     * 处理移动操作 - 共通的拖动移动逻辑
     */
    protected void handleMove(MouseEvent event) {
        double offsetX = event.getSceneX() - stateMachine.getOrgSceneX();
        double offsetY = event.getSceneY() - stateMachine.getOrgSceneY();
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
        stateMachine.updateOrgScene(event.getSceneX(), event.getSceneY());
    }

    /**
     * 开始编辑文本
     */
    protected void startEdit() {
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

    /**
     * 完成文本编辑
     */
    protected void finishEdit() {
        if (textField == null)
            return;
        text = textField.getText();
        Pane parent = (Pane) getParent();
        parent.getChildren().remove(textField);
        textField = null;
        draw();
    }

    /**
     * 处理鼠标点击事件
     * 
     * @param event 鼠标事件
     */
    protected void handleClick(MouseEvent event) {
        System.out.println("[handleClick]" + this.toString());
        if (stateMachine.getCurrentState() != InteractionState.IDLE) {
            stateMachine.toIdle();
            return;
        }
        if (event.getClickCount() == 2) {
            startEdit();
            event.consume();
            return;
        }

        boolean multiSelect = event.isShiftDown() || event.isControlDown();

        if (multiSelect) {
            // 多选：切换当前shape的选中状态，不影响其他shape
            setSelected(!selected);
        } else {
            // 单选：取消其他shape的选中，只选中当前的
            System.out.println("[handleClick] cancel select other shape");
            for (Shape shape : selectedShapes.toArray(new Shape[0])) {
                shape.setSelected(false);
            }
            setSelected(true);
        }
        event.consume();
    }

    /**
     * 设置图形选中状态
     * 
     * @param selected 是否选中
     */
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

    /**
     * 获取文本内容
     * 
     * @return 文本内容
     */
    public String getText() {
        return text;
    }

    /**
     * 设置文本内容
     * 
     * @param text 文本内容
     */
    public void setText(String text) {
        this.text = text;
        draw();
    }

    /**
     * 设置图形宽度
     * 
     * @param width 宽度
     */
    public void setShapeWidth(double width) {
        super.setWidth(width);
        draw();
    }

    /**
     * 设置图形高度
     * 
     * @param height 高度
     */
    public void setShapeHeight(double height) {
        super.setHeight(height);
        draw();
    }

    /**
     * 抽象方法：由子类实现具体的图形绘制逻辑
     * 
     * @param gc     图形上下文
     * @param x      绘制起始x坐标
     * @param y      绘制起始y坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     */
    public abstract void drawShape(GraphicsContext gc, double x, double y, double width, double height);

    /**
     * 绘制图形到画布
     */
    protected void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // 使用工具类计算绘制区域
        double padding = 4;
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(getWidth(), getHeight(), padding);
        double x = drawingArea[0];
        double y = drawingArea[1];
        double shapeWidth = drawingArea[2];
        double shapeHeight = drawingArea[3];

        // 调用子类实现的图形绘制方法
        drawShape(gc, x, y, shapeWidth, shapeHeight);

        // 如果选中，画蓝色虚线方框和控制点
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(2); // 使用较粗的线条

            // 设置虚线样式
            gc.setLineDashes(5, 5); // 虚线长度为5，间隔为5
            gc.strokeRect(x, y, shapeWidth, shapeHeight);

            // 重置为实线，用于绘制控制点
            gc.setLineDashes(null);
            gc.setLineWidth(1);

            // 绘制八个控制点
            ResizeHandleManager.drawResizeHandles(gc, x, y, shapeWidth, shapeHeight);
        }

        // 绘制文本
        if (text != null && !text.isEmpty() && textField == null) {
            gc.setFill(Color.BLACK);
            javafx.scene.text.Font font = javafx.scene.text.Font.font(14);
            gc.setFont(font);

            // 用Text类测量文本宽度
            javafx.scene.text.Text tempText = new javafx.scene.text.Text(text);
            tempText.setFont(font);
            double textWidth = tempText.getLayoutBounds().getWidth();

            // 使用工具类计算文本居中位置
            double[] textPosition = ShapeGeometryUtils.calculateCenteredTextPosition(
                    getWidth(), getHeight(), textWidth, 14);
            double textX = textPosition[0];
            double textY = textPosition[1];

            gc.fillText(text, textX, textY);
        }

        // 绘制调试信息（canvas边界和中心点）
        drawDebugInfo(gc);
    }

    /**
     * 处理鼠标进入事件
     * 
     * @param event 鼠标事件
     */
    protected void handleMouseEntered(MouseEvent event) {
        // 如果没有选中，设置光标为手指
        if (!selected) {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 处理鼠标离开事件
     * 
     * @param event 鼠标事件
     */
    protected void handleMouseExited(MouseEvent event) {
        // 鼠标离开时恢复默认光标
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 绘制调试信息
     * 包括canvas边界框和中心点标记
     * 
     * @param gc 图形上下文
     */
    protected void drawDebugInfo(GraphicsContext gc) {
        // 保存当前绘制状态
        Color originalStroke = (Color) gc.getStroke();
        Color originalFill = (Color) gc.getFill();
        double originalLineWidth = gc.getLineWidth();
        double[] originalLineDashes = gc.getLineDashes();

        // 绘制canvas边界框
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);
        gc.setLineDashes(2, 2); // 红色虚线边框
        gc.strokeRect(0, 0, getWidth(), getHeight());

        // 计算并绘制canvas中心点
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;

        // 绘制中心点十字标记
        gc.setLineDashes(null); // 实线
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);

        double crossSize = 8;
        gc.strokeLine(centerX - crossSize, centerY, centerX + crossSize, centerY); // 水平线
        gc.strokeLine(centerX, centerY - crossSize, centerX, centerY + crossSize); // 垂直线

        // 绘制中心点圆圈
        double circleRadius = 3;
        gc.setFill(Color.RED);
        gc.fillOval(centerX - circleRadius, centerY - circleRadius,
                circleRadius * 2, circleRadius * 2);

        // 恢复原始绘制状态
        gc.setStroke(originalStroke);
        gc.setFill(originalFill);
        gc.setLineWidth(originalLineWidth);
        gc.setLineDashes(originalLineDashes);

        // 输出调试信息到控制台
        /* System.out.println("[DebugCanvas] " + this.getClass().getSimpleName() +
                " - Canvas size: " + getWidth() + "x" + getHeight() +
                ", Center at: (" + centerX + "," + centerY + ")" +
                ", Position: (" + getLayoutX() + "," + getLayoutY() + ")"); */
    }
}
