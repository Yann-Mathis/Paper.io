import java.io.*;
import java.net.Socket;

public class NetworkManager {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private GameClient client;
    
    public NetworkManager(GameClient client) {
        this.client = client;
    }
    
    public boolean connect(String serverAddress, int port, String playerName) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            // Envoyer le nom
            out.println("NAME|" + playerName);
            
            // Démarrer thread de réception
            new Thread(this::receiveMessages).start();
            
            return true;
            
        } catch (IOException e) {
            return false;
        }
    }
    
    private void receiveMessages() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                client.handleServerMessage(message);
            }
        } catch (IOException e) {
            client.onConnectionLost();
        }
    }
    
    public void sendMove(int dx, int dy) {
        if (out != null) {
            out.println("MOVE|" + dx + "|" + dy);
        }
    }
    
    public void sendRespawn() {
        if (out != null) {
            out.println("RESPAWN|");
        }
    }
    
    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
