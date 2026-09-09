public class Predator extends Creature {

    public Predator(int speed, int hunger, int x, int y) {
        super(speed, hunger, false, x, y);
    }

    public void chase(Prey prey, int width, int height)
            throws InvalidPositionException {

        int newX = getX();
        int newY = getY();

        if (prey.getX() > getX()) {
            newX++;
        } else if (prey.getX() < getX()) {
            newX--;
        }

        if (prey.getY() > getY()) {
            newY++;
        } else if (prey.getY() < getY()) {
            newY--;
        }

        moveTo(newX, newY, width, height);
    }
}