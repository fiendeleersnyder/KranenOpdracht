public class Container {
    private int gewicht;
    private int lengte;
    private Slot start;
    private Slot eind;

    public Slot getStart() { return start; }

    public void setStart(Slot start) { this.start = start; }

    public Slot getEind() { return eind; }

    public void setEind(Slot eind) { this.eind = eind; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getGewicht() {
        return gewicht;
    }

    public void setGewicht(int gewicht) {
        this.gewicht = gewicht;
    }

    public int getLengte() {
        return lengte;
    }

    public void setLengte(int lengte) {
        this.lengte = lengte;
    }

}
