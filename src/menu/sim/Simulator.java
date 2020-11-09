/*
    Project: Menu
    Course: COMS 4444 Programming & Problem Solving (Fall 2020)
    Instructor: Prof. Kenneth Ross
    URL: http://www.cs.columbia.edu/~kar/4444f20
    Author: Aditya Sridhar
    Simulator Version: 1.0
*/

package menu.sim;

import java.awt.Desktop;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.*;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import menu.sim.Food.MealType;
import menu.sim.Food.FoodType;

public class Simulator {
	
	// Simulator structures
	private static String teamName, configName;
	private static PlayerWrapper playerWrapper;
	private static MealHistory mealHistory;
	private static Pantry pantry;
	private static Shop shop;
	private static List<FamilyMember> familyMembers;
	private static Random random;
	
	// Simulator inputs
	private static int seed = 42;
	private static int weeks = 52;
	private static int numFamilyMembers = 3;
	private static int capacity = 100;
	private static double fpm = 15;
	private static boolean showGUI = false;
	private static boolean continuousGUI = true;
	private static boolean exportCSV = false;

	// Defaults
	private static boolean enablePrints = false;
	private static long timeout = 1000;
	private static int currentWeek = 0;
	private static String version = "1.0";
	private static String projectPath, sourcePath, staticsPath, mealsPath, plannersPath, pantriesPath, satisfactionPath;
    

	private static void setup() {
		mealHistory = new MealHistory();
		pantry = new Pantry(capacity);
		shop = new Shop();
		familyMembers = new ArrayList<>();
		random = new Random(seed);
		projectPath = new File(".").getAbsolutePath().substring(0, 
				new File(".").getAbsolutePath().indexOf("coms4444-menu") + "coms4444-menu".length());
		sourcePath = projectPath + File.separator + "src";
		staticsPath = projectPath + File.separator + "statics";
	}

	private static void parseCommandLineArguments(String[] args) throws IOException {
		for(int i = 0; i < args.length; i++) {
            switch (args[i].charAt(0)) {
                case '-':
                    if(args[i].equals("-t") || args[i].equals("--team")) {
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The team name is missing!");
                        teamName = args[i];
                    }
                    else if(args[i].equals("-g") || args[i].equals("--gui"))
                        showGUI = true;
                    else if(args[i].equals("-l") || args[i].equals("--log")) {
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The log file path is missing!");
                        Log.setLogFile(args[i]);
                        Log.assignLoggingStatus(true);
                    }
                    else if(args[i].equals("-v") || args[i].equals("--verbose"))
                        Log.assignVerbosityStatus(true);
                    else if(args[i].equals("-f") || args[i].equals("--fpm")) {
                    	i++;
                        if(i == args.length)
                            throw new IllegalArgumentException("The GUI frames per minute is missing!");
                        fpm = Double.parseDouble(args[i]);
                    }
                    else if(args[i].equals("-C") || args[i].equals("--capacity")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The pantry capacity is missing!");
                        capacity = Integer.parseInt(args[i]);
                        pantry = new Pantry(capacity);
                    }
                    else if(args[i].equals("-m") || args[i].equals("--config")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The configuration file is missing!");
                        configName = args[i];
                    }
                    else if(args[i].equals("-c") || args[i].equals("--continuous"))
                        continuousGUI = true;
                    else if(args[i].equals("-d") || args[i].equals("--discrete"))
                        continuousGUI = false;
                    else if(args[i].equals("-s") || args[i].equals("--seed")) {
                    	i++;
                        if(i == args.length) 
                            throw new IllegalArgumentException("The seed number is missing!");
                        seed = Integer.parseInt(args[i]);
                        random = new Random(seed);
                    }
                    else if(args[i].equals("-w") || args[i].equals("--weeks")) {
                    	i++;
                        if(i == args.length)
                            throw new IllegalArgumentException("The number of weeks is not specified!");
                        weeks = Integer.parseInt(args[i]);
                    }
                    else if(args[i].equals("-p") || args[i].equals("--people")) {
                    	i++;
                        if(i == args.length)
                            throw new IllegalArgumentException("The number of people is not specified!");
                        numFamilyMembers = Integer.parseInt(args[i]);
                    }
                    else if(args[i].equals("-e") || args[i].equals("--export")) {
                    	exportCSV = true;
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The CSV filepath for assigned meals is missing!");
                    	mealsPath = args[i];
                        i++;
                    	if(i == args.length)
                            throw new IllegalArgumentException("The CSV filepath for planners is missing!");
                    	plannersPath = args[i];
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The CSV filepath for pantries is missing!");
                    	pantriesPath = args[i];
                        i++;
                    	if(i == args.length) 
                            throw new IllegalArgumentException("The CSV filepath for satisfactions is missing!");
                    	satisfactionPath = args[i];
                    }
                    else 
                        throw new IllegalArgumentException("Unknown argument \"" + args[i] + "\"!");
                    break;
                default:
                    throw new IllegalArgumentException("Unknown argument \"" + args[i] + "\"!");
            }
        }
		
		if(configName == null)
			throw new IOException("You must specify a configuration file.");
		
		if(capacity < 21 * numFamilyMembers)
            throw new IOException("The pantry capacity is insufficient to feed all family members for the week!");		
			
		Log.writeToLogFile("\n");
        Log.writeToLogFile("Project: Menu");
        Log.writeToLogFile("Simulator Version: " + version);
        Log.writeToLogFile("Team: " + teamName);
        Log.writeToLogFile("GUI: " + (showGUI ? "enabled" : "disabled"));
        Log.writeToLogFile("\n");
	}
	
	private static void readConfiguration() throws FileNotFoundException, IOException {
		if(configName != null) {
			File configFile;
			Scanner scanner;
			try {
				configFile = new File(sourcePath + File.separator + "configs" + File.separator + teamName + File.separator + configName);
				scanner = new Scanner(configFile);
			} catch(FileNotFoundException e) {
                throw new FileNotFoundException("Configuration file was not found!");
			}

			try {
				FoodType[] foodTypes = {
						FoodType.BREAKFAST1, FoodType.BREAKFAST2, FoodType.BREAKFAST3, FoodType.BREAKFAST4, FoodType.BREAKFAST5,
						FoodType.BREAKFAST6, FoodType.BREAKFAST7, FoodType.BREAKFAST8, FoodType.BREAKFAST9, FoodType.BREAKFAST10,
						FoodType.LUNCH1, FoodType.LUNCH2, FoodType.LUNCH3, FoodType.LUNCH4, FoodType.LUNCH5,
						FoodType.LUNCH6, FoodType.LUNCH7, FoodType.LUNCH8, FoodType.LUNCH9, FoodType.LUNCH10,
						FoodType.DINNER1, FoodType.DINNER2, FoodType.DINNER3, FoodType.DINNER4, FoodType.DINNER5,
						FoodType.DINNER6, FoodType.DINNER7, FoodType.DINNER8, FoodType.DINNER9, FoodType.DINNER10,
						FoodType.DINNER11, FoodType.DINNER12, FoodType.DINNER13, FoodType.DINNER14, FoodType.DINNER15,
						FoodType.DINNER16, FoodType.DINNER17, FoodType.DINNER18, FoodType.DINNER19, FoodType.DINNER20
				};
				
				List<MemberName> memberNames = new ArrayList<>(Arrays.asList(MemberName.values()));
				
				for(int i = 0; i < numFamilyMembers; i++) {
					if(!scanner.hasNextLine())
		                throw new IOException("The number of lines in the configuration file should be at least the number of family members!");
					
					FamilyMember familyMember = new FamilyMember(memberNames.get(i % numFamilyMembers));
					
					String[] linePreferences = scanner.nextLine().strip().split(" ");
					if(linePreferences.length != foodTypes.length)
		                throw new IOException("Not all food preferences are listed for all family members!");
					
					for(int j = 0; j < linePreferences.length; j++) {
						try {
							Double preference = Double.parseDouble(linePreferences[j]);
							if(preference < 0 || preference > 1)
								throw new IOException("Food preferences must be between 0 and 1, inclusive.");
							
							familyMember.setFoodPreference(foodTypes[j], preference);

						} catch(Exception e) {
							throw new IOException("Food preferences must be decimal values.");
						}
					}
					
					familyMembers.add(familyMember);
				}
			} catch(Exception e) {
				scanner.close();
                throw new IOException("Cannot interpret one or more lines of the configuration file!");
			}
			
			scanner.close();
			
			try {
	        	playerWrapper = loadPlayerWrapper();
			} catch (Exception e) {
				Log.writeToLogFile("Unable to load player: " + e.getMessage());
			}
		}
	}

	private static void runSimulation() throws IOException, JSONException {
		
		HTTPServer server = null;
		if(showGUI) {
            server = new HTTPServer();
            Log.writeToLogFile("Hosting the HTTP Server on " + server.addr());
            if(!Desktop.isDesktopSupported())
                Log.writeToLogFile("Desktop operations not supported!");
            else if(!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
                Log.writeToLogFile("Desktop browse operation not supported!");
            else {
                try {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + server.port()));
                } catch(URISyntaxException e) {}
            }
        }
		
		for(int i = 1; i <= weeks; i++) {
			currentWeek = i;
			
			updateShop();
						
			int numEmptySlots = pantry.getNumEmptySlots();
		    ShoppingList shoppingList = playerWrapper.stockPantry(
		    		currentWeek,
		    		numEmptySlots,
		    		deepClone(familyMembers),
		    		deepClone(pantry),
		    		deepClone(mealHistory)
		    );
		    	    
		    if(!Player.hasValidShoppingList(shoppingList, numEmptySlots)) {
		    	Log.writeToLogFile("The shopping list from week " + currentWeek + " is invalid! The pantry is not being restocked.");
		    	shoppingList = new ShoppingList();
		    }
	    	updatePantry(shoppingList);
	
		    Planner planner = playerWrapper.planMeals(
		    		currentWeek,
		    		deepClone(familyMembers),
		    		deepClone(pantry),
		    		deepClone(mealHistory)
		    );

		    Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();

		    List<MemberName> memberNames = new ArrayList<>();
		    for(FamilyMember familyMember : familyMembers)
		    	memberNames.add(familyMember.getName());

		    Planner finalPlanner = new Planner(memberNames);
		    for(Day day : plan.keySet()) {
		    	Map<MemberName, Map<MealType, FoodType>> dayPlan = plan.get(day);
		    	for(MemberName memberName : memberNames) {
		    		if(!dayPlan.containsKey(memberName))
		    			continue;
		    		for(MealType mealType : dayPlan.get(memberName).keySet()) {
		    			FoodType foodType = dayPlan.get(memberName).get(mealType);
			    		finalPlanner.addMeal(day, memberName, mealType, foodType);
		    		}
		    	}
		    }
		    
		    if(!Player.hasValidPlanner(finalPlanner, pantry)) {
		    	Log.writeToLogFile("The planner from week " + currentWeek + " is invalid! Family members starve this week.");
	    		finalPlanner = new Planner(memberNames);
		    }

		    updateAssignedMeals(finalPlanner);
		    updateMealHistory(currentWeek, shoppingList, finalPlanner);
		    updateSatisfactions(currentWeek, finalPlanner);
		    updateAverageSatisfactions(currentWeek);
		    
		    if(currentWeek == weeks) {
				double leastAverageSatisfaction = getLeastAverageSatisfaction(weeks);
				MemberName leastSatisfiedMember = getLeastSatisfiedMember(weeks);

				DecimalFormat satisfactionFormat = new DecimalFormat("###.####");
				
				Log.writeToLogFile("");
				Log.writeToLogFile("Weeks: " + weeks);
				Log.writeToLogFile("Capacity: " + capacity);
				Log.writeToLogFile("Family members: " + numFamilyMembers);
				Log.writeToLogFile("Seed: " + seed);
				Log.writeToLogFile("Configuration: " + configName);
				Log.writeToLogFile("Average satisfaction of least satisfied member: " + satisfactionFormat.format(leastAverageSatisfaction));
				Log.writeToLogFile("Least satisfied member: " + leastSatisfiedMember.name().substring(0, 1).toUpperCase() + leastSatisfiedMember.name().substring(1).toLowerCase());
						
				if(exportCSV) {
					List<List<String>> plannersRows = new ArrayList<>();
					for(int week : mealHistory.getAllPlanners().keySet()) {
						Planner currentPlanner = mealHistory.getAllPlanners().get(week);
						for(Day day : currentPlanner.getPlan().keySet()) {
							Map<MemberName, Map<MealType, FoodType>> memberMap = currentPlanner.getPlan().get(day);
							for(MemberName memberName : memberMap.keySet()) {
								Map<MealType, FoodType> mealMap = memberMap.get(memberName);
								for(MealType mealType : mealMap.keySet()) {
									List<String> row = Arrays.asList(Integer.toString(week),
											 day.name(),
											 memberName.name(),
											 mealType.name(),
											 mealMap.get(mealType).name());
									plannersRows.add(row);
								}
							}
						}
					}
					
					FileWriter csvWriter = new FileWriter(plannersPath);
					csvWriter.append("Week,Day,Member,Meal Type,Meal\n");
					for(List<String> row : plannersRows)
						csvWriter.append(String.join(",", row) + "\n");
					csvWriter.flush();
					csvWriter.close();
				
					List<List<String>> pantriesRows = new ArrayList<>();
					for(int week : mealHistory.getAllPantries().keySet()) {
						Pantry pantry = mealHistory.getAllPantries().get(week);
						for(MealType mealType : pantry.getMealsMap().keySet()) {
							Map<FoodType, Integer> countMap = pantry.getMealsMap().get(mealType);
							for(FoodType foodType : countMap.keySet()) {
								List<String> row = Arrays.asList(Integer.toString(week),
										 mealType.name(),
										 foodType.name(),
										 Integer.toString(countMap.get(foodType)));
								pantriesRows.add(row);
							}
						}
					}
				
					csvWriter = new FileWriter(pantriesPath);
					csvWriter.append("Week,Meal Type,Meal,Quantity\n");
					for(List<String> row : pantriesRows)
						csvWriter.append(String.join(",", row) + "\n");
					csvWriter.flush();
					csvWriter.close();

					List<List<String>> satisfactionRows = new ArrayList<>();
					for(int week : mealHistory.getAllSatisfactions().keySet()) {
						Map<MemberName, Double> satisfactionMap = mealHistory.getAllSatisfactions().get(week);
						Map<MemberName, Double> averageSatisfactionMap = mealHistory.getAllAverageSatisfactions().get(week);
						for(MemberName memberName : satisfactionMap.keySet()) {
							List<String> row = Arrays.asList(Integer.toString(week),
									 memberName.name(),
									 Double.toString(satisfactionMap.get(memberName)),
									 Double.toString(averageSatisfactionMap.get(memberName)));
							satisfactionRows.add(row);
						}
					}
				
					csvWriter = new FileWriter(satisfactionPath);
					csvWriter.append("Week,Member,Satisfaction,Average Satisfaction\n");
					for(List<String> row : satisfactionRows)
						csvWriter.append(String.join(",", row) + "\n");
					csvWriter.flush();
					csvWriter.close();

					List<List<String>> mealsRows = new ArrayList<>();
					for(int day : mealHistory.getDailyFamilyMeals().keySet()) {
						Map<MemberName, Map<MealType, FoodType>> memberMap = mealHistory.getDailyFamilyMeals().get(day);				
						for(MemberName memberName : memberMap.keySet()) {
							Map<MealType, FoodType> mealMap = memberMap.get(memberName);
							for(MealType mealType : mealMap.keySet()) {
								List<String> row = Arrays.asList(Integer.toString(day),
										 memberName.name(),
										 mealType.name(),
										 mealMap.get(mealType).name());
								mealsRows.add(row);	
							}
						}
					}
					
					csvWriter = new FileWriter(mealsPath);
					csvWriter.append("Day,Member,Meal Type,Meal\n");
					for(List<String> row : mealsRows)
						csvWriter.append(String.join(",", row) + "\n");
					csvWriter.flush();
					csvWriter.close();
				}
		    }
		    
		    if(showGUI) {
		    	if(currentWeek == weeks)
		    		while(true)
						updateGUI(server, getGUIState(currentWeek, shoppingList));
		    	else
					updateGUI(server, getGUIState(currentWeek, shoppingList));
		    }
		}
				
		if(!showGUI)
			System.exit(1);
	}
		
	private static void updateShop() {
		List<FoodType> breakfastItems = Food.getFoodTypes(MealType.BREAKFAST);
		List<FoodType> lunchItems = Food.getFoodTypes(MealType.LUNCH);
		List<FoodType> dinnerItems = Food.getFoodTypes(MealType.DINNER);

		shop.clearInventory();

		int halfSize = breakfastItems.size() / 2;
		for(int i = 0; i < halfSize; i++) {
			int index = random.nextInt(breakfastItems.size());
			FoodType foodType = breakfastItems.get(index);
			for(int j = 0; j < capacity; j++)
				shop.addMealToInventory(foodType);
			breakfastItems.remove(index);
		}

		halfSize = lunchItems.size() / 2;
		for(int i = 0; i < halfSize; i++) {
			int index = random.nextInt(lunchItems.size());
			FoodType foodType = lunchItems.get(index);
			for(int j = 0; j < capacity; j++)
				shop.addMealToInventory(foodType);
			lunchItems.remove(index);
		}

		halfSize = dinnerItems.size() / 2;
		for(int i = 0; i < halfSize; i++) {
			int index = random.nextInt(dinnerItems.size());
			FoodType foodType = dinnerItems.get(index);
			for(int j = 0; j < capacity; j++)
				shop.addMealToInventory(foodType);
			dinnerItems.remove(index);
		}
	}
	
	private static void updatePantry(ShoppingList shoppingList) {
		Map<MealType, List<FoodType>> fullOrderMap = shoppingList.getFullOrderMap();
		Map<MealType, Integer> allLimitsMap = shoppingList.getAllLimitsMap();
		
		for(MealType mealType : MealType.values()) {
			if(!fullOrderMap.containsKey(mealType)) {
				Log.writeToVerboseLogFile("No meals ordered for " + mealType.name() + ".");
				continue;
			}
			if(!allLimitsMap.containsKey(mealType)) {
				Log.writeToVerboseLogFile("No order limit set for " + mealType.name() + ".");
				continue;
			}
			
			List<FoodType> order = fullOrderMap.get(mealType);
			int limit = allLimitsMap.get(mealType);
			int numMealsFilled = 0;
			for(int i = 0; i < order.size(); i++) {
				if(numMealsFilled >= limit)
					break;
				FoodType mealOrdered = order.get(i);
				if(shop.containsMeal(mealOrdered)) {
					pantry.addMealToInventory(mealOrdered);
					numMealsFilled++;
				}
			}
		}
	}
	
	private static void updateMealHistory(Integer week, ShoppingList shoppingList, Planner planner) {
		mealHistory.addShoppingList(week, deepClone(shoppingList));
		mealHistory.addPlanner(week, deepClone(planner));
		mealHistory.addPantry(week, deepClone(pantry));

		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		List<Day> days = new ArrayList<>(Arrays.asList(new Day[]{
			Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY
		}));
		for(FamilyMember familyMember : familyMembers) {
			MemberName memberName = familyMember.getName();
			for(int i = 0; i < days.size(); i++)
				mealHistory.addDailyFamilyMeal(week, days.get(i), memberName, plan.get(days.get(i)).get(memberName));
		}
	}

	private static void updateAssignedMeals(Planner planner) {
		Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();
		for(FamilyMember familyMember : familyMembers) {
			MemberName memberName = familyMember.getName();
			familyMember.resetMealMap();
			for(Day day : plan.keySet()) {
				Map<MemberName, Map<MealType, FoodType>> dayPlan = plan.get(day);
				if(!dayPlan.containsKey(memberName)) {
					Log.writeToVerboseLogFile("No meals planned for family member " + memberName.name() + " on day " + day.name() + ".");
					continue;
				}
				
				Map<MealType, FoodType> memberPlan = dayPlan.get(memberName);
				for(MealType mealType : memberPlan.keySet()) {
					FoodType chosenMeal = memberPlan.get(mealType);
					familyMember.assignMeal(day, mealType, chosenMeal);
					pantry.removeMealFromInventory(chosenMeal);
				}
			}
		}
	}
	
	private static void updateSatisfactions(Integer week, Planner planner) {
		
		Map<Day, Double> dinnerKScalingFactorMap = new HashMap<>();
		Map<Day, FoodType> dinnersChosen = new HashMap<>();
		for(Day day : Day.values()) {
			int numMembersNotEatingDinner = 0;
			for(FamilyMember familyMember : familyMembers) {
				Map<MealType, FoodType> mealPlan = planner.getPlan().get(day).get(familyMember.getName());
				if(!mealPlan.containsKey(MealType.DINNER) || mealPlan.get(MealType.DINNER) == null)
					numMembersNotEatingDinner++;
				else if(!dinnersChosen.containsKey(day))
					dinnersChosen.put(day, mealPlan.get(MealType.DINNER));
			}
			double kScalingFactor = (numFamilyMembers - numMembersNotEatingDinner) * 1.0 / numFamilyMembers;
			dinnerKScalingFactorMap.put(day, kScalingFactor);
		}
		
		for(FamilyMember familyMember : familyMembers) {
			Map<FoodType, Double> foodPreferenceMap = familyMember.getFoodPreferenceMap();
			MemberName memberName = familyMember.getName();

			Map<Day, Map<MemberName, Map<MealType, FoodType>>> plan = planner.getPlan();

			double totalBreakfastSatisfaction = 0.0;
			for(Day day : plan.keySet()) {
				if(!plan.get(day).containsKey(memberName))
					continue;
				if(!plan.get(day).get(memberName).containsKey(MealType.BREAKFAST))
					continue;
				FoodType chosenMeal = plan.get(day).get(memberName).get(MealType.BREAKFAST);
				if(chosenMeal == null)
					continue;

				totalBreakfastSatisfaction += foodPreferenceMap.get(chosenMeal);
			}

			List<Day> days = new ArrayList<>(Arrays.asList(new Day[]{
				Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY
			}));

			double totalLunchSatisfaction = 0.0;
			for(int i = 0; i < days.size(); i++) {
				Day day = days.get(i);
				if(!plan.get(day).containsKey(memberName))
					continue;
				if(!plan.get(day).get(memberName).containsKey(MealType.LUNCH))
					continue;
				FoodType chosenMeal = plan.get(day).get(memberName).get(MealType.LUNCH);
				if(chosenMeal == null)
					continue;
				
				double scalingFactor = getSatisfactionScalingFactor(memberName, week - 1, i + 1, MealType.LUNCH, chosenMeal);
				if(scalingFactor == -1)
					totalLunchSatisfaction += foodPreferenceMap.get(chosenMeal);
				else
					totalLunchSatisfaction += foodPreferenceMap.get(chosenMeal) * scalingFactor;					
			}
						
			double totalDinnerSatisfaction = 0.0;
			for(int i = 0; i < days.size(); i++) {
				Day day = days.get(i);
				if(!dinnersChosen.containsKey(day))
					continue;
				FoodType chosenMeal = dinnersChosen.get(day);
				if(chosenMeal == null)
					continue;
								
				double scalingFactor = getSatisfactionScalingFactor(memberName, week - 1, i + 1, MealType.DINNER, chosenMeal) * 
						dinnerKScalingFactorMap.get(day);
				totalDinnerSatisfaction += foodPreferenceMap.get(chosenMeal) * scalingFactor;
			}
			
			double totalSatisfaction = totalBreakfastSatisfaction + totalLunchSatisfaction + totalDinnerSatisfaction;
			familyMember.setSatisfaction(totalSatisfaction);

			mealHistory.addSatisfaction(week, familyMember.getName(), totalSatisfaction);		    
		}
	}
	
	private static double getSatisfactionScalingFactor(MemberName memberName, Integer week, Integer dayOffset, MealType mealType, FoodType foodType) {

		Map<Integer, Map<MemberName, Map<MealType, FoodType>>> dailyMeals = mealHistory.getDailyFamilyMeals();		
		double totalDays = 0.0;
		for(int i = week * 7 + dayOffset - 1; i >= 1; i--) {
			totalDays++;
			if(!dailyMeals.get(i).get(memberName).containsKey(mealType))
				continue;
			if(dailyMeals.get(i).get(memberName).get(mealType).equals(foodType))
				return totalDays / (totalDays + 1);
		}
		return 1;
	}
	
	private static void updateAverageSatisfactions(Integer week) {
		for(FamilyMember familyMember : familyMembers) {
			Map<Integer, Map<MemberName, Double>> allSatisfactions = mealHistory.getAllSatisfactions();
			double totalSatisfaction = 0.0;
			int numWeeks = 0;
			for(Integer existingWeek : allSatisfactions.keySet()) {
				totalSatisfaction += allSatisfactions.get(existingWeek).get(familyMember.getName());
				numWeeks++;
			}
			double averageSatisfaction = totalSatisfaction / numWeeks;
			mealHistory.addAverageSatisfaction(week, familyMember.getName(), averageSatisfaction);
		}
	}
	
	private static Double getLeastAverageSatisfaction(Integer week) {
		double leastAverageSatisfaction = Double.MAX_VALUE;
		Map<MemberName, Double> lastWeekAverageSatisfactions = mealHistory.getAllAverageSatisfactions().get(week);
		for(MemberName memberName : lastWeekAverageSatisfactions.keySet())
			if(lastWeekAverageSatisfactions.get(memberName) < leastAverageSatisfaction)
				leastAverageSatisfaction = lastWeekAverageSatisfactions.get(memberName);
		return leastAverageSatisfaction;
	}

	private static MemberName getLeastSatisfiedMember(Integer week) {
		double leastAverageSatisfaction = Double.MAX_VALUE;
		MemberName chosenMemberName = null;
		Map<MemberName, Double> lastWeekAverageSatisfactions = mealHistory.getAllAverageSatisfactions().get(week);
		for(MemberName memberName : lastWeekAverageSatisfactions.keySet())
			if(lastWeekAverageSatisfactions.get(memberName) < leastAverageSatisfaction) {
				leastAverageSatisfaction = lastWeekAverageSatisfactions.get(memberName);
				chosenMemberName = memberName;
			}
		return chosenMemberName;
	}
	
	private static String cleanName(String playerName) {
		String cleanedPlayerName = " ";
		if(playerName.contains("_")) {
			Integer index = playerName.lastIndexOf("_");
			cleanedPlayerName = playerName.substring(0, index);
		}
		else
			return playerName;

		return cleanedPlayerName;
	}	
	
	private static <T extends Object> T deepClone(T obj) {
        if(obj == null)
            return null;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            ByteArrayInputStream bais = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(bais);
            
            return (T) objectInputStream.readObject();
        }
        catch(Exception e) {
            return null;
        }
	}
	
	private static PlayerWrapper loadPlayerWrapper() throws Exception {
		Log.writeToLogFile("Loading team " + teamName + "'s player...");

		Player player = loadPlayer();
        if(player == null) {
            Log.writeToLogFile("Cannot load team " + teamName + "'s player!");
            System.exit(1);
        }

        return new PlayerWrapper(player, teamName, timeout);
    }
	
	private static Player loadPlayer() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String playerPackagePath = sourcePath + File.separator + "menu" + File.separator + teamName;
        Set<File> playerFiles = getFilesInDirectory(playerPackagePath, ".java");
		String simPath = sourcePath + File.separator + "menu" + File.separator + "sim";
        Set<File> simFiles = getFilesInDirectory(simPath, ".java");

        File classFile = new File(playerPackagePath + File.separator + "Player.class");

        long classModified = classFile.exists() ? classFile.lastModified() : -1;
        if(classModified < 0 || classModified < lastModified(playerFiles) || classModified < lastModified(simFiles)) {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler == null)
                throw new IOException("Cannot find the Java compiler!");

            StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
            Log.writeToLogFile("Compiling for team " + teamName + "'s player...");

            if(!compiler.getTask(null, manager, null, null, null, manager.getJavaFileObjectsFromFiles(playerFiles)).call())
                throw new IOException("The compilation failed!");
            
            classFile = new File(playerPackagePath + File.separator + "Player.class");
            if(!classFile.exists())
                throw new FileNotFoundException("The class file is missing!");
        }

        ClassLoader loader = Simulator.class.getClassLoader();
        if(loader == null)
            throw new IOException("Cannot find the Java class loader!");

        @SuppressWarnings("rawtypes")
        Class rawClass = loader.loadClass("menu." + teamName + ".Player");
        Class[] classArgs = new Class[]{Integer.class, Integer.class, Integer.class, Integer.class, SimPrinter.class};
        
        return (Player) rawClass.getDeclaredConstructor(classArgs).newInstance(weeks, numFamilyMembers, capacity, seed, new SimPrinter(enablePrints));
    }

	private static long lastModified(Iterable<File> files) {
        long lastDate = 0;
        for(File file : files) {
            long date = file.lastModified();
            if(lastDate < date)
                lastDate = date;
        }
        return lastDate;
    }
	
	private static Set<File> getFilesInDirectory(String path, String extension) {
		Set<File> files = new HashSet<File>();
        Set<File> previousDirectories = new HashSet<File>();
        previousDirectories.add(new File(path));
        do {
        	Set<File> nextDirectories = new HashSet<File>();
            for(File previousDirectory : previousDirectories)
                for(File file : previousDirectory.listFiles()) {
                    if(!file.canRead())
                    	continue;
                    
                    if(file.isDirectory())
                        nextDirectories.add(file);
                    else if(file.getPath().endsWith(extension))
                        files.add(file);
                }
            previousDirectories = nextDirectories;
        } while(!previousDirectories.isEmpty());
        
        return files;
	}
	
	private static void updateGUI(HTTPServer server, String content) {
		if(server == null)
			return;
		
        String guiPath = null;
        while(true) {
            while(true) {
                try {
                	guiPath = server.request();
                    break;
                } catch(IOException e) {
                    Log.writeToVerboseLogFile("HTTP request error: " + e.getMessage());
                }
            }
            
            if(guiPath.equals("data.txt")) {
                try {
                    server.reply(content);
                } catch(IOException e) {
                    Log.writeToVerboseLogFile("HTTP dynamic reply error: " + e.getMessage());
                }
                return;
            }
            
            if(guiPath.equals(""))
            	guiPath = "webpage.html";
            else if(!Character.isLetter(guiPath.charAt(0))) {
                Log.writeToVerboseLogFile("Potentially malicious HTTP request: \"" + guiPath + "\"");
                break;
            }

            try {
                File file = new File(staticsPath + File.separator + guiPath);
                server.reply(file);
            } catch(IOException e) {
                Log.writeToVerboseLogFile("HTTP static reply error: " + e.getMessage());
            }
        }		
	}
	
	private static String getGUIState(Integer week, ShoppingList shoppingList) throws JSONException {		
		
		DecimalFormat satisfactionFormat = new DecimalFormat("###.####");
		
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("refresh", 60000.0 / fpm);
		jsonObj.put("totalWeeks", weeks);
		jsonObj.put("currentWeek", week);
		jsonObj.put("continuous", continuousGUI);
		jsonObj.put("numMembers", numFamilyMembers);
		jsonObj.put("capacity", capacity);
		jsonObj.put("numEmptySlots", pantry.getNumEmptySlots());
		jsonObj.put("leastAverageSatisfaction", satisfactionFormat.format(getLeastAverageSatisfaction(week)));
		jsonObj.put("leastSatisfiedMember", getLeastSatisfiedMember(week));

		JSONObject pantryJSONObj = new JSONObject();
		Map<MealType, Map<FoodType, Integer>> mealsMap = pantry.getMealsMap();
		for(MealType mealType : mealsMap.keySet()) {
			JSONObject mealTypeJSONObj = new JSONObject();
			for(FoodType foodType : mealsMap.get(mealType).keySet())
				mealTypeJSONObj.put(foodType.name(), mealsMap.get(mealType).get(foodType));
			pantryJSONObj.put(mealType.name(), mealTypeJSONObj);
		}
		jsonObj.put("newPantry", pantryJSONObj);
		
		Pantry oldPantry;
		if(mealHistory.getAllPantries().containsKey(week - 1))
			oldPantry = mealHistory.getAllPantries().get(week - 1);
		else
			oldPantry = new Pantry(capacity);
		JSONObject oldPantryJSONObj = new JSONObject();
		Map<MealType, Map<FoodType, Integer>> oldMealsMap = oldPantry.getMealsMap();
		for(MealType mealType : oldMealsMap.keySet()) {
			JSONObject mealTypeJSONObj = new JSONObject();
			for(FoodType foodType : oldMealsMap.get(mealType).keySet())
				mealTypeJSONObj.put(foodType.name(), oldMealsMap.get(mealType).get(foodType));
			oldPantryJSONObj.put(mealType.name(), mealTypeJSONObj);
		}
		jsonObj.put("oldPantry", oldPantryJSONObj);		

		JSONObject shopJSONObj = new JSONObject();
		mealsMap = shop.getMealsMap();
		for(MealType mealType : mealsMap.keySet()) {
			JSONObject mealTypeJSONObj = new JSONObject();
			for(FoodType foodType : mealsMap.get(mealType).keySet())
				mealTypeJSONObj.put(foodType.name(), mealsMap.get(mealType).get(foodType));
			shopJSONObj.put(mealType.name(), mealTypeJSONObj);
		}
		jsonObj.put("shop", shopJSONObj);
		
		JSONObject familyJSONObj = new JSONObject();
		for(FamilyMember familyMember : familyMembers) {
			JSONObject familyMemberJSONObj = new JSONObject();

			MemberName memberName = familyMember.getName();
			
			Map<FoodType, Double> foodPreferenceMap = familyMember.getFoodPreferenceMap();
			JSONObject foodPreferenceJSONObj = new JSONObject();
			for(FoodType foodType : foodPreferenceMap.keySet())
				foodPreferenceJSONObj.put(foodType.name(), foodPreferenceMap.get(foodType));

			Map<Day, Map<MealType, FoodType>> assignedMealMap = familyMember.getAssignedMealMap();
			JSONObject assignedMealsJSONObj = new JSONObject();
			for(Day day : assignedMealMap.keySet()) {
				JSONObject dayJSONObject = new JSONObject();
				for(MealType mealType : assignedMealMap.get(day).keySet())
					dayJSONObject.put(mealType.name(), assignedMealMap.get(day).get(mealType).name());
				assignedMealsJSONObj.put(day.name(), dayJSONObject);
			}
			
			double satisfaction = familyMember.getSatisfaction();
			double averageSatisfaction = mealHistory.getAllAverageSatisfactions().get(week).get(memberName);
			
			familyMemberJSONObj.put("foodPreferences", foodPreferenceJSONObj);
			familyMemberJSONObj.put("assignedMeals", assignedMealsJSONObj);
			familyMemberJSONObj.put("satisfaction", satisfactionFormat.format(satisfaction));
			familyMemberJSONObj.put("averageSatisfaction", satisfactionFormat.format(averageSatisfaction));
			
			familyJSONObj.put(memberName.name(), familyMemberJSONObj);
		}
		jsonObj.put("family", familyJSONObj);
		
		JSONObject shoppingListJSONObj = new JSONObject();
		Map<MealType, List<FoodType>> fullOrderMap = shoppingList.getFullOrderMap();
		Map<MealType, Integer> allLimitsMap = shoppingList.getAllLimitsMap();
		for(MealType mealType : fullOrderMap.keySet()) {
			JSONObject mealJSONObj = new JSONObject();
			if(!allLimitsMap.containsKey(mealType))
				continue;

			JSONArray mealListJSONArray = new JSONArray();
			for(FoodType foodType : fullOrderMap.get(mealType))
				mealListJSONArray.put(foodType.name());
			mealJSONObj.put("order", mealListJSONArray);
			mealJSONObj.put("limit", allLimitsMap.get(mealType));

			shoppingListJSONObj.put(mealType.name(), mealJSONObj);
		}
		jsonObj.put("shoppingList", shoppingListJSONObj);
			
        return jsonObj.toString();
	}
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, JSONException {
		setup();
		parseCommandLineArguments(args);
		readConfiguration();
		runSimulation();
	}
}
