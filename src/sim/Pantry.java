package sim;

public class Pantry extends Inventory {
	
	private Integer capacity;
	
	public Pantry(Integer capacity) {
		super();
		this.capacity = capacity;
	}
}

// Contains an emptySlots field
// Contains a numEmptySlots method that returns the number of empty slots