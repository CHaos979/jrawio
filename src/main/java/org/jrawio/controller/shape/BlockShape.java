package org.jrawio.controller.shape;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.jrawio.controller.components.RightPanel;

/**
 * 块状图形抽象类 - 负责管理拖动和缩放逻辑
 * 作为Shape和具体形状类（如OvalShape、RectangleShape）之间的中间层
 */
public abstract class BlockShape extends Shape {
    /** 箭头创建相关字段 */
    private Point2D arrowStartPoint = null;
    private Point2D currentArrowEndPoint = null;
    private ArrowShape temporaryArrow = null;

    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public BlockShape(double width, double height) {
        // 为了容纳箭头控制点，需要增加canvas的尺寸
        // 箭头控制点位于图形外围，需要额外的空间
        super(width + 2 * (ArrowHandleManager.getArrowHandleOffset() + ArrowHandleManager.getArrowHandleSize()),
                height + 2 * (ArrowHandleManager.getArrowHandleOffset() + ArrowHandleManager.getArrowHandleSize()));
    }

    /**
     * 重写控制点交互处理，检查缩放控制点和箭头控制点
     */
    @Override
    protected boolean handleControlPointInteraction(MouseEvent event) {
        // 首先检查缩放控制点
        ResizeHandleManager.ResizeHandle resizeHandle = getResizeHandleAt(event.getX(), event.getY());
        if (resizeHandle != null) {
            stateMachine.toResizing(resizeHandle, event.getSceneX(), event.getSceneY(),
                    getWidth(), getHeight(), getLayoutX(), getLayoutY());
            return true; // 已处理缩放控制点交互
        }

        // 然后检查箭头控制点
        ArrowHandleManager.ArrowHandle arrowHandle = getArrowHandleAt(event.getX(), event.getY());
        if (arrowHandle != null) {
            handleArrowControlPointClick(arrowHandle, event);
            return true; // 已处理箭头控制点交互
        }

        return false; // 没有控制点或没有处理
    }

    /**
     * 重写特定拖拽处理，优先处理缩放拖拽和箭头创建拖拽
     */
    @Override
    protected boolean handleSpecificDrag(MouseEvent event) {
        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.RESIZING
                && stateMachine.getActiveHandle() != null) {
            handleResize(event);
            return true; // 已处理缩放拖拽
        }

        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.CREATING_ARROW
                && stateMachine.getActiveArrowHandle() != null) {
            handleArrowCreationDrag(event);
            return true; // 已处理箭头创建拖拽
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

        // 更新文本框位置（考虑额外空间）
        if (textField != null) {
            double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
            double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
            double extraSpace = arrowHandleOffset + arrowHandleSize;

            // 文本框位置需要考虑图形在canvas中的偏移
            textField.setLayoutX(getLayoutX() + 4 + extraSpace);
            textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
            textField.setPrefWidth(getWidth() - 8 - 2 * extraSpace);
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

        // 首先检查缩放控制点
        ResizeHandleManager.ResizeHandle resizeHandle = getResizeHandleAt(event.getX(), event.getY());
        if (resizeHandle != null) {
            setCursor(ResizeHandleManager.getCursorForHandle(resizeHandle));
            return;
        }

        // 然后检查箭头控制点
        ArrowHandleManager.ArrowHandle arrowHandle = getArrowHandleAt(event.getX(), event.getY());
        if (arrowHandle != null) {
            setCursor(ArrowHandleManager.getCursorForArrowHandle(arrowHandle));
            return;
        }

        // 如果都不在控制点上，使用默认光标
        setCursor(Cursor.HAND);
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

        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace; // 原有padding + 箭头控制点所需空间

        return ResizeHandleManager.getResizeHandleAt(x, y, getWidth(), getHeight(), padding);
    }

    /**
     * 检测鼠标位置是否在某个箭头控制点上
     * 
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @return 箭头控制点类型，如果不在控制点上则返回null
     */
    protected ArrowHandleManager.ArrowHandle getArrowHandleAt(double x, double y) {
        if (!selected)
            return null;

        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;
        double padding = 4 + extraSpace; // 原有padding + 箭头控制点所需空间

        return ArrowHandleManager.getArrowHandleAt(x, y, getWidth(), getHeight(), padding);
    }

    /**
     * 处理箭头控制点点击事件
     * 
     * @param arrowHandle 被点击的箭头控制点
     * @param event       鼠标事件
     */
    protected void handleArrowControlPointClick(ArrowHandleManager.ArrowHandle arrowHandle, MouseEvent event) {
        // 启动箭头创建模式
        stateMachine.toCreatingArrow(arrowHandle, event.getSceneX(), event.getSceneY());

        // 使用子类实现的方法计算箭头起始点
        arrowStartPoint = calculateArrowConnectionPoint(arrowHandle);
        // 获取容器来进行坐标转换
        Pane container = ArrowCreationManager.getShapeContainer(this);
        if (container != null) {
            // 将场景坐标转换为容器坐标
            Point2D containerPoint = container.sceneToLocal(event.getSceneX(), event.getSceneY());
            currentArrowEndPoint = containerPoint;
        } else {
            currentArrowEndPoint = new Point2D(event.getSceneX(), event.getSceneY());
        }

        System.out.println("Arrow creation started from: " + arrowHandle + " at point: " + arrowStartPoint);

        // 暂时只处理事件消费，防止进一步传播
        event.consume();
    }

    /**
     * 处理箭头创建拖拽
     */
    protected void handleArrowCreationDrag(MouseEvent event) {
        if (arrowStartPoint == null) {
            return;
        }
        // 获取容器来进行坐标转换
        Pane container = ArrowCreationManager.getShapeContainer(this);
        Point2D dragEndPoint;

        if (container != null) {
            // 将场景坐标转换为容器坐标
            dragEndPoint = container.sceneToLocal(event.getSceneX(), event.getSceneY());
        } else {
            dragEndPoint = new Point2D(event.getSceneX(), event.getSceneY());
        }

        // 检查是否有可吸附的目标形状
        Point2D snapPoint = findSnapTargetPoint(dragEndPoint, container);
        if (snapPoint != null) {
            // 使用吸附点作为箭头结束点
            currentArrowEndPoint = snapPoint;
        } else {
            // 使用原始拖拽点
            currentArrowEndPoint = dragEndPoint;
        }

        // 清除之前的临时箭头
        if (temporaryArrow != null) {
            if (container != null) {
                container.getChildren().remove(temporaryArrow);
            }
        }

        // 创建新的临时箭头用于预览
        temporaryArrow = ArrowCreationManager.createArrow(arrowStartPoint, currentArrowEndPoint);
        temporaryArrow.setOpacity(0.5); // 设置半透明以表示这是预览

        // 添加到容器中
        if (container != null) {
            ArrowCreationManager.addArrowToContainer(temporaryArrow, container);
        }
    }

    /**
     * 完成箭头创建
     */
    protected void completeArrowCreation(MouseEvent event) {
        if (arrowStartPoint == null) {
            resetArrowCreationState();
            return;
        }
        // 获取容器来进行坐标转换
        Pane container = ArrowCreationManager.getShapeContainer(this);
        Point2D finalEndPoint;

        if (container != null) {
            // 将场景坐标转换为容器坐标
            Point2D rawEndPoint = container.sceneToLocal(event.getSceneX(), event.getSceneY());

            // 检查是否有可吸附的目标形状
            Point2D snapPoint = findSnapTargetPoint(rawEndPoint, container);
            if (snapPoint != null) {
                // 使用吸附点作为最终箭头结束点
                finalEndPoint = snapPoint;
                System.out.println("Arrow snapped to point: " + snapPoint);
            } else {
                // 使用原始拖拽点
                finalEndPoint = rawEndPoint;
            }
        } else {
            finalEndPoint = new Point2D(event.getSceneX(), event.getSceneY());
        }

        // 检查箭头长度是否足够（避免创建过短的箭头）
        double distance = arrowStartPoint.distance(finalEndPoint);
        if (distance < 10) { // 最小箭头长度
            System.out.println("Arrow too short, creation cancelled");
            resetArrowCreationState();
            return;
        }

        // 移除临时箭头
        if (temporaryArrow != null) {
            if (container != null) {
                container.getChildren().remove(temporaryArrow);
            }
        }

        // 创建最终的箭头
        ArrowShape finalArrow = ArrowCreationManager.createArrow(arrowStartPoint, finalEndPoint);

        // 添加到容器中
        if (container != null) {
            ArrowCreationManager.addArrowToContainer(finalArrow, container);
            System.out.println("Arrow created from " + arrowStartPoint + " to " + finalEndPoint);
        }

        // 重置状态
        resetArrowCreationState();
    }

    /**
     * 重置箭头创建状态
     */
    protected void resetArrowCreationState() { // 移除临时箭头
        if (temporaryArrow != null) {
            Pane container = ArrowCreationManager.getShapeContainer(this);
            if (container != null) {
                container.getChildren().remove(temporaryArrow);
            }
        }

        // 重置状态
        stateMachine.toIdle();
        arrowStartPoint = null;
        currentArrowEndPoint = null;
        temporaryArrow = null;
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 重置缩放状态
     */
    protected void resetResizeState() {
        stateMachine.toIdle();
        setCursor(Cursor.DEFAULT);
    }

    /**
     * 重写特定释放处理，处理缩放结束和箭头创建完成
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

        if (stateMachine.getCurrentState() == ShapeStateMachine.InteractionState.CREATING_ARROW) {
            completeArrowCreation(event);
            return true; // 已处理箭头创建完成
        }

        return false; // 没有特定释放，使用标准释放
    }

    /**
     * 重写绘制方法，添加箭头控制点的绘制
     */
    @Override
    protected void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // 计算额外空间（用于箭头控制点）
        double arrowHandleOffset = ArrowHandleManager.getArrowHandleOffset();
        double arrowHandleSize = ArrowHandleManager.getArrowHandleSize();
        double extraSpace = arrowHandleOffset + arrowHandleSize;

        // 使用工具类计算绘制区域，但要考虑额外的空间
        double padding = 4 + extraSpace; // 原有padding + 箭头控制点所需空间
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

            // 绘制八个缩放控制点
            ResizeHandleManager.drawResizeHandles(gc, x, y, shapeWidth, shapeHeight);

            // 绘制四个箭头控制点（位于缩放控制框外围）
            ArrowHandleManager.drawArrowHandles(gc, x, y, shapeWidth, shapeHeight);
        } // 绘制文本
        if (text != null && !text.isEmpty() && textField == null) {
            gc.setFill(Color.BLACK);
            Font font = Font.font(14);
            gc.setFont(font);

            // 用Text类测量文本宽度
            Text tempText = new Text(text);
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
     * 查找可吸附的目标点
     * 遍历容器中的其他形状，查找最近的可吸附点
     * 
     * @param mousePoint 鼠标当前位置（容器坐标）
     * @param container  形状容器
     * @return 最近的可吸附点，如果没有找到则返回null
     */
    protected Point2D findSnapTargetPoint(Point2D mousePoint, Pane container) {
        if (container == null) {
            return null;
        }

        double snapRadius = 20.0; // 吸附半径，可以根据需要调整
        Point2D nearestSnapPoint = null;
        double minDistance = Double.MAX_VALUE;

        // 遍历容器中的所有子节点
        for (javafx.scene.Node node : container.getChildren()) {
            // 只处理BlockShape类型的节点，且不是当前形状本身
            if (node instanceof BlockShape && node != this) {
                BlockShape targetShape = (BlockShape) node;

                // 将鼠标点转换为目标形状的本地坐标
                Point2D localMousePoint = targetShape.parentToLocal(mousePoint);

                // 使用目标形状的吸附点查找方法
                Point2D shapeSnapPoint = targetShape.findNearestSnapPoint(localMousePoint, snapRadius);

                if (shapeSnapPoint != null) {
                    // 将形状本地坐标转换回容器坐标
                    Point2D containerSnapPoint = targetShape.localToParent(shapeSnapPoint);

                    // 计算距离
                    double distance = mousePoint.distance(containerSnapPoint);

                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestSnapPoint = containerSnapPoint;
                    }
                }
            }
        }

        return nearestSnapPoint;
    }

    /**
     * 抽象方法：由子类实现具体的箭头连接点计算
     * 子类需要根据自己的形状特点计算精确的连接点位置
     * 
     * @param arrowHandle 箭头控制点类型
     * @return 箭头起始点的绝对坐标
     */
    protected abstract Point2D calculateArrowConnectionPoint(ArrowHandleManager.ArrowHandle arrowHandle);

    /**
     * 抽象方法：由子类实现查找最近的可吸附点
     * 子类需要根据自己的形状特点和当前鼠标位置计算最近的可吸附点
     * 
     * @param mousePoint 鼠标当前位置（相对于shape的本地坐标）
     * @param snapRadius 吸附半径，在此半径内的点会被吸附
     * @return 最近的可吸附点，如果没有找到则返回null
     */
    protected abstract Point2D findNearestSnapPoint(Point2D mousePoint, double snapRadius);
}
