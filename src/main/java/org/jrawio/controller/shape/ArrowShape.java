package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import org.jrawio.controller.components.RightPanel;

/**
 * 箭头形状类
 * 包含起始点和结束点属性，绘制从起始点指向结束点的箭头
 */
public class ArrowShape extends Shape {
    
    /** 起始点坐标（相对于Shape的坐标系） */
    private Point2D startPoint;
    
    /** 结束点坐标（相对于Shape的坐标系） */
    private Point2D endPoint;
    
    /** 箭头头部的长度 */
    private static final double ARROW_HEAD_LENGTH = 5.0;
    
    /** 箭头头部的角度（弧度） */
    private static final double ARROW_HEAD_ANGLE = Math.PI / 6; // 30度
    
    /** 控制点半径 */
    private static final double CONTROL_POINT_RADIUS = 4.0;
    
    /** 箭头控制点类型 */
    public enum ArrowControlPoint {
        START_POINT, // 起始点控制点
        END_POINT    // 结束点控制点
    }
    
    /** 当前活动的箭头控制点 */
    private ArrowControlPoint activeArrowControlPoint = null;
    
    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public ArrowShape(double width, double height) {
        super(width, height);
        // 默认箭头从左上角指向右下角
        initializePoints(width, height);
        draw(); // 重新绘制以应用初始点
    }
    
    /**
     * 构造函数 - 指定起始点和结束点
     * 
     * @param width      图形宽度
     * @param height     图形高度
     * @param startPoint 起始点
     * @param endPoint   结束点
     */
    public ArrowShape(double width, double height, Point2D startPoint, Point2D endPoint) {
        super(width, height);
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        draw(); // 重新绘制以应用指定的点
    }
    
    /**
     * 初始化默认的起始点和结束点
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    private void initializePoints(double width, double height) {
        // 让箭头从左侧中心指向右侧中心，这样看起来更美观
        this.startPoint = new Point2D(width * 0.15, height * 0.5);
        this.endPoint = new Point2D(width * 0.85, height * 0.5);
    }
    
    /**
     * 绘制箭头
     * 
     * @param gc     图形上下文
     * @param x      绘制起始x坐标
     * @param y      绘制起始y坐标
     * @param width  绘制宽度
     * @param height 绘制高度
     */
    @Override
    public void drawShape(GraphicsContext gc, double x, double y, double width, double height) {
        // 如果起始点或结束点为空，初始化默认值
        if (startPoint == null || endPoint == null) {
            initializePoints(getWidth(), getHeight());
        }
        
        // 设置绘制属性
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.setFill(Color.BLACK);
        
        // 计算实际的起始点和结束点坐标
        double actualStartX = x + (startPoint.getX() / getWidth()) * width;
        double actualStartY = y + (startPoint.getY() / getHeight()) * height;
        double actualEndX = x + (endPoint.getX() / getWidth()) * width;
        double actualEndY = y + (endPoint.getY() / getHeight()) * height;
        
        // 绘制箭头主线
        gc.strokeLine(actualStartX, actualStartY, actualEndX, actualEndY);
        
        // 计算箭头头部
        drawArrowHead(gc, actualStartX, actualStartY, actualEndX, actualEndY);
    }
    
    /**
     * 处理鼠标按下事件
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handlePressed(MouseEvent event) {
        System.out.println("[handlePressed]" + this.toString());
        this.toFront();
        stateMachine.prepareForInteraction(event.getSceneX(), event.getSceneY());

        // 检查是否点击在箭头控制点上
        if (selected) {
            ArrowControlPoint controlPoint = getArrowControlPointAt(event.getX(), event.getY());
            if (controlPoint != null) {
                System.out.println("[ArrowPressed] Control point detected: " + controlPoint);
                stateMachine.toResizing(null, event.getSceneX(), event.getSceneY(),
                        getWidth(), getHeight(), getLayoutX(), getLayoutY());
                // 保存当前编辑的控制点
                activeArrowControlPoint = controlPoint;
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
     * 绘制箭头头部
     * 
     * @param gc      图形上下文
     * @param startX  起始点X坐标
     * @param startY  起始点Y坐标
     * @param endX    结束点X坐标
     * @param endY    结束点Y坐标
     */
    private void drawArrowHead(GraphicsContext gc, double startX, double startY, double endX, double endY) {
        // 计算箭头方向角度
        double angle = Math.atan2(endY - startY, endX - startX);
        
        // 计算箭头头部的两个端点
        double arrowX1 = endX - ARROW_HEAD_LENGTH * Math.cos(angle - ARROW_HEAD_ANGLE);
        double arrowY1 = endY - ARROW_HEAD_LENGTH * Math.sin(angle - ARROW_HEAD_ANGLE);
        
        double arrowX2 = endX - ARROW_HEAD_LENGTH * Math.cos(angle + ARROW_HEAD_ANGLE);
        double arrowY2 = endY - ARROW_HEAD_LENGTH * Math.sin(angle + ARROW_HEAD_ANGLE);
        
        // 绘制箭头头部（三角形）
        gc.strokeLine(endX, endY, arrowX1, arrowY1);
        gc.strokeLine(endX, endY, arrowX2, arrowY2);
        
        // 可选：填充箭头头部
        double[] xPoints = {endX, arrowX1, arrowX2};
        double[] yPoints = {endY, arrowY1, arrowY2};
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    /**
     * 检测鼠标位置是否在某个箭头控制点上
     * 
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @return 控制点类型，如果不在控制点上则返回null
     */
    private ArrowControlPoint getArrowControlPointAt(double x, double y) {
        if (!selected) return null;

        double padding = 4;
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(getWidth(), getHeight(), padding);
        double drawX = drawingArea[0];
        double drawY = drawingArea[1];
        double drawWidth = drawingArea[2];
        double drawHeight = drawingArea[3];

        // 计算实际的起始点和结束点坐标
        double actualStartX = drawX + (startPoint.getX() / getWidth()) * drawWidth;
        double actualStartY = drawY + (startPoint.getY() / getHeight()) * drawHeight;
        double actualEndX = drawX + (endPoint.getX() / getWidth()) * drawWidth;
        double actualEndY = drawY + (endPoint.getY() / getHeight()) * drawHeight;

        // 检查起始点控制点
        double distToStart = Math.sqrt(Math.pow(x - actualStartX, 2) + Math.pow(y - actualStartY, 2));
        if (distToStart <= CONTROL_POINT_RADIUS + 2) {
            return ArrowControlPoint.START_POINT;
        }

        // 检查结束点控制点
        double distToEnd = Math.sqrt(Math.pow(x - actualEndX, 2) + Math.pow(y - actualEndY, 2));
        if (distToEnd <= CONTROL_POINT_RADIUS + 2) {
            return ArrowControlPoint.END_POINT;
        }

        return null;
    }

    /**
     * 绘制箭头控制点
     */
    private void drawArrowControlPoints(GraphicsContext gc, double drawX, double drawY, double drawWidth, double drawHeight) {
        // 只有在被选中时才绘制控制点
        if (!selected) return;
        
        // 计算实际的起始点和结束点坐标
        double actualStartX = drawX + (startPoint.getX() / getWidth()) * drawWidth;
        double actualStartY = drawY + (startPoint.getY() / getHeight()) * drawHeight;
        double actualEndX = drawX + (endPoint.getX() / getWidth()) * drawWidth;
        double actualEndY = drawY + (endPoint.getY() / getHeight()) * drawHeight;

        // 设置控制点样式
        gc.setFill(Color.BLUE);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);

        // 绘制起始点控制点
        gc.fillOval(actualStartX - CONTROL_POINT_RADIUS, actualStartY - CONTROL_POINT_RADIUS, 
                   CONTROL_POINT_RADIUS * 2, CONTROL_POINT_RADIUS * 2);
        gc.strokeOval(actualStartX - CONTROL_POINT_RADIUS, actualStartY - CONTROL_POINT_RADIUS, 
                     CONTROL_POINT_RADIUS * 2, CONTROL_POINT_RADIUS * 2);

        // 绘制结束点控制点
        gc.fillOval(actualEndX - CONTROL_POINT_RADIUS, actualEndY - CONTROL_POINT_RADIUS, 
                   CONTROL_POINT_RADIUS * 2, CONTROL_POINT_RADIUS * 2);
        gc.strokeOval(actualEndX - CONTROL_POINT_RADIUS, actualEndY - CONTROL_POINT_RADIUS, 
                     CONTROL_POINT_RADIUS * 2, CONTROL_POINT_RADIUS * 2);
    }
    
    /**
     * 获取起始点
     * 
     * @return 起始点坐标
     */
    public Point2D getStartPoint() {
        return startPoint;
    }
    
    /**
     * 设置起始点
     * 
     * @param startPoint 起始点坐标
     */
    public void setStartPoint(Point2D startPoint) {
        this.startPoint = startPoint;
        draw();
    }
    
    /**
     * 获取结束点
     * 
     * @return 结束点坐标
     */
    public Point2D getEndPoint() {
        return endPoint;
    }
    
    /**
     * 设置结束点
     * 
     * @param endPoint 结束点坐标
     */
    public void setEndPoint(Point2D endPoint) {
        this.endPoint = endPoint;
        draw();
    }
    
    /**
     * 设置箭头的起始点和结束点
     * 
     * @param startPoint 起始点坐标
     * @param endPoint   结束点坐标
     */
    public void setArrowPoints(Point2D startPoint, Point2D endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        draw();
    }
    
    /**
     * 绘制图形到画布
     */
    @Override
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

        // 如果选中，绘制箭头控制点
        if (selected) {
            drawArrowControlPoints(gc, x, y, shapeWidth, shapeHeight);
            // 绘制箭头线段中心点用于调试
            drawArrowLineCenter(gc, x, y, shapeWidth, shapeHeight);
        }

        // 绘制canvas中心点用于调试
        drawCanvasCenter(gc);

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
     * 绘制canvas中心点用于调试
     * 在canvas中心绘制一个红色十字标记
     */
    private void drawCanvasCenter(GraphicsContext gc) {
        // 计算canvas中心点
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;

        // 保存当前绘制状态
        Color originalStroke = (Color) gc.getStroke();
        double originalLineWidth = gc.getLineWidth();

        // 设置调试绘制属性
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);

        // 绘制十字标记
        double crossSize = 6;
        gc.strokeLine(centerX - crossSize, centerY, centerX + crossSize, centerY); // 水平线
        gc.strokeLine(centerX, centerY - crossSize, centerX, centerY + crossSize); // 垂直线

        // 绘制中心点圆圈
        double circleRadius = 2;
        gc.strokeOval(centerX - circleRadius, centerY - circleRadius, 
                     circleRadius * 2, circleRadius * 2);

        // 恢复原始绘制状态
        gc.setStroke(originalStroke);
        gc.setLineWidth(originalLineWidth);

        System.out.println("[DebugCenter] Canvas size: " + getWidth() + "x" + getHeight() + 
                          ", Center at: (" + centerX + "," + centerY + ")");
    }

    /**
     * 绘制箭头线段中心点用于调试
     * 在两点连线的中心绘制一个绿色十字标记
     */
    private void drawArrowLineCenter(GraphicsContext gc, double drawX, double drawY, double drawWidth, double drawHeight) {
        if (startPoint == null || endPoint == null) return;

        // 计算实际的起始点和结束点坐标
        double actualStartX = drawX + (startPoint.getX() / getWidth()) * drawWidth;
        double actualStartY = drawY + (startPoint.getY() / getHeight()) * drawHeight;
        double actualEndX = drawX + (endPoint.getX() / getWidth()) * drawWidth;
        double actualEndY = drawY + (endPoint.getY() / getHeight()) * drawHeight;

        // 计算箭头线段的中心点
        double lineCenterX = (actualStartX + actualEndX) / 2.0;
        double lineCenterY = (actualStartY + actualEndY) / 2.0;

        // 保存当前绘制状态
        Color originalStroke = (Color) gc.getStroke();
        double originalLineWidth = gc.getLineWidth();

        // 设置调试绘制属性
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(1);

        // 绘制十字标记
        double crossSize = 8;
        gc.strokeLine(lineCenterX - crossSize, lineCenterY, lineCenterX + crossSize, lineCenterY); // 水平线
        gc.strokeLine(lineCenterX, lineCenterY - crossSize, lineCenterX, lineCenterY + crossSize); // 垂直线

        // 绘制中心点圆圈
        double circleRadius = 3;
        gc.strokeOval(lineCenterX - circleRadius, lineCenterY - circleRadius, 
                     circleRadius * 2, circleRadius * 2);

        // 恢复原始绘制状态
        gc.setStroke(originalStroke);
        gc.setLineWidth(originalLineWidth);

        System.out.println("[DebugLineCenter] Arrow line center at: (" + lineCenterX + "," + lineCenterY + ")");
    }

    /**
     * 处理鼠标移动事件 - 更新光标
     */
    @Override
    protected void handleMouseMoved(MouseEvent event) {
        if (!selected) {
            setCursor(Cursor.HAND);
            return;
        }

        ArrowControlPoint controlPoint = getArrowControlPointAt(event.getX(), event.getY());
        if (controlPoint != null) {
            setCursor(Cursor.HAND);
        } else {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 重写getResizeHandleAt方法，让它返回null，因为箭头不使用标准的缩放控制点
     */
    @Override
    protected ResizeHandleManager.ResizeHandle getResizeHandleAt(double x, double y) {
        // 箭头形状不使用标准的缩放控制点
        return null;
    }

    /**
     * 处理鼠标释放事件
     */
    @Override
    protected void handleMouseReleased(MouseEvent event) {
        if (activeArrowControlPoint != null) {
            activeArrowControlPoint = null;
            setCursor(Cursor.DEFAULT);
            // 通知右侧面板更新
            RightPanel rightPanel = RightPanel.getInstance();
            if (rightPanel != null) {
                rightPanel.onShapeSelectionChanged(selectedShapes);
            }
        } else {
            super.handleMouseReleased(event);
        }
        event.consume();
    }

    /**
     * 处理鼠标拖拽事件
     * 
     * @param event 鼠标事件
     */
    @Override
    protected void handleDragged(MouseEvent event) {
        System.out.println("[ArrowDragged] activeArrowControlPoint: " + activeArrowControlPoint);
        
        if (activeArrowControlPoint != null) {
            // 处理箭头控制点的拖拽
            handleArrowControlPointDrag(event);
        } else {
            // 如果不是控制点拖拽，使用默认的拖拽逻辑（移动整个箭头）
            super.handleDragged(event);
        }
        event.consume();
    }

    /**
     * 处理箭头控制点的拖拽
     */
    private void handleArrowControlPointDrag(MouseEvent event) {
        System.out.println("[ArrowControlPointDrag] Moving " + activeArrowControlPoint);
        
        // 将鼠标位置转换为相对于箭头形状的本地坐标
        double localX = event.getX();
        double localY = event.getY();

        // 移除坐标范围限制，允许箭头超出当前canvas大小
        System.out.println("[ArrowControlPointDrag] New local coordinates: (" + localX + ", " + localY + ")");

        // 根据活动的控制点类型更新相应的点
        if (activeArrowControlPoint == ArrowControlPoint.START_POINT) {
            startPoint = new Point2D(localX, localY);
            System.out.println("[ArrowControlPointDrag] Updated startPoint: " + startPoint);
        } else if (activeArrowControlPoint == ArrowControlPoint.END_POINT) {
            endPoint = new Point2D(localX, localY);
            System.out.println("[ArrowControlPointDrag] Updated endPoint: " + endPoint);
        }

        // 调整canvas大小以适应新的箭头范围
        adjustCanvasSizeToFitArrow();

        // 重新绘制
        draw();

        // 更新文本框位置
        if (textField != null) {
            textField.setLayoutX(getLayoutX() + 4);
            textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
        }
    }

    /**
     * 调整canvas大小以适应箭头的范围
     * Canvas大小完全由起始点和终点直接确定，canvas中心是两点连线的中心
     */
    private void adjustCanvasSizeToFitArrow() {
        if (startPoint == null || endPoint == null) return;

        // 计算两点连线的中心点
        double centerX = (startPoint.getX() + endPoint.getX()) / 2.0;
        double centerY = (startPoint.getY() + endPoint.getY()) / 2.0;

        // 计算箭头的边界框
        double minX = Math.min(startPoint.getX(), endPoint.getX());
        double minY = Math.min(startPoint.getY(), endPoint.getY());
        double maxX = Math.max(startPoint.getX(), endPoint.getX());
        double maxY = Math.max(startPoint.getY(), endPoint.getY());

        // 添加边距以确保箭头头部和控制点有足够空间
        double padding = 20;
        double arrowWidth = (maxX - minX) + 2 * padding;
        double arrowHeight = (maxY - minY) + 2 * padding;

        // 确保最小尺寸
        arrowWidth = Math.max(arrowWidth, 40);
        arrowHeight = Math.max(arrowHeight, 40);

        // 计算新的canvas位置，使canvas中心与两点连线中心对齐
        double newLayoutX = getLayoutX() + centerX - arrowWidth / 2.0;
        double newLayoutY = getLayoutY() + centerY - arrowHeight / 2.0;

        // 计算起始点和结束点在新canvas中的坐标
        double newStartX = startPoint.getX() - centerX + arrowWidth / 2.0;
        double newStartY = startPoint.getY() - centerY + arrowHeight / 2.0;
        double newEndX = endPoint.getX() - centerX + arrowWidth / 2.0;
        double newEndY = endPoint.getY() - centerY + arrowHeight / 2.0;

        // 更新位置和大小
        setLayoutX(newLayoutX);
        setLayoutY(newLayoutY);
        setShapeWidth(arrowWidth);
        setShapeHeight(arrowHeight);

        // 更新起始点和结束点坐标（相对于新的canvas）
        startPoint = new Point2D(newStartX, newStartY);
        endPoint = new Point2D(newEndX, newEndY);

        System.out.println("[AdjustCanvas] Canvas centered on arrow line center");
        System.out.println("[AdjustCanvas] Arrow line center: (" + centerX + "," + centerY + ")");
        System.out.println("[AdjustCanvas] New canvas size: " + arrowWidth + "x" + arrowHeight);
        System.out.println("[AdjustCanvas] New canvas position: (" + newLayoutX + "," + newLayoutY + ")");
        System.out.println("[AdjustCanvas] New StartPoint: " + startPoint + ", EndPoint: " + endPoint);
    }

    @Override
    public String toString() {
        return "ArrowShape{" +
                "startPoint=" + startPoint +
                ", endPoint=" + endPoint +
                ", width=" + getWidth() +
                ", height=" + getHeight() +
                ", selected=" + selected +
                '}';
    }
}
