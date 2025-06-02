package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ResourceBundle;

public class ShapeButton implements Initializable {

    @FXML
    private Canvas shapeCanvas;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        double width = shapeCanvas.getWidth();
        double height = shapeCanvas.getHeight();
        double padding = 4; // 边距
        double diameter = Math.min(width, height) - 2 * padding;
        double x = (width - diameter) / 2;
        double y = (height - diameter) / 2;

        GraphicsContext gc = shapeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.strokeOval(x, y, diameter, diameter); // 居中且不超出边框

        // 添加拖拽事件
        shapeCanvas.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = shapeCanvas.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString("circle"); // 传递标识
            db.setContent(content);
            event.consume();
        });
    }
}
