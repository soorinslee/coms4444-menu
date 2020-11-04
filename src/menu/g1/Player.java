package menu.g1; // TODO modify the package name to reflect your team

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
	 * @param capacity          pantry capacity
	 * @param seed              random seed
	 * @param simPrinter        simulation printer
	 *
	 */

	private final Integer DAYS_PER_WEEK = 7;
	private Weighter weighter;
	private Map<MealType, Map<FamilyMember, ArrayList<FoodType>>> optimisticPlanner;

	public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
		super(weeks, numFamilyMembers, capacity, seed, simPrinter);
		this.optimisticPlanner = new HashMap<>();
		this.weighter = new Weighter(numFamilyMembers);
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
		weighter.update(week, mealHistory, familyMembers);
		resetOptimisticPlanner(familyMembers);

		ShoppingList shoppingList = new ShoppingList();
		addMeals(MealType.BREAKFAST, familyMembers, shoppingList);
		addMeals(MealType.LUNCH, familyMembers, shoppingList);
		addDinner(shoppingList);

		return shoppingList;
	}

	// does not work with dinner meals
	private void addMeals(MealType mealType, List<FamilyMember> familyMembers, ShoppingList shoppingList) {
		Map<FamilyMember, PriorityQueue<FoodScore>> memberScores = weighter.getMemberScoresFor(mealType);

		shoppingList.addLimit(mealType, DAYS_PER_WEEK * numFamilyMembers);
		ArrayList<FoodType> addedFoods = new ArrayList<>();

		for (FamilyMember member: familyMembers) {
			FoodType food = memberScores.get(member).poll().getFoodType();
			addFoodToOrder(shoppingList, food, DAYS_PER_WEEK);
			optimisticPlanner.get(mealType).get(member).add(food);
			addedFoods.add(food);
		}

		PriorityQueue<FoodScore> avgScores = weighter.getAvgScoresFor(mealType);
		while (!avgScores.isEmpty()) {
			FoodType food = avgScores.poll().getFoodType();
			if (!addedFoods.contains(food)) {
				addFoodToOrder(shoppingList, food, DAYS_PER_WEEK);
			}
		}
	}

	private void addDinner(ShoppingList shoppingList) {
		shoppingList.addLimit(MealType.DINNER, DAYS_PER_WEEK * numFamilyMembers);
		PriorityQueue<FoodScore> avgScores = weighter.getAvgScoresFor(MealType.DINNER);
		while (!avgScores.isEmpty()) {
			FoodType food = avgScores.poll().getFoodType();
			addFoodToOrder(shoppingList, food, numFamilyMembers);
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
		//printPantry(pantry);
		Planner planner = new Planner();

		// breakfast
		addFirstChoices(planner, pantry, MealType.BREAKFAST);

		// lunch
		addFirstChoices(planner, pantry, MealType.LUNCH);

		return planner;
	}

	// returns array of family members who did not get their first choice of food
	private ArrayList<FamilyMember> addFirstChoices(Planner planner, Pantry pantry, MealType mealType) {
		Map<FamilyMember, ArrayList<FoodType>> optimisticPlan = optimisticPlanner.get(mealType);
		ArrayList<FamilyMember> noMealAssigned = new ArrayList<>();

		for (Map.Entry<FamilyMember, ArrayList<FoodType>> firstChoice: optimisticPlan.entrySet()) {
			FamilyMember member = firstChoice.getKey();
			// TODO: switch to tackle changes in food per day
			FoodType food = firstChoice.getValue().get(0);
			if (pantry.getNumAvailableMeals(food) >= DAYS_PER_WEEK) {
				addFoodToPlanner(planner, member, food);
			}
			else {
				noMealAssigned.add(member);
			}
		}
		return noMealAssigned;
	}

	private void resetOptimisticPlanner(List<FamilyMember> familyMembers) {
		for (MealType mealType: MealType.values()) {
			Map<FamilyMember, ArrayList<FoodType>> mealSpecificPlanner = new HashMap<>();
			for (FamilyMember familyMember : familyMembers) {
				mealSpecificPlanner.put(familyMember, new ArrayList<FoodType>());
			}
			optimisticPlanner.put(mealType, mealSpecificPlanner);
		}
	}

	private void addFoodToOrder(ShoppingList shoppingList, FoodType foodType, int quantity) {
		for (int i = 0; i < quantity; i++) {
			shoppingList.addToOrder(foodType);
		}
	}

	private void addFoodToPlanner(Planner planner, FamilyMember member, FoodType foodType) {
		for (Day day: Day.values()) {
			planner.addMeal(day, member.getName(), Food.getMealType(foodType), foodType);
		}
	}

	private void addFoodToPlanner(Planner planner, FamilyMember member, FoodType foodType, ArrayList<Day> days) {
		for (Day day: days) {
			planner.addMeal(day, member.getName(), Food.getMealType(foodType), foodType);
		}
	}

	private void printPantry(Pantry pantry) {
		for (FoodType foodType: FoodType.values()) {
			simPrinter.println(foodType.toString() + ": " + pantry.getNumAvailableMeals(foodType));
		}
	}
}