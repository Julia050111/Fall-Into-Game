import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// ★ 1. 匯入圖片工具
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Castle {
    public double x, y;
    public final int size = 120; // 城堡比較大顆
    public boolean isConnected = false;
    
    private double angle = 0;
    private final int orbitRadius = 150; 
    private final double rotationSpeed = 0.05; 

    // ★ 2. 宣告圖片變數
    public Image castleImage;

    public Castle(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        
        // ★ 3. 讀取跟第二關「一模一樣」的圖片檔案！
        try {
            castleImage = ImageIO.read(new File("castle.png"));
        } catch (IOException e) {
            System.out.println("第三關找不到 castle.png，將使用預設青色方塊");
        }
    }

    public void startConnection(double playerCenterX, double playerCenterY) {
        isConnected = true;
        angle = Math.atan2((y + size/2.0) - playerCenterY, (x + size/2.0) - playerCenterX);
    }

    public void update(double playerCenterX, double playerCenterY) {
        if (isConnected) {
            angle += rotationSpeed;
            if (angle > Math.PI * 2) angle -= Math.PI * 2;
            
            x = playerCenterX - size/2.0 + orbitRadius * Math.cos(angle);
            y = playerCenterY - size/2.0 + orbitRadius * Math.sin(angle);
        } else {
            x += Math.sin(System.currentTimeMillis() / 1000.0) * 0.5;
            y += Math.cos(System.currentTimeMillis() / 1000.0) * 0.5;
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, size, size); }

    public void draw(Graphics2D g2) {
        // ★ 4. 畫圖邏輯：把圖片貼上去
        if (castleImage != null) {
            g2.drawImage(castleImage, (int)x, (int)y, size, size, null);
        } else {
            g2.setColor(Color.CYAN); 
            g2.fillRect((int)x, (int)y, size, size);
        }
    }
}