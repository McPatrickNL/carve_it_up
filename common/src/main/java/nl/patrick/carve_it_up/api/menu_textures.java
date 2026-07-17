package nl.patrick.carve_it_up.api;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/api/textures.java

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;


public enum menu_textures
{
    MAIN_MENU(MOD_ID, "hud/main_menu"),
    SUB_MENU_SINGLE(MOD_ID, "hud/sub_menu_single"),
    SUB_MENU_TOP(MOD_ID, "hud/sub_menu_top"),
    SUB_MENU_MIDDLE(MOD_ID, "hud/sub_menu_middle"),
    SUB_MENU_BOTTOM(MOD_ID, "hud/sub_menu_bottom"),
    HIGHLIGHT("hud/hotbar_selection");
    
    private final Identifier identifier;
    
    menu_textures(String mod_id, String path)
    {
        this.identifier = Identifier.fromNamespaceAndPath(mod_id, path);
    }
    
    menu_textures(String path)
    {
        this.identifier = Identifier.withDefaultNamespace(path);
    }
    
    public Identifier getIdentifier()
    {
        return this.identifier;
    }
}
