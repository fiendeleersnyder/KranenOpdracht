import javax.swing.*;
import java.awt.*;

public class CheckerBoardJavaExample extends JPanel {
    private final int ROWS = 32;
    private final int COLS = 32;
    private final int GAP = 2;
    private final int NUM = ROWS * COLS;
    private int x;
    private JPanel pnl = new JPanel(new GridLayout(ROWS, COLS, 2, 2));
    private JPanel[] pnl1 = new JPanel[NUM];
    private Color clr = Color.WHITE;
    private Color clr2 = Color.BLUE;
    private Color tColor;

    public CheckerBoardJavaExample() {
        // in deze methode ook grootte van het schaakbord meegeven zodat dit dynamisch kan worden aangepast
        add(pnl);
        for(x=0; x<NUM;x++){
            pnl1[x] = new JPanel();
            pnl.add(pnl1[x]);
            if(x<ROWS){
                pnl1[x].setBackground(clr);
            }
            else pnl1[x].setBackground(clr2);
        }
//in deze klasse plaatsen we een functie die wordt opgeroepen telkens wanneer een container verplaatst wordt, afhankelijk van het
        // aantal containers is er een andere kleur die wordt gegeven aan het vakje vb groen voor 0, geel voor 1,...
        // functie wordt opgeroepen voor de plaats waar container weggaat en voor plaats waar container komt te staan
    }
}