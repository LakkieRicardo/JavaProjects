package game;

import java.awt.image.BufferStrategy;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import game.objects.Renderable;
import game.objects.RenderableBall;
import game.objects.RenderablePuck;

import java.awt.*;

public class PongRender implements KeyListener {
    
    public final PongFrame frame;
    public final Thread gameThread;

    public final List<Renderable> renderables;

    public final long refreshRate = 60;
    public int playerScore = 0, aiScore = 0;

    public final RenderableBall ball;

    public GameState state = GameState.START;

    public PongRender() {
        frame = new PongFrame();
        renderables = new ArrayList<Renderable>();
        renderables.add(new RenderablePuck(this, false));
        renderables.add(new RenderablePuck(this, true));
        ball = new RenderableBall();
        renderables.add(ball);
        gameThread = new Thread(this::renderLoop, "game");
        gameThread.start();
        frame.addKeyListener(this);
        frame.canvas.addKeyListener(this);
    }

    public void keyTyped(KeyEvent e) { }

    public void keyPressed(KeyEvent e) { }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (state != GameState.GAME) {
                state = GameState.GAME;
            } else {
                state = GameState.PAUSE;
            }
        }
    }

    public int getWidth() {
        return frame.canvas.getWidth();
    }

    public int getHeight() {
        return frame.canvas.getHeight();
    }

    public int projectPoint(float pos, int offset, int projectionSize) {
        return (int)(pos * (float)projectionSize) + offset;
    }

    public int projectPointX(float x, int offsetX) {
        return projectPoint(x, offsetX, getWidth());
    }

    public int projectPointY(float y, int offsetY) {
        return projectPoint(y, offsetY, getHeight());
    }

    private void renderLoop() {
        long lastTime = System.nanoTime(); // 1 ns = 1/1,000,000,000 s
        while (true) {
            long currentTime = System.nanoTime();
            if (currentTime - lastTime > 1000000000 / refreshRate) {
                lastTime = currentTime; 
                render();
            }
        }
    }

    public void render() {
        BufferStrategy bs = frame.canvas.getBufferStrategy();
        if (bs == null) {
            frame.canvas.createBufferStrategy(3);
            bs = frame.canvas.getBufferStrategy();
        }
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        g.setColor(Color.black);
        g.fillRect(0, 0, frame.canvas.getWidth(), frame.canvas.getHeight());

        switch (state) {
            case START:
                Font font = new Font("Helvetica", Font.BOLD, 96);
                FontMetrics metrics = g.getFontMetrics(font);
                int width = metrics.stringWidth("Welcome to Pong!");
                int height = metrics.getHeight();
                g.setFont(font);
                g.setColor(Color.white);
                g.drawString("Welcome to Pong!", (getWidth() - width) / 2, (getHeight() - height) / 2);
                Font fontSub = new Font("Helvetica", Font.PLAIN, 48);
                FontMetrics metricsSub = g.getFontMetrics(fontSub);
                int widthSub = metricsSub.stringWidth("Press \"space\" to start/pause");
                int heightSub = metrics.getHeight();
                g.setFont(fontSub);
                g.setColor(new Color(220, 220, 220));
                g.drawString("Press \"space\" to start/pause", (getWidth() - widthSub) / 2, (getHeight() - heightSub) / 2 + height + 20);
                break;
            case PAUSE:
                Font fontBreak = new Font("Helvetica", Font.BOLD, 48);
                FontMetrics metricsBreak = g.getFontMetrics(fontBreak);
                int widthBreak = metricsBreak.stringWidth("Hit \"space\" to unpause :)");
                g.setFont(fontBreak);
                g.setColor(Color.white);
                g.drawString("Hit \"space\" to unpause :)", (getWidth() - widthBreak) / 2, getHeight() / 2);
                break;
            case GAME:
                for (Renderable renderable : renderables) {
                    renderable.render(this, g);
                }
        
                Font fontScore = new Font("Helvetica", Font.BOLD, 36);
                FontMetrics metricsScore = g.getFontMetrics(fontScore);
                int widthScore = metricsScore.stringWidth(formatScore());
                g.setFont(fontScore);
                g.setColor(Color.red);
                g.drawString(formatScore(), (getWidth() - widthScore) / 2, 50);
                break;
            default:
                break;
        }

        g.dispose();
        bs.show();
    }

    public String formatScore() {
        return String.format("%s:%s", playerScore, aiScore);
    }

    public enum GameState {

        START, GAME, PAUSE;

    }

}
