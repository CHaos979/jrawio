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
     * 重写控制点交互处理，检查缩放控制点
     */
    @Override
    protected boolean handleControlPointInteraction(MouseEvent event) {
        ResizeHandleManager.ResizeHandle handle = getResizeHandleAt(event.getX(), event.getY());
        if (handle != null) {
            stateMachine.toResizing(handle, event.getSceneX(), event.getSceneY(),
                    getWidth(), getHeight(), getLayoutX(), getLayoutY());
            return true; // 已处理控制点交互
        }
        return false; // 没有控制点或没有处理
    }

    /**
     * 重写特定拖拽处理，优先处理缩放拖拽
     */
    @Override
    protected boolean handleSpecificDrag(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING
                && stateMachine.getActiveHandle() != null) {
            handleResize(event);
            return true; // 已处理缩放拖拽
        }
        return false; // 没有特定拖拽，使用标准拖拽
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
     * 重写特定释放处理，处理缩放结束
     */
    @Override
    protected boolean handleSpecificRelease(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING) {
            resetResizeState();
            // 通知右侧面板更新尺寸信息
            RightPanel rightPanel = RightPanel.getInstance();
            if (rightPanel != null) {
                rightPanel.onShapeSelectionChanged(selectedShapes);
            }
            return true; // 已处理缩放释放
        }
        return false; // 没有特定释放，使用标准释放
    }
}
