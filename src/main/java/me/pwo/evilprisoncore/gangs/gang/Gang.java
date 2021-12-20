package me.pwo.evilprisoncore.gangs.gang;

import java.util.List;
import java.util.UUID;

public class Gang {
    private UUID gangId;
    private String gangName;
    private UUID gangOwner;
    private List<UUID> gangMembers;
    private long gangTrophies;
    private String announcement;

    public Gang(UUID gangId, String gangName, UUID gangOwner, List<UUID> gangMembers, long gangTrophies, String announcement) {
        this.gangId = gangId;
        this.gangName = gangName;
        this.gangOwner = gangOwner;
        this.gangMembers = gangMembers;
        this.gangTrophies = gangTrophies;
        this.announcement = announcement;
    }

    public UUID getGangId() {
        return gangId;
    }
}
