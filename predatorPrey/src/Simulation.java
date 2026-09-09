import java.util.ArrayList;

public class Simulation {

    private ArrayList<Entity> entities;

    public Simulation() {
        entities = new ArrayList<>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public ArrayList<Entity> getEntities() {
        return entities;
    }

    public int getEntityCount() {
        return entities.size();
    }
}