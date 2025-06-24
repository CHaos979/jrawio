package org.jrawio.controller.shape;

/**
 * 形状类型枚举
 */
public enum ShapeType {
    OVAL("circle"),
    RECTANGLE("rectangle"),
    ARROW("arrow");

    private final String identifier;

    ShapeType(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
