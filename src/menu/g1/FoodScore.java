package menu.g1;

import menu.sim.FamilyMember;
import menu.sim.Food;

public class FoodScore implements Comparable<FoodScore> {
    private Food.FoodType foodType;
    private Double score;

    public FoodScore(Food.FoodType foodType, Double score) {
        this.foodType = foodType;
        this.score = score;
    }

    public Food.FoodType getFoodType() {
        return foodType;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public int compareTo(FoodScore otherMeal) {
        return this.score.compareTo(otherMeal.getScore());
    }

    @Override
    public String toString() {
        return "foodScore{" +
                "foodType=" + foodType.toString() +
                ", score=" + score.toString() +
                '}';
    }
}
