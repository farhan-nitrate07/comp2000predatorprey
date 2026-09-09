import javax.swing.*;
import java.awt.*;

public class SimulationPanel extends JPanel {

    private Simulation simulation;

    public SimulationPanel(Simulation simulation) {
        this.simulation = simulation;
        setPreferredSize(new Dimension(500, 500));
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