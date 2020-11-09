package menu.g5;

import java.util.*;

import menu.sim.*;
import menu.sim.Food.FoodType;
import menu.sim.Food.MealType;


public class Player extends menu.sim.Player {
	
	private HashMap<MemberName, Map<FoodType, Integer>> lunchServedDaysBefore;
	private HashMap<FoodType, Integer> dinnerServedDaysBefore;
	private Map<MemberName, Map<MealType, List<memberFoodSatis>>> memberMap;
	private Map<MemberName, Map<MealType, List<memberFoodSatis>>> originMemberMap;
	private Map<MealType, Map<FoodType, Integer>> idealPantry;
	private Map<MealType, Map<FoodType, Integer>> backupPantry;
	private Map<MealType, Map<FoodType, Integer>> backupPantry2;
	private Map<MealType, Map<FoodType, Integer>> backupPantry3;
	private Map<MealType, Map<FoodType, Integer>> backupPantry4;

    /**
     * Player constructor
     *
     * @param weeks             number of weeks
     * @param numFamilyMembers  number of family members
     * @param capacity			pantry capacity
     * @param seed        		random seed
     * @param simPrinter   		simulation printer
     *
     */
	public Player(Integer weeks, Integer numFamilyMembers, Integer capacity, Integer seed, SimPrinter simPrinter) {
		super(weeks, numFamilyMembers, capacity, seed, simPrinter);
	}
	
	private void initializeMap(List<FamilyMember> familyMembers) {
		lunchServedDaysBefore = new HashMap<MemberName, Map<FoodType, Integer>>();
		for (FamilyMember fm: familyMembers) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(MealType.LUNCH)) {
				m.put(f, 0);
			}
			lunchServedDaysBefore.put(fm.getName(), m);
		}

		dinnerServedDaysBefore = new HashMap<FoodType, Integer>();
		for (FoodType f: Food.getFoodTypes(MealType.DINNER)) {
			dinnerServedDaysBefore.put(f, 0);
		}
		
		memberMap = new HashMap<MemberName, Map<MealType, List<memberFoodSatis>>>();
		for (FamilyMember fm: familyMembers) {
			Map<MealType, List<memberFoodSatis>> memberFavorite = getFavorite(fm);
			memberMap.put(fm.getName(), memberFavorite);
		}
		
		originMemberMap = new HashMap<MemberName, Map<MealType, List<memberFoodSatis>>>();
		for (FamilyMember fm: familyMembers) {
			Map<MealType, List<memberFoodSatis>> memberFavorite = getFavorite(fm);
			originMemberMap.put(fm.getName(), memberFavorite);
		}
		
		idealPantry = new HashMap<MealType, Map<FoodType, Integer>>();
		int eachMember = capacity / familyMembers.size();
		for (MealType mt: MealType.values()) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(mt)) {
				m.put(f, 0);
			}
			idealPantry.put(mt, m);
		}

		backupPantry = new HashMap<MealType, Map<FoodType, Integer>>();
		for (MealType mt: MealType.values()) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(mt)) {
				m.put(f, 0);
			}
			backupPantry.put(mt, m);
		}

		backupPantry2 = new HashMap<MealType, Map<FoodType, Integer>>();
		for (MealType mt: MealType.values()) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(mt)) {
				m.put(f, 0);
			}
			backupPantry2.put(mt, m);
		}

		backupPantry3 = new HashMap<MealType, Map<FoodType, Integer>>();
		for (MealType mt: MealType.values()) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(mt)) {
				m.put(f, 0);
			}
			backupPantry3.put(mt, m);
		}

		backupPantry4 = new HashMap<MealType, Map<FoodType, Integer>>();
		for (MealType mt: MealType.values()) {
			Map<FoodType, Integer> m = new HashMap<FoodType, Integer>();
			for (FoodType f: Food.getFoodTypes(mt)) {
				m.put(f, 0);
			}
			backupPantry4.put(mt, m);
		}

		for (FamilyMember fm: familyMembers) {
			for (MealType mt: MealType.values()) {
				int cap = (mt == MealType.DINNER ? eachMember/2 : eachMember/4);
				updateBackupPantry(mt, fm.getName(), cap);
			}
		}

		//System.out.println(idealPantry);

		for (FamilyMember fm: familyMembers) {
			for (MealType mt: MealType.values()) {
				int cap = (mt == MealType.DINNER ? eachMember/2 : eachMember/4);
				addEachFoodWeighted(mt, fm.getName(), cap);
			}
		}

		//System.out.println(idealPantry);
	}
	
	private void incrementDay() {
		for (MemberName mn: lunchServedDaysBefore.keySet()) {
			Map<FoodType, Integer> m = lunchServedDaysBefore.get(mn);
			for (FoodType f : m.keySet()) {
				m.put(f, m.get(f)+1);
			}
		}
		
		for (FoodType f : dinnerServedDaysBefore.keySet()) {
			dinnerServedDaysBefore.put(f, dinnerServedDaysBefore.get(f)+1);
		}
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
    public ShoppingList stockPantry(Integer week,
    								Integer numEmptySlots,
    								List<FamilyMember> familyMembers,
    								Pantry pantry,
    								MealHistory mealHistory) {
    	//System.out.println("Week: "+ week+"; shop");
    	if (week == 1) {
    		initializeMap(familyMembers);
    	}
    	ShoppingList shoppingList = new ShoppingList();
    	int space = numEmptySlots;
    	Map<MealType, Integer> limits = new HashMap<MealType, Integer>();
    	ArrayList<FoodType> addList = new ArrayList<FoodType>();
    	
    	for (MealType mt: MealType.values()) {
    		int before = space;
    		
    		Map<FoodType, Integer> m = idealPantry.get(mt);
    		//System.out.println(m);
    		for (FoodType f: m.keySet()) {
    			List<FoodType> left = pantry.getAvailableFoodTypes(mt);
    			int need = m.get(f);
    			if (left.contains(f)) {
    				need = pantry.getNumAvailableMeals() - m.get(f);	
    			}
    			if (need <= 0) {
					continue;
				} else {
					if (space >= need) {
						space -= need;
						for (int i=0; i< need; i++) {
							shoppingList.addToOrder(f);
							addList.add(f);
						}
					} else {
						for (int i=0; i< space; i++) {
							shoppingList.addToOrder(f);
							addList.add(f);
						}
						space = 0;
						break;
					}
				}
    		}
    		
    		limits.put(mt, before-space);
    	}

    	//System.out.println("ShoppingList map before: ");
    	//System.out.println(shoppingList.getFullOrderMap());

		for (MealType mt: MealType.values()) {
			int before = space;

			Map<FoodType, Integer> m = backupPantry.get(mt);
			for (FoodType f: m.keySet()) {
				List<FoodType> left = pantry.getAvailableFoodTypes(mt);
				int need = m.get(f);
				if (left.contains(f)) {
					need = pantry.getNumAvailableMeals() - m.get(f);
				}
				if (need <= 0) {
					continue;
				} else {
					if (space >= need) {
						space -= need;
						for (int i=0; i< need; i++) {
							shoppingList.addToOrder(f);
							addList.add(f);
						}
					} else {
						for (int i=0; i< space; i++) {
							shoppingList.addToOrder(f);
							addList.add(f);
						}
						space = 0;
						break;
					}
				}
			}
		}

    	if (space > 0) {
    		//System.out.println("Space left: " + space);
    		limits.put(MealType.DINNER, limits.get(MealType.DINNER)+space);
    	}

    	for (MealType mt: limits.keySet()) {
    		shoppingList.addLimit(mt, limits.get(mt));
    	}

		for (int j=0; j< addList.size(); j++) {
			shoppingList.addToOrder(addList.get(j));
		}

		//System.out.println("ShoppingList map after: ");
		//System.out.println(shoppingList.getFullOrderMap());

    	//System.out.println("ShoppingList limits: ");
    	//System.out.println(shoppingList.getAllLimitsMap());
    	
    	
    	
    	if(Player.hasValidShoppingList(shoppingList, numEmptySlots)) {
    		return shoppingList;
    	}
    	//System.out.println("Not valid shopping list");
    	return new ShoppingList();
    }

    private List<FoodType> getSecondaryList(List<FamilyMember> familyMembers) {
		List<FoodType> addList = new ArrayList<FoodType>();


		return addList;
	}
    
    
    /**
     * 
     * @param mt
     * @param mn
     * @param cap	capacity for family member fm for meal type mt
     * @return
     */
    public Map<FoodType, Integer> addEachFoodWeighted(MealType mt, MemberName mn, int cap) {
    	List<memberFoodSatis> l = originMemberMap.get(mn).get(mt);
    	Map<FoodType, Integer> m = idealPantry.get(mt);
    	if (cap >= 20) {
    		int eachFood = cap/(5+4+3+2+1*6);
    		int position = 0;
    		for (memberFoodSatis mfs: l) {
    			if (position == 0) {
    				m.put(mfs.f, m.get(mfs.f)+eachFood*5);
    			} else if (position == 1) {
    				m.put(mfs.f, m.get(mfs.f)+eachFood*4);
    			} else if (position == 2) {
    				m.put(mfs.f, m.get(mfs.f)+eachFood*3);
    			} else if (position == 3) {
    				m.put(mfs.f, m.get(mfs.f)+eachFood*2);
    			} else {
    				m.put(mfs.f, m.get(mfs.f)+eachFood);
    			}
    			position++;
    		}
    		
    	} else {
    		int capleft = cap;
    		int position = 0;
    		for (memberFoodSatis mfs: l) {
    			if (position == 0) {
    				if (capleft >= 5) {
    					m.put(mfs.f, m.get(mfs.f)+5);
    					capleft -= 5;
    				} else {
    					m.put(mfs.f, m.get(mfs.f)+capleft);
    					break;
    				}
    			} else if (position == 1) {
    				if (capleft >= 4) {
    					m.put(mfs.f, m.get(mfs.f)+4);
    					capleft -= 4;
    				} else {
    					m.put(mfs.f, m.get(mfs.f)+capleft);
    					break;
    				}
    			} else if (position == 2) {
    				if (capleft >= 3) {
    					m.put(mfs.f, m.get(mfs.f)+3);
    					capleft -= 3;
    				} else {
    					m.put(mfs.f, m.get(mfs.f)+capleft);
    					break;
    				}
    			} else if (position == 3) {
    				if (capleft >= 2) {
    					m.put(mfs.f, m.get(mfs.f)+2);
    					capleft -= 2;
    				} else {
    					m.put(mfs.f, m.get(mfs.f)+capleft);
    					break;
    				}
    			} else {
    				if (capleft >= 1) {
    					m.put(mfs.f, m.get(mfs.f)+1);
    					capleft -= 1;
    				} else {
    					break;
    				}
    			}
    		}
    	}
    	
    	return m;
    }

	public Map<FoodType, Integer> updateBackupPantry(MealType mt, MemberName mn, int cap) {
		List<memberFoodSatis> l = originMemberMap.get(mn).get(mt);
		Map<FoodType, Integer> m = backupPantry.get(mt);
		if (cap >= 20) {
			int eachFood = cap/(5+4+3+2+1*6);
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					m.put(mfs.f, m.get(mfs.f)+eachFood);
				} else if (position == 1) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*5);
				} else if (position == 2) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*4);
				} else if (position == 3) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*3);
				} else {
					m.put(mfs.f, m.get(mfs.f)+eachFood*2);
				}
				position++;
			}

		} else {
			int capleft = cap;
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					if (capleft >= 5) {
						m.put(mfs.f, m.get(mfs.f));
						capleft -= 5;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 1) {
					if (capleft >= 4) {
						m.put(mfs.f, m.get(mfs.f)+5);
						capleft -= 4;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 2) {
					if (capleft >= 3) {
						m.put(mfs.f, m.get(mfs.f)+4);
						capleft -= 3;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 3) {
					if (capleft >= 2) {
						m.put(mfs.f, m.get(mfs.f)+3);
						capleft -= 2;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else {
					if (capleft >= 1) {
						m.put(mfs.f, m.get(mfs.f)+2);
						capleft -= 1;
					} else {
						break;
					}
				}
			}
		}

		m = backupPantry2.get(mt);
		if (cap >= 20) {
			int eachFood = cap/(5+4+3+2+1*6);
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					m.put(mfs.f, m.get(mfs.f)+eachFood);
				} else if (position == 1) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*2);
				} else if (position == 2) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*5);
				} else if (position == 3) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*4);
				} else {
					m.put(mfs.f, m.get(mfs.f)+eachFood*3);
				}
				position++;
			}

		} else {
			int capleft = cap;
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					if (capleft >= 5) {
						m.put(mfs.f, m.get(mfs.f));
						capleft -= 5;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 1) {
					if (capleft >= 4) {
						m.put(mfs.f, m.get(mfs.f)+2);
						capleft -= 4;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 2) {
					if (capleft >= 3) {
						m.put(mfs.f, m.get(mfs.f)+5);
						capleft -= 3;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 3) {
					if (capleft >= 2) {
						m.put(mfs.f, m.get(mfs.f)+4);
						capleft -= 2;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else {
					if (capleft >= 1) {
						m.put(mfs.f, m.get(mfs.f)+3);
						capleft -= 1;
					} else {
						break;
					}
				}
			}
		}

		m = backupPantry3.get(mt);
		if (cap >= 20) {
			int eachFood = cap/(5+4+3+2+1*6);
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					m.put(mfs.f, m.get(mfs.f)+eachFood);
				} else if (position == 1) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*2);
				} else if (position == 2) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*3);
				} else if (position == 3) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*5);
				} else {
					m.put(mfs.f, m.get(mfs.f)+eachFood*4);
				}
				position++;
			}

		} else {
			int capleft = cap;
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					if (capleft >= 5) {
						m.put(mfs.f, m.get(mfs.f));
						capleft -= 5;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 1) {
					if (capleft >= 4) {
						m.put(mfs.f, m.get(mfs.f)+2);
						capleft -= 4;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 2) {
					if (capleft >= 3) {
						m.put(mfs.f, m.get(mfs.f)+3);
						capleft -= 3;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 3) {
					if (capleft >= 2) {
						m.put(mfs.f, m.get(mfs.f)+5);
						capleft -= 2;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else {
					if (capleft >= 1) {
						m.put(mfs.f, m.get(mfs.f)+4);
						capleft -= 1;
					} else {
						break;
					}
				}
			}
		}

		m = backupPantry4.get(mt);
		if (cap >= 20) {
			int eachFood = cap/(5+4+3+2+1*6);
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					m.put(mfs.f, m.get(mfs.f)+eachFood);
				} else if (position == 1) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*2);
				} else if (position == 2) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*3);
				} else if (position == 3) {
					m.put(mfs.f, m.get(mfs.f)+eachFood*4);
				} else {
					m.put(mfs.f, m.get(mfs.f)+eachFood*5);
				}
				position++;
			}

		} else {
			int capleft = cap;
			int position = 0;
			for (memberFoodSatis mfs: l) {
				if (position == 0) {
					if (capleft >= 5) {
						m.put(mfs.f, m.get(mfs.f));
						capleft -= 5;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 1) {
					if (capleft >= 4) {
						m.put(mfs.f, m.get(mfs.f)+2);
						capleft -= 4;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 2) {
					if (capleft >= 3) {
						m.put(mfs.f, m.get(mfs.f)+3);
						capleft -= 3;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else if (position == 3) {
					if (capleft >= 2) {
						m.put(mfs.f, m.get(mfs.f)+4);
						capleft -= 2;
					} else {
						m.put(mfs.f, m.get(mfs.f)+capleft);
						break;
					}
				} else {
					if (capleft >= 1) {
						m.put(mfs.f, m.get(mfs.f)+5);
						capleft -= 1;
					} else {
						break;
					}
				}
			}
		}

		return m;
	}
    
    private class memberFoodSatis {
    	FoodType f;
    	double s;
    	double cs;
    	
    	memberFoodSatis(FoodType f, double s) {
    		this.f = f;
    		this.s = s;
    		this.cs = s;
    	}
    }

    private void updateMemberFavorite(FamilyMember fm) {
    	MemberName mn = fm.getName();
    	Map<MealType, List<memberFoodSatis>> m = memberMap.get(mn);
		for (MealType mt: m.keySet()) {
			if (mt == MealType.BREAKFAST) {
	    		continue;
	    	}
			List<memberFoodSatis> l = m.get(mt);
			for (memberFoodSatis mfs: l) {
	    		FoodType f = mfs.f;
	    		int days = (mt == MealType.LUNCH ? lunchServedDaysBefore.get(mn).get(f):dinnerServedDaysBefore.get(f));
	    		mfs.cs = mfs.s * days/(days+1);
	    	}
	    	Collections.sort(l, new sortmfs());
			Collections.reverse(l);
		}
    }
    
    private Map<MealType, List<memberFoodSatis>> getFavorite(FamilyMember m) {
    	Map<FoodType, Double> foodPreferenceMap = m.getFoodPreferenceMap();
    	Map<MealType, List<memberFoodSatis>> memberFavorite = new HashMap<MealType, List<memberFoodSatis>>();
    	for (MealType mt: MealType.values()) {
    		memberFavorite.put(mt, new ArrayList<memberFoodSatis>());
    	}
    	
    	for (FoodType f: foodPreferenceMap.keySet()) {
    		memberFavorite.get(Food.getMealType(f)).add(new memberFoodSatis(f, foodPreferenceMap.get(f)));
    	}
    	for (MealType mt: memberFavorite.keySet()) {
    		List<memberFoodSatis> l = memberFavorite.get(mt);
    		Collections.sort(l, new sortmfs());
    		Collections.reverse(l);
    	}
    	return memberFavorite;
    }
    
    private class sortmfs implements Comparator<memberFoodSatis> {
    	public int compare(memberFoodSatis mfs1, memberFoodSatis mfs2) {
        	double diff = mfs1.cs - mfs2.cs;
        	if (diff < 0) {
        		return -1;
        	}
        	if (diff > 0) {
        		return 1;
        	}
        	return 0;
        }
    }
    
    public void printMemberMap() {
    	for (MemberName mn: memberMap.keySet()) {
    		System.out.println(mn+": ");
    		for (MealType mt: MealType.values()) {
    			System.out.println(mt+": ");
    			List<memberFoodSatis> l = memberMap.get(mn).get(mt);
    			for (memberFoodSatis mfs: l) {
    				double b = mfs.cs * 100;
    				double c = (int)b;
    				double a = c/100;
    				System.out.print(mfs.f+" "+a+"; ");
    			}
    			System.out.print("\n");
    		}
    		System.out.println();
    	}
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
    public Planner planMeals(Integer week,
    						 List<FamilyMember> familyMembers,
    						 Pantry pantry,
    						 MealHistory mealHistory) {

    	//System.out.println("Week: "+week+"; plan");
//    	for (MealType mt: MealType.values()) {
//    		System.out.println("mt: "+ mt);
//    		System.out.println(pantry.getNumAvailableMeals(mt));
//    	}
    	
    	//printMemberMap();
    	List<MemberName> memberNames = new ArrayList<>();
    	for(FamilyMember familyMember : familyMembers)
    		memberNames.add(familyMember.getName());
    	Planner planner = new Planner(memberNames);
    	
    	Pantry originalPantry = pantry.clone();
    	
    	
    	for (Day d: Day.values()) {
    		incrementDay();
    		for (FamilyMember fm: familyMembers) {
    			updateMemberFavorite(fm);
    		}
    		Map<FoodType, Double> bestTotalSatisfaction = new HashMap<FoodType, Double>();
    		for (FoodType f: Food.getFoodTypes(MealType.DINNER)) {
    			bestTotalSatisfaction.put(f, 0.0);
    		}
    		for (FamilyMember fm: familyMembers) {
    			MemberName mn = fm.getName();
    			for (MealType mt: MealType.values()) {
    				List<FoodType> avaliable = pantry.getAvailableFoodTypes(mt);
    				List<memberFoodSatis> l = memberMap.get(mn).get(mt);
    				if (mt == MealType.DINNER) {
    					for (memberFoodSatis mfs: l) {
    						if  (avaliable.contains(mfs.f)) {
    							double value = bestTotalSatisfaction.get(mfs.f);
    							double newValue = mfs.cs;
    							int num = pantry.getNumAvailableMeals(mfs.f);
    							if (num < familyMembers.size()) {
    								newValue *= num/familyMembers.size();
    							}
    							bestTotalSatisfaction.put(mfs.f, value+newValue);
    						}
    					}
    				} else {
    					for (memberFoodSatis mfs: l) {
    						if  (avaliable.contains(mfs.f)) {
    							int num = pantry.getNumAvailableMeals(mfs.f);
        						if (num > 0) {
        							planner.addMeal(d, fm.getName(), mt, mfs.f);
        							pantry.removeMealFromInventory(mfs.f);
        							if (mt == MealType.LUNCH) {
        								lunchServedDaysBefore.get(mn).put(mfs.f, 0);
        							}
        							break;
        						}
    						}
    					}
    				}
    			}
    		}
    		double highest = 0;
    		FoodType hf = null;
    		for (FoodType f: bestTotalSatisfaction.keySet()) {
    			if (bestTotalSatisfaction.get(f)>highest) {
    				highest = bestTotalSatisfaction.get(f);
    				hf = f;
    			}
    		}
    		if (highest > 0) {
    			for (FamilyMember fm: familyMembers) {
        			MemberName mn = fm.getName();
        			planner.addMeal(d, mn, MealType.DINNER, hf);
        			pantry.removeMealFromInventory(hf);
        		}
    		}
    		
    	}
    	
    	//printPlan(planner);
    	
    	if(Player.hasValidPlanner(planner, originalPantry)) {
    		return planner;
    	}
    	System.out.println("Not valid planner");
    	return new Planner();
    }
    
    public void printPlan(Planner p) {
    	Map<Day, Map<MemberName, Map<MealType, FoodType>>> m = p.getPlan();
    	for (Day d: m.keySet()) {
    		System.out.println("Day: "+d);
    		Map<MemberName, Map<MealType, FoodType>> m1 = m.get(d);
    		for (MemberName mn: m1.keySet()) {
    			System.out.println("MemberName: "+mn);
    			Map<MealType, FoodType> m2 = m1.get(mn);
    			System.out.println("Map: "+m2);
    		}
    		System.out.println();
    	}
    }
    
    private FoodType getMaximumAvailableMeal(Pantry pantry, MealType mealType) {
    	FoodType maximumAvailableMeal = null;
    	int maxAvailableMeals = -1;
    	for(FoodType foodType : Food.getFoodTypes(mealType)) {
    		int numAvailableMeals = pantry.getNumAvailableMeals(foodType);
    		if(numAvailableMeals > maxAvailableMeals) {
    			maxAvailableMeals = numAvailableMeals;
    			maximumAvailableMeal = foodType;
    		}
    	}
    	return maximumAvailableMeal;
    }
}
