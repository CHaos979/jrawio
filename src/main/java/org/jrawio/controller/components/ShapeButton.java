package org.jrawio.controller.components;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
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
    
    // 定义默认的形状大小
    private static final double DEFAULT_SHAPE_WIDTH = 80;
    private static final double DEFAULT_SHAPE_HEIGHT = 80;
    
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
        previewShape.drawShape(gc, padding, padding, previewWidth, previewHeight);
    }
    
    /**
     * 生成形状的预览图片
     * @return 预览图片
     */
    private WritableImage generatePreviewImage() {
        double imageWidth = DEFAULT_SHAPE_WIDTH; // 预览图片宽度，与实际形状大小一致
        double imageHeight = DEFAULT_SHAPE_HEIGHT; // 预览图片高度，与实际形状大小一致
        double padding = 1;    // 减少padding以适应更大的形状
        double shapeWidth = imageWidth - 2 * padding;
        double shapeHeight = imageHeight - 2 * padding;
        
        // 创建临时画布来生成图片
        Canvas tempCanvas = new Canvas(imageWidth, imageHeight);
        GraphicsContext gc = tempCanvas.getGraphicsContext2D();
        
        // 清除背景（透明）
        gc.clearRect(0, 0, imageWidth, imageHeight);
        
        // 使用对应的Shape类来绘制预览
        Shape previewShape = ShapeFactory.createShape(shapeType, shapeWidth, shapeHeight);
        previewShape.drawShape(gc, padding, padding, shapeWidth, shapeHeight);
        
        // 将画布内容转换为图片，使用更高的DPI来提高清晰度
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT); // 透明背景
        return tempCanvas.snapshot(params, null);
    }
    
    /**
     * 设置拖拽功能
     */
    private void setupDragAndDrop() {
        shapeCanvas.setOnDragDetected((MouseEvent event) -> {
            Dragboard db = shapeCanvas.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            
            // 创建ShapeCreator函数式对象（使用独立的可序列化类，包含预定义大小）
            ShapeCreator shapeCreator = new SimpleShapeCreator(shapeType, DEFAULT_SHAPE_WIDTH, DEFAULT_SHAPE_HEIGHT);
            
            // 传递函数式对象（通过序列化）
            content.put(DragDataFormats.SHAPE_CREATOR_FORMAT, shapeCreator);
            
            // 生成并传递预览图片
            WritableImage previewImage = generatePreviewImage();
            content.putImage(previewImage);
            
            db.setContent(content);
            
            // 设置自定义拖拽图像来隐藏默认的文件图标
            // 设置偏移量为图片中心，确保清晰显示
            double offsetX = previewImage.getWidth() / 2;
            double offsetY = previewImage.getHeight() / 2;
            db.setDragView(previewImage, offsetX, offsetY);
            
            event.consume();
        });
    }
}
