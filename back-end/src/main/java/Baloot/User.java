package Baloot;

import Baloot.Exception.CommodityIsNotInBuyList;
import InterfaceServer.CommodityInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User {

    private String username;
    private String password;
    private String email;
    private String birthDate;
    private String address;
    private double credit;

    private BuyList buyList;
    private BuyList purchased;
    ArrayList<DiscountCode> usedDiscountCodes ;

    public User(String inputUserName, String inputPassword, String inputEmail,
         String inputBirthDate, String inputAddress, double inputCredit)
    {
        username = inputUserName;
        password = inputPassword;
        email = inputEmail;
        birthDate = inputBirthDate;
        address = inputAddress;
        credit = inputCredit;
        buyList = new BuyList();
        // usedDiscountCodes = new ArrayList<DiscountCode>();

    }

    public void buyCommodity(Commodity newCommodity)
    {
        CommodityInBuyList commodity = new CommodityInBuyList(newCommodity, 1);
        buyList.addNewCommodityToBuyList(commodity);

    }

    public boolean hasBoughtCommodity(int commodityId)
    {
        return buyList.contains(commodityId);

    }

    public void removeFromBuyList(int commodityId) throws CommodityIsNotInBuyList {
        if(!buyList.contains(commodityId))
        {
            throw new CommodityIsNotInBuyList(commodityId);
        }
        buyList.removeCommodityFromBuyList(commodityId);
    }

    public ArrayList<Commodity> getBoughtCommodities() {
        ArrayList<CommodityInBuyList> commodities = buyList.getAllCommodities();
        ArrayList<Commodity> commoditiesAns = new ArrayList<Commodity>();
        for(CommodityInBuyList commodityInBuyList: commodities)
        {
            commoditiesAns.add(commodityInBuyList.getCommodity());
        }
        return commoditiesAns;
    }

    public void setBoughtCommitiesEmpty() {

        buyList = new BuyList();
    }
    public void setPurchasedCommodityEmpty(){purchased = new BuyList();}

    public void setUSedDiscountCodesEmpty(){usedDiscountCodes = new ArrayList<DiscountCode>();}

    public String getName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getAddress() {
        return address;
    }

    public double getCredit() {
        return credit;
    }

    public void addCredit(double incCredit) {
        credit += incCredit;
    }
    public void decreaseCredit(double outCredit){credit -= outCredit;}

    public void addBuyListToPurchasedCommodities()
    {
        for(CommodityInBuyList commodity : buyList.getAllCommodities())
        {
            purchased.addNewCommodityToBuyList(commodity);
        }
    }
    public void clearBuylist()
    {
        if(buyList.hasDiscount())
        {
            usedDiscountCodes.add(buyList.getBuylistDiscountCode());
        }
        buyList.emptyList();
    }

    public BuyList getPurchasedCommodities() {
        return purchased;
    }

    public BuyList getBuyList()
    {
        return buyList;
    }

    public boolean passwordMatches(String pass)
    {
        if(password.equals(pass))
            return true;
        return false;
    }

    public boolean emailEquals(String emailAdd)
    {
        if(email.equals(emailAdd))
            return true;
        return false;
    }

    public boolean hasUsedDiscountCode(DiscountCode discountCode)
    {
        if(usedDiscountCodes.isEmpty())
            return false;
        if(usedDiscountCodes.contains(discountCode))
            return true;
        return false;
    }

    public void addDiscountToBuylist(DiscountCode discountCode)
    {
        buyList.setDiscountCode(discountCode);
    }

    public int getNumBought(Integer commodityId) {
        return buyList.getBuylistNum(commodityId);
    }
}
