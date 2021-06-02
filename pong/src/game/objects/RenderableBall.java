package game.objects;

import java.awt.Graphics2D;
import game.PongRender;
import java.awt.*;
import java.util.Random;

public class RenderableBall extends Renderable {
    
    public float motionX = 0f, motionY = 0f;

    public RenderableBall() {
        resetPosition();
    }

    private void resetPosition() {
        x = 0.5f;
        y = 0.5f;
        motionY = 0f;
        boolean direction = new Random().nextBoolean();
        if (direction) {
            motionX = 0.005f;
        } else {
            motionX = -0.005f;
        }
    }

    public void render(PongRender render, Graphics2D g) {
        if (y + motionY < 0 || (y + motionY) >= 1) {
            motionY *= -1;
        }
        x += motionX;
        y += motionY;
        if (render.projectPointX(x, 20) < 0) {
            // AI scores
            render.aiScore++;
            resetPosition();
            return;
        }
        if (render.projectPointX(x, -20) > render.getWidth()) {
            // Player scores
            render.playerScore++;
            resetPosition();
            return;
        }
        // Detect collision
        Rectangle ball = new Rectangle(render.projectPointX(x, 0), render.projectPointY(y, 0), 20, 20);
        for (Renderable renderable : render.renderables) {
            if (renderable instanceof RenderablePuck) {
                Rectangle puck = new Rectangle(render.projectPointX(renderable.x, renderable.offsetX), render.projectPointY(renderable.y, renderable.offsetY), 20, 80);
                if (ball.intersects(puck)) {
                    double hitAngle = Math.atan((renderable.y - y) / (x - renderable.x));
                    motionX *= -1f;
                    motionY = -(float)Math.tan(hitAngle) * motionX;
                }
            }
        }
        g.setColor(Color.white);
        g.fillRoundRect((int)(x * (float)render.getWidth()) - 10, (int)(y * (float)render.getHeight()) - 10, 20, 20, 20, 20);
    }

}
