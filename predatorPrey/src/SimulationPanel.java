import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class SimulationPanel extends JPanel {

    private Simulation simulation;
    private Random random = new Random();

    public SimulationPanel(Simulation simulation) {
        this.simulation = simulation;

        setPreferredSize(new Dimension(500, 500));

        Timer timer = new Timer(500, e -> {
            moveCreatures();
            repaint();
        });

        timer.start();
    }

    private void moveCreatures() {

        for (Entity entity : simulation.getEntities()) {

            if (entity instanceof Creature) {

                int changeX = random.nextInt(3) - 1;
                int changeY = random.nextInt(3) - 1;

                int newX = entity.getX() + changeX;
                int newY = entity.getY() + changeY;

                try {
                    entity.moveTo(newX, newY, 20, 20);
                } catch (InvalidPositionException e) {
                    // If movement goes outside the grid,
                    // the entity simply stays where it is.
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Entity entity : simulation.getEntities()) {

            int x = entity.getX() * 20;
            int y = entity.getY() * 20;

            if (entity instanceof Predator) {
                g.setColor(Color.RED);

            } else if (entity instanceof Prey) {
                g.setColor(Color.BLUE);

            } else if (entity instanceof Grass) {
                g.setColor(Color.GREEN);

            } else {
                g.setColor(Color.BLACK);
            }

            g.fillOval(x, y, 15, 15);
        }
    }
}