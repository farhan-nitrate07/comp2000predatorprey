
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Predator extends Creature {

    private List<Creature> sim;

    private static final Random random = new Random();

    private static final int DETECTION_RANGE = 200;
    private static final int EAT_RADIUS = 22;
    private static final int REPRODUCE_THRESHOLD = 20;
    private static final double REPRODUCE_CHANCE = 0.08;
    private static final int HUNT_THRESHOLD = 70;
    private static final double HUNT_SPEED_MULTIPLIER = 2.2;

    // Strategy pattern: predator can switch movement behaviour
    private final MovementStrategy wanderStrategy = new WanderStrategy();

    private final MovementStrategy huntStrategy =
            new HuntStrategy(HUNT_SPEED_MULTIPLIER);

    private MovementStrategy movementStrategy = wanderStrategy;

    public Predator(int speed, int hunger, int x, int y,
                    List<Creature> sim) {

        super(speed, hunger, false, x, y);
        this.sim = sim;
    }

    @Override
    protected int getStarvationRate() {
        return 3;
    }

    @Override
    public void eat() {

        if (getStarvation() < HUNT_THRESHOLD) {
            return;
        }

        // Streams: find all available prey
        List<Prey> preyCandidates = sim.stream()
                .filter(Prey.class::isInstance)
                .map(Prey.class::cast)
                .toList();

        Optional<Prey> closest = findClosest(preyCandidates);

        closest.ifPresent(prey -> {

            double dist = Math.hypot(
                    getX() - prey.getX(),
                    getY() - prey.getY()
            );

            if (dist <= EAT_RADIUS) {
                sim.remove(prey);
                resetStarvation();
            }
        });
    }

    @Override
    public void reproduce() {

        if (sim == null) {
            throw new IllegalStateException(
                    "Cannot reproduce: no simulation list assigned"
            );
        }

        if (getStarvation() <= REPRODUCE_THRESHOLD
                && random.nextDouble() < REPRODUCE_CHANCE) {

            Predator offspring = new Predator(
                    getSpeed(),
                    0,
                    getX(),
                    getY(),
                    sim
            );

            sim.add(offspring);
            addStarvation(REPRODUCE_THRESHOLD);
        }
    }

    @Override
    public void movement() {

        // A predator with low hunger simply wanders
        if (getStarvation() < HUNT_THRESHOLD) {

            movementStrategy = wanderStrategy;
            movementStrategy.move(this, null);

            recordPosition();
            return;
        }

        // Find prey using Java Streams
        List<Prey> preyCandidates = sim.stream()
                .filter(Prey.class::isInstance)
                .map(Prey.class::cast)
                .toList();

        Optional<Prey> nearest = findNearestInRange(
                preyCandidates,
                DETECTION_RANGE
        );

        if (nearest.isPresent()) {

            // Switch to hunting when prey is nearby
            movementStrategy = huntStrategy;
            movementStrategy.move(this, nearest.get());

        } else {

            // Switch back to wandering when no prey is nearby
            movementStrategy = wanderStrategy;
            movementStrategy.move(this, null);
        }

        recordPosition();
    }
}
