import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {
    public double x, y;
    public double dx, dy; 
    public int size = 8;
    public double speed = 5;
    public int damage = 5; // 預設傷害
    public Color color = Color.YELLOW; // 預設顏色

    // 給第二關防禦塔用的建構子 (維持原本的設計)
    public Bullet(int startX, int startY, int targetX, int targetY) {
        init(startX, startY, targetX, targetY);
    }

    // ★ 給第三關 Boss 與小怪用的進階建構子 (自訂傷害、顏色、大小、速度)
    public Bullet(int startX, int startY, int targetX, int targetY, int damage, Color color, int size, double speed) {
        this.damage = damage;
        this.color = color;
        this.size = size;
        this.speed = speed;
        init(startX, startY, targetX, targetY);
    }

    private void init(int startX, int startY, int targetX, int targetY) {
        this.x = startX;
        this.y = startY;
        double dist = Math.hypot(targetX - startX, targetY - startY);
        if (dist > 0) {
            this.dx = ((targetX - startX) / dist) * speed;
            this.dy = ((targetY - startY) / dist) * speed;
        }
    }

    public void update() {
        x += dx;
        y += dy;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, size, size);
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int)x, (int)y, size, size);
    }
}