package org.jrawio.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.jrawio.controller.shape.Shape;
import org.jrawio.controller.shape.ShapeType;
import org.jrawio.controller.shape.ShapeFactory;
import java.net.URL;
import java.util.ResourceBundle;

public class ShapeButton implements Initializable {

    @FXML
    private Canvas shapeCanvas;
    
    private ShapeType shapeType = ShapeType.OVAL; // 默认为椭圆
    
    /**
     * 设置要绘制的形状类型
     */
    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
        if (shapeCanvas != null) {
            drawPreview();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawPreview();
        setupDragAndDrop();
    }
    
    /**
     * 绘制形状预览
     */
    private void drawPreview() {
        double width = shapeCanvas.getWidth();
        double height = shapeCanvas.getHeight();
        double padding = 4;
        double previewWidth = width - 2 * padding;
        double previewHeight = height - 2 * padding;
        
        // 清除画布
        GraphicsContext gc = shapeCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        
        // 使用对应的Shape类来绘制预览
        Shape previewShape = ShapeFactory.createShape(shapeType, previewWidth, previewHeight);
        previewShape.drawPreview(gc, padding, padding, previewWidth, previewHeight);
    }
    
    /**
     * 设置拖拽功能
     */
    private void setupDragAndDrop() {
        shapeCanvas.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = shapeCanvas.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(shapeType.getIdentifier()); // 传递形状标识
            db.setContent(content);
            event.consume();
        });
    }
}
