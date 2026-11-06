
public class WanderStrategy implements MovementStrategy {

    @Override
    public void move(Creature creature, Creature target) {
        creature.moveWithBounce();
    }

}
