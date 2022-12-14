import java.util.Stack;

public class Slot {
    private int id,x,y;
    private Stack<Container> hoogte;

    public Slot(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.hoogte = new Stack();
    }

    public Slot(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getHoogte() {
        return hoogte.size();
    }

    public void voegContainerToe(Container c) {
        this.hoogte.push(c);
    }

    public void verwijderContainer() { this.hoogte.pop(); }

    public Stack<Container> getStack() {
        return hoogte;
    }

    public Container getContainer(int i) {
        return hoogte.get(i);
    }
}
