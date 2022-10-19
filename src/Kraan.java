import java.util.ArrayList;

public class Kraan {
    private int x_coordinaat;
    private int y_coordinaat;
    private int x_snelheid;
    private int y_snelheid;
    private ArrayList<Traject> trajectory;

    public Kraan(int x_coordinaat, int y_coordinaat, int x_snelheid, int y_snelheid) {
        this.x_coordinaat = x_coordinaat;
        this.y_coordinaat = y_coordinaat;
        this.x_snelheid = x_snelheid;
        this.y_snelheid = y_snelheid;
        this.trajectory = new ArrayList<>();
    }

    public int getX_coordinaat() {
        return x_coordinaat;
    }

    public void setX_coordinaat(int x_coordinaat) {
        this.x_coordinaat = x_coordinaat;
    }

    public int getY_coordinaat() {
        return y_coordinaat;
    }

    public void setY_coordinaat(int y_coordinaat) {
        this.y_coordinaat = y_coordinaat;
    }

    public int getX_snelheid() {
        return x_snelheid;
    }

    public void setX_snelheid(int x_snelheid) {
        this.x_snelheid = x_snelheid;
    }

    public int getY_snelheid() {
        return y_snelheid;
    }

    public void setY_snelheid(int y_snelheid) {
        this.y_snelheid = y_snelheid;
    }

    public ArrayList<Traject> getTrajectory() {
        return trajectory;
    }

    public void setTrajectory(ArrayList<Traject> trajectory) {
        this.trajectory = trajectory;
    }
}
