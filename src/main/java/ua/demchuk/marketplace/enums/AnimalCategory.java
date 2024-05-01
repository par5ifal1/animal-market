package ua.demchuk.marketplace.enums;

import lombok.Getter;

@Getter
public enum AnimalCategory {
    FIRST(0, 20),
    SECOND(21, 40),
    THIRD(41, 60),
    FOURTH(61, Integer.MAX_VALUE);

    private final int minCost;
    private final int maxCost;

    AnimalCategory(int minCost, int maxCost) {
        this.minCost = minCost;
        this.maxCost = maxCost;
    }

    public static AnimalCategory getCategoryForCost(int cost) {
        for (AnimalCategory category : AnimalCategory.values()) {
            if (cost >= category.minCost && cost <= category.maxCost) {
                return category;
            }
        }
        throw new IllegalArgumentException("No category for cost: " + cost);
    }

    public static AnimalCategory getCategoryForInteger(int value) {
        for (AnimalCategory category : AnimalCategory.values()) {
            if (value == category.ordinal() + 1) {
                return category;
            }
        }
        throw new IllegalArgumentException("No category for value: " + value);
    }
}
