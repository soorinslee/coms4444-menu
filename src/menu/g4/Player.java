package menu.g4; // TODO modify the package name to reflect your team

import java.util.*;

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

        // A bit tight; might want to adjust in the future (espcially for lunch food)
        // Assumption: You use all breakfast and lunch foods in the pantry each week
        // There are extras for dinner
        int numBreakfastFoods = 7 * numFamilyMembers;
    	int numLunchFoods = 7 * numFamilyMembers;
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

            this.allMemberDinnerSorted = getOptimalDinnerCycle(rewardToFood);

            simPrinter.println("Top 3 breakfast items, optimal lunch cycle, and dinner determined.");
        }
        simPrinter.println("Moving ahead to adding to pantry.");

        // Adding to pantry
        for (FamilyMember member : familyMembers) {
            MemberName name = member.getName();
            // Add 7 breakfast foods per family member
            // Add four of first choices, two of second choice, one of third choice
            for(int i = 0; i < 4; i++) {
                shoppingList.addToOrder(this.allMemberBreakfastSorted.get(name).get(0));
            }
            for(int i = 0; i < 2; i++) {
                shoppingList.addToOrder(this.allMemberBreakfastSorted.get(name).get(1));
            }
            for(int i = 0; i < 1; i++) {
                shoppingList.addToOrder(this.allMemberBreakfastSorted.get(name).get(2));
            }

            // Add 7 lunch foods per family member according to optimal cycle
            int ind = 0;
            for (int count = 0; count < 7; count++) {
                if (ind == this.allMemberLunchSorted.get(name).size())
                    ind = 0;
                shoppingList.addToOrder(this.allMemberLunchSorted.get(name).get(ind));
                ind++;
            }

            simPrinter.println("Iterating through " + name + "'s order and adding to pantry.");
            // simPrinter.println(shoppingList.getFullOrderMap());
        }

        int count = 0;
        int cycleIndex = 0;
        for (int i=0; i<7; i++) {
            if (cycleIndex == this.allMemberDinnerSorted.size()) {
                cycleIndex = 0;
            }
            FoodType food = this.allMemberDinnerSorted.get(cycleIndex);
            cycleIndex += 1;
            for (FamilyMember member : familyMembers) {
                shoppingList.addToOrder(food);
                count += 1;
            }
        }

        // simPrinter.println(optimalDinnerCycle);
        // TODO: Add 7 dinner cycle

        // Check constraints
        if(Player.hasValidShoppingList(shoppingList, numEmptySlots)) {
            simPrinter.println("VALID!");
            simPrinter.println(this.allMemberDinnerSorted);
            simPrinter.println(count);
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
            for (int value : availableFoodsCopy.values()) {
                if (value > 0) {        // if there is still food left in pantry, stay in loop because not all seven
                    stayInLoop = true;  // days are filled yet, or else the outer loop would be broken out of
                    break;
                }
            }
            cycle.addAll(temp);         // add the food to the overall cycle
        }

        return cycle;
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
        for (FamilyMember member : familyMembers) {
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

        // TEST ONE - should print "[LUNCH1, LUNCH2, LUNCH3, LUNCH5, LUNCH1, LUNCH5, LUNCH6]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST ONE - should print [LUNCH1, LUNCH2, LUNCH3, LUNCH5, LUNCH1, LUNCH5, LUNCH6]");
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
        availableFoods.put(FoodType.LUNCH1, 2); // in pantry, there are two LUNCH1
        availableFoods.put(FoodType.LUNCH2, 1); // in pantry, there is one LUNCH2
        availableFoods.put(FoodType.LUNCH3, 1); // in pantry, there is one LUNCH3
        availableFoods.put(FoodType.LUNCH5, 2); // in pantry, there are two LUNCH5
        availableFoods.put(FoodType.LUNCH6, 3); // in pantry, there are three LUNCH6

        List<FoodType> cycle = player.getOptimalCycle(foods, availableFoods);
        System.out.println(cycle);

        // TEST TWO - should print "[LUNCH1, LUNCH2, LUNCH2, LUNCH5, LUNCH1, LUNCH5, LUNCH6]"
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("TEST TWO - should print [LUNCH1, LUNCH1, LUNCH1, LUNCH2, LUNCH5, LUNCH6, LUNCH5]");
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
    }

}