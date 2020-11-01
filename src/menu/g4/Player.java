package menu.g4; // TODO modify the package name to reflect your team

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;


public class Player extends menu.sim.Player {

    Food food;
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

        for (FamilyMember member : familyMembers) {
            TreeMap<Double, List<FoodType>> orderedBreakfastFoods = new TreeMap<>();
            TreeMap<Double, List<FoodType>> orderedLunchFoods = new TreeMap<>();
            Map<FoodType, Double> foods = member.getFoodPreferenceMap();
            System.out.println(foods);
            for (Map.Entry<FoodType,Double> f : foods.entrySet()) {
                FoodType foodType = f.getKey();
                Double reward = f.getValue();
                if (food.isBreakfastType(foodType)) {
                    if (orderedBreakfastFoods.containsKey(-reward)) {
                        List<FoodType> temp = orderedBreakfastFoods.get(-reward);
                        temp.add(foodType);
                        orderedBreakfastFoods.put(-reward, temp);
                    }
                    else {
                        List<FoodType> temp = new ArrayList<>();
                        temp.add(foodType);
                        orderedBreakfastFoods.put(-reward, temp);
                    }
                }
                else if (food.isLunchType(foodType)) {
                    if (orderedLunchFoods.containsKey(-reward)) {
                        List<FoodType> temp = orderedLunchFoods.get(-reward);
                        temp.add(foodType);
                        orderedLunchFoods.put(-reward, temp);
                    }
                    else{
                        List<FoodType> temp = new ArrayList<>();
                        temp.add(foodType);
                        orderedLunchFoods.put(-reward, temp);
                    }
                }
                else {
                    // insert dinner here
                }
            }
            System.out.println(orderedBreakfastFoods);
            System.out.println(orderedLunchFoods);
            System.out.println("~~~");
            System.out.println(getTopNFoods(orderedBreakfastFoods, 3));
            System.out.println(getOptimalCycle(orderedLunchFoods));
            System.out.println("~~~~~~~~~~~~~~~~~~~~~");
        }

        return null; // TODO modify the return statement to return your shopping list
    }

    public List<FoodType> getTopNFoods(TreeMap<Double, List<FoodType>> foods, Integer n) {
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

    public List<FoodType> getOptimalCycle(TreeMap<Double, List<FoodType>> foods) {
        List<FoodType> cycle = new ArrayList<>();
        int d = 0;
        double greatestValue = 0;
        outerloop:
        for (Map.Entry<Double, List<FoodType>> entry : foods.entrySet()) {
            if (d == 0)
                greatestValue = -entry.getKey();
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

    public Planner planBreakfast(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

        return null;
    }
}