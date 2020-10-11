package menu.sim;

import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Pantry extends Inventory {
	
	private Integer capacity;
	
	public Pantry(Integer capacity) {
		super();
		this.capacity = capacity;
	}
	
	@Override
	public void addMealToInventory(FoodType foodType) {
		if(getNumEmptySlots() > 0)
			super.addMealToInventory(foodType);
	}
	
	public Integer getNumEmptySlots() {
		int numEmptySlots = capacity - getNumAvailableMeals();
		if(numEmptySlots < 0)
			return 0;
		return numEmptySlots;
	}
	
	public Pantry clone() {
		Pantry pantry = new Pantry(capacity);
		for(MealType mealType : this.getMealsMap().keySet()) {
			for(FoodType foodType : this.getMealsMap().get(mealType).keySet()) {
				for(int i = 0; i < this.getMealsMap().get(mealType).get(foodType); i++)
					pantry.addMealToInventory(foodType);
			}
		}
		return pantry;
	}
}