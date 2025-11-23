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
        int x = 10 + random.nextInt(grid.getWidth() - 20);
        int y = 10 + random.nextInt(grid.getHeight() - 20);
        String color = generateRandomColor();
        
        Player player = new Player(id, name, color, x, y);
        players.add(player);
        
        grid.initializeTerritory(x, y, id, 2);
        return player;
    }
    
    public void removePlayer(int playerId) {
        players.removeIf(p -> p.getId() == playerId);
    }
    
    public Player getPlayer(int playerId) {
        for (Player player : players) {
            if (player.getId() == playerId) return player;
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

    /* ============================================================
       ===============  UPDATE PLAYER (DÉCOUPÉ) ====================
       ============================================================ */

    private void updatePlayer(Player player) {
        int oldX = player.getX();
        int oldY = player.getY();

        attemptMove(player, oldX, oldY);

        int x = player.getX();
        int y = player.getY();

        if (x != oldX || y != oldY) {
            checkAllTrailCollisions(player, x, y);
            handleTerritoryOrTrail(player, x, y);
        }
    }

    /* === 1) Déplacement, sans changer la logique === */
    private void attemptMove(Player player, int oldX, int oldY) {
        if (Math.max(
                grid.isInBounds(oldX - 1 + player.getDx(), oldY - 1 + player.getDy()),
                grid.isInBounds(oldX + 1 + player.getDx(), oldY + 1 + player.getDy())
        ) == 0) {
            player.move();
        }
    }

    /* === 2) Collisions avec TOUTES les traînées === */
    private void checkAllTrailCollisions(Player player, int x, int y) {
        for (Player other : players) {
            if (!other.isAlive()) continue;

            if (other.isInTrail(x, y)) {
                other.killAndClearTerritory(grid);
                System.out.println("💀 Joueur " + player.getId() +
                                       " a touché la traînée de " + other.getId());
                return;
            }
        }
    }

    /* === 3) Capture ou ajout de traînée === */
    private void handleTerritoryOrTrail(Player player, int x, int y) {
        int cell = grid.getCell(x, y);

        if (cell == player.getId() && !player.getTrail().isEmpty()) {
            grid.captureTerritory(player);
            System.out.println("🎯 Joueur " + player.getId() + " a capturé du territoire!");
        } else if (cell == 0 || cell != player.getId()) {
            player.addToTrail(x, y);
        }
    }

    /* ============================================================
                         FIN UPDATE PLAYER
       ============================================================ */

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
        
        for (Player player : players) {
            if (player.isAlive()) {
                state.append(player.getId()).append(",")
                     .append(player.getX()).append(",")
                     .append(player.getY()).append(",")
                     .append(player.getColor()).append(",")
                     .append(player.getName()).append("|");
                
                for (int[] point : player.getTrail()) {
                    state.append(point[0]).append(",").append(point[1]).append(";");
                }
                state.append("|");
            }
        }
        
        state.append("GRID|").append(grid.serializeGrid());
        return state.toString();
    }
    
    public List<Player> getPlayers() { return players; }
    public GameGrid getGrid() { return grid; }
}

