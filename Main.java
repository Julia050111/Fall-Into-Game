import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Fall Into Game - 期末專題"); 

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack(); 
        window.setLocationRelativeTo(null); 
        window.setVisible(true); 
    }
}