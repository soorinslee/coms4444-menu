package menu.sim;

import java.util.*;

import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;


public abstract class Player {

    public Integer weeks, numFamilyMembers, capacity, seed;
    public Random random;
    public SimPrinter simPrinter;
    
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
		this.weeks = weeks;
		this.numFamilyMembers = numFamilyMembers;
		this.capacity = capacity;
        this.seed = seed;
        this.random = new Random(seed);
        this.simPrinter = simPrinter;        
	}

    /**
     * Create shopping list of meals to stock pantry
     *
     * @param week           current week
     * @param numEmptySlots  number of empty slots left in the pantry
     * @param familyMembers  all family members
     * @param pantry         pantry inventory of remaining foods
     * @param shop           shop inventory of available foods to order
     * @param mealHistory    history of previous meal allocations
     * @return               shopping list of foods to order
     *
     */
    public abstract ShoppingList stockPantry(Integer week,
    										 Integer numEmptySlots,
    										 List<FamilyMember> familyMembers,
    										 Pantry pantry,
    										 MealHistory mealHistory);

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
    public abstract Planner planMeals(Integer week,
    								  List<FamilyMember> familyMembers,
    								  Pantry pantry,
    								  MealHistory mealHistory);
    
    /**
     * Check that shopping list is valid
     *
     * @param shoppingList   a shopping list
     * @param numEmptySlots  number of empty slots left in the pantry
     * @return               validity of the shopping list
     *
     */
    public static boolean hasValidShoppingList(ShoppingList shoppingList, Integer numEmptySlots) {
    	Map<MealType, Integer> allLimitsMap = shoppingList.getAllLimitsMap();

    	int totalLimits = 0;
    	for(MealType mealType : allLimitsMap.keySet())
    		totalLimits += allLimitsMap.get(mealType);
    	
    	return totalLimits <= numEmptySlots;
    }
    
    /**
     * Check that planner is valid
     *
     * @param planner  planner of assigned meals for the week
     * @param pantry   pantry inventory of remaining foods
     * @return         validity of the planner
     *
     */
    public static boolean hasValidPlanner(Planner planner, Pantry pantry) {
    	Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
    	
    	// Check for each day that the assigned dinner for all family members is the same
	    for(Day day : plan.keySet()) {
		    FoodType currentAssignedDinner = null;
		    
		    Map<MemberName, Map<MealType, FoodType>> dayPlan = plan.get(day);
	    	for(MemberName memberName : dayPlan.keySet()) {
	    		if(!dayPlan.get(memberName).containsKey(MealType.DINNER))
	    			continue;
	    		
	    		FoodType chosenMeal = dayPlan.get(memberName).get(MealType.DINNER);
	    		if(chosenMeal == null)
	    			continue;
	    		
	    		if(currentAssignedDinner == null) {
	    			currentAssignedDinner = chosenMeal;
	    			continue;
	    		}

	    		if(!currentAssignedDinner.equals(chosenMeal))
	    			return false;
	    	}
	    }
	    
	    // Check if the planner is compatible with what is available inside the pantry
	    Map<FoodType, Integer> plannedMealTally = new HashMap<>();
	    for(FoodType foodType : Food.getAllFoodTypes())
	    	plannedMealTally.put(foodType, 0);
	    for(Day day : plan.keySet()) {
	    	for(MemberName memberName : plan.get(day).keySet()) {
	    		for(MealType mealType : plan.get(day).get(memberName).keySet()) {
	    			FoodType foodType = plan.get(day).get(memberName).get(mealType);
	    			if(foodType == null)
	    				continue;
	    			plannedMealTally.put(foodType, plannedMealTally.get(foodType) + 1);
	    		}
	    	}
	    }
	    for(FoodType foodType : plannedMealTally.keySet()) {
	    	if(pantry.getNumAvailableMeals(foodType) < plannedMealTally.get(foodType))
	    		return false;
	    }
	    
	    
    	return true;
    }
}
