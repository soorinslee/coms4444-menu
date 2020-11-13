package menu.g1;

import menu.sim.FamilyMember;
import menu.sim.Food;
import menu.sim.MealHistory;

import java.util.*;

public class Weighter {
    private final Integer NUM_BF_MEALS = 10;
    private final Double DEFAULT_SCALE = 1.0;
    private Integer familySize;

    private Map<FamilyMember, Double> memberWeights;

    private Map<Food.MealType, Map<FamilyMember, PriorityQueue<FoodScore>>> memberScores;
    private Map<Food.MealType, PriorityQueue<FoodScore>> avgScores;


    public Weighter(Integer familySize) {
        this.familySize = familySize;
        this.memberWeights = new HashMap<>();
        this.memberScores = new HashMap<>();
        this.avgScores = new HashMap<>();

        for (Food.MealType mealType: Food.MealType.values()) {
            memberScores.put(mealType, new HashMap<FamilyMember, PriorityQueue<FoodScore>>());
            avgScores.put(mealType, null);
        }
    }

    public void update(Integer week, MealHistory mealHistory, List<FamilyMember> familyMembers) {
        this.updateMemberWeights(week, mealHistory, familyMembers);

        for (Food.MealType mealType: Food.MealType.values()) {
            this.updateMealData(mealType);
        }
    }

    private void updateMemberWeights(Integer week, MealHistory mealHistory, List<FamilyMember> familyMembers) {
        memberWeights = new HashMap<>();

        if (week == 1) {
            for (FamilyMember member : familyMembers) {
                memberWeights.put(member, 1.0);
            }
        }
        else {
            for (FamilyMember member : familyMembers) {
                Double avgSat = mealHistory.getAverageSatisfaction(week - 1, member.getName());
                Double weight = DEFAULT_SCALE / (avgSat * avgSat);
                memberWeights.put(member, weight);
            }
        }
    }

    private void updateMealData(Food.MealType mealType) {
        Map<Food.FoodType, ArrayList<Double>> foodScoresMap = new HashMap<>();
        List<Food.FoodType> foods = Food.getFoodTypes(mealType);
        Map<FamilyMember, PriorityQueue<FoodScore>> mealScores = memberScores.get(mealType);


        for (Map.Entry<FamilyMember, Double> memberWeight: memberWeights.entrySet()) {
            PriorityQueue<FoodScore> personalScores =
                    new PriorityQueue<FoodScore>(foods.size(), Collections.reverseOrder());
            FamilyMember member = memberWeight.getKey();

            for (Food.FoodType food: foods) {
                if (!foodScoresMap.containsKey(food)) {
                    foodScoresMap.put(food, new ArrayList<Double>());
                }

                Double score = calculateMemberScore(member, food);
                foodScoresMap.get(food).add(score);

                FoodScore foodScore = new FoodScore(food, score);
                personalScores.add(foodScore);
            }
            mealScores.put(member, personalScores);
        }

        avgScores.put(mealType, new PriorityQueue<FoodScore>(foods.size(), Collections.reverseOrder()));
        PriorityQueue<FoodScore> avgMealScores = avgScores.get(mealType);

        for (Map.Entry<Food.FoodType, ArrayList<Double>> foodScores: foodScoresMap.entrySet()) {
            Food.FoodType food = foodScores.getKey();
            Double score = averageScore(foodScores.getValue());
            FoodScore foodScore = new FoodScore(food, score);
            avgMealScores.add(foodScore);
        }
    }

    private Double averageScore(ArrayList<Double> scores) {
        Double total = 0.0;
        for (Double score: scores) {
            total += score;
        }
        return total/scores.size();
    }

    private Double calculateMemberScore(FamilyMember member, Food.FoodType foodType) {
        return  member.getFoodPreference(foodType) * memberWeights.get(member);
    }

    public Map<FamilyMember, Double> getMemberWeights() {
        return memberWeights;
    }

    public Map<FamilyMember, PriorityQueue<FoodScore>> getMemberScoresFor(Food.MealType mealType) {
        return new HashMap<>(memberScores.get(mealType));
    }

    public PriorityQueue<FoodScore> getAvgScoresFor(Food.MealType mealType) {
        return new PriorityQueue<>(avgScores.get(mealType));
    }
}
