import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private GameServer server;
    private GameEngine engine;
    private Player player;
    private PrintWriter out;
    private BufferedReader in;
    
    public ClientHandler(Socket socket, GameServer server, GameEngine engine, int playerId, String defaultName) {
        this.socket = socket;
        this.server = server;
        this.engine = engine;
        
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Créer le joueur
            this.player = engine.addPlayer(playerId, defaultName);
            
            // Envoyer l'ID et la couleur au client
            out.println("ID|" + player.getId() + "|" + player.getColor());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("❌ Joueur " + player.getId() + " déconnecté");
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(String message) {
        String[] parts = message.split("\\|");
        
        if (parts[0].equals("MOVE") && parts.length >= 3) {
            int dx = Integer.parseInt(parts[1]);
            int dy = Integer.parseInt(parts[2]);
            if (player != null && player.isAlive()) {
                player.setDirection(dx, dy);
            }
        } else if (parts[0].equals("RESPAWN")) {
            if (player != null) {
                engine.respawnPlayer(player.getId());
            }
        }
    }
    
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    
    private void disconnect() {
        engine.removePlayer(player.getId());
        server.removeClient(this);
        
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Player getPlayer() { return player; }
}

