import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.awt.Image;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.FontMetrics;

public class GamePanel extends JPanel {

    final int screenWidth = 800;
    final int screenHeight = 600;

    KeyHandler keyH = new KeyHandler();
    Player player = new Player(100, 100);
    Minecart minecart = new Minecart(155, 155, 605, 455);
    
    ArrayList<IronBlock> blocks = new ArrayList<>();
    ArrayList<Track> tracks = new ArrayList<>(); // ★ 新增：存放玩家鋪設的鐵軌
    
    int placeCooldown = 0; // 防止長按空白鍵瞬間把鐵塊扣光
    int currentLevel = 0; // 1 = 鋪鐵軌, 2 = 反向塔防
    int minecartHp = 100; // 第二關礦車的血量
    boolean gameOver = false; // ★ 新增：判斷是否遊戲結束
    double energy = 100;
    boolean showingIntro = false;
    
    ArrayList<Tower> towers = new ArrayList<>();
    ArrayList<Monster> monsters = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();

    int playerHp = 100; // 第三關玩家血量
    boolean gameWon = false; // 勝利狀態
    
    ArrayList<Enemy> enemies = new ArrayList<>();
    Castle castle;
    // ★ 新增：第三關專用的敵人子彈陣列
    ArrayList<Bullet> enemyBullets = new ArrayList<>();
    boolean spaceLocked = false;
    Image bossBgImage; 
    Image level1BgImage; // ★ 新增：第一關的背景圖片

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true); 
        this.addKeyListener(keyH);
        this.setFocusable(true); 

        generateIronBlocks(25); // 增加鐵塊數量，讓玩家夠鋪鐵軌
        startGameTimer();
        tracks.add(new Track(150, 150)); 
        
        generateIronBlocks(25); 
        startGameTimer();
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();

                // 檢查滑鼠是否點擊在中央的按鈕範圍 (X:300~500, Y:400~450)
                boolean clickedButton = (mx >= 300 && mx <= 500 && my >= 400 && my <= 450);

                // 1. 在標題畫面點擊「Start Game」
                if (currentLevel == 0 && clickedButton) {
                    currentLevel = 1;      
                    showingIntro = true;   // 進入第一關的介紹畫面
                    return;
                }

                // 2. 在關卡介紹畫面點擊「PLAY」
                if (showingIntro && clickedButton) {
                    showingIntro = false; // 關閉介紹，解鎖遊戲時間！
                    
                    // 如果是第2關或第3關，在這裡呼叫關卡重置，確保怪物和塔正確生成
                    if (currentLevel == 2) startLevel2();
                    if (currentLevel == 3) startLevel3();
                    return;
                }

                // 3. 原本的：第二關遊戲進行中，點擊放小兵 (必須確定不是在說明畫面才能放)
                if (currentLevel == 2 && !showingIntro && energy >= 20 && !gameOver) {
                    int spawnX = minecart.x + minecart.size / 2;
                    int spawnY = minecart.y + minecart.size / 2;
                    monsters.add(new Monster(spawnX, spawnY));
                    energy -= 5; 
                }
            }
        });
        
        // 為了測試，預先生成幾座塔在路線周圍
        towers.add(new Tower(300, 100));
        towers.add(new Tower(500, 350));
        try {
            // 請把你的背景圖片命名為 "boss.png" 並放在與 Main.java 同一個資料夾下
            bossBgImage = ImageIO.read(new File("boss.png"));
        } catch (IOException e) {
            System.out.println("找不到 boss.png，將啟動預設的暗紅色 Boss 背景！");
        }
        try {
            level1BgImage = ImageIO.read(new File("hell.png"));
        } catch (IOException e) {
            System.out.println("找不到 hell.png，第一關將使用預設的黑色背景！");
        }
        SpotifyManager.openPlaylist();
    }

    public void generateIronBlocks(int amount) {
        Random random = new Random();
        for (int i = 0; i < amount; i++) {
            int randomX = random.nextInt(screenWidth - 50) + 25;
            int randomY = random.nextInt(screenHeight - 50) + 25;
            blocks.add(new IronBlock(randomX, randomY));
        }
    }

    public void startGameTimer() {
        Timer timer = new Timer(16, e -> {
            update();
            repaint();
        });
        timer.start();
    }

    public void update() {
        if (currentLevel == 0 || showingIntro) {
            return;
        }

        if (currentLevel == 1) updateLevel1();
        else if (currentLevel == 2) updateLevel2();
        else if (currentLevel == 3) updateLevel3();
    }

    public void updateLevel1() {
        // 1. 玩家更新與收集鐵塊
        player.update(keyH, screenWidth, screenHeight);

        for (int i = 0; i < blocks.size(); i++) {
            IronBlock block = blocks.get(i);
            if (player.getBounds().intersects(block.getBounds())) {
                blocks.remove(i); 
                player.ironCount++; 
                break; 
            }
        }

        // 2. ★ 鋪設鐵軌邏輯 (按下空白鍵 + 身上有鐵塊 + 冷卻時間結束)
        if (keyH.spacePressed && player.ironCount > 0 && placeCooldown <= 0) {
            // 網格對齊計算 (讓鐵軌完美切齊 50x50 的格子)
            int trackX = (player.x / 50) * 50;
            int trackY = (player.y / 50) * 50;
            
            // 檢查該位置是否已經有鐵軌了，避免重複鋪設浪費材料
            boolean exists = false;
            for (Track t : tracks) {
                if (t.x == trackX && t.y == trackY) {
                    exists = true; break;
                }
            }
            
            if (!exists) {
                tracks.add(new Track(trackX, trackY));
                player.ironCount--;
                placeCooldown = 15; // 進入冷卻 (約 0.25 秒)
            }
        }
        if (placeCooldown > 0) placeCooldown--; // 冷卻時間遞減

        // 3. ★ 啟動礦車與礦車更新
        if (keyH.enterPressed) {
            minecart.isMoving = true;
        }
        minecart.update(tracks);
        if (Math.abs(minecart.x - minecart.destX) <= 2 && Math.abs(minecart.y - minecart.destY) <= 2) {
            currentLevel = 2;
            showingIntro = true; // ★ 進入第二關介紹畫面
            return;
        }
    }

    private void startLevel2() {
        currentLevel = 2; 
        gameOver = false; // 重置遊戲狀態
        minecartHp = 100; // 恢復滿血
        energy = 100;
        
        // 如果玩家鐵塊花光了死掉，重來時給他一點低保，避免卡關
        if (player.ironCount < 5) {
            player.ironCount = 10; 
        }
        
        minecart.x = 100; 
        minecart.y = 100;
        minecart.destX = 700; 
        minecart.destY = 400;
        minecart.isMoving = true; 
        
        tracks.clear();
        for(int i = 100; i <= 400; i += 50) tracks.add(new Track(i, 100));
        for(int i = 150; i <= 400; i += 50) tracks.add(new Track(400, i));
        for(int i = 450; i <= 700; i += 50) tracks.add(new Track(i, 400));

        towers.clear();
        towers.add(new Tower(250, 200)); 
        towers.add(new Tower(500, 250)); 
        towers.add(new Tower(550, 500)); 
        
        monsters.clear(); // 清除場上殘留的怪物
        bullets.clear();  // 清除場上殘留的子彈
    }

    private void updateLevel2() {
        if (gameOver) {
            if (keyH.rPressed) startLevel2();
            return; 
        }

        minecart.update(tracks); 
        player.x = minecart.x + 8;  
        player.y = minecart.y - 25; 
        
        if (minecartHp <= 0) {
            gameOver = true;
            return;
        }
        // ★ 新增：檢查礦車是否成功抵達第二關的終點
        if (Math.abs(minecart.x - minecart.destX) <= 2 && Math.abs(minecart.y - minecart.destY) <= 2) {
            currentLevel = 3;
            showingIntro = true; // ★ 進入第三關介紹畫面
            return; 
        }

        // ★ 能量隨時間自動回復 (每幀回復 0.1，一秒 60 幀約回復 6 點能量)
        if (energy < 100) {
            energy += 0.1;
        }

        // 1. 小兵邏輯：找最近的塔去撞
        for (int i = monsters.size() - 1; i >= 0; i--) {
            Monster m = monsters.get(i);
            Tower target = null;
            double minDist = 9999;
            for (Tower t : towers) {
                double dist = Math.hypot(t.x - m.x, t.y - m.y);
                if (dist < minDist) { minDist = dist; target = t; }
            }
            m.update(target);
            
            if (target != null && m.getBounds().intersects(target.getBounds())) {
                target.hp -= 10;
                m.hp = 0; 
            }
            if (m.isDead()) monsters.remove(i);
        }

        // 2. ★ 塔的邏輯：智慧索敵 (找最近的小兵或礦車)
        for (Tower t : towers) {
            t.cooldown--;
            if (t.cooldown <= 0) {
                
                // 預設目標是礦車
                double targetX = minecart.x + minecart.size / 2.0;
                double targetY = minecart.y + minecart.size / 2.0;
                double minDist = Math.hypot(t.x - targetX, t.y - targetY);
                
                // 掃描場上所有小兵，如果小兵比礦車更近，就把目標換成小兵！
                for (Monster m : monsters) {
                    double distToMonster = Math.hypot(t.x - (m.x + m.size/2.0), t.y - (m.y + m.size/2.0));
                    if (distToMonster < minDist) {
                        minDist = distToMonster;
                        targetX = m.x + m.size/2.0;
                        targetY = m.y + m.size/2.0;
                    }
                }
                
                // 如果最近的目標在射程內，就開火
                if (minDist < t.attackRange) {
                    bullets.add(new Bullet(t.x + 20, t.y + 20, (int)targetX, (int)targetY));
                    t.cooldown = 45; // 攻擊頻率稍微調快一點點
                }
            }
        }

        // 3. ★ 子彈邏輯：新增打到小兵會消失的判定
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update();
            boolean bulletDestroyed = false;
            
            // 檢查是否打中礦車
            if (b.getBounds().intersects(new Rectangle(minecart.x, minecart.y, minecart.size, minecart.size))) {
                minecartHp -= 5;
                bulletDestroyed = true;
            }
            
            // 檢查是否打中小兵 (如果還沒被打掉的話)
            if (!bulletDestroyed) {
                for (int j = monsters.size() - 1; j >= 0; j--) {
                    Monster m = monsters.get(j);
                    if (b.getBounds().intersects(m.getBounds())) {
                        m.hp -= 15; // 小兵扣血 (假設小兵血量30，兩發死)
                        bulletDestroyed = true;
                        if (m.isDead()) monsters.remove(j);
                        break; 
                    }
                }
            }
            
            // 如果子彈打到東西或飛出螢幕，就移除
            if (bulletDestroyed || b.x < 0 || b.x > screenWidth || b.y < 0 || b.y > screenHeight) {
                bullets.remove(i);
            }
        }
        
        towers.removeIf(Tower::isDead);
    }

    private void startLevel3() {
        currentLevel = 3;
        gameOver = false;
        gameWon = false;
        playerHp = 100;
        
        // 玩家放在畫面正中央
        player.x = screenWidth / 2;
        player.y = screenHeight / 2;
        
        castle = new Castle(200, 200); // 城堡初始位置
        
        enemies.clear();
        enemyBullets.clear(); // ★ 確保重新開始時子彈清空
        enemies.add(new Enemy(700, 100, true));  // 一隻 Boss
        enemies.add(new Enemy(100, 500, false)); // 三隻小怪
        enemies.add(new Enemy(700, 500, false));
        enemies.add(new Enemy(400, 550, false));
    }

    private void updateLevel3() {
        if (gameOver || gameWon) {
            if (keyH.rPressed) startLevel3(); // 按 R 重新挑戰 Boss
            return;
        }

        player.update(keyH, screenWidth, screenHeight);
        double pCenterX = player.x + player.width / 2.0;
        double pCenterY = player.y + player.height / 2.0;

        // ★ 城堡連線邏輯 (判斷距離與空白鍵)
        if (keyH.spacePressed) {
            if (!spaceLocked) { // 確保按一次只判定一次
                // 用中心點算距離比較精準
                double distToCastle = Math.hypot((castle.x + castle.size/2.0) - pCenterX, (castle.y + castle.size/2.0) - pCenterY);
                
                if (!castle.isConnected && distToCastle < 220) {
                    castle.startConnection(pCenterX, pCenterY); // 進入範圍就連線
                } else if (castle.isConnected) {
                    castle.isConnected = false; // 已經連線的話就斷開
                }
                spaceLocked = true; // 上鎖
            }
        } else {
            spaceLocked = false; // 放開空白鍵時解鎖
        }
        castle.update(pCenterX, pCenterY);

        // 敵人邏輯與碰撞
        boolean bossIsDead = true; 
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(player.x, player.y);
            
            if (e.isBoss && !e.isDead()) bossIsDead = false;

            // 城堡撞到敵人 (造成傷害)
            if (castle.isConnected && castle.getBounds().intersects(e.getBounds())) {
                if (e.hitCooldown <= 0) {
                    e.hp -= 50;
                    e.hitCooldown = 15; 
                }
            }

            // ★ 新增：怪物的射擊邏輯 (取代原本的碰撞扣血)
            e.attackCooldown--;
            if (e.attackCooldown <= 0) {
                // 判斷是 Boss 還是小怪，給予不同的子彈外觀
                Color bColor = e.isBoss ? Color.RED : Color.ORANGE;
                int bSize = e.isBoss ? 16 : 10; // Boss 子彈比較大
                double bSpeed = e.isBoss ? 3.5 : 4.0;
                
                // 發射子彈瞄準玩家
                enemyBullets.add(new Bullet(
                    (int)e.x + e.size/2, (int)e.y + e.size/2, 
                    player.x + player.width/2, player.y + player.height/2,
                    e.bulletDamage, bColor, bSize, bSpeed
                ));
                
                e.attackCooldown = e.maxCooldown; // 重置緩衝時間
            }

            if (e.isDead()) enemies.remove(i);
        }

        // ★ 新增：處理敵人的子彈
        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            b.update();
            
            // 子彈打中玩家
            if (b.getBounds().intersects(player.getBounds())) {
                playerHp -= b.damage; // 依照子彈設定的攻擊力扣血
                enemyBullets.remove(i);
                if (playerHp <= 0) gameOver = true;
            } 
            // 如果子彈飛出螢幕邊界就自動消除，節省效能
            else if (b.x < 0 || b.x > screenWidth || b.y < 0 || b.y > screenHeight) {
                enemyBullets.remove(i);
            }
        }

        // 檢查勝利條件
        if (bossIsDead) gameWon = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D g2 = (Graphics2D) g;

        // ★ 1. 畫標題畫面
        if (currentLevel == 0) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            g2.drawString("Fall Into Game", 180, 200); // 你的遊戲名稱

            drawButton(g2, "Start Game", 300, 400); // 畫出開始按鈕
            return; // 畫完標題直接結束，不畫後面的遊戲內容
        }

        // ★ 2. 畫各關卡的說明畫面
        if (showingIntro) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);
            g2.setColor(Color.WHITE);
            
            // ★ 修改：標題改用微軟正黑體
            g2.setFont(new Font("微軟正黑體", Font.BOLD, 40));

            if (currentLevel == 1) {
                g2.drawString("Level 1: 資源收集", 230, 150);
                // ★ 修改：內文改用微軟正黑體
                g2.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
                g2.drawString("操作方向鍵移動，收集地上的鐵塊。", 230, 250);
                g2.drawString("讓礦車安全抵達右下角的終點。", 230, 290);
            } else if (currentLevel == 2) {
                g2.drawString("Level 2: 塔防護衛", 230, 150);
                g2.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
                g2.drawString("消耗能量點擊滑鼠，召喚小兵當作肉盾。", 220, 250);
                g2.drawString("保護礦車不要被防禦塔摧毀！", 250, 290);
            } else if (currentLevel == 3) {
                g2.drawString("Level 3: 最終決戰", 230, 150);
                g2.setFont(new Font("微軟正黑體", Font.PLAIN, 20));
                g2.drawString("在城堡附近按下空白鍵進行連線鎖定。", 220, 250);
                g2.drawString("甩動城堡砸碎魔王，躲避無情的彈幕！", 220, 290);
            }

            drawButton(g2, "PLAY", 300, 400); 
            return;
        }

        if (currentLevel == 1 && level1BgImage != null) {
            g2.drawImage(level1BgImage, 0, 0, screenWidth, screenHeight, null);
        }

        // 畫鐵軌與礦車 (共用)
        for (Track t : tracks) t.draw(g2);
        minecart.draw(g2, currentLevel);
        
        if (currentLevel == 1) {
            // 第一關畫玩家與鐵塊
            for (IronBlock block : blocks) block.draw(g2);
            player.draw(g2);
            
            g2.setColor(Color.WHITE);
            g2.drawString("Level 1 - Iron: " + player.ironCount, 20, 30);
            
        } else if (currentLevel == 2) {
            for (Tower t : towers) t.draw(g2);
            for (Monster m : monsters) m.draw(g2);
            for (Bullet b : bullets) b.draw(g2);
            player.draw(g2);
            
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Minecart HP: " + minecartHp, 20, 30);
            
            g2.setColor(Color.CYAN);
            g2.drawString("Energy: " + (int)energy + "/100", 20, 60);
            g2.drawRect(20, 70, 100, 10); 
            g2.fillRect(20, 70, (int)energy, 10); 
            
            g2.setColor(Color.WHITE);
            g2.drawString("Click to spawn Minion (Cost: 20 Energy)", 20, 100);

            // ★ 將第二關的 Game Over 畫面移到這裡
            if (gameOver) {
                g2.setColor(new Color(0, 0, 0, 150)); 
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(Color.RED);
                g2.setFont(new Font("Arial", Font.BOLD, 60));
                g2.drawString("GAME OVER", 200, 250);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 30));
                g2.drawString("Press 'R' to Restart", 250, 320);
            }

        } else if (currentLevel == 3) {
            if (bossBgImage != null) {
                // 如果有讀取到圖片，就把圖片拉伸到符合視窗大小並畫出來
                g2.drawImage(bossBgImage, 0, 0, screenWidth, screenHeight, null);
            } else {
                // 如果沒有放圖片，就自動畫一個有壓迫感的「暗紅色地磚」場景
                g2.setColor(new Color(40, 0, 0)); // 深暗紅色底
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setColor(new Color(80, 0, 0)); // 亮一點的紅線
                for (int i = 0; i < screenWidth; i += 50) g2.drawLine(i, 0, i, screenHeight);
                for (int i = 0; i < screenHeight; i += 50) g2.drawLine(0, i, screenWidth, i);
            }
            if (castle.isConnected) {
                g2.setColor(Color.WHITE);
                g2.drawLine(player.x + player.width/2, player.y + player.height/2, 
                           (int)castle.x + castle.size/2, (int)castle.y + castle.size/2);
            }
            
            castle.draw(g2);
            for (Enemy e : enemies) e.draw(g2, this);
            for (Bullet b : enemyBullets) b.draw(g2); 
            player.draw(g2);
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString("Player HP: " + playerHp, 20, 30);
            
            if (!castle.isConnected) {
                g2.setColor(Color.YELLOW);
                if (!castle.isConnected) {
                    g2.setColor(Color.YELLOW);
                    g2.drawString("Press SPACE near the Castle to link it!", 20, 60);
                } else {
                    g2.setColor(Color.GREEN);
                    g2.drawString("Castle Linked! Press SPACE to release.", 20, 60);
                }
            }

            // 第三關專屬的勝利與失敗畫面
            if (gameOver || gameWon) {
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(0, 0, screenWidth, screenHeight);
                g2.setFont(new Font("Arial", Font.BOLD, 60));
                
                if (gameWon) {
                    g2.setColor(Color.YELLOW);
                    g2.drawString("VICTORY!", 250, 250);
                } else {
                    g2.setColor(Color.RED);
                    g2.drawString("GAME OVER", 200, 250);
                }
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 30));
                g2.drawString("Press 'R' to Restart Boss Fight", 180, 320);
            }
        }
        
        // 最底下的 if (gameOver) 已經安全移除了！
        g2.dispose(); 
    }
    private void drawButton(Graphics2D g2, String text, int x, int y) {
        int width = 200;
        int height = 50;
        
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y, width, height); 
        g2.setColor(Color.WHITE);
        g2.drawRect(x, y, width, height); 

        // ★ 1. 改用支援中文的系統字體
        g2.setFont(new Font("微軟正黑體", Font.BOLD, 24)); 
        
        // ★ 2. 自動置中文字的魔法
        FontMetrics metrics = g2.getFontMetrics(g2.getFont());
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        
        g2.drawString(text, textX, textY); 
    }
}