package game;

import java.util.Random;

public class Vector2i {
    
    public int x, y;

    public Vector2i() {
        x = 0;
        y = 0;
    }

    public Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2i add(Vector2i other) {
        return new Vector2i(x + other.x, y + other.y);
    }

    public Vector2i subtract(Vector2i other) {
        return new Vector2i(x - other.x, y - other.y);
    }

    public Vector2i multiply(Vector2i other) {
        return new Vector2i(x * other.x, y * other.y);
    }

    public Vector2i divide(Vector2i other) {
        return new Vector2i(x / other.x, y / other.y);
    }

    public boolean equals(Object obj) {
        if (obj instanceof Vector2i) {
            Vector2i vec = (Vector2i) obj;
            return vec.x == x && vec.y == y;
        } else {
            return super.equals(obj);
        }
    }

    public static Vector2i random(Random random, Vector2i limit) {
        return new Vector2i(random.nextInt(limit.x), random.nextInt(limit.y));
    }

}
