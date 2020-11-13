package menu.g1;

import java.text.BreakIterator;
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
	private FamilyTracker familyTracker;
	private Map<MealType, Integer> maxCapacities;
	private Integer totalWeeks;

	
	public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
		super(weeks, numFamilyMembers, capacity, seed, simPrinter);
		this.familyTracker = new FamilyTracker();
		this.maxCapacities = getMaxCapacitiesForMealTypes(capacity, numFamilyMembers);
		this.totalWeeks = weeks;
	}

	private Map<MealType, Integer> getMaxCapacitiesForMealTypes(Integer capacity, Integer numFamilyMembers) {
		Map<MealType, Integer> maxCapForMealTypes = new HashMap<>();
		Integer extraCap = capacity - (21 * numFamilyMembers);
		Integer bfCap = (7 * numFamilyMembers) + (int) Math.floor(extraCap * 0.25);
		Integer lunchCap = (7 * numFamilyMembers) + (int) Math.floor(extraCap * 0.75);
		Integer dinCap = (7 * numFamilyMembers) + (int) Math.floor(extraCap * 0.0);
		Integer allocated = bfCap + lunchCap + bfCap;
		while (allocated < capacity) {
			lunchCap++;
			allocated++;
		}
		maxCapForMealTypes.put(MealType.BREAKFAST, bfCap);
		maxCapForMealTypes.put(MealType.LUNCH, lunchCap);
		maxCapForMealTypes.put(MealType.DINNER, dinCap);

		return maxCapForMealTypes;
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

		ShoppingList shoppingList = new ShoppingList();
		shopBreakfast(week, shoppingList, pantry, familyMembers);
		shopLunch(shoppingList, pantry);
		shopDinner(shoppingList, pantry);

		return shoppingList;
	}

	private void shopBreakfast(Integer week,
							   ShoppingList shoppingList,
							   Pantry pantry,
							   List<FamilyMember> familyMembers) {
		Integer spotsForBreakfast = calculateNewCapacityFor(MealType.BREAKFAST, pantry);
		Integer minBreakfastsNeeded = getMinFoodsNeeded(MealType.BREAKFAST, pantry, numFamilyMembers);
		Map<MemberName, Integer> memberAllocations = getMemberAllocations(familyMembers, spotsForBreakfast);

		simPrinter.println("BREAKFAST LIMIT:" + spotsForBreakfast.toString());
		shoppingList.addLimit(MealType.BREAKFAST, spotsForBreakfast);
		addFirstChoiceBreakfasts(shoppingList, memberAllocations);
		if (minBreakfastsNeeded > 0) {
			addBackupBreakfasts(shoppingList, spotsForBreakfast);
		}
		simPrinter.println("Current pantry size: " + pantry.getNumAvailableMeals(MealType.BREAKFAST));
		//simPrinter.println("Breakfast shopping list: ");
		//simPrinter.println(shoppingList.getMealOrder(MealType.BREAKFAST).toString());
	}

	private void shopLunch(ShoppingList shoppingList, Pantry pantry) {
		Integer openSpots = calculateNewCapacityFor(MealType.LUNCH, pantry);
		shoppingList.addLimit(MealType.LUNCH, openSpots);
		Integer lunchListLength = 0;
		while (lunchListLength < openSpots) {
			PriorityQueue<MemberTracker> memberTrackers = familyTracker.getMembersByAvgSatisfaction();
			Integer quantPerLunch = 2;
			while (!memberTrackers.isEmpty()) {
				MemberTracker mt = memberTrackers.poll();
				PriorityQueue<FoodScore> lunches = mt.getFoodsByPref(MealType.LUNCH);
				while (!lunches.isEmpty()) {
					addFoodToOrder(shoppingList, lunches.poll().getFoodType(), quantPerLunch);
					lunchListLength += quantPerLunch;
				}
			}
		}
	}

	private void shopDinner(ShoppingList shoppingList, Pantry pantry) {
		Integer spotsOpen = calculateNewCapacityFor(MealType.DINNER, pantry);
		shoppingList.addLimit(MealType.DINNER, spotsOpen);
		simPrinter.println("DINNER SPOTS: " + spotsOpen.toString());
		Integer totalQuantity = 0;
		//shoppingList.addLimit(MealType.DINNER, DAYS_PER_WEEK * numFamilyMembers);
		PriorityQueue<FoodScore> dinners = familyTracker.getFoodsByCompositeScore(MealType.DINNER, 6);
		simPrinter.println("DINNERS SIZE: " + Integer.toString(dinners.size()));
		while (!dinners.isEmpty()) {
			FoodType dinner = dinners.poll().getFoodType();
			addFoodToOrder(shoppingList, dinner, numFamilyMembers);
		}

		PriorityQueue<FoodScore> dinners2 = familyTracker.getFoodsByCompositeScore(MealType.DINNER, 6);

		Integer storageScale = 4;
		Integer i = 20;
		while (!dinners2.isEmpty()) {
			Integer storageFactor = 1;
			if (maxCapacities.get(MealType.DINNER) > numFamilyMembers * 7) {
				storageFactor = (int) Math.ceil((i/20.0) * storageScale);
			}
			FoodType dinner = dinners2.poll().getFoodType();
			Integer quantityNeeded = (storageFactor * numFamilyMembers);// - pantry.getNumAvailableMeals(dinner);
			if (quantityNeeded > 0) {
				addFoodToOrder(shoppingList, dinner, quantityNeeded);
				totalQuantity += quantityNeeded;
			}
			i--;
		}
	}

	private void addFirstChoiceBreakfasts(ShoppingList shoppingList, Map<MemberName, Integer> memberAllocations) {
		PriorityQueue<MemberTracker> memberTrackers = familyTracker.getMembersByAvgSatisfaction();
		while (!memberTrackers.isEmpty()) {
			MemberTracker memberTracker = memberTrackers.poll();
			FoodType firstChoice = memberTracker.getFirstChoice(MealType.BREAKFAST);
			Integer quantity = memberAllocations.get(memberTracker.getName());
			addFoodToOrder(shoppingList, firstChoice, quantity);
		}
	}

	private void addBackupBreakfasts(ShoppingList shoppingList, Integer spotsOpen) {
		PriorityQueue<FoodScore> breakfasts = familyTracker.getBreakfastsByCompositeScore();
		while (!breakfasts.isEmpty()) {
			FoodType breakfast = breakfasts.poll().getFoodType();
			addFoodToOrder(shoppingList, breakfast, spotsOpen);
		}
	}

	private Integer calculateNewCapacityFor(MealType mealType, Pantry pantry) {
		Integer maxCapacity = maxCapacities.get(mealType);
		Integer countInPantry = getFoodCountInPantry(mealType, pantry);
		return maxCapacity - countInPantry;
	}

	private Integer getMinFoodsNeeded(MealType mealType, Pantry pantry, Integer numFamilyMembers) {
		Integer servedPerWeek = numFamilyMembers * 7;
		Integer countInPantry = getFoodCountInPantry(mealType, pantry);
		if (servedPerWeek > countInPantry) {
			return servedPerWeek - countInPantry;
		}
		return 0;
	}

	private Integer getFoodCountInPantry(MealType mealType, Pantry pantry) {
		Integer count = 0;
		for(FoodType foodType : Food.getFoodTypes(mealType)) {
			int numAvailableMeals = pantry.getNumAvailableMeals(foodType);
			count += numAvailableMeals;
		}
		return count;
	}

	private Map<MemberName, Integer> getMemberAllocations(List<FamilyMember> familyMembers, Integer breakfastCapacity) {
		Map<MemberName, Integer> memberAllocations = new HashMap<>();
		if (maxCapacities.get(MealType.BREAKFAST)/familyMembers.size() == 7) {
			for (FamilyMember familyMember: familyMembers) {
				memberAllocations.put(familyMember.getName(), 7);
			}
			return memberAllocations;
		}

		PriorityQueue<MemberTracker> memberTrackers = familyTracker.getMembersByAvgSatisfaction();
		Double totalWeight = 0.0;
		Integer totalAllocations = 0;

		while (!memberTrackers.isEmpty()) {
			MemberTracker memberTracker = memberTrackers.poll();
			totalWeight += memberTracker.getWeight();
		}
		
		for (FamilyMember familyMember: familyMembers) {
			MemberTracker memberTracker = familyTracker.getMemberTracker(familyMember.getName());
			Integer allocationScore = (int) Math.round(memberTracker.getWeight()/totalWeight * breakfastCapacity);
			while ((totalAllocations + allocationScore > breakfastCapacity) && allocationScore > 0) {
				allocationScore--;
			}
			memberAllocations.put(familyMember.getName(),  allocationScore);
		}

		return memberAllocations;
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

	public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

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

		//simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.BREAKFAST));
		//simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.LUNCH));
		//simPrinter.println("PANTRY: " + pantry.getMealsMap().get(MealType.DINNER));

		
		//simPrinter.println("Order: " + memberPriorityList.get(MealType.BREAKFAST));
		for (MemberName member : orderedFamilyPreferences.keySet()){
			//simPrinter.println("\t\t" + member + ": " + orderedFamilyPreferences.get(member).get(MealType.BREAKFAST));
			//simPrinter.println("\t\t" + member + ": " + orderedFamilyPreferences.get(member).get(MealType.LUNCH));
		}
		
		for(Day day : Day.values()){
			for(MemberName memberName : memberPriorityList.get(MealType.BREAKFAST)){
				if (pantry.getNumAvailableMeals(MealType.BREAKFAST) > 0){
					FoodType food = getBestFood(MealType.BREAKFAST, memberName, orderedFamilyPreferences);
					planner.addMeal(day, memberName, MealType.BREAKFAST, food);
					pantry.removeMealFromInventory(food);
				}
				if (pantry.getNumAvailableMeals(MealType.LUNCH) > 0){
					updateLunchPreferences(week, day, memberName, planner, mealHistory, orderedFamilyPreferences);
					FoodType food = getBestFood(MealType.LUNCH, memberName, orderedFamilyPreferences);
					planner.addMeal(day, memberName, MealType.LUNCH, food);
					pantry.removeMealFromInventory(food);
				}
				updateFamilyPreferenceMap(pantry, weightedPreferences, orderedFamilyPreferences);
				updateMemberPriorityList(weightedPreferences, memberPriorityList, orderedFamilyPreferences);
			}
			
		}

		planDinners(planner, pantry, familyMembers);

		/*simPrinter.println("\n\n\n********* PLANNER ********\n");
		//for(MealType meal : Food.getAllMealTypes()){
			simPrinter.println("LUNCH: ");
			for(Day day : Day.values()){
				simPrinter.println("\tDAY: " + day); 
				for(MemberName memberName : memberNames){
					simPrinter.println("\t\t"+ memberName + ": " + planner.getPlan().get(day).get(memberName).get(MealType.LUNCH));
				}
			}
		//}*/

		if(Player.hasValidPlanner(planner, originalPantry))
			return planner;
		return new Planner();
	}

	private void updateLunchPreferences(Integer week, Day thisDay, MemberName name, Planner planner, MealHistory mealHistory, Map<MemberName, Map<MealType, Map<FoodType, Double>>> orderedFamilyPreferences){
		int todayNumber = (week - 1) * 7 + new ArrayList<>(Arrays.asList(Day.values())).indexOf(thisDay) + 1;

		MealHistory weightedMealHistory = mealHistory;

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		Map<FoodType, Double> sortedLunchPreferences = new LinkedHashMap<>();

		//simPrinter.println("\n\nOLD \t\t" + name + ": " + orderedFamilyPreferences.get(name).get(MealType.LUNCH));

		for (Day day : plan.keySet())
			weightedMealHistory.addDailyFamilyMeal(week, day, name, plan.get(day).get(name));

		Map<Integer, Map<MemberName, Map<MealType, FoodType>>> dailyMealsMap = weightedMealHistory.getDailyFamilyMeals();

		for(FoodType food : orderedFamilyPreferences.get(name).get(MealType.LUNCH).keySet()){

			Integer lastServedDay = 0;
			for (Integer dayNumber : dailyMealsMap.keySet())
				if (dailyMealsMap.get(dayNumber).get(name).get(MealType.LUNCH) == food && dayNumber > lastServedDay)
					lastServedDay = dayNumber;	

			if (lastServedDay > 0){
				
				Integer daysAgo = (todayNumber - lastServedDay);
				Double scale = (double) daysAgo / (daysAgo + 1);

				//simPrinter.println("daysAgo:  " + daysAgo + "\nlastServedDay: " + lastServedDay + "\nTodaynumber: " + todayNumber + "\nThisDay: " + thisDay + "\nThisWeek:" +week+ "\nScale: " + scale);
				orderedFamilyPreferences.get(name)
							.get(MealType.LUNCH)
							.replace(food, orderedFamilyPreferences.get(name)
										 	       .get(MealType.LUNCH)
										               .get(food) * scale);

				
			}
		}
		orderedFamilyPreferences.get(name).get(MealType.LUNCH)
					.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
		  			.forEachOrdered(x -> sortedLunchPreferences.put(x.getKey(), x.getValue()));
		orderedFamilyPreferences.get(name).replace(MealType.LUNCH, sortedLunchPreferences);
		//simPrinter.println("NEW \t\t" + name + ": " + orderedFamilyPreferences.get(name).get(MealType.LUNCH));
	}

	private void planDinners(Planner planner, Pantry pantry, List<FamilyMember> familyMembers) {
		Day[] days = Day.values();

		for (int i = 0; i < 7; i++) {
			Map<FoodType, Integer> dinnerInventory = pantry.getMealsMap().get(MealType.DINNER);
			PriorityQueue<FoodScore> dinners = familyTracker.getDinnersByCompositeScore(i, dinnerInventory);
			FoodType dinner = dinners.poll().getFoodType();
			Integer quantity = dinnerInventory.get(dinner);
			Integer limit = Math.min(quantity, numFamilyMembers);
			for (int j = 0; j < limit; j++) {
				FamilyMember member = familyMembers.get(j);
				planner.addMeal(days[i], member.getName(), MealType.DINNER, dinner);
				pantry.removeMealFromInventory(dinner);
				familyTracker.getMemberTracker(member.getName()).prefTracker.addTempFood(dinner, i);
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

	private void addFoodToPlanner(Planner planner, FamilyMember member, FoodType foodType, int q) {
		int i = 0;
		for (Day day: Day.values()) {
			if (i < q) {
				planner.addMeal(day, member.getName(), Food.getMealType(foodType), foodType);
				i++;
			}
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