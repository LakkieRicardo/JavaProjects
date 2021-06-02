package game.objects;

import java.awt.*;

import game.PongRender;

import java.awt.event.*;

public class RenderablePuck extends Renderable implements KeyListener {

    private float playerMotion = 0;
    public final boolean isPlayer;

    public RenderablePuck(PongRender render, boolean isPlayer) {
        this.isPlayer = isPlayer;
        if (isPlayer) {
            x = 0f;
            offsetX = 20;
            render.frame.addKeyListener(this);
            render.frame.canvas.addKeyListener(this);
        } else {
            x = 1f;
            offsetX = -40;
        }
        y = 0.5f;
        offsetY = -40; // center vertically
    }

    public void keyTyped(KeyEvent e) { }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
            playerMotion = -1;
        }
        if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
            playerMotion = 1;
        }
    }

    public void keyReleased(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) && playerMotion == -1) {
            playerMotion = 0;
        }
        if ((e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) && playerMotion == 1) {
            playerMotion = 0;
        }
    }

    public void render(PongRender render, Graphics2D g) {
        if (!isPlayer) {
            // Basic pong AI
            playerMotion = 100 * (render.ball.y - y);
            if (playerMotion > 1) playerMotion = 1;
            if (playerMotion < -1) playerMotion = -1;
        }
        float futurePositionTop = render.projectPointY((float)(y + playerMotion * 0.01), -40);
        float futurePositionBottom = render.projectPointY((float)(y + playerMotion * 0.01), 40);
        if (futurePositionTop > 0 && futurePositionBottom < render.getHeight()) {
            y += playerMotion * 0.01;
        }
        g.setColor(Color.white);
        int posX = (int)(x * (float)render.getWidth()) + offsetX;
        int posY = (int)(y * (float)render.getHeight()) + offsetY;
        g.fillRect(posX, posY, 20, 80);
    }
    
}
