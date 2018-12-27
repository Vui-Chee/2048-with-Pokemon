public class GameState {
   
    public static final int WIN_ID = 151;

    private int numMoves = 0;
    private int totalCombines = 0;
    private boolean gameEnds = false;
    private int highestId = 1;
    private Grid grid;

    public GameState(Grid grid){
        this.grid = grid;
    }

    // Getters
    public boolean hasGameEnded(){ return this.gameEnds; }
    public int getHighestId(){ return this.highestId; }
    public int getNumMoves(){ return this.numMoves; }
    public int getTotalCombines(){ return this.totalCombines; }
    public Grid getGrid(){ return this.grid; }

    // Setters
    public void setGameEnds(boolean ge){ this.gameEnds = ge; }
    public void setHighestId(int id){ this.highestId = id; }
    public void incrementMoves(){ this.numMoves++; }
    public void incrementCombines(){ this.totalCombines++; }

    public void resetGameState(){
        this.gameEnds = false;
        this.highestId = 1;
        this.numMoves = 0;
        this.totalCombines = 0;
    }

    public boolean checkLoseGame(){
        boolean lose = true;
        for (int i = 0;i < this.grid.getNumRows();i++){
            for (int j = 0;j < this.grid.getNumCols();j++){
                int i1 = i - 1;
                int i2 = i + 1;
                int j1 = j - 1;
                int j2 = j + 1;
                try {
                    int centreId = this.grid.getCell(i,j).getId();
                    if (i1 >= 0){
                        lose &= this.grid.getCell(i1,j).getId() != centreId;
                    }
                    if (i2 < this.grid.getNumRows()){
                        lose &= this.grid.getCell(i2,j).getId() != centreId;
                    }
                    if (j1 >= 0){
                        lose &= this.grid.getCell(i,j1).getId() != centreId;
                    }
                    if (j2 < this.grid.getNumCols()){
                        lose &= this.grid.getCell(i,j2).getId() != centreId;
                    }
                } catch (NullPointerException e){
                    System.out.println("GameState.java (checkLoseGame).");
                    return false;
                }
            }
        }
        return lose;
    }
}
