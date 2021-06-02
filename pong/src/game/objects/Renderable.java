package game.objects;

import java.awt.*;

import game.PongRender;

public abstract class Renderable {
 
    /**
     * 0 = top/left of screen
     * 1 = bottom/right of screen
     */
    public float x, y;
    public int offsetX, offsetY;

    public Renderable(float x, int offsetX, float y, int offsetY) {
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Renderable() {
        this.x = 0f;
        this.y = 0f;
        this.offsetX = 0;
        this.offsetY = 0;
    }

    public abstract void render(PongRender render, Graphics2D g);

}
