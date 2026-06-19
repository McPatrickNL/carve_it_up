package nl.patrick.carve_it_up;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import nl.patrick.carve_it_up.block.ModBlocks;
import nl.patrick.carve_it_up.item.ModItems;
import nl.patrick.carve_it_up.tab.ModCreativeModeTabs;

public class CarveItUpCommon
{
    public static final String MOD_ID = "ciu";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static void init()
    {
        ModBlocks.init();
        ModItems.init();
        ModCreativeModeTabs.init();
    }
}