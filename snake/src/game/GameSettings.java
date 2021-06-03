package game;

import java.util.Random;

public class GameSettings {
    
    public static final long UPDATE_RATE = 15;

    public static final int GRID_SCALE = 50;

    public static final int GRID_WIDTH = GRID_SCALE, GRID_HEIGHT = GRID_SCALE / 16 * 9;

    public static final Vector2i GRID_SIZE = new Vector2i(GRID_WIDTH, GRID_HEIGHT);

    public static final Random RANDOM = new Random();

    private GameSettings() { }

}
