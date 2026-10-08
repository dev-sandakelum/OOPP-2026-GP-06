package UI.Kit;

import javax.swing.*;

public class Page {
    private final JFrame jf;
    public Page(){
        jf = new JFrame();
    }

    public void createFrame(int width , int height ){
        jf.setSize(width , height);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setVisible(){
        jf.setVisible(true);
    }

}
