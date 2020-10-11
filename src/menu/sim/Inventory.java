package menu.sim;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Inventory implements Serializable {
	
	private Map<MealType, Map<FoodType, Integer>> mealsMap;
		
	public Inventory() {
		clearInventory();
	}
	
	public List<FoodType> getAvailableFoodTypes(MealType mealType) {
		List<FoodType> availableMeals = new ArrayList<>();
		Map<FoodType, Integer> mealTypeFoods = mealsMap.get(mealType);
		for(FoodType foodType : mealTypeFoods.keySet())
			if(mealTypeFoods.get(foodType) > 0)
				availableMeals.add(foodType);
		return availableMeals;
	}
	
	public Integer getNumAvailableFoodTypes(MealType mealType) {
		return getAvailableFoodTypes(mealType).size();
	}
	
	public Integer getNumAvailableMeals() {
		int numMeals = 0;
		for(MealType mealType : mealsMap.keySet()) {
			Map<FoodType, Integer> mealTypeFoods = mealsMap.get(mealType);
			for(FoodType foodType : mealTypeFoods.keySet())
				numMeals += mealTypeFoods.get(foodType);		
		}
		return numMeals;
	}

	public Integer getNumAvailableMeals(MealType mealType) {
		int numMeals = 0;
		Map<FoodType, Integer> mealTypeFoods = mealsMap.get(mealType);
		for(FoodType foodType : mealTypeFoods.keySet())
			numMeals += mealTypeFoods.get(foodType);		
		return numMeals;
	}
	
	public Integer getNumAvailableMeals(FoodType foodType) {
		MealType mealType = Food.getMealType(foodType);
		return mealsMap.get(mealType).get(foodType);
	}
	
	public boolean containsMeal(FoodType foodType) {
		return getNumAvailableMeals(foodType) > 0;
	}
	
	public void addMealToInventory(FoodType foodType) {
		MealType mealType = Food.getMealType(foodType);
		mealsMap.get(mealType).put(foodType, mealsMap.get(mealType).get(foodType) + 1);
	}
	
	public void removeMealFromInventory(FoodType foodType) {
		MealType mealType = Food.getMealType(foodType);
		if(mealsMap.get(mealType).get(foodType) > 0)
			mealsMap.get(mealType).put(foodType, mealsMap.get(mealType).get(foodType) - 1);
	}
	
	public Map<MealType, Map<FoodType, Integer>> getMealsMap() {
		return mealsMap;
	}
	
	public void clearInventory() {
		mealsMap = new HashMap<>();
		for(MealType mealType : Food.getAllMealTypes()) {
			Map<FoodType, Integer> mealTypeFoods = new HashMap<>();
			for(FoodType foodType : Food.getFoodTypes(mealType))
				mealTypeFoods.put(foodType, 0);
			mealsMap.put(mealType, mealTypeFoods);
		}
	}
	
	public Inventory clone() {
		Inventory inventory = new Inventory();
		for(MealType mealType : mealsMap.keySet()) {
			for(FoodType foodType : mealsMap.get(mealType).keySet()) {
				for(int i = 0; i < mealsMap.get(mealType).get(foodType); i++)
					inventory.addMealToInventory(foodType);
			}
		}
		return inventory;
	}
}