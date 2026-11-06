
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Prey extends Creature {

    private boolean inDanger;
    private List<Creature> sim;
    private List<Grass> grassList;

    private static final Random random = new Random();

    private static final int THREAT_RANGE = 120;
    private static final double FLEE_SPEED_MULTIPLIER = 1.8;
    private static final int EAT_RADIUS = 10;
    private static final double REPRODUCE_CHANCE = 0.05;
    private static final int REPRODUCE_THRESHOLD = 20;
    private static final int GRAZE_THRESHOLD = 60;

    private int starvationRate = 1;

    // Strategy pattern: prey can switch movement behaviour
    private final MovementStrategy wanderStrategy = new WanderStrategy();

    private final MovementStrategy fleeStrategy =
            new FleeStrategy(FLEE_SPEED_MULTIPLIER);

    private MovementStrategy movementStrategy = wanderStrategy;

    public Prey(int speed, int hunger, int x, int y,
                List<Creature> sim, List<Grass> grassList) {

        super(speed, hunger, false, x, y);

        this.inDanger = false;
        this.sim = sim;
        this.grassList = grassList;
    }

    public boolean isInDanger() {
        return inDanger;
    }

    public void setInDanger(boolean inDanger) {
        this.inDanger = inDanger;
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

            Prey offspring = new Prey(
                    getSpeed(),
                    0,
                    getX(),
                    getY(),
                    sim,
                    grassList
            );

            sim.add(offspring);
            addStarvation(REPRODUCE_THRESHOLD);
        }
    }

    @Override
    public void movement() {

        // Streams: find predators in the simulation
        List<Predator> predatorCandidates = sim.stream()
                .filter(Predator.class::isInstance)
                .map(Predator.class::cast)
                .toList();

        Optional<Predator> nearestThreat =
                findNearestInRange(
                        predatorCandidates,
                        THREAT_RANGE
                );

        if (nearestThreat.isPresent()) {

            // Switch to fleeing when a predator is nearby
            setInDanger(true);

            movementStrategy = fleeStrategy;
            movementStrategy.move(this, nearestThreat.get());

            recordPosition();
            return;
        }

        setInDanger(false);

        // Wander normally when there is no danger
        // and the prey is not hungry enough to seek food
        if (getStarvation() < GRAZE_THRESHOLD) {

            movementStrategy = wanderStrategy;
            movementStrategy.move(this, null);

            recordPosition();
            return;
        }

        // Preserve the existing grazing behaviour
        List<Grass> edibleGrass = grassList.stream()
                .filter(Grass::isEdible)
                .toList();

        Optional<Grass> target = findClosest(edibleGrass);

        target.ifPresent(grass -> {

            double diffX = grass.getX() - getX();
            double diffY = grass.getY() - getY();

            double dist = Math.hypot(diffX, diffY);

            if (dist == 0) {
                return;
            }

            int moveX = (int) Math.round(
                    (diffX / dist) * getSpeed()
            );

            int moveY = (int) Math.round(
                    (diffY / dist) * getSpeed()
            );

            setX(getX() + moveX);
            setY(getY() + moveY);

            clampToWorld();
        });

        recordPosition();
    }

    @Override
    protected int getStarvationRate() {

        if (isInDanger()) {
            return starvationRate * 2;
        }

        return starvationRate;
    }

    @Override
    public void eat() {

        // Streams: find grass that can be eaten
        List<Grass> edibleGrass = grassList.stream()
                .filter(Grass::isEdible)
                .toList();

        Optional<Grass> closest = findClosest(edibleGrass);

        closest.ifPresent(grass -> {

            double dist = Math.hypot(
                    getX() - grass.getX(),
                    getY() - grass.getY()
            );

            if (dist <= EAT_RADIUS) {
                grass.setEaten(true);
                resetStarvation();
            }
        });
    }
}
