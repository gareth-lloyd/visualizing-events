package com.heychinaski.historyhack.experimental;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.JFrame;

public class TestStatefulBlob {

    /**
     * @param args
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setMinimumSize(new Dimension(1000, 1000));
        f.setBackground(Color.BLACK);
        
        StatefulBlob sb = new StatefulBlob(new Point(500, 500));
        BlobComponent bc = new BlobComponent(sb);
        bc.setVisible(true);
        f.add(bc);
        f.pack();
        f.setVisible(true);
        
        while (! sb.isRetired()) {
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            bc.repaint();
        }
    }

}

class BlobComponent extends Component {
    
    private StatefulBlob sb;

    public BlobComponent(StatefulBlob sb) {
        this.sb = sb;
    }
    
    public Dimension getPreferredSize(){
        return new Dimension(200, 200);
    }

    
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        sb.drawAndIncrementState(g2);
    }
    
}