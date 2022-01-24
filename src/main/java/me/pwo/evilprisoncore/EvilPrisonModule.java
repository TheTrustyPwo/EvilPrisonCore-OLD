package me.pwo.evilprisoncore;

public interface EvilPrisonModule {
    void enable();
    void disable();
    void reload();
    boolean isEnabled();
    String getName();
}
