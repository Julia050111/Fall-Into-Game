import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

// ★ 1. 匯入圖片工具
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Monster {
    public double x, y;
    public int maxHp = 150; 
    public int hp = maxHp;  
    public final int size = 20;
    public double speed = 1.5;

    // ★ 2. 宣告圖片變數
    public Image minionImage;

    public Monster(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        
        // ★ 3. 嘗試讀取小怪圖片
        try {
            minionImage = ImageIO.read(new File("minion.png"));
        } catch (IOException e) {
            System.out.println("第二關找不到 minion.png，將使用預設紫色圓形");
        }
    }

    public boolean isDead() { return hp <= 0; }

    public void update(Tower targetTower) {
        if (targetTower == null) return;
        double dx = targetTower.x + (targetTower.size/2.0) - (this.x + size/2.0);
        double dy = targetTower.y + (targetTower.size/2.0) - (this.y + size/2.0);
        double distance = Math.hypot(dx, dy);

        if (distance > 0) {
            this.x += (dx / distance) * speed;
            this.y += (dy / distance) * speed;
        }
    }

    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, size, size); }

    public void draw(Graphics2D g2) {
        // ★ 4. 畫圖邏輯：有圖片畫圖片，沒圖片畫圓形
        if (minionImage != null) {
            g2.drawImage(minionImage, (int)x, (int)y, size, size, null);
        } else {
            g2.setColor(Color.MAGENTA); 
            g2.fillOval((int)x, (int)y, size, size);
        }
        
        // 畫血條 (血條要畫在圖片下面，這樣才不會被圖片蓋住)
        g2.setColor(Color.GREEN);
        g2.fillRect((int)x, (int)y - 8, (int)(size * ((double)hp / maxHp)), 4);
    }
}