package Baloot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class User {

    private String userName;
    private String password;
    private String email;
    private String birthDate;
    private String address;
    private double credit;

    Map<Integer, Commodity> boughtCommodities;
    public User(String inputUserName, String inputPassword, String inputEmail,
         String inputBirthDate, String inputAddress, double inputCredit)
    {
        userName = inputUserName;
        password = inputPassword;
        email = inputEmail;
        birthDate = inputBirthDate;
        address = inputAddress;
        credit = inputCredit;
    }

    public void buyCommodity(int commodityId, Commodity newCommodity)
    {
        boughtCommodities.put(commodityId, newCommodity);
    }

    public boolean hasBoughtCommodity(int commodityId)
    {
        return boughtCommodities.containsKey(commodityId);
    }

    public void removeFromBuyList(int commodityId) {
        if(!boughtCommodities.containsKey(commodityId))
        {
            // throw
        }
        boughtCommodities.remove(commodityId);
    }

    public ArrayList<Commodity> getCommodities() {
        Collection<Commodity> commoditiesBought = boughtCommodities.values();
        return new ArrayList<Commodity>(commoditiesBought);
    }
}
