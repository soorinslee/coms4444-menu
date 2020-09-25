package sim;

import java.util.List;
import java.util.Random;


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
     * @param familyMembers  all family members
     * @param pantry         pantry inventory of remaining foods
     * @param shop           shop inventory of available foods to order
     * @param mealHistory    history of previous meal allocations
     * @return               shopping list of foods to order
     *
     */
    public abstract ShoppingList stockPantry(Integer week,
    									   	 List<FamilyMember> familyMembers,
    									   	 Pantry pantry,
    									   	 Shop shop,
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
    
}