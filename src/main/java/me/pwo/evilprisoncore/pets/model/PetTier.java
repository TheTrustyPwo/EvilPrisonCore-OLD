package me.pwo.evilprisoncore.pets.model;

public class PetTier {
    private final int id;
    private final String name;
    private final int maxLevel;

    public PetTier(int id, String name, int maxLevel) {
        this.id = id;
        this.name = name;
        this.maxLevel = maxLevel;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
