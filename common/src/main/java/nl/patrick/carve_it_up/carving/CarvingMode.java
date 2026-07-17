package nl.patrick.carve_it_up.carving;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingMode.java

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;


public enum CarvingMode
{
    REMOVE("Remove", "hud_remove_icon"),
    ADD("Add", "hud_add_icon"),
    REPLACE("Replace", "hud_replace_icon");
    
    private final String name;
    private final Identifier identifier;
    
    CarvingMode(String name, String path)
    {
        this.name = name;
        this.identifier = Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public Identifier getIdentifier()
    {
        return this.identifier;
    }
    
    
}