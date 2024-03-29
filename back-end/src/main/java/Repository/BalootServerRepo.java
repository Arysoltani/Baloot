package Repository;



import Baloot.*;
import Baloot.DTOObjects.*;
import Baloot.Exception.*;
import com.google.common.hash.Hashing;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import Baloot.Managers.*;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BalootServerRepo {

    private final EntityManagerFactory entityManagerFactory;

    final private HashMap<String, User> sessions = new HashMap<>();

    private UserManager userManager;
    //    private ProviderManager providerManager;
    private CommodityManager commodityManager;

    private PaymentManager paymentManager;
    //    private PaymentManager paymentManager;
    static int commentIdNow;

    private static BalootServerRepo instance = null;
//


    public static BalootServerRepo getInstance() {
        if(instance == null) {
            EntityManagerFactory entityManagerFactory;
            var registry = new StandardServiceRegistryBuilder().configure().build();
            entityManagerFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
            instance = new BalootServerRepo(entityManagerFactory );
        }

        return instance;

    }

    public BalootServerRepo(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        userManager = new UserManager();
//        providerManager = new ProviderManager();
        commodityManager = new CommodityManager();

        paymentManager = new PaymentManager();
    }

    public User logIn(String email, String password) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        List users = entityManager.createQuery("SELECT u FROM User u where u.email=:userEmail")
                .setParameter("userEmail", email).getResultList();
        if (users.size() == 0) {
            entityManager.getTransaction().rollback();
            entityManager.getTransaction().commit();
            entityManager.close();
            throw new UserNotExist(email);
        } else {
            User user = (User) users.get(0);
            password = Hashing.sha256().hashString(password, StandardCharsets.UTF_8).toString();
            if (password.equals(user.getPassword())) {
                sessions.put(user.getName(), user);
                entityManager.getTransaction().commit();
                entityManager.close();
                return user;
            } else {
                entityManager.getTransaction().rollback();
                entityManager.getTransaction().commit();
                entityManager.close();
                throw new IncorrectPassword();
            }
        }
    }

    private User getUserByEmail(String userEmail,EntityManager entityManager) throws Exception{
        List users = entityManager.createQuery("SELECT u FROM User u where u.email=:userEmail")
                .setParameter("userEmail", userEmail).getResultList();
        if (users.isEmpty()) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new Exception();
        }
        return (User) users.get(0);
    }

    public boolean isUserExist(String userName)
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        User user = findUserById(userName, entityManager);
        if(user == null)
        {
            return false;
        }
        return true;
    }

    public boolean userIsLoggedIn(String username, String password) throws Exception {
        if (sessions.containsKey(username))
            return true;
        else
            return false;

    }

    public void logOut(String username, String password) throws Exception {
        if (sessions.containsKey(username))
            sessions.remove(username);
        else
            throw new UserNotExist(username);

    }

    public UserDTO getUserById(String username) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        User user = findUserById(username, entityManager);
        entityManager.close();

        return new UserDTO(user);
        //  return userManager.getUserById(username, entityManagerFactory);
    }

    public Provider getProviderById(int providerId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        Provider provider = entityManager.find(Provider.class, providerId);
        entityManager.close();
        return provider;
        //  return userManager.getUserById(username, entityManagerFactory);
    }


    public void addUser(User newUser) throws Exception {
        newUser.passwordGetHash();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
//        System.out.println(newUser.getEmail());
        if(isEmailExist(newUser.getEmail(), entityManager))
        {
            entityManager.getTransaction().commit();
            entityManager.close();
            throw new EmailAlreadyExist(newUser.getEmail());
        }
        try {
            entityManager.persist(newUser);
            entityManager.getTransaction().commit();
            entityManager.close();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new UserAlreadyExistError(newUser.getName());
        }
    }

    public BuyListDTO getUserBuyList(String userName) throws Exception { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        BuyListDTO buyListDTO = userManager.getUserBuyList(userName, entityManager);
        entityManager.close();
        return buyListDTO;

    }

    public BuyListDTO getUserPurchesedBuyList(String userName) throws Exception { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        BuyListDTO buyListDTO = userManager.getUserPurchesedBuyList(userName, entityManager);
        entityManager.close();
        return buyListDTO;
    }

    public void addCredit(String userName, String credit) throws Exception {
        if (!credit.matches("-?(0|[1-9]\\d*)")) {

            throw new InvalidCreditValue();
        }
        double creditVal = Double.parseDouble(credit);
        if (creditVal < 1)
            throw new InvalidCreditValue();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        User user = findUserById(userName, entityManager);
        if (user == null) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new UserNotExist(userName);
        } else {
            user.addCredit(creditVal);
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private User findUserById(String userName, EntityManager entityManager) {
        String query = "FROM User u WHERE u.username = :userName";
        List userNeeded = entityManager.createQuery(query)
                .setParameter("userName", userName).getResultList();
        ;
        if (userNeeded.size() == 0) {
            return null;
        }
        return (User) userNeeded.get(0);
    }

    public void updateCommodityCountInUserBuyList(String username, int commodityId, int count) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        System.out.println("before in updateCommodityCountInUserBuyList");

        var countList = entityManager.createQuery("select c from CommodityInBuyList c  " +
                        "                                             where c.user.username=:userId " +
                        "                                                  and c.isBought=false " +
                        "                                                   and c.commodity.id=:commodityId")
                .setParameter("userId", username)
                .setParameter("commodityId", commodityId)
                .getResultList();
        System.out.println("after in updateCommodityCountInUserBuyList");
        User user = entityManager.find(User.class , username);
        Commodity commodity = entityManager.find(Commodity.class , commodityId);

        if (countList.isEmpty()) {

            BuyList buyList = new BuyList();
            entityManager.persist(buyList);

            CommodityInBuyList commodityInBuyList = new CommodityInBuyList(user,commodity, buyList, 1);
            entityManager.persist(commodityInBuyList);

        } else {
//
            CommodityInBuyList commodityInBuyList =(CommodityInBuyList) countList.get(0);
            if(count < 0)
            {
                if (commodityInBuyList.getNumInStock() >= count) {
                    commodityInBuyList.updateNumInStock(count);
                }
            }
            else
                commodityInBuyList.updateNumInStock(count);

        }

        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public boolean commodityExistsInBuylist(BuyList buyList, int commodityId) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        var userBuylistID = entityManager.createNativeQuery("select c.id" +
                        "                                       from BUY_LIST_COMMODITIES b join CommodityInBuyList c on b.commodityInBuyListId = c.commodityInBuyListId " +
                        "                                       where b.buyListId =: buylistId and c.id =: commodityId")
                .setParameter("commodityId", commodityId).setParameter("buylistId", buyList.getId())
                .getResultList();
        if (userBuylistID.isEmpty()) {
            entityManager.close();
            return false;
        } else{
            entityManager.close();

            return true;}
    }


    public List getCommodityList(EntityManager entityManager) {
        List commoditiesList = commodityManager.getAllCommodities(entityManager);
        return commoditiesList;
    }

    public ArrayList<Commodity> getCommoditiesByCategory(String category) { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        ArrayList<Commodity> commodities = commodityManager.getCommoditiesByCategory(category, entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
        return commodities;
    }

    public CommodityDTO findCommodityById(int commodityId, EntityManager entityManager) {

        List commoditiesList = entityManager.createQuery("select c from Commodity c where c.id=:commodityId")
                .setParameter("commodityId", commodityId).getResultList();


        var stream = commoditiesList.stream().map(
                commodity -> new CommodityDTO((Commodity) commodity)
        );
        //   System.out.println("\n\n\n\nin get a;; commoditis\n\n\n\n"+stream.toList());
        return (CommodityDTO) stream.toList().get(0);
    }

    public ArrayList<Commodity> getCommodityRangePrice(double startPrice, double endPrice) { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        ArrayList<Commodity> commodities = commodityManager.getCommodityByRangePrice(startPrice, endPrice, entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
        return commodities;
    }

    public void addComment(Comment comment) throws Exception { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        User user = userManager.getUserByUseremail(comment.getUserEmail(), entityManager);
        commodityManager.addCommentToCommodity(comment, comment.getId(), user.getName(), entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public void rateCommodity(String username, int commodityId, String scoreStr) throws Exception {

        if (!scoreStr.matches("-?(0|[1-9]\\d*)"))
            throw new InvalidRating();
        int score = Integer.parseInt(scoreStr);
        if (score < 1 || score > 10)
            throw new InvalidRating();

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        if (!userManager.doesExist(username, entityManager)) {
            entityManager.close();
            throw new UserNotExist(username);
        }
        commodityManager.rateCommodity(username, commodityId, score, entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public List<CommodityDTO> getSuggestedCommodities(int commodityID,String username) throws Exception {
        System.out.println("in Csuggesstions");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        List<CommodityDTO> suggestions = commodityManager.getMostSimilarCommodities(commodityID, username, entityManager);
        entityManager.close();
        return suggestions;


    }

    public boolean isEmailExist(String email, EntityManager entityManager) throws Exception
    {
        List users = entityManager.createQuery("SELECT u FROM User u where u.email=:userEmail")
                .setParameter("userEmail", email).getResultList();
        if (users.isEmpty()) {
            return false;
        }
        return true;
    }

    //todo
    public void handlePayment(String username, String code) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        System.out.println("in apply discount code");
        DiscountCode discountCode;
        if(code!="")
            discountCode = paymentManager.getDiscountCode(code, entityManager);
        else  {
            discountCode = null;
        }
        User user = entityManager.find(User.class, username);
        System.out.println("after user");


        commodityManager.checkIfAllCommoditiesAreAvailabel(username,entityManager);
        System.out.println("after check");
        double totalPrice = commodityManager.getBuylistPrice(username, entityManager);
        userManager.buyBuyList(user,discountCode, totalPrice,entityManager);
        commodityManager.handleBuy(username,entityManager);
        entityManager.getTransaction().commit();
        entityManager.close();

    }

    public DiscountCodeDTO validateDiscountCode(String username, String code) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        System.out.println("befire get discount");

        DiscountCode discountCode = paymentManager.getDiscountCode(code, entityManager);

        System.out.println("after get discount");
        User user = entityManager.find(User.class, username);


        if (userManager.userHasNotUsedCode(user, discountCode, entityManager)) {
            entityManager.close();
            return new DiscountCodeDTO(discountCode);
        }
        else
            entityManager.close();
            throw new InvalidDiscountCode(code);
    }

    public List getAllCommodities() {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List commoditiesList = commodityManager.getAllCommodities(entityManager);
        entityManager.close();
        return commoditiesList;
    }

    public CommodityDTO getCommodityById(Integer id)throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        CommodityDTO commodity = findCommodityById(id,entityManager);
        List<CommentDTO> comments = getCommodityComments(id);
        commodity.setComments(comments);
        entityManager.close();
        return commodity;
    }

    public List getCommodityComments(int commodityId)
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<CommentDTO> comments = commodityManager.getCommodityComments(commodityId, entityManager);
        entityManager.close();
        return comments;
    }


    public int getUserNumBought(String username, Integer commodityId) throws Exception {


        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        int numBought = userManager.countOfCommodityInBuylist( username, commodityId, entityManager);
        entityManager.close();
        return numBought;
    }

    public CommentDTO addRatingToComment(int commentId, String userName, int rate) throws Exception { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        User user = entityManager.find(User.class, userName);

        Comment comment = entityManager.find(Comment.class, commentId);

        Commodity commodity = entityManager.find(Commodity.class, comment.getCommodityId());

        var resultList = entityManager.createQuery("select v from Vote v where v.user.username=:userId and" +
                        " v.comment.commentId=:commentId")
                .setParameter("userId",  userName)
                .setParameter("commentId", commentId)
                .getResultList();
        Vote vote;
        if (resultList.isEmpty()) {
            vote = new Vote(comment, user, rate);
            entityManager.persist(vote);
        } else {
            vote = (Vote) resultList.get(0);
            vote.setVote(rate);
        }
        entityManager.getTransaction().commit();
        entityManager.close();

        return new CommentDTO(comment);
    }

    public CommentDTO addComment(String username, int commodityID, String commentText, String date) throws Exception { //done
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();

        User user = entityManager.find(User.class, username);
        Commodity commodity = entityManager.find(Commodity.class, commodityID);
        Comment com = new Comment(user,  commodity,commentText, date);

        try {

            entityManager.persist(com);
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            entityManager.close();
            throw new Exception(e.getMessage());
        }
        entityManager.getTransaction().commit();
        entityManager.close();

        return new CommentDTO(com);

    }


    public User getUserByEmail(String email) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List users = entityManager.createQuery("SELECT u FROM User u where u.email=:userEmail")
                .setParameter("userEmail", email).getResultList();
        if (users.isEmpty()) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            entityManager.close();
            throw new Exception();
        }
        entityManager.getTransaction().commit();
        entityManager.close();
        return (User) users.get(0);
    }
}

