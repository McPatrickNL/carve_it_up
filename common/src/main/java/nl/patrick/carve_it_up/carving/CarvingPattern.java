// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingPattern.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Enumeration of available carving tool brush patterns.
 */
public enum CarvingPattern {
    VOXEL("One voxel", "hud/pattern/voxel_icon"),
    MULTI_VOXEL("Multi voxel", "hud/pattern/multi_voxel_icon"),
    LINE("Line", "hud/pattern/line_icon"),
    FACE("Face", "hud/pattern/face_icon"),
    CONNECTED_FACE("Connected Face", "hud/pattern/connected_face_icon");

    private final String name;
    private final Identifier identifier;

    CarvingPattern(String name, String path) {
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