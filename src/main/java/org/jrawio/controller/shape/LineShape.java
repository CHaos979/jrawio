package org.jrawio.controller.shape;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import org.jrawio.controller.components.RightPanel;

/**
 * 线形基类
 * 包含起始点和结束点属性，以及拖拽缩放的通用逻辑
 */
public abstract class LineShape extends Shape {

    /** 起始点坐标（相对于Shape的坐标系） */
    protected Point2D startPoint;

    /** 结束点坐标（相对于Shape的坐标系） */
    protected Point2D endPoint;

    /** 控制点半径 */
    protected static final double CONTROL_POINT_SIZE = 6.0;
    private BlockShape start, end;

    /** 线条样式属性 */
    private Color lineColor = Color.BLACK; // 默认线条颜色为黑色
    private double lineWidth = 1.0; // 默认线条粗细为2

    /** 线形控制点类型 */
    public enum LineControlPoint {
        START_POINT, // 起始点控制点
        END_POINT // 结束点控制点
    }

    /** 当前活动的线形控制点 */
    protected LineControlPoint activeLineControlPoint = null;

    /**
     * 构造函数
     * 
     * @param width  图形宽度
     * @param height 图形高度
     */
    public LineShape(double width, double height) {
        super(width, height);
        // 默认线形从左边指向右边
        initializePoints(width, height);
        draw(); // 重新绘制以应用初始点
    }

    /**
     * 根据两个点创建线形的构造函数
     * 会自动计算合适的canvas大小
     * 
     * @param startPoint 起始点（绝对坐标）
     * @param endPoint   结束点（绝对坐标）
     */
    public LineShape(Point2D startPoint, Point2D endPoint) {
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
     * 拷贝构造方法
     * 创建一个与源LineShape具有相同属性的新LineShape实例
     * 
     * @param source 源LineShape对象
     */
    protected LineShape(LineShape source) {
        super(source);

        // 复制LineShape特有属性
        this.startPoint = new Point2D(source.startPoint.getX(), source.startPoint.getY());
        this.endPoint = new Point2D(source.endPoint.getX(), source.endPoint.getY());

        // 复制线条样式属性
        this.lineColor = source.lineColor;
        this.lineWidth = source.lineWidth;

        // 不复制连接状态，新对象应该没有连接
        this.start = null;
        this.end = null;

        // 不复制活动控制点状态
        this.activeLineControlPoint = null;

        // 重新绘制以应用复制的点
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
    protected void initializePoints(double width, double height) {
        // 设置默认的起始点和结束点（相对坐标）
        double defaultStartX = 20; // 左边距20像素
        double defaultStartY = height / 2; // 垂直居中
        double defaultEndX = width - 20; // 右边距20像素
        double defaultEndY = height / 2; // 垂直居中

        this.startPoint = new Point2D(defaultStartX, defaultStartY);
        this.endPoint = new Point2D(defaultEndX, defaultEndY);
    }

    /**
     * 重写控制点交互处理，检查线形控制点
     */
    @Override
    protected boolean handleControlPointInteraction(MouseEvent event) {
        LineControlPoint controlPoint = getLineControlPointAt(event.getX(), event.getY());
        if (controlPoint != null) {
            stateMachine.toResizing(null, event.getSceneX(), event.getSceneY(),
                    getWidth(), getHeight(), getLayoutX(), getLayoutY());
            // 保存当前编辑的控制点
            activeLineControlPoint = controlPoint;
            return true; // 已处理控制点交互
        }
        return false; // 没有控制点或没有处理
    }

    /**
     * 检测鼠标位置是否在某个线形控制点上
     * 
     * @param x 鼠标X坐标
     * @param y 鼠标Y坐标
     * @return 控制点类型，如果不在控制点上则返回null
     */
    protected LineControlPoint getLineControlPointAt(double x, double y) {
        if (!selected)
            return null;

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
            return LineControlPoint.START_POINT;
        }

        // 检查结束点控制点
        double distToEnd = Math.sqrt(Math.pow(x - actualEndX, 2) + Math.pow(y - actualEndY, 2));
        if (distToEnd <= CONTROL_POINT_SIZE / 2 + 2) {
            return LineControlPoint.END_POINT;
        }

        return null;
    }

    /**
     * 绘制线形控制点
     */
    protected void drawLineControlPoints(GraphicsContext gc, double drawX, double drawY, double drawWidth,
            double drawHeight) {
        // 只有在被选中时才绘制控制点
        if (!selected)
            return;

        // 计算实际的起始点和结束点坐标
        double actualStartX = drawX + (startPoint.getX() / getWidth()) * drawWidth;
        double actualStartY = drawY + (startPoint.getY() / getHeight()) * drawHeight;
        double actualEndX = drawX + (endPoint.getX() / getWidth()) * drawWidth;
        double actualEndY = drawY + (endPoint.getY() / getHeight()) * drawHeight;

        // 设置控制点样式 - 与其他图形保持一致
        gc.setFill(Color.WHITE); // 白色填充
        gc.setStroke(Color.BLUE); // 蓝色边框
        gc.setLineWidth(1); // 边框宽度为1

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
     * 处理鼠标移动事件 - 更新光标
     */
    @Override
    protected void handleMouseMoved(MouseEvent event) {
        if (!selected) {
            setCursor(Cursor.HAND);
            return;
        }

        LineControlPoint controlPoint = getLineControlPointAt(event.getX(), event.getY());
        if (controlPoint != null) {
            // 在线形控制点上时使用缩放光标
            if (controlPoint == LineControlPoint.START_POINT) {
                setCursor(Cursor.CROSSHAIR);
            } else if (controlPoint == LineControlPoint.END_POINT) {
                setCursor(Cursor.CROSSHAIR);
            }
        } else {
            setCursor(Cursor.HAND);
        }
    }

    /**
     * 重写特定释放处理，处理线形控制点释放
     */
    @Override
    protected boolean handleSpecificRelease(MouseEvent event) {
        if (activeLineControlPoint != null) {
            activeLineControlPoint = null;

            // 释放后重新检查鼠标位置，设置合适的光标
            LineControlPoint controlPoint = getLineControlPointAt(event.getX(), event.getY());
            if (controlPoint != null) {
                if (controlPoint == LineControlPoint.START_POINT) {
                    setCursor(Cursor.MOVE);
                } else if (controlPoint == LineControlPoint.END_POINT) {
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
            return true; // 已处理线形控制点释放
        }
        return false; // 没有线形控制点释放，使用标准释放
    }

    /**
     * 重写特定拖拽处理，优先处理线形控制点拖拽
     */
    @Override
    protected boolean handleSpecificDrag(MouseEvent event) {
        if (activeLineControlPoint != null) {
            handleLineControlPointDrag(event);
            return true; // 已处理线形控制点拖拽
        }
        return false; // 没有线形控制点拖拽，使用标准拖拽
    }

    /**
     * 重写拖拽开始前的处理
     * 当线形被拖拽时断开所有连接
     */
    @Override
    protected boolean onBeforeSelectionHandling(MouseEvent event) {
        // 如果不是在控制点上，且这是一个拖拽操作的开始，则断开所有连接
        if (selected && getLineControlPointAt(event.getX(), event.getY()) == null) {
            disconnectAll();
        }
        return false; // 继续执行标准的选择处理
    }

    /**
     * 处理线形控制点的拖拽
     */
    protected void handleLineControlPointDrag(MouseEvent event) {

        // 在拖拽过程中保持相应的光标样式
        if (activeLineControlPoint == LineControlPoint.START_POINT) {
            setCursor(Cursor.MOVE);
        } else if (activeLineControlPoint == LineControlPoint.END_POINT) {
            setCursor(Cursor.CROSSHAIR);
        }

        // 将鼠标位置转换为相对于线形的本地坐标
        double localX = event.getX();
        double localY = event.getY();

        // 移除坐标范围限制，允许线形超出当前canvas大小
        // 根据活动的控制点类型更新相应的点
        if (activeLineControlPoint == LineControlPoint.START_POINT) {
            startPoint = new Point2D(localX, localY);
        } else if (activeLineControlPoint == LineControlPoint.END_POINT) {
            endPoint = new Point2D(localX, localY);
        }

        // 调整canvas大小以适应新的线形范围
        adjustCanvasSizeToFitLine();

        // 重新绘制
        draw();

        // 更新文本框位置
        if (textField != null) {
            textField.setLayoutX(getLayoutX() + 4);
            textField.setLayoutY(getLayoutY() + getHeight() / 2 - 12);
        }
    }

    /**
     * 调整canvas大小以适应线形的范围
     * 根据起始点和结束点直接计算所需的canvas宽高
     */
    protected void adjustCanvasSizeToFitLine() {
        if (startPoint == null || endPoint == null)
            return;

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
        // 调整canvas大小以适应新的线形范围
        adjustCanvasSizeToFitLine();
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
        // 调整canvas大小以适应新的线形范围
        adjustCanvasSizeToFitLine();
        draw();
    }

    /**
     * 设置线形的起始点和结束点
     * 
     * @param startPoint 起始点坐标
     * @param endPoint   结束点坐标
     */
    public void setLinePoints(Point2D startPoint, Point2D endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        // 调整canvas大小以适应新的线形范围
        adjustCanvasSizeToFitLine();
        draw();
    }

    /**
     * 获取起始连接的形状
     * 
     * @return 起始连接的BlockShape，如果没有连接则返回null
     */
    public BlockShape getStartShape() {
        return start;
    }

    /**
     * 设置起始连接的形状
     * 
     * @param start 起始连接的BlockShape
     */
    public void setStartShape(BlockShape start) {
        // 先断开原有连接
        if (this.start != null) {
            this.start.removeLineStart(this);
        }

        // 建立新连接
        this.start = start;
        if (start != null) {
            start.addLineStart(this);
        }
    }

    /**
     * 获取结束连接的形状
     * 
     * @return 结束连接的BlockShape，如果没有连接则返回null
     */
    public BlockShape getEndShape() {
        return end;
    }

    /**
     * 设置结束连接的形状
     * 
     * @param end 结束连接的BlockShape
     */
    public void setEndShape(BlockShape end) {
        // 先断开原有连接
        if (this.end != null) {
            this.end.removeLineEnd(this);
        }

        // 建立新连接
        this.end = end;
        if (end != null) {
            end.addLineEnd(this);
        }
    }

    /**
     * 设置线形连接的起始和结束形状
     * 
     * @param startShape 起始连接的BlockShape
     * @param endShape   结束连接的BlockShape
     */
    public void setConnectedShapes(BlockShape startShape, BlockShape endShape) {
        setStartShape(startShape);
        setEndShape(endShape);
    }

    /**
     * 检查线形是否连接到指定形状
     * 
     * @param shape 要检查的形状
     * @return true如果线形连接到该形状，false如果没有连接
     */
    public boolean isConnectedTo(BlockShape shape) {
        return shape != null && (start == shape || end == shape);
    }

    /**
     * 断开与指定形状的连接
     * 
     * @param shape 要断开连接的形状
     */
    public void disconnectFrom(BlockShape shape) {
        if (start == shape) {
            if (start != null) {
                start.removeLineStart(this);
            }
            start = null;
        }
        if (end == shape) {
            if (end != null) {
                end.removeLineEnd(this);
            }
            end = null;
        }
    }

    /**
     * 断开所有连接
     */
    public void disconnectAll() {
        System.out.println("LineShape.disconnectAll() called - start: " + start + ", end: " + end);
        if (start != null) {
            start.removeLineStart(this);
            start = null;
        }
        if (end != null) {
            end.removeLineEnd(this);
            end = null;
        }
        System.out.println("LineShape.disconnectAll() completed - start: " + start + ", end: " + end);
    }

    /**
     * 绘制线形到画布
     */
    @Override
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        // 使用工具类计算绘制区域
        double padding = 4;
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(getWidth(), getHeight(), padding);
        double x = drawingArea[0];
        double y = drawingArea[1];
        double shapeWidth = drawingArea[2];
        double shapeHeight = drawingArea[3];

        // 设置线条样式属性
        gc.setStroke(lineColor != null ? lineColor : Color.BLACK);
        gc.setLineWidth(lineWidth);

        // 调用子类实现的图形绘制方法
        drawShape(gc, x, y, shapeWidth, shapeHeight);

        // 如果选中，绘制线形控制点
        if (selected) {
            drawLineControlPoints(gc, x, y, shapeWidth, shapeHeight);
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
        // drawDebugInfo(gc);
    }

    /**
     * 内部设置起始连接的形状（避免循环调用）
     * 
     * @param start 起始连接的BlockShape
     */
    protected void setStartShapeInternal(BlockShape start) {
        this.start = start;
    }

    /**
     * 内部设置结束连接的形状（避免循环调用）
     * 
     * @param end 结束连接的BlockShape
     */
    protected void setEndShapeInternal(BlockShape end) {
        this.end = end;
    }

    /**
     * 重写移除连接箭头的方法
     * LineShape删除时断开所有连接
     */
    @Override
    protected void removeConnectedArrows() {
        disconnectAll();
    }

    /**
     * 设置线条颜色
     * 
     * @param lineColor 线条颜色
     */
    public void setLineColor(Color lineColor) {
        if (lineColor != null) {
            this.lineColor = lineColor;
            // 重新绘制线条以应用新颜色
            draw();
        }
    }

    /**
     * 获取线条颜色
     * 
     * @return 线条颜色
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * 设置线条粗细
     * 
     * @param lineWidth 线条粗细
     */
    public void setLineWidth(double lineWidth) {
        if (lineWidth > 0) {
            this.lineWidth = lineWidth;
            // 重新绘制线条以应用新粗细
            draw();
        }
    }

    /**
     * 获取线条粗细
     * 
     * @return 线条粗细
     */
    public double getLineWidth() {
        return lineWidth;
    }

    /**
     * 重写创建形状特定的控制组件，添加线条样式控制
     */
    @Override
    protected java.util.List<javafx.scene.Node> createShapeSpecificControls() {
        java.util.List<javafx.scene.Node> controls = new java.util.ArrayList<>();

        // 先添加父类的控制组件（位置控制）
        controls.addAll(super.createShapeSpecificControls());

        // 添加线条样式控制组件
        controls.addAll(createLineStyleControls());

        return controls;
    }

    /**
     * 创建线条样式控制组件（线条颜色和粗细）
     */
    private java.util.List<javafx.scene.Node> createLineStyleControls() {
        java.util.List<javafx.scene.Node> styleControls = new java.util.ArrayList<>();

        // 线条颜色控制
        Label lineColorLabel = new Label("线条颜色：");
        ColorPicker lineColorPicker = new ColorPicker();

        // 设置当前线条颜色
        if (lineColor != null) {
            lineColorPicker.setValue(lineColor);
        } else {
            lineColorPicker.setValue(Color.BLACK); // 默认黑色
        }

        // 设置颜色选择器的事件处理
        lineColorPicker.setOnAction(e -> {
            Color selectedColor = lineColorPicker.getValue();
            setLineColor(selectedColor);
        });

        // 线条粗细控制
        Label lineWidthLabel = new Label("线条粗细：");
        Spinner<Double> lineWidthSpinner = new Spinner<>(0.5, 10.0, lineWidth, 0.5);
        lineWidthSpinner.setEditable(true);
        lineWidthSpinner.setPrefWidth(80);

        // 设置粗细调节器的事件处理
        lineWidthSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue > 0) {
                setLineWidth(newValue);
            }
        });

        // 添加控件到列表
        styleControls.add(lineColorLabel);
        styleControls.add(lineColorPicker);
        styleControls.add(lineWidthLabel);
        styleControls.add(lineWidthSpinner);

        return styleControls;
    }
}
