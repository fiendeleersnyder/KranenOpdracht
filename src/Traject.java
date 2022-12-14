public class Traject {
    private int start_crane_x;
    private int start_crane_y;
    private int startTime_crane;
    private int pickup_x;
    private int pickup_y;
    private int dropoff_x;
    private int dropoff_y;
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

    public int getPickup_x() { return pickup_x; }

    public void setPickup_x(int pickup_x) { this.pickup_x = pickup_x; }

    public int getPickup_y() { return pickup_y; }

    public void setPickup_y(int pickup_y) { this.pickup_y = pickup_y; }

    public int getDropoff_x() { return dropoff_x; }

    public void setDropoff_x(int dropoff_x) { this.dropoff_x = dropoff_x; }

    public int getDropoff_y() { return dropoff_y; }

    public void setDropoff_y(int dropoff_y) { this.dropoff_y = dropoff_y; }

    public Kraan getKraan() { return kraan; }

    public int getStart_crane_x() {
        return start_crane_x;
    }

    public void setStart_crane_x(int start_crane_x) {
        this.start_crane_x = start_crane_x;
    }

    public int getStart_crane_y() {
        return start_crane_y;
    }

    public void setStart_crane_y(int start_crane_y) {
        this.start_crane_y = start_crane_y;
    }

    public int getStartTime_crane() {
        return startTime_crane;
    }

    public void setStartTime_crane(int startTime_crane) {
        this.startTime_crane = startTime_crane;
    }

    public void setKraan(Kraan kraan) { this.kraan = kraan; }

    public int getContainer_id() { return container_id; }

    public void setContainer_id(int container_id) { this.container_id = container_id; }

    public int getStarttijd() { return starttijd; }

    public void setStarttijd(int starttijd) { this.starttijd = starttijd; }

    public int getEindtijd() { return eindtijd; }

    public void setEindtijd(int eindtijd) { this.eindtijd = eindtijd;}
}
