import java.util.*;

public class GameGrid {
    private int width;
    private int height;
    private int[][] grid;
    private List<int[]> changes;
    private boolean complete_state;
    public GameGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new int[width][height];
        this.changes = new ArrayList<>();
        this.complete_state = false;
    }
    
    public void setCell(int x, int y, int value) {
        if (isInBounds(x, y) == 0) {
            grid[x][y] = value;
            changes.add(new int[]{x,y});
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
                    changes.add(new int[]{x,y});
                }
            }
        }
        complete_state = true;
    }
    
    public void clearPlayerTerritory(int playerId) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (grid[x][y] == playerId) {
                    grid[x][y] = 0; // Remettre à neutre
                    changes.add(new int[]{x,y});
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
                if ( !visited[x][y]) {
                    List<int[]> region = new ArrayList<>();
                    boolean touchesBorder = floodFill(x, y, visited, region, playerId);
                    
                    if (!touchesBorder) {
                        for (int[] point : region) {
                            grid[point[0]][point[1]] = playerId;
                            changes.add(new int[]{point[0],point[1]});
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
        if(!this.complete_state){
            for(int [] par: changes){
                int x = par[0];
                int y = par[1];
                sb.append(x).append(",").append(y).append(",")
                    .append(grid[x][y]).append(";");
            }
            System.out.println(sb);
            this.changes = new ArrayList<>();
        }else{
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (grid[x][y] != 0) {
                        sb.append(x).append(",").append(y).append(",")
                        .append(grid[x][y]).append(";");
                    }
                }
            }
            this.complete_state = false;
        }
        return sb.toString();
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
