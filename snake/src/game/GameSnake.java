package game;

import java.awt.image.*;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.awt.Color;

public class GameSnake implements KeyListener {
    
    public final GameFrame frame;
    public final Thread gameThread;

    public int[] grid;

    public Vector2i snakeMotion = new Vector2i();
    public Vector2i[] snakePieces;

    public Vector2i fruit;

    public GameSnake() {
        frame = new GameFrame();
        // 1 centered piece
        frame.addKeyListener(this);
        frame.canvas.addKeyListener(this);
        
        grid = new int[GameSettings.GRID_WIDTH * GameSettings.GRID_HEIGHT];
        
        resetState();

        gameThread = new Thread(this::loop, "game");
        gameThread.start();
    }

    public int getWidth() {
        return frame.canvas.getWidth();
    }

    public int getHeight() {
        return frame.canvas.getHeight();
    }

    public void resetState() {
        snakePieces = new Vector2i[] { GameSettings.GRID_SIZE.divide(new Vector2i(2, 2)) };
        fruit = Vector2i.random(GameSettings.RANDOM, GameSettings.GRID_SIZE);
    }

    public void eatFruit() {
        fruit = Vector2i.random(GameSettings.RANDOM, GameSettings.GRID_SIZE);
        addNewPieces(5);
    }

    public Vector2i clipPosition(Vector2i pos) {
        Vector2i clippedPos = new Vector2i(pos.x, pos.y);
        if (pos.x < 0) {
            clippedPos.x = GameSettings.GRID_WIDTH - 1;
        }
        if (pos.x >= GameSettings.GRID_WIDTH) {
            clippedPos.x = 0;
        }
        if (pos.y < 0) {
            clippedPos.y = GameSettings.GRID_HEIGHT - 1;
        }
        if (pos.y >= GameSettings.GRID_HEIGHT) {
            clippedPos.y = 0;
        }
        return clippedPos;
    }

    public void addNewPieces(int count) {
        Vector2i[] newSnakePieces = new Vector2i[snakePieces.length + count];
        for (int i = 0; i < snakePieces.length; i++) {
            newSnakePieces[i] = snakePieces[i];
        }
        Vector2i lastPiece = snakePieces[snakePieces.length - 1];
        for (int i = 0; i < count; i++) {
            newSnakePieces[i + snakePieces.length] = new Vector2i(lastPiece.x, lastPiece.y);
        }
        snakePieces = newSnakePieces;
    }

    public void updateSnakePieces() {
        Vector2i lastPosition = snakePieces[0].subtract(snakeMotion);
        for (int i = 1; i < snakePieces.length; i++) {
            Vector2i oldPosition = snakePieces[i];
            snakePieces[i] = clipPosition(lastPosition);
            lastPosition = oldPosition;
        }
    }

    public void render() {
        for (int y = 0; y < GameSettings.GRID_HEIGHT; y++) {
            for (int x = 0; x < GameSettings.GRID_WIDTH; x++) {
                grid[y * GameSettings.GRID_WIDTH + x] = 0x000000;
            }
        }
        grid[fruit.y * GameSettings.GRID_WIDTH + fruit.x] = 0xff0000;
        for (Vector2i snakePiece : snakePieces) {
            grid[snakePiece.y * GameSettings.GRID_WIDTH + snakePiece.x] = 0xffffff;
        }
    }

    public void update() {
        BufferStrategy bs = frame.canvas.getBufferStrategy();
        if (bs == null) {
            frame.canvas.createBufferStrategy(3);
            bs = frame.canvas.getBufferStrategy();
        }
        Graphics2D g = (Graphics2D) bs.getDrawGraphics();

        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        snakePieces[0] = clipPosition(snakePieces[0].add(snakeMotion));
        for (int i = 1; i < snakePieces.length; i++) {
            // Collision
            if (snakePieces[i].equals(snakePieces[0])) {
                resetState();
            }
        }

        for (Vector2i snakePiece : snakePieces) {
            if (snakePiece.equals(fruit)) {
                eatFruit();
                break;
            }
        }

        updateSnakePieces();

        render();

        float gridCellWidth = (float)(getWidth()) / (float)(GameSettings.GRID_WIDTH);
        float gridCellHeight = (float)(getHeight()) / (float)(GameSettings.GRID_HEIGHT);
        
        for (int gridY = 0; gridY < GameSettings.GRID_HEIGHT; gridY++) {
            for (int gridX = 0; gridX < GameSettings.GRID_WIDTH; gridX++) {
                float x = gridCellWidth * (float)(gridX);
                float y = gridCellHeight * (float)(gridY);
                g.setColor(new Color(grid[gridX + gridY * GameSettings.GRID_WIDTH]));
                g.fillRect((int)x, (int)y, (int)gridCellWidth, (int)gridCellHeight);
            }
        }

        g.dispose();
        bs.show();
    }

    public void loop() {
        long lastTime = System.nanoTime();
        while (true) {
            long currentTime = System.nanoTime();
            if (currentTime - lastTime > 1000000000 / GameSettings.UPDATE_RATE) {
                lastTime = currentTime;
                update();
            }
        }
    }

    public void keyTyped(KeyEvent e) { }
    
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                snakeMotion.y = -1;
                snakeMotion.x = 0;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                snakeMotion.y = 1;
                snakeMotion.x = 0;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                snakeMotion.x = -1;
                snakeMotion.y = 0;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                snakeMotion.x = 1;
                snakeMotion.y = 0;
                break;
        }
    }

    public void keyReleased(KeyEvent e) { }

}
