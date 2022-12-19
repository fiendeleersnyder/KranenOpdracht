public class Traject {
    private int start_crane_x;
    private int start_crane_y;
    private int startTime_crane;
    private double pickup_x;
    private double pickup_y;
    private double dropoff_x;
    private double dropoff_y;
    private int starttijd;
    private int eindtijd;
    private Kraan kraan;
    private int container_id;

    public Traject(int kraanX, int kraanY, int startKraan, int start_x, int start_y, int eind_x, int eind_y, int starttijd, int eindtijd, Kraan kraan, int id) {
        this.start_crane_x = kraanX;
        this.start_crane_y = kraanY;
        this.startTime_crane = startKraan;
        this.pickup_x = start_x;
        this.pickup_y = start_y;
        this.dropoff_x = eind_x;
        this.dropoff_y = eind_y;
        this.starttijd = starttijd;
        this.eindtijd = eindtijd;
        this.kraan = kraan;
        this.container_id = id;
    }

    public double getStart_crane_x() {
        return start_crane_x;
    }

    public void setStart_crane_x(double start_crane_x) {
        this.start_crane_x = start_crane_x;
    }

    public double getStart_crane_y() {
        return start_crane_y;
    }

    public void setStart_crane_y(double start_crane_y) {
        this.start_crane_y = start_crane_y;
    }

    public int getStartTime_crane() {
        return startTime_crane;
    }

    public void setStartTime_crane(int startTime_crane) {
        this.startTime_crane = startTime_crane;
    }

    public double getPickup_x() {
        return pickup_x;
    }

    public void setPickup_x(double pickup_x) {
        this.pickup_x = pickup_x;
    }

    public double getPickup_y() {
        return pickup_y;
    }

    public void setPickup_y(double pickup_y) {
        this.pickup_y = pickup_y;
    }

    public double getDropoff_x() {
        return dropoff_x;
    }

    public void setDropoff_x(double dropoff_x) {
        this.dropoff_x = dropoff_x;
    }

    public double getDropoff_y() {
        return dropoff_y;
    }

    public void setDropoff_y(double dropoff_y) {
        this.dropoff_y = dropoff_y;
    }

    public int getStarttijd() {
        return starttijd;
    }

    public void setStarttijd(int starttijd) {
        this.starttijd = starttijd;
    }

    public int getEindtijd() {
        return eindtijd;
    }

    public void setEindtijd(int eindtijd) {
        this.eindtijd = eindtijd;
    }

    public Kraan getKraan() {
        return kraan;
    }

    public void setKraan(Kraan kraan) {
        this.kraan = kraan;
    }

    public int getContainer_id() {
        return container_id;
    }

    public void setContainer_id(int container_id) {
        this.container_id = container_id;
    }
}
