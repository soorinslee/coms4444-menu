package menu.sim;

import java.util.Map;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import menu.sim.Food.MealType;
import menu.sim.Food.FoodType;


public class MealHistory implements Serializable {

	private Map<Integer, Planner> allPlanners = new HashMap<>();
	private Map<Integer, ShoppingList> allShoppingLists = new HashMap<>();
	private Map<Integer, Pantry> allPantries = new HashMap<>();
	private Map<Integer, Map<MemberName, Map<MealType, FoodType>>> dailyFamilyMeals = new HashMap<>();
	private Map<Integer, Map<MemberName, Double>> allSatisfactions = new HashMap<>();
	private Map<Integer, Map<MemberName, Double>> allAverageSatisfactions = new HashMap<>();
	
	
	public Map<Integer, Planner> getAllPlanners() {
		return allPlanners;
	}
	
	public Map<Integer, ShoppingList> getAllShoppingLists() {
		return allShoppingLists;
	}
	
	public Map<Integer, Pantry> getAllPantries() {
		return allPantries;
	}
	
	public Map<Integer, Map<MemberName, Map<MealType, FoodType>>> getDailyFamilyMeals() {
		return dailyFamilyMeals;
	}
	
	public Map<Integer, Map<MemberName, Double>> getAllSatisfactions() {
		return allSatisfactions;
	}

	public Map<Integer, Map<MemberName, Double>> getAllAverageSatisfactions() {
		return allAverageSatisfactions;
	}

	public FoodType getDailyFamilyMeal(Integer week, Day day, MemberName memberName, MealType mealType) {
		int numDays = (week - 1) * 7 + new ArrayList<>(Arrays.asList(Day.values())).indexOf(day) + 1;
		if(!dailyFamilyMeals.containsKey(numDays))
			return null;
		if(!dailyFamilyMeals.get(numDays).containsKey(memberName))
			return null;
		if(!dailyFamilyMeals.get(numDays).get(memberName).containsKey(mealType))
			return null;		
		return dailyFamilyMeals.get(numDays).get(memberName).get(mealType);
	}
	
	public Planner getPlanner(Integer week) {
		if(allPlanners.containsKey(week))
			return allPlanners.get(week);
		return null;
	}
	
	public ShoppingList getShoppingList(Integer week) {
		if(allShoppingLists.containsKey(week))
			return allShoppingLists.get(week);
		return null;
	}

	public Pantry getPantry(Integer week) {
		if(allPantries.containsKey(week))
			return allPantries.get(week);
		return null;
	}

	public Double getSatisfaction(Integer week, MemberName memberName) {
		return allSatisfactions.get(week).get(memberName);
	}

	public Double getAverageSatisfaction(Integer week, MemberName memberName) {
		return allAverageSatisfactions.get(week).get(memberName);
	}
	
	public void addDailyFamilyMeal(Integer week, Day day, MemberName memberName, Map<MealType, FoodType> mealMap) {
		int numDays = (week - 1) * 7 + new ArrayList<>(Arrays.asList(Day.values())).indexOf(day) + 1;
		if(!dailyFamilyMeals.containsKey(numDays))
			dailyFamilyMeals.put(numDays, new HashMap<>());
		dailyFamilyMeals.get(numDays).put(memberName, mealMap);
	}
	
	public void addPlanner(Integer week, Planner planner) {
		allPlanners.put(week, planner);
	}
	
	public void addShoppingList(Integer week, ShoppingList shoppingList) {
		allShoppingLists.put(week, shoppingList);
	}

	public void addPantry(Integer week, Pantry pantry) {
		allPantries.put(week, pantry);
	}
	
	public void addSatisfaction(Integer week, MemberName memberName, Double satisfaction) {
		if(!allSatisfactions.containsKey(week))
			allSatisfactions.put(week, new HashMap<>());
		allSatisfactions.get(week).put(memberName, satisfaction);
	}

	public void addAverageSatisfaction(Integer week, MemberName memberName, Double averageSatisfaction) {
		if(!allAverageSatisfactions.containsKey(week))
			allAverageSatisfactions.put(week, new HashMap<>());
		allAverageSatisfactions.get(week).put(memberName, averageSatisfaction);
	}
}