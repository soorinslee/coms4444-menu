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

		if (week == 1) {
			this.weighter = new Weighter(familyMembers);
		}
		weighter.update(week, mealHistory, familyMembers);

		ShoppingList shoppingList = new ShoppingList();
		addBreakfast(familyMembers, shoppingList);
		addLunch(familyMembers, shoppingList);
		addDinner(shoppingList);

		return shoppingList;
	}

	private void addBreakfast(List<FamilyMember> familyMembers, ShoppingList shoppingList) {
		shoppingList.addLimit(MealType.BREAKFAST, DAYS_PER_WEEK * numFamilyMembers * 3);

		for (FamilyMember member: familyMembers) {
			for (int j = 0; j < 3; j++) {
				FoodType food = weighter.memberScores.get(MealType.BREAKFAST).get(member).poll().getFoodType();
				for (int i = 0; i < DAYS_PER_WEEK; i++) {
					shoppingList.addToOrder(food);
				}
			}
		}
	}

	private void addLunch(List<FamilyMember> familyMembers, ShoppingList shoppingList) {
		shoppingList.addLimit(MealType.LUNCH, DAYS_PER_WEEK * numFamilyMembers * 3);

		for (FamilyMember member: familyMembers) {
			for (int j = 0; j < 3; j++) {
				FoodType food = weighter.memberScores.get(MealType.LUNCH).get(member).poll().getFoodType();
				for (int i = 0; i < DAYS_PER_WEEK; i++) {
					shoppingList.addToOrder(food);
				}
			}
		}
	}

	private void addDinner(ShoppingList shoppingList) {
		shoppingList.addLimit(MealType.DINNER,DAYS_PER_WEEK * numFamilyMembers * 3);
		for (int k = 0; k < 3; k++) {
			for (int i = 0; i < DAYS_PER_WEEK; i++) {
				FoodType food = weighter.avgScores.get(MealType.DINNER).poll().getFoodType();
				for (int j = 0; j < numFamilyMembers; j++) {
					shoppingList.addToOrder(food);
				}
			}
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
		printPantry(pantry);

		return null; // TODO modify the return statement to return your planner
	}

	private void printPantry(Pantry pantry) {
		for (FoodType foodType: FoodType.values()) {
			simPrinter.println(foodType.toString() + ": " + pantry.getNumAvailableMeals(foodType));
		}
	}

	private void printAvgFoodScores() {
		simPrinter.println("AVG: ");
		while (!weighter.avgBreakfastScores.isEmpty()) {
			FoodScore avgScore = weighter.avgBreakfastScores.poll();
			simPrinter.println(avgScore.toString());
		}
	}

	private void printMemberFoodScores(List<FamilyMember> familyMembers) {
		simPrinter.println("MEM SCORES: ");
		for (FamilyMember member: familyMembers) {
			PriorityQueue<FoodScore> foodScores = weighter.memberBreakfastScores.get(member);
			simPrinter.println(member.getName().toString());
			while (!foodScores.isEmpty()) {
				FoodScore foodScore = foodScores.poll();
				simPrinter.println(foodScore.toString());
			}
		}
	}
}