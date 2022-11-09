import java.io.*;
import java.util.ArrayList;

import org.json.simple.*;
import org.json.simple.parser.*;

import java.awt.*;
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


        CheckerBoardJavaExample CheckerBoard = new CheckerBoardJavaExample();
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
