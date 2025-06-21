package org.jrawio.controller.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Pane;
import org.jrawio.controller.shape.Shape;

/**
 * 画布右键菜单类
 * 负责处理画布的右键菜单功能
 */
public class CanvasContextMenu {
    private final ContextMenu contextMenu;
    private final Pane canvasPane;
    
    // 回调接口，用于与主画布类通信
    public interface CanvasMenuCallback {
        void onSelectAll();
        void onPaste();
    }
    
    private CanvasMenuCallback callback;

    /**
     * 构造函数
     * @param canvasPane 画布面板
     */
    public CanvasContextMenu(Pane canvasPane) {
        this.canvasPane = canvasPane;
        this.contextMenu = new ContextMenu();
        initializeContextMenu();
        setupContextMenuEvents();
    }

    /**
     * 设置回调接口
     * @param callback 回调实现
     */
    public void setCallback(CanvasMenuCallback callback) {
        this.callback = callback;
    }

    /**
     * 初始化右键菜单
     */
    private void initializeContextMenu() {
        // 为右键菜单添加样式类
        contextMenu.getStyleClass().add("context-menu");
        
        // 全选菜单项
        MenuItem selectAllItem = new MenuItem("全选");
        selectAllItem.setOnAction(event -> {
            if (callback != null) {
                callback.onSelectAll();
            }
        });
        
        // 分隔符
        SeparatorMenuItem separator = new SeparatorMenuItem();
        
        // 粘贴菜单项
        MenuItem pasteItem = new MenuItem("粘贴");
        pasteItem.setOnAction(event -> {
            if (callback != null) {
                callback.onPaste();
            }
        });
        
        // 添加菜单项到右键菜单
        contextMenu.getItems().addAll(selectAllItem, separator, pasteItem);
    }

    /**
     * 设置右键菜单事件
     */
    private void setupContextMenuEvents() {
        // 设置右键菜单事件
        canvasPane.setOnContextMenuRequested(event -> {
            // 只在点击画布空白区域时显示右键菜单
            if (event.getTarget() == canvasPane) {
                contextMenu.show(canvasPane, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });
    }

    /**
     * 隐藏右键菜单
     */
    public void hide() {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
    }

    /**
     * 检查右键菜单是否正在显示
     * @return true如果正在显示，false否则
     */
    public boolean isShowing() {
        return contextMenu.isShowing();
    }

    /**
     * 全选画布中的所有形状
     */
    public void selectAllShapes() {
        for (javafx.scene.Node node : canvasPane.getChildren()) {
            if (node instanceof Shape) {
                Shape shape = (Shape) node;
                shape.setSelected(true);
            }
        }
    }

    /**
     * 添加新的菜单项
     * @param text 菜单项文本
     * @param action 菜单项动作
     */
    public void addMenuItem(String text, Runnable action) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(event -> action.run());
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 添加分隔符
     */
    public void addSeparator() {
        contextMenu.getItems().add(new SeparatorMenuItem());
    }

    /**
     * 清空所有菜单项
     */
    public void clearMenuItems() {
        contextMenu.getItems().clear();
    }
}
