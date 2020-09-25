package sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.Food.FoodType;
import sim.Food.MealType;

public class Inventory {
	
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
}