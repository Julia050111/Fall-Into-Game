import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
// ★ 1. 匯入處理圖片需要的工具
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Player {
    
    public int x, y;
    public int speed;
    public final int width = 140; 
    public final int height = 120; 
    
    public int ironCount = 0; 
    
    // ★ 2. 宣告一個變數來裝玩家的圖片
    public Image playerImage;

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.speed = 5; 
        
        // ★ 3. 在誕生時嘗試讀取圖片
        try {
            // 先把檔案路徑存起來
            File imageFile = new File("player_2.png");
            
            // 印出 Java 實際上在尋找的「絕對路徑」
            System.out.println("【系統除錯】Java 正在這裡尋找圖片：" + imageFile.getAbsolutePath());
            
            // 檢查檔案到底存不存在
            if (imageFile.exists()) {
                System.out.println("【系統除錯】太棒了！檔案存在，準備讀取...");
                playerImage = ImageIO.read(imageFile);
            } else {
                System.out.println("【系統除錯】錯誤！在這個路徑下找不到圖片。");
            }
            
        } catch (IOException e) {
            System.out.println("【系統除錯】讀取失敗，原因：" + e.getMessage());
        }
    }

    public void update(KeyHandler keyH, int screenWidth, int screenHeight) {
        if (keyH.upPressed) { y -= speed; }
        if (keyH.downPressed) { y += speed; }
        if (keyH.leftPressed) { x -= speed; }
        if (keyH.rightPressed) { x += speed; }

        if (x < 0) { x = 0; }
        if (y < 0) { y = 0; }
        if (x + width > screenWidth) { x = screenWidth - width; }
        if (y + height > screenHeight) { y = screenHeight - height; }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g2) {
        // ★ 4. 畫圖邏輯：有圖片就畫圖片，沒有就畫白色方塊
        if (playerImage != null) {
            // drawImage 參數：(圖片, X, Y, 寬度, 高度, 觀察者(通常填 null))
            g2.drawImage(playerImage, x, y, width, height, null);
        } else {
            g2.setColor(Color.WHITE); 
            g2.fillRect(x, y, width, height);
        }
    }
}