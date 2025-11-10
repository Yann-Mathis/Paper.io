import java.util.*;

public class PlayerData {
    private int id;
    private int x;
    private int y;
    private String color;
    private String name;
    private List<Point> trail;
    
    public PlayerData(int id, int x, int y, String color, String name) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.color = color;
        this.name = name;
        this.trail = new ArrayList<>();
    }
    
    public void addTrailPoint(int x, int y) {
        trail.add(new Point(x, y));
    }
    
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getColor() { return color; }
    public String getName() { return name; }
    public List<Point> getTrail() { return trail; }
    
    public static class Point {
        public int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

