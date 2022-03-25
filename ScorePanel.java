package com.example.pokemon2048;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.BorderFactory;

public class ScorePanel extends JPanel{

    private static final int  HEIGHT = 100;
    private static final int WIDTH = 100;
    private static final Color PANEL_BGCOLOR = Color.GRAY; 
    private static final int SCORE_MULTIPLIER = 10;
	private static final int MAX_STAGES = 17;  

    private int totalCombines = 0;
	private int stage = 1;

    public ScorePanel(){
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));       
    }

    public int getTotalScore(){ return this.totalCombines * SCORE_MULTIPLIER; }

	public void nextStage(){
		// Current stage exceeded max stage.
		if (this.stage >= MAX_STAGES){
			return;
		}
		this.stage++;
		repaint(); // Update stage.
	}

    public void updateScore(int newTotalCombines){
        this.totalCombines = newTotalCombines;
        repaint();
    }

	public void setStage(int s){ 
		this.stage = s; 
		repaint();	
	}

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        setBackground(PANEL_BGCOLOR);
        Font font = new Font("Verdana", Font.BOLD, 20);
        Graphics2D g2 = (Graphics2D) g;
        // Score.
        g2.setFont(font);
        g2.setColor(Color.WHITE);
        g2.drawString(Integer.toString(getTotalScore()), 10, 80);
        // Panel Title.
        Font titleFont = new Font("Comic Sans MS", Font.BOLD, 24);
        g2.setFont(titleFont);
        g2.setColor(Color.WHITE);
        g2.drawString("SCORE", 10, 30);
		// Stage number.
        Font stageFont = new Font("Comic Sans MS", Font.BOLD, 12);
        g2.setFont(stageFont);
        g2.setColor(Color.WHITE);
        g2.drawString("Stage " + Integer.toString(this.stage), 10, 55);
    }
}
