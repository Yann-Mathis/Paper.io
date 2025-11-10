import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.*;

public class GamePanel extends JPanel implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 10;
    
    private GameClient client;
    private Map<Integer, PlayerData> players;
    private Set<String> territories;
    private int myPlayerId;
    private int myScore;
    private boolean isDead;
    
    public GamePanel(GameClient client) {
        this.client = client;
        this.players = new HashMap<>();
        this.territories = new HashSet<>();
        this.myScore = 0;
        this.isDead = false;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);
    }
    
    public void setMyPlayerId(int id) {
        this.myPlayerId = id;
    }
    
    public void updateGameState(String[] parts) {
        players.clear();
        territories.clear();
        
        // Parser les joueurs
        if (parts.length > 1 && !parts[1].isEmpty()) {
            String[] playerData = parts[1].split(";");
            for (String data : playerData) {
                if (data.isEmpty()) continue;
                
                String[] info = data.split("\\|");
                String[] coords = info[0].split(",");
                
                int id = Integer.parseInt(coords[0]);
                int x = Integer.parseInt(coords[1]);
                int y = Integer.parseInt(coords[2]);
                String color = coords[3];
                String name = coords.length > 4 ? coords[4] : "Joueur";
                
                PlayerData player = new PlayerData(id, x, y, color, name);
                
                // Parser traînée
                if (info.length > 1 && !info[1].isEmpty()) {
                    String[] trailPoints = info[1].split(":");
                    for (int i = 0; i < trailPoints.length - 1; i += 2) {
                        if (i + 1 < trailPoints.length) {
                            int tx = Integer.parseInt(trailPoints[i]);
                            int ty = Integer.parseInt(trailPoints[i + 1]);
                            player.addTrailPoint(tx, ty);
                        }
                    }
                }
                
                players.put(id, player);
                
                if (id == myPlayerId) {
                    isDead = false;
                }
            }
        }
        
        // Parser la grille
        if (parts.length > 2 && parts[2].equals("GRID") && parts.length > 3) {
            String[] gridData = parts[3].split(";");
            for (String cell : gridData) {
                if (!cell.isEmpty()) {
                    territories.add(cell);
                }
            }
        }
        
        // Calculer score
        myScore = 0;
        for (String cell : territories) {
            String[] coords = cell.split(",");
            if (coords.length >= 3) {
                int owner = Integer.parseInt(coords[2]);
                if (owner == myPlayerId) {
                    myScore++;
                }
            }
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Grille de fond
        g2d.setColor(new Color(240, 240, 240));
        for (int x = 0; x < WIDTH; x += CELL_SIZE * 5) {
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += CELL_SIZE * 5) {
            g2d.drawLine(0, y, WIDTH, y);
        }
        
        // Territoires
        for (String cell : territories) {
            String[] coords = cell.split(",");
            if (coords.length >= 3) {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int owner = Integer.parseInt(coords[2]);
                
                PlayerData player = players.get(owner);
                if (player != null) {
                    Color c = Color.decode(player.getColor());
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        
        // Joueurs et traînées
        for (PlayerData player : players.values()) {
            // Traînée
            Color trailColor = Color.decode(player.getColor());
            g2d.setColor(new Color(trailColor.getRed(), trailColor.getGreen(), 
                                   trailColor.getBlue(), 200));
            for (PlayerData.Point p : player.getTrail()) {
                g2d.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
            
            // Joueur
            g2d.setColor(Color.decode(player.getColor()));
            g2d.fillRect(player.getX() * CELL_SIZE, player.getY() * CELL_SIZE, 
                        CELL_SIZE, CELL_SIZE);
            
            // Nom
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(player.getName(), player.getX() * CELL_SIZE - 10, 
                          player.getY() * CELL_SIZE - 5);
        }
        
        // UI Score
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(10, 10, 200, 60, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + myScore, 20, 35);
        g2d.drawString("Joueurs: " + players.size(), 20, 55);
        
        // Message de mort
        if (isDead) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("MORT!", WIDTH / 2 - 80, HEIGHT / 2);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Appuyez sur R pour réapparaître", WIDTH / 2 - 150, HEIGHT / 2 + 40);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (isDead && e.getKeyCode() == KeyEvent.VK_R) {
            client.requestRespawn();
            return;
        }
        
        int key = e.getKeyCode();
        
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_Q) {
            client.sendMove(-1, 0);
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            client.sendMove(1, 0);
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_Z) {
            client.sendMove(0, -1);
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            client.sendMove(0, 1);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    public void setDead(boolean dead) {
        this.isDead = dead;
    }
}
