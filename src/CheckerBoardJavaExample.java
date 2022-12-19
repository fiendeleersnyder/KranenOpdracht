import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CheckerBoardJavaExample extends JPanel {
    private int ROWS;
    private int COLS;
    private int NUM;
    private JPanel pnl;
    private JPanel[] pnl1;
    private JPanel[] pnl2;
    private JPanel pnlLegende;
    private Color clr = Color.WHITE; // 0 containers
    private Color paars = Color.MAGENTA; // 1 container
    private Color blauw = Color.BLUE; // 2 containers
    private Color lichtblauw = Color.CYAN; // 3 containers
    private Color groen = Color.GREEN; // 4 containers
    private Color geel = Color.YELLOW; // 5 containers
    private Color oranje = Color.ORANGE; // 6 containers
    private Color rood = Color.RED; // 7 containers
    private Color roos = Color.PINK; // 8 containers
    private Color grijs = Color.LIGHT_GRAY; // 9 containers
    private Color zwart = Color.BLACK; // 10 containers
    private ArrayList<Color> kleuren = new ArrayList<>();

    public CheckerBoardJavaExample(int rows, int cols) {
        maakLijst();
        this.ROWS= rows;
        this.COLS= cols;
        this.pnl = new JPanel(new GridLayout(ROWS, COLS, 2, 2));
        this.NUM = ROWS * COLS;
        this.pnl1 = new JPanel[NUM];
        add(pnl);
        for(int i=0; i<NUM;i++){
            pnl1[i] = new JPanel();
            pnl.add(pnl1[i]);
            pnl1[i].setBackground(clr);
        }
    }

    private void maakLijst() {
        this.kleuren.add(clr);
        this.kleuren.add(paars);
        this.kleuren.add(blauw);
        this.kleuren.add(lichtblauw);
        this.kleuren.add(groen);
        this.kleuren.add(geel);
        this.kleuren.add(oranje);
        this.kleuren.add(rood);
        this.kleuren.add(roos);
        this.kleuren.add(grijs);
        this.kleuren.add(zwart);
    }

    public void veranderKleur(int vakjeX, int vakjeY, int kleur){
        int vak = vakjeX * COLS+ vakjeY;
        this.pnl1[vak].setBackground(kleuren.get(kleur));

    }
}