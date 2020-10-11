package menu.sim;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import menu.sim.Food.MealType;
import menu.sim.Food.FoodType;

public class ShoppingList {
	private Map<MealType, List<FoodType>> fullOrderMap;
	private Map<MealType, Integer> allLimitsMap;
	
	public ShoppingList() {
		fullOrderMap = new HashMap<>();
		allLimitsMap = new HashMap<>();
		for(MealType mealType : Food.getAllMealTypes()) {
			fullOrderMap.put(mealType, new ArrayList<>());
			allLimitsMap.put(mealType, 0);
		}
	}
	
	public Map<MealType, List<FoodType>> getFullOrderMap() {
		return fullOrderMap;
	}
	
	public Map<MealType, Integer> getAllLimitsMap() {
		return allLimitsMap;
	}
	
	public List<FoodType> getMealOrder(MealType mealType) {
		return fullOrderMap.get(mealType);
	}
	
	public Integer getLimit(MealType mealType) {
		return allLimitsMap.get(mealType);
	}
	
	public void addToOrder(MealType mealType, FoodType foodType) {
		fullOrderMap.get(mealType).add(foodType);
	}
	
	public void addLimit(MealType mealType, Integer limit) {
		allLimitsMap.put(mealType, limit);
	}
}