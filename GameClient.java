import javax.swing.*;
import java.util.Random;

public class GameClient extends JFrame {
    private GamePanel gamePanel;
    private NetworkManager network;
    private int myPlayerId;
    private String myColor;
    
    public GameClient(String serverAddress, String playerName) {
        setTitle("Paper.io - Multijoueur");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        gamePanel = new GamePanel(this);
        network = new NetworkManager(this);
        
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Connexion au serveur
        if (!network.connect(serverAddress, 5555, playerName)) {
            JOptionPane.showMessageDialog(this, 
                "Impossible de se connecter au serveur", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
    
    public void handleServerMessage(String message) {
        String[] parts = message.split("\\|");
        
        if (parts[0].equals("ID")) {
            myPlayerId = Integer.parseInt(parts[1]);
            myColor = parts[2];
            gamePanel.setMyPlayerId(myPlayerId);
            System.out.println("✅ Connecté avec ID: " + myPlayerId);
            
        } else if (parts[0].equals("STATE")) {
            gamePanel.updateGameState(parts);
        }
    }
    
    public void onConnectionLost() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, 
                "Connexion perdue avec le serveur", 
                "Erreur", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        });
    }
    
    public void sendMove(int dx, int dy) {
        network.sendMove(dx, dy);
    }
    
    public void requestRespawn() {
        network.sendRespawn();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String serverAddress = JOptionPane.showInputDialog(
                null,
                "Adresse du serveur:",
                "Connexion",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (serverAddress == null || serverAddress.isEmpty()) {
                serverAddress = "localhost";
            }
            
            String playerName = JOptionPane.showInputDialog(
                null,
                "Votre pseudo:",
                "Pseudo",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (playerName == null || playerName.isEmpty()) {
                playerName = "Joueur" + new Random().nextInt(1000);
            }
            
            new GameClient(serverAddress, playerName);
        });
    }
}
