
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public abstract class Creature extends Entity {

    private int speed;
    private int starvation;

    // Position and movement tracking
    private int lastX;
    private int lastY;
    private int velocityX;
    private int velocityY;

    private int dx;
    private int dy;

    private static final Random random = new Random();
    private static final int STARVATION_THRESHOLD = 100;

    // World dimensions shared with the simulation
    public static final int WORLD_WIDTH = 1500;
    public static final int WORLD_HEIGHT = 800;
    protected static final int SIZE = 20;

    public Creature(int speed, int starvation, boolean isFood, int x, int y) {
        super(x, y, isFood);

        this.speed = speed;
        this.starvation = starvation;

        this.lastX = x;
        this.lastY = y;

        // No movement has happened when the creature is created
        this.velocityX = 0;
        this.velocityY = 0;

        this.dx = random.nextBoolean() ? 1 : -1;
        this.dy = random.nextBoolean() ? 1 : -1;
    }

    protected abstract int getStarvationRate();

    public boolean starve() {
        starvation += getStarvationRate();
        return starvation >= STARVATION_THRESHOLD;
    }

    // Save the most recent movement before updating previous position
    protected void recordPosition() {

        velocityX = getX() - lastX;
        velocityY = getY() - lastY;

        lastX = getX();
        lastY = getY();
    }

    // Used by HuntStrategy to predict target movement
    public int getVelocityX() {
        return velocityX;
    }

    public int getVelocityY() {
        return velocityY;
    }

    // Normal wandering movement with world boundary bouncing
    protected void moveWithBounce() {

        int newX = getX() + dx * getSpeed();
        int newY = getY() + dy * getSpeed();

        if (newX < 0 || newX > WORLD_WIDTH - SIZE) {
            dx = -dx;
            newX = getX() + dx * getSpeed();
        }

        if (newY < 0 || newY > WORLD_HEIGHT - SIZE) {
            dy = -dy;
            newY = getY() + dy * getSpeed();
        }

        setX(Math.max(0, Math.min(newX, WORLD_WIDTH - SIZE)));
        setY(Math.max(0, Math.min(newY, WORLD_HEIGHT - SIZE)));
    }

    // Keep creatures inside the visible world
    protected void clampToWorld() {

        setX(Math.max(0, Math.min(getX(), WORLD_WIDTH - SIZE)));
        setY(Math.max(0, Math.min(getY(), WORLD_HEIGHT - SIZE)));
    }

    public void resetStarvation() {
        starvation = 0;
    }

    protected void addStarvation(int amount) {
        starvation += amount;
    }

    public abstract void movement();

    public abstract void eat();

    public abstract void reproduce();

    // Find the nearest entity using generics, Streams and a Comparator
    public <E extends Entity> Optional<E> findClosest(List<E> targets) {

        return targets.stream()
                .min(Comparator.comparingDouble(
                        target -> Math.hypot(
                                getX() - target.getX(),
                                getY() - target.getY()
                        )
                ));
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getStarvation() {
        return starvation;
    }

    // Filter creatures by range using a Stream
    protected <E extends Creature> Optional<E> findNearestInRange(
            List<E> candidates, int range) {

        List<E> withinRange = candidates.stream()
                .filter(c -> Math.hypot(
                        getX() - c.getX(),
                        getY() - c.getY()
                ) <= range)
                .toList();

        return findClosest(withinRange);
    }
}
