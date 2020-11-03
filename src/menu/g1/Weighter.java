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

    public Map<Food.MealType, Map<FamilyMember, PriorityQueue<FoodScore>>> memberScores;
    public Map<Food.MealType, PriorityQueue<FoodScore>> avgScores;

    public Map<FamilyMember, PriorityQueue<FoodScore>> memberBreakfastScores;
    public Map<FamilyMember, PriorityQueue<FoodScore>> memberLunchScores;
    public Map<FamilyMember, PriorityQueue<FoodScore>> memberDinnerScores;

    public PriorityQueue<FoodScore> avgBreakfastScores;
    public PriorityQueue<FoodScore> avgLunchScores;
    public PriorityQueue<FoodScore> avgDinnerScores;


    public Weighter(List<FamilyMember> familyMembers) {
        familySize = familyMembers.size();
        memberWeights = new HashMap<>();
        memberScores = new HashMap<Food.MealType, Map<FamilyMember, PriorityQueue<FoodScore>>>();
        avgScores = new HashMap<Food.MealType, PriorityQueue<FoodScore>>();

        for (FamilyMember member: familyMembers) {
            memberWeights.put(member, 1.0);
        }

        for (Food.MealType mealType: Food.MealType.values()) {
            memberScores.put(mealType, new HashMap<FamilyMember, PriorityQueue<FoodScore>>());
            avgScores.put(mealType, null);
        }
    }

    public void update(Integer week, MealHistory mealHistory, List<FamilyMember> familyMembers) {
        if (week > 1) {
            this.updateMemberWeights(week, mealHistory, familyMembers);
        }

        for (Food.MealType mealType: Food.MealType.values()) {
            this.updateMealData(mealType);
        }
    }

    private void updateMemberWeights(Integer week, MealHistory mealHistory, List<FamilyMember> familyMembers) {
        memberWeights = new HashMap<>();

        for (FamilyMember member: familyMembers) {
            Double weight = DEFAULT_SCALE/mealHistory.getAverageSatisfaction(week, member.getName());
            memberWeights.put(member, weight);
        }
    }

    public void updateMealData(Food.MealType mealType) {
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
}
