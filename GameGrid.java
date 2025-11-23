import java.util.*;

public class GameGrid {
    private int width;
    private int height;
    private int[][] grid;
    
    public GameGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[width][height];
    }
    
    public void setCell(int x, int y, int value) {
        if (isInBounds(x, y) == 0) {
            grid[x][y] = value;
        }
    }
    
    public int getCell(int x, int y) {
        if (isInBounds(x, y) == 0) {
            return grid[x][y];
        }
        return -1;
    }
    
    public int isInBounds(int x, int y) {
        if(x >= 0 && x < width ){
            if(y >= 0 && y < height){
                return 0;
            }else{
                return 1;
            }
        }else{
            if(y >= 0 && y < height){
                return 2;
            }else{
                return 3;
            }
        }
    }
    
    public void initializeTerritory(int centerX, int centerY, int playerId, int size) {
        for (int i = -size; i <= size; i++) {
            for (int j = -size; j <= size; j++) {
                int x = centerX + i;
                int y = centerY + j;
                if (isInBounds(x, y)==0) {
                    grid[x][y] = playerId;
                }
            }
        }
    }
    
    public void clearPlayerTerritory(int playerId) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == playerId) {
                    grid[x][y] = 0; // Remettre à neutre
                }
            }
        }
        System.out.println("🧹 Territoire du joueur " + playerId + " supprimé");
    }
    
    public void captureTerritory(Player player) {
        int playerId = player.getId();
        
        // Marquer la traînée comme territoire
        for (int[] point : player.getTrail()) {
            setCell(point[0], point[1], playerId);
        }
        
        // Flood fill pour remplir l'intérieur
        boolean[][] visited = new boolean[width][height];
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == 0 && !visited[x][y]) {
                    List<int[]> region = new ArrayList<>();
                    boolean touchesBorder = floodFill(x, y, visited, region, playerId);
                    
                    if (!touchesBorder) {
                        for (int[] point : region) {
                            grid[point[0]][point[1]] = playerId;
                        }
                    }
                }
            }
        }
        
        player.clearTrail();
    }
    
    private boolean floodFill(int x, int y, boolean[][] visited, List<int[]> region, int playerId) {
        if (isInBounds(x, y) != 0) {
            return true;
        }
        
        if (visited[x][y] || grid[x][y] == playerId) {
            return false;
        }
        
        visited[x][y] = true;
        region.add(new int[]{x, y});
        
        boolean touchesBorder = false;
        touchesBorder |= floodFill(x + 1, y, visited, region, playerId);
        touchesBorder |= floodFill(x - 1, y, visited, region, playerId);
        touchesBorder |= floodFill(x, y + 1, visited, region, playerId);
        touchesBorder |= floodFill(x, y - 1, visited, region, playerId);
        
        return touchesBorder;
    }
    
    public String serializeGrid() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] != 0) {
                    sb.append(x).append(",").append(y).append(",")
                      .append(grid[x][y]).append(";");
                }
            }
        }
        return sb.toString();
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
