package org.jrawio.controller.shape;

import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import org.jrawio.controller.components.RightPanel;

/**
 * 块状图形抽象类 - 负责管理拖动和缩放逻辑
 * 作为Shape和具体形状类（如OvalShape、RectangleShape）之间的中间层
 */
public abstract class BlockShape extends Shape {

    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public BlockShape(double width, double height) {
        super(width, height);
    }

    /**
     * 处理鼠标按下事件 - 专门处理拖动和缩放的开始
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handlePressed(MouseEvent event) {
        System.out.println("[BlockShape.handlePressed]" + this.toString());
        this.toFront();
        stateMachine.prepareForInteraction(event.getSceneX(), event.getSceneY());

        // 检查是否点击在控制点上
        if (selected) {
            ResizeHandleManager.ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
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
     * 处理鼠标拖拽事件 - 专门处理拖动和缩放逻辑
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handleDragged(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING
                && stateMachine.getActiveHandle() != null) {
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
     * 处理缩放操作
     */
    protected void handleResize(MouseEvent event) {
        double deltaX = event.getSceneX() - stateMachine.getOrgSceneX();
        double deltaY = event.getSceneY() - stateMachine.getOrgSceneY();

        // 使用控制点管理器计算新的尺寸和位置
        double[] newDimensions = ResizeHandleManager.calculateNewDimensions(
                stateMachine.getActiveHandle(), deltaX, deltaY,
                stateMachine.getOriginalWidth(), stateMachine.getOriginalHeight(),
                stateMachine.getOriginalX(), stateMachine.getOriginalY(),
                20 // 最小尺寸
        );

        // 应用新的尺寸和位置
        setShapeWidth(newDimensions[0]); // newWidth
        setShapeHeight(newDimensions[1]); // newHeight
        setLayoutX(newDimensions[2]); // newX
        setLayoutY(newDimensions[3]); // newY

        // 更新文本框位置
        if (textField != null) {
            textField.setLayoutX(getLayoutX() + 4);
            textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
            textField.setPrefWidth(getWidth() - 8);
        }
    }

    /**
     * 处理鼠标移动事件 - 管理光标变化
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handleMouseMoved(MouseEvent event) {
        if (!selected) {
            setCursor(Cursor.HAND);
            return;
        }

        ResizeHandleManager.ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
        if (handle != null) {
            setCursor(ResizeHandleManager.getCursorForHandle(handle));
        } else {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 检测鼠标位置是否在某个控制点上
     * 
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @return 控制点类型，如果不在控制点上则返回null
     */
    protected ResizeHandleManager.ResizeHandle getResizeHandleAt(double x, double y) {
        if (!selected)
            return null;

        double padding = 4;
        return ResizeHandleManager.getResizeHandleAt(x, y, getWidth(), getHeight(), padding);
    }

    /**
     * 重置缩放状态
     */
    protected void resetResizeState() {
        stateMachine.toIdle();
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 处理鼠标释放事件 - 完成拖动或缩放操作
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handleMouseReleased(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING) {
            resetResizeState();
            // 通知右侧面板更新尺寸信息
            RightPanel rightPanel = RightPanel.getInstance();
            if (rightPanel != null) {
                rightPanel.onShapeSelectionChanged(selectedShapes);
            }
        } else if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.DRAGGING) {
            setCursor(Cursor.HAND);
        }
        event.consume();
    }
}
