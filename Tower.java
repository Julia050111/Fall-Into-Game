import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Tower {
    public int x, y;
    public int size = 60; 
    public int hp = 100;
    public int cooldown = 0;
    public int attackRange = 150;
    
    // 宣告圖片變數
    public Image towerImage;

    public Tower(int x, int y) {
        this.x = x;
        this.y = y;
        
        // 在建構子嘗試讀取圖片
        try {
            towerImage = ImageIO.read(new File("castle.png"));
        } catch (IOException e) {
            System.out.println("第二關找不到 castle.png，將使用預設紅色方塊");
        }
    }

    // ★ 補回被刪掉的死亡判定方法
    public boolean isDead() {
        return hp <= 0;
    }

    // ★ 補回被刪掉的碰撞範圍判定方法
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public void draw(Graphics2D g2) {
        // 畫圖邏輯：有圖片畫圖片，沒圖片畫色塊
        if (towerImage != null) {
            g2.drawImage(towerImage, x, y, size, size, null);
        } else {
            g2.setColor(Color.RED); 
            g2.fillRect(x, y, size, size);
        }
        
        // 畫血條 (確保血條畫在圖片上方)
        g2.setColor(Color.GREEN);
        g2.fillRect(x, y - 8, (int)(size * ((double)hp / 100)), 4); 
    }
}