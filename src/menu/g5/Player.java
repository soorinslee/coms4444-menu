package menu.g5;

import java.time.temporal.WeekFields;
import java.util.*;

import javax.sound.midi.MetaMessage;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;


public class Player extends menu.sim.Player {

	private final boolean PRINT_STATEMENTS = false;

	private final int BREAKFAST_TYPES = 10;
	private final int LUNCH_TYPES = 10;
	private final int DINNER_TYPES = 20;
	private final int WEEK_DAYS = 7;

	private HashMap<MemberName, List<FoodSatisfaction>> sortedBreakfastPreferences;
	private HashMap<MemberName, List<FoodSatisfaction>> sortedLunchPreferences;
	private HashMap<MemberName, List<FoodSatisfaction>> sortedDinnerPreferences;

	private HashMap<MemberName, HashMap<FoodType, Integer>> lastUsed;


	private HashMap<MemberName, List<FoodSatisfaction>> cloneMap(HashMap<MemberName, List<FoodSatisfaction>> inMap) {
		HashMap<MemberName, List<FoodSatisfaction>> outMap = new HashMap<>();

		for (MemberName key : inMap.keySet()) {
			ArrayList<FoodSatisfaction> satisfactionList = new ArrayList<>();
			for (FoodSatisfaction satisfaction : inMap.get(key)) {
				FoodSatisfaction newSatisfaction = new FoodSatisfaction(satisfaction.food, satisfaction.currentSatisfcation);
				satisfactionList.add(newSatisfaction);
			}
			outMap.put(key, satisfactionList);
		}
		return outMap;
	}

	private HashMap<MemberName, HashMap<FoodType, Integer>> cloneLastUsedMap(HashMap<MemberName, HashMap<FoodType, Integer>> inMap) {
		HashMap<MemberName, HashMap<FoodType, Integer>> outMap = new HashMap<>();

		for (MemberName key : inMap.keySet()) {
			HashMap<FoodType, Integer> tempMap = new HashMap<>();

			for (FoodType foodKey : Food.getAllFoodTypes()) {
				tempMap.put(foodKey, inMap.get(key).get(foodKey));
			}
			outMap.put(key, tempMap);
		}
		return outMap;
	}

	private void print(String s) {
		if (PRINT_STATEMENTS) {
			System.out.println(s);
		}
	}

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

		if (week == 1) {
			initialize(familyMembers);
		}
    	
		return stockInitialPantry(week, numEmptySlots, familyMembers, pantry, mealHistory);
	}

	private void initialize(List<FamilyMember> familyMembers) {

		sortedBreakfastPreferences = new HashMap<>();
		sortedLunchPreferences = new HashMap<>();
		sortedDinnerPreferences = new HashMap<>();

		// Add sorted meal preferences
		for (FamilyMember member : familyMembers) {
			sortedBreakfastPreferences.put(member.getName(), getSortedMealPreferences(MealType.BREAKFAST, member));
			sortedLunchPreferences.put(member.getName(), getSortedMealPreferences(MealType.LUNCH, member));
			sortedDinnerPreferences.put(member.getName(), getSortedMealPreferences(MealType.DINNER, member));
		}

		// Initialize hashMap with predicted 'lastUsed'
		lastUsed = new HashMap<>();
		for (FamilyMember member : familyMembers) {
			HashMap<FoodType, Integer> lastUsedByMember = new HashMap<>();

			for (FoodType type : Food.getAllFoodTypes()) {
				lastUsedByMember.put(type, Integer.MAX_VALUE - 1000); //1000 buffer to prevent int overflow
			}
			lastUsed.put(member.getName(), lastUsedByMember);
		}

	}

	private List<FoodSatisfaction> getSortedMealPreferences(MealType mealType, FamilyMember member) {
		ArrayList<FoodSatisfaction> mealSatisfactions = new ArrayList<>();

		for (FoodType foodType : Food.getFoodTypes(mealType)) {
			FoodSatisfaction foodSatisfaction = new FoodSatisfaction(foodType, member.getFoodPreference(foodType));
			mealSatisfactions.add(foodSatisfaction);
		}
		Collections.sort(mealSatisfactions);
		return mealSatisfactions;
	}
	
	// Stock pantry at week 1
	private ShoppingList stockInitialPantry(int week, int numEmptySlots,
											List<FamilyMember> familyMembers,
											Pantry pantry,
											MealHistory mealHistory) {

		// TODO: for now only add 21 * N_members breakfasts and lunches, and remaining - dinners
		int numBreakfastFoods = 7 * familyMembers.size();
		int numLunchFoods = 7 * familyMembers.size();
		int numDinnerFoods = numEmptySlots - numBreakfastFoods - numLunchFoods;

		ShoppingList shoppingList = new ShoppingList();
    	shoppingList.addLimit(MealType.BREAKFAST, numBreakfastFoods);
    	shoppingList.addLimit(MealType.LUNCH, numLunchFoods);
		shoppingList.addLimit(MealType.DINNER, numDinnerFoods);

		HashMap<MemberName, Double> satisfactionByMember = new HashMap<>();

		for (FamilyMember member : familyMembers) {
			satisfactionByMember.put(member.getName(), member.getSatisfaction());
		}
		
		// Add breakfasts

		for (int i = 0; i < BREAKFAST_TYPES; i++) {
			for (FamilyMember member : familyMembers) {
				FoodType foodType = sortedBreakfastPreferences.get(member.getName()).get(i).food;
				for (int j = 0; j < WEEK_DAYS; j++) {
					shoppingList.addToOrder(foodType);
					satisfactionByMember.put(member.getName(), satisfactionByMember.get(member.getName()) + member.getFoodPreference(foodType));
				}
			}
		}

		HashMap<MemberName, List<FoodSatisfaction>> tempSortedLunchPreferences = cloneMap(sortedLunchPreferences);
		HashMap<MemberName, List<FoodSatisfaction>> tempSortedDinnerPreferences = cloneMap(sortedDinnerPreferences);



		// Initialize hashMap with predicted 'lastUsed'
		HashMap<MemberName, HashMap<FoodType, Integer>> tempLastUsed = cloneLastUsedMap(lastUsed);

		for (int i = 0; i < WEEK_DAYS; i++) {
			for (FamilyMember member : familyMembers) {
				
				MemberName name = member.getName();
				
				// TODO: increment lastUsed by 1
				for (FoodType foodKey : Food.getAllFoodTypes()) {
					tempLastUsed.get(name).put(foodKey, tempLastUsed.get(name).get(foodKey) + 1);
				}
				
				// TODO: update preference with last used
				for (FoodSatisfaction satisfaction : tempSortedLunchPreferences.get(name)) {
					int lastTimeUsed = tempLastUsed.get(name).get(satisfaction.food);
					satisfaction.currentSatisfcation = member.getFoodPreference(satisfaction.food) * lastTimeUsed / (lastTimeUsed + 1);
				}

				for (FoodSatisfaction satisfaction : tempSortedDinnerPreferences.get(name)) {
					int lastTimeUsed = tempLastUsed.get(name).get(satisfaction.food);
					satisfaction.currentSatisfcation = member.getFoodPreference(satisfaction.food) * lastTimeUsed / (lastTimeUsed + 1);
				}

				Collections.sort(tempSortedLunchPreferences.get(name));
				Collections.sort(tempSortedDinnerPreferences.get(name));
			}

			// Add lunches

			for (FamilyMember member : familyMembers) {
				MemberName name = member.getName();

				FoodType favoriteFood = tempSortedLunchPreferences.get(member.getName()).get(0).food;
				int lastTimeUsed = tempLastUsed.get(member.getName()).get(favoriteFood);
				satisfactionByMember.put(name, satisfactionByMember.get(name) + member.getFoodPreference(favoriteFood) * lastTimeUsed / (lastTimeUsed + 1));
				tempLastUsed.get(member.getName()).put(favoriteFood, 0);

				shoppingList.addToOrder(favoriteFood);
			}

			// Add dinners

			MemberName leastHappy = familyMembers.get(0).getName();

			double lowestSatisfaction = Double.MAX_VALUE;
			for (FamilyMember member : familyMembers) {
				if (satisfactionByMember.get(member.getName()) < lowestSatisfaction) {
					leastHappy = member.getName();
					lowestSatisfaction = satisfactionByMember.get(leastHappy);
				}
			}

			FoodType favoriteDinner = tempSortedDinnerPreferences.get(leastHappy).get(0).food;
			
			for (int j = 0; j < familyMembers.size(); j++) {
				shoppingList.addToOrder(favoriteDinner);
			}

			for (FamilyMember member : familyMembers) {
				MemberName name = member.getName();
				int lastTimeUsed = tempLastUsed.get(member.getName()).get(favoriteDinner);
				satisfactionByMember.put(name, satisfactionByMember.get(name) + member.getFoodPreference(favoriteDinner) * lastTimeUsed / (lastTimeUsed + 1));
				tempLastUsed.get(member.getName()).put(favoriteDinner, 0);
			}


		}

		// Add more lunch and dinner items to the list to avoid missing items
		MemberName leastHappy = familyMembers.get(0).getName();
		
		double lowestSatisfaction = Double.MAX_VALUE;
		for (FamilyMember member : familyMembers) {
			if (satisfactionByMember.get(member.getName()) < lowestSatisfaction) {
				leastHappy = member.getName();
				lowestSatisfaction = satisfactionByMember.get(leastHappy);
			}
		}

		for (int i = 0; i < LUNCH_TYPES; i++) {
			FoodType foodType = sortedLunchPreferences.get(leastHappy).get(i).food;
			for (int j = 0; j < familyMembers.size(); j++) {
				shoppingList.addToOrder(foodType);
			}
		}

		for (int i = 0; i < DINNER_TYPES; i++) {
			FoodType foodType = sortedDinnerPreferences.get(leastHappy).get(i).food;
			for (int j = 0; j < familyMembers.size(); j++) {
				shoppingList.addToOrder(foodType);
			}
		}

		if(Player.hasValidShoppingList(shoppingList, numEmptySlots))
			return shoppingList;
		print("Invalid initial shopping list!");
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
			for(FamilyMember familyMember : familyMembers)
				memberNames.add(familyMember.getName());
			Planner planner = new Planner(memberNames);

			Pantry originalPantry = pantry.clone();

			ArrayList<MemberSatisfaction> membersSatisfaction = new ArrayList<>();
			
			for (FamilyMember member : familyMembers) {
				MemberSatisfaction currentMember = new MemberSatisfaction(member.getName(), member.getSatisfaction(), member);
				membersSatisfaction.add(currentMember);
			}



			for (Day day : Day.values()) {

				for (FamilyMember member : familyMembers) {
				
					MemberName name = member.getName();
					
					for (FoodType foodKey : Food.getAllFoodTypes()) {
						lastUsed.get(name).put(foodKey, lastUsed.get(name).get(foodKey) + 1);
					}
					
					for (FoodSatisfaction satisfaction : sortedLunchPreferences.get(name)) {
						int lastTimeUsed = lastUsed.get(name).get(satisfaction.food);
						satisfaction.currentSatisfcation = member.getFoodPreference(satisfaction.food) * lastTimeUsed / (lastTimeUsed + 1);
					}
	
					for (FoodSatisfaction satisfaction : sortedDinnerPreferences.get(name)) {
						int lastTimeUsed = lastUsed.get(name).get(satisfaction.food);
						satisfaction.currentSatisfcation = member.getFoodPreference(satisfaction.food) * lastTimeUsed / (lastTimeUsed + 1);
					}
	
					Collections.sort(sortedLunchPreferences.get(name));
					Collections.sort(sortedDinnerPreferences.get(name));
				}


				// Sort in increasing order
				Collections.sort(membersSatisfaction);


				// Breakfast
				for (MemberSatisfaction satisfaction : membersSatisfaction) {
					boolean hadBreakfast = false;
					int i = 0;

					while (!hadBreakfast && i < BREAKFAST_TYPES) {
						FoodType favoriteBreakfast = sortedBreakfastPreferences.get(satisfaction.name).get(i).food;
												
						if (pantry.containsMeal(favoriteBreakfast)) {
							pantry.removeMealFromInventory(favoriteBreakfast);
							planner.addMeal(day, satisfaction.name, MealType.BREAKFAST, favoriteBreakfast);
							hadBreakfast = true;
							satisfaction.satisfaction += satisfaction.member.getFoodPreference(favoriteBreakfast);
						}
						i++;
					}
					if (!hadBreakfast) {
						print("Error while allocating breakfast!");
					}
				}

				Collections.sort(membersSatisfaction);


				// Lunch
				for (MemberSatisfaction satisfaction : membersSatisfaction) {
					boolean hadLunch = false;
					int i = 0;

					while (!hadLunch && i < LUNCH_TYPES) {
						FoodType favoriteLunch = sortedLunchPreferences.get(satisfaction.name).get(i).food;
						
						if (pantry.containsMeal(favoriteLunch)) {
							pantry.removeMealFromInventory(favoriteLunch);
							planner.addMeal(day, satisfaction.name, MealType.LUNCH, favoriteLunch);
							hadLunch = true;
							int lastTimeUsed = lastUsed.get(satisfaction.name).get(favoriteLunch);
							satisfaction.satisfaction += satisfaction.member.getFoodPreference(favoriteLunch) * lastTimeUsed / (lastTimeUsed + 1);
							lastUsed.get(satisfaction.name).put(favoriteLunch, 0);
						}
						i++;
					}
					if (!hadLunch) {
						print("Error while allocating lunch!");
					}
				}
				Collections.sort(membersSatisfaction);


				// Dinner
				MemberSatisfaction leastSatisfication = membersSatisfaction.get(0);
				
				boolean hadDinner = false;
				int i = 0;

				while (!hadDinner && i < DINNER_TYPES) {
					FoodType favoriteDinner = sortedDinnerPreferences.get(leastSatisfication.name).get(i).food;
					
					// TODO: allow dinners lower than # of people
					if (pantry.getNumAvailableMeals(favoriteDinner) >= familyMembers.size()) {
						hadDinner = true;

						for (MemberSatisfaction satisfaction : membersSatisfaction) {
							planner.addMeal(day, satisfaction.name, MealType.DINNER, favoriteDinner);
							pantry.removeMealFromInventory(favoriteDinner);
							int lastTimeUsed = lastUsed.get(satisfaction.name).get(favoriteDinner);
							satisfaction.satisfaction += satisfaction.member.getFoodPreference(favoriteDinner) * lastTimeUsed / (lastTimeUsed + 1);
							lastUsed.get(satisfaction.name).put(favoriteDinner, 0);
						}
					}
					i++;
				}
				if (!hadDinner) {
					print("Error while allocating dinner!");
				}
			}

			

    	if(Player.hasValidPlanner(planner, originalPantry))
			return planner;
		print("Invalid planner!");
    	return new Planner();
	}

	public void printPlan(Planner p) {

		if (!PRINT_STATEMENTS) {
			return;
		}

    	Map<Day, Map<MemberName, Map<MealType, FoodType>>> m = p.getPlan();
    	for (Day d: m.keySet()) {
    		System.out.println("Day: "+d);
    		Map<MemberName, Map<MealType, FoodType>> m1 = m.get(d);
    		for (MemberName mn: m1.keySet()) {
    			System.out.println("MemberName: "+mn);
    			Map<MealType, FoodType> m2 = m1.get(mn);
    			System.out.println("Map: "+m2);
    		}
    		System.out.println();
    	}
    }
	
	private class FoodSatisfaction implements Comparable<FoodSatisfaction> {
		FoodType food;
		double currentSatisfcation;

		FoodSatisfaction(FoodType foodType, double satisfaction) {
			this.food = foodType;
			this.currentSatisfcation = satisfaction;
		}
		
		// Foods will be sorted in DECREASING order
		@Override
		public int compareTo(FoodSatisfaction other) {
			return Double.compare(other.currentSatisfcation, this.currentSatisfcation);
		}

		public String toString() {
			return this.food + ": " + this.currentSatisfcation;
		}
	}

	private class MemberSatisfaction implements Comparable<MemberSatisfaction> {
		MemberName name;
		double satisfaction;
		FamilyMember member;

		MemberSatisfaction(MemberName name, double satisfaction, FamilyMember member) {
			this.name = name;
			this.satisfaction = satisfaction;
			this.member = member;
		}

		@Override
		public int compareTo(MemberSatisfaction other) {
			return Double.compare(this.satisfaction, other.satisfaction);
		}

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
