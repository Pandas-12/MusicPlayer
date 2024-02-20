import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import static com.mongodb.client.model.Filters.eq;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws IOException {
        MongoClientURI uri = new MongoClientURI("mongodb://root:byteburst@freaks.dev/?authSource=admin");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase db = mongoClient.getDatabase("audioDatabase");
        MongoCollection<org.bson.Document> users = db.getCollection("users");
        MongoCollection<org.bson.Document> songs = db.getCollection("songs");
        MongoCollection<org.bson.Document> playlists = db.getCollection("playlists");
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

        /*do {
            registerUser(users, userInput);
            System.out.print("Do you want to register another user? (1=Yes, 2=No): ");
        } while (Integer.parseInt(userInput.readLine()) != 2);

         */

        System.out.println("Enter the username of the user whose playlist you want to view: ");
        String username = userInput.readLine();
        if (!checkUser(username, users)) {
            System.out.println("Invalid username.");
            return;
        }
        System.out.println("Enter the password of the user whose playlist you want to view: ");
        String password = userInput.readLine();
        if (!checkPassword(username, password, users)) {
            System.out.println("Invalid password.");
            return;
        }
        System.out.println("Original playlist for " + username + ":");
        showPlaylist(username, playlists);

        System.out.println("Songs:");
        FindIterable<Document> results = songs.find();
        int i = 0;
        for (Document doc : results) {
            System.out.println((i++) + ". " + doc.getString("title") + " - " + doc.getString("artist"));
        }
        int request = 1;
        while (request!= 2) {
            addSong(username, playlists, songs, userInput);
            System.out.print("Do you want to add another song? (1=Yes, 2=No): ");
            request = Integer.parseInt(userInput.readLine());
        }

        mongoClient.close();
    }

    public static boolean checkUser(String username, MongoCollection<org.bson.Document> users) {
        Document result = users.find(eq("username", username)).first();
        return result != null;
    }

    public static boolean checkPassword(String username, String password, MongoCollection<org.bson.Document> users) {
        Document user = users.find(eq("username", username)).first();
        return user != null && password.equals(user.getString("password"));
    }

    public static void registerUser(MongoCollection<org.bson.Document> users, BufferedReader userInput) throws IOException {
        System.out.println("Register Process");
        System.out.print("Enter username: ");
        String userName = userInput.readLine();
        if (checkUser(userName, users)) {
            System.out.println("Cannot register. User already exist.");
        } else {
            System.out.print("Enter password: ");
            String userPassword = userInput.readLine();
            users.insertOne(new Document("username", userName).append("password", userPassword));
            System.out.println("Successfully registered.");
        }
    }

    public static void showPlaylist(String username, MongoCollection<org.bson.Document> playlists) {
        FindIterable<Document> results = playlists.find(eq("username", username));
        for (Document doc : results) {
            System.out.println(doc);
        }
    }

    public static int showSongs(MongoCollection<org.bson.Document> songs, BufferedReader userInput) throws IOException {
        System.out.println("Songs:");
        FindIterable<Document> results = songs.find();
        int i = 0;
        for (Document doc : results) {
            System.out.println((i++) + ". " + doc.getString("title") + " - " + doc.getString("artist"));
        }
        System.out.print("Enter the index of the song you want to add to the playlist: ");
        return Integer.parseInt(userInput.readLine());
    }

    public static void addSong(String username, MongoCollection<org.bson.Document> playlists, MongoCollection<org.bson.Document> songs, BufferedReader userInput) throws IOException {
        int songIndex = showSongs(songs, userInput); // Get the song index from user input
        Document song = songs.find().skip(songIndex - 1).limit(1).first(); // Retrieve the song using the index

        Document playlist = playlists.find(eq("username", username)).first();
        if (playlist == null) {
            ArrayList<Document> songList = new ArrayList<>();
            songList.add(song);
            playlist = new Document("username", username).append("songs", songList);
            playlists.insertOne(playlist);
        } else {
            // Retrieve the existing songs array from the playlist
            ArrayList<Document> songList = (ArrayList<Document>) playlist.get("songs");

            // Check if the song is already in the playlist
            if (!songList.contains(song)) {
                songList.add(song);
                playlist.put("songs", songList);
                playlists.replaceOne(eq("username", username), playlist);
                System.out.println("Added song at index " + songIndex + " to " + username + "'s playlist.");
            } else {
                System.out.println("Song already exists in the playlist.");
            }
        }
    }

}
