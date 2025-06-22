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
    private static final double CONTROL_POINT_SIZE = 6.0;
    
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
     * 根据两个点创建箭头的构造函数
     * 会自动计算合适的canvas大小
     * 
     * @param startPoint 起始点（绝对坐标）
     * @param endPoint   结束点（绝对坐标）
     */
    public ArrowShape(Point2D startPoint, Point2D endPoint) {
        // 先计算所需的canvas大小，然后调用父类构造函数
        super(calculateCanvasDimensions(startPoint, endPoint)[0], 
              calculateCanvasDimensions(startPoint, endPoint)[1]);
        
        // 重新计算相关数据
        double[] boundingBox = ShapeGeometryUtils.calculateBoundingBox(startPoint, endPoint);
        double padding = 20;
        double[] canvasPosition = ShapeGeometryUtils.calculateCanvasPosition(boundingBox, padding);
        
        // 转换为相对坐标
        Point2D relativeStart = ShapeGeometryUtils.toRelativeCoordinate(startPoint, canvasPosition);
        Point2D relativeEnd = ShapeGeometryUtils.toRelativeCoordinate(endPoint, canvasPosition);
        
        // 设置起始点和结束点
        this.startPoint = relativeStart;
        this.endPoint = relativeEnd;
        
        // 设置位置
        setLayoutX(canvasPosition[0]);
        setLayoutY(canvasPosition[1]);
        
        // 重新绘制以应用指定的点
        draw();
    }
    
    /**
     * 计算canvas尺寸的辅助方法
     * 
     * @param startPoint 起始点
     * @param endPoint   结束点
     * @return [width, height]数组
     */
    private static double[] calculateCanvasDimensions(Point2D startPoint, Point2D endPoint) {
        double[] boundingBox = ShapeGeometryUtils.calculateBoundingBox(startPoint, endPoint);
        double padding = 20;
        return ShapeGeometryUtils.calculateCanvasSize(boundingBox, padding, 60, 40);
    }
    
    /**
     * 初始化默认的起始点和结束点
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    private void initializePoints(double width, double height) {
        // 设置默认的起始点和结束点（相对坐标）
        double defaultStartX = 20;  // 左边距20像素
        double defaultStartY = height / 2;  // 垂直居中
        double defaultEndX = width - 20;  // 右边距20像素
        double defaultEndY = height / 2;  // 垂直居中
        
        this.startPoint = new Point2D(defaultStartX, defaultStartY);
        this.endPoint = new Point2D(defaultEndX, defaultEndY);
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
        if (distToStart <= CONTROL_POINT_SIZE / 2 + 2) {
            return ArrowControlPoint.START_POINT;
        }

        // 检查结束点控制点
        double distToEnd = Math.sqrt(Math.pow(x - actualEndX, 2) + Math.pow(y - actualEndY, 2));
        if (distToEnd <= CONTROL_POINT_SIZE / 2 + 2) {
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

        // 设置控制点样式 - 与其他图形保持一致
        gc.setFill(Color.WHITE);  // 白色填充
        gc.setStroke(Color.BLUE); // 蓝色边框
        gc.setLineWidth(1);       // 边框宽度为1

        // 绘制起始点控制点 - 使用矩形而不是圆形，与其他图形一致
        gc.fillRect(actualStartX - CONTROL_POINT_SIZE / 2, actualStartY - CONTROL_POINT_SIZE / 2, 
                   CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
        gc.strokeRect(actualStartX - CONTROL_POINT_SIZE / 2, actualStartY - CONTROL_POINT_SIZE / 2, 
                     CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);

        // 绘制结束点控制点 - 使用矩形而不是圆形，与其他图形一致
        gc.fillRect(actualEndX - CONTROL_POINT_SIZE / 2, actualEndY - CONTROL_POINT_SIZE / 2, 
                   CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
        gc.strokeRect(actualEndX - CONTROL_POINT_SIZE / 2, actualEndY - CONTROL_POINT_SIZE / 2, 
                     CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
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
            // 在箭头控制点上时使用缩放光标
            if (controlPoint == ArrowControlPoint.START_POINT) {
                setCursor(Cursor.CROSSHAIR);  
            } else if (controlPoint == ArrowControlPoint.END_POINT) {
                setCursor(Cursor.CROSSHAIR); 
            }
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
            
            // 释放后重新检查鼠标位置，设置合适的光标
            ArrowControlPoint controlPoint = getArrowControlPointAt(event.getX(), event.getY());
            if (controlPoint != null) {
                if (controlPoint == ArrowControlPoint.START_POINT) {
                    setCursor(Cursor.MOVE);
                } else if (controlPoint == ArrowControlPoint.END_POINT) {
                    setCursor(Cursor.CROSSHAIR);
                }
            } else {
                setCursor(Cursor.HAND);
            }
            
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
        
        // 在拖拽过程中保持相应的光标样式
        if (activeArrowControlPoint == ArrowControlPoint.START_POINT) {
            setCursor(Cursor.MOVE);
        } else if (activeArrowControlPoint == ArrowControlPoint.END_POINT) {
            setCursor(Cursor.CROSSHAIR);
        }
        
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
     * 根据起始点和结束点直接计算所需的canvas宽高
     */
    private void adjustCanvasSizeToFitArrow() {
        if (startPoint == null || endPoint == null) return;

        // 使用工具类计算边界框
        double[] boundingBox = ShapeGeometryUtils.calculateBoundingBox(startPoint, endPoint);
        
        // 计算所需的canvas尺寸
        double padding = 20;
        double[] canvasSize = ShapeGeometryUtils.calculateCanvasSize(boundingBox, padding, 60, 40);
        double requiredWidth = canvasSize[0];
        double requiredHeight = canvasSize[1];

        // 计算两点连线的中心点
        Point2D lineCenter = ShapeGeometryUtils.calculateLineCenter(startPoint, endPoint);

        // 计算新的canvas位置，使canvas中心与两点连线中心对齐
        double newLayoutX = getLayoutX() + lineCenter.getX() - requiredWidth / 2.0;
        double newLayoutY = getLayoutY() + lineCenter.getY() - requiredHeight / 2.0;

        // 计算起始点和结束点在新canvas中的坐标
        double newStartX = startPoint.getX() - lineCenter.getX() + requiredWidth / 2.0;
        double newStartY = startPoint.getY() - lineCenter.getY() + requiredHeight / 2.0;
        double newEndX = endPoint.getX() - lineCenter.getX() + requiredWidth / 2.0;
        double newEndY = endPoint.getY() - lineCenter.getY() + requiredHeight / 2.0;

        // 更新canvas位置和大小
        setLayoutX(newLayoutX);
        setLayoutY(newLayoutY);
        setShapeWidth(requiredWidth);
        setShapeHeight(requiredHeight);

        // 更新起始点和结束点坐标（相对于新的canvas）
        startPoint = new Point2D(newStartX, newStartY);
        endPoint = new Point2D(newEndX, newEndY);

        System.out.println("[AdjustCanvas] Canvas size calculated from two points using utility methods");
        System.out.println("[AdjustCanvas] Required size: " + requiredWidth + "x" + requiredHeight);
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
