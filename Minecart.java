import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;

// ★ 新增圖片匯入工具
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Minecart {
    public int x, y; 
    public int destX, destY; 
    public final int size = 80; 
    public boolean isMoving = false; 

    // ★ 新增：宣告兩個圖片變數
    public Image imgLevel1;
    public Image imgLevel2;

    public Minecart(int startX, int startY, int destX, int destY) {
        this.x = startX;
        this.y = startY;
        this.destX = destX;
        this.destY = destY;
        
        // ★ 新增：讀取兩張不同的礦車圖片 (雙保險設計)
        try {
            imgLevel1 = ImageIO.read(new File("minecart_1.png"));
        } catch (IOException e) {
            System.out.println("找不到 minecart1.png，第一關將使用橘色方塊");
        }
        
        try {
            imgLevel2 = ImageIO.read(new File("minecart_2.png"));
        } catch (IOException e) {
            System.out.println("找不到 minecart2.png，第二關將使用橘色方塊");
        }
    }

    public void update(ArrayList<Track> tracks) {
        if (!isMoving) return;

        int speed = 1; 

        if (Math.abs(x - destX) <= speed && Math.abs(y - destY) <= speed) {
            x = destX;
            y = destY;
            isMoving = false;
            return;
        }

        int dirX = 0;
        int dirY = 0;
        if (x < destX) dirX = 1;
        if (x > destX) dirX = -1;
        if (y < destY) dirY = 1;
        if (y > destY) dirY = -1;

        boolean canMoveX = false;
        boolean canMoveY = false;

        if (dirX != 0) {
            Rectangle nextRectX = new Rectangle(x + (dirX * speed), y, size, size);
            if (isOnTrack(nextRectX, tracks)) canMoveX = true;
        }
        
        if (dirY != 0) {
            Rectangle nextRectY = new Rectangle(x, y + (dirY * speed), size, size);
            if (isOnTrack(nextRectY, tracks)) canMoveY = true;
        }

        if (canMoveX && canMoveY) {
            if (Math.abs(destX - x) > Math.abs(destY - y)) {
                x += dirX * speed;
            } else {
                y += dirY * speed;
            }
        } else if (canMoveX) {
            x += dirX * speed;
        } else if (canMoveY) {
            y += dirY * speed;
        } else {
            isMoving = false; 
        }
    }

    private boolean isOnTrack(Rectangle rect, ArrayList<Track> tracks) {
        int centerX = rect.x + rect.width / 2;
        int centerY = rect.y + rect.height / 2;
        
        Rectangle destArea = new Rectangle(destX - 5, destY - 5, 50, 50); 
        if (destArea.contains(centerX, centerY)) return true;

        for (Track t : tracks) {
            if (t.getBounds().contains(centerX, centerY)) {
                return true;
            }
        }
        return false;
    }

    // ★ 修改：接收 currentLevel 作為參數
    public void draw(Graphics2D g2, int currentLevel) {
        // 畫出目的地 (綠色方塊)
        g2.setColor(Color.GREEN);
        g2.drawRect(destX - 5, destY - 5, 50, 50); 
        g2.setColor(new Color(0, 255, 0, 50)); 
        g2.fillRect(destX - 5, destY - 5, 50, 50);

        // ★ 新增：根據關卡決定畫哪一張圖
        if (currentLevel == 1) {
            if (imgLevel1 != null) {
                g2.drawImage(imgLevel1, x, y, size, size, null);
            } else {
                g2.setColor(Color.ORANGE);
                g2.fillRect(x, y, size, size);
            }
        } else {
            // 第二關 (或之後) 使用這張圖
            if (imgLevel2 != null) {
                g2.drawImage(imgLevel2, x, y, size, size, null);
            } else {
                g2.setColor(Color.ORANGE);
                g2.fillRect(x, y, size, size);
            }
        }
    }
}