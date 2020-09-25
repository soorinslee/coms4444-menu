package sim;

import java.util.HashMap;
import java.util.Map;

public class FamilyMember {
	
	private Integer memberID;
	private Map<Food, Integer> foodPreferenceMap;
		
	public FamilyMember(Integer memberID) {
		this.memberID = memberID;
		this.foodPreferenceMap = new HashMap<>();
	}
	
	
}


//* Contains a unique personID field (integer type)
//* Contains a map of Food to integer preference
//* Contains a satisfaction field (double type)
//* Contains an assignFood(Food, Day) method
//* Contains getSatisfaction method