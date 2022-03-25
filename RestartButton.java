package com.example.pokemon2048;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RestartButton extends JButton {
    
    private static final String BUTTON_NAME = "NEW GAME";

    private AnimateBlock ab;

    public RestartButton(AnimateBlock ab){
        super(BUTTON_NAME); 
        setPreferredSize(new Dimension(100, 40));
        setBackground(Color.GRAY);
        setFocusable(false); // IMPT.
        setOpaque(true);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("Arial", Font.BOLD, 10));
        setContentAreaFilled(false);

        this.ab = ab;
        this.addActionListener(new RestartButtonListener());
    }

    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            g.setColor(Color.lightGray);
        } else {
            g.setColor(getBackground());
        }
        g.fillRoundRect(0,  0, getSize().width - 1, getSize().height - 1, 5, 5);
        super.paintComponent(g);
    }

    private class RestartButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e){
            ab.newGame();
        }
    }
}
