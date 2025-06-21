package org.jrawio.controller.shape;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import org.jrawio.controller.components.RightPanel;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

public abstract class Shape extends Canvas {
    /** 是否被选中 */
    private boolean selected = false;
    
    /** 被选中的所有图形 */
    private static final Set<Shape> selectedShapes = new HashSet<>();

    /** 文本内容 */
    private String text;
    
    /** 文本框控件 */
    private TextField textField;

    /** 操作状态机 */
    private final ShapeStateMachine stateMachine = new ShapeStateMachine();
    
    /** 控制点大小 */
    private static final double HANDLE_SIZE = 6;


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
        private ResizeHandle activeHandle = null;
        
        /** 原始尺寸和位置 */
        private double originalWidth, originalHeight;
        private double originalX, originalY;

        /**
         * 获取当前交互状态
         * @return 当前交互状态
         */
        public InteractionState getCurrentState() {
            return currentState;
        }

        /**
         * 切换到空闲状态
         */
        public void toIdle() {
            this.currentState = InteractionState.IDLE;
            this.activeHandle = null;
        }

        /**
         * 切换到拖动状态
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
         * @param handle 活动控制点
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         * @param width 原始宽度
         * @param height 原始高度
         * @param x 原始X位置
         * @param y 原始Y位置
         */
        public void toResizing(ResizeHandle handle, double sceneX, double sceneY,
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
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         */
        public void updateOrgScene(double sceneX, double sceneY) {
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
        }

        /**
         * 为交互准备初始坐标
         * @param sceneX 场景X坐标
         * @param sceneY 场景Y坐标
         */
        public void prepareForInteraction(double sceneX, double sceneY) {
            this.orgSceneX = sceneX;
            this.orgSceneY = sceneY;
        }
    }

    /**
     * 缩放控制点枚举
     */
    public enum ResizeHandle {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    /**
     * 构造函数
     * @param width 图形宽度
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
     * 开始编辑文本
     */
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

    /**
     * 完成文本编辑
     */
    private void finishEdit() {
        if (textField == null)
            return;
        text = textField.getText();
        Pane parent = (Pane) getParent();
        parent.getChildren().remove(textField);
        textField = null;
        draw();
    }

    /**
     * 处理鼠标按下事件
     * @param event 鼠标事件
     */
    private void handlePressed(MouseEvent event) {
        System.out.println("[handlePressed]" + this.toString());
        this.toFront();
        stateMachine.prepareForInteraction(event.getSceneX(), event.getSceneY());

        // 检查是否点击在控制点上
        if (selected) {
            ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
            if (handle != null) {
                stateMachine.toResizing(handle, event.getSceneX(), event.getSceneY(),
                        getWidth(), getHeight(), getLayoutX(), getLayoutY());
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

    /**
     * 处理鼠标拖拽事件
     * @param event 鼠标事件
     */
    private void handleDragged(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING && stateMachine.getActiveHandle() != null) {
            handleResize(event);
        } else {
            // 如果当前不是拖动状态，先切换到拖动状态
            if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.IDLE) {
                stateMachine.toDragging(stateMachine.getOrgSceneX(), stateMachine.getOrgSceneY());
            }
            handleMove(event);
        }
        event.consume();
    }

    /**
     * 处理移动操作
     */
    private void handleMove(MouseEvent event) {
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
     * 处理缩放操作
     */
    private void handleResize(MouseEvent event) {
        double deltaX = event.getSceneX() - stateMachine.getOrgSceneX();
        double deltaY = event.getSceneY() - stateMachine.getOrgSceneY();

        double newWidth = stateMachine.getOriginalWidth();
        double newHeight = stateMachine.getOriginalHeight();
        double newX = stateMachine.getOriginalX();
        double newY = stateMachine.getOriginalY();

        switch (stateMachine.getActiveHandle()) {
            case TOP_LEFT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() - deltaX);
                newHeight = Math.max(20, stateMachine.getOriginalHeight() - deltaY);
                newX = stateMachine.getOriginalX() + (stateMachine.getOriginalWidth() - newWidth);
                newY = stateMachine.getOriginalY() + (stateMachine.getOriginalHeight() - newHeight);
                break;
            case TOP_CENTER:
                newHeight = Math.max(20, stateMachine.getOriginalHeight() - deltaY);
                newY = stateMachine.getOriginalY() + (stateMachine.getOriginalHeight() - newHeight);
                break;
            case TOP_RIGHT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() + deltaX);
                newHeight = Math.max(20, stateMachine.getOriginalHeight() - deltaY);
                newY = stateMachine.getOriginalY() + (stateMachine.getOriginalHeight() - newHeight);
                break;
            case MIDDLE_LEFT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() - deltaX);
                newX = stateMachine.getOriginalX() + (stateMachine.getOriginalWidth() - newWidth);
                break;
            case MIDDLE_RIGHT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() + deltaX);
                break;
            case BOTTOM_LEFT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() - deltaX);
                newHeight = Math.max(20, stateMachine.getOriginalHeight() + deltaY);
                newX = stateMachine.getOriginalX() + (stateMachine.getOriginalWidth() - newWidth);
                break;
            case BOTTOM_CENTER:
                newHeight = Math.max(20, stateMachine.getOriginalHeight() + deltaY);
                break;
            case BOTTOM_RIGHT:
                newWidth = Math.max(20, stateMachine.getOriginalWidth() + deltaX);
                newHeight = Math.max(20, stateMachine.getOriginalHeight() + deltaY);
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

    /**
     * 处理鼠标点击事件
     * @param event 鼠标事件
     */
    private void handleClick(MouseEvent event) {
        System.out.println("[handleClick]" + this.toString());
        if (event.getClickCount() == 2) {
            startEdit();
            event.consume();
            return;
        }
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.DRAGGING || 
            stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING) {
            // 如果是拖动或缩放后产生的点击，忽略
            stateMachine.toIdle();
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
     * @return 文本内容
     */
    public String getText() {
        return text;
    }

    /**
     * 设置文本内容
     * @param text 文本内容
     */
    public void setText(String text) {
        this.text = text;
        draw();
    }

    /**
     * 设置图形宽度
     * @param width 宽度
     */
    public void setShapeWidth(double width) {
        super.setWidth(width);
        draw();
    }

    /**
     * 设置图形高度
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
    protected abstract void drawShape(GraphicsContext gc, double x, double y, double width, double height);

    /**
     * 公共方法：用于预览绘制（不包含选中状态和文本）
     * @param gc 图形上下文
     * @param x 绘制X坐标
     * @param y 绘制Y坐标
     * @param width 绘制宽度
     * @param height 绘制高度
     */
    public final void drawPreview(GraphicsContext gc, double x, double y, double width, double height) {
        drawShape(gc, x, y, width, height);
    }

    /**
     * 绘制图形到画布
     */
    private void draw() {
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

        // 如果选中，画蓝色方框和控制点
        if (selected) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(1);
            gc.strokeRect(x, y, shapeWidth, shapeHeight);

            // 绘制八个控制点
            drawResizeHandles(gc, x, y, shapeWidth, shapeHeight);
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
    }

    /**
     * 绘制缩放控制点
     * @param gc 图形上下文
     * @param shapeX 图形X坐标
     * @param shapeY 图形Y坐标
     * @param shapeWidth 图形宽度
     * @param shapeHeight 图形高度
     */
    private void drawResizeHandles(GraphicsContext gc, double shapeX, double shapeY, double shapeWidth,
            double shapeHeight) {
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);

        // 使用工具类计算控制点位置
        double[][] handlePositions = ShapeGeometryUtils.calculateResizeHandlePositions(
                shapeX, shapeY, shapeWidth, shapeHeight, HANDLE_SIZE);

        // 绘制每个控制点
        for (double[] pos : handlePositions) {
            gc.fillRect(pos[0], pos[1], HANDLE_SIZE, HANDLE_SIZE);
            gc.strokeRect(pos[0], pos[1], HANDLE_SIZE, HANDLE_SIZE);
        }
    }

    /**
     * 处理鼠标进入事件
     * @param event 鼠标事件
     */
    private void handleMouseEntered(MouseEvent event) {
        // 如果没有选中，设置光标为手指
        if (!selected) {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 处理鼠标离开事件
     * @param event 鼠标事件
     */
    private void handleMouseExited(MouseEvent event) {
        // 鼠标离开时恢复默认光标
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 处理鼠标移动事件
     * @param event 鼠标事件
     */
    private void handleMouseMoved(MouseEvent event) {
        if (!selected) {
            setCursor(Cursor.HAND);
            return;
        }

        ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
        if (handle != null) {
            setCursor(getCursorForHandle(handle));
        } else {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 检测鼠标位置是否在某个控制点上
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @return 控制点类型，如果不在控制点上则返回null
     */
    private ResizeHandle getResizeHandleAt(double x, double y) {
        if (!selected)
            return null;

        // 使用工具类计算绘制区域
        double padding = 4;
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(getWidth(), getHeight(), padding);
        double shapeX = drawingArea[0];
        double shapeY = drawingArea[1];
        double shapeWidth = drawingArea[2];
        double shapeHeight = drawingArea[3];

        // 使用工具类计算控制点位置
        double[][] handlePositions = ShapeGeometryUtils.calculateResizeHandlePositions(
                shapeX, shapeY, shapeWidth, shapeHeight, HANDLE_SIZE);

        // 控制点类型数组，与位置数组对应
        ResizeHandle[] handleTypes = {
                ResizeHandle.TOP_LEFT, ResizeHandle.TOP_CENTER, ResizeHandle.TOP_RIGHT,
                ResizeHandle.MIDDLE_LEFT, ResizeHandle.MIDDLE_RIGHT,
                ResizeHandle.BOTTOM_LEFT, ResizeHandle.BOTTOM_CENTER, ResizeHandle.BOTTOM_RIGHT
        };

        // 检测每个控制点
        for (int i = 0; i < handlePositions.length; i++) {
            if (ShapeGeometryUtils.isPointInHandle(x, y, handlePositions[i][0], handlePositions[i][1], HANDLE_SIZE)) {
                return handleTypes[i];
            }
        }

        return null;
    }

    /**
     * 根据控制点获取对应的鼠标光标
     * @param handle 控制点类型
     * @return 对应的光标类型
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
        stateMachine.toIdle();
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 处理鼠标释放事件
     * @param event 鼠标事件
     */
    private void handleMouseReleased(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING) {
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
