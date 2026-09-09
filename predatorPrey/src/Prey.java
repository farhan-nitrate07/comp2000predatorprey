public class Prey extends Creature {

    private boolean inDanger;

    public Prey(int speed, int hunger, boolean isFood, int x, int y) {
        super(speed, hunger, isFood, x, y);
        this.inDanger = false;
    }

    public boolean isInDanger() {
        return inDanger;
    }

    public void setInDanger(boolean inDanger) {
        this.inDanger = inDanger;
    }

    public void escapeFrom(Predator predator, int width, int height)
            throws InvalidPositionException {

        int distanceX = Math.abs(getX() - predator.getX());
        int distanceY = Math.abs(getY() - predator.getY());

        // Predator is considered close if within 4 grid spaces
        inDanger = distanceX <= 4 && distanceY <= 4;

        if (!inDanger) {
            return;
        }

        int newX = getX();
        int newY = getY();

        // Move away horizontally
        if (predator.getX() < getX()) {
            newX++;
        } else if (predator.getX() > getX()) {
            newX--;
        }

        // Move away vertically
        if (predator.getY() < getY()) {
            newY++;
        } else if (predator.getY() > getY()) {
            newY--;
        }

        moveTo(newX, newY, width, height);
    }
}