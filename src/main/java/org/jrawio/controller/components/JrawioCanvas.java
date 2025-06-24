package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Point2D;
import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;
import java.util.Arrays;
import java.util.List;

public class JrawioCanvas {
    @FXML
    private Canvas gridCanvas;

    @FXML
    private Pane canvasPane;

    // 框选相关成员变量
    private Rectangle selectionRect = new Rectangle();
    private double startX, startY;

    // 右键菜单
    private RightClickMenu canvasContextMenu;

    @FXML
    public void initialize() {
        // 设置canvasPane的大小
        double paneWidth = gridCanvas.getWidth() + 500;
        double paneHeight = gridCanvas.getHeight() + 500;
        canvasPane.setPrefWidth(paneWidth);
        canvasPane.setPrefHeight(paneHeight);

        // 让gridCanvas居中
        gridCanvas.setLayoutX((paneWidth - gridCanvas.getWidth()) / 2);
        gridCanvas.setLayoutY((paneHeight - gridCanvas.getHeight()) / 2);

        // 让gridCanvas不响应鼠标事件
        gridCanvas.setMouseTransparent(true);

        // 初始化拖拽功能
        initializeDragAndDrop();

        // 初始化右键菜单
        initializeContextMenu();

        // 初始化框选功能
        initializeSelection();

        drawGrid();
    }

    private void drawGrid() {
        double width = gridCanvas.getWidth();
        double height = gridCanvas.getHeight();
        double gridSize = 20; // 网格间隔

        GraphicsContext gc = gridCanvas.getGraphicsContext2D();

        // 填充白色背景
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, width, height);

        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(1);

        // 画竖线
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
        // 画横线
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        // 添加阴影效果
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(20);
        dropShadow.setOffsetX(5);
        dropShadow.setOffsetY(5);
        dropShadow.setColor(Color.rgb(50, 50, 50, 0.4));
        gridCanvas.setEffect(dropShadow);
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
        // 创建菜单项配置列表
        List<RightClickMenu.MenuItemConfig> menuItems = Arrays.asList(
                // 全选菜单项
                RightClickMenu.menuItem("全选", this::selectAllShapes),

                // 分隔符
                RightClickMenu.separator(),

                // 粘贴菜单项
                RightClickMenu.menuItem("粘贴", this::pasteFromClipboard));

        // 创建右键菜单
        canvasContextMenu = new RightClickMenu(canvasPane, menuItems);

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
}
