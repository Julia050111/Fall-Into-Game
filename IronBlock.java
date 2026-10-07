import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// ★ 新增圖片匯入工具
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class IronBlock {
    public int x, y;
    public final int size = 30; // 鐵礦的大小
    
    // ★ 宣告圖片變數
    public Image ironImage;

    public IronBlock(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        
        // ★ 嘗試讀取鐵礦圖片
        try {
            ironImage = ImageIO.read(new File("iron.png"));
        } catch (IOException e) {
            System.out.println("找不到 iron.png，將使用預設的灰色方塊");
        }
    }

    public void draw(Graphics2D g2) {
        // ★ 畫圖邏輯：有圖片就畫圖片，沒有就畫灰色方塊
        if (ironImage != null) {
            g2.drawImage(ironImage, x, y, size, size, null);
        } else {
            g2.setColor(Color.LIGHT_GRAY); 
            g2.fillRect(x, y, size, size);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}