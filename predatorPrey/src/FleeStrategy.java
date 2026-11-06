
public class FleeStrategy implements MovementStrategy {

    private final double speedMultiplier;

    public FleeStrategy(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void move(Creature creature, Creature target) {

        if (target == null) {
            creature.moveWithBounce();
            return;
        }

        double diffX = creature.getX() - target.getX();
        double diffY = creature.getY() - target.getY();

        double distance = Math.hypot(diffX, diffY);

        if (distance == 0) {
            diffX = 1;
            distance = 1;
        }

        double boostedSpeed = creature.getSpeed() * speedMultiplier;

        int moveX = (int) Math.round(
                (diffX / distance) * boostedSpeed
        );

        int moveY = (int) Math.round(
                (diffY / distance) * boostedSpeed
        );

        creature.setX(creature.getX() + moveX);
        creature.setY(creature.getY() + moveY);

        creature.clampToWorld();
    }
}
