import java.util.ArrayList;

public class Assignment {
    private int container_id;
    ArrayList<Slot> slots;

    public Assignment(int container_id) {
        this.container_id = container_id;
        this.slots= new ArrayList<>();
    }

    public void addSlot(Slot s) {
        slots.add(s);
    }

    public int getContainer_id(){ return container_id;}

    public ArrayList<Slot> getSlots() { return slots; }

    public void setSlot_id(ArrayList<Slot> slots) { this.slots = slots; }
}
