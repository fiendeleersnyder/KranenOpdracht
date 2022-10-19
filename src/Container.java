public class Container {
    private int gewicht;
    private int lengte;
    private int locatie_x;
    private int locatie_y;

    public Container(int gewicht, int lengte, int locatie_x, int locatie_y) {
        this.gewicht = gewicht;
        this.lengte = lengte;
        this.locatie_x = locatie_x;
        this.locatie_y = locatie_y;
    }

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

    public int getLocatie_x() {
        return locatie_x;
    }

    public void setLocatie_x(int locatie_x) {
        this.locatie_x = locatie_x;
    }

    public int getLocatie_y() {
        return locatie_y;
    }

    public void setLocatie_y(int locatie_y) {
        this.locatie_y = locatie_y;
    }
}
