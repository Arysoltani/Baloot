package Baloot;

import com.google.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Scanner;

public class Baloot {

    static final int TYPE_COMMAND_INDEX = 0;
    static final int JSON_INDEX = 1;
    static final String ADD_USER_COMMAND = "addUser";

    static final String ADD_PROVIDER_COMMAND = "addProvider";

    static final String ADD_COMMODITY = "addCommodity";

    static final String GET_COMMODITIES_LIST="getCommoditiesList";
    static final String RATE_COMMODITY="rateCommodity";
    static final String ADD_TO_BUYLIST="addToBuyList";

    static final String REMOVE_FROM_BUYLIST="removeFromBuyList";

    static final String GET_COMMODITY_BY_ID="getCommodityById";

    static final String GET_COMMODITIES_BY_Category="getCommoditiesByCategory";
    static final String GET_BUY_LIST="fetBuyList";

    //static final String SUCCESSFUL = "addUser";
    static final int MAX_PARSE_COMMAND = 2;


    private static BalootServer balootServer = new BalootServer();
    Baloot()
    {

    }

    private static void printOutput(boolean successful, String data) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", successful);
        jsonObject.put("data", data);
        System.out.println(jsonObject.toString());

    }

    private static void printOutput(boolean successful, JSONObject JSONdata) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", successful);
        jsonObject.put("data", JSONdata);
        System.out.println(jsonObject.toString());
    }


    private static void run()
    {
        Scanner inputReader = new Scanner(System.in);
        while(true)
        {
            String new_command = inputReader.nextLine();
            String[] command_parsed = new_command.split(" ", MAX_PARSE_COMMAND);
            String data="{}";
            if(command_parsed.length == MAX_PARSE_COMMAND)
                data = command_parsed[Baloot.JSON_INDEX];
            try{
             parseCommand(command_parsed[Baloot.TYPE_COMMAND_INDEX], data);
            }
            catch (Exception e){
                printOutput(false,e.getMessage());
            }
        }
    }

    private static void parseCommand(String commandInput, String jsonInput)
    {

        JSONParser jsonCommand = new JSONParser();
        Gson gsonParser = new GsonBuilder().create();
        try {

            JSONObject jsonParser = (JSONObject)jsonCommand.parse(jsonInput);

            switch(commandInput)
            {
                case Baloot.ADD_USER_COMMAND:
                    addUser(jsonParser, jsonInput, gsonParser);
                    break;

                case Baloot.ADD_PROVIDER_COMMAND:
                    addProvider(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.ADD_COMMODITY:
                    addCommodity(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.GET_COMMODITIES_LIST:
                    getCommoditiesList();
                    break;
                case Baloot.RATE_COMMODITY:
                    rateCommodity(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.ADD_TO_BUYLIST:
                    addToBuyList(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.REMOVE_FROM_BUYLIST:
                    removeFromBuyList(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.GET_COMMODITY_BY_ID:
                    getCommodityById(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.GET_COMMODITIES_BY_Category:
                    getCommoditiesByCategory(jsonParser, jsonInput, gsonParser);
                    break;
                case Baloot.GET_BUY_LIST:
                    getBuyList(jsonParser, jsonInput, gsonParser);
                    break;
            }
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void addUser(JSONObject jsonParser, String jsonInp, Gson gsonParser)
    {
        User user = gsonParser.fromJson(jsonInp, User.class);
        String name = (String)jsonParser.get("username");

        if(balootServer.doesExist(name)) {
            balootServer.updateUser(name, user);
            printOutput(true, "User "+name+" updated");
        }
        else{
            balootServer.addUser(name, user);
            printOutput(true, "User "+name+" added");
        }
    }

    private static void addProvider(JSONObject jsonParser, String jsonInp, Gson gsonParser) {
        Provider newProvider = gsonParser.fromJson(jsonInp, Provider.class);
        String name = (String)jsonParser.get("name");
        int id = ((Number) jsonParser.get("id")).intValue();
        balootServer.addProvider(id, newProvider);
        printOutput(true, "Provider " + name + " added");
    }

    private static void addCommodity(JSONObject jsonParser, String jsonInp, Gson gsonParser) {

        int providerId = ((Number)jsonParser.get("providerId")).intValue();
        if(!balootServer.checkExistProvider(providerId))
        {
            printOutput(false, "Not exist provider");
        }
        else {
            int id = ((Number) jsonParser.get("id")).intValue();
            Commodity offering = gsonParser.fromJson(jsonInp, Commodity.class);

            balootServer.addCommidity(id, offering);
            balootServer.setCommodityProviderName(id,providerId);
            printOutput(true, "Commodity " + id + "added");
        }
    }

    private static void getCommoditiesList() {

        ArrayList<Commodity> commoditiesList = balootServer.getCommodityList();
        ArrayList<JSONObject> commoditiesInfo = new ArrayList<JSONObject>();

        for (Commodity commodity : commoditiesList) {
            JSONObject commodityInfo = commodity.getJsonData();
            commodityInfo.remove("provider");
            commoditiesInfo.add(commodityInfo);
        }

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("commoditiesList", commoditiesInfo);

        printOutput(true, jsonObject);

    }

    private static void rateCommodity(JSONObject jsonParser, String jsonInp, Gson gsonParser) {


        String username = (String)jsonParser.get("username");
        int commodityId = ((Number) jsonParser.get("commodityId")).intValue();
        int score = ((Number) jsonParser.get("score")).intValue();


        balootServer.rateCommodity(username,commodityId,score);

       // todo
    }

    private static void addToBuyList(JSONObject jsonParser, String jsonInp, Gson gsonParser) {


        String username = (String)jsonParser.get("username");
        int commodityId = ((Number) jsonParser.get("commodityId")).intValue();
        if(balootServer.commodityExistsInUserBuyList(username,commodityId))
        {
            printOutput(false, "Commodity is already added to buyList");
        }
        else if(balootServer.commodityIsAvailable(commodityId))
        {
            balootServer.addCommidityToUserBuyList(username,commodityId);
            printOutput(true, "Commodity added to buyList");
        }
        else{
            printOutput(false, "Commodity is not available");
        }

        // todo
    }

    private static void removeFromBuyList(JSONObject jsonParser, String jsonInp, Gson gsonParser) {


        String username = (String)jsonParser.get("username");
        int commodityId = ((Number) jsonParser.get("commodityId")).intValue();
        if(!balootServer.commodityExistsInUserBuyList(username,commodityId))
        {
            printOutput(false, "Commodity does not exist in buyList");
        }
        else{
            balootServer.removeFromBuyList(username,commodityId);
            printOutput(true, "Commodity removed from buyList");

        }

        // todo
    }

    private static void getCommodityById(JSONObject jsonParser, String jsonInp, Gson gsonParser) {

        int commodityId = ((Number) jsonParser.get("id")).intValue();
        Commodity commodity = balootServer.getCommodityById(commodityId);

        JSONObject commodityInfo = commodity.getJsonData();
        commodityInfo.remove("providerID");
        printOutput(true, commodityInfo);
    }

    private static void getCommoditiesByCategory(JSONObject jsonParser, String jsonInp, Gson gsonParser) {
        String category = (String)jsonParser.get("category");

        ArrayList<Commodity> commoditiesList  = balootServer.getCommoditiesByCategory(category);

        ArrayList<JSONObject> commoditiesInfo = new ArrayList<JSONObject>();

        for (Commodity commodity : commoditiesList) {
            JSONObject commodityInfo = commodity.getJsonData();
            commodityInfo.remove("provider");
            commoditiesInfo.add(commodityInfo);
        }

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("commoditiesListByCategory", commoditiesInfo);

        printOutput(true, jsonObject);

            //jsonObject.put("weeklySchedule", jsonArray);
    }

    private static void getBuyList(JSONObject jsonParser, String jsonInp, Gson gsonParser) {
        String username = (String)jsonParser.get("username");
        ArrayList<Commodity> commoditiesList  = balootServer.getUserBuyList(username);
        ArrayList<JSONObject> commoditiesInfo = new ArrayList<JSONObject>();

        for (Commodity commodity : commoditiesList) {
            JSONObject commodityInfo = commodity.getJsonData();
            commodityInfo.remove("provider");
            commoditiesInfo.add(commodityInfo);
        }

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("buyList", commoditiesInfo);

        printOutput(true, jsonObject);


    }

    public static void main(String[] args) {
        run();
    }

}
