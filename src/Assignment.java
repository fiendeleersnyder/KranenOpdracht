import java.util.ArrayList;

public class Assignment {
    private int container_id;
    ArrayList<Slot> slot_id;

    public Assignment(int container_id) {
        this.container_id = container_id;
        this.slot_id= new ArrayList<Slot>();
    }

    public void addSlot(Slot s) {
        slot_id.add(s);
    }
}
