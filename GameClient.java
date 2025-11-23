import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class GameClient extends JFrame {
    private GamePanel gamePanel;
    private NetworkManager network;
    private int myPlayerId;
    private String myColor;
    
    private GameClient(String serverAddress, String playerName) {
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
            dispose();
            showMainMenu();
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
            dispose();
            showMainMenu();
        });
    }
    
    public void sendMove(int dx, int dy) {
        network.sendMove(dx, dy);
    }
    
    public void requestRespawn() {
        network.sendRespawn();
    }
    
    private static void showMainMenu() {
        SwingUtilities.invokeLater(() -> {
            JFrame menuFrame = new JFrame("Paper.io - Menu Principal");
            menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            menuFrame.setSize(400, 300);
            menuFrame.setLocationRelativeTo(null);
            
            // Panel principal avec gradient
            JPanel mainPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(79, 70, 229),
                        0, getHeight(), new Color(147, 51, 234)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            mainPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            // Titre
            JLabel titleLabel = new JLabel("PAPER.IO");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
            titleLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            mainPanel.add(titleLabel, gbc);
            
            // Sous-titre
            JLabel subtitleLabel = new JLabel("Jeu Multijoueur");
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            subtitleLabel.setForeground(new Color(200, 200, 255));
            gbc.gridy = 1;
            mainPanel.add(subtitleLabel, gbc);
            
            // Champ serveur
            gbc.gridwidth = 1;
            gbc.gridy = 2;
            JLabel serverLabel = new JLabel("Serveur:");
            serverLabel.setForeground(Color.WHITE);
            serverLabel.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(serverLabel, gbc);
            
            gbc.gridx = 1;
            JTextField serverField = new JTextField("localhost", 15);
            mainPanel.add(serverField, gbc);
            
            // Champ pseudo
            gbc.gridx = 0;
            gbc.gridy = 3;
            JLabel nameLabel = new JLabel("Pseudo:");
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            mainPanel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            JTextField nameField = new JTextField("Joueur" + new Random().nextInt(1000), 15);
            mainPanel.add(nameField, gbc);
            
            // Bouton Jouer
            gbc.gridx = 0;
            gbc.gridy = 4;
            gbc.gridwidth = 2;
            JButton playButton = new JButton("JOUER");
            playButton.setFont(new Font("Arial", Font.BOLD, 18));
            playButton.setBackground(new Color(34, 197, 94));
            playButton.setForeground(Color.WHITE);
            playButton.setFocusPainted(false);
            playButton.setBorderPainted(false);
            playButton.setPreferredSize(new Dimension(200, 50));
            playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            playButton.addActionListener(e -> {
                String serverAddress = serverField.getText().trim();
                String playerName = nameField.getText().trim();
                
                // Valeurs par défaut si vide
                if (serverAddress.isEmpty()) {
                    serverAddress = "localhost";
                }
                if (playerName.isEmpty()) {
                    playerName = "Joueur" + new Random().nextInt(1000);
                }
                
                menuFrame.dispose();
                new GameClient(serverAddress, playerName);
            });
            
            mainPanel.add(playButton, gbc);
            
            menuFrame.add(mainPanel);
            menuFrame.setVisible(true);
        });
    }
    
    public static void main(String[] args) {
        showMainMenu();
    }
}
