import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private static final int PORT = 5555;
    private static final int GRID_WIDTH = 80;
    private static final int GRID_HEIGHT = 60;
    private static final int TICK_RATE = 50;
    
    private ServerSocket serverSocket;
    private GameEngine engine;
    private List<ClientHandler> clients;
    private int nextPlayerId;
    private boolean running;
    
    public GameServer() {
        this.engine = new GameEngine(GRID_WIDTH, GRID_HEIGHT);
        this.clients = new CopyOnWriteArrayList<>();
        this.nextPlayerId = 1;
        this.running = true;
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("🎮 Serveur Paper.io démarré sur le port " + PORT);
            
            // Thread pour accepter les connexions
            new Thread(this::acceptClients).start();
            
            // Boucle de jeu principale
            gameLoop();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void acceptClients() {
        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler client = new ClientHandler(socket, this, engine, 
                                                        nextPlayerId++, "Joueur");
                clients.add(client);
                new Thread(client).start();
                
                System.out.println("✅ Nouveau joueur: ID=" + client.getPlayer().getId() + 
                                 " (" + clients.size() + " connectés)");
                
            } catch (IOException e) {
                if (running) e.printStackTrace();
            }
        }
    }
    
    private void gameLoop() {
        while (running) {
            long startTime = System.currentTimeMillis();
            
            // Mettre à jour le jeu
            engine.updateAllPlayers();
            
            // Envoyer l'état à tous les clients
            broadcastGameState();
            
            // Maintenir le tick rate
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed < TICK_RATE) {
                try {
                    Thread.sleep(TICK_RATE - elapsed);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void broadcastGameState() {
        String state = engine.serializeGameState();
        for (ClientHandler client : clients) {
            client.sendMessage(state);
        }
    }
    
    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("📊 Joueurs restants: " + clients.size());
    }
    
    public void stop() {
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new GameServer().start();
    }
}

