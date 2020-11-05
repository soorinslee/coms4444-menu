package menu.g1;

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
	 * @param capacity			pantry capacity
	 * @param seed        		random seed
	 * @param simPrinter   		simulation printer
	 *
	 */
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
	public ShoppingList stockPantry(Integer week,
									Integer numEmptySlots,
									List<FamilyMember> familyMembers,
									Pantry pantry,
									MealHistory mealHistory) {
		
		int numBreakfastFoods = random.nextInt(numEmptySlots + 1);
		int numLunchFoods = random.nextInt(numEmptySlots - numBreakfastFoods + 1);
		int numDinnerFoods = numEmptySlots - numBreakfastFoods - numLunchFoods;
		
		ShoppingList shoppingList = new ShoppingList();
		shoppingList.addLimit(MealType.BREAKFAST, numBreakfastFoods);
		shoppingList.addLimit(MealType.LUNCH, numLunchFoods);
		shoppingList.addLimit(MealType.DINNER, numDinnerFoods);
		
		List<FoodType> breakfastFoods = Food.getFoodTypes(MealType.BREAKFAST);
		List<FoodType> lunchFoods = Food.getFoodTypes(MealType.LUNCH);
		List<FoodType> dinnerFoods = Food.getFoodTypes(MealType.DINNER);
		
		for(int i = 0; i < 2 * capacity; i++)
			shoppingList.addToOrder(breakfastFoods.get(random.nextInt(breakfastFoods.size())));
		for(int i = 0; i < 2 * capacity; i++)
			shoppingList.addToOrder(lunchFoods.get(random.nextInt(lunchFoods.size())));
		for(int i = 0; i < 2 * capacity; i++)
			shoppingList.addToOrder(dinnerFoods.get(random.nextInt(dinnerFoods.size())));
		
		if(Player.hasValidShoppingList(shoppingList, numEmptySlots))
			return shoppingList;
		return new ShoppingList();
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
	public Planner planMeals(Integer week,
							 List<FamilyMember> familyMembers,
							 Pantry pantry,
							 MealHistory mealHistory) {

		List<MemberName> memberNames = new ArrayList<>();
		Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences = new HashMap<>();

		for(FamilyMember familyMember : familyMembers){

			Map<FoodType, Double> memberPrefs = familyMember.getFoodPreferenceMap();
			Map<FoodType, Double> breakfastPrefs = new HashMap<>();
			Map<FoodType, Double> lunchPrefs = new HashMap<>();
			Map<FoodType, Double> dinnerPrefs = new HashMap<>();

			memberNames.add(familyMember.getName());
			orderedFamilyPreferences.put(familyMember.getName(), new LinkedHashMap<>());
			orderedFamilyPreferences.get(familyMember.getName()).put(MealType.BREAKFAST, new LinkedHashMap<>());
			orderedFamilyPreferences.get(familyMember.getName()).put(MealType.LUNCH, new LinkedHashMap<>());
			orderedFamilyPreferences.get(familyMember.getName()).put(MealType.DINNER, new LinkedHashMap<>());

			for(FoodType thisFood : Food.getAllFoodTypes()){
				switch(Food.getMealType(thisFood)) {
					case BREAKFAST:
						if(pantry.getNumAvailableMeals(thisFood) > 0)
							breakfastPrefs.put(thisFood, memberPrefs.get(thisFood));
						break; 
					case LUNCH:
						if(pantry.getNumAvailableMeals(thisFood) > 0)
							lunchPrefs.put(thisFood, memberPrefs.get(thisFood));	
						break; 
					case DINNER:
						if(pantry.getNumAvailableMeals(thisFood) > 2)
							dinnerPrefs.put(thisFood, memberPrefs.get(thisFood));
						break;
				}
			}

			for (MealType meal : Food.getAllMealTypes()){
				switch(meal) {
					case BREAKFAST:
						breakfastPrefs.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.forEachOrdered(x -> orderedFamilyPreferences
													.get(familyMember.getName())
													.get(meal)
													.put(x.getKey(), x.getValue()));
						break; 
					case LUNCH:
						lunchPrefs.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.forEachOrdered(x -> orderedFamilyPreferences
													.get(familyMember.getName())
													.get(meal)
													.put(x.getKey(), x.getValue()));
						break; 
					case DINNER:
						dinnerPrefs.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
							.forEachOrdered(x -> orderedFamilyPreferences
													.get(familyMember.getName())
													.get(meal)
													.put(x.getKey(), x.getValue()));
						break;
				}
			}
		}

		Map<MealType, List<MemberName>> orderedMembersMeals = new HashMap<>();
		for (MealType meal : Food.getAllMealTypes()){
			List<MemberName> membersOrder = new ArrayList<>();
			while(membersOrder.size() < familyMembers.size()){
				double bestPref = -1.0;
				MemberName lowestMember = null;
				for(FamilyMember familyMember : familyMembers){
					Map<MealType, Map<FoodType, Double>> memberMealPrefs = orderedFamilyPreferences.get(familyMember.getName());
					Map<FoodType, Double> memberFoodPrefs = memberMealPrefs.get(meal);
					if (memberFoodPrefs.size() < 1)
						continue;
					double memberBestPref = memberFoodPrefs.values().stream().findFirst().get();
					if (bestPref < 0 || bestPref > memberBestPref){
						if (!membersOrder.contains(familyMember.getName())){
							bestPref = memberBestPref;
							lowestMember = familyMember.getName();
						}	
					}
				}
				membersOrder.add(lowestMember);
			}
			orderedMembersMeals.put(meal, membersOrder);
		}

		for (MemberName member : orderedFamilyPreferences.keySet())
			for (MealType meal : Food.getAllMealTypes()){
				simPrinter.println("\n" + member);
				simPrinter.println("   " + meal);
				simPrinter.println(orderedFamilyPreferences.get(member).get(meal));
			}
		Pantry originalPantry = pantry.clone();
		
		Planner planner = new Planner(memberNames);
		for(MemberName memberName : memberNames) {

			for(Day day : Day.values()) {
				FoodType maxAvailableBreakfastMeal = getMaximumAvailableMeal(pantry, MealType.BREAKFAST);
				if(pantry.getNumAvailableMeals(maxAvailableBreakfastMeal) > 0) {
					planner.addMeal(day, memberName, MealType.BREAKFAST, maxAvailableBreakfastMeal);
					pantry.removeMealFromInventory(maxAvailableBreakfastMeal);    				
				}
				FoodType maxAvailableLunchMeal = getMaximumAvailableMeal(pantry, MealType.LUNCH);
				if(pantry.getNumAvailableMeals(maxAvailableLunchMeal) > 0) {
					planner.addMeal(day, memberName, MealType.LUNCH, maxAvailableLunchMeal);
					pantry.removeMealFromInventory(maxAvailableLunchMeal);    				
				}
			}
		}
		for(Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal = getMaximumAvailableMeal(pantry, MealType.DINNER);
			Integer numDinners = Math.min(pantry.getNumAvailableMeals(maxAvailableDinnerMeal), familyMembers.size());
			for(int i = 0; i < numDinners; i++) {
				MemberName memberName = memberNames.get(i);
				planner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
				pantry.removeMealFromInventory(maxAvailableDinnerMeal);
			}
		}

		if(Player.hasValidPlanner(planner, originalPantry))
			return planner;
		return new Planner();
	}
	
	private FoodType getMaximumAvailableMeal(Pantry pantry, MealType mealType) {
		FoodType maximumAvailableMeal = null;
		int maxAvailableMeals = -1;
		for(FoodType foodType : Food.getFoodTypes(mealType)) {
			int numAvailableMeals = pantry.getNumAvailableMeals(foodType);
			if(numAvailableMeals > maxAvailableMeals) {
				maxAvailableMeals = numAvailableMeals;
				maximumAvailableMeal = foodType;
			}
		}
		return maximumAvailableMeal;
	}
}
