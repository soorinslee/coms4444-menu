package menu.sim;

import java.util.List;

public class PlayerWrapper {

	private Timer timer;
    private Player player;
    private String playerName;
    private long timeout;

    public PlayerWrapper(Player player, String playerName, long timeout) {
        this.player = player;
        this.playerName = playerName;
        this.timeout = timeout;
        this.timer = new Timer();
    }

    public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

    	Log.writeToVerboseLogFile("Team " + this.playerName + " stocking pantry for week " + week + "...");
        
    	ShoppingList shoppingList = new ShoppingList();

        try {
            if(!timer.isAlive())
            	timer.start();
            timer.callStart(() -> { return player.stockPantry(week, numEmptySlots, familyMembers, pantry, mealHistory); });
            shoppingList = timer.callWait(timeout);
        }
        catch(Exception e) {
            Log.writeToVerboseLogFile("Team " + this.playerName + " generated an exception while stocking pantry.");
            Log.writeToVerboseLogFile("Exception for team " + this.playerName + ": " + e);
        }

        return shoppingList;
    }
    
    public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

    	Log.writeToVerboseLogFile("Team " + this.playerName + " planning meals for week " + week + "...");
        
    	Planner planner = new Planner();

        try {
            if(!timer.isAlive())
            	timer.start();
            timer.callStart(() -> { return player.planMeals(week, familyMembers, pantry, mealHistory); });
            planner = timer.callWait(timeout);
        }
        catch(Exception e) {
            Log.writeToVerboseLogFile("Team " + this.playerName + " generated an exception while planning meals.");
            Log.writeToVerboseLogFile("Exception for team " + this.playerName + ": " + e);
        }

        return planner;
    }
       
    public Player getPlayer() {
    	return player;
    }

    public String getPlayerName() {
        return playerName;
    }
}