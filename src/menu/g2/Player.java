package menu.g2;

import java.lang.reflect.Member;
import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Player extends menu.sim.Player {

	/* Basic simulator variables */
	private int week;
	private int numFamilyMembers;
	private int pantrySize;
	private List<FamilyMember> familyMembers;
	private Pantry pantry;
	private MealHistory mealHistory;

	/* Static ranking of satisfactions associated with each mealtype */
	private Map<FamilyMember, List<FoodType>> breakfastRanks;
	private Map<FamilyMember, List<FoodType>> lunchRanks;
	private List<FoodType> dinnerRanks;

	/* Cutoffs for pantry partitioning */
	private List<Integer> shoppingQuantities;

	/* Final Shopping List */
	private ShoppingList shoppingList;

	/*
	 * Dynamic ranking of satisfactions associated with each mealtype taking
	 * repetition into account
	 */
	private Map<FamilyMember, List<FoodType>> breakfastAllocRanks;
	private Map<FamilyMember, List<FoodType>> lunchAllocRanks;
	private List<FoodType> dinnerAllocRanks;

	/*
	 * Dynamic ranking of satisfactions associated with each mealtype taking
	 * repetition into account, used for simulator
	 */
	private Map<FamilyMember, List<FoodType>> breakfastShopRanks;
	private Map<FamilyMember, List<FoodType>> lunchShopRanks;
	private List<FoodType> dinnerShopRanks;

	/* Final planner used for both simulation and allocation */
	private Planner currentPlanner;

	/* Sorted list of picky family members */
	private List<FamilyMember> squeakyFamilyMembers;

	/**
	 * Player constructor
	 *
	 * @param weeks            number of weeks
	 * @param numFamilyMembers number of family members
	 * @param capacity         pantry capacity
	 * @param seed             random seed
	 * @param simPrinter       simulation printer
	 *
	 */
	public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
		super(weeks, numFamilyMembers, capacity, seed, simPrinter);
		this.numFamilyMembers = numFamilyMembers;
		this.breakfastRanks = new HashMap<>();
		this.lunchRanks = new HashMap<>();
		this.dinnerRanks = new ArrayList<>();
		this.pantrySize = capacity;
	}

	/**
	 * Create shopping list of meals to stock pantry
	 *
	 * @param week          current week
	 * @param numEmptySlots number of empty slots left in the pantry
	 * @param familyMembers all family members
	 * @param pantry        pantry inventory of remaining foods
	 * @param mealHistory   history of previous meal allocations
	 * @return shopping list of foods to order
	 *
	 */
	public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers,
			Pantry pantry, MealHistory mealHistory) {
		this.week = week;
		this.familyMembers = familyMembers;
		this.mealHistory = mealHistory;
		this.pantry = pantry;
		this.breakfastShopRanks = this.breakfastRanks;
		this.lunchShopRanks = this.lunchRanks;
		this.dinnerShopRanks = this.dinnerRanks;

		/*
		 * Prevent NullPointerExceptions since there is no history for dynamic ranking
		 */
		if (this.week == 1) {
			this.squeakyFamilyMembers = this.familyMembers;
			this.breakfastAllocRanks = this.breakfastRanks;
			this.lunchAllocRanks = this.lunchRanks;
			this.dinnerAllocRanks = this.dinnerRanks;
		}

		calculateQuantities();

		return calculateShoppingList();
	}

	/*
	 * Partition the pantry based on the mealtype by restricting how much of each
	 * meal is ordered. Partitions are tuned through testing.
	 */
	private void calculateQuantities() {
		this.shoppingQuantities = new ArrayList<>();
		// int minQuantity = Math.max(7 * this.numFamilyMembers,
		// this.pantry.getNumEmptySlots() / 4);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
	}

	/*
	 * Call helper methods for each mealtype to create a shopping list and run
	 * simulation to determine optimal shopping cart
	 */
	private ShoppingList calculateShoppingList() {
		this.shoppingList = new ShoppingList();
		this.shoppingList.addLimit(MealType.BREAKFAST, this.shoppingQuantities.get(0));
		this.shoppingList.addLimit(MealType.LUNCH, this.shoppingQuantities.get(1));
		this.shoppingList.addLimit(MealType.DINNER, this.shoppingQuantities.get(2));

		calculateBreakfastRanks();
		calculateLunchRanks();
		calculateDinnerRanks();
		
		if (this.week == 1) {
			this.squeakyFamilyMembers = this.familyMembers;
			this.breakfastAllocRanks = this.breakfastRanks;
			this.lunchAllocRanks = this.lunchRanks;
			this.dinnerAllocRanks = this.dinnerRanks;
		}

		Planner sim = runSimulation();

		calculateBreakfastShoppingList(sim);

		calculateLunchShoppingList(sim);
		calculateDinnerShoppingList(sim);

		if (Player.hasValidShoppingList(this.shoppingList, this.pantry.getNumEmptySlots())) {
			return this.shoppingList;
		}
		return new ShoppingList();
	}

	/*
	 * Calculate static rankings of breakfasts for each person and store in global
	 * variable
	 */
	private void calculateBreakfastRanks() {
		this.breakfastRanks = new HashMap<>();
		for (FamilyMember member : this.familyMembers) {

			List<FoodType> allBreakfasts = Food.getFoodTypes(MealType.BREAKFAST);
			Map<FoodType, Double> prefMap = new HashMap<>();
			for (FoodType food : allBreakfasts) {
				prefMap.put(food, member.getFoodPreference(food));
			}
			this.breakfastRanks.put(member, sortByValue(prefMap));
		}
	}

	/*
	 * Calculate static rankings of lunches for each person and store in global
	 * variable
	 */
	private void calculateLunchRanks() {
		this.lunchRanks = new HashMap<>();
		for (FamilyMember member : this.familyMembers) {

			List<FoodType> allLunches = Food.getFoodTypes(MealType.LUNCH);
			Map<FoodType, Double> prefMap = new HashMap<>();
			for (FoodType food : allLunches) {
				prefMap.put(food, member.getFoodPreference(food));
			}
			this.lunchRanks.put(member, sortByValue(prefMap));
		}
	}

	/*
	 * Calculate static rankings of dinner using a mean of the preferences. Only
	 * used for the first day since a picky eater isn't always easy to determine
	 * without allocating food.
	 */
	private void calculateDinnerRanks() {
		this.dinnerRanks = new ArrayList<>();
		List<FoodType> allDinners = Food.getFoodTypes(MealType.DINNER);
		HashMap<FoodType, Double> familyPreferences = new HashMap<>();
		for (FoodType food : allDinners) {
			double meanSatisfaction = 0.0;
			for (FamilyMember member : this.familyMembers) {
				meanSatisfaction += member.getFoodPreference(food);
			}
			meanSatisfaction /= this.numFamilyMembers;
			familyPreferences.put(food, meanSatisfaction);
		}
		this.dinnerRanks = (sortByValue(familyPreferences));
	}

	/*
	 * Sort a map of a member's food preferences by their satisfaction in descending
	 * order.
	 */
	private List<FoodType> sortByValue(Map<FoodType, Double> hm) {
		List<Map.Entry<FoodType, Double>> list = new LinkedList<Map.Entry<FoodType, Double>>(hm.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<FoodType, Double>>() {
			public int compare(Map.Entry<FoodType, Double> o1, Map.Entry<FoodType, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		HashMap<FoodType, Double> temp = new LinkedHashMap<FoodType, Double>();
		for (Map.Entry<FoodType, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return new ArrayList(temp.keySet());
	}

	/*
	 * Run a simulation of the allocation assuming an infinite pantry to determine
	 * the ideal shopping cart and pantry
	 */
	private Planner runSimulation() {
		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = this.pantry.clone();

		this.currentPlanner = new Planner(memberNames);

		// For each family member, for every day calculate the maximum breakfast and
		// lunch. Then add to planner.
		for (FamilyMember member : this.familyMembers) {
			for (Day day : Day.values()) {
				FoodType maxAvailableBreakfastMeal = getMaximumAvailableBreakfastSim(originalPantry, MealType.BREAKFAST,
						member);
				currentPlanner.addMeal(day, member.getName(), MealType.BREAKFAST, maxAvailableBreakfastMeal);
				FoodType maxAvailableLunchMeal = getMaximumAvailableLunchSim(originalPantry, MealType.LUNCH, member);
				currentPlanner.addMeal(day, member.getName(), MealType.LUNCH, maxAvailableLunchMeal);
			}
		}

		// For each day, calculate the maximum dinner and add to the planner.
		for (Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal = getMaximumAvailableDinnerSim(originalPantry, MealType.DINNER);
			for (int i = 0; i < this.familyMembers.size(); i++) {
				MemberName memberName = memberNames.get(i);
				currentPlanner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
			}
		}
		return currentPlanner;
	}

	/*
	 * Return highest rated breakfast from the static rankings since there are no
	 * penalites associated with breakfast.
	 */
	private FoodType getMaximumAvailableBreakfastSim(Pantry pantry, MealType mealType, FamilyMember member) {
		return this.breakfastRanks.get(member).get(0);
	}

	/*
	 * Return highest rated lunch from the dynamic rankings taking repetition into
	 * acccount.
	 */
	private FoodType getMaximumAvailableLunchSim(Pantry pantry, MealType mealType, FamilyMember member) {
		this.lunchShopRanks = new HashMap<FamilyMember, List<FoodType>>();
		updateShopSimLunch();
		return this.lunchShopRanks.get(member).get(0);
	}

	/*
	 * Update the lunch rankings to account for repetition in the simulator.
	 */
	private void updateShopSimLunch() {
		this.lunchShopRanks = this.lunchAllocRanks;
		for (FamilyMember familyMember : familyMembers) {
			HashMap<FoodType, Double> currentPreferences = new HashMap<>();
			for (FoodType foodType : lunchRanks.get(familyMember)) {
				int daysAgo = lastEaten(foodType, familyMember, MealType.LUNCH);

				double factor = 1;
				if (daysAgo > 0) {
					factor = (double) daysAgo / (double) (daysAgo + 1);
				}
				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor * globalPreference;
				currentPreferences.put(foodType, currentPreference);
			}
			// sort lunches by current preference
			List<FoodType> lunches = new ArrayList<>(lunchRanks.get(familyMember));
			lunches.sort((lunch1, lunch2) -> (int) (100 * currentPreferences.get(lunch2))
					- (int) (100 * currentPreferences.get(lunch1)));
			// add currentPreference list to lunchAllocRanks
			this.lunchShopRanks.put(familyMember, lunches);
		}
	}

	/*
	 * Update the dinner rankings to account for repetition in the simulator and
	 * target the least satisfied member.
	 */
	private FoodType getMaximumAvailableDinnerSim(Pantry pantry, MealType mealType) {
		HashMap<FoodType, Double> currentPrefAverages = new HashMap<>();
		for (FoodType foodType : dinnerRanks) {
			int daysAgo = lastEaten(foodType, this.familyMembers.get(0), MealType.DINNER);
			double factor = 1;
			// calculate factor based on last eaten
			if (daysAgo > 0) {
				factor = (double) daysAgo / (double) (daysAgo + 1);
			}
			// calculate sum by adding all preferences*factor
			double sum = 2.0;

			for (FamilyMember familyMember : this.familyMembers) {
				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor * globalPreference;
				sum += currentPreference;
			}
			currentPrefAverages.put(foodType, sum);
		}

		this.dinnerShopRanks = new ArrayList<>(dinnerRanks);

		this.dinnerShopRanks.sort((dinner1, dinner2) -> (int) (100 * currentPrefAverages.get(dinner2))
				- (int) (100 * currentPrefAverages.get(dinner1)));
		return this.dinnerShopRanks.get(0);
	}

	/*
	 * Calculate the breakfast shopping list, prioritizing the pickiest eaters and
	 * attempting to generate the ideal pantry calculated in the simulator.
	 */
	private void calculateBreakfastShoppingList(Planner sim) {

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();
		double pantryWeeksSize = (double) pantrySize / (double) (numFamilyMembers * 21);

		// Part 1: add what everyone wants the most once
		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.BREAKFAST);
		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for (FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);
			int currentAmount = this.pantry.getNumAvailableMeals(foodType);
			int difference = desiredFreqOneWeek - currentAmount;
			totalFoodFreqs.put(foodType, difference);
		}

		for (FoodType foodType : totalFoodFreqs.keySet()) {
			for (int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		/// Part 2: stock up the rest of the pantry with the bottom 30% of pickiest to
		/// prevent flipping and create consistent improvement. Number was tuned after
		/// testing.
		double percentile = .3;

		for (int repeats = 0; repeats < pantryWeeksSize; repeats++) {
			for (int i = 0; i < Math.max(percentile * numFamilyMembers, 1); i++) {
				MemberName memberName = this.squeakyFamilyMembers.get(i).getName();
				for (Day day : idealPlan.keySet()) {
					FoodType foodType = idealPlan.get(day).get(memberName).get(MealType.BREAKFAST);
					this.shoppingList.addToOrder(foodType);
				}
			}
		}

		// Part 3: last-minute resort to add the top 3 breakfasts in for each person
		// starting from the pickiest eater so that everyone is fed.
		for (FamilyMember member : this.squeakyFamilyMembers) {
			for (int i = 1; i < 3; i++) {
				FoodType foodType = this.breakfastAllocRanks.get(member).get(i);
				for (int j = 0; j < 0.3 * this.shoppingQuantities.get(0); j++)
					this.shoppingList.addToOrder(foodType);
			}
		}

	}

	/*
	 * Calculate the lunch shopping list, prioritizing the pickiest eaters and
	 * attempting to generate the ideal pantry calculated in the simulator.
	 */
	private void calculateLunchShoppingList(Planner sim) {

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();

		// Part 1: add what everyone wants the most once
		double pantryWeeksSize = (double) pantrySize / (double) (numFamilyMembers * 21);
		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.LUNCH);
		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for (FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);
			int currentAmount = this.pantry.getNumAvailableMeals(foodType);
			int difference = desiredFreqOneWeek - currentAmount;
			totalFoodFreqs.put(foodType, difference);
		}

		// desired
		for (FoodType foodType : totalFoodFreqs.keySet()) {
			for (int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		//// Part 2: stock up the rest of the pantry with the bottom 30% of pickiest to
		/// prevent flipping and create consistent improvement. Number was tuned after
		/// testing.
		double percentile = .3;
		for (int repeats = 0; repeats < pantryWeeksSize; repeats++) {
			for (int i = 0; i < Math.max(percentile * numFamilyMembers, 1); i++) {
				MemberName memberName = this.squeakyFamilyMembers.get(i).getName();
				for (Day day : idealPlan.keySet()) {
					FoodType foodType = idealPlan.get(day).get(memberName).get(MealType.LUNCH);
					this.shoppingList.addToOrder(foodType);
				}
			}
		}

		// Part 3: last-minute resort to add the top 3 lunches in for each person
		// starting from the pickiest eater so that everyone is fed.
		for (FamilyMember member : this.squeakyFamilyMembers) {
			for (int i = 1; i < 3; i++) {
				FoodType foodType = this.lunchAllocRanks.get(member).get(i);
				for (int j = 0; j < 0.3 * this.shoppingQuantities.get(0); j++)
					this.shoppingList.addToOrder(foodType);
			}
		}
	}

	/*
	 * Calculate the dinner shopping list, prioritizing the pickiest eaters and
	 * attempting to generate the ideal pantry calculated in the simulator.
	 */
	private void calculateDinnerShoppingList(Planner sim) {

		double pantryWeeksSize = (double) pantrySize / (double) (numFamilyMembers * 21);
		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.DINNER);
		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for (FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);
			int desiredTotal = (int) Math.ceil(pantryWeeksSize * desiredFreqOneWeek);
			int currentAmount = this.pantry.getNumAvailableMeals(foodType);
			int difference = desiredTotal - currentAmount;
			totalFoodFreqs.put(foodType, difference);
		}

		// Part 1: add what pickiest eater wants the most once
		for (FoodType foodType : totalFoodFreqs.keySet()) {
			for (int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		// Part 2: add every single dinner to the shopping list in descending order as a
		// failsafe.
		for (FoodType foodType : totalFoodFreqs.keySet()) {
			for (int j = 1; j < this.dinnerAllocRanks.size(); j++) {
				for (int i = 0; i < 3 * this.shoppingQuantities.get(2) / this.dinnerRanks.size(); i++) {
					this.shoppingList.addToOrder(this.dinnerAllocRanks.get(j));
				}
			}
		}
	}

	/*
	 * CDetermine how many times a foodtype appears in a planner to convert the
	 * ideal planner into a shopping list.
	 */
	HashMap<FoodType, Integer> findFreqsFromPlanner(Planner sim, MealType mealType) {
		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();

		HashMap<FoodType, Integer> foodFreqs = new HashMap<>();

		for (Day day : idealPlan.keySet()) {
			for (MemberName member : idealPlan.get(day).keySet()) {
				FoodType meal = idealPlan.get(day).get(member).get(mealType);

				// if food already encountered, add 1
				if (foodFreqs.containsKey(meal)) {
					int freq = foodFreqs.get(meal);
					foodFreqs.put(meal, freq + 1);
				}

				// otherwise make it one
				else {
					foodFreqs.put(meal, 1);
				}
			}
		}

		return foodFreqs;
	}

	/*
	 * Update the order of the squekiest members based on current mean
	 * satisfactions.
	 */
	private void updateSqueakyMembers() {
		if (week == 1) {
			return;
		}
		this.squeakyFamilyMembers = new ArrayList<>();
		Map<MemberName, Double> satisfactions = this.mealHistory.getAllAverageSatisfactions().get(this.week - 1);
		List<MemberName> squeakyNames = sortBySatisfaction(satisfactions);
		for (MemberName memberName : squeakyNames) {
			for (FamilyMember f : this.familyMembers) {
				if (f.getName().equals(memberName)) {
					this.squeakyFamilyMembers.add(f);
				}
			}
		}
	}

	/*
	 * Sort a map of the members and the average satisfaction in ascending order.
	 */
	private List<MemberName> sortBySatisfaction(Map<MemberName, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<MemberName, Double>> list = new LinkedList<Map.Entry<MemberName, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<MemberName, Double>>() {
			public int compare(Map.Entry<MemberName, Double> o1, Map.Entry<MemberName, Double> o2) {
				return (o1.getValue()).compareTo(o2.getValue());

			}
		});

		// put data from sorted list to hashmap
		HashMap<MemberName, Double> temp = new LinkedHashMap<MemberName, Double>();
		for (Map.Entry<MemberName, Double> aa : list) {

			temp.put(aa.getKey(), aa.getValue());
		}
		return new ArrayList(temp.keySet());
	}

	/**
	 * Plan meals
	 *
	 * @param week          current week
	 * @param familyMembers all family members
	 * @param pantry        pantry inventory of remaining foods
	 * @param mealHistory   history of previous meal allocations
	 * @return planner of assigned meals for the week
	 *
	 */
	public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {
		this.mealHistory = mealHistory;
		this.week = week;

		this.pantry = pantry;
		updateSqueakyMembers();

		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = pantry.clone();

		this.currentPlanner = new Planner(memberNames);
		for (FamilyMember member : this.squeakyFamilyMembers) {

			for (Day day : Day.values()) {
				FoodType maxAvailableBreakfastMeal = getMaximumAvailableBreakfast(pantry, MealType.BREAKFAST, member);
				if (pantry.getNumAvailableMeals(maxAvailableBreakfastMeal) > 0) {
					this.currentPlanner.addMeal(day, member.getName(), MealType.BREAKFAST, maxAvailableBreakfastMeal);
					pantry.removeMealFromInventory(maxAvailableBreakfastMeal);
				}
				FoodType maxAvailableLunchMeal = getMaximumAvailableLunch(pantry, MealType.LUNCH, member);
				if (pantry.getNumAvailableMeals(maxAvailableLunchMeal) > 0) {
					this.currentPlanner.addMeal(day, member.getName(), MealType.LUNCH, maxAvailableLunchMeal);
					pantry.removeMealFromInventory(maxAvailableLunchMeal);
				}
			}
		}
		for (Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal = getMaximumAvailableDinner(pantry, MealType.DINNER);

			Integer numDinners = Math.min(pantry.getNumAvailableMeals(maxAvailableDinnerMeal), familyMembers.size());
			for (int i = 0; i < numDinners; i++) {
				MemberName memberName = memberNames.get(i);
				this.currentPlanner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
				pantry.removeMealFromInventory(maxAvailableDinnerMeal);
			}
		}

		if (Player.hasValidPlanner(this.currentPlanner, originalPantry))
			return this.currentPlanner;
		return new Planner();
	}

	/*
	 * Calculate the maximum breakfast available in the pantry after updating the
	 * dynamic rankings.
	 */
	private FoodType getMaximumAvailableBreakfast(Pantry pantry, MealType mealType, FamilyMember member) {
		updateBreakfastAlloc();

		int num = 0;
		int max = 0;
		FoodType maximumAvailableMealType = this.breakfastAllocRanks.get(member).get(num);
		while (num < this.breakfastAllocRanks.get(member).size()) {
			FoodType maximumAvailableMeal = this.breakfastAllocRanks.get(member).get(num);
			if (pantry.getNumAvailableMeals(maximumAvailableMeal) >= 1) {
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)) {
					maximumAvailableMealType = this.breakfastAllocRanks.get(member).get(num);
					max = pantry.getNumAvailableMeals(maximumAvailableMeal);
				}
			}
			num++;
		}
		return maximumAvailableMealType;
	}

	/*
	 * Calculate the maximum lunch available in the pantry after updating the
	 * dynamic rankings.
	 */
	private FoodType getMaximumAvailableLunch(Pantry pantry, MealType mealType, FamilyMember member) {
		updateLunchAlloc();

		int num = 0;
		int max = 0;
		FoodType maximumAvailableMealType = this.lunchAllocRanks.get(member).get(num);
		while (num < this.lunchAllocRanks.get(member).size()) {
			FoodType maximumAvailableMeal = this.lunchAllocRanks.get(member).get(num);
			if (pantry.getNumAvailableMeals(maximumAvailableMeal) >= 1) {
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)) {
					maximumAvailableMealType = this.lunchAllocRanks.get(member).get(num);
					max = pantry.getNumAvailableMeals(maximumAvailableMeal);
				}
			}
			num++;
		}
		return maximumAvailableMealType;
	}

	/*
	 * Calculate the maximum dinner available to the most people in the pantry after
	 * updating the dynamic rankings.
	 */
	private FoodType getMaximumAvailableDinner(Pantry pantry, MealType mealType) {
		updateDinnerAlloc();

		int num = 0;
		int max = 0;
		FoodType maximumAvailableMealType = this.dinnerAllocRanks.get(num);
		while (num < this.dinnerAllocRanks.size()) {
			FoodType maximumAvailableMeal = this.dinnerAllocRanks.get(num);
			if (pantry.getNumAvailableMeals(maximumAvailableMeal) >= this.numFamilyMembers) {
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)) {
					maximumAvailableMealType = this.dinnerAllocRanks.get(num);
					max = pantry.getNumAvailableMeals(maximumAvailableMeal);
				}
			}
			num++;
		}
		return maximumAvailableMealType;
	}

	/*
	 * Update the dynamic rankings for breakfast.
	 */
	private void updateBreakfastAlloc() {
		this.breakfastAllocRanks = this.breakfastRanks;
	}

	/*
	 * Update the dynamic rankings for lunch for each family member by including the
	 * repetition penalty if it is relevant.
	 */
	private void updateLunchAlloc() {
		this.lunchAllocRanks = new HashMap<FamilyMember, List<FoodType>>();

		for (FamilyMember familyMember : familyMembers) {
			// for each family member, calculate their current preferences
			HashMap<FoodType, Double> currentPreferences = new HashMap<>();
			for (FoodType foodType : lunchRanks.get(familyMember)) {
				int daysAgo = lastEaten(foodType, familyMember, MealType.LUNCH);
				double factor = 1;
				if (daysAgo > 0) {
					factor = (double) daysAgo / (double) (daysAgo + 1);
				}

				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor * globalPreference;

				currentPreferences.put(foodType, currentPreference);
			}

			// sort lunches by current preference
			List<FoodType> lunches = new ArrayList<>(lunchRanks.get(familyMember));
			lunches.sort((lunch1, lunch2) -> (int) (100 * currentPreferences.get(lunch2))
					- (int) (100 * currentPreferences.get(lunch1)));

			// add currentPreference list to lunchAllocRanks
			this.lunchAllocRanks.put(familyMember, lunches);

		}
	}

	/*
	 * Calculate how many days ago in the last 1-2 weeks a meal was eaten using the
	 * most recent planner and the mealhistory.
	 */
	private int lastEaten(FoodType foodType, FamilyMember familyMember, MealType mealType) {
		int daysAgoThisWeek = searchPlanner(foodType, familyMember, mealType, this.currentPlanner);

		// found in current planner
		if (daysAgoThisWeek > 0) {
			return daysAgoThisWeek;
		}
		// check last week
		if (this.week > 1) {
			Planner lastPlanner = this.mealHistory.getPlanner(week - 1);

			int daysAgoLastWeek = searchPlanner(foodType, familyMember, mealType, lastPlanner);

			if (daysAgoLastWeek > 0) {
				int daysAgo = daysAgoThisWeek * -1 + daysAgoLastWeek;
				return daysAgo;
			}
		}
		return -1;
	}

	/*
	 * Search through a planner for the number of days ago a foodtype was eaten by a
	 * familymember.
	 */
	private int searchPlanner(FoodType foodType, FamilyMember familyMember, MealType mealType, Planner planner) {
		MemberName name = familyMember.getName();

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		int end = 7;

		if (plan.keySet().size() > 0) {
			for (int i = (Day.values().length - 1); i >= 0; i--) {
				Day day = Day.values()[i];
				if (!plan.get(day).get(name).containsKey(mealType)) {
					end--;
				}

				// if the day has been planned and it is the sought after foodType
				else if (plan.get(day).get(name).get(mealType) == foodType) {
					int start = i;
					return end - start;
				}
			}
		}
		return -1 * end;
	}

	/*
	 * Update the dynamic rankings for dinner including the
	 * repetition penalty if it is relevant.
	 */
	private void updateDinnerAlloc() {
		this.dinnerAllocRanks = new ArrayList<FoodType>(dinnerRanks);

		FamilyMember pickiestFamMember = this.squeakyFamilyMembers.get(0);

		HashMap<FoodType, Double> currentPreferences = new HashMap<>();

		for (FoodType foodType : this.dinnerRanks) {
			int daysAgo = lastEaten(foodType, pickiestFamMember, MealType.DINNER);
			double factor = 1;

			if (daysAgo > 0) {
				factor = (double) daysAgo / (double) (daysAgo + 1);
			}

			double globalPreference = pickiestFamMember.getFoodPreference(foodType);
			double currentPreference = factor * globalPreference;

			currentPreferences.put(foodType, currentPreference);
		}

		// sort dinners by current preference
		List<FoodType> dinners = new ArrayList<>(dinnerRanks);
		dinners.sort((dinner1, dinner2) -> (int) (100 * currentPreferences.get(dinner2))
				- (int) (100 * currentPreferences.get(dinner1)));

		this.dinnerAllocRanks = dinners;
	}
}