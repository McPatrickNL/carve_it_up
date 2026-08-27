// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/RightClickMode.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Enumeration of right-click actions available on the Carving Tool.
 */
public enum RightClickMode {
    REPLACE_BASE("Replace Base", "hud/right_click/replace_base_icon"),
    COPY("Copy Data", "hud/right_click/copy_icon"),
    PASTE("Paste Data", "hud/right_click/paste_icon");

    private final String name;
    private final Identifier identifier;

    RightClickMode(String name, String path) {
        this.name = name;
        this.identifier = Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public String getName() {
        return this.name;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }
}
