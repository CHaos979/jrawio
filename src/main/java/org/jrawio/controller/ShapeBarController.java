package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;

public class ShapeBarController {
    @FXML
    private TitledPane titledPane;

    @FXML 
    public void initialize(){}

    // 供外部调用的初始化方法
    public void init(String title, boolean expanded) {
        titledPane.setText(title);
        titledPane.setExpanded(expanded);
    }
}
