package game;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;

public class PongFrame extends JFrame {
    
    public final Canvas canvas;

    public PongFrame() {
        setTitle("Pong");
        setSize(1280, 720);
        setMinimumSize(new Dimension(900, 500));
        canvas = new Canvas();
        canvas.setSize(1280, 720);
        add(canvas);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

}
