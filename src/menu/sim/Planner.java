package menu.sim;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Planner implements Serializable {
	
	private Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan;
	
	public Planner() {
		plan = new HashMap<>();
		for(Day day : Day.values())
			plan.put(day, new HashMap<>());
	}
	
	public Planner(List<MemberName> memberNames) {
		plan = new HashMap<>();
		for(Day day : Day.values()) {
			plan.put(day, new HashMap<>());
			for(MemberName memberName : memberNames)
				plan.get(day).put(memberName, new HashMap<>());
		}
	}
	
	public void addMeal(Day day, MemberName memberName, MealType mealType, FoodType foodType) {
		if(!plan.get(day).containsKey(memberName))
			plan.get(day).put(memberName, new HashMap<>());
		plan.get(day).get(memberName).put(mealType, foodType);
	}
	
	public FoodType getMeal(Day day, MemberName memberName, MealType mealType) {
		if(!plan.containsKey(day))
			return null;
		if(!plan.get(day).containsKey(memberName))
			return null;
		if(!plan.get(day).get(memberName).containsKey(mealType))
			return null;
		return plan.get(day).get(memberName).get(mealType);
	}
	
	public Map<Day, Map<MemberName, Map<MealType, FoodType>>> getPlan() {
		return plan;
	}
}