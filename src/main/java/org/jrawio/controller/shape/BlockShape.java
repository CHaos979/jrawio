package org.jrawio.controller.shape;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private Set<LineShape> LineStart = new HashSet<>();
    private Set<LineShape> LineEnd = new HashSet<>();

    /** 形状颜色属性 */
    private Color fillColor = Color.TRANSPARENT; // 默认填充为透明
    private Color strokeColor = Color.BLACK; // 默认边框颜色为黑色

    /** 可吸附点视觉显示相关字段 */
    private List<javafx.scene.shape.Circle> snapPointIndicators = new ArrayList<>();
    private SnapTargetResult currentSnapTarget = null;

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
     * 拷贝构造方法
     * 创建一个与源BlockShape具有相同属性的新BlockShape实例
     * 
     * @param source 源BlockShape对象
     */
    protected BlockShape(BlockShape source) {
        super(source);

        // 复制BlockShape特有属性
        // 注意：不复制连接线集合，新对象应该没有连接
        this.LineStart = new HashSet<>();
        this.LineEnd = new HashSet<>();

        // 复制颜色属性
        this.fillColor = source.fillColor;
        this.strokeColor = source.strokeColor;

        // 不复制箭头创建状态，新对象应该是干净状态
        this.arrowStartPoint = null;
        this.currentArrowEndPoint = null;
        this.temporaryArrow = null;
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
        SnapTargetResult snapResult = findSnapTarget(dragEndPoint, container);
        Point2D snapPoint = null;

        if (snapResult != null) {
            snapPoint = snapResult.snapPoint;
            // 使用吸附点作为箭头结束点
            currentArrowEndPoint = snapPoint;

            // 更新可吸附点的视觉显示
            updateSnapPointVisuals(snapResult, container);
        } else {
            // 使用原始拖拽点
            currentArrowEndPoint = dragEndPoint;

            // 清除可吸附点的视觉显示
            clearSnapPointVisuals(container);
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
        BlockShape targetShape = null;

        if (container != null) {
            // 将场景坐标转换为容器坐标
            Point2D rawEndPoint = container.sceneToLocal(event.getSceneX(), event.getSceneY());

            // 检查是否有可吸附的目标形状
            SnapTargetResult snapResult = findSnapTarget(rawEndPoint, container);
            if (snapResult != null) {
                // 使用吸附点作为最终箭头结束点
                finalEndPoint = snapResult.snapPoint;
                targetShape = snapResult.targetShape;
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
        }

        // 建立双向连接
        addLineStart(finalArrow);
        if (targetShape != null) {
            targetShape.addLineEnd(finalArrow);
        }

        // 重置状态
        resetArrowCreationState();
    }

    /**
     * 重置箭头创建状态
     */
    protected void resetArrowCreationState() {
        // 获取容器
        Pane container = ArrowCreationManager.getShapeContainer(this);

        // 移除临时箭头
        if (temporaryArrow != null) {
            if (container != null) {
                container.getChildren().remove(temporaryArrow);
            }
        }

        // 清除可吸附点的视觉显示
        clearSnapPointVisuals(container);

        // 重置状态
        stateMachine.toIdle();
        arrowStartPoint = null;
        currentArrowEndPoint = null;
        temporaryArrow = null;
        currentSnapTarget = null;
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
    public void draw() {
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

        // 设置图形绘制颜色，处理NULL情况为透明色
        gc.setFill(fillColor != null ? fillColor : Color.TRANSPARENT);
        gc.setStroke(strokeColor != null ? strokeColor : Color.TRANSPARENT);

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
        // drawDebugInfo(gc); // 调试绘制已禁用
    }

    /**
     * 查找可吸附的目标点
     * 遍历容器中的其他形状，查找最近的可吸附点
     * 
     * @param mousePoint 鼠标当前位置（容器坐标）
     * @param container  形状容器
     * @return 吸附目标结果，如果没有找到则返回null
     */
    protected SnapTargetResult findSnapTarget(Point2D mousePoint, Pane container) {
        if (container == null) {
            return null;
        }

        double snapRadius = 20.0; // 吸附半径，可以根据需要调整
        SnapTargetResult nearestTarget = null;
        double minDistance = Double.MAX_VALUE; // 第一步：遍历容器中的所有子节点，找到最近的图形
        BlockShape nearestShape = null;
        double minShapeDistance = Double.MAX_VALUE;

        for (javafx.scene.Node node : container.getChildren()) {
            // 只处理BlockShape类型的节点，且不是当前形状本身
            if (node instanceof BlockShape && node != this) {
                BlockShape targetShape = (BlockShape) node;

                // 计算鼠标点到图形中心的距离作为初步筛选
                double shapeCenterX = targetShape.getLayoutX() + targetShape.getWidth() / 2;
                double shapeCenterY = targetShape.getLayoutY() + targetShape.getHeight() / 2;
                Point2D shapeCenter = new Point2D(shapeCenterX, shapeCenterY);

                double distanceToShape = mousePoint.distance(shapeCenter);
                if (distanceToShape < minShapeDistance) {
                    minShapeDistance = distanceToShape;
                    nearestShape = targetShape;
                }
            }
        }

        // 第二步：如果找到最近的图形，则从该图形中找最近的吸附点
        if (nearestShape != null) {
            List<Point2D> snapPoints = nearestShape.getAllSnapPoints();

            for (Point2D snapPoint : snapPoints) {
                // 将形状本地坐标转换回容器坐标
                Point2D containerSnapPoint = nearestShape.localToParent(snapPoint);

                // 计算距离
                double distance = mousePoint.distance(containerSnapPoint);

                if (distance <= snapRadius && distance < minDistance) {
                    minDistance = distance;
                    nearestTarget = new SnapTargetResult(nearestShape, containerSnapPoint);
                }
            }
        }

        return nearestTarget;
    }

    /**
     * 查找可吸附的目标点（保持向后兼容）
     * 遍历容器中的其他形状，查找最近的可吸附点
     * 
     * @param mousePoint 鼠标当前位置（容器坐标）
     * @param container  形状容器
     * @return 最近的可吸附点，如果没有找到则返回null
     */
    protected Point2D findSnapTargetPoint(Point2D mousePoint, Pane container) {
        SnapTargetResult result = findSnapTarget(mousePoint, container);
        return result != null ? result.snapPoint : null;
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
     * 抽象方法：由子类实现获取所有可吸附点
     * 子类需要根据自己的形状特点返回所有可用的吸附点
     * 
     * @return 所有可吸附点的列表
     */
    protected abstract List<Point2D> getAllSnapPoints();

    /**
     * 获取从此形状开始的线形集合
     * 
     * @return 线形集合
     */
    public Set<LineShape> getLineStart() {
        return new HashSet<>(LineStart);
    }

    /**
     * 获取连接到此形状的线形集合
     * 
     * @return 线形集合
     */
    public Set<LineShape> getLineEnd() {
        return new HashSet<>(LineEnd);
    }

    /**
     * 添加从此形状开始的线形
     * 
     * @param line 线形
     */
    public void addLineStart(LineShape line) {
        if (!LineStart.contains(line)) {
            LineStart.add(line);
            // 只有当线形的起始形状不是当前形状时才设置，避免循环调用
            if (line.getStartShape() != this) {
                line.setStartShapeInternal(this);
            }
        }
    }

    /**
     * 添加连接到此形状的线形
     * 
     * @param line 线形
     */
    public void addLineEnd(LineShape line) {
        if (!LineEnd.contains(line)) {
            LineEnd.add(line);
            // 只有当线形的结束形状不是当前形状时才设置，避免循环调用
            if (line.getEndShape() != this) {
                line.setEndShapeInternal(this);
            }
        }
    }

    /**
     * 移除从此形状开始的线形
     * 
     * @param line 线形
     */
    public void removeLineStart(LineShape line) {
        if (LineStart.remove(line)) {
            // 只有当线形的起始形状是当前形状时才清除，避免循环调用
            if (line.getStartShape() == this) {
                line.setStartShapeInternal(null);
            }
        }
    }

    /**
     * 移除连接到此形状的线形
     * 
     * @param line 线形
     */
    public void removeLineEnd(LineShape line) {
        if (LineEnd.remove(line)) {
            // 只有当线形的结束形状是当前形状时才清除，避免循环调用
            if (line.getEndShape() == this) {
                line.setEndShapeInternal(null);
            }
        }
    }

    /**
     * 重写位置变化后的处理逻辑
     * 当形状位置改变时，更新所有连接线的端点
     */
    @Override
    protected void onPositionChanged(double offsetX, double offsetY) {
        updateConnectedLines();
    }

    /**
     * 重写尺寸变化后的处理逻辑
     * 当形状尺寸改变时，更新所有连接线的端点
     */
    @Override
    protected void onSizeChanged() {
        updateConnectedLines();
    }

    /**
     * 更新所有连接的线形的端点位置
     * 此方法在形状移动或尺寸改变后被调用
     */
    protected void updateConnectedLines() {
        // 更新从此形状开始的线形的起始点
        for (LineShape line : LineStart) {
            if (line instanceof ArrowShape) {
                ArrowShape arrow = (ArrowShape) line;
                // 根据箭头的方向重新计算起始点
                Point2D newStartPoint = calculateConnectionPointForArrow(arrow);
                if (newStartPoint != null) {
                    // 将绝对坐标转换为相对于线形画布的坐标
                    Point2D relativeStartPoint = convertToLineRelativeCoordinate(arrow, newStartPoint);
                    arrow.setStartPoint(relativeStartPoint);
                }
            }
        }

        // 更新连接到此形状的线形的结束点
        for (LineShape line : LineEnd) {
            if (line instanceof ArrowShape) {
                ArrowShape arrow = (ArrowShape) line;
                // 根据箭头的方向重新计算结束点
                Point2D newEndPoint = calculateConnectionPointForArrow(arrow);
                if (newEndPoint != null) {
                    // 将绝对坐标转换为相对于线形画布的坐标
                    Point2D relativeEndPoint = convertToLineRelativeCoordinate(arrow, newEndPoint);
                    arrow.setEndPoint(relativeEndPoint);
                }
            }
        }
    }

    /**
     * 为箭头计算连接点
     * 根据箭头的当前端点位置，找到形状上最近的吸附点
     * 
     * @param arrow 箭头形状
     * @return 连接点的绝对坐标
     */
    protected Point2D calculateConnectionPointForArrow(ArrowShape arrow) {
        // 获取箭头的起始点和结束点（绝对坐标）
        Point2D arrowStart = arrow.localToParent(arrow.getStartPoint());
        Point2D arrowEnd = arrow.localToParent(arrow.getEndPoint());

        // 根据箭头的连接关系确定要更新的端点
        Point2D targetPoint;
        if (LineStart.contains(arrow)) {
            // 如果箭头从此形状开始，使用箭头的起始点
            targetPoint = arrowStart;
        } else {
            // 如果箭头连接到此形状，使用箭头的结束点
            targetPoint = arrowEnd;
        } // 将目标点转换为此形状的本地坐标
        Point2D localPoint = this.parentToLocal(targetPoint);

        // 获取所有可吸附点，找到最近的点
        List<Point2D> snapPoints = getAllSnapPoints();
        Point2D nearestSnapPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point2D snapPoint : snapPoints) {
            double distance = localPoint.distance(snapPoint);
            if (distance < minDistance) {
                minDistance = distance;
                nearestSnapPoint = snapPoint;
            }
        }

        if (nearestSnapPoint != null) {
            // 将吸附点转换为绝对坐标
            return this.localToParent(nearestSnapPoint);
        }

        // 如果没有找到吸附点，返回原来的位置
        return targetPoint;
    }

    /**
     * 将绝对坐标转换为相对于线形画布的坐标
     * 
     * @param line          线形
     * @param absolutePoint 绝对坐标点
     * @return 相对于线形画布的坐标
     */
    protected Point2D convertToLineRelativeCoordinate(LineShape line, Point2D absolutePoint) {
        return line.parentToLocal(absolutePoint);
    }

    /**
     * 吸附目标结果类
     * 包含目标形状和吸附点信息
     */
    protected static class SnapTargetResult {
        public final BlockShape targetShape;
        public final Point2D snapPoint;

        public SnapTargetResult(BlockShape targetShape, Point2D snapPoint) {
            this.targetShape = targetShape;
            this.snapPoint = snapPoint;
        }
    }

    /**
     * 重写移除连接箭头的方法
     * 移除所有连接到此BlockShape的箭头
     */
    @Override
    protected void removeConnectedArrows() {
        // 获取父容器用于移除箭头
        Pane container = ArrowCreationManager.getShapeContainer(this);

        // 创建拷贝以避免并发修改异常
        Set<LineShape> startLinesCopy = new HashSet<>(LineStart);
        Set<LineShape> endLinesCopy = new HashSet<>(LineEnd);

        // 移除从此形状开始的箭头
        for (LineShape line : startLinesCopy) {
            if (line instanceof ArrowShape) {
                // 断开连接
                line.disconnectAll();
                // 从画布中移除
                if (container != null) {
                    container.getChildren().remove(line);
                }
                System.out.println("Removed arrow starting from this shape");
            }
        }

        // 移除连接到此形状的箭头
        for (LineShape line : endLinesCopy) {
            if (line instanceof ArrowShape) {
                // 断开连接
                line.disconnectAll();
                // 从画布中移除
                if (container != null) {
                    container.getChildren().remove(line);
                }
                System.out.println("Removed arrow ending at this shape");
            }
        }

        // 清空连接集合
        LineStart.clear();
        LineEnd.clear();
    }

    /**
     * 设置填充颜色
     * 
     * @param fillColor 填充颜色
     */
    public void setFillColor(Color fillColor) {
        if (fillColor != null) {
            this.fillColor = fillColor;
            // 重新绘制图形以应用新颜色
            draw();
        }
    }

    /**
     * 设置边框颜色
     * 
     * @param strokeColor 边框颜色
     */
    public void setStrokeColor(Color strokeColor) {
        if (strokeColor != null) {
            this.strokeColor = strokeColor;
            // 重新绘制图形以应用新颜色
            draw();
        }
    }

    /**
     * 重写创建形状特定的控制组件，添加颜色控制
     */
    @Override
    protected List<javafx.scene.Node> createShapeSpecificControls() {
        List<javafx.scene.Node> controls = new ArrayList<>();

        // 先添加父类的控制组件（位置控制）
        controls.addAll(super.createShapeSpecificControls());

        // 添加颜色控制组件
        controls.addAll(createColorControls());

        return controls;
    }

    /**
     * 创建颜色控制组件（填充色和边框色）
     */
    private List<javafx.scene.Node> createColorControls() {
        List<javafx.scene.Node> colorControls = new ArrayList<>();

        // 填充颜色控制
        javafx.scene.control.Label fillColorLabel = new javafx.scene.control.Label("填充色：");
        javafx.scene.control.ColorPicker fillColorPicker = new javafx.scene.control.ColorPicker();

        // 设置当前填充颜色
        if (fillColor != null && !fillColor.equals(Color.TRANSPARENT)) {
            fillColorPicker.setValue(fillColor);
        } else {
            fillColorPicker.setValue(Color.WHITE); // 默认白色
        }

        fillColorPicker.setOnAction(e -> {
            Color selectedColor = fillColorPicker.getValue();
            setFillColor(selectedColor);
        });

        // 边框颜色控制
        javafx.scene.control.Label strokeColorLabel = new javafx.scene.control.Label("边框色：");
        javafx.scene.control.ColorPicker strokeColorPicker = new javafx.scene.control.ColorPicker();

        // 设置当前边框颜色
        if (strokeColor != null && !strokeColor.equals(Color.TRANSPARENT)) {
            strokeColorPicker.setValue(strokeColor);
        } else {
            strokeColorPicker.setValue(Color.BLACK); // 默认黑色
        }

        strokeColorPicker.setOnAction(e -> {
            Color selectedColor = strokeColorPicker.getValue();
            setStrokeColor(selectedColor);
        });

        colorControls.add(fillColorLabel);
        colorControls.add(fillColorPicker);
        colorControls.add(strokeColorLabel);
        colorControls.add(strokeColorPicker);

        return colorControls;
    }

    /**
     * 更新可吸附点的视觉显示
     * 显示目标形状的所有可吸附点为红色圆点
     * 
     * @param snapResult 吸附目标结果
     * @param container  形状容器
     */
    private void updateSnapPointVisuals(SnapTargetResult snapResult, Pane container) {
        if (container == null || snapResult == null) {
            return;
        }

        // 清除之前的显示
        clearSnapPointVisuals(container);

        // 保存当前的吸附目标
        currentSnapTarget = snapResult;

        // 获取目标形状的所有可吸附点
        List<Point2D> snapPoints = snapResult.targetShape.getAllSnapPoints();

        // 为每个吸附点创建红色圆点指示器
        for (Point2D snapPoint : snapPoints) {
            // 将形状本地坐标转换为容器坐标
            Point2D containerPoint = snapResult.targetShape.localToParent(snapPoint);

            // 创建红色圆点
            javafx.scene.shape.Circle indicator = new javafx.scene.shape.Circle();
            indicator.setCenterX(containerPoint.getX());
            indicator.setCenterY(containerPoint.getY());
            indicator.setRadius(4.0); // 圆点半径
            indicator.setFill(Color.RED);
            indicator.setStroke(Color.DARKRED);
            indicator.setStrokeWidth(1.0);

            // 设置为不可交互，避免干扰其他操作
            indicator.setMouseTransparent(true);

            // 添加到容器中
            container.getChildren().add(indicator);
            snapPointIndicators.add(indicator);
        }
    }

    /**
     * 清除所有可吸附点的视觉显示
     * 
     * @param container 形状容器
     */
    private void clearSnapPointVisuals(Pane container) {
        if (container == null) {
            return;
        }

        // 从容器中移除所有指示器
        for (javafx.scene.shape.Circle indicator : snapPointIndicators) {
            container.getChildren().remove(indicator);
        }

        // 清空指示器列表
        snapPointIndicators.clear();

        // 清除当前吸附目标
        currentSnapTarget = null;
    }
}
