import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        ArrayList<Traject> trajecten = new ArrayList<>();

        String name = "";
        int length = 0;
        int width = 0;

        int maxHeight = 0;
        int targetHeight = 0;

        int max_x = 0;
        int max_y = 0;

        try (FileReader reader = new FileReader("data/1t/TerminalA_20_10_3_2_100.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            name = (String)jsonObject.get("name");
            length = ((Long) jsonObject.get("length")).intValue();
            width = ((Long)jsonObject.get("width")).intValue();
            maxHeight = ((Long)jsonObject.get("maxheight")).intValue();

            if (jsonObject.containsKey("targetheight")) {
                targetHeight = ((Long) jsonObject.get("targetheight")).intValue();
            }

            JSONArray slotList = (JSONArray) jsonObject.get("slots");
            slotList.forEach( data -> parseSlots( (JSONObject) data, slots) );

            JSONArray craneList = (JSONArray) jsonObject.get("cranes");
            craneList.forEach( data -> parseCranes( (JSONObject) data, kranen) );

            JSONArray containerList = (JSONArray) jsonObject.get("containers");
            containerList.forEach( data -> parseContainer( (JSONObject) data, containers ) );
            containers.sort(Comparator.comparing(Container::getId));

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            int finalLength = length;
            assignmentList.forEach(data -> parseAssignment( (JSONObject) data, assignments ,slots, containers, finalLength) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        String finishName;
        int finishMaxHeight = 0;
        Set<Assignment> toFinishAssignments = new HashSet<>();

        try (FileReader reader = new FileReader("data/1t/targetTerminalA_20_10_3_2_100.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            finishName = (String)jsonObject.get("name");
            finishMaxHeight = ((Long)jsonObject.get("maxheight")).intValue();

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            int finalLength1 = length;
            assignmentList.forEach(data -> parseAssignment( (JSONObject) data, toFinishAssignments ,slots, containers, finalLength1) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        CheckerBoardJavaExample CheckerBoard = new CheckerBoardJavaExample(length,width);
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

        JButton b = new JButton("Set initial yard");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStartPositie(assignments, containers, CheckerBoard);
            }
        });
        legende.add(b);

        JButton button = new JButton("Do assignments");
        int finalMaxHeight = maxHeight;
        int finalTargetHeight = targetHeight;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //if(finalTargetHeight!=0) {
                    doAssignments(toFinishAssignments, kranen, containers, finalMaxHeight, slots, CheckerBoard, tijd, trajecten);
                //} //else minimizeHeight();
            }
        });
        legende.add(button);

        JButton b2 = new JButton("Get outputfile");
        String finalName = name;
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                schrijfOutput(trajecten, finalName);
            }
        });
        legende.add(b2);

        legende.add(textArea);

        JFrame frame = new JFrame();
        frame.add(pane);
        frame.setSize(600, 600);
        frame.setTitle("Kranenopdracht");
        frame.setVisible(true);

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

    private static void parseAssignment(JSONObject data, Set<Assignment> assignement, ArrayList<Slot> slotLijst, ArrayList<Container> containers, int lengte){
        Long slot_id = (Long) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        Assignment assignment = new Assignment(container_id);
        Container container = containers.get(container_id);

        for(int j=0;j<container.getLengte();j++){
            for(int i=0; i<slotLijst.size(); i++){
                if(slotLijst.get(i).getId()== slot_id + j * lengte){
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
       boolean plaatsen;
       Set<Assignment> wachtlijst = assignments;
       int hoogteStart;

       while (!wachtlijst.isEmpty()) {
           for (Container c : containers) {
               ArrayList<Assignment> executed_assignments = new ArrayList<>();
               for (Assignment assignment : wachtlijst) {
                   if (assignment.getContainer_id() == c.getId()) {
                       plaatsen = true;
                       slotlist = assignment.getSlots();
                       for (Slot slot : slotlist) {
                           hoogteStart = slotlist.get(0).getHoogte();
                           if (slot.getHoogte() != hoogteStart) {
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
                       break;
                   }
               }
           }
       }
       plaatsContainer(container, toegewezenSlots, checkerBoard);
   }

   public static void doAssignments(Set<Assignment> toFinishAssignments, ArrayList<Kraan> kranen, ArrayList<Container> containers, int max_hoogte, ArrayList<Slot> slots, CheckerBoardJavaExample checkerBoard, int tijd, ArrayList<Traject> trajecten ) {
        while (!toFinishAssignments.isEmpty()) {
            ArrayList<Assignment> executed_assignments = new ArrayList<>();
            for (Assignment assignment : toFinishAssignments) {
                int positieContainerX = containers.get(assignment.getContainer_id()).getStart().getX();
                double positiePickupX = positieContainerX + 0.5;

                int positieContainerY = containers.get(assignment.getContainer_id()).getStart().getY();
                double positiePickupY = positieContainerY + (containers.get(assignment.getContainer_id()).getLengte() / 2.0);

                if (positieContainerX == assignment.getSlots().get(0).getX() && positieContainerY == assignment.getSlots().get(0).getY()) {
                    executed_assignments.add(assignment);
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
                //kranen coordinaten
                double positieEindY = assignment.getSlots().get(0).getY() + (containers.get(assignment.getContainer_id()).getLengte() / 2.0);
                double positieEindX = assignment.getSlots().get(0).getX() + 0.5;
                double eindeX = positieEindX;
                double eindeY = positieEindY;
                if (containerEronderOke) {
                    double positieBeginX = positiePickupX;
                    double positieBeginY = positiePickupY;

                    while (positieBeginX != eindeX || positieBeginY != eindeY) {
                        for (Kraan kraan : kranen) {
                            if (positieBeginX <= kraan.getX_maximum() && positieBeginX >= kraan.getX_minimum() && positieBeginY <= kraan.getY_maximum() && positieBeginY >= kraan.getY_minimum()) {
                                kranenToDoAssignment.add(kraan);
                                if (eindeX <= kraan.getX_maximum() && eindeX >= kraan.getX_minimum() && eindeY <= kraan.getY_maximum() && eindeY >= kraan.getY_minimum()) {
                                    eindeX = positieBeginX;
                                    eindeY = positieBeginY;
                                    break;
                                } else {
                                    if (moveLeft(eindeY, positieBeginY)) {
                                        positieBeginY = kraan.getY_minimum();
                                    }
                                    else if (moveRight(eindeY, positieBeginY)) {
                                        positieBeginY = kraan.getY_maximum();
                                    }
                                    if (moveUp(eindeX, positieBeginX)) {
                                        positieBeginX = kraan.getX_minimum();
                                    }
                                    else if (moveDown(eindeX, positieBeginX)) {
                                        positieBeginX = kraan.getX_maximum();
                                    }
                                }

                            }
                        }
                    }
                }

                //elke kraan gaat een verplaatsing doen, eindx is de plek waar de container moet komen of een minimum of maximum van een kraan
                //de volgende kraan zal dan vanaf dat minimum of maximum verder werken en richting de eindplek te komen
                double beginX = positiePickupX;
                double beginY = positiePickupY;
                ArrayList<Slot> toegewezenSlots;
                for (int i = 0; i < kranenToDoAssignment.size(); i++) {
                    double eindX = positieEindX;
                    double eindY = positieEindY;
                    toegewezenSlots = new ArrayList<>(assignment.getSlots());
                    if (kranenToDoAssignment.get(i).getY_minimum() > eindY || kranenToDoAssignment.get(i).getY_maximum() < eindY) {
                        toegewezenSlots.clear();
                        if (moveLeft(eindY, beginY)) {
                            eindY = kranenToDoAssignment.get(i).getY_minimum();
                        }
                        else if (moveRight(eindY, beginY)){
                            eindY = kranenToDoAssignment.get(i).getY_maximum();
                        }
                        if (moveUp(eindX, beginX)) {
                            eindX = kranenToDoAssignment.get(i).getX_minimum();
                        }
                        else if (moveDown(eindX, beginX)) {
                            eindX = kranenToDoAssignment.get(i).getX_maximum();
                        }
                        for (int j = 0; j < slots.size(); j++) {
                            if (Math.floor(eindX) == slots.get(j).getX() && slots.get(j).getY() == Math.floor(eindY)) {
                                int helft = (int) Math.floor(assignment.getSlots().size() / 2.0);
                                for (int k = -helft; k < assignment.getSlots().size() - helft; k++) {
                                    toegewezenSlots.add(slots.get(j+k));
                                }
                            }
                        }
                    }
                    tijd = zetKranenOpzij(kranen, kranenToDoAssignment.get(i), beginX, eindX, tijd, trajecten);
                    verplaatsContainer(containers.get(assignment.getContainer_id()), slots, toegewezenSlots, checkerBoard);
                    int startPickupTime = berekenPickup(kranenToDoAssignment.get(i), positiePickupX, positiePickupY, tijd);
                    int endPickupTime = berekenVerplaatsing(kranenToDoAssignment.get(i), positiePickupX, positiePickupY, eindX, eindY, tijd);
                    Traject traject = new Traject(kranenToDoAssignment.get(i).getX_coordinaat(), kranenToDoAssignment.get(i).getY_coordinaat(),
                            tijd, positiePickupX, positiePickupY, eindX, eindY, startPickupTime, endPickupTime, kranenToDoAssignment.get(i), assignment.getContainer_id());
                   kranenToDoAssignment.get(i).setX_coordinaat(eindX);
                   kranenToDoAssignment.get(i).setY_coordinaat(eindY);
                   tijd=endPickupTime;
                   trajecten.add(traject);
                   executed_assignments.add(assignment);
                   beginY = eindY;
                   beginX = eindX;
                }

            }
            for (Assignment assignment: executed_assignments) {
                toFinishAssignments.remove(assignment);
            }
        }
        System.out.println("klaar");
   }

    public static boolean moveLeft(double y_eind, double y_begin) {
        if (y_eind < y_begin) {
            return true;
        }
        return false;
    }

    public static boolean moveRight(double y_eind, double y_begin) {
        if (y_eind > y_begin) {
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

    public static boolean moveDown(Double x_eind, double x_begin) {
        if (x_eind > x_begin) {
            return true;
        }
        return false;
    }


    public static int zetKranenOpzij(ArrayList<Kraan> kranen, Kraan kraan, double beginY, double eindY, int tijd, ArrayList<Traject> trajecten) {
        for(Kraan andereKraan: kranen){
            if (!Objects.equals(andereKraan, kraan)) {
                if (moveLeft(eindY, beginY)) {
                    if (andereKraan.getY_coordinaat() >= beginY && andereKraan.getY_coordinaat() <= eindY) {
                        if ((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2) + 1 < beginY || ((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2)) - 1 > eindY) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), (andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2));
                            tijd = eindTijd;
                        }
                        else if (beginY-1 >= andereKraan.getY_minimum() && beginY-1 <= andereKraan.getY_maximum()){
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (beginY-1)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), beginY-1, tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(beginY-1);
                            tijd = eindTijd;
                        }
                        else if (eindY + 1 >= andereKraan.getY_minimum() && eindY + 1 <= andereKraan.getY_maximum()) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (eindY+1)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), eindY + 1, tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(eindY + 1);
                            tijd = eindTijd;
                        }
                        else if (andereKraan.getY_minimum() < beginY-1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - andereKraan.getY_minimum()) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), andereKraan.getY_minimum(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(andereKraan.getY_minimum());
                            tijd = eindTijd;
                        }
                        else if (andereKraan.getY_maximum() > eindY + 1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - andereKraan.getY_maximum()) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), andereKraan.getY_maximum(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(andereKraan.getY_maximum());
                            tijd = eindTijd;
                        }
                    }
                }
                else if (moveRight(eindY, beginY)){
                    if (andereKraan.getY_coordinaat() <= beginY && andereKraan.getY_coordinaat() >= eindY) {
                        if ((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2) + 1 > beginY || ((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2)) - 1 < eindY) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0,andereKraan.getX_coordinaat() , (andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat((andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2));
                            tijd = eindTijd;
                        }
                        else if (beginY-1 <= andereKraan.getY_minimum() && beginY-1 >= andereKraan.getY_maximum()){
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (beginY-1)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), beginY-1, tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(beginY-1);
                            tijd = eindTijd;
                        }
                        else if (eindY + 1 <= andereKraan.getY_minimum() && eindY + 1 >= andereKraan.getY_maximum()) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - (eindY+1)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), eindY + 1, tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(eindY + 1);
                            tijd = eindTijd;
                        }
                        else if (andereKraan.getY_minimum() > beginY-1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - andereKraan.getY_minimum()) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), andereKraan.getY_minimum(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(andereKraan.getY_minimum());
                            tijd = eindTijd;
                        }
                        else if (andereKraan.getY_maximum() < eindY + 1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getY_coordinaat() - andereKraan.getY_maximum()) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), andereKraan.getY_maximum(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setY_coordinaat(andereKraan.getY_maximum());
                            tijd = eindTijd;
                        }
                    }
                }
            }
        }
        return tijd;
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
