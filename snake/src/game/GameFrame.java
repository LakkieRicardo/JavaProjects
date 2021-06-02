package game;

import javax.swing.JFrame;
import java.awt.*;

public class GameFrame extends JFrame {
    
    public Canvas canvas;

    public GameFrame() {
        setSize(1280, 720);
        setMinimumSize(new Dimension(800, 800 / 16 * 9));
        setTitle("Snake");
        canvas = new Canvas();
        canvas.setSize(1280, 720);
        add(canvas);
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

}
