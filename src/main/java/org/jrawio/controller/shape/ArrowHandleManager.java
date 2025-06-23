package org.jrawio.controller.shape;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 箭头控制点管理器
 * 负责箭头控制点的绘制、检测和管理
 */
public class ArrowHandleManager {
    
    /** 箭头控制点大小 */
    private static final double ARROW_HANDLE_SIZE = 8;
    
    /** 箭头控制点与缩放框的距离 */
    private static final double ARROW_HANDLE_OFFSET = 15;
    
    /**
     * 箭头控制点枚举
     */
    public enum ArrowHandle {
        ARROW_TOP,      // 上方箭头控制点
        ARROW_BOTTOM,   // 下方箭头控制点
        ARROW_LEFT,     // 左侧箭头控制点
        ARROW_RIGHT     // 右侧箭头控制点
    }
    
    /**
     * 绘制箭头控制点
     * @param gc 图形上下文
     * @param shapeX 图形X坐标
     * @param shapeY 图形Y坐标
     * @param shapeWidth 图形宽度
     * @param shapeHeight 图形高度
     */
    public static void drawArrowHandles(GraphicsContext gc, double shapeX, double shapeY, 
                                       double shapeWidth, double shapeHeight) {
        gc.setFill(Color.LIGHTGREEN);
        gc.setStroke(Color.DARKGREEN);
        gc.setLineWidth(2);

        // 计算箭头控制点位置
        double[][] arrowPositions = calculateArrowHandlePositions(
                shapeX, shapeY, shapeWidth, shapeHeight);        // 绘制每个箭头控制点（圆形）
        for (double[] pos : arrowPositions) {
            gc.fillOval(pos[0], pos[1], ARROW_HANDLE_SIZE, ARROW_HANDLE_SIZE);
            gc.strokeOval(pos[0], pos[1], ARROW_HANDLE_SIZE, ARROW_HANDLE_SIZE);
        }
    }
    
    /**
     * 计算箭头控制点的位置
     * @param shapeX 图形X坐标
     * @param shapeY 图形Y坐标
     * @param shapeWidth 图形宽度
     * @param shapeHeight 图形高度
     * @return 包含四个箭头控制点位置的二维数组，每个元素为[x, y]坐标
     */
    public static double[][] calculateArrowHandlePositions(double shapeX, double shapeY, 
                                                          double shapeWidth, double shapeHeight) {
        double halfHandle = ARROW_HANDLE_SIZE / 2;
        
        // 四个箭头控制点的位置（位于缩放框外围）
        return new double[][] {
            // ARROW_TOP - 上方中心，向上偏移
            { shapeX + shapeWidth / 2 - halfHandle, shapeY - ARROW_HANDLE_OFFSET - halfHandle },
            // ARROW_BOTTOM - 下方中心，向下偏移
            { shapeX + shapeWidth / 2 - halfHandle, shapeY + shapeHeight + ARROW_HANDLE_OFFSET - halfHandle },
            // ARROW_LEFT - 左侧中心，向左偏移
            { shapeX - ARROW_HANDLE_OFFSET - halfHandle, shapeY + shapeHeight / 2 - halfHandle },
            // ARROW_RIGHT - 右侧中心，向右偏移
            { shapeX + shapeWidth + ARROW_HANDLE_OFFSET - halfHandle, shapeY + shapeHeight / 2 - halfHandle }
        };
    }
    
    /**
     * 检测鼠标位置是否在某个箭头控制点上
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @param padding 内边距
     * @return 箭头控制点类型，如果不在控制点上则返回null
     */
    public static ArrowHandle getArrowHandleAt(double mouseX, double mouseY, 
                                              double canvasWidth, double canvasHeight, 
                                              double padding) {
        // 使用工具类计算绘制区域
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(canvasWidth, canvasHeight, padding);
        double shapeX = drawingArea[0];
        double shapeY = drawingArea[1];
        double shapeWidth = drawingArea[2];
        double shapeHeight = drawingArea[3];

        // 计算箭头控制点位置
        double[][] arrowPositions = calculateArrowHandlePositions(
                shapeX, shapeY, shapeWidth, shapeHeight);

        // 箭头控制点类型数组，与位置数组对应
        ArrowHandle[] arrowTypes = {
                ArrowHandle.ARROW_TOP, ArrowHandle.ARROW_BOTTOM,
                ArrowHandle.ARROW_LEFT, ArrowHandle.ARROW_RIGHT
        };

        // 检测每个箭头控制点
        for (int i = 0; i < arrowPositions.length; i++) {
            if (ShapeGeometryUtils.isPointInHandle(mouseX, mouseY, 
                    arrowPositions[i][0], arrowPositions[i][1], ARROW_HANDLE_SIZE)) {
                return arrowTypes[i];
            }
        }

        return null;
    }
    
    /**
     * 根据箭头控制点获取对应的鼠标光标
     * @param handle 箭头控制点类型
     * @return 对应的光标类型
     */
    public static Cursor getCursorForArrowHandle(ArrowHandle handle) {
        // 对于箭头控制点，使用十字光标表示可以创建箭头
        return Cursor.CROSSHAIR;
    }
    
    /**
     * 获取箭头控制点大小
     * @return 箭头控制点大小
     */
    public static double getArrowHandleSize() {
        return ARROW_HANDLE_SIZE;
    }
    
    /**
     * 获取箭头控制点偏移距离
     * @return 箭头控制点偏移距离
     */
    public static double getArrowHandleOffset() {
        return ARROW_HANDLE_OFFSET;
    }
}
