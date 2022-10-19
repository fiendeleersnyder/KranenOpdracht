public class Traject {
    private int start_x;
    private int start_y;
    private int eind_x;
    private int eind_y;
    private int starttijd;
    private int eindtijd;

    public Traject(int start_x, int start_y, int eind_x, int eind_y, int starttijd, int eindtijd) {
        this.start_x = start_x;
        this.start_y = start_y;
        this.eind_x = eind_x;
        this.eind_y = eind_y;
        this.starttijd = starttijd;
        this.eindtijd = eindtijd;
    }

    public int getStart_x() {
        return start_x;
    }

    public void setStart_x(int start_x) {
        this.start_x = start_x;
    }

    public int getStart_y() {
        return start_y;
    }

    public void setStart_y(int start_y) {
        this.start_y = start_y;
    }

    public int getEind_x() {
        return eind_x;
    }

    public void setEind_x(int eind_x) {
        this.eind_x = eind_x;
    }

    public int getEind_y() {
        return eind_y;
    }

    public void setEind_y(int eind_y) {
        this.eind_y = eind_y;
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
}
