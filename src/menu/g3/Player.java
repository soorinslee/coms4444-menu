package menu.g3;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
// import sun.util.locale.provider.AvailableLanguageTags;

public class Player extends menu.sim.Player {

    private HashMap<MemberName, List<Double>> breakfastArray; // never gets updated 
    private HashMap<MemberName, List<Double>> lunchArray; // updated with each day according to frequency 
    private HashMap<MemberName, List<Double>> dinnerArray; // updated with each day according to frequency 

    private HashMap<MemberName, List<Integer>> frequencyArray; // keeps track of how many times this meal was eaten in the past X days 
    /* these take the form of:
        B1, L1, D1 ... are satisfactions for each meal
        fm 1: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 2: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 3: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        . . .
    */

    private HashMap<MemberName, Double> familySatisfaction; // array that keeps track of the family member's satisfaction for each day
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
        
        // Spencer: 
        // for every day:
            // breakfast
            // never modify breakfast satisfactions after it's created

            // get order of family members (Nuneke's function)
            // familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            // for every family member:
                // for each of that family member's breakfast array (sorted):
                    // assign the meal if it's available & break 
                        // assign = hashmap: family member --> assigned breakfast      

            // recalculate satisfcation
            // recalcSatisfaction(hashmap of assigned breakfasts, 0) // --> update lunch + dinner satisfaction matrices




            // lunch
            // familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's lunch array (sorted):
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            // recalcSatisfaction(hashmap of assigned lunches, 1) // --> update lunch + dinner satisfaction matrices




            // dinner
            // familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's meals:
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            // recalcSatisfaction([list of dinners assigned in that day], familyMemberList, 2) // --> update lunch + dinner satisfaction matrices

            // update frequency array
            // updateFrequency(hashmap of lunches, hashmap of dinners)
            // updatePreferneces(hashmap of lunches, hashmap of dinners)


        // create Planner object 

        return null; // TODO modify the return statement to return your planner
    }


    // Qi
    // void updatePreference()
        // updates the preferences for each meal according to the meals that were assigned today 

    // Qi   
    // void updateFrequency()
        // remember to change the frequency of meals that weren't eaten too! 
            // 1 meal in the past two days --> 1 meal in the past 3 days if not eaten
            // 1 meal in the past two days --> 2 meals in the past 3 days if eaten
        // updates the frequency array for each meal according to the meals that were assigned today 

    // Nuneke
    private void recalcSatisfaction(HashMap<MemberName, FoodType> assignedMeal, Integer flagDigit){
        // hashmap of assigned meals, flag digit = 0B, 1L, 2D 
        // updates each individual family member's satisfaction 

        for (Map.Entry<MemberName, FoodType> am : assignedMeal.entrySet()) {
            MemberName theName = am.getKey(); 
            double satis = familySatisfaction.get(theName);
            double pref;
            FoodType theFoodType = am.getValue(); 
            int theFoodIndex = getFoodInteger(theFoodType);
            if (flagDigit == 0){
                pref = breakfastArray.get(theName).get(theFoodIndex);
            } 
            else if (flagDigit == 1){
                pref = lunchArray.get(theName).get(theFoodIndex);
            } 
            else {
                pref = dinnerArray.get(theName).get(theFoodIndex);
            }

            satis += pref;
            familySatisfaction.replace(theName, satis);

        } 
    }

    private int getFoodInteger(FoodType theFoodType){
        switch(theFoodType){
            case BREAKFAST1:
            case LUNCH1:
            case DINNER1:
                return 0;
            case BREAKFAST2:
            case LUNCH2:
            case DINNER2:
                return 1;
            case BREAKFAST3:
            case LUNCH3:
            case DINNER3:
                return 2;
            case BREAKFAST4:
            case LUNCH4:
            case DINNER4:
                return 3;
            case BREAKFAST5:
            case LUNCH5:
            case DINNER5:
                return 4;
            case BREAKFAST6:
            case LUNCH6:
            case DINNER6:
                return 5;
            case BREAKFAST7:
            case LUNCH7:
            case DINNER7:
                return 6;
            case BREAKFAST8:
            case LUNCH8:
            case DINNER8:
                return 7;
            case BREAKFAST9:
            case LUNCH9:
            case DINNER9:
                return 8;
            case BREAKFAST10:
            case LUNCH10:
            case DINNER10:
                return 9;
            case DINNER11:
                return 10;
            case DINNER12:
                return 11;
            case DINNER13:
                return 12;
            case DINNER14:
                return 13;
            case DINNER15:
                return 14;
            case DINNER16:
                return 15;
            case DINNER17:
                return 16;
            case DINNER18:
                return 17;
            case DINNER19:
                return 18;
            case DINNER20:
                return 19;
            default: return -1;
        }
    }
        

    // Nuneke: 
    private List getFamilyMembers() {
        // looks at family member satsifaction hashmap
        // update each individual satisfaction
        // return ordered list of hashmap keys ]

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
