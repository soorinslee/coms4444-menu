package menu.g1;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;


public class Player extends menu.sim.Player {

    /**
     * Player constructor
     *
     * @param weeks             number of weeks
     * @param numFamilyMembers  number of family members
     * @param capacity			pantry capacity
     * @param seed        		random seed
     * @param simPrinter   		simulation printer
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
    public ShoppingList stockPantry(Integer week,
    								Integer numEmptySlots,
    								List<FamilyMember> familyMembers,
    								Pantry pantry,
    								MealHistory mealHistory) {
    	
    	int numBreakfastFoods = random.nextInt(numEmptySlots + 1);
    	int numLunchFoods = random.nextInt(numEmptySlots - numBreakfastFoods + 1);
    	int numDinnerFoods = numEmptySlots - numBreakfastFoods - numLunchFoods;
    	
    	ShoppingList shoppingList = new ShoppingList();
    	shoppingList.addLimit(MealType.BREAKFAST, numBreakfastFoods);
    	shoppingList.addLimit(MealType.LUNCH, numLunchFoods);
    	shoppingList.addLimit(MealType.DINNER, numDinnerFoods);
    	
    	List<FoodType> breakfastFoods = Food.getFoodTypes(MealType.BREAKFAST);
    	List<FoodType> lunchFoods = Food.getFoodTypes(MealType.LUNCH);
    	List<FoodType> dinnerFoods = Food.getFoodTypes(MealType.DINNER);
    	
    	for(int i = 0; i < 2 * capacity; i++)
    		shoppingList.addToOrder(breakfastFoods.get(random.nextInt(breakfastFoods.size())));
    	for(int i = 0; i < 2 * capacity; i++)
    		shoppingList.addToOrder(lunchFoods.get(random.nextInt(lunchFoods.size())));
    	for(int i = 0; i < 2 * capacity; i++)
    		shoppingList.addToOrder(dinnerFoods.get(random.nextInt(dinnerFoods.size())));
    	
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
    public Planner planMeals(Integer week,
    						 List<FamilyMember> familyMembers,
    						 Pantry pantry,
    						 MealHistory mealHistory) {
    	List<MemberName> memberNames = new ArrayList<>();
    	for(FamilyMember familyMember : familyMembers)
    		memberNames.add(familyMember.getName());

    	Pantry originalPantry = pantry.clone();
    	
    	Planner planner = new Planner(memberNames);
    	for(MemberName memberName : memberNames) {
    		for(Day day : Day.values()) {
    			FoodType maxAvailableBreakfastMeal = getMaximumAvailableMeal(pantry, MealType.BREAKFAST);
    			if(pantry.getNumAvailableMeals(maxAvailableBreakfastMeal) > 0) {
        	    	planner.addMeal(day, memberName, MealType.BREAKFAST, maxAvailableBreakfastMeal);
        	    	pantry.removeMealFromInventory(maxAvailableBreakfastMeal);    				
    			}
    			FoodType maxAvailableLunchMeal = getMaximumAvailableMeal(pantry, MealType.LUNCH);
    			if(pantry.getNumAvailableMeals(maxAvailableLunchMeal) > 0) {
        	    	planner.addMeal(day, memberName, MealType.LUNCH, maxAvailableLunchMeal);
        	    	pantry.removeMealFromInventory(maxAvailableLunchMeal);    				
    			}
    		}
    	}
    	for(Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal = getMaximumAvailableMeal(pantry, MealType.DINNER);
			Integer numDinners = Math.min(pantry.getNumAvailableMeals(maxAvailableDinnerMeal), familyMembers.size());
			for(int i = 0; i < numDinners; i++) {
				MemberName memberName = memberNames.get(i);
    	    	planner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
		    	pantry.removeMealFromInventory(maxAvailableDinnerMeal);
			}
    	}

    	if(Player.hasValidPlanner(planner, originalPantry))
    		return planner;
    	return new Planner();
    }
    
    private FoodType getMaximumAvailableMeal(Pantry pantry, MealType mealType) {
    	FoodType maximumAvailableMeal = null;
    	int maxAvailableMeals = -1;
    	for(FoodType foodType : Food.getFoodTypes(mealType)) {
    		int numAvailableMeals = pantry.getNumAvailableMeals(foodType);
    		if(numAvailableMeals > maxAvailableMeals) {
    			maxAvailableMeals = numAvailableMeals;
    			maximumAvailableMeal = foodType;
    		}
    	}
    	return maximumAvailableMeal;
    }
}
