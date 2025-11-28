import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.*;

public class GamePanel extends JPanel implements KeyListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int CELL_SIZE = 10;
    private static final int columns = WIDTH/CELL_SIZE;
    private static final int rows = HEIGHT/CELL_SIZE;

    private GameClient client;
    private Map<Integer, Player> players;
    private int [][] grade;
    private Set<String> territories;
    private int myPlayerId;

    public GamePanel(GameClient client) {
        this.client = client;
        this.players = new HashMap<>();
        this.territories = new HashSet<>();
        this.grade = new int[columns][rows];

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
        //territories.clear();

        parsePlayers(parts);
        parseGrid(parts);

        // Mise à jour des scores
        for (Player player : players.values()) {
            //player.calculateScore(territories);
            //player.calculateScore(grade,columns,rows);
        }

        repaint();
    }

    private void parsePlayers(String[] parts) {
        if (parts.length <= 1 || parts[1].isEmpty()) return;

        int gridIndex = findGridIndex(parts);
        int currentIndex = 1;

        while (currentIndex < gridIndex) {
            String playerInfo = parts[currentIndex];

            if (!playerInfo.isEmpty()) {
                String[] coords = playerInfo.split(",");
                if (coords.length >= 5) {

                    int id = Integer.parseInt(coords[0]);
                    int x = Integer.parseInt(coords[1]);
                    int y = Integer.parseInt(coords[2]);
                    String color = coords[3];
                    String name = coords[4];

                    Player player = new Player(id, name, color, x, y);

                    // Traînée
                    if (currentIndex + 1 < gridIndex) {
                        parseTrail(parts[currentIndex + 1], player);
                    }

                    players.put(id, player);
                }
            }
            currentIndex += 2;
        }
    }

    private void parseTrail(String trailData, Player player) {
        if (trailData == null || trailData.isEmpty() || trailData.equals("GRID")) return;

        String[] points = trailData.split(";");
        for (String point : points) {
            if (!point.isEmpty()) {
                String[] xy = point.split(",");
                if (xy.length == 2) {
                    int tx = Integer.parseInt(xy[0]);
                    int ty = Integer.parseInt(xy[1]);
                    player.addToTrail(tx, ty);
                }
            }
        }
    }

    private int findGridIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if ("GRID".equals(parts[i])) return i;
        }
        return -1;
    }

    private void parseGrid(String[] parts) {
        int gridIndex = findGridIndex(parts);
        if (gridIndex == -1 || gridIndex + 1 >= parts.length) return;

        String[] gridData = parts[gridIndex + 1].split(";");
        for (String cell : gridData) {
            if (!cell.isEmpty()){
               // territories.add(cell);
                String[] coords = cell.split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int owner = Integer.parseInt(coords[2]);
                grade[x][y] = owner;
            }
        }
    }

    private int getMyScore() {
        Player me = players.get(myPlayerId);
        return (me != null) ? me.getScore() : 0;
    }

    // ------------------- Affichage -------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGridBackground(g2d);
        drawTerritories(g2d);
        drawPlayersAndTrails(g2d);
        drawUI(g2d);

        if (isMyPlayerDead()) drawDeathMessage(g2d);
    }

    private boolean isMyPlayerDead() {
        Player me = players.get(myPlayerId);
        return (me != null && !me.isAlive());
    }

    private void drawGridBackground(Graphics2D g2d) {
        g2d.setColor(new Color(240, 240, 240));
        for (int x = 0; x < WIDTH; x += CELL_SIZE * 5) g2d.drawLine(x, 0, x, HEIGHT);
        for (int y = 0; y < HEIGHT; y += CELL_SIZE * 5) g2d.drawLine(0, y, WIDTH, y);
    }

    private void drawTerritories(Graphics2D g2d) {
        for(int x = 0; x < columns; x++){
            for(int y = 0; y < rows; y++){
                 if (grade[x][y] != 0) {
                    Player player = players.get(grade[x][y]);
                    if(player != null){
                    //System.out.println(player.getId());
                    Color c = Color.decode(player.getColor());
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
        /*for (String cell : territories) {
            String[] coords = cell.split(",");
            if (coords.length >= 3) {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int owner = Integer.parseInt(coords[2]);

                Player player = players.get(owner);
                if (player != null) {
                    Color c = Color.decode(player.getColor());
                    g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
                    g2d.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }*/
    }

    private void drawPlayersAndTrails(Graphics2D g2d) {
        for (Player player : players.values()) {

            // Traînée
            Color trailColor = Color.decode(player.getColor());
            g2d.setColor(new Color(trailColor.getRed(), trailColor.getGreen(), trailColor.getBlue(), 200));
            for (int[] p : player.getTrail()) {
                g2d.fillRect(p[0] * CELL_SIZE, p[1] * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }

            // Joueur
            g2d.setColor(Color.decode(player.getColor()));
            g2d.fillRect(player.getX() * CELL_SIZE, player.getY() * CELL_SIZE, CELL_SIZE, CELL_SIZE);

            // Nom
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(player.getName(), player.getX() * CELL_SIZE - 10, player.getY() * CELL_SIZE - 5);
        }
    }

    private void drawUI(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(10, 10, 200, 60, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Score: " + getMyScore(), 20, 35);
        g2d.drawString("Joueurs: " + players.size(), 20, 55);
    }

    private void drawDeathMessage(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString("MORT!", WIDTH / 2 - 80, HEIGHT / 2);
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.drawString("Appuyez sur R pour réapparaître", WIDTH / 2 - 150, HEIGHT / 2 + 40);
    }

    // ------------------- Input -------------------
    @Override
    public void keyPressed(KeyEvent e) {
        Player me = players.get(myPlayerId);

        int key = e.getKeyCode();

        // Demander un respawn uniquement si on est MORT
        if (key == KeyEvent.VK_R) {
            if (me == null || !me.isAlive()) {
                client.requestRespawn();
            }
            return; // Toujours retourner après avoir géré R
        }

        // Si le joueur n'existe pas encore → ne rien faire
        if (me == null) return;

        // Si on est mort → pas de déplacement
        if (!me.isAlive()) return;

        // Déplacements
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_Q) client.sendMove(-1, 0);
        else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) client.sendMove(1, 0);
        else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_Z) client.sendMove(0, -1);
        else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) client.sendMove(0, 1);
    }


    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // Ne sert plus vraiment, mais je laisse au cas où tu l’appelles encore
    public void setDead(boolean dead) {}
}

