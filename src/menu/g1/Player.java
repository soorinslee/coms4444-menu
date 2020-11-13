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
	 * @param capacity          pantry capacity
	 * @param seed              random seed
	 * @param simPrinter        simulation printer
	 *
	 */

	private final Integer DAYS_PER_WEEK = 7;
	private Weighter weighter;
	private Map<MealType, Map<FamilyMember, ArrayList<FoodType>>> optimisticPlanner;
	private FamilyTracker familyTracker;

	
	public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
		super(weeks, numFamilyMembers, capacity, seed, simPrinter);
		this.optimisticPlanner = new HashMap<>();
		this.weighter = new Weighter(numFamilyMembers);
		this.familyTracker = new FamilyTracker();
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

	public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {
		if (week == 1) {
			familyTracker.init(familyMembers);
		}
		familyTracker.update(week, mealHistory);

		weighter.update(week, mealHistory, familyMembers);
		resetOptimisticPlanner(familyMembers);

		ShoppingList shoppingList = new ShoppingList();
		addMeals(MealType.BREAKFAST, familyMembers, shoppingList);
		addMeals(MealType.LUNCH, familyMembers, shoppingList);
		shopDinner(shoppingList);

		return shoppingList;
	}

	private void shopDinner(ShoppingList shoppingList) {
		// TODO: Pick based on each day
		shoppingList.addLimit(MealType.DINNER, DAYS_PER_WEEK * numFamilyMembers);
		PriorityQueue<FoodScore> dinners = familyTracker.getDinnersByCompositeScore(6);
		simPrinter.println("DINNERS SIZE: " + Integer.toString(dinners.size()));
		while (!dinners.isEmpty()) {
			FoodType dinner = dinners.poll().getFoodType();
			addFoodToOrder(shoppingList, dinner, numFamilyMembers);
		}
	}

	// does not work with dinner meals
	private void addMeals(MealType mealType, List<FamilyMember> familyMembers, ShoppingList shoppingList) {
		Map<FamilyMember, PriorityQueue<FoodScore>> memberScores = weighter.getMemberScoresFor(mealType);

		shoppingList.addLimit(mealType, DAYS_PER_WEEK * numFamilyMembers);
		ArrayList<FoodType> addedFoods = new ArrayList<>();

		for (FamilyMember member: familyMembers) {
			FoodType food = memberScores.get(member).poll().getFoodType();
			addFoodToOrder(shoppingList, food, DAYS_PER_WEEK);
			optimisticPlanner.get(mealType).get(member).add(food);
			addedFoods.add(food);
		}

		PriorityQueue<FoodScore> avgScores = weighter.getAvgScoresFor(mealType);
		while (!avgScores.isEmpty()) {
			FoodType food = avgScores.poll().getFoodType();
			if (!addedFoods.contains(food)) {
				addFoodToOrder(shoppingList, food, DAYS_PER_WEEK);
			}
		}
	}

	private void addDinner(ShoppingList shoppingList) {
		shoppingList.addLimit(MealType.DINNER, DAYS_PER_WEEK * numFamilyMembers);
		PriorityQueue<FoodScore> avgScores = weighter.getAvgScoresFor(MealType.DINNER);
		while (!avgScores.isEmpty()) {
			FoodType food = avgScores.poll().getFoodType();
			addFoodToOrder(shoppingList, food, numFamilyMembers);
		}
	}

	// returns array of family members who did not get their first choice of food
	private ArrayList<FamilyMember> addFirstChoices(Planner planner, Pantry pantry, MealType mealType) {
		Map<FamilyMember, ArrayList<FoodType>> optimisticPlan = optimisticPlanner.get(mealType);
		ArrayList<FamilyMember> noMealAssigned = new ArrayList<>();

		for (Map.Entry<FamilyMember, ArrayList<FoodType>> firstChoice: optimisticPlan.entrySet()) {
			FamilyMember member = firstChoice.getKey();
			// TODO: switch to tackle changes in food per day
			FoodType food = firstChoice.getValue().get(0);
			if (pantry.getNumAvailableMeals(food) >= DAYS_PER_WEEK) {
				addFoodToPlanner(planner, member, food);
			}
			else {
				noMealAssigned.add(member);
			}
		}
		return noMealAssigned;
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
		List<FamilyMember> weightedPreferences = new ArrayList<>(familyMembers);

		Map<MealType, List<MemberName>> memberPriorityList = new LinkedHashMap<>();

		Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences = new HashMap<>();

		for(FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Planner planner = new Planner(memberNames);

		updateFamilyPreferenceMap(pantry, familyMembers, orderedFamilyPreferences);
		updateMemberPriorityList(familyMembers, memberPriorityList, orderedFamilyPreferences);


		simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.BREAKFAST));
		simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.LUNCH));
		simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.DINNER));

		
		simPrinter.println("Order: " + memberPriorityList.get(MealType.BREAKFAST));
		for (MemberName member : orderedFamilyPreferences.keySet()){
			simPrinter.println("\t\t" + member + ": " + orderedFamilyPreferences.get(member).get(MealType.BREAKFAST));
		}
		
		for(MealType meal : Food.getAllMealTypes()){
			for(Day day : Day.values()){
				for(MemberName memberName : memberPriorityList.get(meal)){
					if (pantry.getNumAvailableMeals(meal) > 0){
						FoodType food;
						switch(meal){

							case BREAKFAST:
								food = getBestFood(meal, memberName, orderedFamilyPreferences);
								planner.addMeal(day, memberName, meal, food);
								pantry.removeMealFromInventory(food);
								updateFamilyPreferenceMap(pantry, weightedPreferences, orderedFamilyPreferences);
								break;

							case LUNCH:
								food = getBestFood(meal, memberName, orderedFamilyPreferences);
								planner.addMeal(day, memberName, meal, food);
								pantry.removeMealFromInventory(food);
								updateFamilyPreferenceMap(pantry, weightedPreferences, orderedFamilyPreferences);
								break;

							case DINNER:
								
								break;
						}
					}
				}
				updateMemberPriorityList(weightedPreferences, memberPriorityList, orderedFamilyPreferences);
			}
		}

		planDinners(planner, pantry, familyMembers);

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

	private void planDinners(Planner planner, Pantry pantry, List<FamilyMember> familyMembers) {
		Map<FoodType, Integer> dinnerInventory = pantry.getMealsMap().get(MealType.DINNER);
		PriorityQueue<FoodScore> dinners = familyTracker.getDinnersByCompositeScore(6, dinnerInventory);

		for (Day day: Day.values()) {
			if (!dinners.isEmpty()) {
				FoodType dinner = dinners.poll().getFoodType();
				Integer quantity = dinnerInventory.get(dinner);
				for (int i = 0; i < quantity; i++) {
					FamilyMember member = familyMembers.get(i);
					planner.addMeal(day, member.getName(), MealType.DINNER, dinner);
				}
			}
		}
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
					Map<FoodType, Double> memberFoodPrefs = orderedFamilyPreferences.get(familyMember.getName()).get(meal);

					if (memberFoodPrefs.size() < 1){
						continue;
					}
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

	private void resetOptimisticPlanner(List<FamilyMember> familyMembers) {
		for (MealType mealType: MealType.values()) {
			Map<FamilyMember, ArrayList<FoodType>> mealSpecificPlanner = new HashMap<>();
			for (FamilyMember familyMember : familyMembers) {
				mealSpecificPlanner.put(familyMember, new ArrayList<FoodType>());
			}
			optimisticPlanner.put(mealType, mealSpecificPlanner);
		}
	}

	private void addFoodToOrder(ShoppingList shoppingList, FoodType foodType, int quantity) {
		for (int i = 0; i < quantity; i++) {
			shoppingList.addToOrder(foodType);
		}
	}

	private void addFoodToPlanner(Planner planner, FamilyMember member, FoodType foodType) {
		for (Day day: Day.values()) {
			planner.addMeal(day, member.getName(), Food.getMealType(foodType), foodType);
		}
	}

	private void addFoodToPlanner(Planner planner, FamilyMember member, FoodType foodType, ArrayList<Day> days) {
		for (Day day: days) {
			planner.addMeal(day, member.getName(), Food.getMealType(foodType), foodType);
		}
	}

	private void printPantry(Pantry pantry) {
		for (FoodType foodType: FoodType.values()) {
			simPrinter.println(foodType.toString() + ": " + pantry.getNumAvailableMeals(foodType));
		}
	}
}