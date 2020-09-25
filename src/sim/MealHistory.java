package sim;

import java.util.Map;
import java.util.HashMap;

public class MealHistory {

	private Map<Integer, Planner> pastPlanners = new HashMap<>();
	private Map<Integer, ShoppingList> pastShoppingLists = new HashMap<>();
	private Map<Integer, Pantry> pastPantries = new HashMap<>();
}