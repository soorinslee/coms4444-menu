package menu.g3;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
import java.util.HashMap;
import java.util.Map;
// import sun.util.locale.provider.AvailableLanguageTags;

public class Player extends menu.sim.Player {

    private HashMap<MemberName, List<Double>> breakfastArray = new HashMap<>(MemberName, List<Double>); // never gets updated 
    private HashMap<MemberName, List<Double>> lunchArray = new HashMap<>(MemberName, List<Double>); // updated with each day according to frequency 
    private HashMap<MemberName, List<Double>> dinnerArray = new HashMap<>(MemberName, List<Double>); // updated with each day according to frequency 

    private HashMap<MemberName, List<Integer>> frequencyArray = new HashMap<>(MemberName, List<Integer>); // keeps track of how many times this meal was eaten in the past X days 
    /* these take the form of:
        B1, L1, D1 ... are satisfactions for each meal
        fm 1: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 2: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 3: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        . . .
    */

    // private familySatisfaction // array that keeps track of the family member's satisfaction for each day
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
        // TODO: create the breakfast, lunch, and dinner arrays (Nuneke)
        // family satisfaction, all 0 to start off 
        // frequency array, all 0 to start off 
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
        simPrinter.println(shoppingList.getFullOrderMap());
        if(Player.hasValidShoppingList(shoppingList, numEmptySlots))
    		return shoppingList;
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

        // recalculate family satisfaction with new satisfaction for this week

        // make hashmaps for planned meals
        HashMap<MemberName, FoodType> lunchList = new HashMap<MemberName, FoodType>;
        HashMap<MemberName, FoodType> lunchList = new HashMap<MemberName, FoodType>;

        // the order of family members, sorted by their satisfaction 
        List<FamilyMember> familyMemberOrder = null;
        
        // Spencer: 
        // for every day:
        for (int day = 1; day <= 7, day++) {
            // breakfast
            // never modify breakfast satisfactions after it's created

            // get order of family members (Nuneke's function)
            familyMemberOrder = getFamilyMembers() // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (FamilyMember fam : familyMemberOrder) {
                // for each of that family member's breakfast array (sorted):
                for (FoodType breakfast : breakfastArray.get(fam)) {

                }
                    // assign the meal if it's available & break 
                        // assign = hashmap: family member --> assigned breakfast      
            }
            // recalculate satisfcation
            // recalcSatisfaction(hashmap of assigned breakfasts, 0) // --> update lunch + dinner satisfaction matrices




            // lunch
            familyMemberOrder = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's lunch array (sorted):
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            // recalcSatisfaction(hashmap of assigned lunches, 1) // --> update lunch + dinner satisfaction matrices




            // dinner
            familyMemberOrder = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's meals:
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            // recalcSatisfaction([list of dinners assigned in that day], familyMemberList, 2) // --> update lunch + dinner satisfaction matrices

            // update frequency array
            //udpateFrequency()
            // updatePreferneces()

        }
        // create Planner object 

        return null; // TODO modify the return statement to return your planner
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
    // void recalcSatisfaction(hashmap of assigned meals, flag digit = 0B, 1L, 2D )
        // updates each individual family member's satisfaction 

    // Nuneke: 
    // list getFamilyMembers()
        // looks at family member satsifaction hashmap
        // update each individual satisfaction
        // return ordered list of hashmap keys 

}
