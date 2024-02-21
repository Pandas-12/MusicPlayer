import java.net.URL;
import java.net.URLConnection;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

public class PlaySong {
    public static void main(String[] args) {
        Player player = null;
        String mp3Source = "https://freaks.dev/music/[MADE]_-_04_Let's_Not_Fall_In_Love_(우리사랑하지말아요).mp3";
        try {
            URLConnection urlConnection = new URL(mp3Source).openConnection();
            urlConnection.connect();
            player = new Player(urlConnection.getInputStream());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        try {
            player.play();
        } catch (JavaLayerException e) {
            System.out.println(e.getMessage());
        }
    }
}