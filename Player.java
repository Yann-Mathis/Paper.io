import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {
    private int id;
    private String name;
    private String color;
    private int x;
    private int y;
    private int dx;
    private int dy;
    private boolean alive;
    private List<int[]> trail;
    
    public Player(int id, String name, String color, int x, int y) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
        this.dx = 1;
        this.dy = 0;
        this.alive = true;
        this.trail = new CopyOnWriteArrayList<>();
    }
    
    public void move() {
        x += dx;
        y += dy;
    }
    
    public void setDirection(int dx, int dy) {
        // Empêcher le demi-tour
        if (!(this.dx == -dx && this.dy == -dy)) {
            this.dx = dx;
            this.dy = dy;
        }
    }
    
    public void addToTrail(int x, int y) {
        trail.add(new int[]{x, y});
    }
    
    public void clearTrail() {
        trail.clear();
    }
    
    public boolean isInTrail(int x, int y) {
        for (int[] point : trail) {
            if (point[0] == x && point[1] == y) {
                return true;
            }
        }
        return false;
    }
    
    public void kill() {
        this.alive = false;
        this.trail.clear();
    }
    
    public void respawn(int x, int y) {
        this.x = x;
        this.y = y;
        this.alive = true;
        this.trail.clear();
    }
    
    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getDx() { return dx; }
    public int getDy() { return dy; }
    public boolean isAlive() { return alive; }
    public List<int[]> getTrail() { return trail; }
}
