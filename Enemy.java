import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

// ★ 1. 匯入讀取 GIF 需要的兩個新工具
import javax.swing.ImageIcon;
import java.awt.image.ImageObserver;

public class Enemy {
    public double x, y;
    public int hp, maxHp;
    public int size;
    public double speed;
    public boolean isBoss;
    
    public int hitCooldown = 0; 
    public int attackCooldown; 
    public int maxCooldown;
    public int bulletDamage;

    public Image minionImage;
    // ★ 2. 新增一個裝 Boss 動畫的變數
    public Image bossGif; 

    public Enemy(int x, int y, boolean isBoss) {
        this.x = x;
        this.y = y;
        this.isBoss = isBoss;
        
        if (isBoss) {
            this.maxHp = 300;
            this.size = 120;
            this.speed = 0.5; 
            this.maxCooldown = 180; 
            this.bulletDamage = 5; 
            
            // ★ 3. Boss 使用 ImageIcon 來讀取 GIF (這樣動畫才會動)
            try {
                bossGif = new ImageIcon("boss.gif").getImage();
            } catch (Exception e) {
                System.out.println("第三關找不到 boss.gif");
            }
            
        } else {
            this.maxHp = 50;
            this.size = 30;
            this.speed = 1.0; 
            this.maxCooldown = 120;  
            this.bulletDamage = 2;  
            
            try {
                minionImage = ImageIO.read(new File("minion.png"));
            } catch (IOException e) {
                System.out.println("第三關找不到 minion.png");
            }
        }
        this.hp = maxHp;
        this.attackCooldown = maxCooldown; 
    }

    // ... (維持原本的 update 和 isDead, getBounds 方法)
    public void update(double playerX, double playerY) {
        if (hitCooldown > 0) hitCooldown--;
        double dx = playerX - x;
        double dy = playerY - y;
        double distance = Math.hypot(dx, dy);
        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }

    public boolean isDead() { return hp <= 0; }
    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, size, size); }

    // ★ 4. 修改 draw 方法，增加 ImageObserver 參數
    public void draw(Graphics2D g2, ImageObserver observer) {
        
        if (isBoss) {
            if (bossGif != null) {
                // ★ Boss 畫圖時，把 null 換成傳進來的 observer，GIF 才會更新！
                g2.drawImage(bossGif, (int)x, (int)y, size, size, observer);
            } else {
                g2.setColor(Color.RED);
                g2.fillRect((int)x, (int)y, size, size);
            }
        } else {
            if (minionImage != null) {
                // 小怪還是靜態圖，所以維持 null 沒關係
                g2.drawImage(minionImage, (int)x, (int)y, size, size, null);
            } else {
                g2.setColor(new Color(255, 100, 100));
                g2.fillRect((int)x, (int)y, size, size);
            }
        }

        // 畫血條
        g2.setColor(Color.GREEN);
        g2.fillRect((int)x, (int)y - 10, (int)(size * ((double)hp / maxHp)), 5);
    }
}