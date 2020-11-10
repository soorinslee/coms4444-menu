package menu.g2;
//Done by Aum
import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;

public class StorePredictor {

    Pantry currentPantry;
    Pantry previousPantry;
    ShoppingList previous;
    Map<FoodType, Double> probs;

    public StorePredictor() {
        this.probs = new HashMap<>();
        List<FoodType> foodList = new ArrayList<>(EnumSet.allOf(FoodType.class));
        for (FoodType food : foodList) {
            this.probs.put(food, 1.0);
        }
    }

    public Map<FoodType, Double> calculateProbabilities(ShoppingList shoppingList, int week) {
        this.previous = shoppingList;
        //System.out.println("PREV: "  + this.previousPantry.getMealsMap().toString());
        //System.out.println("CURR: "  + this.currentPantry.getMealsMap().toString());
        if (week != 1) {
            HashMap<FoodType, Double> probsClone = new HashMap<>();
            probsClone.putAll(this.probs);
            for (FoodType key : probsClone.keySet()) {
                double previousProb = this.probs.get(key);
                previousProb *= (week - 1);
                previousProb = isMissing(key) ? previousProb / week : (previousProb + 1) / week;
                this.probs.remove(key);
                this.probs.put(key, previousProb);
            }
            this.previousPantry = this.currentPantry;
            //System.out.println("MAPS: "  + this.probs.toString());
        }
        return this.probs;
    }

    public void setPreviousPantry(Pantry pantry) {
        this.previousPantry = pantry;
    }

    public void setCurrentPantry(Pantry pantry) {
        this.currentPantry = pantry;
    }

    private boolean isMissing(FoodType food) {
        if(this.previous == null){
            System.out.println("NULL");
        }
        List<FoodType> breakfast = this.previous.getFullOrderMap().get(MealType.BREAKFAST);
        // System.out.println(breakfast.toString());
        List<FoodType> lunch = this.previous.getFullOrderMap().get(MealType.LUNCH);
        List<FoodType> dinner = this.previous.getFullOrderMap().get(MealType.DINNER);
        // System.out.println(breakfast);
        for (FoodType food2 : this.probs.keySet()) {
            if (!breakfast.contains(food) && !lunch.contains(food) && !dinner.contains(food)) {
                return false;
            } else if (breakfast.contains(food2)) {
                List<FoodType> diff = getDifferences(MealType.BREAKFAST);
                int maxIndex = 0;
                for (int i = 0; i < breakfast.size(); i++) {
                    maxIndex = (diff.contains(breakfast.get(i))) ? i : maxIndex;
                }
                return !(breakfast.indexOf(food) < maxIndex && !diff.contains(food));
            } else if (lunch.contains(food2)) {
                
                List<FoodType> diff = getDifferences(MealType.LUNCH);

                int maxIndex = 0;
                for (int i = 0; i < lunch.size(); i++) {
                    maxIndex = (diff.contains(lunch.get(i))) ? i : maxIndex;
                }
                return !(lunch.indexOf(food) < maxIndex && !diff.contains(food));
            } else if (dinner.contains(food2)){
                List<FoodType> diff = getDifferences(MealType.DINNER);

                int maxIndex = 0;
                for (int i = 0; i < dinner.size(); i++) {
                    maxIndex = (diff.contains(dinner.get(i))) ? i : maxIndex;
                }
                return !(dinner.indexOf(food) < maxIndex && !diff.contains(food));
            }
        }
        return true;
    }

    private List<FoodType> getDifferences(MealType mealType) {
        ArrayList<FoodType> diff = new ArrayList<>();
        Food f = new Food();
        // System.out.println(f.getFoodTypes(mealType).toString());
        for (FoodType food : f.getFoodTypes(mealType)) {
            if (this.previousPantry.getNumAvailableMeals(food) < this.currentPantry.getNumAvailableMeals(food)) {
                diff.add(food);
            }
        }
        
        if(mealType == MealType.BREAKFAST){
             System.out.println("DIFF: " + diff.toString());
        }
        return diff;
    }

}