import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.image.BufferedImage;


public class Grid extends JPanel {

    public static Color bgColor = Color.ORANGE;
    public static final int cellLength = 100;
    public static final int strokeSize = 5;

    private int rows, cols, width, height;
    private PokemonCell[][] cells;
   
    public Grid(int rows, int cols){
        this.width = cols * cellLength;
        this.height = rows * cellLength; 
        this.rows = rows;
        this.cols = cols;
        this.setBorder(BorderFactory.createLineBorder(Color.GRAY, strokeSize));
        this.setPreferredSize(new Dimension(this.width,this.height)); 
         // Keeps track of occupied cells.
        this.cells = new PokemonCell[this.rows][this.cols];
        // Need to set this otherwise key events will not registered.
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    // Getters.
    public int getNumRows(){ return this.rows; }
    public int getNumCols(){ return this.cols; }
    public PokemonCell getCell(int i, int j){ return this.cells[i][j]; }

    // Setters.
    public void setCell(int i, int j, PokemonCell c){
        this.cells[i][j] = c;
    }

    public void setCellPositionInGrid(int i, int j, int x, int y){
        this.cells[i][j].setX(x);
        this.cells[i][j].setY(y);
    }

    public void resetGridCells(){
        this.cells = new PokemonCell[this.rows][this.cols];
    }

    private void renderGrid(Graphics2D g2){
        for (int i = 0;i < rows;i++){
            for (int j = 0;j < cols;j++){
                g2.drawRect(i * cellLength, j * cellLength, cellLength, cellLength); 
            }
        }   
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        setBackground(bgColor);
		renderPokemonImages(g2);
        g2.setStroke(new BasicStroke(strokeSize));
        g2.setColor(Color.GRAY);
        renderGrid(g2);
    }

    private void renderPokemonImages(Graphics2D g2){
        for (int i = 0;i < this.rows;i++){
            for (int j = 0;j < this.cols;j++){
                if (this.cells[i][j] != null){
                    BufferedImage image = this.cells[i][j].getImage();
                    int x = this.cells[i][j].getX();
                    int y = this.cells[i][j].getY();
					g2.drawImage(image, x, y, this);
                }
            }
        }
    }
}// End of grid class
