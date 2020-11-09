package menu.g4; // TODO modify the package name to reflect your team

import java.util.*;
import java.util.stream.Collectors;
import java.util.Map.Entry;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
import menu.sim.MemberName;


public class Player extends menu.sim.Player {

    Food food;
    Map<MemberName, List<FoodType>> allMemberBreakfastSorted;
    Map<MemberName, TreeMap<Double, List<FoodType>>> allMemberLunch;
    Map<MemberName, List<FoodType>> allMemberLunchSorted;
    List<FoodType> allMemberDinnerSorted;
    HashMap<FoodType, Double> allMemberFoodToRewards; 
    TreeMap<Double, FoodType> allMemberRewardToFood;


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
        this.food = new Food();
        this.allMemberBreakfastSorted = new HashMap<>();
        this.allMemberLunch = new HashMap<>();
        this.allMemberLunchSorted = new HashMap<>();
        this.allMemberDinnerSorted = new ArrayList<>();
        this.allMemberFoodToRewards = new HashMap<FoodType, Double>();
        this.allMemberRewardToFood = new TreeMap<Double, FoodType>();
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

        // 1/4 capacity to breakfast and lunch and rest to dinner
        int numBreakfastFoods = Math.round(super.capacity/4) - numInPantry(MealType.BREAKFAST, pantry);
    	int numLunchFoods = Math.round(super.capacity/4) - numInPantry(MealType.LUNCH, pantry);
    	int numDinnerFoods = numEmptySlots - numBreakfastFoods - numLunchFoods;

        ShoppingList shoppingList = new ShoppingList();
    	shoppingList.addLimit(MealType.BREAKFAST, numBreakfastFoods);
    	shoppingList.addLimit(MealType.LUNCH, numLunchFoods);
        shoppingList.addLimit(MealType.DINNER, numDinnerFoods);

        simPrinter.println();
        simPrinter.println("~~~~~~~~~~~~~~~~~~ WEEK " + week + " ~~~~~~~~~~~~~~~~~~");
        // Get top breakfast/lunch/dinner foods if first week
        if (week == 1) {
            for (FamilyMember member : familyMembers) {
                TreeMap<Double, List<FoodType>> orderedBreakfastFoods = new TreeMap<>();
                TreeMap<Double, List<FoodType>> orderedLunchFoods = new TreeMap<>();
                Map<FoodType, Double> foods = member.getFoodPreferenceMap();

                for (Map.Entry<FoodType, Double> f : foods.entrySet()) {
                    FoodType foodType = f.getKey();
                    Double reward = f.getValue();
                    if (food.isBreakfastType(foodType)) {
                        addFoods(orderedBreakfastFoods, reward, foodType);
                    } else if (food.isLunchType(foodType)) {
                        addFoods(orderedLunchFoods, reward, foodType);
                    }
                }

                List<FoodType> topBreakfastFoods = getTopNFoods(orderedBreakfastFoods, 3);
                this.allMemberBreakfastSorted.put(member.getName(), topBreakfastFoods);

                List<FoodType> optimalLunchCycle = getOptimalCycle(orderedLunchFoods);
                this.allMemberLunch.put(member.getName(), orderedLunchFoods);
                this.allMemberLunchSorted.put(member.getName(), optimalLunchCycle);
            }

            HashMap<FoodType, Double> foodToRewards = new HashMap<>();
            // handle dinner separately
            for (FamilyMember member : familyMembers) {
                Map<FoodType, Double> foods = member.getFoodPreferenceMap();
                for (Map.Entry<FoodType, Double> f: foods.entrySet()) {
                    FoodType fType = f.getKey();
                    Double r = f.getValue();
                    if (Food.isDinnerType(fType)) {
                        if (foodToRewards.containsKey(fType)) {
                            foodToRewards.put(fType, foodToRewards.get(fType) + r);
                        }
                        else {
                            foodToRewards.put(fType, r);
                        }
                    }
                }
            }

            TreeMap<Double, FoodType> rewardToFood = new TreeMap<>();
            for (Map.Entry<FoodType, Double> f: foodToRewards.entrySet()) {
                FoodType fType = f.getKey();
                Double r = -1*f.getValue();
                rewardToFood.put(r, fType);
            }

            this.allMemberRewardToFood = rewardToFood;
            this.allMemberFoodToRewards = foodToRewards;


            this.allMemberDinnerSorted = getOptimalDinnerCycle(rewardToFood);

            simPrinter.println("Top 3 breakfast items, optimal lunch cycle, and dinner determined.");
        }
        simPrinter.println("Moving ahead to adding to pantry.");

        // store top breakfast foods in lists
        List<FoodType> firstBreakfast = new ArrayList<FoodType>();
        List<FoodType> secondBreakfast = new ArrayList<FoodType>();


        // Adding to pantry
        Map<MemberName, Double> averageSatisfactionMap = mealHistory.getAllAverageSatisfactions().get(week-1);
        List<FamilyMember> sortedFamilyMembers = familyMembers;
        // Sort from least average satisfaction to most
        if (week > 1)
            sortedFamilyMembers = sortByValue(averageSatisfactionMap, true, familyMembers);
        for (FamilyMember member : sortedFamilyMembers) {
            MemberName name = member.getName();
            
            // first and second choice breakfast foods to list
            firstBreakfast.add(this.allMemberBreakfastSorted.get(name).get(0));
            secondBreakfast.add(this.allMemberBreakfastSorted.get(name).get(1));

            // Add 14 lunch foods per family member according to optimal cycle
            int ind = 0;
            for (int count = 0; count < 14; count++) {
                if (ind == this.allMemberLunchSorted.get(name).size() || ind == 7)
                    ind = 0;
                shoppingList.addToOrder(this.allMemberLunchSorted.get(name).get(ind));
                ind++;
            }

            simPrinter.println("Iterating through " + name + "'s order and adding to pantry.");
            // simPrinter.println(shoppingList.getFullOrderMap());
        }

        // Add breakfast to shopping list
        // 10 of first choice
        // 5 of second choice
        for (FoodType firstFood : firstBreakfast) {
            for (int count = 0; count < 15; count++) {
                shoppingList.addToOrder(firstFood);
            }
        }
        for (FoodType secondFood : secondBreakfast) {
            for (int count = 0; count < 5; count++) {
                shoppingList.addToOrder(secondFood);
            }
        }


        // get the available from pantry
        simPrinter.println("Hello");

        HashMap<FoodType, Integer> availableMap = new HashMap<FoodType, Integer>();
        List<FoodType> availableFoods = pantry.getAvailableFoodTypes(MealType.DINNER);
        for (FoodType availableFood: availableFoods) {
            if (food.isDinnerType(availableFood) && pantry.getNumAvailableMeals(availableFood) > familyMembers.size()) {
                availableMap.put(availableFood, pantry.getNumAvailableMeals(availableFood));
            }
        }

        simPrinter.println("Hello");

        List<FoodType> dinnerCycle = getOptimalDinnerCycleWithConstraints(this.allMemberRewardToFood, this.allMemberFoodToRewards, availableMap, 3);
        simPrinter.println("Hello");
        simPrinter.println(dinnerCycle);
        Pantry clone = pantry.clone();
        for (FoodType foodType: dinnerCycle) {
            for (FamilyMember member : familyMembers) {
                if (clone.containsMeal(foodType)) {
                    clone.removeMealFromInventory(foodType);
                    continue;
                }
                else {
                    shoppingList.addToOrder(foodType);
                }
            }
        }



        // simPrinter.println(optimalDinnerCycle);
        // TODO: Add 7 dinner cycle

        // Check constraints
        if(Player.hasValidShoppingList(shoppingList, numEmptySlots)) {
            simPrinter.println("VALID!");
            simPrinter.println(this.allMemberDinnerSorted);
            simPrinter.println(shoppingList.getLimit(MealType.DINNER));
            simPrinter.println(numEmptySlots);
            simPrinter.println(numDinnerFoods);
            // Add 7
        }

        // Check constraints
        if(hasShoppingList(shoppingList, numEmptySlots)) {
            simPrinter.println("Valid");
            return shoppingList;
        }
        else {
            return new ShoppingList();
        }
    }

    private int numInPantry(MealType type, Pantry pantry) {
        int num = 0;
        for (FoodType food : pantry.getMealsMap().get(type).keySet()) {
            num += pantry.getMealsMap().get(type).get(food);
        }

        return num;
    }

    private boolean hasShoppingList(ShoppingList shoppingList, Integer numEmptySlots) {
    	Map<MealType, Integer> allLimitsMap = shoppingList.getAllLimitsMap();

    	int totalLimits = 0;
    	for(MealType mealType : allLimitsMap.keySet())
            totalLimits += allLimitsMap.get(mealType);
        simPrinter.println("totalLimits");
        simPrinter.println(totalLimits);
        simPrinter.println(numEmptySlots);
    	return totalLimits <= numEmptySlots;
    }

    /**
     * Add foods to corresponding TreeMaps
     *
     * @param map            ordered map of -value : FoodTypes for a family member
     * @param reward         reward of food
     * @param foodType       type of food
     *
     */
    private void addFoods(TreeMap<Double, List<FoodType>> map, Double reward, FoodType foodType) {
        if (map.containsKey(-reward)) {
            List<FoodType> temp = map.get(-reward);
            temp.add(foodType);
            map.put(-reward, temp);   // using -reward since TreeMap orders smallest->largest
        }
        else {
            List<FoodType> temp = new ArrayList<>();
            temp.add(foodType);
            map.put(-reward, temp);
        }
    }

    /**
     * Get top foods given ordered TreeMap of family member
     *
     * @param foods          ordered map of -value : FoodTypes for a family member
     * @param n              number of foods to return
     * @return               list of top n rewarding FoodTypes for a family member
     *
     */
    private List<FoodType>  getTopNFoods(TreeMap<Double, List<FoodType>> foods, Integer n) {
        List<FoodType> topNFoods = new ArrayList<>();
        int i = 0;
        outerloop:
        for (Map.Entry<Double, List<FoodType>> entry : foods.entrySet()) {
            for (FoodType food : entry.getValue()) {
                if (i == n)
                    break outerloop;
                topNFoods.add(food);
                i++;
            }
        }
        return topNFoods;
    }

    private List<FamilyMember> getLeastSatisfiedMembers(Integer week, MealHistory mealHistory, List<FamilyMember> familyMembers, int numMembers) {
        // get average satisfactions

        Map<MemberName, Double> satisfactions = mealHistory.getAllAverageSatisfactions().get(week-1);
        List<Map.Entry<MemberName, Double> > list = 
               new LinkedList<Map.Entry<MemberName, Double>>(satisfactions.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<MemberName, Double> >() { 
            public int compare(Map.Entry<MemberName, Double> o1,  
                               Map.Entry<MemberName, Double> o2) 
            { 
                return (o2.getValue()).compareTo(o1.getValue()); 
            } 
        }); 

        Set<MemberName> leastSatisfiedMemberSet = new HashSet<MemberName>();
        for (int i=0; i<numMembers && i < familyMembers.size(); i++) {
            leastSatisfiedMemberSet.add(list.get(i).getKey());
        }
        
        List<FamilyMember> leastSatisfiedMembers = new LinkedList<FamilyMember>();
        for (FamilyMember member: familyMembers) { 
            if (leastSatisfiedMemberSet.contains(member.getName())) {
                leastSatisfiedMembers.add(member);
            }
        }
        return leastSatisfiedMembers;
    }

    private List<FoodType> getOptimalDinnerCycleWithConstraints(TreeMap<Double, FoodType> rewardToFood, HashMap<FoodType, Double> foodToReward, Map<FoodType, Integer> availableFoods, int desiredCycleLength) {
        List<FoodType> cycle = new ArrayList<>();
        int d = 0;
        double greatestValue = -rewardToFood.firstKey();

        // get relaxation constraint 
        List<FoodType> optimalCycle = this.allMemberDinnerSorted;
        int relaxation = 0;
        if (optimalCycle.size() < desiredCycleLength) {
            relaxation = desiredCycleLength - optimalCycle.size(); 
        }


        // build availability cycle
        Map<FoodType, Integer> availableFoodsCopy = new HashMap<>(availableFoods);
        HashMap<FoodType, Double> availableFoodsToReward = new HashMap<FoodType, Double>();
        for (Map.Entry<FoodType, Integer> entry: availableFoodsCopy.entrySet()) {
            availableFoodsToReward.put(entry.getKey(), foodToReward.get(entry.getKey()));
        }


        //invert the map 
        TreeMap<Double, FoodType> availableRewardToFood = new TreeMap<Double, FoodType>(); 
        for (Map.Entry<FoodType, Double> entry: availableFoodsToReward.entrySet()) {
            availableRewardToFood.put(entry.getValue(), entry.getKey());
        }


        if (!availableRewardToFood.isEmpty()) {
            List<FoodType> availableCycle = new ArrayList<>();
            int d2 = 0;
            double greatestValue2 = availableRewardToFood.firstKey();
            for (Map.Entry<Double, FoodType> entry : availableRewardToFood.entrySet()) {
                FoodType food = entry.getValue();
                if (d2 > 0 && -entry.getKey() < (d2*greatestValue2/(d2+1))) {
                    break;
                }
                availableCycle.add(food);
                d2++;
            }

            for (FoodType meal: availableCycle) {
                availableFoodsCopy.put(meal, availableFoodsCopy.get(meal) - 5);
                cycle.add(meal);
            }
        }
        


        if (cycle.size() == 7) {
            return cycle;
        }
        
        // take a gamble
        while (cycle.size() != 7) {
            simPrinter.print("not working");
            for (Map.Entry<Double, FoodType> entry : rewardToFood.entrySet()) {
                if (cycle.size() == 7) {
                    return cycle;
                }
                FoodType food = entry.getValue();
                if (d > 0 && -entry.getKey() < (d*greatestValue/(d+1))) {
                    if (relaxation > 0) {
                        cycle.add(food);
                        relaxation -= 1;
                    }
                    else {
                        break;
                    }
                }
                else {
                    cycle.add(food);
                }
                d++;
            }
        }
 
        return cycle;
    }


    /**
     * Get optimal cycle of foods to repeat given ordered TreeMap of family member
     * For stockPantry
     *
     * @param foods          ordered map of -value : FoodTypes for a family member
     * @return               list of foods to repeat in a cycle
     *
     */
    private List<FoodType> getOptimalCycle(TreeMap<Double, List<FoodType>> foods) {
        List<FoodType> cycle = new ArrayList<>();
        int d = 0;
        double greatestValue = -foods.firstKey();
        outerloop:
        for (Map.Entry<Double, List<FoodType>> entry : foods.entrySet()) {
            for (FoodType food : entry.getValue()) {
                if (d > 0 && -entry.getKey()<(d*greatestValue/(d+1)))
                    break outerloop;
                cycle.add(food);
                d++;
            }
        }
        return cycle;
    }

    /**
     * Get optimal list of foods to plan given ordered TreeMap of family member and available foods
     * For planMeals
     *
     * @param foods          ordered map of -value : FoodTypes for a family member
     * @param availableFoods map of FoodType : number of food available in pantry
     * @return               list of foods eat in order
     *
     */
    private List<FoodType> getOptimalCycle(TreeMap<Double, List<FoodType>> foods, Map<FoodType, Integer> availableFoods) {
        List<FoodType> cycle = new ArrayList<>();
        Map<FoodType, Integer> availableFoodsCopy = new HashMap<>(availableFoods);
        int d = 0;
        boolean stayInLoop = true;
        outerloop:
        while (stayInLoop) {
            stayInLoop = false;
            double greatestValue = 0;
            List<FoodType> temp = new ArrayList<>();
            innerloop:
            for (Map.Entry<Double, List<FoodType>> entry : foods.entrySet()) {
                for (FoodType food : entry.getValue()) {
                    // check if there is this FoodType available in the pantry
                    if (availableFoodsCopy.containsKey(food) && availableFoodsCopy.get(food) > 0) {
                        // check if all 7 days are already filled; if yes, break out of all loops and return
                        if (cycle.size() + temp.size() >= 7) {
                            cycle.addAll(temp);
                            break outerloop;
                        }
                        // check if the current food value is greater than the best food with penalty; if yes, restart the loop
                        if (d > 0 && -entry.getKey()<(d*greatestValue/(d+1))) {
                            stayInLoop = true;
                            break innerloop;
                        }
                        // check if current food value is 0; if yes, restart loop without adding food
                        if (entry.getKey() == 0.0) {
                            break innerloop;
                        }
                        // update pantry copy with one less food
                        availableFoodsCopy.put(food, availableFoodsCopy.get(food) - 1);
                        // get greatest value of available food for NEXT loop
                        if (availableFoodsCopy.get(food) > 0 && greatestValue == 0)
                            greatestValue = -entry.getKey();
                        temp.add(food);
                        d++;
                    }
                }
            }
            for (FoodType food : availableFoodsCopy.keySet()) {
                int count = availableFoodsCopy.get(food);
                boolean notZeroValuedFood = foods.containsKey(0.0) && !foods.get(0.0).contains(food);
                if (!foods.containsKey(0.0) && count > 0) {     // if there is still food left in pantry and no foods,
                    stayInLoop = true;                          // with zero value, stay in loop
                    break;
                }
                else if (notZeroValuedFood && count > 0) {     // if there are foods with zero value but there exist
                    stayInLoop = true;                         // foods with value > 0 and count > 0, stay in loop
                    break;
                }
            }
            cycle.addAll(temp);         // add the food to the overall cycle
        }

        return redistribute(cycle);
    }

    /**
     * Redistribute list of foods to make repeated foods more spaced out
     * For planMeals
     *
     * @param foods          list of foods to be spaced out
     * @return               list of foods eat in order
     *
     */
    private List<FoodType> redistribute(List<FoodType> foods) {
        List<FoodType> result = new ArrayList<>();
        Map<FoodType, Integer> foodMap = new HashMap<>();
        for (FoodType food : foods) {
            if (!foodMap.containsKey(food))
                foodMap.put(food, 1);
            else
                foodMap.put(food, foodMap.get(food)+1);
        }
        Map<FoodType, Integer> foodMapSorted = sortByValue(foodMap, false);
        boolean hasFoodLeft = true;
        while (hasFoodLeft) {
            hasFoodLeft = false;
            for (Map.Entry<FoodType, Integer> entry : foodMapSorted.entrySet()) {
                if (entry.getValue() > 0) {
                    result.add(entry.getKey());
                    foodMapSorted.put(entry.getKey(), foodMapSorted.get(entry.getKey())-1);
                }
            }
            for (int i : foodMapSorted.values()) {
                if (i > 0) {
                    hasFoodLeft = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Sort map by value
     * For planMeals
     *
     * @param unsortedMap    map of unsorted foods : count of food
     * @param order          true -> ascending, false -> descending
     * @return               map of sorted foods : count of food
     *
     */
    private static Map<FoodType, Integer> sortByValue(Map<FoodType, Integer> unsortedMap, final boolean order)
    {
        List<Entry<FoodType, Integer>> list = new LinkedList<>(unsortedMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * Sort map by value
     * For planMeals and stockPantry
     *
     * @param unsortedMap    map of unsorted member : average satisfaction
     * @param familyMembers  list of unsorted family members
     * @param order          true -> ascending, false -> descending
     * @return               list of sorted family members
     *
     */
    private static List<FamilyMember> sortByValue(Map<MemberName, Double> unsortedMap, final boolean order,
                                                  List<FamilyMember> familyMembers)
    {
        List<Entry<MemberName, Double>> list = new LinkedList<>(unsortedMap.entrySet());
        List<FamilyMember> sortedFamilyMembers = new ArrayList<>();

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));

        Map<MemberName, Double> sortedMap = list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));
        for (Map.Entry<MemberName, Double> entry : sortedMap.entrySet())
            for (FamilyMember member : familyMembers)
                if (entry.getKey().equals(member.getName()))
                    sortedFamilyMembers.add(member);

        return sortedFamilyMembers;
    }


    private List<FoodType> getOptimalDinnerCycle(TreeMap<Double, FoodType> rewardToFood) {
        List<FoodType> cycle = new ArrayList<>();
        int d = 0;
        double greatestValue = -rewardToFood.firstKey();
        for (Map.Entry<Double, FoodType> entry : rewardToFood.entrySet()) {
            FoodType food = entry.getValue();
            if (d > 0 && -entry.getKey() < (d*greatestValue/(d+1))) {
                break;
            }
            cycle.add(food);
            d++;
        }
        return cycle;
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
        Planner planner = new Planner();
        Pantry pantryCopy = pantry.clone();

        Map<MemberName, Double> averageSatisfactionMap = mealHistory.getAllAverageSatisfactions().get(week-1);
        List<FamilyMember> sortedFamilyMembers = familyMembers;
        // Sort from least average satisfaction to most
        if (week > 1)
            sortedFamilyMembers = sortByValue(averageSatisfactionMap, true, familyMembers);

        HashMap<FoodType, Integer> availableMap = new HashMap<FoodType, Integer>();
        List<FoodType> availableFoods = pantry.getAvailableFoodTypes(MealType.DINNER);
        for (FoodType availableFood: availableFoods) {
            if (food.isDinnerType(availableFood) && pantry.getNumAvailableMeals(availableFood) > familyMembers.size()) {
                availableMap.put(availableFood, pantry.getNumAvailableMeals(availableFood));
            }
        }

        simPrinter.println("Hello");

        List<FoodType> dinnerCycle = getOptimalDinnerCycleWithConstraints(this.allMemberRewardToFood, this.allMemberFoodToRewards, availableMap, 3);
        for (FamilyMember member : sortedFamilyMembers) {
            MemberName name = member.getName();
            int l = 0;
            int d = 0;
            List<FoodType> lunches = getOptimalCycle(this.allMemberLunch.get(name), pantryCopy.getMealsMap().get(MealType.LUNCH));
            for (Day day : Day.values()) {

                // Breakfast
                int b = 0;
                while (b < 3) {
                    FoodType breakfast = this.allMemberBreakfastSorted.get(name).get(b);
                    if (pantryCopy.containsMeal(breakfast)) {
                        planner.addMeal(day, name, MealType.BREAKFAST, breakfast);
                        pantryCopy.removeMealFromInventory(breakfast);
                        break;
                    }
                    b++;
                }

                // Lunch
                if (l < lunches.size()) {
                    FoodType lunch = lunches.get(l);
                    planner.addMeal(day, name, MealType.LUNCH, lunch);
                    pantryCopy.removeMealFromInventory(lunch);
                    l++;
                }

                simPrinter.println("Day: " + day);

                FoodType dinner = dinnerCycle.get(d);
                if (pantryCopy.containsMeal(dinner)) {
                    planner.addMeal(day, name, MealType.DINNER, dinner);
                    pantryCopy.removeMealFromInventory(dinner);
                }
                d++;
                    //    break;
                    //}

                // Dinner
                //FoodType dinner = this.allMemberDinner.get(d);
                //if (pantryCopy.containsMeal(dinner)) {
                //    planner.addMeal(day, name, MealType.DINNER, dinner);
                //    pantryCopy.removeMealFromInventory(dinner);
                //    break;
                //}
                //d++;
            }
        }
        return planner;
    }

    // Testing - run with "java menu/g4/Player.java" in src folder
    public static void main(String[] args) {
        System.out.println("Testing...");
        SimPrinter printer = new SimPrinter(true);
        Player player = new Player(1, 1, 1, 1, printer);

        // TEST ONE - should print "[LUNCH5, LUNCH2, LUNCH6, LUNCH3, LUNCH1, LUNCH5, LUNCH2]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST ONE - should print [LUNCH5, LUNCH2, LUNCH6, LUNCH3, LUNCH1, LUNCH5, LUNCH2]");
        List<FoodType> favorite = new ArrayList<>();
        favorite.add(FoodType.LUNCH1);
        favorite.add(FoodType.LUNCH2);
        List<FoodType> favorite2 = new ArrayList<>();
        favorite2.add(FoodType.LUNCH3);
        List<FoodType> favorite3 = new ArrayList<>();
        favorite3.add(FoodType.LUNCH4);
        favorite3.add(FoodType.LUNCH5);
        List<FoodType> favorite4 = new ArrayList<>();
        favorite4.add(FoodType.LUNCH6);
        favorite4.add(FoodType.LUNCH7);
        favorite4.add(FoodType.LUNCH8);
        List<FoodType> favorite5 = new ArrayList<>();
        favorite5.add(FoodType.LUNCH9);
        favorite5.add(FoodType.LUNCH10);

        TreeMap<Double, List<FoodType>> foods = new TreeMap<>();
        foods.put(-1.0, favorite);
        foods.put(-0.9, favorite2);
        foods.put(-0.8, favorite3);
        foods.put(-0.4, favorite4);
        foods.put(-0.3, favorite5);

        Map<FoodType, Integer> availableFoods = new HashMap<>();
        availableFoods.put(FoodType.LUNCH1, 1); // in pantry, there are one LUNCH1
        availableFoods.put(FoodType.LUNCH2, 2); // in pantry, there is two LUNCH2
        availableFoods.put(FoodType.LUNCH3, 1); // in pantry, there is one LUNCH3
        availableFoods.put(FoodType.LUNCH5, 2); // in pantry, there are two LUNCH5
        availableFoods.put(FoodType.LUNCH6, 3); // in pantry, there are three LUNCH6

        List<FoodType> cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        // TEST TWO - should print "[LUNCH1, LUNCH5, LUNCH6, LUNCH2, LUNCH1, LUNCH5, LUNCH1]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST TWO - should print [LUNCH1, LUNCH5, LUNCH6, LUNCH2, LUNCH1, LUNCH5, LUNCH1]");
        favorite = new ArrayList<>();
        favorite.add(FoodType.LUNCH1);
        favorite2 = new ArrayList<>();
        favorite2.add(FoodType.LUNCH2);
        favorite3 = new ArrayList<>();
        favorite3.add(FoodType.LUNCH3);
        favorite4 = new ArrayList<>();
        favorite4.add(FoodType.LUNCH4);
        favorite5 = new ArrayList<>();
        favorite5.add(FoodType.LUNCH5);
        favorite5.add(FoodType.LUNCH6);
        favorite5.add(FoodType.LUNCH7);
        favorite5.add(FoodType.LUNCH8);
        favorite5.add(FoodType.LUNCH9);
        favorite5.add(FoodType.LUNCH10);

        foods = new TreeMap<>();
        foods.put(-1.0, favorite);
        foods.put(-0.4, favorite2);
        foods.put(-0.3, favorite3);
        foods.put(-0.2, favorite4);
        foods.put(-0.1, favorite5);

        availableFoods = new HashMap<>();
        availableFoods.put(FoodType.LUNCH1, 3); // in pantry, there are three LUNCH1
        availableFoods.put(FoodType.LUNCH2, 1); // in pantry, there is one LUNCH2
        availableFoods.put(FoodType.LUNCH5, 2); // in pantry, there are two LUNCH5
        availableFoods.put(FoodType.LUNCH6, 3); // in pantry, there are three LUNCH6

        cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        // TEST THREE - should print "[]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST THREE - should print []");
        availableFoods = new HashMap<>();
        cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        // TEST FOUR - should print "[LUNCH1, LUNCH1, LUNCH1]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST FOUR - should print [LUNCH1, LUNCH1, LUNCH1]");
        availableFoods = new HashMap<>();
        availableFoods.put(FoodType.LUNCH1, 3); // in pantry, there are three LUNCH1
        cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        // TEST FIVE - should print "[LUNCH3, LUNCH2, LUNCH1]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST FIVE - should print [LUNCH3, LUNCH2, LUNCH1]");
        foods = new TreeMap<>();
        foods.put(-1.0, favorite);
        foods.put(-0.9, favorite2);
        foods.put(-0.8, favorite3);
        foods.put(-0.7, favorite4);
        foods.put(0.0, favorite5);
        availableFoods = new HashMap<>();
        availableFoods.put(FoodType.LUNCH1, 1);
        availableFoods.put(FoodType.LUNCH2, 1);
        availableFoods.put(FoodType.LUNCH3, 1);
        availableFoods.put(FoodType.LUNCH5, 1);
        availableFoods.put(FoodType.LUNCH6, 2);
        cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("DINNER TESTS");
        TreeMap<Double, FoodType> foodDinner = new TreeMap<>();
        HashMap<FoodType, Double> reverseFoods = new HashMap<FoodType, Double>();
        foodDinner.put(-1.0, FoodType.DINNER1);
        foodDinner.put(-0.9, FoodType.DINNER2);
        foodDinner.put(-0.8, FoodType.DINNER3);
        reverseFoods.put(FoodType.DINNER1, -1.0);
        reverseFoods.put(FoodType.DINNER2, -0.9);
        reverseFoods.put(FoodType.DINNER3, -0.8);
        availableFoods = new HashMap<>();
        availableFoods.put(FoodType.DINNER2, 3);
        availableFoods.put(FoodType.DINNER3, 2);
        cycle = player.getOptimalDinnerCycleWithConstraints(foodDinner, reverseFoods, availableFoods, 2);
        System.out.println(cycle);

    }

}