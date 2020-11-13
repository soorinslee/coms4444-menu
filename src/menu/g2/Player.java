package menu.g2;

import java.lang.reflect.Member;
import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Player extends menu.sim.Player {

	private int week;
	private int numFamilyMembers;
	private int pantrySize;
	private List<FamilyMember> familyMembers;
	private Pantry pantry;
	private MealHistory mealHistory;
	private Map<FamilyMember, List<FoodType>> breakfastRanks;
	private Map<FamilyMember, List<FoodType>> lunchRanks;
	private List<FoodType> dinnerRanks;
	private List<Integer> shoppingQuantities;
	private ShoppingList shoppingList;

	private Map<FamilyMember, List<FoodType>> breakfastAllocRanks;
	private Map<FamilyMember, List<FoodType>> lunchAllocRanks;
	private List<FoodType> dinnerAllocRanks;

	private Map<FamilyMember, List<FoodType>> breakfastShopRanks;
	private Map<FamilyMember, List<FoodType>> lunchShopRanks;
	private List<FoodType> dinnerShopRanks;

	private Planner currentPlanner;

	private List<FamilyMember> squeakyFamilyMembers;

	// for tomorrow:
	// Aum
	// todo: add more backups to shopping list (should always feed everyone)
	// Scott
	// lunch alloc ranks
	// change alloc to reference the alloc ranks (shouldn't always repeat meals)

	// Ahad
	// dinner alloc ranks

	// for friday:
	// todo: new squeakyFamilyMembers
	// how far should meal history go back?
	// pantry partitioning dynamic (small, medium, large pantry sizes)

	// actual todo list for friday:
	// shopping list which foods
	// shopping list, stocking up more for person who is least satisfied
	// guarantee that everyone will always be fed, guarentee pantry will have at
	// least #people*21

	// plan ahead week
	// plan ahead


	////todo: test ideal planner with targeting squeakiest
	////extremes: only look squeakiest for ideal planner
	////look at everyone's preferences

	////allocating first to lowest satisfied person

	////to do: stocking up for lowest x% of people

	///still buy what everyone wants, only stock up on lowest x squeakiest people

	////order what everyone wants, then do repeats for squeakiest people a lot


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

		if (this.week == 1) {
			this.squeakyFamilyMembers = this.familyMembers;
			this.breakfastAllocRanks = this.breakfastRanks;
			this.lunchAllocRanks = this.lunchRanks;
			this.dinnerAllocRanks = this.dinnerRanks;
		}
		calculateQuantities();
		return calculateShoppingList();
	}

	private Planner runSimulation() {
		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = this.pantry.clone();

		this.currentPlanner = new Planner(memberNames);
		for (FamilyMember member : this.familyMembers) {
			for (Day day : Day.values()) {
				FoodType maxAvailableBreakfastMeal = getMaximumAvailableBreakfastSim(originalPantry, MealType.BREAKFAST,
						member);
				// System.out.println("sizeeee here is " +
				// this.currentPlanner.getPlan().size());
				currentPlanner.addMeal(day, member.getName(), MealType.BREAKFAST, maxAvailableBreakfastMeal);
				FoodType maxAvailableLunchMeal = getMaximumAvailableLunchSim(originalPantry, MealType.LUNCH, member);
				currentPlanner.addMeal(day, member.getName(), MealType.LUNCH, maxAvailableLunchMeal);
			}
		}
		// System.out.println(this.currentPlanner.getPlan());
		for (Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal = getMaximumAvailableDinnerSim(originalPantry, MealType.DINNER);
			// System.out.println(maxAvailableDinnerMeal + " " +
			// pantry.getNumAvailableMeals(maxAvailableDinnerMeal));
			for (int i = 0; i < this.familyMembers.size(); i++) {
				MemberName memberName = memberNames.get(i);
				currentPlanner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
			}
		}
		//System.out.println(this.currentPlanner.getPlan().toString());
		return currentPlanner;
	}

	private FoodType getMaximumAvailableBreakfastSim(Pantry pantry, MealType mealType, FamilyMember member) {
		return this.breakfastRanks.get(member).get(0);
	}

	private FoodType getMaximumAvailableLunchSim(Pantry pantry, MealType mealType, FamilyMember member) {
		this.lunchShopRanks = new HashMap<FamilyMember, List<FoodType>>();
		// this.currentPlanner = new Planner();
		updateShopSimLunch();
		// System.out.println(this.lunchShopRanks.toString());
		return this.lunchShopRanks.get(member).get(0);
	}

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

			// double
		}

		this.dinnerShopRanks = new ArrayList<>(dinnerRanks);

		this.dinnerShopRanks.sort((dinner1, dinner2) -> (int) (100 * currentPrefAverages.get(dinner2))
				- (int) (100 * currentPrefAverages.get(dinner1)));
		return this.dinnerShopRanks.get(0);
	}

	/////// maybe at end?
	// TODO add dynamic partitioning
	private void calculateQuantities() {
		this.shoppingQuantities = new ArrayList<>();
		int minQuantity = 7 * this.numFamilyMembers;
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
	}

	private ShoppingList calculateShoppingList() {
		this.shoppingList = new ShoppingList();
		this.shoppingList.addLimit(MealType.BREAKFAST, this.shoppingQuantities.get(0));
		this.shoppingList.addLimit(MealType.LUNCH, this.shoppingQuantities.get(1));
		this.shoppingList.addLimit(MealType.DINNER, this.shoppingQuantities.get(2));

		calculateBreakfastRanks();
		calculateLunchRanks();
		calculateDinnerRanks();
		Planner sim = runSimulation();

		calculateBreakfastShoppingList(sim);

		// System.out.println("HERE");

		calculateLunchShoppingList(sim);
		calculateDinnerShoppingList(sim);

		if (Player.hasValidShoppingList(this.shoppingList, this.pantry.getNumEmptySlots())) {
			return this.shoppingList;
		}
		return new ShoppingList();
	}


	///////for low pantries should probably just load up on what squeakiest wants
	
	////to do: stocking up for lowest x% of people

	///still buy what everyone wants, only stock up on lowest x squeakiest people

	////order what everyone wants, then do repeats for squeakiest people a lot

	//////TODO: For Friday, Scott, Aum
	//////ideal planner week*# weeks pantry can fit - what's already in pantry
	//////if we get everything we want, then pantry would be filled
	private void calculateBreakfastShoppingList(Planner sim) {

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();
		double pantryWeeksSize = (double) pantrySize/(double) (numFamilyMembers*21);

		double smallCaseCutoff = 2;
		//how many in small case to focus on
		double topNFoods = 3;
		double topNFoodsFreqDivisor = 2;


		////small case: try to stockpile for pickiest eaters
		/*if(pantryWeeksSize < smallCaseCutoff) {
			//get top n foods for breakfast
			MemberName memberName = squeakyFamilyMembers.get(0).getName();
			System.out.println("here");
			for(int i = 0; i < topNFoods; i++) {
				System.out.println("i is " + i);
				for(int j = 0; j < pantrySize/topNFoodsFreqDivisor; i++) {
					System.out.println("j is " + j);
					System.out.println("rank is " + breakfastShopRanks.get(memberName));
					FoodType foodType = breakfastRanks.get(memberName).get(i);
					this.shoppingList.addToOrder(foodType);
				}
			}
			System.out.println("here2");

			////backups

		}

		/////large case try to satisfy everyone, stockpile for pickiest eaters
		else {*/

			////Part 1: add what everyone wants once
			HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.BREAKFAST);

			HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

			for(FoodType foodType : foodFreqs.keySet()) {
				int desiredFreqOneWeek = foodFreqs.get(foodType);

				//int desiredTotal = (int) Math.ceil(pantryWeeksSize*desiredFreqOneWeek);

				int currentAmount = this.pantry.getNumAvailableMeals(foodType);

				int difference = desiredFreqOneWeek - currentAmount;

				totalFoodFreqs.put(foodType, difference);
			}


			//desired
			for(FoodType foodType : totalFoodFreqs.keySet()) {
				for(int i = 0; i < totalFoodFreqs.get(foodType); i++) {
					this.shoppingList.addToOrder(foodType);
				}
			}

			///Part 2: stock up the rest of the pantry with the bottom 30% of pickiest people's preferences
			double percentile = .3;
			//repeat to fill pantry n-1 times
			for(int repeats = 0; repeats < pantryWeeksSize; repeats++) {
				for(int i = 0; i < Math.max(percentile*numFamilyMembers,1); i++) {
					MemberName memberName = this.squeakyFamilyMembers.get(i).getName();

					for(Day day: idealPlan.keySet()) {
						FoodType foodType = idealPlan.get(day).get(memberName).get(MealType.BREAKFAST);
						this.shoppingList.addToOrder(foodType);
					}	
				}
			}


			////part 3: backups: loop through the pickiest person's preferences, add .2*pantry/3 size for each value


			System.out.println("shopping list is " + this.shoppingList.getMealOrder(MealType.BREAKFAST));
		//}



		//////backups:

		//add what everyone wants once
		/*double pantryWeeksSize = (double) pantrySize/(double) (numFamilyMembers*21);

		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.BREAKFAST);

		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for(FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);

			int desiredTotal = (int) Math.ceil(pantryWeeksSize*desiredFreqOneWeek);

			int currentAmount = this.pantry.getNumAvailableMeals(foodType);

			int difference = desiredTotal - currentAmount;

			totalFoodFreqs.put(foodType, difference);
		}


		//desired
		for(FoodType foodType : totalFoodFreqs.keySet()) {
			for(int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}*/

		//backups

		
		
		/*for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0)/this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(0));
		}

		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0) / this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(1));
		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0) / this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(2));

		}*/
	}
  
	//////TODO: For Friday, Scott, Aum
	private void calculateLunchShoppingList(Planner sim) {

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();

		//add what everyone wants once
		double pantryWeeksSize = (double) pantrySize/(double) (numFamilyMembers*21);

		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.LUNCH);

		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for(FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);

			//int desiredTotal = (int) Math.ceil(pantryWeeksSize*desiredFreqOneWeek);

			int currentAmount = this.pantry.getNumAvailableMeals(foodType);

			int difference = desiredFreqOneWeek - currentAmount;

			totalFoodFreqs.put(foodType, difference);
		}


		//desired
		for(FoodType foodType : totalFoodFreqs.keySet()) {
			for(int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		double percentile = .3;
		//repeat to fill pantry n-1 times
		for(int repeats = 0; repeats < pantryWeeksSize; repeats++) {
			for(int i = 0; i < Math.max(percentile*numFamilyMembers,1); i++) {
				MemberName memberName = this.squeakyFamilyMembers.get(i).getName();

				for(Day day: idealPlan.keySet()) {
					FoodType foodType = idealPlan.get(day).get(memberName).get(MealType.LUNCH);
					this.shoppingList.addToOrder(foodType);
				}	
			}
		}

		System.out.println("shopping list is " + this.shoppingList.getMealOrder(MealType.LUNCH));

		/*double pantryWeeksSize = (double) pantrySize/(double) (numFamilyMembers*21);
		System.out.println("pantry size is " + pantrySize);
		System.out.println("pantry week size is " + pantryWeeksSize);

		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.LUNCH);

		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for(FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);

			int desiredTotal = (int) Math.ceil(pantryWeeksSize*desiredFreqOneWeek);
			
			System.out.println("FOOD: " + foodType);
			System.out.println("desired total is " + desiredTotal);

			int currentAmount = this.pantry.getNumAvailableMeals(foodType);
			System.out.println("pantry has " + currentAmount);

			int difference = desiredTotal - currentAmount;
			System.out.println("difference is " + difference);

			totalFoodFreqs.put(foodType, difference);
		}

		System.out.println("size is " + foodFreqs.size());

		for(FoodType foodType : foodFreqs.keySet()) {
			System.out.println("food:" + foodType + ", freq: " + foodFreqs.get(foodType));
			//System.out.println(", desiredFreqOneWeek: " + )
			System.out.println(", desiredTotal: " + totalFoodFreqs.get(foodType));
		}


		//desired
		for(FoodType foodType : totalFoodFreqs.keySet()) {
			for(int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		System.out.println("shopping list is " + this.shoppingList.getMealOrder(MealType.LUNCH));*/

		//backups
		//if pantry size is under a certain threshold, add backups
		


		/*int quantity = (int) Math.max(7, this.shoppingQuantities.get(1)/this.familyMembers.size());
		for (FamilyMember member : this.familyMembers) {

			// System.out.println(this.lunchRanks.get(member).toString());

			for (int i = 0; i < quantity; i++)
				this.shoppingList.addToOrder(this.lunchRanks.get(member).get(0));
		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < quantity; i++)
				this.shoppingList.addToOrder(this.lunchRanks.get(member).get(1));
		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < quantity; i++)
				this.shoppingList.addToOrder(this.lunchRanks.get(member).get(2));

		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < quantity; i++)
				this.shoppingList.addToOrder(this.lunchRanks.get(member).get(3));
		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < quantity; i++)
				this.shoppingList.addToOrder(this.lunchRanks.get(member).get(4));

		}*/
	}

	//////TODO: For Friday, Scott, Aum
	private void calculateDinnerShoppingList(Planner sim) {

		double pantryWeeksSize = (double) pantrySize/(double) (numFamilyMembers*21);

		HashMap<FoodType, Integer> foodFreqs = findFreqsFromPlanner(sim, MealType.DINNER);

		HashMap<FoodType, Integer> totalFoodFreqs = new HashMap<>();

		for(FoodType foodType : foodFreqs.keySet()) {
			int desiredFreqOneWeek = foodFreqs.get(foodType);

			int desiredTotal = (int) Math.ceil(pantryWeeksSize*desiredFreqOneWeek);

			int currentAmount = this.pantry.getNumAvailableMeals(foodType);

			int difference = desiredTotal - currentAmount;

			totalFoodFreqs.put(foodType, difference);
		}


		//desired
		for(FoodType foodType : totalFoodFreqs.keySet()) {
			for(int i = 0; i < totalFoodFreqs.get(foodType); i++) {
				this.shoppingList.addToOrder(foodType);
			}
		}

		//backups
		
		/*int quantity = (int) Math.max(7*this.familyMembers.size(), this.shoppingQuantities.get(2)/1.6);
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(0));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(1));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(2));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(3));

		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(4));*/

	}

	HashMap<FoodType, Integer> findFreqsFromPlanner(Planner sim, MealType mealType) {
		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();

		HashMap<FoodType, Integer> foodFreqs = new HashMap<>();

		for(Day day : idealPlan.keySet()) {
			for(MemberName member : idealPlan.get(day).keySet()) {
				FoodType meal = idealPlan.get(day).get(member).get(mealType);

				//if food already encountered, add 1
				if(foodFreqs.containsKey(meal)) {
					int freq = foodFreqs.get(meal);
					foodFreqs.put(meal, freq+1);
				}

				//otherwise make it one
				else {
					foodFreqs.put(meal, 1);
				}
			}
		}

		return foodFreqs;
	}

	/*HashMap<FamilyMember, HashMap<FoodType, Integer>> findFreqsPPFromPlanner(Planner sim, MealType mealType) {
		Map<Day, Map<MemberName, Map<MealType, FoodType>>> idealPlan = sim.getPlan();

		HashMap<FamilyMember, HashMap<FoodType, Integer>> freqsPerPerson = new HashMap<>();

		for(FamilyMember fm : this.familyMembers) {
			HashMap<FoodType, Integer> foodFreqs = new HashMap<>();
			freqs.put()
		}

		for()
	}*/


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

	public static List<FoodType> sortByValue(Map<FoodType, Double> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<FoodType, Double>> list = new LinkedList<Map.Entry<FoodType, Double>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<FoodType, Double>>() {
			public int compare(Map.Entry<FoodType, Double> o1, Map.Entry<FoodType, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// put data from sorted list to hashmap
		HashMap<FoodType, Double> temp = new LinkedHashMap<FoodType, Double>();
		for (Map.Entry<FoodType, Double> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return new ArrayList(temp.keySet());
	}

	public void updateSqueakyMembers() {
		if (week == 1) {
			return;
		}
		this.squeakyFamilyMembers = new ArrayList<>();
		Map<MemberName, Double> satisfactions = this.mealHistory.getAllAverageSatisfactions().get(this.week - 1);
		// System.out.println("SATS" + satisfactions.toString());
		List<MemberName> squeakyNames = sortBySatisfaction(satisfactions);
		// System.out.println("Here");
		for (MemberName memberName : squeakyNames) {
			for (FamilyMember f : this.familyMembers) {
				if (f.getName().equals(memberName)) {
					this.squeakyFamilyMembers.add(f);
					// System.out.println(f.getSatisfaction());
				}
			}
		}

		System.out.println(squeakyNames.toString());
		// System.out.println(this.squeakyFamilyMembers.toString());
	}

	public static List<MemberName> sortBySatisfaction(Map<MemberName, Double> hm) {
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
		// System.out.println(this.pantry.getAvailableFoodTypes(MealType.LUNCH).toString());
		System.out.println("Here");
		updateSqueakyMembers();

		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = pantry.clone();

		this.currentPlanner = new Planner(memberNames);
		// System.out.println("sizeeee here is " + currentPlanner.getPlan().size());

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
			// System.out.println(maxAvailableDinnerMeal + " " +
			// pantry.getNumAvailableMeals(maxAvailableDinnerMeal));
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

	private FoodType getMaximumAvailableLunch(Pantry pantry, MealType mealType, FamilyMember member) {
		updateLunchAlloc();

		Random r = new Random();
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

	// Scott
	private void updateBreakfastAlloc() {
		this.breakfastAllocRanks = this.breakfastRanks;
	}

	// Scott
	// find most recent time in past week that food was eaten:
	// look at this planner
	// if not in it,
	// look at last week
	// calc new value
	private void updateLunchAlloc() {
		this.lunchAllocRanks = new HashMap<FamilyMember, List<FoodType>>();

		for (FamilyMember familyMember : familyMembers) {
			// for each family member, calculate their current preferences
			HashMap<FoodType, Double> currentPreferences = new HashMap<>();

			// System.out.println(lunchRanks.get(familyMember));

			for (FoodType foodType : lunchRanks.get(familyMember)) {
				int daysAgo = lastEaten(foodType, familyMember, MealType.LUNCH);
				double factor = 1;
				// System.out.println("food type is " + foodType);

				if (daysAgo > 0) {
					// System.out.println("days ago is " + daysAgo);
					factor = (double) daysAgo / (double) (daysAgo + 1);
					// System.out.println("factor is " + factor);
				}

				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor * globalPreference;

				currentPreferences.put(foodType, currentPreference);
			}

			// sort lunches by current preference
			List<FoodType> lunches = new ArrayList<>(lunchRanks.get(familyMember));
			lunches.sort((lunch1, lunch2) -> (int) (100 * currentPreferences.get(lunch2))
					- (int) (100 * currentPreferences.get(lunch1)));

			/*
			 * for(FoodType lunch : lunches) { System.out.println(lunch + ", " +
			 * currentPreferences.get(lunch)); }
			 */

			// add currentPreference list to lunchAllocRanks
			this.lunchAllocRanks.put(familyMember, lunches);

		}
	}

	// look in planner and last week
	private int lastEaten(FoodType foodType, FamilyMember familyMember, MealType mealType) {
		// System.out.println();
		// System.out.println("WEEK: " + this.week + ", SEARCHING FOR MEAL: " +
		// foodType);

		// check in this planner
		// System.out.println("this week:");
		int daysAgoThisWeek = searchPlanner(foodType, familyMember, mealType, this.currentPlanner);

		// found in this planner
		if (daysAgoThisWeek > 0) {
			// System.out.println("TOTALLLLL: " + daysAgoThisWeek);
			return daysAgoThisWeek;
		}
		// System.out.println("HEREEEE");
		// check last week
		if (this.week > 1) {
			// System.out.println("last week:");
			Planner lastPlanner = this.mealHistory.getPlanner(week - 1);

			int daysAgoLastWeek = searchPlanner(foodType, familyMember, mealType, lastPlanner);

			if (daysAgoLastWeek > 0) {
				int daysAgo = daysAgoThisWeek * -1 + daysAgoLastWeek;
				return daysAgo;
			}
		}

		// System.out.println("TOTALLLL: NOT EITHER WEEKS");
		return -1;
	}

	int searchPlanner(FoodType foodType, FamilyMember familyMember, MealType mealType, Planner planner) {
		MemberName name = familyMember.getName();

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		int end = 7;

		if (plan.keySet().size() > 0) {
			// System.out.println("size is " + Day.values().length);

			for (int i = (Day.values().length - 1); i >= 0; i--) {
				// System.out.println("i is " + i);
				Day day = Day.values()[i];
				if (!plan.get(day).get(name).containsKey(mealType)) {
					end--;
					// System.out.println("not here");
				}

				// if the day has been planned and it is the sought after foodType
				else if (plan.get(day).get(name).get(mealType) == foodType) {
					// System.out.println("end is " + end);
					int start = i;
					// System.out.println("days ago is " + (end-start));
					return end - start;
				}
			}
		}
		// System.out.println(foodType + " not found");
		return -1 * end;
	}

	// Ahad
	// find most recent time in past week that food was eaten:
	// look at this planner
	// look through list
	// find how long ago food was eaten

	// update mean:
	// look through all people's preferences, multiply by factor, recalc mean

	// sorting all the means:
	// pass to sortByValue to sort dinners
	// set dinnerallocranks to that

	private void updateDinnerAlloc() {
		this.dinnerAllocRanks = new ArrayList<FoodType>(dinnerRanks);

		
		//HashMap<FoodType, Double> currentPreferences = new HashMap<>();

		//List<FoodType> pickiestPreferences = this.squeakyFamilyMembers.get(0);
		FamilyMember pickiestFamMember = this.squeakyFamilyMembers.get(0);
		//FamilyMember pickiestFamMember = this.squeakyFamilyMembers.get(squeakyFamilyMembers.size()-1);
		HashMap<FoodType, Double> currentPreferences = new HashMap<>();

		// System.out.println(lunchRanks.get(familyMember));

		for (FoodType foodType : this.dinnerRanks) {
			int daysAgo = lastEaten(foodType, pickiestFamMember, MealType.DINNER);
			double factor = 1;
			// System.out.println("food type is " + foodType);

			if (daysAgo > 0) {
				// System.out.println("days ago is " + daysAgo);
				factor = (double) daysAgo / (double) (daysAgo + 1);
				// System.out.println("factor is " + factor);
			}

			double globalPreference = pickiestFamMember.getFoodPreference(foodType);
			double currentPreference = factor * globalPreference;

			currentPreferences.put(foodType, currentPreference);
		}

		// sort dinners by current preference
		List<FoodType> dinners = new ArrayList<>(dinnerRanks);
		dinners.sort((dinner1, dinner2) -> (int) (100 * currentPreferences.get(dinner2))
				- (int) (100 * currentPreferences.get(dinner1)));

		/*
			* for(FoodType lunch : lunches) { System.out.println(lunch + ", " +
			* currentPreferences.get(lunch)); }
			*/

		// add currentPreference list to lunchAllocRanks
		this.dinnerAllocRanks = dinners;
	}

		
		/*HashMap<FoodType, Double> currentPrefAverages = new HashMap<>();
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

			// double
		}

		this.dinnerAllocRanks = new ArrayList<>(dinnerRanks);

		this.dinnerAllocRanks.sort((dinner1, dinner2) -> (int) (100 * currentPrefAverages.get(dinner2))
				- (int) (100 * currentPrefAverages.get(dinner1)));
	}*/

}