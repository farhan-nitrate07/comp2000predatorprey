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

        System.out.println(
            "Starting position: " + prey.getX() + ", " + prey.getY()
        );

        try {
            // Valid movement
            prey.moveTo(6, 5, 20, 20);

            System.out.println(
                "New position: " + prey.getX() + ", " + prey.getY()
            );

            // Invalid movement - outside the 20 x 20 grid
            prey.moveTo(25, 5, 20, 20);

        } catch (InvalidPositionException e) {

            System.out.println(
                "Movement error: " + e.getMessage()
            );
        }

        System.out.println("Program continues...");
    }
}