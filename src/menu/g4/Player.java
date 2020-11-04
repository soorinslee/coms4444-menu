package menu.g4; // TODO modify the package name to reflect your team

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
import menu.sim.MemberName;


public class Player extends menu.sim.Player {

    Food food;
    Map<MemberName, Map<String, List<FoodType>>> allMemberFoods;

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
        this.allMemberFoods = new HashMap<>();
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
        for (FamilyMember member : familyMembers)
            System.out.println(member);

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

        // Get top N breakfast/lunch foods
        for (FamilyMember member : familyMembers) {
            TreeMap<Double, List<FoodType>> orderedBreakfastFoods = new TreeMap<>();
            TreeMap<Double, List<FoodType>> orderedLunchFoods = new TreeMap<>();
            TreeMap<Double, List<FoodType>> orderedDinnerFoods = new TreeMap<>();
            Map<FoodType, Double> foods = member.getFoodPreferenceMap();
            Map<String, List<FoodType>> memberFoods = new HashMap<>();

            // System.out.println(foods);

            for (Map.Entry<FoodType,Double> f : foods.entrySet()) {
                FoodType foodType = f.getKey();
                Double reward = f.getValue();
                if (food.isBreakfastType(foodType)) {
                    addFoods(orderedBreakfastFoods, reward, foodType);
                }
                else if (food.isLunchType(foodType)) {
                    addFoods(orderedLunchFoods, reward, foodType);
                }
                else {
                    addFoods(orderedDinnerFoods, reward, foodType);
                }
            }
            // System.out.println(orderedBreakfastFoods);
            // System.out.println(orderedLunchFoods);
            // System.out.println("~~~");
            System.out.println(getTopNFoods(orderedBreakfastFoods, 3));
            System.out.println(getOptimalCycle(orderedLunchFoods));
            System.out.println("~~~~~~~~~~~~~~~~~~~~~");

            List<FoodType> topBreakfastFoods = getTopNFoods(orderedBreakfastFoods, 3);
            memberFoods.put("breakfast", topBreakfastFoods);

            // Add 7 breakfast foods per family member
            // Add four of first choices, two of second choice, one of third choice
            for(int i = 0; i < 4; i++) {
                shoppingList.addToOrder(topBreakfastFoods.get(0));
            }
            for(int i = 0; i < 2; i++) {
                shoppingList.addToOrder(topBreakfastFoods.get(1));
            }
            for(int i = 0; i < 1; i++) {
                shoppingList.addToOrder(topBreakfastFoods.get(2));
            }

            List<FoodType> optimalLunchCycle = getOptimalCycle(orderedLunchFoods);
            memberFoods.put("lunch", optimalLunchCycle);

            // Add 7 lunch foods per family member according to optimal cycle
            // Three of one type, two of another, two of another
            for(int i = 0; i < 3; i++) {
                shoppingList.addToOrder(optimalLunchCycle.get(0));
            }
            for(int i = 0; i < 2; i++) {
                shoppingList.addToOrder(optimalLunchCycle.get(1));
            }
            for(int i = 0; i < 2; i++) {
                shoppingList.addToOrder(optimalLunchCycle.get(2));
            }
    
            // TODO: Figure out dinner
            List<FoodType> topDinnerFoods = getTopNFoods(orderedDinnerFoods, 3);
            for(int i = 0; i < 4; i++) {
                shoppingList.addToOrder(topDinnerFoods.get(0));
            }
            for(int i = 0; i < 2; i++) {
                shoppingList.addToOrder(topDinnerFoods.get(1));
            }
            for(int i = 0; i < 1; i++) {
                shoppingList.addToOrder(topDinnerFoods.get(2));
            }
        }

        // Check constraints
        if(Player.hasValidShoppingList(shoppingList, numEmptySlots)) {
            return shoppingList;
        }
        else {
            return new ShoppingList();
        }
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
    private List<FoodType> getTopNFoods(TreeMap<Double, List<FoodType>> foods, Integer n) {
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

        return null; // TODO modify the return statement to return your planner
    }

}