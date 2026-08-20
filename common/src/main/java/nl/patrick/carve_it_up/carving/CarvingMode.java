// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingMode.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Enumeration of available carving modes including voxel modification and 3D block rotations.
 */
public enum CarvingMode {
    REMOVE("Remove", "hud/mode/remove_icon"),
    ADD("Add", "hud/mode/add_icon"),
    REPLACE("Replace", "hud/mode/replace_icon"),
    ROTATE_Y_CW("Rotate Right", "hud/mode/rotate_right_icon"),
    ROTATE_Y_CCW("Rotate Left", "hud/mode/rotate_left_icon"),
    ROTATE_X_UP("Rotate Up", "hud/mode/rotate_up_icon"),
    ROTATE_X_DOWN("Rotate Down", "hud/mode/rotate_down_icon");

    private final String name;
    private final Identifier identifier;

    CarvingMode(String name, String path) {
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