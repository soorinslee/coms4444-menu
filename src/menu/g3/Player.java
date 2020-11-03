package menu.g3; // TODO modify the package name to reflect your team

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;
import sun.util.locale.provider.AvailableLanguageTags;


public class Player extends menu.sim.Player {

    private breakfastArray // never gets updated 
    private lunchArray // updated with each day according to frequency 
    private dinnerArray // updated with each day according to frequency 

    private frequencyArray // keeps track of how many times this meal was eaten in the past X days 
    /* these take the form of:
        B1, L1, D1 ... are satisfactions for each meal
        fm 1: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 2: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        fm 3: [ B1, B2, B3 ... L1, L2, L3 ..., D1, D2, D3 ... ]
        . . .
    */

    private familySatisfaction // array that keeps track of the family member's satisfaction for each day
    /* takes the form:
        fm1 --> satisfaction 
        fm2 --> satisfaction 
    */


    /**
    * Player constructor
    *
    * @param weeks             number of weeks
    * @param numFamilyMembers  number of family members
    * @param capacity          pantry capacity
    * @param seed              random seed
    * @param simPrinter        simulation printer
    *
    */
    public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
        super(weeks, numFamilyMembers, capacity, seed, simPrinter);
        // TODO: create the breakfast, lunch, and dinner arrays (Nuneke)
        // family satisfaction, all 0 to start off 
        // frequency array, all 0 to start off 
    }

    /**
    * Create shopping list of meals to stock pantry
    *
    * @param week           current week
    * @param numEmptySlots  number of empty slots left in the pantry
    * @param familyMembers  all family members
    * @param pantry         pantry inventory of remaining foods
    * @param mealHistory    history of previous meal allocations
    * @return               shopping list of foods to order
    *
    */
    public ShoppingList stockPantry(Integer week, Integer numEmptySlots, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

        // TODO add your code here to generate a shopping list
        // (Spencer)
        // just add 21 of each meal for each member (enough to last 3 weeks if we cannot buy again)

        return null; // TODO modify the return statement to return your shopping list
    }

    /**
    * Plan meals
    *
    * @param week           current week
    * @param familyMembers  all family members
    * @param pantry         pantry inventory of remaining foods
    * @param mealHistory    history of previous meal allocations
    * @return               planner of assigned meals for the week
    *
    */
    public Planner planMeals(Integer week, List<FamilyMember> familyMembers, Pantry pantry, MealHistory mealHistory) {

        // recalculate family satisfaction with new satisfaction for this week

        // make hashmaps for planned meals
        
        // Spencer: 
        for every day:
            // breakfast
            // never modify breakfast satisfactions after it's created

            // get order of family members (Nuneke's function)
            familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction, utilize satisfaction array 
            // for every family member:
                // for each of that family member's breakfast array (sorted):
                    // assign the meal if it's available & break 
                        // assign = hashmap: family member --> assigned breakfast      

            // recalculate satisfcation
            recalcSatisfaction(hashmap of assigned breakfasts, 0) // --> update lunch + dinner satisfaction matrices




            // lunch
            familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's lunch array (sorted):
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            recalcSatisfaction(hashmap of assigned lunches, 1) // --> update lunch + dinner satisfaction matrices




            // dinner
            familyMemberList = getFamilyMembers() // --> returns orderded list of family members by satisfaction
            // for every family member:
                // for each of that family member's meals:
                    // assign the meal if it's available & break     

            // recalculate satisfcation
            recalcSatisfaction([list of dinners assigned in that day], familyMemberList, 2) // --> update lunch + dinner satisfaction matrices

            // update frequency array
            updateFrequency(hashmap of lunches, hashmap of dinners)
            updatePreferneces(hashmap of lunches, hashmap of dinners)


        // create Planner object 

        return null; // TODO modify the return statement to return your planner
    }


    // Qi
    void updatePreference()
        // updates the preferences for each meal according to the meals that were assigned today 

    // Qi   
    void updateFrequency()
        // remember to change the frequency of meals that weren't eaten too! 
            // 1 meal in the past two days --> 1 meal in the past 3 days if not eaten
            // 1 meal in the past two days --> 2 meals in the past 3 days if eaten
        // updates the frequency array for each meal according to the meals that were assigned today 

    // Nuneke
    void recalcSatisfaction(hashmap of assigned meals, flag digit = 0B, 1L, 2D )
        // updates each individual family member's satisfaction 

    // Nuneke: 
    list getFamilyMembers()
        // looks at family member satsifaction hashmap
        // update each individual satisfaction
        // return ordered list of hashmap keys 

}
