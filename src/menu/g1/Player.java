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

		Pantry originalPantry = pantry.clone();

		List<MemberName> memberNames = new ArrayList<>();

		Map<MealType, List<MemberName>> memberPriorityList = new HashMap<>();

		Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences = new HashMap<>();

		for(FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Planner planner = new Planner(memberNames);

		updateFamilyPreferenceMap(pantry, familyMembers, orderedFamilyPreferences);
		updateMemberPriorityList(familyMembers, memberPriorityList, orderedFamilyPreferences);

		for (MemberName member : orderedFamilyPreferences.keySet()){
			simPrinter.println("\n" + member);
			for (MealType meal : Food.getAllMealTypes()){
				simPrinter.println("   " + meal);
				simPrinter.println(orderedFamilyPreferences.get(member).get(meal));
				simPrinter.println(memberPriorityList.get(meal));
			}
		}
		
		for(MealType meal : Food.getAllMealTypes()){
			for(Day day : Day.values()){
				for(MemberName memberName : memberPriorityList.get(meal)){
					switch(meal){

						case BREAKFAST:
							FoodType food = getBestFood(meal, memberName, orderedFamilyPreferences);
							planner.addMeal(day, memberName, meal, food);
							pantry.removeMealFromInventory(food);
							updateFamilyPreferenceMap(pantry, familyMembers, orderedFamilyPreferences);
							break;

						case LUNCH:
							FoodType maxAvailableLunchMeal = getMaximumAvailableMeal(pantry, MealType.LUNCH);
    						if(pantry.getNumAvailableMeals(maxAvailableLunchMeal) > 0) {
        	    				planner.addMeal(day, memberName, MealType.LUNCH, maxAvailableLunchMeal);
        	    				pantry.removeMealFromInventory(maxAvailableLunchMeal); 
        	    			}
							break;

						case DINNER:
							FoodType maxAvailableDinnerMeal = getMaximumAvailableMeal(pantry, MealType.DINNER);
							Integer numDinners = Math.min(pantry.getNumAvailableMeals(maxAvailableDinnerMeal), familyMembers.size());
							for(int i = 0; i < numDinners; i++) {
								MemberName thisMember = memberNames.get(i);
				    	    	planner.addMeal(day, thisMember, MealType.DINNER, maxAvailableDinnerMeal);
						    	pantry.removeMealFromInventory(maxAvailableDinnerMeal);
							}
							break;
					}
				}
				updateMemberPriorityList(familyMembers, memberPriorityList, orderedFamilyPreferences);
			}
		}
		simPrinter.println("\n\n\n********* PLANNER ********\n");
		for(MealType meal : Food.getAllMealTypes()){
			simPrinter.println("MEAL: " + meal);
			for(Day day : Day.values()){
				simPrinter.println("\tDAY: " + day); 
				for(MemberName memberName : memberNames){
					simPrinter.println("\t\t"+ memberName + ": " + planner.getPlan().get(day).get(memberName).get(meal));
				}
			}
		}

		if(Player.hasValidPlanner(planner, originalPantry))
			return planner;
		return new Planner();
	}

	private void updateFamilyPreferenceMap(Pantry pantry, List<FamilyMember> familyMembers, Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences){

		orderedFamilyPreferences.clear();

		for(FamilyMember familyMember : familyMembers){

			Map<FoodType, Double> memberPrefs = familyMember.getFoodPreferenceMap();
			Map<FoodType, Double> breakfastPrefs = new HashMap<>();
			Map<FoodType, Double> lunchPrefs = new HashMap<>();
			Map<FoodType, Double> dinnerPrefs = new HashMap<>();

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
	}

	private void updateMemberPriorityList(List<FamilyMember> familyMembers, Map<MealType, List<MemberName>> memberPriorityList, Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences){
		
		memberPriorityList.clear();

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
			memberPriorityList.put(meal, membersOrder);
		}
	}

	private MemberName getPriorityMember(List<MemberName> membersOrder){
		return membersOrder.get(0);
	}

	private FoodType getBestFood(MealType meal, MemberName member, Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences){
		return orderedFamilyPreferences.get(member).get(meal).keySet().iterator().next();

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
