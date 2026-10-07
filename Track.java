import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Track {
    public int x, y;
    public final int size = 50;

    public Track(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(new Color(139, 69, 19)); 
        g2.fillRect(x, y, size, size);
        
        // ★ 畫成「十字型」鐵軌，這樣不管是直走還是轉彎 L 型，視覺上都能完美接合！
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(x + 15, y, 20, size); // 直向鐵軌
        g2.fillRect(x, y + 15, size, 20); // 橫向鐵軌
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }
}