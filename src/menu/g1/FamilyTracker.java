package menu.g1;

import menu.sim.FamilyMember;
import menu.sim.Food;
import menu.sim.MealHistory;
import menu.sim.MemberName;

import java.util.*;

public class FamilyTracker {
    private final Double DEFAULT_SCALE = 1.0;
    private Map<MemberName, MemberTracker> members;

    public FamilyTracker() {
        this.members = new HashMap<>();
    }

    public void init(List<FamilyMember> familyMembers) {
        for (FamilyMember member: familyMembers) {
            members.put(member.getName(), new MemberTracker(member, familyMembers.size()));
        }
    }

    public void update(Integer week, MealHistory mealHistory) {
        for (MemberTracker member: members.values()) {
            member.update(week, mealHistory, DEFAULT_SCALE);
        }
        Integer rank = 1;// members.size();
        PriorityQueue<MemberTracker> memberTrackers = getMembersByAvgSatisfaction();
        while (!memberTrackers.isEmpty()) {
            MemberTracker mt = memberTrackers.poll();
            mt.updateRankAndWeight(rank, DEFAULT_SCALE);
            rank++;
        }
    }

    public MemberTracker getMemberTracker(MemberName memberName) {
        return members.get(memberName);
    }

    public void addTempDinner(Food.FoodType food, int day) {
        for (MemberTracker memberTracker: members.values()) {
            memberTracker.prefTracker.addTempFood(food, day);
        }
    }

    public void resetAllPrefTrackers() {
        for (MemberTracker memberTracker: members.values()) {
            memberTracker.prefTracker.reset();
        }
    }

    public PriorityQueue<FoodScore> getFoodsByCompositeScore(Food.MealType mealType, int day) {
        PriorityQueue<FoodScore> foods = new PriorityQueue<>(20, Collections.reverseOrder());
        for (Food.FoodType food: Food.getFoodTypes(mealType)) {
            Double compositeScore = 0.0;
            for (MemberTracker mt: members.values()) {
                compositeScore += mt.getWeightedPreference(food, day);
            }
            FoodScore foodScore = new FoodScore(food, compositeScore);
            foods.add(foodScore);
        }
        return foods;
    }

    public PriorityQueue<FoodScore> getBreakfastsByCompositeScore() {
        PriorityQueue<FoodScore> breakfasts = new PriorityQueue<>(20, Collections.reverseOrder());
        for (Food.FoodType breakfast: Food.getFoodTypes(Food.MealType.BREAKFAST)) {
            Double compositeScore = 0.0;
            for (MemberTracker mt: members.values()) {
                compositeScore += mt.getWeightedPreference(breakfast);
            }
            FoodScore dinnerScore = new FoodScore(breakfast, compositeScore);
            breakfasts.add(dinnerScore);
        }
        return breakfasts;
    }

    public PriorityQueue<FoodScore> getDinnersByCompositeScore(int day, Map<Food.FoodType, Integer> inventory) {
        PriorityQueue<FoodScore> dinners = new PriorityQueue<>(20, Collections.reverseOrder());
        for (Map.Entry<Food.FoodType, Integer> dinnerEntry: inventory.entrySet()) {
            Food.FoodType dinner = dinnerEntry.getKey();
            Integer quantity = dinnerEntry.getValue();
            Double compositeScore = 0.0;
            for (MemberTracker mt: members.values()) {
                compositeScore += mt.getWeightedPreference(dinner, day);
            }
            compositeScore *= (((double) quantity)/members.size());
            FoodScore dinnerScore = new FoodScore(dinner, compositeScore);
            dinners.add(dinnerScore);
        }
        return dinners;
    }

    // from least to most satisfied
    public PriorityQueue<MemberTracker> getMembersByAvgSatisfaction() {
        PriorityQueue<MemberTracker> membersByAvgSat = new PriorityQueue<>();
        for (Map.Entry<MemberName, MemberTracker> trackerEntry: members.entrySet()) {
            membersByAvgSat.add(trackerEntry.getValue());
        }
        return membersByAvgSat;
    }
}
