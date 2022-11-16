import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

import org.json.simple.*;
import org.json.simple.parser.*;
import javax.swing.*;

public class Main {
    public static void main(String args[]) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("data/terminal_4_3.json")){
            Object obj = jsonParser.parse(reader);
            JSONObject jsonObject = (JSONObject)obj;

            String name = (String)jsonObject.get("name");

            ArrayList<Slot> slots = new ArrayList<>();
            JSONArray slotList = (JSONArray) jsonObject.get("slots");
            slotList.forEach( data -> parseSlots( (JSONObject) data, slots ) );

            ArrayList<Container> containers = new ArrayList<>();
            JSONArray containerList = (JSONArray) jsonObject.get("containers");
            containerList.forEach( data -> parseContainer( (JSONObject) data, containers ) );

            ArrayList<Assignment> assignments = new ArrayList<>();
            JSONArray assignmentList = (JSONArray) jsonObject.get("assignments");
            assignmentList.forEach( data -> parseAssignment( (JSONObject) data, assignments ,slots) );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        CheckerBoardJavaExample CheckerBoard = new CheckerBoardJavaExample(ROWS,COLS);
        final int SIZE = 600;
        CheckerBoard.setSize(SIZE, SIZE);
        JPanel leeg = new JPanel();
        leeg.setSize(300, 600);
        CheckerBoard.setVisible(true);
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, CheckerBoard, leeg);
        pane.setSize(600,600);
        pane.setVisible(true);

        JFrame frame = new JFrame();
        frame.add(pane);
        frame.setSize(600, 600);
        frame.setTitle("Kranenopdracht");
        frame.setVisible(true);

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

        containers.sort(Comparator.comparing(Container::getLengte));

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
                        if (!plaatsen) {
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
        }

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

    private static void parseAssignment(JSONObject data, ArrayList<Assignment> assignement, ArrayList<Slot> slotLijst){
        JSONArray slotList = (JSONArray) data.get("slot_id");
        int container_id = ((Long) data.get("container_id")).intValue();

        for(int j=0;j<slotList.size();j++){
            int id = ((Long) slotList.get(j)).intValue();
            Assignment a = new Assignment(container_id);
            for(int i=0; i<slotLijst.size(); i++){
                if(slotLijst.get(i).getId()==id){
                    a.addSlot(slotLijst.get(i));
                }
            }
            assignement.add(a);
        }
   }
}
