import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.*;
import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        JSONParser jsonParser = new JSONParser();
        ArrayList<Slot> slots = new ArrayList<>();
        ArrayList<Container> containers = new ArrayList<>();
        ArrayList<Assignment> assignments = new ArrayList<>();
        ArrayList<Kraan> kranen = new ArrayList<>();
        ArrayList<Traject> trajecten = new ArrayList<>();

        String name = "";
        int length = 0;
        int width = 0;

        int maxHeight = 0;
        int targetHeight = 0;

        double tijd = 0;

        try (FileReader reader = new FileReader("data/terminal5_5target.json")) {
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject) obj;

            name = (String) jsonObject.get("name");
            length = ((Long) jsonObject.get("length")).intValue();
            width = ((Long) jsonObject.get("width")).intValue();
            maxHeight = ((Long) jsonObject.get("maxheight")).intValue();

            if (jsonObject.containsKey("targetheight")) {
                targetHeight = ((Long) jsonObject.get("targetheight")).intValue();
            }

            JSONArray slotList = (JSONArray) jsonObject.get("slots");
            slotList.forEach(data -> parseSlots((JSONObject) data, slots));

            JSONArray craneList = (JSONArray) jsonObject.get("cranes");
            craneList.forEach(data -> parseCranes((JSONObject) data, kranen));

            JSONArray containerList = (JSONArray) jsonObject.get("containers");
            containerList.forEach(data -> parseContainer((JSONObject) data, containers));
            containers.sort(Comparator.comparing(Container::getId));

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            assignmentList.forEach(data -> parseAssignment((JSONObject) data, assignments, slots, containers));

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        String finishName;
        int finishMaxHeight = 0;
        ArrayList<Assignment> toFinishAssignments = new ArrayList<>();
        if (targetHeight == 0) {
            try (FileReader reader = new FileReader("data/1t/targetTerminalA_20_10_3_2_100.json")) {
                Object obj = jsonParser.parse(reader);
                JSONObject jsonObject = (JSONObject) obj;

                finishName = (String) jsonObject.get("name");
                finishMaxHeight = ((Long) jsonObject.get("maxheight")).intValue();

                JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
                assignmentList.forEach(data -> parseAssignment((JSONObject) data, toFinishAssignments, slots, containers));

            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }

        CheckerBoard CheckerBoard = new CheckerBoard(width, length);
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
        pane.setSize(600, 600);
        pane.setVisible(true);


        JTextArea textArea = new JTextArea("""
                 White: 0 containers
                Purple: 1 container
                Darkblue: 2 containers
                Lightblue: 3 containers
                Green: 4 containers
                Yellow: 5 containers
                Orange: 6 containers
                Red:  7 containers
                Pink: 8 containers
                Grey: 9 containers
                Black: 10 containers""");

        textArea.setEditable(false);

        JButton b = new JButton("Set initial yard");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setStartPosition(assignments, containers, CheckerBoard);
            }
        });
        legende.add(b);

        JButton button = new JButton("Do assignments");
        int finalMaxHeight = maxHeight;
        int finalTargetHeight = targetHeight;
        int finalLength = length;
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (finalTargetHeight == 0) {
                    checkAndPlace(toFinishAssignments, kranen, containers, finalMaxHeight, slots, CheckerBoard, tijd, trajecten);
                } else
                    minimize(kranen, containers, finalTargetHeight, slots, CheckerBoard, tijd, trajecten, finalLength);
            }
        });
        legende.add(button);

        JButton b2 = new JButton("Get outputfile");
        String finalName = name;
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeOutput(trajecten, finalName);
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

    private static void minimize(ArrayList<Kraan> kranen, ArrayList<Container> containers, int finalTargetHeight, ArrayList<Slot> slots, CheckerBoard checkerBoard, double tijd, ArrayList<Traject> trajecten, int lengte) {
        ArrayList<Container> teHogeContainers = vindTeHogeContainers(slots, finalTargetHeight);
        teHogeContainers.sort(Comparator.comparing(Container::getLengte));

        ArrayList<Slot> legeSlots = findEmptySlots(slots, lengte);
        ArrayList<Container> verplaatsteContainers = new ArrayList<>();

        while (!teHogeContainers.isEmpty()) {
            for (Container container : teHogeContainers) {
                //********* CONTAINER MET LENGTE 1 *********
                if (container.getLengte() == 1) {
                    if (!legeSlots.isEmpty()) {
                        //dichtsbijzijnde slot vinden
                        int X = container.getStart().getX();
                        int Y = container.getStart().getY();

                        int kortsteAfstand = Integer.MAX_VALUE;
                        Slot dichtsteBij = legeSlots.get(0);
                        for (Slot slot : legeSlots) {
                            int x_verplaatsing = Math.abs(X - slot.getX());
                            int y_verplaatsing = Math.abs(Y - slot.getY());

                            if (Math.max(x_verplaatsing, y_verplaatsing) < kortsteAfstand) {
                                kortsteAfstand = Math.max(x_verplaatsing, y_verplaatsing);
                                dichtsteBij = slot;
                            }
                        }
                        //slot gevonden, nu kraan zoeken om verplaatsing te doen
                        ArrayList<Kraan> kranenToDoAssignment = new ArrayList<>();
                        findCranes(X, Y, dichtsteBij.getX(), dichtsteBij.getY(), kranen, kranenToDoAssignment);
                        double positiePickupX = X + (container.getLengte() / 2.0);
                        double positiePickupY = Y + 0.5;

                        double positieEindX = dichtsteBij.getX() + (container.getLengte() / 2.0);
                        double positieEindY = dichtsteBij.getY() + 0.5;

                        ArrayList<Slot> sloten = new ArrayList<>();
                        sloten.add(dichtsteBij);
                        // TODO: moeten kranen nog uit de weg worden gezet?
                        doAssignment(positiePickupX, positiePickupY, kranenToDoAssignment, positieEindX, positieEindY, tijd, slots, sloten, kranen, trajecten, container, checkerBoard);
                        legeSlots.remove(dichtsteBij);
                        verplaatsteContainers.add(container);

                    } else {
                        //overige containers op een andere container van 1 plaatsen
                        int X = container.getStart().getX();
                        int Y = container.getStart().getY();
                        Slot vrijSlot = container.getStart();
                        for (Slot slot : slots) {
                            if (slot.getContainer(slot.getHoogte() - 1).getLengte() == 1 && slot.getHoogte() + 1 <= finalTargetHeight) {
                                vrijSlot = slot;
                                break;
                            } else {
                                //TODO: random containers verplaasten
                                //in het geval dat er nergens plek hebt om 1'tje op te zetten
                                //miss functie random verplaatsen oproepen?
                                //vb kijken of er een container van lengte 2 onderaan staat, deze verplaatsten net als mogelijk bovenstaanden
                                //container van 1 daar plaatsen + container van lengte 1 die ergens bovenaanstaat daar zetten en dan container
                                //van lengte twee erop en alle erboven
                                System.out.println("DE CONTAINER HEEFT GEEN PLAATS, VERPLAATS ANDERE CONTAINERS");
                            }
                        }

                        //slot gevonden, nu kraan zoeken om verplaatsing te doen
                        ArrayList<Kraan> kranenToDoAssigment = new ArrayList<>();
                        findCranes(X, Y, vrijSlot.getX(), vrijSlot.getY(), kranen, kranenToDoAssigment);

                        double positiePickupX = X + (container.getLengte() / 2.0);
                        double positiePickupY = Y + 0.5;

                        double positieEindX = vrijSlot.getX() + (container.getLengte() / 2.0);
                        double positieEindY = vrijSlot.getY() + 0.5;

                        ArrayList<Slot> sloten = new ArrayList<>();
                        sloten.add(vrijSlot);
                        // TODO: moeten kranen nog uit de weg worden gezet?
                        doAssignment(positiePickupX, positiePickupY, kranenToDoAssigment, positieEindX, positieEindY, tijd, slots, sloten, kranen, trajecten, container, checkerBoard);
                        legeSlots.remove(vrijSlot);
                        verplaatsteContainers.add(container);
                    }
                } else {
                    //********* CONTAINER MET LENGTE != 1 *********
                    //1e situatie: er is plaats en container mag erop
                    //pakt 1 random slot, neem X en controleer of X+lengte <= lengte van de yard-1
                    // als het kan qua lengte, controleren of hoogtes van alle sloten gelijk zijn,
                    // controleren of hij erop mag qua target height en qua onderliggende containers, zo ja: plaatsen die handel
                    // zo nee & op einde van de sloten, door naar optie van er is geen plaats
                    //2e situatie: er is geen enkele plaats meer waar hij op mag
                    ArrayList<Slot> mogelijkeSloten = new ArrayList<>();
                    slots.sort(Comparator.comparing(Slot::getId));
                    boolean placed = false;

                    //******** HIER EERST OOK IF LEGESLOTS IS NIET LEEG TOEPASSEN ********
                    if(!legeSlots.isEmpty()){
                        int X = container.getStart().getX();
                        int Y = container.getStart().getY();

                        for (Slot slot : legeSlots) {
                            mogelijkeSloten.clear();
                            if (slot.getX() + container.getLengte() <= lengte - 1) {
                                for (int i = slot.getId(); i < slot.getId() + container.getLengte(); i++) {
                                    mogelijkeSloten.add(slots.get(i));
                                }
                            }
                        }
                        //TODO: MOET DIT BINNEN DE FORLOOP VAN SLOTS? ik denk van niet
                        double positiePickupX = X + (container.getLengte() / 2.0);
                        double positiePickupY = Y + 0.5;

                        double positieEindX = mogelijkeSloten.get(0).getX() + (container.getLengte() / 2.0);
                        double positieEindY = mogelijkeSloten.get(0).getY() + 0.5;

                        ArrayList<Kraan> kranenToDoAssigment = new ArrayList<>();
                        findCranes(X,Y, mogelijkeSloten.get(0).getX(), mogelijkeSloten.get(0).getY(), kranen, kranenToDoAssigment);
                        // TODO: moeten kranen nog uit de weg worden gezet?
                        doAssignment(positiePickupX, positiePickupY, kranenToDoAssigment, positieEindX, positieEindY, tijd, slots, mogelijkeSloten, kranen, trajecten, container, checkerBoard);
                        for(Slot s: mogelijkeSloten){
                            legeSlots.remove(s);
                        }
                        verplaatsteContainers.add(container);
                    } else{
                        //******** INDIEN ER GEEN LEGE SLOTEN MEER OP DE GROND ZIJN ********
                        for (Slot slot : slots) {
                            mogelijkeSloten.clear();
                            if (slot.getX() + container.getLengte() <= lengte - 1) {
                                //TODO:checken als hier wel het juiste slot wordt gepakt
                                for (int i = slot.getId(); i < slot.getId() + container.getLengte(); i++) {
                                    mogelijkeSloten.add(slots.get(i));
                                }
                                //controleren of de hoogte van elke slot wel dezelfde is & of hoogte + 1 <= target is & of hij erop mag
                                if (checkSameHeight(mogelijkeSloten) && checkHeight(mogelijkeSloten, finalTargetHeight) && checkContainersUnder(mogelijkeSloten.get(0), mogelijkeSloten.get(mogelijkeSloten.size()))) {
                                    int X = container.getStart().getX();
                                    int Y = container.getStart().getY();

                                    double positiePickupX = X + (container.getLengte() / 2.0);
                                    double positiePickupY = Y + 0.5;

                                    double positieEindX = slot.getX() + (container.getLengte() / 2.0);
                                    double positieEindY = slot.getY() + 0.5;

                                    ArrayList<Kraan> kranenToDoAssigment = new ArrayList<>();
                                    findCranes(X,Y, slot.getX(), slot.getY(), kranen, kranenToDoAssigment);
                                    // TODO: moeten kranen nog uit de weg worden gezet?
                                    doAssignment(positiePickupX, positiePickupY, kranenToDoAssigment, positieEindX, positieEindY, tijd, slots, mogelijkeSloten, kranen, trajecten, container, checkerBoard);
                                    verplaatsteContainers.add(container);
                                    placed = true;
                                    break;
                                }
                            }
                        }
                    }

                    //TODO: hier wanneer er geen slot gevonden is
                    if (!placed) {
                        System.out.println("DE CONTAINER HEEFT GEEN PLAATS, VERPLAATS ANDERE CONTAINERS");
                    }
                }
            }
            for (Container c : verplaatsteContainers) {
                teHogeContainers.remove(c);
            }
    }
}

    public static ArrayList<Container> vindTeHogeContainers(ArrayList<Slot> slots, int targetHeight) {
        ArrayList<Container> teHogeContainers = new ArrayList<>();
        for (Slot slot: slots) {
            if (slot.getHoogte() > targetHeight) {
                for(int i=targetHeight; i< slot.getHoogte();i++){
                    if(!teHogeContainers.contains(slot.getContainer(i))){
                        teHogeContainers.add(slot.getContainer(i));
                    }
                }
            }
        }
        return teHogeContainers;
    }

    public static ArrayList<Slot> findEmptySlots(ArrayList<Slot> slots, int lengte) {
        ArrayList<Slot> legeSlots = new ArrayList<>();
        for (Slot slot: slots) {
            if (slot.getHoogte()==0) {
                legeSlots.add(slot);
            }
        }
        ArrayList<Slot> kleineSlots= new ArrayList<>();
        for (Slot slot: legeSlots) {
            if (slot.getX() == 1 && slot.getX() != 0|| slot.getX() == lengte+1 && slots.get(slot.getId()-1).getHoogte() != 0) {
                kleineSlots.add(slot);
            }
            else if (slots.get(slot.getId()-1).getHoogte() == 0 && slots.get(slot.getId()+1).getHoogte() == 0) {
                kleineSlots.add(slot);
            }
        }
        for (Slot slot: legeSlots) {
            legeSlots.remove(slot);
        }
        ArrayList<Slot> emptySlots = new ArrayList<>();
        emptySlots.addAll(kleineSlots);
        emptySlots.addAll(legeSlots);

        return legeSlots;
    }

    private static void writeOutput(ArrayList<Traject> trajecten, String name) {
        try{
            FileWriter fileWriter = new FileWriter(name + "output.txt");
            fileWriter.write("CraneId;ContainerId;PickupTime;EndTime;CranePickupPosX;CranePickupPosY;CraneEndPosX;CraneEndPosY;" +"\n");
            for(Traject traject: trajecten) {
                if (traject.getContainer_id() == -1) {
                    fileWriter.write(traject.getKraan().getId() + ";"
                            + " " + ";" + traject.getStarttijd() + ";" +
                            traject.getEindtijd() + ";" + traject.getPickup_x() + ";" +
                            traject.getPickup_y() + ";" + traject.getDropoff_x() + ";" +
                            traject.getDropoff_y() + "\n");
                }
                else {
                    fileWriter.write(traject.getKraan().getId() + ";"
                            + traject.getContainer_id() + ";" + traject.getStarttijd() + ";" +
                            traject.getEindtijd() + ";" + traject.getPickup_x() + ";" +
                            traject.getPickup_y() + ";" + traject.getDropoff_x() + ";" +
                            traject.getDropoff_y() + "\n");
                }

            }
            fileWriter.close();
        }
        catch  (IOException ex) {
            System.out.println("Error occurred while trying to write the outputfile. Try again.");
            ex.printStackTrace();
        }
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

    private static void parseAssignment(JSONObject data, ArrayList<Assignment> assignement, ArrayList<Slot> slotLijst, ArrayList<Container> containers){
        Long slot_id = (Long) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        Container container = null;
        Assignment assignment = new Assignment(container_id);
        for (Container c: containers){
            if(c.getId()== container_id){
                container = c;
            }
        }

        for(int j=0;j<container.getLengte();j++){
            for(int i=0; i<slotLijst.size(); i++){
                if(slotLijst.get(i).getId() == slot_id + j){
                    assignment.addSlot(slotLijst.get(i));
                    break;
                }
            }
        }
        assignement.add(assignment);
   }

   private static void setStartPosition(ArrayList<Assignment> assignments, ArrayList<Container> containers, CheckerBoard checkerboard) {
       containers.sort(Comparator.comparing(Container::getLengte));

       ArrayList<Slot> slotlist;
       boolean plaatsen = true;
       ArrayList<Assignment> wachtlijst = assignments;
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

   public static void plaatsContainer(Container c, ArrayList<Slot> toegewezenSlots, CheckerBoard checkerboard) {
       c.setStart(toegewezenSlots.get(0));
       c.setEind(toegewezenSlots.get(toegewezenSlots.size()-1));
       for(Slot slot: toegewezenSlots) {
           slot.voegContainerToe(c);
           int hoogte_container = slot.getHoogte();
           checkerboard.veranderKleur(slot.getX(), slot.getY(),hoogte_container);
       }
   }

   public static void verplaatsContainer(Container container, ArrayList<Slot> alleSlots, ArrayList<Slot> toegewezenSlots, CheckerBoard checkerBoard) {
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

   public static void checkAndPlace(ArrayList<Assignment> toFinishAssignments, ArrayList<Kraan> kranen, ArrayList<Container> containers, int max_hoogte, ArrayList<Slot> slots, CheckerBoard checkerBoard, double tijd, ArrayList<Traject> trajecten ) {
       while (!toFinishAssignments.isEmpty()) {
           ArrayList<Assignment> executed_assignments = new ArrayList<>();
           for (Assignment assignment : toFinishAssignments) {
               Container container = null;
               for (Container c : containers) {
                   if (c.getId() == assignment.getContainer_id()) {
                       container = c;
                       break;
                   }
               }

               int positieContainerX = container.getStart().getX();
               double positiePickupX = positieContainerX + (container.getLengte() / 2.0);

               int positieContainerY = container.getStart().getY();
               double positiePickupY = positieContainerY + 0.5;

               System.out.println("container x  " + positieContainerX + " " + assignment.getSlots().get(0).getX());
               System.out.println("container y  " + positieContainerY + " " + assignment.getSlots().get(0).getY());

               if (positieContainerX == assignment.getSlots().get(0).getX() && positieContainerY == assignment.getSlots().get(0).getY()) {
                   executed_assignments.add(assignment);
                   System.out.println("****DEZELFDE PLEK*****");
                   continue;
               }

               if (checkSameHeight(assignment.getSlots()) && checkHeight(assignment.getSlots(), max_hoogte) && checkContainersUnder(assignment.getSlots().get(0), assignment.getSlots().get(assignment.getSlots().size() - 1))) {
                   ArrayList<Kraan> kranenToDoAssignment = new ArrayList<>();
                   double positieEindX = assignment.getSlots().get(0).getX() + (container.getLengte() / 2.0);
                   double positieEindY = assignment.getSlots().get(0).getY() + 0.5;
                   double positieBeginX = positiePickupX;
                   double positieBeginY = positiePickupY;
                   findCranes(positieBeginX, positieBeginY, positieEindX, positieEindY, kranen, kranenToDoAssignment);
                   doAssignment(positiePickupX, positiePickupY, kranenToDoAssignment, positieEindX, positieEindY, tijd, slots, assignment.getSlots(), kranen, trajecten, container, checkerBoard);
                   executed_assignments.add(assignment);
               }
           }
           for (Assignment a : executed_assignments) {
               toFinishAssignments.remove(a);
           }

           System.out.println("Klaar");
       }
   }

    private static double berekenVerplaatsing(Kraan kraan, double pickX, double pickY, double eindX, double eindY) {
        double verplaatsingTijd;
        double x_snelheid = Math.abs(eindX-pickX) * kraan.getX_snelheid();
        double y_snelheid = Math.abs(eindY-pickY) * kraan.getY_snelheid();
        verplaatsingTijd = Math.max(x_snelheid, y_snelheid);
        return verplaatsingTijd;
    }

    private static double berekenPickup(Kraan kraan, double positiePickupX, double positiePickupY, double tijd) {
        double pickupTijd;
        double x_snelheid = Math.abs(kraan.getX_coordinaat()-positiePickupX) * kraan.getX_snelheid();
        double y_snelheid = Math.abs(kraan.getY_coordinaat()-positiePickupY) * kraan.getY_snelheid();
        pickupTijd = tijd + Math.max(x_snelheid, y_snelheid);
        return pickupTijd;
    }

    private static boolean moveDown(double y_eind, double y_begin) {
        if (y_eind > y_begin) {
            return true;
        }
        return false;
    }

    private static boolean moveRight(double x_eind, double x_begin) {
        if (x_eind > x_begin) {
            return true;
        }
        return false;
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

    public static double moveCranes(ArrayList<Kraan> kranen, Kraan kraan, double beginX, double eindX, double tijd, ArrayList<Traject> trajecten) {
        for(Kraan andereKraan: kranen){
            if (!Objects.equals(andereKraan, kraan)) {
                if (moveLeft(eindX, beginX)) {
                    if (andereKraan.getX_coordinaat() >= beginX && andereKraan.getX_coordinaat() <= eindX) {
                        if ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2) + 1 < beginX || ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) - 1 > eindX) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_coordinaat(), (andereKraan.getY_maximum() - andereKraan.getY_minimum() / 2), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2));
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (beginX-1 >= andereKraan.getX_minimum() && beginX-1 <= andereKraan.getX_maximum()){
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (beginX-1)) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0,beginX-1, andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(beginX-1);
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (eindX + 1 >= andereKraan.getX_minimum() && eindX + 1 <= andereKraan.getX_maximum()) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (eindX+1)) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0,eindX + 1, andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(eindX + 1);
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (andereKraan.getX_minimum() < beginX-1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - andereKraan.getX_minimum()) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_minimum(), andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(andereKraan.getX_minimum());
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (andereKraan.getX_maximum() > eindX + 1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - andereKraan.getX_maximum()) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_maximum(), andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(andereKraan.getX_maximum());
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                    }
                }
                else if (moveRight(eindX, beginX)){
                    if (andereKraan.getX_coordinaat() <= beginX && andereKraan.getX_coordinaat() >= eindX) {
                        if ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2) + 1 > beginX || ((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) - 1 < eindX) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2)) * andereKraan.getY_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, (andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2), andereKraan.getY_coordinaat() , tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat((andereKraan.getX_maximum() - andereKraan.getX_minimum() / 2));
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (beginX-1 <= andereKraan.getX_minimum() && beginX-1 >= andereKraan.getX_maximum()){
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (beginX-1)) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, beginX-1, andereKraan.getY_coordinaat(),  tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(beginX-1);
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (eindX + 1 <= andereKraan.getX_minimum() && eindX + 1 >= andereKraan.getX_maximum()) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - (eindX+1)) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0,eindX + 1,andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(eindX + 1);
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (andereKraan.getX_minimum() > beginX-1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - andereKraan.getX_minimum()) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_minimum(), andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(andereKraan.getX_minimum());
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                        else if (andereKraan.getX_maximum() < eindX + 1) {
                            int eindTijd = (int) (tijd + Math.abs(andereKraan.getX_coordinaat() - andereKraan.getX_maximum()) * andereKraan.getX_snelheid());
                            trajecten.add(new Traject(andereKraan.getX_coordinaat(), andereKraan.getY_coordinaat(), tijd, 0, 0, andereKraan.getX_maximum(), andereKraan.getY_coordinaat(), tijd, eindTijd, andereKraan, -1));
                            andereKraan.setX_coordinaat(andereKraan.getX_maximum());
                            tijd = eindTijd;
                            System.out.println("empty move");
                        }
                    }

                }
            }
        }
    return tijd;
    }

    public static void findCranes(double positieBeginX, double positieBeginY, double positieEindX, double positieEindY, ArrayList<Kraan> kranen, ArrayList<Kraan> kranenToDoAssignment) {
        double eindeX = positieEindX;
        double eindeY = positieEindY;
        for (Kraan kraan: kranen) {
            if (positieBeginX <= kraan.getX_maximum() && positieBeginX >= kraan.getX_minimum() && positieBeginY <= kraan.getY_maximum() && positieBeginY >= kraan.getY_minimum()
                    && positieEindX <= kraan.getX_maximum() && positieEindX >= kraan.getX_minimum() && positieEindY <= kraan.getY_maximum() && positieEindY >= kraan.getY_minimum()) {
                kranenToDoAssignment.add(kraan);
                break;
            }
        }
        if (kranenToDoAssignment.isEmpty()) {
            while (positieBeginX != eindeX || positieBeginY != eindeY) {
                for (Kraan kraan : kranen) {
                    if (positieBeginX <= kraan.getX_maximum() && positieBeginX >= kraan.getX_minimum() && positieBeginY <= kraan.getY_maximum() && positieBeginY >= kraan.getY_minimum()) {
                        kranenToDoAssignment.add(kraan);
                        if (positieEindX <= kraan.getX_maximum() && positieEindX >= kraan.getX_minimum() && positieEindY <= kraan.getY_maximum() && positieEindY >= kraan.getY_minimum()) {
                            eindeX = positieBeginX;
                            eindeY = positieBeginY;
                            break;
                        } else {
                            if (moveLeft(eindeX, positieBeginX)) {
                                positieBeginX = kraan.getX_minimum();
                            } else if (moveRight(eindeX, positieBeginX)) {
                                positieBeginX = kraan.getX_maximum();
                            }
                            if (moveUp(eindeY, positieBeginY)) {
                                positieBeginY = kraan.getY_minimum();
                            } else if (moveDown(eindeY, positieBeginY)) {
                                positieBeginY = kraan.getY_maximum();
                            }
                        }

                    }
                }
            }
        }

    }

    public static void doAssignment(double positiePickupX, double positiePickupY, ArrayList<Kraan> kranenToDoAssignment, double positieEindX, double positieEindY, double tijd, ArrayList<Slot> slots, ArrayList<Slot> assignmentSlots, ArrayList<Kraan> kranen, ArrayList<Traject> trajecten, Container container, CheckerBoard checkerBoard) {
        double beginX = positiePickupX;
        double beginY = positiePickupY;
        ArrayList<Slot> toegewezenSlots;
        for (int i = 0; i < kranenToDoAssignment.size(); i++) {
            double eindX = positieEindX;
            double eindY = positieEindY;
            toegewezenSlots = new ArrayList<>(assignmentSlots);
            if (kranenToDoAssignment.get(i).getX_minimum() > eindX || kranenToDoAssignment.get(i).getX_maximum() < eindX) {
                toegewezenSlots.clear();
                if (moveLeft(eindX, beginX)) {
                    eindX = kranenToDoAssignment.get(i).getX_minimum();
                } else if (moveRight(eindX, beginX)) {
                    eindX = kranenToDoAssignment.get(i).getX_maximum();
                }
                if (moveUp(eindY, beginY)) {
                    eindY = kranenToDoAssignment.get(i).getY_minimum();
                } else if (moveDown(eindY, beginY)) {
                    eindY = kranenToDoAssignment.get(i).getY_maximum();
                }
                for (int j = 0; j < slots.size(); j++) {
                    if (Math.floor(eindX) == slots.get(j).getX() && slots.get(j).getY() == Math.floor(eindY)) {
                        int helft = (int) Math.floor(assignmentSlots.size() / 2.0);
                        for (int k = -helft; k < assignmentSlots.size() - helft; k++) {
                            toegewezenSlots.add(slots.get(j + k));
                        }
                    }
                }
            }
            tijd = moveCranes(kranen, kranenToDoAssignment.get(i), beginX, eindX, tijd, trajecten);
            System.out.println("container id  " + container.getId() + " " + assignmentSlots.get(0).getId());
            verplaatsContainer(container, slots, toegewezenSlots, checkerBoard);
            double startPickupTime = berekenPickup(kranenToDoAssignment.get(i), positiePickupX, positiePickupY, tijd);
            double endPickupTime = startPickupTime + berekenVerplaatsing(kranenToDoAssignment.get(i), positiePickupX, positiePickupY, eindX, eindY);
            Traject traject = new Traject(kranenToDoAssignment.get(i).getX_coordinaat(), kranenToDoAssignment.get(i).getY_coordinaat(),
                    tijd, positiePickupX, positiePickupY, eindX, eindY, startPickupTime, endPickupTime, kranenToDoAssignment.get(i), container.getId());
            kranenToDoAssignment.get(i).setX_coordinaat(eindX);
            kranenToDoAssignment.get(i).setY_coordinaat(eindY);
            tijd = endPickupTime;
            trajecten.add(traject);

            System.out.println("*********");
            beginY = eindY;
            beginX = eindX;
        }
    }

    public static boolean checkSameHeight(ArrayList<Slot> slots) {
        int hoogte = slots.get(0).getHoogte();
        for (Slot slot: slots) {
            if (slot.getHoogte() != hoogte) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkHeight(ArrayList<Slot> slots, int heigth) {
        for (Slot slot : slots) {
            if (slot.getHoogte() + 1 > heigth) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkContainersUnder(Slot slotBegin, Slot slotEind) {
        if (slotBegin.getHoogte() != 0) {
            Container containerBegin = slotBegin.getStack().get(slotBegin.getHoogte() - 1);
            Container containerEind = slotEind.getStack().get(slotEind.getHoogte() - 1);
            if (containerBegin.getStart() == slotBegin && containerEind.getEind() == slotEind) {
                return true;
            }
        } else {
            return true; //voor wanneer er geen container onderstaat
        }
        return false;
    }

}
