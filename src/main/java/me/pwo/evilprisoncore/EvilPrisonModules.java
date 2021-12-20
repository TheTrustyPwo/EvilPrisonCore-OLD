package me.pwo.evilprisoncore;

public interface EvilPrisonModules {
    void enable();
    void disable();
    void reload();
    boolean isEnabled();
    String getName();
}
