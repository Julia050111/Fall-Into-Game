import java.awt.Desktop;
import java.net.URI;

public class SpotifyManager {
    public static void openPlaylist() {
        try {
            // 替換成你的 Spotify 歌單連結 (URI 格式可以直接喚醒 Spotify App)
            // 例如這是 Spotify 官方的 Gaming 歌單
            URI uri = new URI("https://open.spotify.com/playlist/4SkXUOGMKkdEE2vM43Z60z?si=REzs28PnRnGB5x0QbRbLQA"); 
            
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
                System.out.println("已在背景開啟 Spotify！");
            }
        } catch (Exception e) {
            System.out.println("無法開啟 Spotify 歌單。");
        }
    }
}