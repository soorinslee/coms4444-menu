package menu.g2;

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

	//for tomorrow:
	//Aum
	//todo: add more backups to shopping list (should always feed everyone)
	// Scott
	//lunch alloc ranks
	//change alloc to reference the alloc ranks (shouldn't always repeat meals)

	//Ahad
	//dinner alloc ranks

	//for friday:
	//todo: new squeakyFamilyMembers
	//how far should meal history go back?
	//pantry partitioning dynamic (small, medium, large pantry sizes)

	//actual todo list for friday:
	//shopping list which foods
	//shopping list, stocking up more for person who is least satisfied
	//guarantee that everyone will always be fed, guarentee pantry will have at least #people*21

	//plan ahead week
	//plan ahead 

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

		Planner sim = runSimulation();

		if(this.week == 1){
			this.squeakyFamilyMembers = this.familyMembers;
		}

		calculateQuantities();

		return calculateShoppingList(sim);
	}

	private Planner runSimulation(){
		return new Planner();
	}

	///////maybe at end?
	//TODO add dynamic partitioning
	private void calculateQuantities() {
		this.shoppingQuantities = new ArrayList<>();
		int minQuantity = 7 * this.numFamilyMembers;
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
		this.shoppingQuantities.add(this.pantry.getNumEmptySlots() / 3);
	}


	private ShoppingList calculateShoppingList(Planner sim) {
		this.shoppingList = new ShoppingList();
		this.shoppingList.addLimit(MealType.BREAKFAST, this.shoppingQuantities.get(0));
		this.shoppingList.addLimit(MealType.LUNCH, this.shoppingQuantities.get(1));
		this.shoppingList.addLimit(MealType.DINNER, this.shoppingQuantities.get(2));

		calculateBreakfastRanks();
		calculateLunchRanks();
		calculateDinnerRanks();

		calculateBreakfastShoppingList();

		// System.out.println("HERE");

		calculateLunchShoppingList();
		calculateDinnerShoppingList();

		if (Player.hasValidShoppingList(this.shoppingList, this.pantry.getNumEmptySlots())) {
			return this.shoppingList;
		}
		return new ShoppingList();
	}

	
	//////TODO: For Friday, Scott, Aum
	private void calculateBreakfastShoppingList() {
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0)/this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(0));
		}

		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0)/this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(1));
		}
		for (FamilyMember member : this.familyMembers) {
			for (int i = 0; i < this.shoppingQuantities.get(0)/this.familyMembers.size(); i++)
				this.shoppingList.addToOrder(this.breakfastRanks.get(member).get(2));

		}
	}

	//////TODO: For Friday, Scott, Aum
	private void calculateLunchShoppingList() {
		int quantity = (int) Math.max(7, this.shoppingQuantities.get(1)/this.familyMembers.size());
		for (FamilyMember member : this.familyMembers) {

			//System.out.println(this.lunchRanks.get(member).toString());

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

		}
	}

	//////TODO: For Friday, Scott, Aum
	private void calculateDinnerShoppingList() {
		int quantity = (int) Math.max(7*this.familyMembers.size(), this.shoppingQuantities.get(2)/1.6);
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(0));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(1));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(2));
		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(3));

		for (int i = 0; i < quantity; i++)
			shoppingList.addToOrder(this.dinnerRanks.get(4));

	}

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

	public void updateSqueakyMembers(){
		this.squeakyFamilyMembers = new ArrayList<>();
		Map<MemberName, Double> satisfactions = this.mealHistory.getAllSatisfactions().get(this.week-1);
		List<MemberName> squeakyNames = sortBySatisfaction(satisfactions);
		for(MemberName memberName: squeakyNames){
			for(FamilyMember f: this.familyMembers){
				if(f.getName().equals(memberName)){
					this.squeakyFamilyMembers.add(f);
				}
			}
		}
		//System.out.println(this.squeakyFamilyMembers.toString());
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
		//System.out.println(this.pantry.getAvailableFoodTypes(MealType.LUNCH).toString());
		updateSqueakyMembers();

		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = pantry.clone();

		this.currentPlanner = new Planner(memberNames);
		//System.out.println("sizeeee here is " + currentPlanner.getPlan().size());

		for (FamilyMember member : this.familyMembers) {


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
			// System.out.println(maxAvailableDinnerMeal + " " + pantry.getNumAvailableMeals(maxAvailableDinnerMeal));
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
		while(num < this.breakfastAllocRanks.get(member).size()){
			FoodType maximumAvailableMeal = this.breakfastAllocRanks.get(member).get(num);
			if(pantry.getNumAvailableMeals(maximumAvailableMeal) >= 1){
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)){
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
		while(num < this.lunchAllocRanks.get(member).size()){
			FoodType maximumAvailableMeal = this.lunchAllocRanks.get(member).get(num);
			if(pantry.getNumAvailableMeals(maximumAvailableMeal) >= 1){
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)){
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
		while(num < this.dinnerAllocRanks.size()){
			FoodType maximumAvailableMeal = this.dinnerAllocRanks.get(num);
			if(pantry.getNumAvailableMeals(maximumAvailableMeal) >= this.numFamilyMembers){
				return maximumAvailableMeal;
			} else {
				if (max < pantry.getNumAvailableMeals(maximumAvailableMeal)){
					maximumAvailableMealType = this.dinnerAllocRanks.get(num);
					max = pantry.getNumAvailableMeals(maximumAvailableMeal);
				}
			}
			num++;
		}
		return maximumAvailableMealType;
	}

	//Scott
	private void updateBreakfastAlloc() {
		this.breakfastAllocRanks = this.breakfastRanks;
	}

	//Scott
	//find most recent time in past week that food was eaten:
	//look at this planner
	//if not in it,
	//look at last week
	//calc new value
	private void updateLunchAlloc() {
		this.lunchAllocRanks = new HashMap<FamilyMember, List<FoodType>>();

		for(FamilyMember familyMember : familyMembers) {
			//for each family member, calculate their current preferences
			HashMap<FoodType, Double> currentPreferences = new HashMap<>();


		 	// System.out.println(lunchRanks.get(familyMember));

			for(FoodType foodType : lunchRanks.get(familyMember)) {
				int daysAgo = lastEaten(foodType, familyMember, MealType.LUNCH);
				double factor = 1;
				//System.out.println("food type is " + foodType);

				if(daysAgo > 0) {
					//System.out.println("days ago is " + daysAgo);
					factor = (double) daysAgo/ (double) (daysAgo+1);
					//System.out.println("factor is " + factor);
				}

				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor*globalPreference;
				
				currentPreferences.put(foodType, currentPreference);
			}

			//sort lunches by current preference
			List<FoodType> lunches = new ArrayList<>(lunchRanks.get(familyMember));
			lunches.sort((lunch1, lunch2) -> (int) (100*currentPreferences.get(lunch2)) - (int) (100*currentPreferences.get(lunch1)));

			/*for(FoodType lunch : lunches) {
				System.out.println(lunch + ", " + currentPreferences.get(lunch));
			}*/

			//add currentPreference list to lunchAllocRanks
			this.lunchAllocRanks.put(familyMember, lunches);

		}
	}

	//look in planner and last week
	private int lastEaten(FoodType foodType, FamilyMember familyMember, MealType mealType) {
		//System.out.println();
		//System.out.println("WEEK: " + this.week + ", SEARCHING FOR MEAL: " + foodType);

		//check in this planner
		//System.out.println("this week:");
		int daysAgoThisWeek = searchPlanner(foodType, familyMember, mealType, this.currentPlanner);

		//found in this planner
		if(daysAgoThisWeek > 0){
			//System.out.println("TOTALLLLL: " + daysAgoThisWeek);
			return daysAgoThisWeek;
		}

		//check last week
		if(this.week > 1) {
			//System.out.println("last week:");
			Planner lastPlanner = this.mealHistory.getPlanner(week-1);

			int daysAgoLastWeek = searchPlanner(foodType, familyMember, mealType, lastPlanner);

			if(daysAgoLastWeek > 0) {
				int daysAgo =  daysAgoThisWeek*-1 + daysAgoLastWeek;
				return daysAgo;
			}
		}

		//System.out.println("TOTALLLL: NOT EITHER WEEKS");
		return -1;
	}

	int searchPlanner(FoodType foodType, FamilyMember familyMember, MealType mealType, Planner planner) {
		MemberName name = familyMember.getName();

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		int end = 7;

		if(plan.keySet().size() > 0) {
			//System.out.println("size is " + Day.values().length);

			for(int i = (Day.values().length-1); i >= 0; i--) {
				//System.out.println("i is "  + i);
				Day day = Day.values()[i];
				if(!plan.get(day).get(name).containsKey(mealType)) {
					end--;
					//System.out.println("not here");
				}

				//if the day has been planned and it is the sought after foodType
				else if(plan.get(day).get(name).get(mealType) == foodType) {
					//System.out.println("end is " + end);
					int start = i;
					//System.out.println("days ago is " + (end-start));
					return end - start;
				}
			}
		}
		//System.out.println(foodType + " not found");
		return -1*end;
	}

	//Ahad
	//find most recent time in past week that food was eaten:
	//look at this planner
	//look through list
	//find how long ago food was eaten

	//update mean:
	//look through all people's preferences, multiply by factor, recalc mean

	//sorting all the means:
	//pass to sortByValue to sort dinners
	//set dinnerallocranks to that

	private void updateDinnerAlloc() {
		HashMap<FoodType, Double> currentPrefAverages = new HashMap<>();
		for(FoodType foodType : dinnerRanks) {
			int daysAgo = lastEaten(foodType, this.familyMembers.get(0), MealType.DINNER);

			double factor = 1;
			//calculate factor based on last eaten
			if(daysAgo > 0) {
				factor = (double) daysAgo/ (double) (daysAgo+1);
			}

			//calculate sum by adding all preferences*factor
			double sum = 2.0;

			for(FamilyMember familyMember : this.familyMembers) {
				double globalPreference = familyMember.getFoodPreference(foodType);
				double currentPreference = factor*globalPreference;

				sum += currentPreference;
			}

			currentPrefAverages.put(foodType, sum);

			//double 
		}

		this.dinnerAllocRanks = new ArrayList<>(dinnerRanks);

		this.dinnerAllocRanks.sort((dinner1, dinner2) -> (int) (100*currentPrefAverages.get(dinner2)) - (int) (100*currentPrefAverages.get(dinner1)));	
	}

}