package sim;

import java.util.HashMap;
import java.util.Map;

import sim.Food.FoodType;
import sim.Food.MealType;

public class Planner {
	
	private Map<Day, Map<Integer, Map<MealType, FoodType>>> plan;
	
	public Planner() {
		plan = new HashMap<>();
		for(Day day : Day.values())
			plan.put(day, new HashMap<>());
	}
	
}
