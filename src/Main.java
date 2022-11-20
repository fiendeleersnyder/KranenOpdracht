import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.*;
import org.json.simple.parser.*;
import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        JSONParser jsonParser = new JSONParser();
        ArrayList<Slot> slots = new ArrayList<>();
        ArrayList<Container> containers = new ArrayList<>();
        Set<Assignment> assignments = new HashSet<>();

        int max_x = 0;
        int max_y = 0;

        try (FileReader reader = new FileReader("data/terminal_4_3.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            String name = (String)jsonObject.get("name");

            JSONArray slotList = (JSONArray) jsonObject.get("slots");
            slotList.forEach( data -> parseSlots( (JSONObject) data, slots) );

            JSONArray containerList = (JSONArray) jsonObject.get("containers");
            containerList.forEach( data -> parseContainer( (JSONObject) data, containers ) );

            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            assignmentList.forEach( data -> parseAssignment( (JSONObject) data, assignments ,slots) );

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        for (Slot slot: slots) {
            if (slot.getX() > max_x) {
                max_x = slot.getX();
            }
            if (slot.getY() > max_y) {
                max_y = slot.getY();
            }
        }

        final int ROWS = max_y+1; //y
        final int COLS = max_x+1; //x

        CheckerBoardJavaExample CheckerBoard = new CheckerBoardJavaExample(ROWS,COLS);
        final int SIZE = 600;
        CheckerBoard.setSize(SIZE, SIZE);
        JPanel legende = new JPanel();
        legende.setSize(300, 600);
        CheckerBoard.setVisible(true);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, CheckerBoard, legende);
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

        ArrayList<Slot> toegewezenSlots = new ArrayList<>();

        for (Slot slot: slots) {
            if (slot.getId() == 2 || slot.getId() == 3) {
                toegewezenSlots.add(slot);
            }
        }

        verplaatsContainer(containers.get(3), slots, toegewezenSlots, CheckerBoard);

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

    private static void parseContainer(JSONObject data,ArrayList<Container> containers){
        int id = ((Long) data.get("id")).intValue();
        int lengte = ((Long) data.get("length")).intValue();
        containers.add(new Container(id, lengte));
    }

    private static void parseAssignment(JSONObject data, Set<Assignment> assignement, ArrayList<Slot> slotLijst){
        JSONArray slotList = (JSONArray) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        Assignment assignment = new Assignment(container_id);

        for(int j=0;j<slotList.size();j++){
            int id = ((Long) slotList.get(j)).intValue();
            for(int i=0; i<slotLijst.size(); i++){
                if(slotLijst.get(i).getId()==id){
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
}
