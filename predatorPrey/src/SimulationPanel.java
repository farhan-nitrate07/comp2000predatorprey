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

        Predator predator = null;
        Prey prey = null;

        for (Entity entity : simulation.getEntities()) {

            if (entity instanceof Predator) {
                predator = (Predator) entity;
            }

            if (entity instanceof Prey) {
                prey = (Prey) entity;
            }
        }

        // Prey still moves randomly for now
        if (prey != null) {

            int changeX = random.nextInt(3) - 1;
            int changeY = random.nextInt(3) - 1;

            int newX = prey.getX() + changeX;
            int newY = prey.getY() + changeY;

            try {
                prey.moveTo(newX, newY, 20, 20);
            } catch (InvalidPositionException e) {
                // Stay in the same position
            }
        }

        // Predator moves towards prey
        if (predator != null && prey != null) {

            try {
                predator.chase(prey, 20, 20);
            } catch (InvalidPositionException e) {
                // Stay in the same position
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