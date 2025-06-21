package org.jrawio.controller.shape;

import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * 缩放控制点管理器
 * 负责控制点的绘制、检测和光标管理
 */
public class ResizeHandleManager {
    
    /** 控制点大小 */
    private static final double HANDLE_SIZE = 6;
    
    /**
     * 缩放控制点枚举
     */
    public enum ResizeHandle {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }
    
    /**
     * 绘制缩放控制点
     * @param gc 图形上下文
     * @param shapeX 图形X坐标
     * @param shapeY 图形Y坐标
     * @param shapeWidth 图形宽度
     * @param shapeHeight 图形高度
     */
    public static void drawResizeHandles(GraphicsContext gc, double shapeX, double shapeY, 
                                       double shapeWidth, double shapeHeight) {
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
     * 检测鼠标位置是否在某个控制点上
     * @param mouseX 鼠标X坐标
     * @param mouseY 鼠标Y坐标
     * @param canvasWidth 画布宽度
     * @param canvasHeight 画布高度
     * @param padding 内边距
     * @return 控制点类型，如果不在控制点上则返回null
     */
    public static ResizeHandle getResizeHandleAt(double mouseX, double mouseY, 
                                               double canvasWidth, double canvasHeight, 
                                               double padding) {
        // 使用工具类计算绘制区域
        double[] drawingArea = ShapeGeometryUtils.calculateDrawingArea(canvasWidth, canvasHeight, padding);
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
            if (ShapeGeometryUtils.isPointInHandle(mouseX, mouseY, 
                    handlePositions[i][0], handlePositions[i][1], HANDLE_SIZE)) {
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
    public static Cursor getCursorForHandle(ResizeHandle handle) {
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
     * 计算缩放后的新尺寸和位置
     * @param handle 活动控制点
     * @param deltaX X轴变化量
     * @param deltaY Y轴变化量
     * @param originalWidth 原始宽度
     * @param originalHeight 原始高度
     * @param originalX 原始X位置
     * @param originalY 原始Y位置
     * @param minSize 最小尺寸
     * @return 包含新宽度、高度、X位置、Y位置的数组 [newWidth, newHeight, newX, newY]
     */
    public static double[] calculateNewDimensions(ResizeHandle handle, double deltaX, double deltaY,
                                                double originalWidth, double originalHeight,
                                                double originalX, double originalY,
                                                double minSize) {
        double newWidth = originalWidth;
        double newHeight = originalHeight;
        double newX = originalX;
        double newY = originalY;

        switch (handle) {
            case TOP_LEFT:
                newWidth = Math.max(minSize, originalWidth - deltaX);
                newHeight = Math.max(minSize, originalHeight - deltaY);
                newX = originalX + (originalWidth - newWidth);
                newY = originalY + (originalHeight - newHeight);
                break;
            case TOP_CENTER:
                newHeight = Math.max(minSize, originalHeight - deltaY);
                newY = originalY + (originalHeight - newHeight);
                break;
            case TOP_RIGHT:
                newWidth = Math.max(minSize, originalWidth + deltaX);
                newHeight = Math.max(minSize, originalHeight - deltaY);
                newY = originalY + (originalHeight - newHeight);
                break;
            case MIDDLE_LEFT:
                newWidth = Math.max(minSize, originalWidth - deltaX);
                newX = originalX + (originalWidth - newWidth);
                break;
            case MIDDLE_RIGHT:
                newWidth = Math.max(minSize, originalWidth + deltaX);
                break;
            case BOTTOM_LEFT:
                newWidth = Math.max(minSize, originalWidth - deltaX);
                newHeight = Math.max(minSize, originalHeight + deltaY);
                newX = originalX + (originalWidth - newWidth);
                break;
            case BOTTOM_CENTER:
                newHeight = Math.max(minSize, originalHeight + deltaY);
                break;
            case BOTTOM_RIGHT:
                newWidth = Math.max(minSize, originalWidth + deltaX);
                newHeight = Math.max(minSize, originalHeight + deltaY);
                break;
        }

        return new double[]{newWidth, newHeight, newX, newY};
    }
    
    /**
     * 获取控制点大小
     * @return 控制点大小
     */
    public static double getHandleSize() {
        return HANDLE_SIZE;
    }
}
