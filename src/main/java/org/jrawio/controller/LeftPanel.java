package org.jrawio.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class LeftPanel {
    @FXML
    private VBox leftPanel;
    private final List<ShapeBar> shapeBarControllers = new ArrayList<>();

    @FXML
    public void initialize() throws IOException {
        shapeBarControllers.add(new ShapeBar.Builder()
                .setTitle("基本图形")
                .setExpanded(true)
                .build());
        shapeBarControllers.add(new ShapeBar.Builder().setTitle("流程图").build());
        shapeBarControllers.stream().forEach(leftPanel.getChildren()::add);
    }
}
