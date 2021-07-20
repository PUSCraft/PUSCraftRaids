package co.linuxman.puscraftraids.raids;

import co.linuxman.puscraftraids.PUSCraftRaids;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RaidManager {
    public boolean isRaidInProgress;
    private ProtectedRegion region;

    public RaidManager(){

    }

    public void newRaid(){

    }

    public void cancelRaid(){
        PUSCraftRaids.cancelledRaid = true;
    }
}
