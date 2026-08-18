// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/CarveItUpCommon.java

package nl.patrick.carve_it_up;

import com.mojang.logging.LogUtils;
import nl.patrick.carve_it_up.component.ModComponents;
import org.slf4j.Logger;

import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;


public class CommonMod
{
    /*
     * TO-DO SECTION
     */
    
    // Future add a check allowed to break? -> Then also allowed to chisel.
    
    // Future add public functions to set locks or something for other mods.
    
    // Future add more hooks for other mods in general.
    
    // Removed example text
    
    // Review the below addition as it requires a reference to it in class x.
    
    // Review the below comment as it might be redundant.
    
    // NewStart Added and example integer call
    
    public static final int example = 1;
    
    // NewEnd
    
    
    //public static final String MOD_ID = "carve_it_up";
    public static final String MOD_ID = "carve_it_up";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static void init()
    {
        LOGGER.info("Initialising Common Setup for Carve It Up.");
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTabs.init();
        ModComponents.init();
    }
}