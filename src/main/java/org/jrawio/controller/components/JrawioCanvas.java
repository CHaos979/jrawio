package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.shape.Rectangle;
import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeFactory;

public class JrawioCanvas {
    @FXML
    private Canvas gridCanvas;

    @FXML
    private Pane canvasPane;

    // 框选相关成员变量
    private Rectangle selectionRect = new Rectangle();
    private double startX, startY;
    
    // 右键菜单
    private ContextMenu contextMenu;

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
            if (db.hasString() && isSupportedShapeType(db.getString())) {
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
            if (db.hasString()) {
                success = createShapeFromDrag(db.getString(), event.getX(), event.getY());
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * 检查是否为支持的形状类型
     */
    private boolean isSupportedShapeType(String identifier) {
        try {
            for (org.jrawio.controller.shape.ShapeType type : org.jrawio.controller.shape.ShapeType.values()) {
                if (type.getIdentifier().equals(identifier)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return false;
    }

    /**
     * 从拖拽创建形状
     */
    private boolean createShapeFromDrag(String identifier, double x, double y) {
        try {
            // 使用ShapeFactory根据标识符创建Shape对象
            Shape shape = ShapeFactory.createShape(identifier, 80, 80);
            // 设置放置位置
            shape.setLayoutX(x - shape.getWidth() / 2);
            shape.setLayoutY(y - shape.getHeight() / 2);
            canvasPane.getChildren().add(shape);
            return true;
        } catch (IllegalArgumentException e) {
            // 不支持的形状类型，忽略
            System.err.println("不支持的形状类型: " + identifier);
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
        selectionRect.setVisible(false);
        canvasPane.getChildren().add(selectionRect);
    }

    /**
     * 设置框选相关的鼠标事件
     */
    private void setupSelectionMouseEvents() {
        // 鼠标按下事件已在 initializeContextMenu 中设置
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
        contextMenu = new ContextMenu();
        
        // 为右键菜单添加样式类
        contextMenu.getStyleClass().add("context-menu");
        
        // 全选菜单项
        MenuItem selectAllItem = new MenuItem("全选");
        selectAllItem.setOnAction(event -> selectAllShapes());
        
        // 分隔符
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        // 粘贴菜单项
        MenuItem pasteItem = new MenuItem("粘贴");
        pasteItem.setOnAction(event -> pasteShapes());
        
        // 添加菜单项到右键菜单
        contextMenu.getItems().addAll(selectAllItem, separator, pasteItem);
        
        // 设置右键菜单事件
        canvasPane.setOnContextMenuRequested(event -> {
            // 只在点击画布空白区域时显示右键菜单
            if (event.getTarget() == canvasPane) {
                contextMenu.show(canvasPane, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });
        
        // 点击其他地方时隐藏右键菜单
        canvasPane.setOnMousePressed(event -> {
            if (contextMenu.isShowing()) {
                contextMenu.hide();
            }
            // 只响应鼠标左键且不是拖拽Shape时
            if (event.isPrimaryButtonDown() && event.getTarget() == canvasPane) {
                startX = event.getX();
                startY = event.getY();
                selectionRect.setX(startX);
                selectionRect.setY(startY);
                selectionRect.setWidth(0);
                selectionRect.setHeight(0);
                selectionRect.setVisible(true);
            }
        });
    }
    
    /**
     * 全选功能的占位实现
     */
    private void selectAllShapes() {
        // TODO: 实现全选功能
        System.out.println("全选功能待实现");
    }
    
    /**
     * 粘贴功能的占位实现
     */
    private void pasteShapes() {
        // TODO: 实现粘贴功能
        System.out.println("粘贴功能待实现");
    }
}
