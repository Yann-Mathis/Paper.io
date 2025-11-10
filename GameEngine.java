import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine {
    private GameGrid grid;
    private List<Player> players;
    private Random random;
    
    public GameEngine(int gridWidth, int gridHeight) {
        this.grid = new GameGrid(gridWidth, gridHeight);
        this.players = new CopyOnWriteArrayList<>();
        this.random = new Random();
    }
    
    public Player addPlayer(int id, String name) {
        // Générer position et couleur aléatoires
        int x = 10 + random.nextInt(grid.getWidth() - 20);
        int y = 10 + random.nextInt(grid.getHeight() - 20);
        String color = generateRandomColor();
        
        Player player = new Player(id, name, color, x, y);
        players.add(player);
        
        // Initialiser territoire
        grid.initializeTerritory(x, y, id, 2);
        
        return player;
    }
    
    public void removePlayer(int playerId) {
        players.removeIf(p -> p.getId() == playerId);
    }
    
    public Player getPlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }
    
    public void updateAllPlayers() {
        for (Player player : players) {
            if (player.isAlive()) {
                updatePlayer(player);
            }
        }
    }
    
    private void updatePlayer(Player player) {
        // Déplacer le joueur
        player.move();
        
        int x = player.getX();
        int y = player.getY();
        
        // Vérifier limites
        if (!grid.isInBounds(x, y)) {
            player.kill();
            return;
        }
        
        // Vérifier collision avec traînée adverse
        for (Player other : players) {
            if (other != player && other.isAlive()) {
                if (other.isInTrail(x, y)) {
                    player.kill();
                    return;
                }
            }
        }
        
        int cell = grid.getCell(x, y);
        
        // Vérifier retour sur territoire
        if (cell == player.getId() && !player.getTrail().isEmpty()) {
            grid.captureTerritory(player);
        } else if (cell == 0 || cell != player.getId()) {
            // Ajouter à la traînée
            player.addToTrail(x, y);
        }
    }
    
    public void respawnPlayer(int playerId) {
        Player player = getPlayer(playerId);
        if (player != null) {
            int x = 10 + random.nextInt(grid.getWidth() - 20);
            int y = 10 + random.nextInt(grid.getHeight() - 20);
            player.respawn(x, y);
            grid.initializeTerritory(x, y, playerId, 2);
        }
    }
    
    private String generateRandomColor() {
        int r = random.nextInt(200) + 55;
        int g = random.nextInt(200) + 55;
        int b = random.nextInt(200) + 55;
        return String.format("#%02x%02x%02x", r, g, b);
    }
    
    public String serializeGameState() {
        StringBuilder state = new StringBuilder("STATE|");
        
        // Sérialiser tous les joueurs
        for (Player player : players) {
            if (player.isAlive()) {
                state.append(player.getId()).append(",")
                     .append(player.getX()).append(",")
                     .append(player.getY()).append(",")
                     .append(player.getColor()).append(",")
                     .append(player.getName()).append(";");
                
                // Traînées
                for (int[] point : player.getTrail()) {
                    state.append(point[0]).append(":").append(point[1]).append(":");
                }
                state.append("|");
            }
        }
        
        // Ajouter la grille
        state.append("GRID|").append(grid.serializeGrid());
        
        return state.toString();
    }
    
    public List<Player> getPlayers() { return players; }
    public GameGrid getGrid() { return grid; }
}

