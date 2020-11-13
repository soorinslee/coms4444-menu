package menu.g1;

import menu.sim.FamilyMember;
import menu.sim.Food;
import menu.sim.MealHistory;
import menu.sim.MemberName;

import java.util.List;
import java.util.PriorityQueue;

public class MemberTracker implements Comparable<MemberTracker> {
    public FamilyMember member;
    public PreferenceTracker prefTracker;
    private Double avgSatisfaction;
    private Double weight;
    private Integer famSize;
    private Integer rank;

    public MemberTracker(FamilyMember member, Integer famSize) {
        this.member = member;
        this.prefTracker = new PreferenceTracker(member);
        this.avgSatisfaction = 0.0;
        this.weight = 1.0;
        this.famSize = famSize;
        this.rank = 1;
    }

    public void update(Integer week, MealHistory mealHistory, Double scale) {
        updateAvgSatisfation(week, mealHistory);
        //updateWeight(scale);
        updatePrefTracker(week, mealHistory);
    }

    public void updateRankAndWeight(Integer newRank, Double scale) {
        this.rank = newRank;
        updateWeight(scale);
    }

    public Double getWeightedPreference(Food.FoodType foodType, int day) {
        return prefTracker.satisfactionsForWeek.get(foodType)[day] * weight;
    }

    public Double getWeightedPreference(Food.FoodType foodType) {
        return member.getFoodPreference(foodType) * weight;
    }

    public Double getAvgSatisfaction() {
        return avgSatisfaction;
    }

    private void updateAvgSatisfation(Integer week, MealHistory mealHistory) {
        if (week > 1) {
            avgSatisfaction = mealHistory.getAverageSatisfaction(week - 1, member.getName());
        }
        else {
            avgSatisfaction = 0.0;
        }
    }

    private void updateWeight(Double scale) {
        weight = (scale * (famSize - rank)) / (Math.exp(avgSatisfaction));
    }

    public Double getWeight() {
        return weight;
    }

    private void updatePrefTracker(Integer week, MealHistory mealHistory) {
        prefTracker.update(week, mealHistory);
    }

    /*public PreferenceTracker getPrefTrackerCopy() {
        //return new PreferenceTracker(prefTracker);
        return prefTracker;
    }*/

    @Override
    public int compareTo(MemberTracker otherMember) {
        return this.avgSatisfaction.compareTo(otherMember.getAvgSatisfaction());
    }

    public Food.FoodType getFirstChoice(Food.MealType mealType) {
        List<Food.FoodType> foods = Food.getFoodTypes(mealType);
        Food.FoodType firstChoice = foods.get(0);
        Double maxPref = member.getFoodPreference(firstChoice);

        for (Food.FoodType food: foods) {
            Double pref = member.getFoodPreference(food);
            if (pref > maxPref) {
                firstChoice = food;
                maxPref = pref;
            }
        }
        return firstChoice;
    }

    public PriorityQueue<FoodScore> getFoodsByPref(List<Food.FoodType> foods) {
        PriorityQueue<FoodScore> foodsQueue = new PriorityQueue<>();
        for (Food.FoodType food: foods) {
            FoodScore foodScore = new FoodScore(food, member.getFoodPreference(food));
            foodsQueue.add(foodScore);
        }
        return foodsQueue;
    }

    public PriorityQueue<FoodScore> getFoodsByPref(Food.MealType mealType) {
        PriorityQueue<FoodScore> foods = new PriorityQueue<>();
        for (Food.FoodType food: Food.getFoodTypes(mealType)) {
            FoodScore foodScore = new FoodScore(food, member.getFoodPreference(food));
            foods.add(foodScore);
        }
        return foods;
    }

    public MemberName getName() {
        return member.getName();
    }
}
