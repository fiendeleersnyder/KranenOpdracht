import java.awt.*;
import java.io.*;
import java.sql.Array;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.*;
import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        JSONParser jsonParser = new JSONParser();
        ArrayList<Slot> slots = new ArrayList<>();
        ArrayList<Container> containers = new ArrayList<>();
        Set<Assignment> assignments = new HashSet<>();
        ArrayList<Kraan> kranen = new ArrayList<>();
        int length = 0;
        int width = 0;

        int maxHeight = 0;

        int max_x = 0;
        int max_y = 0;

        try (FileReader reader = new FileReader("data/terminal22_1_100_1_10.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            String name = (String)jsonObject.get("name");
            length = ((Long) jsonObject.get("length")).intValue();
            width = ((Long)jsonObject.get("width")).intValue();
            maxHeight = ((Long)jsonObject.get("maxheight")).intValue();


            JSONArray slotList = (JSONArray) jsonObject.get("slots");
            slotList.forEach( data -> parseSlots( (JSONObject) data, slots) );

            JSONArray craneList = (JSONArray) jsonObject.get("cranes");
            craneList.forEach( data -> parseCranes( (JSONObject) data, kranen) );

            JSONArray containerList = (JSONArray) jsonObject.get("containers");
            containerList.forEach( data -> parseContainer( (JSONObject) data, containers ) );
            containers.sort(Comparator.comparing(Container::getId));

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            assignmentList.forEach( data -> parseAssignment( (JSONObject) data, assignments ,slots, containers) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        String finishName;
        int finishMaxHeight = 0;
        Set<Assignment> toFinishAssignments = new HashSet<>();

        try (FileReader reader = new FileReader("data/terminal22_1_100_1_10target.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            finishName = (String)jsonObject.get("name");
            finishMaxHeight = ((Long)jsonObject.get("maxheight")).intValue();

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            assignmentList.forEach( data -> parseAssignment( (JSONObject) data, toFinishAssignments ,slots, containers) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        CheckerBoardJavaExample CheckerBoard = new CheckerBoardJavaExample(width,length);
        final int SIZE = 600;
        CheckerBoard.setSize(SIZE, SIZE);
        JPanel legende = new JPanel();
        legende.setSize(300, 600);
        CheckerBoard.setVisible(true);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JScrollPane scrollPane = new JScrollPane(CheckerBoard);
        panel.add(scrollPane);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel, legende);
        pane.setSize(600,600);
        pane.setVisible(true);


        JTextArea textArea = new JTextArea("""
                Wit: 0 containers
                Paars: 1 container
                Donkerblauw: 2 containers
                Lichtblauw: 3 containers
                Groen: 4 containers
                Geel: 5 containers
                Oranje: 6 containers
                Rood:  7 containers
                Roos: 8 containers
                Grijs: 9 containers
                Zwart: 10 containers""");

        textArea.setEditable(false);
        legende.add(textArea);

        JFrame frame = new JFrame();
        frame.add(pane);
        frame.setSize(600, 600);
        frame.setTitle("Kranenopdracht");
        frame.setVisible(true);

        setStartPositie(assignments, containers, CheckerBoard);

        doAssignments(toFinishAssignments, kranen, containers, maxHeight);

        ArrayList<Slot> toegewezenSlots = new ArrayList<>();

        /*for (Slot slot: slots) {
            if (slot.getId() == 2 || slot.getId() == 3) {
                toegewezenSlots.add(slot);
            }
        }*/

        //verplaatsContainer(containers.get(3), slots, toegewezenSlots, CheckerBoard);

        /*CheckerBoard.veranderKleur(5, 3, 0);
        CheckerBoard.veranderKleur(5, 4, 1);
        CheckerBoard.veranderKleur(5, 5, 2);
        CheckerBoard.veranderKleur(5, 6, 3);
        CheckerBoard.veranderKleur(5, 7, 4);
        CheckerBoard.veranderKleur(5, 8, 5);
        CheckerBoard.veranderKleur(5, 9, 6);
        CheckerBoard.veranderKleur(5, 10, 7);
        CheckerBoard.veranderKleur(5, 11, 8);
        CheckerBoard.veranderKleur(5, 12, 9);
        CheckerBoard.veranderKleur(5, 31, 10);*/

        /*containers.sort(Comparator.comparing(Container::getLengte));

        ArrayList<Slot> slotlist;
        boolean plaatsen = true;
        ArrayList<Assignment> wachtlijst = assignments;

        while (!wachtlijst.isEmpty()) {
            for (Container c : containers) {
                for (Assignment assignment : wachtlijst) {
                    if (assignment.getContainer_id() == c.getId()) {
                        slotlist = assignment.getSlots();
                        for (int i = 1; i < slotlist.size(); i++) {
                            int hoogteStart = slotlist.get(0).getHoogte();
                            if (slotlist.get(i).getHoogte() != hoogteStart) {
                                plaatsen = false;
                            }
                        }
                        if (plaatsen) {
                            //controleren of start en eindslot van container al geset zijn
                            // zo nee: container moet gewoon geplaatst worden en éénmaal kleuren zetten
                            // zo ja: container moet verplaatst worden dus kleuren moeten zowel verwijderen als toevoegen
                            if (c.getStart() != null && c.getEind() != null) {
                                //container wordt van huidige locatie gehaald --> kleuren aanpassen
                                int kleur = c.getStart().getHoogte()-1;
                                for (int i=c.getStart().getX(); i < c.getEind().getX() ; i++) {
                                    for (int j = c.getStart().getY(); j < c.getEind().getY(); j++) {
                                        CheckerBoard.veranderKleur(i,j,kleur);
                                        for (Slot slot: slots) {
                                            if (slot.getX() == i && slot.getY() == j) {
                                                slot.verwijderContainer();
                                            }
                                        }
                                    }
                                }
                            }
                            c.setStart(slotlist.get(0));
                            c.setEind(slotlist.get(-1));
                            for(Slot slot: slotlist) {
                                slot.voegContainerToe(c);
                                int hoogte_container = slot.getHoogte();
                                CheckerBoard.veranderKleur(slot.getX(), slot.getY(),hoogte_container);
                            }
                        }
                    }
                }
            }
        }*/

    }

    private static void parseSlots(JSONObject data,ArrayList<Slot> slots){
        int id = ((Long) data.get("id")).intValue();
        int x = ((Long) data.get("x")).intValue();
        int y = ((Long) data.get("y")).intValue();
        slots.add(new Slot(id, x, y));
    }

    private static void parseCranes(JSONObject data,ArrayList<Kraan> kranen){
        int id = ((Long) data.get("id")).intValue();
        double x = ((Number) data.get("x")).doubleValue();
        double y = ((Number) data.get("y")).doubleValue();
        double xmin = ((Number) data.get("xmin")).doubleValue();
        double ymin = ((Number) data.get("ymin")).doubleValue();
        double xmax = ((Number) data.get("xmax")).doubleValue();
        double ymax = ((Number) data.get("ymax")).doubleValue();
        double xspeed = ((Number) data.get("xspeed")).doubleValue();
        double yspeed = ((Number) data.get("yspeed")).doubleValue();
        kranen.add(new Kraan(id, x, y, xmin, ymin, xmax, ymax, xspeed, yspeed));
    }

    private static void parseContainer(JSONObject data,ArrayList<Container> containers){
        int id = ((Long) data.get("id")).intValue();
        int lengte = ((Long) data.get("length")).intValue();
        containers.add(new Container(id, lengte));
    }

    private static void parseAssignment(JSONObject data, Set<Assignment> assignement, ArrayList<Slot> slotLijst, ArrayList<Container> containers){
        Long slot_id = (Long) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        Assignment assignment = new Assignment(container_id);
        Container container = containers.get(container_id-1);

        for(int j=0;j<container.getLengte();j++){
            for(int i=0; i<slotLijst.size(); i++){
                if(slotLijst.get(i).getId()== slot_id + j){
                    assignment.addSlot(slotLijst.get(i));
                    break;
                }
            }
        }
        assignement.add(assignment);
   }

   private static void setStartPositie(Set<Assignment> assignments, ArrayList<Container> containers, CheckerBoardJavaExample checkerboard) {
       containers.sort(Comparator.comparing(Container::getLengte));

       ArrayList<Slot> slotlist;
       boolean plaatsen = true;
       Set<Assignment> wachtlijst = assignments;
       int hoogteStart;

       while (!wachtlijst.isEmpty()) {
           for (Container c : containers) {
               ArrayList<Assignment> executed_assignments = new ArrayList<>();
               for (Assignment assignment : wachtlijst) {
                   if (assignment.getContainer_id() == c.getId()) {
                       slotlist = assignment.getSlots();
                       for (int i = 0; i < slotlist.size(); i++) {
                           hoogteStart = slotlist.get(0).getHoogte();
                           if (slotlist.get(i).getHoogte() != hoogteStart) {
                               plaatsen = false;
                           }
                       }
                       if (plaatsen) {
                           plaatsContainer(c, slotlist, checkerboard);
                           executed_assignments.add(assignment);
                       }
                   }
               }
               for(Assignment a: executed_assignments) {
                   wachtlijst.remove(a);
               }
           }
       }
   }

   public static void plaatsContainer(Container c, ArrayList<Slot> toegewezenSlots, CheckerBoardJavaExample checkerboard) {
       c.setStart(toegewezenSlots.get(0));
       c.setEind(toegewezenSlots.get(toegewezenSlots.size()-1));
       for(Slot slot: toegewezenSlots) {
           slot.voegContainerToe(c);
           int hoogte_container = slot.getHoogte();
           checkerboard.veranderKleur(slot.getX(), slot.getY(),hoogte_container);
       }
   }

   public static void verplaatsContainer(Container container, ArrayList<Slot> alleSlots, ArrayList<Slot> toegewezenSlots, CheckerBoardJavaExample checkerBoard) {
        //container wordt van huidige locatie gehaald --> kleuren aanpassen
       int kleur = container.getStart().getHoogte() - 1;
       for (int i = container.getStart().getX(); i < container.getEind().getX()+1; i++) {
           for (int j = container.getStart().getY(); j < container.getEind().getY()+1; j++) {
               checkerBoard.veranderKleur(i, j, kleur);
               for (Slot slot : alleSlots) {
                   if (slot.getX() == i && slot.getY() == j) {
                       slot.verwijderContainer();
                   }
               }
           }
       }
       plaatsContainer(container, toegewezenSlots, checkerBoard);
   }

   public static void doAssignments(Set<Assignment> toFinishAssignments, ArrayList<Kraan> kranen, ArrayList<Container> containers, int max_hoogte ) {
        while (!toFinishAssignments.isEmpty()) {
            ArrayList<Assignment> executed_assignments = new ArrayList<>();
            for (Assignment assignment : toFinishAssignments) {
                int positieContainerX = containers.get(assignment.getContainer_id() - 1).getStart().getX();
                double positiePickupX = positieContainerX + (containers.get(assignment.getContainer_id() - 1).getLengte() / 2);

                int positieContainerY = containers.get(assignment.getContainer_id() - 1).getStart().getY();
                double positiePickupY = positieContainerY + 0.5;

                if (positieContainerX == assignment.getSlots().get(0).getX() && positieContainerY == assignment.getSlots().get(0).getY()) {
                    continue;
                }

                boolean hoogteOke = true;
                for (Slot slot : assignment.getSlots()) {
                    if (slot.getHoogte() + 1 > max_hoogte) {
                        hoogteOke = false;
                        break;
                    }
                }

                boolean containerEronderOke = false;
                if(hoogteOke){
                    Slot slotBegin = assignment.getSlots().get(0);
                    Slot slotEind = assignment.getSlots().get(assignment.getSlots().size()-1);
                    if (slotBegin.getHoogte() != 0){
                        Container containerBegin = slotBegin.getStack().get(slotBegin.getHoogte()-1);
                        Container containerEind = slotEind.getStack().get(slotEind.getHoogte()-1);
                        if (containerBegin.getStart() == slotBegin && containerEind.getEind() == slotEind) {
                            containerEronderOke = true;
                        }
                    }
                }

                ArrayList<Kraan> kranenToDoAssignment = new ArrayList<>();
                double positieEindX = assignment.getSlots().get(0).getX() + (containers.get(assignment.getContainer_id() - 1).getLengte() / 2);
                double positieEindY = assignment.getSlots().get(0).getY() + 0.5;
                if (containerEronderOke) {
                    double positieBeginX = positiePickupX;
                    double positieBeginY = positiePickupY;

                    while (positieBeginX != positieEindX && positieBeginY != positieEindY) {
                        for (Kraan kraan : kranen) {
                            if (positieBeginX <= kraan.getX_maximum() && positieBeginX >= kraan.getX_minimum() && positieBeginY <= kraan.getY_maximum() && positieBeginY >= kraan.getY_minimum()) {
                                kranenToDoAssignment.add(kraan);
                                if (positieEindX <= kraan.getX_maximum() && positieEindX >= kraan.getX_minimum() && positieEindY <= kraan.getY_maximum() && positieEindY >= kraan.getY_minimum()) {
                                    positieEindX = positieBeginX;
                                    positieEindY = positieBeginY;
                                    break;
                                } else {
                                    if (moveLeft(positieEindX, positieBeginX)) {
                                        positieBeginX = kraan.getX_minimum();
                                    } else {
                                        positieBeginX = kraan.getX_maximum();
                                    }
                                    if (moveUp(positieEindY, positieBeginY)) {
                                        positieBeginY = kraan.getY_minimum();
                                    } else {
                                        positieBeginY = kraan.getY_maximum();
                                    }
                                }

                            }
                        }
                    }
                }

                //elke kraan gaat een verplaatsing doen, eindx is de plek waar de container moet komen of een minimum of maximum van een kraan
                //de volgende kraan zal dan vanaf dat minimum of maximum verder werken en richting de eindplek te komen
                double beginX = positiePickupX;
                for (int i = 0; i < kranenToDoAssignment.size(); i++) {
                    double eindX = positieEindX;
                    if (kranenToDoAssignment.get(i).getX_minimum() > eindX || kranenToDoAssignment.get(i).getX_maximum() < eindX) {
                        if (moveLeft(eindX, beginX)) {
                            eindX = kranenToDoAssignment.get(i).getX_minimum();
                        }
                        else {
                            eindX = kranenToDoAssignment.get(i).getX_maximum();
                        }
                    }
                    zetKranenOpzij(kranen, kranenToDoAssignment.get(i), beginX, eindX);
                    //ik weet niet als er verder nog iets moet gecontroleerd worden, maar zo nee dan kan je denk ik hier de functie verplaatscontainer oproepen
                    //de doAssignments functie zal wel nog wat extra parameters moeten meekrijgen dan
                    //wel nog de sloten waar de container opmoet in een lijst steken denk ik?
                    //als de container tussendoor nog moet ergens neergezet worden zal dit wel nog wat werk vereisen denk ik want momenteel wordt hier enkel met coördinaten gewerkt
                    //mss de lijst met sloten overlopen en kijken welke bij het minimum of maximum horen? geen idee is het eerste waar ik aan denk
                    beginX = eindX;
                }




                boolean kranenOke = false;
                if(containerEronderOke){

                }
                if (kranenOke) {

                }


            }
            for (Assignment assignment: executed_assignments) {
                toFinishAssignments.remove(assignment);
            }
        }
   }

    public static boolean moveLeft(double x_eind, double x_begin) {
        if (x_eind < x_begin) {
            return true;
        }
        return false;
    }

    public static boolean moveUp(Double y_eind, double y_begin) {
        if (y_eind < y_begin) {
            return true;
        }
        return false;
    }


    //hierin heb ik al gecontroleerd als een andere kraan op het pad staat dat de kraan zou moeten afleggen, zo ja wordt deze andere kraan verplaats zodat die niet meer in de weg staat
    public static void zetKranenOpzij(ArrayList<Kraan> kranen, Kraan kraan, double beginX, double eindX) {
        for(Kraan andereKraan: kranen){
            if (!Objects.equals(andereKraan, kraan)) {
                if (moveLeft(eindX, beginX)) {
                    if (andereKraan.getX_coordinaat() >= beginX && andereKraan.getX_coordinaat() <= eindX) {
                        if ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2) + 1 < beginX || ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) - 1 > eindX) {
                            andereKraan.setX_coordinaat((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2));
                        }
                        else if (beginX-1 >= andereKraan.getX_minimum() && beginX-1 <= andereKraan.getX_maximum()){
                            andereKraan.setX_coordinaat(beginX-1);
                        }
                        else if (eindX + 1 >= andereKraan.getX_minimum() && eindX + 1 <= andereKraan.getX_maximum()) {
                            andereKraan.setX_coordinaat(eindX + 1);
                        }
                        else if (andereKraan.getX_minimum() < beginX-1) {
                            andereKraan.setX_coordinaat(andereKraan.getX_minimum());
                        }
                        else if (andereKraan.getX_maximum() > eindX + 1) {
                            andereKraan.setX_coordinaat(andereKraan.getX_maximum());
                        }
                    }
                }
                else {
                    if (andereKraan.getX_coordinaat() <= beginX && andereKraan.getX_coordinaat() >= eindX) {
                        if ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2) + 1 > beginX || ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) - 1 < eindX) {
                            andereKraan.setX_coordinaat((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2));
                        }
                        else if (beginX-1 <= andereKraan.getX_minimum() && beginX-1 >= andereKraan.getX_maximum()){
                            andereKraan.setX_coordinaat(beginX-1);
                        }
                        else if (eindX + 1 <= andereKraan.getX_minimum() && eindX + 1 >= andereKraan.getX_maximum()) {
                            andereKraan.setX_coordinaat(eindX + 1);
                        }
                        else if (andereKraan.getX_minimum() > beginX-1) {
                            andereKraan.setX_coordinaat(andereKraan.getX_minimum());
                        }
                        else if (andereKraan.getX_maximum() < eindX + 1) {
                            andereKraan.setX_coordinaat(andereKraan.getX_maximum());
                        }
                    }

                    //if Xanderekraan is een element van [Xhuidigekraan, Xcontainer + lengte]
                    //if positie andere kraan is tussen huidige kraanpositie en gewenste plaats container + lengte --> kraan empty move laten doen om te verplaatsen
                    // else gwn verplaatsen
                }
            }
            //op dit moment zou er geen enkele kraan tussen het start en eindpunt mogen staan (bij 2 kranen toch)
        }

    }
}

//lijst met trajecten


//assignement uit lijst halen, vervolgens controleren welke kraan deze zou moeten oppikken: checken of slot waar container staat + (lengte/2) in bereik van container ligt
// controleren of container kan verplaatst worden qua hoogte en middenshit en of andere kraan niet in de weg staat
        //kan container niet verplaatst worden qua hoogte of middenshit --> voeg assignement toe op einde van todo lijst
        // kan container niet verplaatst worden qua andere container in de weg --> empty move om deze weg te halen en dan verdergaan
// startpostitie kranen setten + tijd setten
// TRAJECT: pickups van traject setten  + starttijd van pickup
// TRAJECT: positie aanpassen en tijden berekenen
// GUI: kleuren aanpassen
// TRAJECT: tijden setten en eindpositie setten
// KRAAN: in kraan huidige locatie aanpassen.



// positie container is coordinaat van container + (lengte/2)
