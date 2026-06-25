// common/src/main/java/nl/patrick/carve_it_up/CarveItUpCommon.java
package nl.patrick.carve_it_up;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/CarveItUpCommon.java
public class CommonMod
{
    //public static final String MOD_ID = "carve_it_up";
    public static final String MOD_ID = "carve_it_up";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static void init()
    {
        LOGGER.info("Initialising Common Setup for Carve It Up.");
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTabs.init();
    }
}