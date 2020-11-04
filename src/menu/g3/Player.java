package menu.g3;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
// import sun.util.locale.provider.AvailableLanguageTags;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Player extends menu.sim.Player {
    Integer[] breakfastIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    Integer[] lunchIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
    Integer[] dinnerIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

    private HashMap<MemberName, List<Double>> breakfastArray = new HashMap<>(); // never gets updated 
    private HashMap<MemberName, List<Double>> lunchArray = new HashMap<>(); // updated with each day according to frequency 
    private HashMap<MemberName, List<Double>> dinnerArray = new HashMap<>(); // updated with each day according to frequency 

    private HashMap<MemberName, List<Integer>> frequencyArray = new HashMap<>(); // keeps track of how many times this meal was eaten in the past X days 
    /* these take the form of:
        B1, L1, D1 ... are satisfactions for each meal
        fm 1: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 2: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 3: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        . . .
    */

    private HashMap<MemberName, Double> familySatisfaction = new HashMap<>(); // array that keeps track of the family member's satisfaction for each day
    /* takes the form:
        fm1 --> satisfaction 
        fm2 --> satisfaction 
    */


    /**
    * Player constructor
    *
    * @param weeks             number of weeks
    * @param numFamilyMembers  number of family members
    * @param capacity          pantry capacity
    * @param seed              random seed
    * @param simPrinter        simulation printer
    *
    */
    public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
        super(weeks, numFamilyMembers, capacity, seed, simPrinter);
    }

    /**
    * Create shopping list of meals to stock pantry
    *
    * @param week           current week
    * @param numEmptySlots  number of empty slots left in the pantry
    * @param familyMembers  all family members
    * @param pantry         pantry inventory of remaining foods
    * @param mealHistory    history of previous meal allocations
    * @return               shopping list of foods to order
    *
    */
    public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {
        
        // TODO: create the breakfast, lunch, and dinner arrays (Nuneke)
        
        // family satisfaction, all 0 to start off 
        // frequency array, all 0 to start off 

        FoodType[] breakfastList = new FoodType[]{ FoodType.BREAKFAST1, FoodType.BREAKFAST2, FoodType.BREAKFAST3, FoodType.BREAKFAST4, FoodType.BREAKFAST5, FoodType.BREAKFAST6, FoodType.BREAKFAST7, FoodType.BREAKFAST8, FoodType.BREAKFAST9, FoodType.BREAKFAST10 };
        FoodType[] lunchList = new FoodType[]{ FoodType.LUNCH1, FoodType.LUNCH2, FoodType.LUNCH3, FoodType.LUNCH4, FoodType.LUNCH5, FoodType.LUNCH6, FoodType.LUNCH7, FoodType.LUNCH8, FoodType.LUNCH9, FoodType.LUNCH10 };
        FoodType[] dinnerList = new FoodType[]{ FoodType.DINNER1, FoodType.DINNER2, FoodType.DINNER3, FoodType.DINNER4, FoodType.DINNER5, FoodType.DINNER6, FoodType.DINNER7, FoodType.DINNER8, FoodType.DINNER9, FoodType.DINNER10, FoodType.DINNER11, FoodType.DINNER12, FoodType.DINNER13, FoodType.DINNER14, FoodType.DINNER15, FoodType.DINNER16, FoodType.DINNER17, FoodType.DINNER18, FoodType.DINNER19, FoodType.DINNER20 };

        if (breakfastArray == null){
            breakfastArray = new HashMap<>();
            lunchArray = new HashMap<>();
            dinnerArray = new HashMap<>();
            frequencyArray = new HashMap<>();
            familySatisfaction = new HashMap<>();

            for (FamilyMember fm : familyMembers) {
                MemberName fName = fm.getName();
                Map<FoodType, Double> foodMap = fm.getFoodPreferenceMap();

                List<Double> mapBList = new ArrayList<>();
                List<Double> mapLList = new ArrayList<>();
                List<Double> mapDList = new ArrayList<>();
                List<Integer> repList = new ArrayList<>(List.of(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); 

                for (FoodType ft : breakfastList) {
                    mapBList.add((double) foodMap.get(ft));
                }
                for (FoodType ft : lunchList) {
                    mapLList.add((double) foodMap.get(ft));
                }
                for (FoodType ft : dinnerList) {
                    mapDList.add((double) foodMap.get(ft));
                }

                familySatisfaction.put(fName, 0.0);
                breakfastArray.put(fName, mapBList);
                lunchArray.put(fName, mapLList);
                dinnerArray.put(fName, mapDList);
                frequencyArray.put(fName, repList);
            }
        }

        // (Spencer) just add 28 of each meal for each member (enough to last 4 weeks if we cannot buy again)
        int numMeals = familyMembers.size() * 28;
    	
    	ShoppingList shoppingList = new ShoppingList();
    	shoppingList.addLimit(MealType.BREAKFAST, numMeals * 10);
    	shoppingList.addLimit(MealType.LUNCH, numMeals * 10);
    	shoppingList.addLimit(MealType.DINNER, numMeals * 20);
    	
    	List<FoodType> breakfastFoods = Food.getFoodTypes(MealType.BREAKFAST);
    	List<FoodType> lunchFoods = Food.getFoodTypes(MealType.LUNCH);
    	List<FoodType> dinnerFoods = Food.getFoodTypes(MealType.DINNER);
        
        for (int repeat = 0; repeat < numMeals; repeat++) {
            for (int i = 0; i < 10; i++) {
                shoppingList.addToOrder(breakfastFoods.get(i));
                shoppingList.addToOrder(lunchFoods.get(i));
                shoppingList.addToOrder(dinnerFoods.get(i));
            }
            for (int i = 10; i < 20; i++) { 
                shoppingList.addToOrder(dinnerFoods.get(i));
            }
        }
        // simPrinter.println(shoppingList.getFullOrderMap());
        if(Player.hasValidShoppingList(shoppingList, numEmptySlots))
            return shoppingList;
        simPrinter.println("\n\nShopping list was invalid\n\n");
    	return new ShoppingList();
    }

    /**
    * Plan meals
    *
    * @param week           current week
    * @param familyMembers  all family members
    * @param pantry         pantry inventory of remaining foods
    * @param mealHistory    history of previous meal allocations
    * @return               planner of assigned meals for the week
    *
    */
    public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

        List<MemberName> memberNames = new ArrayList<>();
        for(FamilyMember familyMember : familyMembers)
            memberNames.add(familyMember.getName());
        
        Pantry originalPantry = pantry.clone();
        Planner planner = new Planner(memberNames);

        // TODO? recalculate family satisfaction with new satisfaction for this week

        // make hashmaps for planned meals
        HashMap<MemberName, FoodType> breakfastList = new HashMap<>();
        HashMap<MemberName, FoodType> lunchList = new HashMap<>();
        HashMap<MemberName, FoodType> dinnerList = new HashMap<>();

        // the order of family members, sorted by their satisfaction 
        List<MemberName> familyMemberOrder = null;
        
        // Spencer: 
        for(Day day : Day.values()) {
            // breakfast
            // never modify breakfast satisfactions after it's created

            // get order of family members (Nuneke's function)
            familyMemberOrder = getFamilyMembers(); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (MemberName fam : familyMemberOrder) {
                // for each of that family member's breakfast array (sorted):
                Arrays.sort(breakfastIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(breakfastArray.get(fam).get(o1), breakfastArray.get(fam).get(o2));
                    }
                }); 

                for (Integer breakfastIndx : breakfastIndices) {
                    // assign the meal if it's available & break 
                    int bre = pantry.getNumAvailableMeals(Food.getAllFoodTypes().get(breakfastIndx));
                    if (bre >= 1) {
                        planner.addMeal(day, fam, MealType.BREAKFAST, Food.getAllFoodTypes().get(breakfastIndx));
                        pantry.removeMealFromInventory(Food.getAllFoodTypes().get(breakfastIndx));
                        breakfastList.put(fam,Food.getAllFoodTypes().get(breakfastIndx));
                        break;
                    }
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(breakfastList, 0);

            
            // lunch
            familyMemberOrder = getFamilyMembers(); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (MemberName fam : familyMemberOrder) {
                // for each of that family member's breakfast array (sorted):
                Arrays.sort(lunchIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(lunchArray.get(fam).get(o1), lunchArray.get(fam).get(o2));
                    }
                }); 

                for (Integer lunchcIndx : lunchIndices) {
                    // assign the meal if it's available & break 
                    int lun = pantry.getNumAvailableMeals(Food.getAllFoodTypes().get(lunchcIndx + 10));
                    if (lun >= 1) {
                        planner.addMeal(day, fam, MealType.LUNCH, Food.getAllFoodTypes().get(lunchcIndx + 10));
                        pantry.removeMealFromInventory(Food.getAllFoodTypes().get(lunchcIndx + 10));
                        lunchList.put(fam,Food.getAllFoodTypes().get(lunchcIndx + 10));
                        break;
                    }
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(lunchList, 1);

            // dinner
            familyMemberOrder = getFamilyMembers(); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (MemberName fam : familyMemberOrder) {
                // for each of that family member's breakfast array (sorted):
                Arrays.sort(dinnerIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(dinnerArray.get(fam).get(o1), dinnerArray.get(fam).get(o2));
                    }
                }); 

                for (Integer dinnerIndx : dinnerIndices) {
                    // assign the meal if it's available & break 
                    int din = pantry.getNumAvailableMeals(Food.getAllFoodTypes().get(dinnerIndx + 20));
                    if (din >= 1) {
                        planner.addMeal(day, fam, MealType.DINNER, Food.getAllFoodTypes().get(dinnerIndx + 20));
                        pantry.removeMealFromInventory(Food.getAllFoodTypes().get(dinnerIndx + 20));
                        dinnerList.put(fam,Food.getAllFoodTypes().get(dinnerIndx + 20));
                        break;
                    }
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(dinnerList, 2);
            

            // update frequency + preference arrays after every day
            updateFrequency(lunchList, dinnerList);
            updatePreference(familyMembers);
        }

        if(Player.hasValidPlanner(planner, originalPantry))
            return planner;
        simPrinter.println("\n\nPlanner was invalid\n\n");
    	return new Planner();
    }


    /**
    * Updates the preferences for each meal at the end of each day
    * If a food has never been eaten, i.e. frequency is 0, perference remains same as original preference
    * If a food was eaten d days ago, set preference to (d/d+1) original perference
    * @param familyMembers    list of family memebers
    **/
    void updatePreference(List<FamilyMember> familyMembers) {
        //update preferences for eah family member
        for (FamilyMember m : familyMembers) {
            MemberName p = m.getName();
            int days;
            double oldP; //original preference value from the configuration file
            double newP;

            //update lunch preferences
            for (int l=0; l<20; l++){
                days = frequencyArray.get(p).get(l+10); //food eaten d days ago
                oldP = m.getFoodPreference(FoodType.values()[l+10]);
                newP= (days > 0) ? ((double)days/(days+1)*oldP) : oldP;
                lunchArray.get(p).set(l, newP);
            }

            //update dinner preferences
            for (int d=0; d<10; d++){
                days = frequencyArray.get(p).get(d+30);
                oldP = m.getFoodPreference(FoodType.values()[d+30]);
                newP = (days > 0) ? ((double)days/(days+1)*oldP) : oldP;
                dinnerArray.get(p).set(d, newP);
            }
        }
    }

    
    /**
    * Updates frequency for each meal according to the meals that were assigned today
    * if a food has never been eaten, set frequency to 0 if still not eaten today, otherwise set to 1
    * if a food was eaten d days ago, set frequency to d+1 if not eaten today, otherwise set to 1
    * @param lunchList      lunch assigned to each family memeber
    * @param dinnerList     dinner assigned to each family member
    **/
    void updateFrequency(HashMap<MemberName, FoodType> lunchList, HashMap<MemberName, FoodType> dinnerList){
        //verify that lunchList contains lunch for every family member
        assert lunchList.size() == this.numFamilyMembers;

        //update frequency list for each family member
        for (MemberName p : lunchList.keySet()) {
            int lunch = lunchList.get(p).ordinal();
            int dinner = dinnerList.get(p).ordinal();
            int daysAgo;
            for (int i=0; i<40; i++){
                if (frequencyArray.get(p).get(i) == 0) { //this food type has never been eaten
                    daysAgo = (i == lunch || i == dinner) ? 1:0;
                } else { //this food type was eaten x days ago > x+1 if not eaten today
                    daysAgo = (i == lunch || i == dinner) ? 1:frequencyArray.get(p).get(i)+1;
                }
                //update
                frequencyArray.get(p).set(i, daysAgo);
            }
        }
    }

    // Nuneke
    private void recalcSatisfaction(HashMap<MemberName, FoodType> assignedMeal, Integer flagDigit){
        // hashmap of assigned meals, flag digit = 0B, 1L, 2D 
        // updates each individual family member's satisfaction 

        for (Map.Entry<MemberName, FoodType> am : assignedMeal.entrySet()) {
            MemberName theName = am.getKey(); 
            double satis = familySatisfaction.get(theName);
            double pref;
            FoodType theFoodType = am.getValue(); 
            if (flagDigit == 0){
                pref = breakfastArray.get(theName).get(theFoodType.ordinal());
            } 
            else if (flagDigit == 1){
                pref = lunchArray.get(theName).get(theFoodType.ordinal()-10);
            } 
            else {
                pref = dinnerArray.get(theName).get(theFoodType.ordinal()-20);
            }

            satis += pref;
            familySatisfaction.replace(theName, satis);

        } 
    }

    // Nuneke: 
    private List<MemberName> getFamilyMembers() {
        // looks at family member satsifaction hashmap
        // update each individual satisfaction
        // return ordered list of hashmap keys

        List<Map.Entry<MemberName, Double> > satList = new LinkedList<Map.Entry<MemberName, Double> >(familySatisfaction.entrySet()); 
  
        // Sort the list 
        Collections.sort(satList, new Comparator<Map.Entry<MemberName, Double> >() { 
            public int compare(Map.Entry<MemberName, Double> l1,  
                               Map.Entry<MemberName, Double> l2) 
            { 
                return (l1.getValue()).compareTo(l2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        List<MemberName> orderedFamMem = new LinkedList<MemberName>(); 
        for (Map.Entry<MemberName, Double> t : satList) { 
            orderedFamMem.add(t.getKey()); 
        } 
        return orderedFamMem; 
    }

}
