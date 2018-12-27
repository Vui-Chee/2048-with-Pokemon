import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Stack;
import java.util.EmptyStackException;
import javax.swing.JOptionPane;

public class AnimateBlock implements KeyListener {
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}

    private static final int XSPEED = 25;
    private static final int YSPEED = 25;
    private static final int UPDATE_INTERVAL = 7;
    private static final int TOTAL_POKEMON = 151;
    private static final int FRAMES_PER_EXPANSION = 10;
    private static final int DUD_LIMIT = 10; // For every n combinations, remove all duds.
    private static final int DUD_ID = 152;  
    private static final int FULL_GRID = 65535; // Basically, 2^(#bits) - 1.

    private ExecutorService threadPool;
    private ReentrantLock lock;

    private boolean[][] spawnLocations; // including the new cell for each move.
    private BufferedImage[][] animationImages;
    private int locnMask = 0; 
    private int numCombines = 0;
    private int basePokemonId = 1; // Start with Bulbasaur.
    private boolean stillCalcMove = false;

    private GameState gs;
    private Grid grid; 
    private ScorePanel sp;

    public void newGame(){
        this.spawnLocations = new boolean[this.grid.getNumRows()][this.grid.getNumCols()];
        this.locnMask = 0;
        this.numCombines = 0;
        this.basePokemonId = 1;
        this.stillCalcMove = false;
        this.grid.resetGridCells();
        // Reset game state.
        this.gs.resetGameState();
        sp.updateScore(0); // Score starts at 0 again.
        sp.setStage(1); // Reset stage to 1.
        spawnTwoCells(); // Spawn 2 cells again.
    }

    public AnimateBlock(GameState initialGS, ScorePanel sp){
        this.threadPool = Executors.newFixedThreadPool(1);
        this.lock = new ReentrantLock();
        this.gs = initialGS;
        this.grid = initialGS.getGrid();
        this.sp = sp;
        this.spawnLocations = new boolean[this.grid.getNumRows()][this.grid.getNumCols()];
        this.animationImages = ImageLoader.readyImages("animation-images");
        spawnTwoCells();
    }

    // Assumes the grid is completely empty.
    private void spawnTwoCells(){
        int i1, i2, j1, j2;
        ThreadLocalRandom t = ThreadLocalRandom.current();
        i1 = t.nextInt(0, this.grid.getNumRows());
        i2 = t.nextInt(0, this.grid.getNumRows());
        j1 = t.nextInt(0, this.grid.getNumCols());
        j2 = t.nextInt(0, this.grid.getNumCols());
        if (i1 == i2 && j1 == j2){
            int flipCoin = t.nextInt(0,1+1);
            if (flipCoin == 1){
                i2 = (i1 + t.nextInt(1, this.grid.getNumRows())) % this.grid.getNumRows();
            } else{
                j2 = (j1 + t.nextInt(1, this.grid.getNumCols())) % this.grid.getNumCols();
            }
        }
        // IMPT: Keep track of cells in grid.
        this.locnMask |= 1 << (i1 * this.grid.getNumCols() + j1);
        this.locnMask |= 1 << (i2 * this.grid.getNumCols() + j2);
        this.grid.setCell(i1, j1, new PokemonCell(i1, j1, this.basePokemonId));
        this.grid.setCell(i2, j2, new PokemonCell(i2, j2, this.basePokemonId));
        this.spawnLocations[i1][j1] = true;
        this.spawnLocations[i2][j2] = true;
        this.threadPool.execute(new Runnable(){
             public void run(){
                for (int i = 0;i < FRAMES_PER_EXPANSION;i++){
                    tempReplaceGridCells(i); // spawnLocn should be resetted afterwards.
                    grid.repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ignore){}
                }
            }           
        }); 
    }

    private void tempReplaceGridCells(int imageIndex){
        int i, j;
        for (i = 0;i < this.grid.getNumRows();i++){
            for (j = 0;j < this.grid.getNumCols();j++){
                if (this.spawnLocations[i][j]){
                    int pokemonId = this.grid.getCell(i,j).getId();
                    BufferedImage img = this.animationImages[pokemonId - 1][imageIndex];
                    if (imageIndex >= 9){
                        this.spawnLocations[i][j] = false;
                    }
                    this.grid.getCell(i,j).setImage(img);
                    // How about x,y relative to centre of each grid cell?
                    int newX = j * Grid.cellLength + (Grid.cellLength - img.getWidth()) / 2;
                    int newY = i * Grid.cellLength + (Grid.cellLength - img.getHeight()) / 2;
                    this.grid.setCellPositionInGrid(i, j, newX, newY);
                }
            }
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (gs.hasGameEnded()){ return; }
        // Execute when keycode is the direction keys.
        if (keyCode != KeyEvent.VK_RIGHT && keyCode != KeyEvent.VK_LEFT
            && keyCode != KeyEvent.VK_UP && keyCode != KeyEvent.VK_DOWN)
            return;
        // Prevent other threads from firing when one is still operating???
        if (stillCalcMove){ return; }
        threadPool.execute(new Runnable(){
            public void run(){
                try {
                    lock.lock(); // Lock to ensure only one type of movement is managed at a time.
                    stillCalcMove = true; // Prevents user from spamming keys to update movement.
                    // Stops other threads which reach this point from further updates. 
                    if (gs.hasGameEnded()){ return; } 
                    ArrayList<Stack<PokemonCell>> oldCells = retrieveStacksFromGrid(keyCode);
                    ArrayList<Stack<PokemonCell>> newCells = new ArrayList<Stack<PokemonCell>>();
                    for (Stack<PokemonCell> oldStack : oldCells){
                        // NOTE: final row,col for each cell is calculated in getNewCells().
                        newCells.add(getNewCells(oldStack, keyCode));                         
                    }
                    // Once each stack gets called, the final pos should be in each grid cell.
                    while (true) {
                        boolean hasAllUpdated = updateCellPositions(keyCode);
                        grid.repaint(); 
                        if (hasAllUpdated) {
                            renewGridCells(newCells); 
                            for (int i = 0;i < FRAMES_PER_EXPANSION;i++){
                                tempReplaceGridCells(i);
                                grid.repaint();
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ignore){}
                            }
                            // NOTE: if Mew is caught, then the game should end.
                            if (!gs.hasGameEnded() && gs.getHighestId() == GameState.WIN_ID){
                                JOptionPane.showMessageDialog(null, "You WON !!!");
                                gs.setGameEnds(true);
                            } else if (!gs.hasGameEnded() && locnMask == FULL_GRID && gs.checkLoseGame()){
                                JOptionPane.showMessageDialog(null, "You lost the game.");
                                gs.setGameEnds(true);
                            }
                            /* sp.repaint(); // Update score panel. */
                            sp.updateScore(gs.getTotalCombines());
                            break;
                        }
                        try {
                            Thread.sleep(UPDATE_INTERVAL);  // milliseconds
                        } catch (InterruptedException ignore) {}
                    }
                } 
                finally {
                    stillCalcMove = false;
                    lock.unlock(); // IMPT unlock otherwise no further movements.
                }
            }
        });
    } // End of method.

    private ArrayList<PokemonCell> retrieveEmptyCells(){
        ArrayList<PokemonCell> emptyCells = new ArrayList<PokemonCell>();
        int i,j;
        for (i = 0;i < this.grid.getNumRows();i++){
            for (j = 0;j < this.grid.getNumCols();j++){
                // null == empty cell.
                if (this.grid.getCell(i,j) == null){
                    // Default pokemon is a bulbasaur. 
                    emptyCells.add(new PokemonCell(i,j,this.basePokemonId)); 
                }
            }
        }
        return emptyCells;
    }

    private int lastPositionOnDirection(int direction){
        if (direction == KeyEvent.VK_RIGHT){
            return this.grid.getNumCols() - 1; 
        } else if (direction == KeyEvent.VK_LEFT){
            return 0;
        } else if (direction == KeyEvent.VK_UP){
            return 0;
        } else if (direction == KeyEvent.VK_DOWN){
            return this.grid.getNumRows() - 1;
        } else {
            System.out.println("AnimateBlock.java (lastPositionOnDirection) error.");
            return -1;
        }
    }

    private void setNewPositionOnDirection(int pos , int direction, PokemonCell c){
        if (direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_LEFT){
            c.setC(pos);
        } else if (direction == KeyEvent.VK_UP || direction == KeyEvent.VK_DOWN){
            c.setR(pos);
        }
    }

    private void pushStackOnDirection(Stack<PokemonCell> stack, int direction, 
                    PokemonCell c, int latestPos, boolean isOldCell){
        // NOTE: max id is 151.
        int newId = (isOldCell || c.getId() >= (DUD_ID-1)) ? c.getId() : c.getId() + 1; 
        BufferedImage newImage = this.animationImages[newId - 1][FRAMES_PER_EXPANSION - 1];
        if (direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_LEFT){
            stack.push(new PokemonCell( latestPos * Grid.cellLength, 
                        c.getY(), c.getR(), latestPos, newId, newImage ));
            if (!isOldCell){
                this.spawnLocations[c.getR()][latestPos] = true;
            }
        } else if (direction == KeyEvent.VK_UP || direction == KeyEvent.VK_DOWN){
            stack.push(new PokemonCell( c.getX(), latestPos * Grid.cellLength, 
                                    latestPos, c.getC(), newId, newImage ));
            if (!isOldCell){
                this.spawnLocations[latestPos][c.getC()] = true;
            }
        } 
    }

    private Stack<PokemonCell> getNewCells(Stack<PokemonCell> stack, int direction){
        Stack<PokemonCell> newCells = new Stack<PokemonCell>();
        int latestPos = lastPositionOnDirection(direction);
        while (!stack.empty()){
            try {
                PokemonCell c1 = stack.pop();
                try {
                    PokemonCell c2 = stack.pop();
                    // Dud cells do not combine.
                    if (c1.getId() == c2.getId() && c1.getId() != DUD_ID){
                        setNewPositionOnDirection(latestPos, direction, c1);
                        setNewPositionOnDirection(latestPos, direction, c2);
                        // setNewPositionOnDirection no affect since it sets other row or col.
                        pushStackOnDirection(newCells, direction, c1, latestPos,  false);
                    } else{
                        setNewPositionOnDirection(latestPos, direction, c1);
                        pushStackOnDirection(newCells, direction, c1, latestPos, true);
                        stack.push(c2);
                    }
                } catch (EmptyStackException e){
                    setNewPositionOnDirection(latestPos, direction, c1);
                    pushStackOnDirection(newCells, direction, c1, latestPos, true);
                }
            } catch(EmptyStackException e){
                System.out.println("AnimateBlock.java (getNewCells) : Empty stack.");
            }
            // IMPT: next cell or cells will get this new position.
            if (direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_DOWN){
                latestPos--;
            } else {
                latestPos++;
            }
        }
        return newCells;
    }

    // Each stack will contain the new pokemon cells at the end of each motion.
    private ArrayList<Stack<PokemonCell>> retrieveStacksFromGrid(int direction){
        boolean horizontal = ( direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_LEFT );
        int numStacks = horizontal ? this.grid.getNumRows() : this.grid.getNumCols();
        ArrayList<Stack<PokemonCell>> stacks = new ArrayList<Stack<PokemonCell>>(numStacks);
        for (int s = 0;s < numStacks;s++){
            stacks.add(new Stack<PokemonCell>());
        }
        int i, j, incr = 1;
        i = j = 0;
        if (direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_LEFT){
            incr = ( direction == KeyEvent.VK_RIGHT ) ? 1 : -1;
            int startJ = ( direction == KeyEvent.VK_RIGHT ) ? 0 : this.grid.getNumCols() - 1;
            for (i = 0; i < this.grid.getNumRows() ;i++){
                for (j = startJ ;j >= 0 && j < this.grid.getNumCols() ; j += incr){
                    if (this.grid.getCell(i,j) != null){
                        stacks.get(i).push(this.grid.getCell(i,j));
                    }
                }
            }
        } else if (direction == KeyEvent.VK_UP || direction == KeyEvent.VK_DOWN){
            incr = (direction == KeyEvent.VK_DOWN ) ? 1 : -1;
            int startI = ( direction == KeyEvent.VK_DOWN ) ? 0 : this.grid.getNumRows() - 1;
            for (j = 0; j < this.grid.getNumCols() ;j++){
                for (i = startI ; i >= 0 && i < this.grid.getNumRows() ; i += incr){
                    if (this.grid.getCell(i,j) != null){
                        stacks.get(j).push(this.grid.getCell(i,j));
                    }              
                }
            }
        }
        return stacks;
    }

    // x,y positions calculated for 1 frame.
    private boolean updateCellPositions(int direction){
        int i, j;
        i = j = 0;
        // NOTE: does not scale well for arbitrary-sized grids.
        int hasAllUpdated = 0;
        int checkMask = 0; // Should equal to hasAllUpdated when all updated.
        for (i = 0;i < this.grid.getNumRows();i++){
            for (j = 0;j < this.grid.getNumCols();j++){
                if (this.grid.getCell(i,j) != null){
                    checkMask |= 1 << (i * this.grid.getNumCols() + j); // Keep track of all old cells in grid. 
                    if (direction == KeyEvent.VK_RIGHT || direction == KeyEvent.VK_LEFT){
                        int finalX = this.grid.getCell(i,j).getC() * Grid.cellLength;
                        int currentX = this.grid.getCell(i,j).getX();
                        int incr = (direction == KeyEvent.VK_RIGHT) ? XSPEED : -XSPEED;
                        int diff = currentX + incr;
                        boolean willUpdate = (direction == KeyEvent.VK_RIGHT) ? (diff < finalX) : (diff >= finalX);
                        if ( willUpdate ){
                            this.grid.getCell(i,j).setX(currentX + incr);
                        } else {
                            // Cell should not move once it reaches final pos.
                            this.grid.getCell(i,j).setX(finalX);
                        }
                        if (direction == KeyEvent.VK_RIGHT && this.grid.getCell(i,j).getX() >= finalX){
                            // Updates are based on OLD i,j indices;
                            hasAllUpdated |= 1 << (i * this.grid.getNumCols() + j); 
                        }
                        if (direction == KeyEvent.VK_LEFT && this.grid.getCell(i,j).getX() <= finalX){
                            hasAllUpdated |= 1 << (i * this.grid.getNumCols() + j); 
                        }
                    } else if (direction == KeyEvent.VK_DOWN || direction == KeyEvent.VK_UP){
                        int finalY = this.grid.getCell(i,j).getR() * Grid.cellLength;
                        int currentY = this.grid.getCell(i,j).getY();
                        int incr = (direction == KeyEvent.VK_DOWN) ? YSPEED : -YSPEED;
                        int diff = currentY + incr;
                        boolean willUpdate = (direction == KeyEvent.VK_DOWN) ? (diff < finalY) : (diff >= finalY);
                        if ( willUpdate ){
                            this.grid.getCell(i,j).setY(currentY + incr);
                        } else {
                            // Cell should not move once it reaches final pos.
                            this.grid.getCell(i,j).setY(finalY);
                        }
                        if (direction == KeyEvent.VK_DOWN && this.grid.getCell(i,j).getY() >= finalY){
                            // Updates are based on OLD i,j indices;
                            hasAllUpdated |= 1 << (i * this.grid.getNumCols() + j); 
                        }
                        if (direction == KeyEvent.VK_UP && this.grid.getCell(i,j).getY() <= finalY){
                            hasAllUpdated |= 1 << (i * this.grid.getNumCols() + j); 
                        }                  
                    }
                }
            }
        }
        if ((hasAllUpdated ^ checkMask) == 0){
            return true;
        }
        return false;
    }

    // After calculating final indices for each old cell, new cells formed should 
    // have the new indices so direction is NOT needed here.
    private void renewGridCells(ArrayList<Stack<PokemonCell>> newCells){
        int i,j, newLocnMask = 0;
        boolean toResetBoard = false;
        this.grid.resetGridCells();
        for (Stack<PokemonCell> stack : newCells){
            while (!stack.empty()){
                PokemonCell c = stack.pop();
                // Keep track of highest id pokemon. NOTE: ids may be DUDs.
                if (c.getId() != DUD_ID && c.getId() > gs.getHighestId()){ gs.setHighestId(c.getId()); }
                // NOTE: Once id == 151, you win.
                if (c.getId() == (basePokemonId + 9)){
                    toResetBoard = true;
                }
                this.grid.setCell(c.getR(), c.getC(), c);
                newLocnMask |= 1 << (c.getR() * this.grid.getNumCols() + c.getC());
                if (this.spawnLocations[c.getR()][c.getC()]){
                    this.numCombines++; // locn points to new combined cells.
                    this.gs.incrementCombines(); // This is total combines NOT one used for duds.
                }
            }
        }
        if (toResetBoard){
            if (this.basePokemonId + 10 <= TOTAL_POKEMON){
                this.basePokemonId += 9;
            }
            // Reset cells and spawn location.
            this.spawnLocations = new boolean[this.grid.getNumRows()][this.grid.getNumCols()]; 
            this.grid.resetGridCells(); // OK. Seems to be updated along in Game State?
            spawnTwoCells();
            this.gs.incrementMoves(); // New move.
            this.sp.nextStage();
        }
        // When no new moves are made, no new cells shall be spawned.
        else if (newLocnMask != this.locnMask){
            ArrayList<PokemonCell> emptyCells = retrieveEmptyCells(); 
            ThreadLocalRandom t = ThreadLocalRandom.current();
            int randomIndex = t.nextInt(0,emptyCells.size());
            PokemonCell nextCell = emptyCells.get(randomIndex);
            // Change id to 152 for dud block with probability p.
            if (this.numCombines >= DUD_LIMIT){
                // Next spawn is normal and clear all duds.
                clearDuds();
            } else{
                // Spawn dud with probability p.
                int randInt = t.nextInt(1,30+1);
                if (randInt == 30){
                    nextCell.setId(DUD_ID);
                }
            }
            // IMPT: new spawn must be added to mask.
            int newSpawnIndex = 1 << (nextCell.getR() * this.grid.getNumCols() + nextCell.getC());
            this.locnMask = newLocnMask | newSpawnIndex; // Remember old location.
            this.grid.setCell(nextCell.getR(), nextCell.getC(), nextCell);
            this.spawnLocations[nextCell.getR()][nextCell.getC()] = true;
            emptyCells.clear(); // Get all unused cells.
            this.gs.incrementMoves(); // New move.
        } 
    }

    private void clearDuds(){
        for (int i = 0;i < this.grid.getNumRows();i++){
            for (int j = 0;j < this.grid.getNumCols();j++){
                // All other dud cells except those whose cell is used to spawn the next cell.
                if (this.grid.getCell(i,j) != null && this.grid.getCell(i,j).getId() == DUD_ID 
                        && !this.spawnLocations[i][j]){
                    this.grid.setCell(i,j,null);
                }
            }
        }  
        this.numCombines = 0; // reset counter.
    }
}// End of class
