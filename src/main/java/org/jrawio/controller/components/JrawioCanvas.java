package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import javafx.scene.Node;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;
import java.util.List;
import java.util.ArrayList;

public class JrawioCanvas {
    @FXML
    private Canvas gridCanvas;

    @FXML
    private Pane canvasPane;

    // 框选相关成员变量
    private Rectangle selectionRect = new Rectangle();
    private double startX, startY; // 右键菜单
    private RightClickMenu canvasContextMenu;

    // 剪贴板
    private ShapeClipboard shapeClipboard;

    @FXML
    public void initialize() {
        // 初始化剪贴板
        shapeClipboard = ShapeClipboard.getInstance();

        // 设置canvasPane的大小
        double paneWidth = 1200; // 设置更大的画布区域
        double paneHeight = 800;
        canvasPane.setPrefWidth(paneWidth);
        canvasPane.setPrefHeight(paneHeight);

        // 调整gridCanvas大小以覆盖整个区域
        gridCanvas.setWidth(paneWidth);
        gridCanvas.setHeight(paneHeight);
        gridCanvas.setLayoutX(0);
        gridCanvas.setLayoutY(0);

        // 让gridCanvas不响应鼠标事件
        gridCanvas.setMouseTransparent(true);

        // 初始化拖拽功能
        initializeDragAndDrop();

        // 初始化右键菜单
        initializeContextMenu(); // 初始化框选功能
        initializeSelection();

        // 初始化键盘快捷键
        initializeKeyboardShortcuts();

        drawGrid();
    }

    private void drawGrid() {
        double width = gridCanvas.getWidth();
        double height = gridCanvas.getHeight();
        double gridSize = 20; // 网格间隔

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        // 清除画布
        gc.clearRect(0, 0, width, height);

        // 填充白色背景
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        // 设置网格线样式
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        // 画竖线 - 覆盖整个宽度
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // 画横线 - 覆盖整个高度
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        // 添加边框
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1);
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * 初始化拖拽功能
     */
    private void initializeDragAndDrop() {
        setupDragOver();
        setupDragDropped();
    }

    /**
     * 设置拖拽悬停事件
     */
    private void setupDragOver() {
        canvasPane.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            // 检查是否有形状类型和大小信息
            if (db.hasContent(DragDataFormats.SHAPE_TYPE_FORMAT) &&
                    db.hasContent(DragDataFormats.SHAPE_WIDTH_FORMAT) &&
                    db.hasContent(DragDataFormats.SHAPE_HEIGHT_FORMAT)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
    }

    /**
     * 设置拖拽放下事件
     */
    private void setupDragDropped() {
        canvasPane.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            // 使用形状类型和大小信息创建形状
            if (db.hasContent(DragDataFormats.SHAPE_TYPE_FORMAT) &&
                    db.hasContent(DragDataFormats.SHAPE_WIDTH_FORMAT) &&
                    db.hasContent(DragDataFormats.SHAPE_HEIGHT_FORMAT)) {
                success = createShapeFromData(db, event.getX(), event.getY());
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * 使用形状类型和大小信息创建形状
     */
    private boolean createShapeFromData(Dragboard db, double x, double y) {
        try {
            // 获取形状类型和大小信息
            ShapeType shapeType = (ShapeType) db.getContent(DragDataFormats.SHAPE_TYPE_FORMAT);
            Double width = (Double) db.getContent(DragDataFormats.SHAPE_WIDTH_FORMAT);
            Double height = (Double) db.getContent(DragDataFormats.SHAPE_HEIGHT_FORMAT);

            if (shapeType == null || width == null || height == null) {
                return false;
            }

            // 使用ShapeFactory创建形状
            Shape shape = ShapeFactory.createShape(shapeType, width, height);

            // 设置放置位置
            shape.setLayoutX(x - shape.getWidth() / 2);
            shape.setLayoutY(y - shape.getHeight() / 2);
            canvasPane.getChildren().add(shape);
            shape.draw();
            return true;
        } catch (Exception e) {
            System.err.println("创建形状失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 初始化框选功能
     */
    private void initializeSelection() {
        setupSelectionRectangle();
        setupSelectionMouseEvents();
    }

    /**
     * 设置选区框的样式
     */
    private void setupSelectionRectangle() {
        selectionRect.setStroke(Color.BLUE);
        selectionRect.setStrokeWidth(1);
        selectionRect.setFill(Color.web("rgba(100,100,255,0.2)"));
        // 设置虚线样式
        selectionRect.getStrokeDashArray().addAll(5d, 5d);
        selectionRect.setVisible(false);
        canvasPane.getChildren().add(selectionRect);
    }

    /**
     * 设置框选相关的鼠标事件
     */
    private void setupSelectionMouseEvents() {
        // 鼠标按下事件在 setupCanvasMouseEvents 中统一设置
        canvasPane.setOnMouseDragged(this::onSelectionMouseDragged);
        canvasPane.setOnMouseReleased(this::onSelectionMouseReleased);
    }

    /**
     * 处理框选拖拽事件
     */
    private void onSelectionMouseDragged(javafx.scene.input.MouseEvent event) {
        if (selectionRect.isVisible()) {
            updateSelectionRectangle(event.getX(), event.getY());
        }
    }

    /**
     * 更新选区框的位置和大小
     */
    private void updateSelectionRectangle(double currentX, double currentY) {
        double x = Math.min(startX, currentX);
        double y = Math.min(startY, currentY);
        double w = Math.abs(currentX - startX);
        double h = Math.abs(currentY - startY);
        selectionRect.setX(x);
        selectionRect.setY(y);
        selectionRect.setWidth(w);
        selectionRect.setHeight(h);
    }

    /**
     * 处理框选结束事件
     */
    private void onSelectionMouseReleased(javafx.scene.input.MouseEvent event) {
        if (selectionRect.isVisible()) {
            selectShapesInRectangle();
            selectionRect.setVisible(false);
        }
    }

    /**
     * 选择在选区框内的所有Shape
     */
    private void selectShapesInRectangle() {
        for (javafx.scene.Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                boolean isInSelection = isShapeInSelection(shape);
                shape.setSelected(isInSelection);
            }
        }
    }

    /**
     * 判断Shape是否在选区内
     */
    private boolean isShapeInSelection(Shape shape) {
        double sx = shape.getLayoutX();
        double sy = shape.getLayoutY();
        double sw = shape.getWidth();
        double sh = shape.getHeight();
        return selectionRect.getBoundsInParent().intersects(sx, sy, sw, sh);
    }

    /**
     * 初始化右键菜单
     */
    private void initializeContextMenu() {
        // 创建右键菜单但不设置菜单项（动态创建）
        canvasContextMenu = new RightClickMenu(canvasPane);

        // 设置鼠标事件处理
        setupCanvasMouseEvents();
    }

    /**
     * 设置画布鼠标事件处理
     */
    private void setupCanvasMouseEvents() {
        canvasPane.setOnMousePressed(this::onCanvasMousePressed);
    }

    /**
     * 处理画布鼠标按下事件
     */
    private void onCanvasMousePressed(javafx.scene.input.MouseEvent event) {
        // 隐藏右键菜单（如果正在显示）
        if (canvasContextMenu.isShowing()) {
            canvasContextMenu.hide();
        }

        // 处理右键点击 - 动态创建菜单
        if (event.isSecondaryButtonDown() && event.getTarget() == canvasPane) {
            createDynamicContextMenu();
            return;
        }

        // 只响应鼠标左键且点击目标是画布本身（不是拖拽Shape）
        if (event.isPrimaryButtonDown() && event.getTarget() == canvasPane) {
            startSelectionBox(event.getX(), event.getY());
        }
    }

    /**
     * 开始框选操作
     */
    private void startSelectionBox(double x, double y) {
        startX = x;
        startY = y;
        selectionRect.setX(startX);
        selectionRect.setY(startY);
        selectionRect.setWidth(0);
        selectionRect.setHeight(0);
        selectionRect.setVisible(true);
    }

    // 右键菜单操作方法

    /**
     * 全选功能实现
     */
    private void selectAllShapes() {
        for (javafx.scene.Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                shape.setSelected(true);
            }
        }
    }

    /**
     * 粘贴功能实现
     */
    private void pasteFromClipboard() {
        // 获取右键点击位置
        Point2D clickPosition = canvasContextMenu.getLastClickPosition();
        if (clickPosition == null) {
            System.out.println("No right-click position recorded");
            return;
        }

        // 获取剪贴板实例
        ShapeClipboard clipboard = ShapeClipboard.getInstance();

        // 从剪贴板粘贴图形
        List<Shape> pastedShapes = clipboard.paste(clickPosition);

        if (pastedShapes.isEmpty()) {
            System.out.println("Clipboard is empty or paste failed");
            return;
        }

        // 将粘贴的图形添加到画布
        for (Shape shape : pastedShapes) {
            canvasPane.getChildren().add(shape);
        }

        System.out.println("Successfully pasted " + pastedShapes.size() + " shapes at position (" +
                clickPosition.getX() + ", " + clickPosition.getY() + ")");
    }

    /**
     * 动态创建右键菜单
     */
    private void createDynamicContextMenu() {
        // 清空现有菜单项
        canvasContextMenu.clearMenuItems();

        // 检查是否有选中的图形
        boolean hasSelectedShapes = hasSelectedShapes();

        if (hasSelectedShapes) {
            // 有选中图形时的菜单
            canvasContextMenu.addMenuItem("复制", this::copySelectedShapes);
            canvasContextMenu.addMenuItem("删除", this::deleteSelectedShapes);
            canvasContextMenu.addSeparator();
        }

        // 通用菜单项
        canvasContextMenu.addMenuItem("全选", this::selectAllShapes);
        canvasContextMenu.addSeparator();
        canvasContextMenu.addMenuItem("粘贴", this::pasteFromClipboard);
        canvasContextMenu.addSeparator();
        canvasContextMenu.addMenuItem("导出为PNG", this::exportToPNG);
    }

    /**
     * 检查是否有选中的图形
     */
    private boolean hasSelectedShapes() {
        for (javafx.scene.Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                if (shape.isSelected()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 复制选中的图形
     */
    private void copySelectedShapes() {
        // 获取所有选中的图形
        List<Shape> selectedShapes = new ArrayList<>();
        for (Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                if (shape.isSelected()) {
                    selectedShapes.add(shape);
                }
            }
        }

        if (!selectedShapes.isEmpty()) {
            // 将选中的图形复制到剪贴板
            shapeClipboard.copy(selectedShapes);
            System.out.println("Copied " + selectedShapes.size() + " selected shapes to clipboard");
        } else {
            System.out.println("No shapes selected for copying");
        }
    }

    /**
     * 删除选中的图形
     */
    private void deleteSelectedShapes() {
        // 获取所有选中的图形
        List<Shape> shapesToDelete = new ArrayList<>();
        for (Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                if (shape.isSelected()) {
                    shapesToDelete.add(shape);
                }
            }
        }

        if (!shapesToDelete.isEmpty()) {
            // 删除所有选中的图形
            for (Shape shape : shapesToDelete) {
                // 调用 Shape 的删除方法，会自动处理清理逻辑
                shape.deleteShape();
            }
            System.out.println("Deleted " + shapesToDelete.size() + " selected shapes");
        } else {
            System.out.println("No shapes selected for deletion");
        }
    }

    /**
     * 初始化键盘快捷键
     */
    private void initializeKeyboardShortcuts() {
        // 设置 canvasPane 可以接收键盘焦点
        canvasPane.setFocusTraversable(true);

        // 添加键盘事件处理
        canvasPane.setOnKeyPressed(this::handleKeyPressed);

        // 确保 canvasPane 在点击时获得焦点
        canvasPane.setOnMouseClicked(event -> {
            canvasPane.requestFocus();
        });
    }

    /**
     * 处理键盘按键事件
     */
    private void handleKeyPressed(KeyEvent event) {
        // Ctrl+C - 复制选中的图形
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            copySelectedShapes();
            event.consume();
        }
        // Ctrl+V - 粘贴图形
        else if (event.isControlDown() && event.getCode() == KeyCode.V) {
            pasteAtCenter();
            event.consume();
        }
        // Delete 键 - 删除选中的图形
        else if (event.getCode() == KeyCode.DELETE) {
            deleteSelectedShapes();
            event.consume();
        }
        // Ctrl+A - 全选
        else if (event.isControlDown() && event.getCode() == KeyCode.A) {
            selectAllShapes();
            event.consume();
        }
    }

    /**
     * 在画布中心粘贴图形（用于快捷键粘贴）
     */
    private void pasteAtCenter() {
        // 计算画布中心位置
        double centerX = gridCanvas.getWidth() / 2;
        double centerY = gridCanvas.getHeight() / 2;
        Point2D centerPosition = new Point2D(centerX, centerY);

        // 获取剪贴板实例
        ShapeClipboard clipboard = ShapeClipboard.getInstance();

        // 从剪贴板粘贴图形
        List<Shape> pastedShapes = clipboard.paste(centerPosition);

        if (pastedShapes.isEmpty()) {
            System.out.println("Clipboard is empty or paste failed");
            return;
        }

        // 将粘贴的图形添加到画布
        for (Shape shape : pastedShapes) {
            canvasPane.getChildren().add(shape);
        }

        System.out.println("Successfully pasted " + pastedShapes.size() + " shapes at center position (" +
                centerX + ", " + centerY + ")");
    }

    /**
     * 导出画布为PNG图片
     */
    private void exportToPNG() {
        try {
            // 计算所有图形的边界框
            double[] bounds = calculateShapesBounds();
            if (bounds == null) {
                System.out.println("No shapes to export");
                return;
            }

            double minX = bounds[0];
            double minY = bounds[1];
            double maxX = bounds[2];
            double maxY = bounds[3];

            // 添加一些边距
            double padding = 20;
            double exportWidth = maxX - minX + 2 * padding;
            double exportHeight = maxY - minY + 2 * padding;

            // 创建导出用的Canvas
            Canvas exportCanvas = new Canvas(exportWidth, exportHeight);
            GraphicsContext gc = exportCanvas.getGraphicsContext2D();

            // 设置透明背景
            gc.clearRect(0, 0, exportWidth, exportHeight);

            // 计算偏移量，使所有图形都在导出Canvas内
            double offsetX = -minX + padding;
            double offsetY = -minY + padding;

            // 绘制所有图形到导出Canvas
            for (Node node : canvasPane.getChildren()) {
                if (node instanceof Shape) {
                    Shape shape = (Shape) node;
                    drawShapeToExportCanvas(shape, gc, offsetX, offsetY);
                }
            }

            // 使用快照导出
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(Color.TRANSPARENT); // 透明背景
            javafx.scene.image.WritableImage image = exportCanvas.snapshot(params, null);

            // 打开文件保存对话框
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("导出PNG图片");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PNG文件", "*.png"));
            fileChooser.setInitialFileName("diagram.png");

            // 获取当前窗口
            javafx.stage.Stage stage = (javafx.stage.Stage) canvasPane.getScene().getWindow();
            java.io.File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // 保存图片
                saveImageAsPNG(image, file);
                System.out.println("Successfully exported to: " + file.getAbsolutePath());
            } else {
                System.out.println("Export cancelled by user");
            }

            System.out.println("Export completed successfully!");
            System.out.println("Export size: " + (int) exportWidth + "x" + (int) exportHeight);

        } catch (Exception e) {
            System.err.println("Failed to export PNG: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 计算所有图形的边界框
     * 
     * @return [minX, minY, maxX, maxY] 或 null 如果没有图形
     */
    private double[] calculateShapesBounds() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        boolean hasShapes = false;

        for (Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                hasShapes = true;

                double shapeMinX = shape.getLayoutX();
                double shapeMinY = shape.getLayoutY();
                double shapeMaxX = shapeMinX + shape.getWidth();
                double shapeMaxY = shapeMinY + shape.getHeight();

                minX = Math.min(minX, shapeMinX);
                minY = Math.min(minY, shapeMinY);
                maxX = Math.max(maxX, shapeMaxX);
                maxY = Math.max(maxY, shapeMaxY);
            }
        }

        return hasShapes ? new double[] { minX, minY, maxX, maxY } : null;
    }

    /**
     * 将Shape绘制到导出Canvas上
     */
    private void drawShapeToExportCanvas(Shape shape, GraphicsContext gc, double offsetX, double offsetY) {
        // 保存当前变换状态
        gc.save();

        // 应用偏移变换
        gc.translate(shape.getLayoutX() + offsetX, shape.getLayoutY() + offsetY);

        // 直接对Shape进行快照
        javafx.scene.SnapshotParameters snapParams = new javafx.scene.SnapshotParameters();
        snapParams.setFill(Color.TRANSPARENT);
        javafx.scene.image.WritableImage shapeImage = shape.snapshot(snapParams, null);

        gc.drawImage(shapeImage, 0, 0);

        // 恢复变换状态
        gc.restore();
    }

    /**
     * 保存图片为PNG文件
     */
    private void saveImageAsPNG(javafx.scene.image.WritableImage image, java.io.File file) {
        try {
            // 确保文件名以.png结尾
            String fileName = file.getName();
            if (!fileName.toLowerCase().endsWith(".png")) {
                file = new java.io.File(file.getParentFile(), fileName + ".png");
            }

            // 使用Java AWT/Swing的BufferedImage来处理PNG导出
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // 创建BufferedImage
            java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);

            // 从JavaFX的WritableImage读取像素数据并转换到BufferedImage
            javafx.scene.image.PixelReader pixelReader = image.getPixelReader();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color fxColor = pixelReader.getColor(x, y);

                    // 转换为ARGB格式
                    int a = (int) (fxColor.getOpacity() * 255);
                    int r = (int) (fxColor.getRed() * 255);
                    int g = (int) (fxColor.getGreen() * 255);
                    int b = (int) (fxColor.getBlue() * 255);

                    int argb = (a << 24) | (r << 16) | (g << 8) | b;
                    bufferedImage.setRGB(x, y, argb);
                }
            }

            // 使用ImageIO写入PNG文件
            javax.imageio.ImageIO.write(bufferedImage, "PNG", file);

            System.out.println("PNG文件成功保存到: " + file.getAbsolutePath());
            System.out.println("- 图片尺寸: " + width + "x" + height);

        } catch (Exception e) {
            System.err.println("保存PNG文件失败: " + e.getMessage());
            e.printStackTrace();

            // 如果ImageIO不可用，回退到简化方法
            System.out.println("正在尝试备用保存方法...");
            saveImageAsSimpleFormat(image, file);
        }
    }

    /**
     * 备用的图片保存方法（当ImageIO不可用时）
     */
    private void saveImageAsSimpleFormat(javafx.scene.image.WritableImage image, java.io.File file) {
        try {
            // 确保文件名以.png结尾
            String fileName = file.getName();
            if (!fileName.toLowerCase().endsWith(".png")) {
                file = new java.io.File(file.getParentFile(), fileName + ".png");
            }

            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // 创建字节数组来存储图片数据
            byte[] buffer = new byte[width * height * 4]; // RGBA格式

            // 从WritableImage读取像素数据
            javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
            int index = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    javafx.scene.paint.Color color = pixelReader.getColor(x, y);

                    // 转换为RGBA字节
                    buffer[index++] = (byte) (color.getRed() * 255);
                    buffer[index++] = (byte) (color.getGreen() * 255);
                    buffer[index++] = (byte) (color.getBlue() * 255);
                    buffer[index++] = (byte) (color.getOpacity() * 255);
                }
            }

            // 使用简单的数据文件格式
            writeImageDataFile(file, buffer, width, height);

            System.out.println("图片数据文件保存成功: " + file.getAbsolutePath());
            System.out.println("- 图片尺寸: " + width + "x" + height);
            System.out.println("注意: 由于系统限制，保存为自定义格式而非标准PNG");

        } catch (Exception e) {
            System.err.println("保存图片数据文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 写入图片数据文件的实现
     */
    private void writeImageDataFile(java.io.File file, byte[] imageData, int width, int height) {
        try {
            // 创建文件输出流
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                // 写入简单的头信息
                writeIntBE(fos, width); // 宽度
                writeIntBE(fos, height); // 高度

                // 写入图片数据
                fos.write(imageData);
                fos.flush();
            }

            System.out.println("图片数据文件写入成功: " + file.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("写入图片数据文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 以大端序写入整数
     */
    private void writeIntBE(java.io.FileOutputStream fos, int value) throws java.io.IOException {
        fos.write((value >>> 24) & 0xFF);
        fos.write((value >>> 16) & 0xFF);
        fos.write((value >>> 8) & 0xFF);
        fos.write(value & 0xFF);
    }
}
