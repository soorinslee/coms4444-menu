package sim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public List<Game> reallocate(Integer round, GameHistory gameHistory, List<Game> playerGames, Map<Integer, List<Game>> opponentGamesMap) {

    	Log.writeToVerboseLogFile("Retrieving reallocated goals from team " + this.playerName + " in round " + round + "...");
        
    	List<Game> reallocatedPlayerGames = new ArrayList<>();

        try {
            if(!timer.isAlive())
            	timer.start();
            timer.callStart(() -> { return player.reallocate(round, gameHistory, playerGames, opponentGamesMap); });
            reallocatedPlayerGames = timer.callWait(timeout);
        }
        catch(Exception e) {
            Log.writeToVerboseLogFile("Team " + this.playerName + " has possibly timed out.");
            Log.writeToVerboseLogFile("Exception for team " + this.playerName + ": " + e);
        }

        return reallocatedPlayerGames;
    }
    
    public Player getPlayer() {
    	return player;
    }

    public String getPlayerName() {
        return playerName;
    }
}