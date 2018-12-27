import java.awt.*;
import javax.swing.*;

public class GameContainer extends JFrame {

    private static final int CANVAS_WIDTH = 505;
    private static final int CANVAS_HEIGHT = 400;

    private DrawCanvas canvas;
    private GameState gs;

    public GameContainer(){
        canvas = new DrawCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvas.setLayout(new GridBagLayout());

        addComponentsToPane(canvas);

        this.setContentPane(canvas);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.pack();
        this.setTitle("Pokemon - Can you catch them all ?");
        this.setVisible(true);
        this.setResizable(false); // Prevent user from resizing window.
    }

    private void addComponentsToPane(Container pane){
        GameState gs = new GameState(new Grid(4,4));
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        ScorePanel sp = new ScorePanel();
        AnimateBlock aniBlk = new AnimateBlock(gs, sp);

        // Grid component.
        this.gs = gs;
        this.gs.getGrid().addKeyListener(aniBlk);
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        pane.add(this.gs.getGrid(), c); 

        // Score panel.
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0,5,0,0);
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.NORTH;
        pane.add(sp, c);

        // Restart Button.
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(110,5,0,0);
        c.weighty = 1.0;
        RestartButton b = new RestartButton(aniBlk);
        pane.add(b, c);
    }

    // Define Inner class DrawCanvas, which is a JPanel used for custom drawing
    class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);  // paint parent's background
            setBackground(Color.WHITE);
        }
    }

    // The entry main method
    public static void main(String[] args) {
        // Run GUI codes in Event-Dispatching thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameContainer(); // Let the constructor do the job
            }
        });
    }
}
