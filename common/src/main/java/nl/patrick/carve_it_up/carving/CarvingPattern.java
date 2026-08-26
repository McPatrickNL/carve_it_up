// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingPattern.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.resources.Identifier;

import static nl.patrick.carve_it_up.CommonMod.MOD_ID;

/**
 * Enumeration of available carving tool brush patterns.
 */
public enum CarvingPattern {
    VOXEL("1x1x1 Voxel", "hud/pattern/voxel_icon", 1, 1),
    MULTI_VOXEL_2("2x2x2 Cube", "hud/pattern/multi_voxel_2_icon", 2, 2),
    MULTI_VOXEL_3("3x3x3 Cube", "hud/pattern/multi_voxel_3_icon", 3, 3),
    MULTI_VOXEL_4("4x4x4 Cube", "hud/pattern/multi_voxel_4_icon", 4, 4),
    PLANE_2("2x2x1 Plane", "hud/pattern/plane_2_icon", 2, 1),
    PLANE_3("3x3x1 Plane", "hud/pattern/plane_3_icon", 3, 1),
    PLANE_4("4x4x1 Plane", "hud/pattern/plane_4_icon", 4, 1),
    LINE("Line", "hud/pattern/line_icon", 1, 16),
    FACE("Face", "hud/pattern/face_icon", 16, 1),
    CONNECTED_FACE("Connected Face", "hud/pattern/connected_face_icon", 16, 1);

    private final String name;
    private final Identifier identifier;
    private final int width;
    private final int depth;

    CarvingPattern(String name, String path, int width, int depth) {
        this.name = name;
        this.identifier = Identifier.fromNamespaceAndPath(MOD_ID, path);
        this.width = width;
        this.depth = depth;
    }

    public String getName() {
        return this.name;
    }

    public Identifier getIdentifier() {
        return this.identifier;
    }

    public int getWidth() {
        return this.width;
    }

    public int getDepth() {
        return this.depth;
    }
}