# Project 3: Menu

## Course Summary

Course: COMS 4444 Programming and Problem Solving (Fall 2020)  
Website: http://www.cs.columbia.edu/~kar/4444f20  
University: Columbia University  
Instructor: Prof. Kenneth Ross  
TA: Aditya Sridhar

## Project Description

During this project, you will be ordering meals and assigning breakfasts, lunches, and dinners to family members throught the week. Each family member has a personal satisfaction for each of 10 breakfasts, 10 lunches, and 10 dinners, and your objective is to maximize the average satisfaction of the least-satisfied family member across all weeks.

## Implementation

Along with designing some configurations for the project, you will be creating your own player that extends the simulator's player. Please follow these steps to begin your implementation:
1.  Enter the `coms4444-menu/src/configs` directory, and create a folder called "g*x*" (where *x* is the number of your team). For example, if you are team "g5," please create a folder called "g5" in the `configs` directory.
2.  Create your configurations inside your newly-created folder.
3.  Enter the `coms4444-menu/src/menu` directory, and create a folder called "g*x*" (where *x* is the number of your team). For example, if you are team "g5," please create a folder called "g5" in the `menu` directory.
4.  Create a Java file called `Player.java` inside your newly-created folder.
5.  Copy the following code into `Player` (the TODOs indicate all changes you need to make):
```
package menu.gx; // TODO modify the package name to reflect your team

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
    public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {
    	
      // TODO add your code here to generate a shopping list

      return null; // TODO modify the return statement to return your shopping list
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
 
      // TODO add your code here to generate a planner

      return null; // TODO modify the return statement to return your planner
    }
}
```


## Submission
You will be submitting your created team folder, which includes the implemented `Player` class and any other helper classes you create. We ask that you please do not modify any code in the `sim` or `random` directories, especially the simulator, when you submit your code. This makes it easier for us to merge in your code.

To submit your code for each class and for the final deliverable of the project, you will create a pull request to merge your forked repository's *master* branch into the TA's base repository's *master* branch. The TA will merge the commits from the pull request after the deliverable deadline has passed. The base repository will be updated before the start of the next class meeting.

In order to improve performance and readability of code during simulations, we would like to prevent flooding the console with print statements. Therefore, we have provided a printer called `SimPrinter` to allow for toggled printing to the console. When adding print statements for testing/debugging in your code, please make sure to use the methods in `SimPrinter` (instance available in `Player`) rather than use `System.out` statements directly. Additionally, please set the `enablePrints` default variable in `Simulator` to *true* in order to enable printing. This also allows us to not require that you comment out any print statements in your code submissions.

## Simulator

#### Steps to run the simulator:
1.  On your command line, *fork* the Git repository, and then clone the forked version. Do NOT clone the original repository.
2.  Enter `cd coms4444-menu/src` to enter the source folder of the repository.
3.  Run `make clean` and `make compile` to clean and compile the code.
5.  Run one of the following:
    * `make run`: view results from the command line
    * `make gui`: view results from the GUI

#### Simulator arguments:
> **[-w | --weeks]**: number of weeks (default = 52)

> **[-p | --people]**: number of family members (default = 3)

> **[-m PATH | --config PATH]**: path to the simulation preferences configuration

> **[-t | --team]**: team/player

> **[-s | --seed]**: seed value for random player (default = 42)

> **[-l PATH | --log PATH]**: enable logging and output log to both console and log file

> **[-v | --verbose]**: record verbose log when logging is enabled (default = false)

> **[-g | --gui]**: enable GUI (default = false)

> **[-f | --fpm]**: speed (frames per minute) of GUI when GUI is enabled (default = 15)

> **[-c | --continuous]**: enable continuous GUI for simulation when GUI is enabled (default = true)

> **[-d | --discrete]**: enable discrete/frame-by-frame GUI for simulation when GUI is enabled (default = false)

> **[-C | --capacity]**: capacity of the pantry inventory (default = 50)

> **[-e PATH PATH PATH PATH | --export PATH PATH PATH PATH]**: export all detailed information about meals, planners, pantries, and satisfactions to CSV files



## Preferences Configuration

A preferences configuration file (*.dat* extension) contains a list of space-separated preferences (decimal values between 0 and 1) for each food. Each family member has a separate list of preferences. Because there are 40 food items in total (10 breakfast items, 10 lunch items, and 20 dinner items), the number of entries in the configuration file is 40*p*, where *p* is the number of family members. The *j*th entry of the *i*th line of the file corresponds to the *i*th family member's preference of the *j*th food item in the list {BREAKFAST1, BREAKFAST2, ..., BREAKFAST10, LUNCH1, LUNCH2, ..., LUNCH10, DINNER1, DINNER2, ..., DINNER20}.

An example of a preferences configuration is as follows:

```
0.4 0.2 0.3 0.8 0.6 0.3 ... 0.6 0.3 0.1 0.4 0.6 0.9
0.2 0.3 0.8 0.6 0.3 0.1 ... 0.3 0.1 0.4 0.6 0.9 0.4
0.3 0.8 0.6 0.3 0.1 0.4 ... 0.1 0.4 0.6 0.9 0.4 0.2
...
```


## API Description

The following provides the API available for students to use:
1. `Day`: an enumerated type of all seven days.
2. `FamilyMember`: a wrapper class for a family member, containing food preferences and satisfactions/assigned meals for the current week.
3. `Food`: a helper class for easy access to meals and meal types.
	* `getAllMealTypes`
	* `getAllFoodTypes`
	* `getMealType`
	* `getFoodTypes`
	* `isBreakfastType`
	* `isLunchType`
	* `isDinnerType`
4. `Inventory`: a superclass representing any inventory of meals.
	* `getAvailableFoodTypes`
	* `getNumAvailableFoodTypes`
	* `getNumAvailableMeals`
	* `containsMeal`
	* `addMealToInventory`
	* `removeMealFromInventory`
	* `getMealsMap`
	* `clearInventory`
	* `clone`
5. `MealHistory`: a running history of all assigned meals, planners, pantries, and satisfactions.
	* `getAllPlanners`
	* `getAllShoppingLists`
	* `getAllPantries`
	* `getDailyFamilyMeals`
	* `getAlLSatisfactions`
	* `getAllAverageSatisfactions`
	* `getDailyFamilyMeal`
	* `getPlanner`
	* `getShoppingList`
	* `getPantry`
	* `getSatisfaction`
	* `getAverageSatisfaction`
	* `addDailyFamilyMeal`
	* `addPlanner`
	* `addShoppingList`
	* `addPantry`
	* `addSatisfaction`
	* `addAverageSatisfaction`
	
	`Map<Integer, Planner> allPlanners` is a map of the week number to the planner for that week.
	
	`Map<Integer, ShoppingList> allShoppingLists` is a map of the week number to the shopping list for that week.
	
	`Map<Integer, Pantry> allPantries` is a map of the week number to the pantry for that week.
	
	`Map<Integer, Map<MemberName, Map<MealType, FoodType>>> dailyFamilyMeals` is a map of the day number (for 52 weeks, the map will contain keys 1 to 364) to a member map. The member map is a map of the member name to a meal map. Finally, the meal map is a map of meal type to the meal assigned.
	
	`Map<Integer, Map<MemberName, Double>> allSatisfactions` is a map of the week number to a member map. The member map is a map of the member name to the weekly satisfaction for that week.
	
	`Map<Integer, Map<MemberName, Double>> allAverageSatisfactions` is a map of the week number to a member map. The member map is a map of the member name to the average weekly satisfaction (cumulative) up to and including that week.

6. `MemberName`: an enumerated type of all possible family member names..
7. `Pantry`: the pantry inventory containing all available meals to assign for the week (extends `Inventory`).
	* `addMealToInventory`
	* `getNumEmptySlots`
	* `clone`
8. `Planner`: a planner of assigned meals for the week, subject to pantry availability.
	* `addMeal`
	* `getMeal`
	* `getPlan`
9. `Player`: the player abstraction that should be extended by implemented players.
	* `stockPantry`
	* `planMeals`
	* `hasValidShoppingList`
	* `hasValidPlanner`
10. `Shop`: the shop inventory of available meals to order for the week (extends `Inventory`).
11. `ShoppingList`: a shopping list of meals to order for each meal type.
	* `getFullOrderMap`
	* `getAllLimitsMap`
	* `getMealOrder`
	* `getLimit`
	* `addToOrder`
	* `addLimit`
12. `SimPrinter`: contains methods for toggled printing.
	* `println`: prints with cursor at start of the next line.
	* `print`: prints with cursor at the end of the current line.

Classes that are used by the simulator include:
1. `PlayerWrapper`: a player wrapper that enforces appropriate timeouts on shopping list and planner generation.
2. `HTTPServer`: a lightweight web server for the simulator.
3. `Log`: basic functionality to log results, with the option to enable verbose logging.
4. `Simulator`: the simulator and entry point for the project; manages the player, wrapper, logging, server, and GUI state.
5. `Timer`: basic functionality for imposing timeouts.


## Piazza
If you have any questions about the project, please post them in the [Piazza forum](https://piazza.com/class/kdjd7v2b8925zz?cid=67) for the course, and an instructor will reply to them as soon as possible. Any updates to the project itself will be available in Piazza.


## Disclaimer
This project belongs to Columbia University. It may be freely used for educational purposes.
