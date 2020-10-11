package menu.sim;

import java.io.Serializable;
import java.util.*;

import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class FamilyMember implements Serializable {
	
	private MemberName memberName;
	private Map<FoodType, Double> foodPreferenceMap;
	private Map<Day, Map<MealType, FoodType>> assignedMealMap;
	private Double satisfaction;
		
	public FamilyMember(MemberName memberName) {
		this.memberName = memberName;
		this.foodPreferenceMap = new HashMap<>();
		this.assignedMealMap = new HashMap<>();
		this.satisfaction = 1.0;
	}
	
	public MemberName getName() {
		return memberName;
	}
	
	public Map<FoodType, Double> getFoodPreferenceMap() {
		return foodPreferenceMap;
	}
	
	public Double getFoodPreference(FoodType foodType) {
		return foodPreferenceMap.get(foodType);
	}
	
	public void setFoodPreference(FoodType foodType, Double preference) {
		foodPreferenceMap.put(foodType, preference);
	}

	public Map<Day, Map<MealType, FoodType>> getAssignedMealMap() {
		return assignedMealMap;
	}
	
	public Map<MealType, FoodType> getAssignedDayMealMap(Day day) {
		return assignedMealMap.get(day);
	}
	
	public FoodType getAssignedMeal(Day day, MealType mealType) {
		return assignedMealMap.get(day).get(mealType);
	}
	
	public void assignMeal(Day day, MealType mealType, FoodType foodType) {
		if(!assignedMealMap.containsKey(day))
			assignedMealMap.put(day, new HashMap<>());
		assignedMealMap.get(day).put(mealType, foodType);
	}
	
	public void resetMealMap() {
		assignedMealMap = new HashMap<>();
	}
	
	public void clearPreferences() {
		foodPreferenceMap = new HashMap<>();
	}
	
	public Double getSatisfaction() {
		return satisfaction;
	}
	
	public void setSatisfaction(Double satisfaction) {
		this.satisfaction = satisfaction;
	}	
}