import javax.swing.*;

public class App {

    public static void main(String[] args) {

        Simulation simulation = new Simulation();

        Prey prey = new Prey(1, 0, true, 5, 5);
        Predator predator = new Predator(1, 0, 10, 10);
        Grass grass = new Grass(3, 3, 5);

        simulation.addEntity(prey);
        simulation.addEntity(predator);
        simulation.addEntity(grass);

        System.out.println(
            "Entities in simulation: " + simulation.getEntityCount()
        );

        try {
            // Valid movement
            prey.moveTo(6, 5, 20, 20);

            System.out.println(
                "Prey moved to: " + prey.getX() + ", " + prey.getY()
            );

            // Invalid movement to demonstrate exception handling
            prey.moveTo(25, 5, 20, 20);

        } catch (InvalidPositionException e) {
            System.out.println(
                "Movement error: " + e.getMessage()
            );
        }

        JFrame frame = new JFrame("Predator Prey Simulation");

        SimulationPanel panel = new SimulationPanel(simulation);

        frame.add(panel);
        frame.pack();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }
}