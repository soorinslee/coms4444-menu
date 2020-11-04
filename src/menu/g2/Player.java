package menu.g2;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class Player extends menu.sim.Player {

	int size = 0;
	List<FoodType> breakfastRanks; // weekly
	List<FoodType> lunchRanks;
	List<FoodType> dinnerRanks;

	int numBreakfasts = 0;
	int numLunches = 0;
	int numDinners = 0;

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

	}

	// option 1:
	// to calculate static ideal pantry:
	// 1.determine spread of how many dinners/lunches/breakfasts we should have
	// based on preferences
	// 2.rank breakfast, lunch, and dinner foods based on how much they are
	// generally liked
	// 3.calculate ideal shopping list based on this

	// how we're gonna order in the future
	// 4.order difference between current pantry and ideal list

	// option 2:
	// expected value
	// simulate pantry being used
	// simulate an expected value
	// search space, choose the ideal pantry

	// rankings based on history
	// longterm: tree of rankings, tree static
	// daily rankings for lunch, dinner

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

		// 1.) calculate how many breakfast, lunch, and dinner items we want based on
		// preferences
		System.out.println("HERE1");

		List<Integer> cutoffs = calcFreqMeals(pantry, size, familyMembers);
		numBreakfasts = cutoffs.get(0);
		numLunches = cutoffs.get(1);
		numDinners = cutoffs.get(2);
		System.out.println("HERE2");

		// 2.) rank breakfast, lunch, and dinner items
		breakfastRanks = calcOrderRanksBreakfast(familyMembers);
		lunchRanks = calcOrderRanksLunch(familyMembers);
		dinnerRanks = calcOrderRanksDinner(familyMembers);
		System.out.println("HERE3");

		return calcShoppingList(pantry, mealHistory, familyMembers);

		// TODO: Make these smart allocations
		/*
		 * int numBreakfastFoods = random.nextInt(numEmptySlots + 1); int numLunchFoods
		 * = random.nextInt(numEmptySlots - numBreakfastFoods + 1); int numDinnerFoods =
		 * numEmptySlots - numBreakfastFoods - numLunchFoods;
		 * 
		 * 
		 * int numBreakfastFoods = random.nextInt(numEmptySlots + 1); int numLunchFoods
		 * = random.nextInt(numEmptySlots - numBreakfastFoods + 1); int numDinnerFoods =
		 * numEmptySlots - numBreakfastFoods - numLunchFoods;
		 * 
		 * 
		 * ShoppingList shoppingList = new ShoppingList();
		 * shoppingList.addLimit(MealType.BREAKFAST, numBreakfastFoods);
		 * shoppingList.addLimit(MealType.LUNCH, numLunchFoods);
		 * shoppingList.addLimit(MealType.DINNER, numDinnerFoods);
		 * 
		 * List<FoodType> breakfastFoods = Food.getFoodTypes(MealType.BREAKFAST);
		 * List<FoodType> lunchFoods = Food.getFoodTypes(MealType.LUNCH); List<FoodType>
		 * dinnerFoods = Food.getFoodTypes(MealType.DINNER);
		 * 
		 * for(int i = 0; i < 2 * capacity; i++)
		 * shoppingList.addToOrder(MealType.BREAKFAST,
		 * breakfastFoods.get(random.nextInt(breakfastFoods.size()))); for(int i = 0; i
		 * < 2 * capacity; i++) shoppingList.addToOrder(MealType.LUNCH,
		 * lunchFoods.get(random.nextInt(lunchFoods.size()))); for(int i = 0; i < 2 *
		 * capacity; i++) shoppingList.addToOrder(MealType.DINNER,
		 * dinnerFoods.get(random.nextInt(dinnerFoods.size())));
		 * 
		 * if(Player.hasValidShoppingList(shoppingList, numEmptySlots)) return
		 * shoppingList;
		 * 
		 * return new ShoppingList();
		 */
	}


	private ShoppingList calcShoppingList(Pantry pantry, MealHistory mealHistory, List<FamilyMember> familyMembers) {
		//how many breakfast items
		List<FoodType> breakfasts = calcBreakfast(pantry, mealHistory);
		System.out.println(breakfasts.toString());
		//how many lunch items
		List<FoodType> lunches = calcLunch(pantry, mealHistory);
		//how many dinner items
		List<FoodType> dinners = calcDinner(pantry, mealHistory, familyMembers);

		//combine lists?
		return combineShoppingLists(breakfasts, lunches, dinners, pantry);
	}

	// Aum
	// TODO
	// 1.) calculate how many breakfast, lunch, and dinner items we want based on
	// preferences
	List<Integer> calcFreqMeals(Pantry pantry, int size, List<FamilyMember> familyMembers) {
		int breakfast = 7*familyMembers.size();
		int lunch = 7*familyMembers.size();
		int dinner = 7*familyMembers.size();

		double meanBreakfastSatisfaction = 0.0;
		double meanLunchSatisfaction = 0.0;
		double meanDinnerSatisfaction = 0.0;
		for(FamilyMember member: familyMembers){
			Map<FoodType, Double> foodPreferenceMap = member.getFoodPreferenceMap();
			for(Food.FoodType key: foodPreferenceMap.keySet()){
				Food f = new Food();
				if(f.getMealType(key) == MealType.BREAKFAST){
					meanBreakfastSatisfaction += foodPreferenceMap.get(key);
				} else if(f.getMealType(key) == MealType.LUNCH){
					meanLunchSatisfaction += foodPreferenceMap.get(key);
				} else {
					meanDinnerSatisfaction += foodPreferenceMap.get(key);
				}
			}
		}
		
		meanBreakfastSatisfaction = meanBreakfastSatisfaction/(10.0 * (double)familyMembers.size());
		meanLunchSatisfaction = meanLunchSatisfaction/(10.0 * (double)familyMembers.size());
		meanDinnerSatisfaction = meanDinnerSatisfaction/(20.0 * (double)familyMembers.size());

		int extraBreakfastAllocation = (int) ((size-breakfast)*(meanBreakfastSatisfaction/(meanBreakfastSatisfaction+meanLunchSatisfaction+meanDinnerSatisfaction)));
		int extraLunchAllocation = (int) ((size-lunch)*(meanLunchSatisfaction/(meanBreakfastSatisfaction+meanLunchSatisfaction+meanDinnerSatisfaction)));
		int extraDinnerAllocation = (int) ((size-dinner)*(meanDinnerSatisfaction/(meanBreakfastSatisfaction+meanLunchSatisfaction+meanDinnerSatisfaction)));

		breakfast+= extraBreakfastAllocation;
		lunch += extraLunchAllocation;
		dinner += extraDinnerAllocation;

		breakfast -= pantry.getNumAvailableMeals(MealType.BREAKFAST);
		lunch -= pantry.getNumAvailableMeals(MealType.LUNCH);
		dinner -= pantry.getNumAvailableMeals(MealType.DINNER);
		ArrayList<Integer> cutoffs = new ArrayList<Integer>();
		cutoffs.add(breakfast);
		cutoffs.add(lunch);
		cutoffs.add(dinner);
		
		return cutoffs;
	}

	// Ahad - Done by Aum
	// TODO
	// 2.) rank breakfast items
	// cereal, milk, oatmeal....
	// highest minimum
	// for each meal find lowest satisfaction
	// use that value to rank all the foods
	List<FoodType> calcOrderRanksBreakfast(List<FamilyMember> familyMembers) {
		Food f = new Food();
		List<FoodType> allBreakfasts = f.getFoodTypes(MealType.BREAKFAST);
		Map<FoodType, Double> lowestPerson = new HashMap<>();
		for(FoodType food: allBreakfasts){
			double lowest = 1.1;
			for(FamilyMember member: familyMembers){
				lowest = Math.min (member.getFoodPreference(food), lowest);
			}
			lowestPerson.put(food, lowest);
		}
		return sortByValue(lowestPerson);
	}

	// Ahad - Done by Aum
	// TODO
	// 2.) rank lunch items
	List<FoodType> calcOrderRanksLunch(List<FamilyMember> familyMembers) {
		Food f = new Food();
		List<FoodType> allBreakfasts = f.getFoodTypes(MealType.LUNCH);
		Map<FoodType, Double> lowestPerson = new HashMap<>();
		for(FoodType food: allBreakfasts){
			double lowest = 1.1;
			for(FamilyMember member: familyMembers){
				lowest = Math.min (member.getFoodPreference(food), lowest);
			}
			lowestPerson.put(food, lowest);
		}
		return sortByValue(lowestPerson);
	}

	// Ahad - Done by Aum
	// TODO
	// 2.) rank dinner items
	List<FoodType> calcOrderRanksDinner(List<FamilyMember> familyMembers) {
		Food f = new Food();
		List<FoodType> allBreakfasts = f.getFoodTypes(MealType.DINNER);
		Map<FoodType, Double> lowestPerson = new HashMap<>();
		for(FoodType food: allBreakfasts){
			double lowest = 1.1;
			for(FamilyMember member: familyMembers){
				lowest = Math.min (member.getFoodPreference(food), lowest);
			}
			lowestPerson.put(food, lowest);
		}
		return sortByValue(lowestPerson);
	}

	public static List<FoodType> sortByValue(Map<FoodType, Double> hm) 
    { 
        // Create a list from elements of HashMap 
        List<Map.Entry<FoodType, Double> > list = 
               new LinkedList<Map.Entry<FoodType, Double> >(hm.entrySet()); 
  
        // Sort the list 
        Collections.sort(list, new Comparator<Map.Entry<FoodType, Double> >() { 
            public int compare(Map.Entry<FoodType, Double> o1,  
                               Map.Entry<FoodType, Double> o2) 
            { 
                return (o1.getValue()).compareTo(o2.getValue()); 
            } 
        }); 
          
        // put data from sorted list to hashmap  
        HashMap<FoodType, Double> temp = new LinkedHashMap<FoodType, Double>(); 
        for (Map.Entry<FoodType, Double> aa : list) { 
            temp.put(aa.getKey(), aa.getValue()); 
        } 
        return new ArrayList(temp.keySet()); 
    } 

	//SCOTT
	//TODO
	//determine frequency for breakfast items
	//based on cutoffs, rankings
	//stick with highest ranking
	List<FoodType> calcBreakfast(Pantry pantry, MealHistory mealHistory) {
		//numBreakfasts
		int difference = numBreakfasts -  pantry.getNumAvailableMeals(MealType.BREAKFAST);

		Map<MealType, Map<FoodType, Integer>> map = pantry.getMealsMap();

		List<FoodType> breakfasts = new ArrayList<>();

		//target top 3 breakfasts
		for(int i = 0; i < 3; i++) {
			FoodType topFood = breakfastRanks.get(i);
			int freqTop = map.get(MealType.BREAKFAST).get(topFood);

			breakfasts = addFoods(breakfasts, topFood, (int) (difference/3*1.5));
		}

		for(int i = 4; i < breakfastRanks.size(); i++) {
			FoodType badFood = breakfastRanks.get(i);
			int freqBad = map.get(MealType.BREAKFAST).get(badFood);
			breakfasts = addFoods(breakfasts, badFood, (int) (difference/5*1.5));
		}

		//System.out.println(breakfasts);
		return breakfasts;
	}


	//SCOTT
	//TODO
	//determine frequency for lunch items
	//balance between top couple
	List<FoodType> calcLunch(Pantry pantry, MealHistory mealHistory) {
		int difference = numLunches -  pantry.getNumAvailableMeals(MealType.LUNCH);

		Map<MealType, Map<FoodType, Integer>> map = pantry.getMealsMap();

		List<FoodType> lunches = new ArrayList<>();

		//target top 5 top lunches
		for(int i = 0; i < 5; i++) {
			FoodType topFood = lunchRanks.get(i);
			int freqTop = map.get(MealType.LUNCH).get(topFood);

			lunches = addFoods(lunches, topFood, (int) (difference/5*1.3));
		}

		for(int i = 6; i < lunchRanks.size(); i++) {
			FoodType badFood = lunchRanks.get(i);
			int freqBad = map.get(MealType.LUNCH).get(badFood);

			lunches = addFoods(lunches, badFood, (int) (difference/7*1.2));
		}

		//System.out.println(lunches);
		return lunches;
		
	}

	//SCOTT
	//TODO
	//determine frequency for dinner items
	//multiples of the number of family members
	List<FoodType> calcDinner(Pantry pantry, MealHistory mealHistory, List<FamilyMember> familyMembers) {
		int numFamMembers = familyMembers.size();
		int difference = numDinners -  pantry.getNumAvailableMeals(MealType.DINNER);
		//System.out.println("difference is " + difference);
		//System.out.println("size is " + size);

		Map<MealType, Map<FoodType, Integer>> map = pantry.getMealsMap();

		List<FoodType> dinners = new ArrayList<>();

		//target top 5 top dinners
		for(int i = 0; i < 5; i++) {
			FoodType topFood = dinnerRanks.get(i);
			int freqTop = map.get(MealType.DINNER).get(topFood);



			dinners = addFoods(dinners, topFood, (int) (difference/5*1.3));
		}

		for(int i = 6; i < dinnerRanks.size(); i++) {
			FoodType badFood = dinnerRanks.get(i);
			int freqBad = map.get(MealType.DINNER).get(badFood);

			dinners = addFoods(dinners, badFood, (int) (difference/7*1.2));
		}

		//System.out.println(dinners);
		return dinners;
		
	}

	List<FoodType> addFoods(List<FoodType> li, FoodType food, int num) {
		List<FoodType> foods = new ArrayList<>(li);
		for(int i = 0; i < num; i++) {
			foods.add(food);
		}
		return foods;
	}

	//SCOTT
	//TODO
	//combine breakfast, lunch, and dinner shopping lists into one
	//take into account cutoffs -> generate shopping list
	//backup ones total to 7*2 or 7*3 of everything (pantry size/n)
	ShoppingList combineShoppingLists(List<FoodType> breakfasts, List<FoodType> lunches, List<FoodType> dinners, Pantry pantry) {
		ShoppingList shoppingList = new ShoppingList();

		//add cutoffs for how many of each meal type
    	shoppingList.addLimit(MealType.BREAKFAST, numBreakfasts -  pantry.getNumAvailableMeals(MealType.BREAKFAST));
    	shoppingList.addLimit(MealType.LUNCH, numLunches - pantry.getNumAvailableMeals(MealType.LUNCH));
		shoppingList.addLimit(MealType.DINNER, numDinners - pantry.getNumAvailableMeals(MealType.DINNER));
		
		//add all breakfast foods
		for(FoodType breakfast : breakfasts) {
			shoppingList.addToOrder(MealType.BREAKFAST, breakfast);
		}

		//add all lunch foods
		for(FoodType lunch : lunches) {
			shoppingList.addToOrder(MealType.LUNCH, lunch);
		}

		//add all dinner foods
		for(FoodType dinner : dinners) {
			shoppingList.addToOrder(MealType.DINNER, dinner);
		}

		return shoppingList;
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

	// TODOs
	// AUM

	public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

		// 1. randomly choose between top three meals only in lunch and dinner
		// get max available or second max available, remove from inventory, add to
		// planner

		List<MemberName> memberNames = new ArrayList<>();
		for (FamilyMember familyMember : familyMembers)
			memberNames.add(familyMember.getName());

		Pantry originalPantry = pantry.clone();

		Planner planner = new Planner(memberNames);
		for (MemberName memberName : memberNames) {
			for (Day day : Day.values()) {
				FoodType maxAvailableBreakfastMeal = getMaximumAvailableMeal(pantry, MealType.BREAKFAST, familyMembers);
				if (pantry.getNumAvailableMeals(maxAvailableBreakfastMeal) > 0) {
					planner.addMeal(day, memberName, MealType.BREAKFAST, maxAvailableBreakfastMeal);
					pantry.removeMealFromInventory(maxAvailableBreakfastMeal);
				}
				FoodType maxAvailableLunchMeal = FoodType.LUNCH1;
				try {
				 maxAvailableLunchMeal= getRandomAvailableMeal(pantry, MealType.DINNER);//this.dinnerRanks.get(getRandomAvailableMeal());
				} catch (Exception e){
				System.out.println("HERE4");
				}
				if (pantry.getNumAvailableMeals(maxAvailableLunchMeal) > 0) {
					planner.addMeal(day, memberName, MealType.LUNCH, maxAvailableLunchMeal);
					pantry.removeMealFromInventory(maxAvailableLunchMeal);
				}
			}
		}

		for (Day day : Day.values()) {
			FoodType maxAvailableDinnerMeal =  getRandomAvailableMeal(pantry, MealType.DINNER); //this.dinnerRanks.get(getRandomAvailableMeal());
			Integer numDinners = Math.min(pantry.getNumAvailableMeals(maxAvailableDinnerMeal), familyMembers.size());
			for (int i = 0; i < numDinners; i++) {
				MemberName memberName = memberNames.get(i);
				planner.addMeal(day, memberName, MealType.DINNER, maxAvailableDinnerMeal);
				pantry.removeMealFromInventory(maxAvailableDinnerMeal);
			}
		}

		if (Player.hasValidPlanner(planner, originalPantry))
			return planner;
		return new Planner();
	}

	private FoodType getRandomAvailableMeal(Pantry pantry, MealType mealType){
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
/*		Random rand = new Random();
		int randomNum = rand.nextInt(3);
		return randomNum; */
	}

	private FoodType getMaximumAvailableMeal(Pantry pantry, MealType mealType, List<FamilyMember> members){
		FoodType maximumAvailableMeal = FoodType.BREAKFAST1;
		int maxAvailableMeals = -1;
		int i =  0;
		while(maxAvailableMeals < members.size() && i < this.breakfastRanks.size()){
			if(pantry.getNumAvailableMeals(this.breakfastRanks.get(i))>= members.size()){
				return this.breakfastRanks.get(i);
			}
			i++;
		}
    	return maximumAvailableMeal;
/*		FoodType maximumAvailableMeal = null;
		int maxAvailableMeals = 21;
		for (FoodType foodType : Food.getFoodTypes(mealType)) {
			int ranking = this.breakfastRanks.indexOf(foodType);
			if (maxAvailableMeals > ranking) {
				maxAvailableMeals = ranking;
				maximumAvailableMeal = foodType;
			}
		}
		return maximumAvailableMeal; */
	}
}