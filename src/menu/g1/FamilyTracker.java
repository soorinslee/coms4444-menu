package menu.g1;

import menu.sim.FamilyMember;
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
            members.put(member.getName(), new MemberTracker(member));
        }
    }

    public void update(Integer week, MealHistory mealHistory) {
        for (MemberTracker member: members.values()) {
            member.update(week, mealHistory, DEFAULT_SCALE);
        }
    }

    public MemberTracker getMemberTracker(MemberName memberName) {
        return members.get(memberName);
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
