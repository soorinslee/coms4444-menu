package menu.g1;

import menu.sim.FamilyMember;
import menu.sim.Food;
import menu.sim.MealHistory;
import menu.sim.MemberName;

import java.util.List;

public class MemberTracker implements Comparable<MemberTracker> {
    public FamilyMember member;
    public PreferenceTracker prefTracker;
    private Double avgSatisfaction;
    private Double weight;

    public MemberTracker(FamilyMember member) {
        this.member = member;
        this.prefTracker = new PreferenceTracker(member);
        this.avgSatisfaction = 0.0;
        this.weight = 1.0;
    }

    public void update(Integer week, MealHistory mealHistory, Double scale) {
        updateAvgSatisfation(week, mealHistory);
        updateWeight(scale);
        updatePrefTracker(week, mealHistory);
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
        if (avgSatisfaction == 0) {
            weight = scale/0.0000001;
        }
        else {
            weight = scale/(avgSatisfaction * avgSatisfaction);
        }
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

    public MemberName getName() {
        return member.getName();
    }
}
