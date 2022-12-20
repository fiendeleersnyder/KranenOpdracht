import java.util.ArrayList;

public class Kraan {
    private int id;
    private double x_coordinaat;
    private double y_coordinaat;
    private double x_minimum;
    private double y_minimum;
    private double x_maximum;
    private double y_maximum;
    private double x_snelheid;
    private double y_snelheid;
    private ArrayList<Traject> trajectory;

    public Kraan(int id, double x_coordinaat, double y_coordinaat, double x_minimum, double y_minimum, double x_maximum, double y_maximum, double x_snelheid, double y_snelheid) {
        this.id = id;
        this.x_coordinaat = x_coordinaat;
        this.y_coordinaat = y_coordinaat;
        this.x_minimum = x_minimum;
        this.y_minimum = y_minimum;
        this.x_maximum = x_maximum;
        this.y_maximum = y_maximum;
        this.x_snelheid = x_snelheid;
        this.y_snelheid = y_snelheid;
        this.trajectory = new ArrayList<>();
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public double getX_coordinaat() { return x_coordinaat; }

    public void setX_coordinaat(double x_coordinaat) { this.x_coordinaat = x_coordinaat; }

    public double getY_coordinaat() { return y_coordinaat; }

    public void setY_coordinaat(double y_coordinaat) { this.y_coordinaat = y_coordinaat; }

    public double getX_minimum() { return x_minimum; }

    public void setX_minimum(double x_minimum) { this.x_minimum = x_minimum; }

    public double getY_minimum() { return y_minimum; }

    public void setY_minimum(double y_minimum) { this.y_minimum = y_minimum; }

    public double getX_maximum() { return x_maximum; }

    public void setX_maximum(double x_maximum) { this.x_maximum = x_maximum; }

    public double getY_maximum() { return y_maximum; }

    public void setY_maximum(double y_maximum) { this.y_maximum = y_maximum; }

    public double getX_snelheid() { return x_snelheid; }

    public void setX_snelheid(double x_snelheid) { this.x_snelheid = x_snelheid; }

    public double getY_snelheid() { return y_snelheid; }

    public void setY_snelheid(double y_snelheid) { this.y_snelheid = y_snelheid; }

    public ArrayList<Traject> getTrajectory() { return trajectory; }

    public void setTrajectory(ArrayList<Traject> trajectory) { this.trajectory = trajectory; }
}