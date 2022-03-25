package com.example.pokemon2048;

import java.awt.image.BufferedImage;

public class PokemonCell {
	// x,y refers to coordinates for each time frame.
	private int pokemonId, x, y;
    private BufferedImage image; 

    private int rowIndex, colIndex;

    public PokemonCell(int r, int c, int id){
        this.rowIndex = r;
        this.colIndex = c;
        this.pokemonId = id;
    }

	public PokemonCell(int x, int y, int r, int c,  int id, BufferedImage img){
		this.x = x; // x is for column 
		this.y = y; // y is for row
		this.pokemonId = id;
		this.image = img;
        this.rowIndex = r;
        this.colIndex = c;
	}

	// SETTERS.
	public void setX(int x){ this.x = x; }
	public void setY(int y){ this.y = y; }
	public void setImage(BufferedImage image){ this.image = image; }
	public void setId(int id){ this.pokemonId = id; }
	public void setR(int r){ this.rowIndex = r; }
	public void setC(int c){ this.colIndex = c; }

	// GETTERS.
	public BufferedImage getImage(){ return this.image; }
	public int getX(){ return this.x; }
	public int getY(){ return this.y; }
	public int getId(){ return this.pokemonId; }
	public int getR(){ return this.rowIndex; }
	public int getC(){ return this.colIndex; }
}
