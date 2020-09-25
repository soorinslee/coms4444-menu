package sim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Food {
	
	private FoodType foodType;
	
	public Food(FoodType foodType) {
		this.foodType = foodType;
	}
	
	public enum MealType {
		BREAKFAST, LUNCH, DINNER
	}
	
	public enum FoodType {
		BREAKFAST1, BREAKFAST2, BREAKFAST3, BREAKFAST4, BREAKFAST5, BREAKFAST6, BREAKFAST7, BREAKFAST8, BREAKFAST9, BREAKFAST10,
		LUNCH1, LUNCH2, LUNCH3, LUNCH4, LUNCH5, LUNCH6, LUNCH7, LUNCH8, LUNCH9, LUNCH10,
		DINNER1, DINNER2, DINNER3, DINNER4, DINNER5, DINNER6, DINNER7, DINNER8, DINNER9, DINNER10, DINNER11, DINNER12, DINNER13,
			DINNER14, DINNER15, DINNER16, DINNER17, DINNER18, DINNER19, DINNER20
	}
	
	public static List<MealType> getAllMealTypes() {
		return Arrays.asList(MealType.values());
	}
	
	public static List<FoodType> getAllFoodTypes() {
		return Arrays.asList(FoodType.values());
	}
	
	public static MealType getMealType(FoodType foodType) {
		switch(foodType) {
		case BREAKFAST1:
		case BREAKFAST2:
		case BREAKFAST3:
		case BREAKFAST4:
		case BREAKFAST5:
		case BREAKFAST6:
		case BREAKFAST7:
		case BREAKFAST8:
		case BREAKFAST9:
		case BREAKFAST10:
			return MealType.BREAKFAST;
		case LUNCH1:
		case LUNCH2:
		case LUNCH3:
		case LUNCH4:
		case LUNCH5:
		case LUNCH6:
		case LUNCH7:
		case LUNCH8:
		case LUNCH9:
		case LUNCH10:
			return MealType.LUNCH;
		case DINNER1:
		case DINNER2:
		case DINNER3:
		case DINNER4:
		case DINNER5:
		case DINNER6:
		case DINNER7:
		case DINNER8:
		case DINNER9:
		case DINNER10:
		case DINNER11:
		case DINNER12:
		case DINNER13:
		case DINNER14:
		case DINNER15:
		case DINNER16:
		case DINNER17:
		case DINNER18:
		case DINNER19:
		case DINNER20:
			return MealType.DINNER;
		default: return null;
		}
	}
		
	public static List<FoodType> getFoodTypes(MealType mealType) {
		List<FoodType> foodTypes = new ArrayList<>();
		
		switch(mealType) {
		case BREAKFAST:
			foodTypes.addAll(Arrays.asList(new FoodType[]{
				FoodType.BREAKFAST1, FoodType.BREAKFAST2, FoodType.BREAKFAST3, FoodType.BREAKFAST4, FoodType.BREAKFAST5,
				FoodType.BREAKFAST6, FoodType.BREAKFAST7, FoodType.BREAKFAST8, FoodType.BREAKFAST9, FoodType.BREAKFAST10 
			}));
			return foodTypes;
		case LUNCH:
			foodTypes.addAll(Arrays.asList(new FoodType[]{
					FoodType.LUNCH1, FoodType.LUNCH2, FoodType.LUNCH3, FoodType.LUNCH4, FoodType.LUNCH5,
					FoodType.LUNCH6, FoodType.LUNCH7, FoodType.LUNCH8, FoodType.LUNCH9, FoodType.LUNCH10 
				}));
			return foodTypes;
		case DINNER:
			foodTypes.addAll(Arrays.asList(new FoodType[]{
					FoodType.DINNER1, FoodType.DINNER2, FoodType.DINNER3, FoodType.DINNER4, FoodType.DINNER5,
					FoodType.DINNER6, FoodType.DINNER7, FoodType.DINNER8, FoodType.DINNER9, FoodType.DINNER10, 
					FoodType.DINNER11, FoodType.DINNER12, FoodType.DINNER13, FoodType.DINNER14, FoodType.DINNER15,
					FoodType.DINNER16, FoodType.DINNER17, FoodType.DINNER18, FoodType.DINNER19, FoodType.DINNER20 
				}));
			return foodTypes;
		default: return null;
		}
	}
	
	public boolean isBreakfastType(FoodType foodType) {
		switch(foodType) {
		case BREAKFAST1:
		case BREAKFAST2:
		case BREAKFAST3:
		case BREAKFAST4:
		case BREAKFAST5:
		case BREAKFAST6:
		case BREAKFAST7:
		case BREAKFAST8:
		case BREAKFAST9:
		case BREAKFAST10:
			return true;
		default: return false;
		}
	}

	public boolean isBreakfastType(MealType mealType) {
		return mealType.equals(MealType.BREAKFAST);
	}
	
	public boolean isLunchType(FoodType foodType) {
		switch(foodType) {
		case LUNCH1:
		case LUNCH2:
		case LUNCH3:
		case LUNCH4:
		case LUNCH5:
		case LUNCH6:
		case LUNCH7:
		case LUNCH8:
		case LUNCH9:
		case LUNCH10:
			return true;
		default: return false;
		}
	}

	public static boolean isLunchType(MealType mealType) {
		return mealType.equals(MealType.LUNCH);
	}

	public static boolean isDinnerType(FoodType foodType) {
		switch(foodType) {
		case DINNER1:
		case DINNER2:
		case DINNER3:
		case DINNER4:
		case DINNER5:
		case DINNER6:
		case DINNER7:
		case DINNER8:
		case DINNER9:
		case DINNER10:
		case DINNER11:
		case DINNER12:
		case DINNER13:
		case DINNER14:
		case DINNER15:
		case DINNER16:
		case DINNER17:
		case DINNER18:
		case DINNER19:
		case DINNER20:
			return true;
		default: return false;
		}
	}
	
	public static boolean isDinnerType(MealType mealType) {
		return mealType.equals(MealType.DINNER);
	}
	
	public FoodType getFoodType() {
		return foodType;
	}
	
	public void setFoodType(FoodType foodType) {
		this.foodType = foodType;
	}
}