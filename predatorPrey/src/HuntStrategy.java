
public class HuntStrategy implements MovementStrategy {

    private final double speedMultiplier;

    public HuntStrategy(double speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    @Override
    public void move(Creature creature, Creature target) {

        if (target == null) {
            creature.moveWithBounce();
            return;
        }

        int velX = target.getVelocityX();
        int velY = target.getVelocityY();

        double huntSpeed = creature.getSpeed() * speedMultiplier;

        double distance = Math.hypot(
                target.getX() - creature.getX(),
                target.getY() - creature.getY()
        );

        if (distance == 0 || huntSpeed <= 0) {
            return;
        }

        double lookAheadTicks = Math.min(distance / huntSpeed, 10);

        int predictedX = target.getX()
                + (int) (velX * lookAheadTicks);

        int predictedY = target.getY()
                + (int) (velY * lookAheadTicks);

        double diffX = predictedX - creature.getX();
        double diffY = predictedY - creature.getY();

        double dist = Math.hypot(diffX, diffY);

        if (dist == 0) {
            return;
        }

        double moveDistance = Math.min(huntSpeed, dist);

        int moveX = (int) Math.round(
                (diffX / dist) * moveDistance
        );

        int moveY = (int) Math.round(
                (diffY / dist) * moveDistance
        );

        if (moveX == 0 && Math.abs(diffX) > 0.1) {
            moveX = (int) Math.signum(diffX);
        }

        if (moveY == 0 && Math.abs(diffY) > 0.1) {
            moveY = (int) Math.signum(diffY);
        }

        creature.setX(creature.getX() + moveX);
        creature.setY(creature.getY() + moveY);

        creature.clampToWorld();
    }
}
