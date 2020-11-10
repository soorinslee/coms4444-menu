package menu.g3;

import java.io.ObjectInputStream.GetField;
import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
// import sun.util.locale.provider.AvailableLanguageTags;

public class Player extends menu.sim.Player {
    Integer[] breakfastIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    Integer[] lunchIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    Integer[] dinnerIndices = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19 };

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

    // Used in simulation to determine how we stock our pantry 
    HashMap<MealType, List<FoodType>> covetedFoods = new HashMap<>();
    HashMap<MealType, HashMap<MemberName, List<FoodType>>> bestCycles = new HashMap<>();

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
        // initialize frequency, preference, and satisfaction maps
        if (week == 1) {
            List<FoodType> breakfastFoods = Food.getFoodTypes(MealType.BREAKFAST);
            List<FoodType> lunchFoods = Food.getFoodTypes(MealType.LUNCH);
            List<FoodType> dinnerFoods = Food.getFoodTypes(MealType.DINNER);
            
            initializePreference(familyMembers);
            covetedFoods.put(MealType.BREAKFAST, breakfastFoods);
            covetedFoods.put(MealType.LUNCH, lunchFoods);
            covetedFoods.put(MealType.DINNER, dinnerFoods); 
            initializePreference(familyMembers);
        } else {
            covetedFoods = sortCovetedFoods(0.25);
            simPrinter.println("Coveted Breakfast: ");
            simPrinter.println(covetedFoods.get(MealType.BREAKFAST));
            simPrinter.println("Coveted Lunch: ");
            simPrinter.println(covetedFoods.get(MealType.LUNCH));
            simPrinter.println("Coveted Dinner: ");
            simPrinter.println(covetedFoods.get(MealType.DINNER));
        }
        // printPreference();

        // TODO: vv should depend on the number of people in the family and be more reliable lmao 
        // (Spencer) just add 28 of each meal for each member (enough to last 4 weeks if we cannot buy again)
        if (pantry.getNumAvailableMeals() + pantry.getNumEmptySlots() > 100000) {
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

        // if we can max out every food item for every family member to last at least one week under high demand
        // else if (pantry.getNumAvailableMeals() + pantry.getNumEmptySlots() >= 280*familyMembers.size()) {
            // pay attention to what we have in our pantry rn 
            // breakfast 
                    // for first week breafkast (40x7xn slots available)
                            // Qi's magic probability math, reduces the size of the shopping list for breakfast
                                // order 4*7 copies of each item for each person
                                // order 3*7 copies of each item for each person
                                // order 2*7 copies of each item for each person
                                // order 1*7 copies of each item for each person
                    // for subsequent weeks, only do ^ when the least satsified is not getting the breakfasts they like 
        // }
        
        // for small pantry sizes (minimum = 21*number of family members)
        else {
            int numMeals = familyMembers.size() * 7;
        
            ShoppingList shoppingList = new ShoppingList();

            // how many extra meals are available for every family member (assuming empty pantry)
            // int extraSpace = Math.floor((pantry.getNumAvailableMeals() + pantry.getNumEmptySlots() - 21*familyMembers.size())/familyMembers.size());
            // if we have some leeway for overstocking pantry
            // if (extraSpace > 0) {
            if (false) {
                return new ShoppingList();
                // TODO?: make sure to use up "stale" food items
            }

            // we cannot overstock the pantry 
            // determine overall preference for each meal by running a fake week simulation
                    // keeping track of the preverence values for every meal for every person for every day
                    // paying attention to the lowest satisfied family members when buying 
                    // find the best outcome cycle of dinners for the least satisfied family member 
            else {
                shoppingList.addLimit(MealType.BREAKFAST, numMeals);
                shoppingList.addLimit(MealType.LUNCH, numMeals);
                shoppingList.addLimit(MealType.DINNER, numMeals);

                Pantry full_pantry = new Pantry(40 * numMeals);
                for(FoodType ft : Food.getFoodTypes(MealType.BREAKFAST)){
                    for(int i=0; i<numMeals; i++) {
                        full_pantry.addMealToInventory(ft);
                    }
                }
                for(FoodType ft : Food.getFoodTypes(MealType.LUNCH)){
                    for(int i=0; i<numMeals; i++) {
                        full_pantry.addMealToInventory(ft);
                    }
                }
                for(FoodType ft : Food.getFoodTypes(MealType.DINNER)){
                    for(int i=0; i<numMeals; i++) {
                        full_pantry.addMealToInventory(ft);
                    }
                }
                simulatePlan(familyMembers, full_pantry, mealHistory);  

                

                // breakfast: purhcase at least 7 * n, try to over stock because it's dependable 
                    // for breakfast: 
                        // according to family member preferences, find everyone's favorite breakfast foods
                        // add 7 of each person's favoite breakfast foods 

                    // 7*least satisfied favorite breakfast, 7*next least satisfied favorite breakfast ... 
                        // if every family member does not get their # 1 favorite, then we will move on to the ranked preferences 
                    
                    // the rest of the list 
                        // 7 * n * most coveted breakfast - (7 * most coveted * number of times it appeared in ^^) - # of breakfast we have already 
                        // 7 * n * 2nd most coveted breakfast - (7 * 2nd most coveted * number of times it appeared in ^^) - # of breakfast we have already 
                        // . . . for the top 6 breakfasts (ensures we get at least one breakfast) - # of breakfast we have already  
                List<FoodType> breakfastList = getBreakfastList(familyMembers);
                for(FoodType breakfastItem : breakfastList){
                    shoppingList.addToOrder(breakfastItem);
                }

                // lunch 
                    // similar to dinner, get everyone's top options for every day and this is the beginning of the list 
                    // Everything after this: zipping together preferneces like in dinner
                List<FoodType> lunchList = getLunchList(familyMembers);
                for(FoodType lunchItem : lunchList){
                    shoppingList.addToOrder(lunchItem);
                }

                // dinner: purhchase at least 7 * n, make list long enough for worst case 
                    // get best cycle from loop
                        // try to buy that cycle

                    // if that cycle is not available
                        // look at the 25% least satisfied people
                            // for every day in simulation:
                                // for the least satisfied family member:
                                    // for each dinner:
                                        // adjust the preference value for that dinner

                        // buy maximum 7*n, list is 37*n
                            // 3*N*d1, 2*n*dinner2, 2*n*dinner3 (remember to subtract what is available in pantry)
                            // 2*n*dinner4, 1*n*dinner2
                            // 2*n*dinner5, 1*n*dinnner3
                List<FoodType> dinnerList = getDinnerList(familyMembers);
                for(FoodType dinnerItem : dinnerList){
                    shoppingList.addToOrder(dinnerItem);
                }

                simPrinter.println("numemptyspots: " + numEmptySlots);
                for (MealType mt: Food.getAllMealTypes()){
                    simPrinter.println("listlimit: " + shoppingList.getAllLimitsMap().get(mt));
                }

                if(Player.hasValidShoppingList(shoppingList, numEmptySlots)){
                    return shoppingList;
                }
                simPrinter.println("Shopping list was invalid");
                return new ShoppingList();
            }
        }
    }

    private List getBreakfastList(List<FamilyMember> familyMembers) {
        
        List<FoodType> breakfastList = new ArrayList<>();

        for(FamilyMember fm : familyMembers){
            List<FoodType> fav = bestCycles.get(MealType.BREAKFAST).get(fm.getName());
            for(FoodType ft : fav){
                breakfastList.add(ft);
            }
        }

        List<FoodType> covBreakfast = covetedFoods.get(MealType.BREAKFAST);
        for (int i=0; i<6; i++) {
            for(FamilyMember fm : familyMembers){
                for (int j=0; j<7; j++) {
                    breakfastList.add(covBreakfast.get(i));
                }
            }
        }

        return breakfastList;

    }

    private List getLunchList(List<FamilyMember> familyMembers) {
        
        List<FoodType> lunchList = new ArrayList<>();
        for(FamilyMember fm : familyMembers){
            List<FoodType> fav = bestCycles.get(MealType.LUNCH).get(fm.getName());
            for(FoodType ft : fav){
                lunchList.add(ft);
            }
        }

        List<FoodType> covLunch = covetedFoods.get(MealType.LUNCH);
        for (int i=0; i<6; i++) {
            for(FamilyMember fm : familyMembers){
                if (i==0){
                    for (int j=0; j<3; j++) {
                        lunchList.add(covLunch.get(i));
                    }
                    for (int j=0; j<2; j++) {
                        lunchList.add(covLunch.get(i+1));
                    }
                    for (int j=0; j<2; j++) {
                        lunchList.add(covLunch.get(i+2));
                    }
                }
                else{
                    for (int j=0; j<2; j++) {
                        lunchList.add(covLunch.get(i+2));
                    }
                    lunchList.add(covLunch.get(i));
                }
            }
        }

        return lunchList;

    }

    private List getDinnerList(List<FamilyMember> familyMembers) {
        
        List<FoodType> dinnerList = new ArrayList<>();

        for(FamilyMember fm : familyMembers){
            List<FoodType> fav = bestCycles.get(MealType.DINNER).get(fm.getName());
            for(FoodType ft : fav){
                dinnerList.add(ft);
            }
        }

        List<FoodType> covDinner = covetedFoods.get(MealType.DINNER);
        for (int i=0; i<11; i++) {
            if (i==0){
                for(FamilyMember fm : familyMembers){
                    for (int j=0; j<3; j++) {
                        dinnerList.add(covDinner.get(i));
                    }
                }
                for(FamilyMember fm : familyMembers){
                    for (int j=0; j<2; j++) {
                        dinnerList.add(covDinner.get(i+1));
                    }
                }
                for(FamilyMember fm : familyMembers){
                    for (int j=0; j<2; j++) {
                        dinnerList.add(covDinner.get(i+2));
                    }
                }
            }
            else{
                for(FamilyMember fm : familyMembers){
                    for (int j=0; j<2; j++) {
                        dinnerList.add(covDinner.get(i+2));
                    }
                }
                for(FamilyMember fm : familyMembers){
                    dinnerList.add(covDinner.get(i));
                }
            }
        }

        simPrinter.println(dinnerList);

        return dinnerList;

    }


    /* 
    * Simulation modifies lists of covetedFoods and ideal meal orders assuming we had every food item
    *     HashMap<MealType, List<FoodType>> covetedFoods = new HashMap<>();
    *         BREAKFAST : list of most coveted food item -> least coveted food item (based on the weighted satisfaction they would give)
    *         . . . 
    * 
    *     HashMap<MealType, HashMap<MemberName, List<FoodType>>> bestCycles
    *         REAKFAST : Pam : <BMon, BTues ... >
    *                    Jim : <BMon, BTues ... >
    *                    . . . 
    *         . . . 
    */
    private void simulatePlan(List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

        Boolean first = true;

        HashMap<MemberName, List<Double>> breakfastArrayCopy = deepCopyMeals(breakfastArray); 
        HashMap<MemberName, List<Double>> lunchArrayCopy = deepCopyMeals(lunchArray); 
        HashMap<MemberName, List<Double>> dinnerArrayCopy = deepCopyMeals(dinnerArray); 

        HashMap<MemberName, Double> familySatisfactionCopy = deepCopySatisfaction(familySatisfaction); 
        HashMap<MemberName, List<Integer>> frequencyArrayCopy = deepCopyFrequency(frequencyArray); 
        bestCycles.put(MealType.BREAKFAST, new HashMap<MemberName, List<FoodType>>());
        bestCycles.put(MealType.LUNCH, new HashMap<MemberName, List<FoodType>>());
        bestCycles.put(MealType.DINNER, new HashMap<MemberName, List<FoodType>>());

        List<MemberName> familyMemberOrder = getFamilyMembers(familySatisfactionCopy);
        for (MemberName fam : familyMemberOrder) {
            bestCycles.get(MealType.BREAKFAST).put(fam, new ArrayList<FoodType>());
            bestCycles.get(MealType.LUNCH).put(fam, new ArrayList<FoodType>());
            bestCycles.get(MealType.DINNER).put(fam, new ArrayList<FoodType>());
        }

        List<MemberName> memberNames = new ArrayList<>();
        for(FamilyMember familyMember : familyMembers)
            memberNames.add(familyMember.getName());

        // make hashmaps for planned meals
        HashMap<MemberName, FoodType> breakfastList = new HashMap<>();
        HashMap<MemberName, FoodType> lunchList = new HashMap<>();
        HashMap<MemberName, FoodType> dinnerList = new HashMap<>();

        for(Day day : Day.values()) {
            familyMemberOrder = getFamilyMembers(familySatisfactionCopy); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            
            for (MemberName fam : familyMemberOrder) {
                Arrays.sort(breakfastIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(breakfastArrayCopy.get(fam).get(o2), breakfastArrayCopy.get(fam).get(o1));
                    }
                });

                first = true;
                for (Integer breakfastIndx : breakfastIndices) {
                    if (first) { // "choosing" first meal
                        first = false;
                        bestCycles.get(MealType.BREAKFAST).get(fam).add(Food.getAllFoodTypes().get(breakfastIndx));
                        breakfastList.put(fam, Food.getAllFoodTypes().get(breakfastIndx));
                    }
                    
                    // Qi:
                    // calculate how much satisfaction it would give you 
                    // add this satisfaction * weighting factor to covetedFoods
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(breakfastList, 0, familySatisfactionCopy, breakfastArrayCopy, lunchArrayCopy, dinnerArrayCopy);

            
            // lunch
            familyMemberOrder = getFamilyMembers(familySatisfactionCopy); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (MemberName fam : familyMemberOrder) {
                Arrays.sort(lunchIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(lunchArrayCopy.get(fam).get(o2), lunchArrayCopy.get(fam).get(o1));
                    }
                }); 

                first = true;
                for (Integer lunchcIndx : lunchIndices) {
                    if (first) { // "choosing" first meal
                        first = false;
                        bestCycles.get(MealType.LUNCH).get(fam).add(Food.getAllFoodTypes().get(lunchcIndx + 10));
                        lunchList.put(fam, Food.getAllFoodTypes().get(lunchcIndx + 10));
                    }

                    // Qi:
                    // calculate how much satisfaction it would give you 
                    // add this satisfaction * weighting factor to covetedFoods
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(lunchList, 1, familySatisfactionCopy, breakfastArrayCopy, lunchArrayCopy, dinnerArrayCopy);

            // dinner
            familyMemberOrder = getFamilyMembers(familySatisfactionCopy); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            MemberName sadFam = familyMemberOrder.get(0);

            Arrays.sort(dinnerIndices, new Comparator<Integer>() {
                @Override public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(dinnerArrayCopy.get(sadFam).get(o2), dinnerArrayCopy.get(sadFam).get(o1));
                }
            }); 
            
            for (MemberName fam2 : familyMemberOrder) {
                bestCycles.get(MealType.DINNER).get(fam2).add(Food.getAllFoodTypes().get(dinnerIndices[0] + 20));
                dinnerList.put(fam2, Food.getAllFoodTypes().get(dinnerIndices[0] + 20));
            }

            for (MemberName fam : familyMemberOrder) {
                first = true;
                for (Integer lunchcIndx : lunchIndices) {
                    if (first)
                        first = false;

                    // Qi:
                    // calculate how much satisfaction it would give you 
                    // add this satisfaction * weighting factor to covetedFoods
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(dinnerList, 2, familySatisfactionCopy, breakfastArrayCopy, lunchArrayCopy, dinnerArrayCopy);

            // update frequency + preference arrays after every day
            updateFrequency(lunchList, dinnerList, frequencyArrayCopy);
            updatePreference(familyMembers, frequencyArrayCopy, lunchArrayCopy, dinnerArrayCopy);
        }
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
        
        // simPrinter.println("breakfast array:" + breakfastArray);
        // simPrinter.println("lunch array:" + lunchArray);
        // simPrinter.println("dinner array:" + dinnerArray);

        // TODO: Do we have any "stale" food in pantry --> keep track

        // Spencer: 
        for(Day day : Day.values()) {
            // breakfast
            // never modify breakfast satisfactions after it's created

            // get order of family members (Nuneke's function)
            familyMemberOrder = getFamilyMembers(familySatisfaction); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            // simPrinter.println("family member order: " + familyMemberOrder);
            for (MemberName fam : familyMemberOrder) {
                // TODO: (Spencer) Create a set of meals not to touch for bottom 25% 
                    // highest satisfaction - lowest satisfaction / 4 --> anyone < this number is nnot satisfied 

                // for each of that family member's breakfast array (sorted):
                Arrays.sort(breakfastIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(breakfastArray.get(fam).get(o2), breakfastArray.get(fam).get(o1));
                    }
                });
                //simPrinter.println("Sorted Breakfasts: " + Arrays.toString(breakfastIndices));

                for (Integer breakfastIndx : breakfastIndices) {
                    // assign the meal if it's available & break 
                    int bre = pantry.getNumAvailableMeals(Food.getAllFoodTypes().get(breakfastIndx));
                    if (bre >= 1) {
                        // simPrinter.println("Added breakfast for " + fam + ": " + Food.getAllFoodTypes().get(breakfastIndx));
                        planner.addMeal(day, fam, MealType.BREAKFAST, Food.getAllFoodTypes().get(breakfastIndx));
                        pantry.removeMealFromInventory(Food.getAllFoodTypes().get(breakfastIndx));
                        breakfastList.put(fam,Food.getAllFoodTypes().get(breakfastIndx));
                        break;
                    }
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(breakfastList, 0, familySatisfaction, breakfastArray, lunchArray, dinnerArray);

            
            // lunch
            familyMemberOrder = getFamilyMembers(familySatisfaction); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            for (MemberName fam : familyMemberOrder) {
                // for each of that family member's breakfast array (sorted):
                Arrays.sort(lunchIndices, new Comparator<Integer>() {
                    @Override public int compare(final Integer o1, final Integer o2) {
                        return Double.compare(lunchArray.get(fam).get(o2), lunchArray.get(fam).get(o1));
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
            recalcSatisfaction(lunchList, 1, familySatisfaction, breakfastArray, lunchArray, dinnerArray);

            // dinner
            familyMemberOrder = getFamilyMembers(familySatisfaction); // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            MemberName fam = familyMemberOrder.get(0);
            // for each of that family member's breakfast array (sorted):
            Arrays.sort(dinnerIndices, new Comparator<Integer>() {
                @Override public int compare(final Integer o1, final Integer o2) {
                    return Double.compare(dinnerArray.get(fam).get(o2), dinnerArray.get(fam).get(o1));
                }
            }); 
            // TODO: see if we should choose a less favorable dinner if we have enough to go around 
            for (MemberName fam2 : familyMemberOrder) {
                for (Integer dinnerIndx : dinnerIndices) {
                    // assign the meal if it's available & break 
                    // TODO?: make sure we have enough of the meal!
                    int din = pantry.getNumAvailableMeals(Food.getAllFoodTypes().get(dinnerIndx + 20));
                    if (din >= 1) {
                        planner.addMeal(day, fam2, MealType.DINNER, Food.getAllFoodTypes().get(dinnerIndx + 20));
                        pantry.removeMealFromInventory(Food.getAllFoodTypes().get(dinnerIndx + 20));
                        dinnerList.put(fam2,Food.getAllFoodTypes().get(dinnerIndx + 20));
                        break;
                    }
                }
            }
            // recalculate satisfcation
            recalcSatisfaction(dinnerList, 2, familySatisfaction, breakfastArray, lunchArray, dinnerArray);

            // update frequency + preference arrays after every day
            updateFrequency(lunchList, dinnerList, frequencyArray);
            updatePreference(familyMembers, frequencyArray, lunchArray, dinnerArray);
        }

        printPlanner(planner, week);

        if(Player.hasValidPlanner(planner, originalPantry))
            return planner;
        System.out.println("\nPlanner was invalid");
        return new Planner();
    }


    /**
    * Initializes the preference arrays and the frequency array at the beginning of week 1
    * Set all frequency to zero
    * Set all preference value to match the configuration file
    * @param familyMembers    list of family memebers
    **/
    private void initializePreference(List<FamilyMember> familyMembers){
         
        for (FamilyMember m : familyMembers) {
            MemberName p = m.getName();
            
            //initialize breakfastArray
            List<Double> breakfast = new ArrayList<>();
            for (FoodType food : Food.getFoodTypes(MealType.BREAKFAST)) {
                breakfast.add(m.getFoodPreference(food));
            }
            breakfastArray.put(p, breakfast);

            //initialize lunchArray
            List<Double> lunch = new ArrayList<>();
            for (FoodType food : Food.getFoodTypes(MealType.LUNCH)) {
                lunch.add(m.getFoodPreference(food));
            }
            lunchArray.put(p, lunch);

            //initialize dinnerArray
            List<Double> dinner = new ArrayList<>();
            for (FoodType food : Food.getFoodTypes(MealType.DINNER)) {
                dinner.add(m.getFoodPreference(food));
            }
            dinnerArray.put(p, dinner);

            //initialize frequency
            List<Integer> frequency = new ArrayList<Integer>(Collections.nCopies(40, 0));
            frequencyArray.put(p, frequency);

            //initialize satisfaction
            familySatisfaction.put(p, 0.0);
        }
    }

    //print food preference arrays and frequency array
    private void printPreference(){
        simPrinter.println("Breakfast preferences: "); 
        breakfastArray.entrySet().forEach(entry->{
            simPrinter.println(entry.getKey() + " " + entry.getValue());  
        });
        simPrinter.println("Lunch preferences: "); 
        lunchArray.entrySet().forEach(entry->{
            simPrinter.println(entry.getKey() + " " + entry.getValue());  
        });
        simPrinter.println("Dinner preferences: "); 
        dinnerArray.entrySet().forEach(entry->{
            simPrinter.println(entry.getKey() + " " + entry.getValue());  
        });
        simPrinter.println("Frequencies: "); 
        frequencyArray.entrySet().forEach(entry->{
            simPrinter.println(entry.getKey() + " " + entry.getValue());  
        });
    }


    /**
    * Updates the preferences for each meal at the end of each day
    * If a food has never been eaten, i.e. frequency is 0, perference remains same as original preference
    * If a food was eaten d days ago, set preference to (d/d+1) original perference
    * @param familyMembers    list of family memebers
    **/
    void updatePreference(List<FamilyMember> familyMembers, HashMap<MemberName, List<Integer>> frequencyArray, 
                          HashMap<MemberName, List<Double>> lunchArray, HashMap<MemberName, List<Double>> dinnerArray) {
        //update preferences for eah family member
        for (FamilyMember m : familyMembers) {
            MemberName p = m.getName();
            int days;
            double oldP; //original preference value from the configuration file
            double newP;

            //update lunch preferences
            for (int l=0; l<10; l++){
                days = frequencyArray.get(p).get(l+10); //food eaten d days ago
                oldP = m.getFoodPreference(FoodType.values()[l+10]);
                newP= (days > 0) ? ((double)days/(days+1)*oldP) : oldP;
                lunchArray.get(p).set(l, newP);
            }

            //update dinner preferences
            for (int d=0; d<20; d++){
                days = frequencyArray.get(p).get(d+20);
                oldP = m.getFoodPreference(FoodType.values()[d+20]);
                newP = (days > 0) ? ((double)days/(days+1)*oldP) : oldP;
                dinnerArray.get(p).set(d, newP);
            }
        }
    }

    private void printPlanner(Planner planner, Integer week) {
        simPrinter.println("\nWeek " + week + " planner: ");
        for (Day key : new Day[]{Day.MONDAY,Day.TUESDAY,Day.WEDNESDAY,Day.THURSDAY,Day.FRIDAY,Day.SATURDAY,Day.SUNDAY}) {
            simPrinter.println("\t" + key + ":");
            for (MemberName name : planner.getPlan().get(key).keySet()) {
                simPrinter.println("\t\t" + name + "\t\t" + planner.getPlan().get(key).get(name).get(MealType.BREAKFAST) + "\t" +
                planner.getPlan().get(key).get(name).get(MealType.LUNCH) + "\t\t" + planner.getPlan().get(key).get(name).get(MealType.DINNER));
            }
            simPrinter.println("");
        }
    }

    /**
    * Updates frequency for each meal according to the meals that were assigned today
    * if a food has never been eaten, set frequency to 0 if still not eaten today, otherwise set to 1
    * if a food was eaten d days ago, set frequency to d+1 if not eaten today, otherwise set to 1
    * @param lunchList      lunch assigned to each family memeber
    * @param dinnerList     dinner assigned to each family member
    **/
    private void updateFrequency(HashMap<MemberName, FoodType> lunchList, HashMap<MemberName, FoodType> dinnerList, HashMap<MemberName, List<Integer>> frequencyArray){
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

    /**
    * Sort all food according to weighted satisfaction
    * @param percentile    the preference of the bottom p percentile members will be weighted the most
    **/
    private HashMap<MealType, List<FoodType>> sortCovetedFoods(Double percentile) {
        HashMap<MealType, List<FoodType>> covetedFoods = new HashMap<>();

        List<MemberName> members = getFamilyMembers(familySatisfaction);
        Double minSatisfaction = familySatisfaction.get(members.get(0));
        Double maxSatisfaction = familySatisfaction.get(members.get(members.size() - 1));
        Double threshold = (maxSatisfaction - minSatisfaction) * percentile + minSatisfaction;
        //simPrinter.println("least satisfied: " + members.get(0));
        //simPrinter.println("least satisfied member's breakfast preference: " + breakfastArray.get(members.get(0)));
        //simPrinter.println("min : " + minSatisfaction);
        //simPrinter.println("max : " + maxSatisfaction);
        //simPrinter.println("threshold : " + threshold);

        //find the Kth member whose satisfaction is just above the threshold
        int start = 0, end = members.size() - 1, k = -1;
        while (start <= end) {
            int mid = (start + end) / 2;
            if (familySatisfaction.get(members.get(mid)) <= threshold){
                start = mid + 1;
            } else {
                k = mid;
                end = mid - 1;
            }
        }
        //simPrinter.println("k:");
        //simPrinter.println(k);

        //weighted Breakfast, lunch, and dinner preference
        Double weightedBreakfast[] = new Double[10];
        Double weightedLunch[] = new Double[10];
        Double weightedDinner[] = new Double[20];
        Arrays.fill(weightedBreakfast, 0.0);
        Arrays.fill(weightedLunch, 0.0);
        Arrays.fill(weightedDinner, 0.0);

        for (int i=0; i<20; i++){

            for (int j=0; j<k; j++){ //only consider the least satisfied k members
                //weighted preference for ith breakfast/lunch/dinner = sum of the preferences for k least satisfied member
                //weights = k, k-1, ..., 1
                if (i < 10) {
                    //simPrinter.println("i: " + breakfastArray.get(members.get(j)).get(i) * (k-j));
                    weightedBreakfast[i] += breakfastArray.get(members.get(j)).get(i) * (k-j);
                    //simPrinter.println("weighted p: " + members.get(j));
                    weightedLunch[i] += lunchArray.get(members.get(j)).get(i) * (k-j);
                }
                weightedDinner[i] += dinnerArray.get(members.get(j)).get(i) * (k-j);
            }
        }

        //sort the indices of the breakfast, lunch, and dinner items according to weighted preference values
        Arrays.sort(breakfastIndices, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return Double.compare(weightedBreakfast[o2], weightedBreakfast[o1]);
            }
        });
        Arrays.sort(lunchIndices, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return Double.compare(weightedLunch[o2], weightedLunch[o1]);
            }
        });
        Arrays.sort(dinnerIndices, new Comparator<Integer>() {
            @Override public int compare(final Integer o1, final Integer o2) {
                return Double.compare(weightedDinner[o2], weightedDinner[o1]);
            }
        });

        //make sorted list of covted food for each meal type
        List<FoodType> orderedBreakfast = new ArrayList<>();
        List<FoodType> orderedLunch = new ArrayList<>();
        List<FoodType> orderedDinner = new ArrayList<>(); 
        for (int i=0; i<20; i++){
            if (i < 10){
                orderedBreakfast.add(FoodType.values()[breakfastIndices[i]]);
                orderedLunch.add(FoodType.values()[lunchIndices[i] + 10]);
            }
            orderedDinner.add(FoodType.values()[dinnerIndices[i] + 20]);
        }

        covetedFoods.put(MealType.BREAKFAST, orderedBreakfast);
        covetedFoods.put(MealType.LUNCH, orderedLunch);
        covetedFoods.put(MealType.DINNER, orderedDinner);

        return covetedFoods;
    }

    // Nuneke
    private void recalcSatisfaction(HashMap<MemberName, FoodType> assignedMeal, Integer flagDigit, HashMap<MemberName, Double> familySatisfaction,
                                    HashMap<MemberName, List<Double>> breakfastArray, HashMap<MemberName, List<Double>> lunchArray, 
                                    HashMap<MemberName, List<Double>> dinnerArray){
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
    private List<MemberName> getFamilyMembers(HashMap<MemberName, Double> familySatisfaction) {
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

    private static HashMap deepCopyMeals(HashMap<MemberName, List<Double>> original) {
        HashMap<MemberName, List<Double>> copy = new HashMap<>();
        for (Map.Entry<MemberName, List<Double>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<Double>(entry.getValue()));
        }
        return copy;
    }

    private static HashMap deepCopyFrequency(HashMap<MemberName, List<Integer>> original) {
        HashMap<MemberName, List<Integer>> copy = new HashMap<>();
        for (Map.Entry<MemberName, List<Integer>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<Integer>(entry.getValue()));
        }
        return copy;
    }

    private static HashMap deepCopySatisfaction(HashMap<MemberName, Double> original) {
        HashMap<MemberName, Double> copy = new HashMap<>();
        for (Map.Entry<MemberName, Double> entry : original.entrySet()) {
            copy.put(entry.getKey(), Double.valueOf(entry.getValue()));
        }
        return copy;
    }

}

/*  Makefile info:
gui:
    java -cp .:menu/org.json.jar menu.sim.Simulator --team g3 -m nunekeConfig.dat -C 1000000 -p 4 -w 52 -s 42 -l log.txt --gui -c -f 120 --export meals.csv planners.csv pantries.csv satisfactions.csv
family:
    java -cp .:menu/org.json.jar menu.sim.Simulator --team g3 -m family.dat -C 84 -p 4 -w 52 -s 42 -l log.txt --gui -c -f 120 --export meals_family.csv planners_family.csv pantries_family.csv satisfactions_family.csv
sharon:
    java -cp .:menu/org.json.jar menu.sim.Simulator --team g3 -m sharonConfig.dat -C 105 -p 5 -w 52 -s 42 -l log.txt --gui -c -f 120 --export meals_sharon.csv planners_sharon.csv pantries_sharon.csv satisfactions_sharon.csv
*/