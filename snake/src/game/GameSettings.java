package game;

import java.util.Random;

public class GameSettings {
    
    public static final long UPDATE_RATE = 30;

    public static final int GRID_WIDTH = 50, GRID_HEIGHT = 50 / 16 * 9;

    public static final Random RANDOM = new Random();

    private GameSettings() { }

}
