package org.jrawio.controller.components;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.Pane;
import javafx.geometry.Point2D;
import java.util.List;

/**
 * 通用右键菜单类
 * 负责根据提供的菜单项和操作列表绘制右键菜单布局
 * 具体的操作实现由调用方提供
 */
public class RightClickMenu {
    private final ContextMenu contextMenu;
    private final Pane targetPane;

    // 记录右键点击位置
    private Point2D lastClickPosition;

    /**
     * 菜单项配置类
     */
    public static class MenuItemConfig {
        private final String text;
        private final Runnable action;
        private final boolean isSeparator;

        /**
         * 创建普通菜单项
         * 
         * @param text   菜单项文本
         * @param action 菜单项操作
         */
        public MenuItemConfig(String text, Runnable action) {
            this.text = text;
            this.action = action;
            this.isSeparator = false;
        }

        /**
         * 创建分隔符
         */
        public MenuItemConfig() {
            this.text = null;
            this.action = null;
            this.isSeparator = true;
        }

        public String getText() {
            return text;
        }

        public Runnable getAction() {
            return action;
        }

        public boolean isSeparator() {
            return isSeparator;
        }
    }

    /**
     * 构造函数
     * 
     * @param targetPane 目标面板（显示右键菜单的面板）
     */
    public RightClickMenu(Pane targetPane) {
        this.targetPane = targetPane;
        this.contextMenu = new ContextMenu();
        setupContextMenuEvents();
    }

    /**
     * 构造函数，同时初始化菜单项
     * 
     * @param targetPane 目标面板
     * @param menuItems  菜单项配置列表
     */
    public RightClickMenu(Pane targetPane, List<MenuItemConfig> menuItems) {
        this.targetPane = targetPane;
        this.contextMenu = new ContextMenu();
        buildMenu(menuItems);
        setupContextMenuEvents();
    }

    /**
     * 根据菜单项配置列表构建菜单
     * 
     * @param menuItems 菜单项配置列表
     */
    public void buildMenu(List<MenuItemConfig> menuItems) {
        // 清空现有菜单项
        contextMenu.getItems().clear();

        // 添加样式类
        contextMenu.getStyleClass().add("context-menu");

        // 根据配置创建菜单项
        for (MenuItemConfig config : menuItems) {
            if (config.isSeparator()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
            } else {
                MenuItem menuItem = new MenuItem(config.getText());
                if (config.getAction() != null) {
                    menuItem.setOnAction(event -> config.getAction().run());
                }
                contextMenu.getItems().add(menuItem);
            }
        }
    }

    /**
     * 设置右键菜单事件
     */
    private void setupContextMenuEvents() {
        targetPane.setOnContextMenuRequested(event -> {
            // 只在点击目标面板时显示右键菜单
            if (event.getTarget() == targetPane) {
                // 记录右键点击位置（相对于targetPane的坐标）
                lastClickPosition = new Point2D(event.getX(), event.getY());
                contextMenu.show(targetPane, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });
    }

    /**
     * 手动显示右键菜单
     * 
     * @param screenX 屏幕X坐标
     * @param screenY 屏幕Y坐标
     */
    public void show(double screenX, double screenY) {
        contextMenu.show(targetPane, screenX, screenY);
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
     * 
     * @return true如果正在显示，false否则
     */
    public boolean isShowing() {
        return contextMenu.isShowing();
    }

    /**
     * 动态添加菜单项
     * 
     * @param text   菜单项文本
     * @param action 菜单项操作
     */
    public void addMenuItem(String text, Runnable action) {
        MenuItem menuItem = new MenuItem(text);
        if (action != null) {
            menuItem.setOnAction(event -> action.run());
        }
        contextMenu.getItems().add(menuItem);
    }

    /**
     * 动态添加分隔符
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

    /**
     * 获取底层的ContextMenu对象，用于高级定制（包内可见）
     * 
     * @return ContextMenu对象
     */
    ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * 设置菜单样式类
     * 
     * @param styleClass 样式类名
     */
    public void setStyleClass(String styleClass) {
        contextMenu.getStyleClass().clear();
        contextMenu.getStyleClass().add(styleClass);
    }

    /**
     * 便利方法：创建分隔符配置
     * 
     * @return 分隔符配置
     */
    public static MenuItemConfig separator() {
        return new MenuItemConfig();
    }

    /**
     * 便利方法：创建菜单项配置
     * 
     * @param text   菜单项文本
     * @param action 菜单项操作
     * @return 菜单项配置
     */
    public static MenuItemConfig menuItem(String text, Runnable action) {
        return new MenuItemConfig(text, action);
    }

    /**
     * 获取最后一次右键点击的位置
     * 
     * @return 最后一次右键点击的位置（相对于targetPane的坐标），如果没有点击过则返回null
     */
    public Point2D getLastClickPosition() {
        return lastClickPosition;
    }
}
