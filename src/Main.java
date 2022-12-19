import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
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

        int tijd = 0;

        try (FileReader reader = new FileReader("data/5t/TerminalB_20_10_3_2_160.json")){
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
            assignmentList.forEach(data -> parseAssignment( (JSONObject) data, assignments ,slots, containers) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        String finishName;
        int finishMaxHeight = 0;
        Set<Assignment> toFinishAssignments = new HashSet<>();
        if (targetHeight == 0) {
            try (FileReader reader = new FileReader("data/5t/targetTerminalB_20_10_3_2_160.json")) {
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

        CheckerBoard CheckerBoard = new CheckerBoard(length,width);
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

    private static void schrijfOutput(ArrayList<Traject> trajecten, String name) {
        try{
            FileWriter fileWriter = new FileWriter(name + "output.txt");
            fileWriter.write("CraneId;ContainerId;PickupTime;EndTime;PickupPosX;PickupPosY;EndPosX;EndPosY;" +"\n");
            for(Traject traject: trajecten) {
                if (traject.getContainer_id() == -1) {
                    fileWriter.write(traject.getKraan().getId() + ";"
                            + " " + ";" + traject.getStarttijd() + ";" +
                            traject.getEindtijd() + ";" + traject.getPickup_x() + ";" +
                            traject.getPickup_y() + ";" + traject.getDropoff_x() +
                            traject.getDropoff_y() + "\n");
                }
                else {
                    fileWriter.write(traject.getKraan().getId() + ";"
                            + traject.getContainer_id() + ";" + traject.getStarttijd() + ";" +
                            traject.getEindtijd() + ";" + traject.getPickup_x() + ";" +
                            traject.getPickup_y() + ";" + traject.getDropoff_x() +
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

    private static void parseAssignment(JSONObject data, Set<Assignment> assignement, ArrayList<Slot> slotLijst, ArrayList<Container> containers){
        Long slot_id = (Long) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        Assignment assignment = new Assignment(container_id);
        Container container = containers.get(container_id-1);

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

   private static void setStartPositie(Set<Assignment> assignments, ArrayList<Container> containers, CheckerBoard checkerboard) {
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

   public static void doAssignments(Set<Assignment> toFinishAssignments, ArrayList<Kraan> kranen, ArrayList<Container> containers, int max_hoogte, ArrayList<Slot> slots, CheckerBoard checkerBoard, int tijd, ArrayList<Traject> trajecten ) {
        while (!toFinishAssignments.isEmpty()) {
            ArrayList<Assignment> executed_assignments = new ArrayList<>();
            for (Assignment assignment : toFinishAssignments) {
                int positieContainerX = containers.get(assignment.getContainer_id()).getStart().getX();
                double positiePickupX = positieContainerX + (containers.get(assignment.getContainer_id()).getLengte() / 2);

                int positieContainerY = container.getStart().getY();
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
                    else{
                        containerEronderOke = true; //voor wanneer er geen container onderstaat
                    }
                }

                ArrayList<Kraan> kranenToDoAssignment = new ArrayList<>();
                double positieEindX = assignment.getSlots().get(0).getX() + (container.getLengte() / 2.0);
                double positieEindY = assignment.getSlots().get(0).getY() + 0.5;
                double eindeX = positieEindX;
                double eindeY = positieEindY;
                if (containerEronderOke) {
                    double positieBeginX = positiePickupX;
                    double positieBeginY = positiePickupY;

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
                                    } else if(moveRight(eindeX, positieBeginX)){
                                        positieBeginX = kraan.getX_maximum();
                                    }
                                    if (moveUp(eindeY, positieBeginY)) {
                                        positieBeginY = kraan.getY_minimum();
                                    } else if(moveDown(eindeY, positieBeginY)) {
                                        positieBeginY = kraan.getY_maximum();
                                    }
                                }

                            }
                        }
                    }
                }
                double beginX = positiePickupX;
                double beginY = positiePickupY;
                ArrayList<Slot> toegewezenSlots;
                for (int i = 0; i < kranenToDoAssignment.size(); i++) {
                    double eindX = positieEindX;
                    double eindY = positieEindY;
                    toegewezenSlots = new ArrayList<>(assignment.getSlots());
                    if (kranenToDoAssignment.get(i).getX_minimum() > eindX || kranenToDoAssignment.get(i).getX_maximum() < eindX) {
                        toegewezenSlots.clear();
                        if (moveLeft(eindX, beginX)) {
                            eindX = kranenToDoAssignment.get(i).getX_minimum();
                        }
                        else if(moveRight(eindX, beginX)){
                            eindX = kranenToDoAssignment.get(i).getX_maximum();
                        }
                        if(moveUp(eindY, beginY)){
                            eindY = kranenToDoAssignment.get(i).getY_minimum();
                        }
                        else if(moveDown(eindY, beginY)){
                            eindY = kranenToDoAssignment.get(i).getY_maximum();
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
   }

    private static int berekenVerplaatsing(Kraan kraan, double pickX, double pickY, double eindX, double eindY, int tijd) {
        int pickupTijd;
        int x_snelheid = (int)(Math.abs(eindX-pickX) * kraan.getX_snelheid());
        int y_snelheid =  (int)(Math.abs(eindY-pickY) * kraan.getY_snelheid());
        pickupTijd = tijd + Math.max(x_snelheid, y_snelheid);
        return pickupTijd;
    }

    private static int berekenPickup(Kraan kraan, double positiePickupX, double positiePickupY, int tijd) {
        int pickupTijd;
        int x_snelheid = (int)(Math.abs(kraan.getX_coordinaat()-positiePickupX) * kraan.getX_snelheid());
        int y_snelheid =  (int)(Math.abs(kraan.getY_coordinaat()-positiePickupY) * kraan.getY_snelheid());
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

    public static double zetKranenOpzij(ArrayList<Kraan> kranen, Kraan kraan, double beginX, double eindX, double tijd, ArrayList<Traject> trajecten) {
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
}
