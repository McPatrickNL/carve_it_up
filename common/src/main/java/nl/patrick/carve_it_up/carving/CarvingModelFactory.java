// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingModelFactory.java
package nl.patrick.carve_it_up.carving;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory class responsible for server-side carving calculations, dynamic collision
 * and visual shape generation, and client-side greedy-meshed baked model construction.
 */
public class CarvingModelFactory {

    // Client rendering configuration settings
    public static boolean renderingEnabled = true;
    public static boolean skipOtherPlayerCarvings = false;

    // --- ENUMS & RESULT CONTAINER ---

    /**
     * Container holding the result of a server-side carving operation.
     */
    public static class CarvingResult {
        private final int voxelsModified;
        private final List<Block> depletedMaterials;

        public CarvingResult(int voxelsModified, List<Block> depletedMaterials) {
            this.voxelsModified = voxelsModified;
            this.depletedMaterials = depletedMaterials;
        }

        public int getVoxelsModified() {
            return voxelsModified;
        }

        public List<Block> getDepletedMaterials() {
            return depletedMaterials;
        }
    }

    // --- SERVER-SIDE CALCULATIONS ---

    /**
     * Applies a carve action on the server-authoritative voxel grid.
     *
     * @param data The carved data instance representing the block's current voxel state
     * @param mode The carving mode (REMOVE, ADD, REPLACE)
     * @param pattern The carving tool pattern (VOXEL, MULTI_VOXEL, LINE, FACE)
     * @param targetX The target voxel X coordinate within the resolution grid
     * @param targetY The target voxel Y coordinate within the resolution grid
     * @param targetZ The target voxel Z coordinate within the resolution grid
     * @param toolMaterial The block state material to place when in ADD or REPLACE mode
     * @param width The brush width / radius for multi-voxel patterns
     * @param direction The primary placement direction for directional patterns
     * @param face The block face clicked by the player
     * @return CarvingResult containing modification statistics and depleted material types
     */
    public static CarvingResult applyCarvingAction(
        CarvedData data,
        CarvingMode mode,
        CarvingPattern pattern,
        int targetX, int targetY, int targetZ,
        BlockState toolMaterial,
        int width,
        Direction direction,
        Direction face
    ) {
        int resolution = data.getResolution();
        int totalVoxels = data.getTotalVoxels();
        Map<Integer, BlockState> voxelMaterials = data.getVoxelMaterials();

        // 1. Gather all blocks present in grid before action
        Set<Block> blocksBefore = new HashSet<>();
        for (BlockState state : voxelMaterials.values()) {
            if (state != null) {
                blocksBefore.add(state.getBlock());
            }
        }

        // 2. Identify all targeted 3D voxel coordinates
        List<int[]> targets = new ArrayList<>();
        Direction directionalStep = direction != null ? direction : Direction.UP;
        Direction targetedFace = face != null ? face : Direction.UP;

        switch (pattern) {
            case VOXEL:
            case MULTI_VOXEL_2:
            case MULTI_VOXEL_3:
            case MULTI_VOXEL_4:
            case PLANE_2:
            case PLANE_3:
            case PLANE_4: {
                int brushWidth = pattern.getWidth();
                int brushDepth = pattern.getDepth();
                int halfLower = (brushWidth - 1) / 2;
                int halfUpper = brushWidth / 2;

                boolean isAddMode = (mode == CarvingMode.ADD);
                int stepX = isAddMode ? targetedFace.getStepX() : -targetedFace.getStepX();
                int stepY = isAddMode ? targetedFace.getStepY() : -targetedFace.getStepY();
                int stepZ = isAddMode ? targetedFace.getStepZ() : -targetedFace.getStepZ();

                Direction.Axis normalAxis = targetedFace.getAxis();

                for (int d = 0; d < brushDepth; d++) {
                    int baseNormX = targetX + stepX * d;
                    int baseNormY = targetY + stepY * d;
                    int baseNormZ = targetZ + stepZ * d;

                    for (int deltaU = -halfLower; deltaU <= halfUpper; deltaU++) {
                        for (int deltaV = -halfLower; deltaV <= halfUpper; deltaV++) {
                            int vx, vy, vz;
                            if (normalAxis == Direction.Axis.X) {
                                vx = baseNormX;
                                vy = baseNormY + deltaU;
                                vz = baseNormZ + deltaV;
                            } else if (normalAxis == Direction.Axis.Y) {
                                vx = baseNormX + deltaU;
                                vy = baseNormY;
                                vz = baseNormZ + deltaV;
                            } else { // Z
                                vx = baseNormX + deltaU;
                                vy = baseNormY + deltaV;
                                vz = baseNormZ;
                            }
                            targets.add(new int[]{vx, vy, vz});
                        }
                    }
                }
                break;
            }

            case LINE:
                // Step inward into the block along the specified direction
                int stepX = directionalStep.getStepX();
                int stepY = directionalStep.getStepY();
                int stepZ = directionalStep.getStepZ();
                for (int step = 0; step < resolution; step++) {
                    int currentX = targetX + stepX * step;
                    int currentY = targetY + stepY * step;
                    int currentZ = targetZ + stepZ * step;
                    if (currentX < 0 || currentX >= resolution || currentY < 0 || currentY >= resolution || currentZ < 0 || currentZ >= resolution) {
                        break;
                    }
                    targets.add(new int[]{currentX, currentY, currentZ});
                }
                break;

            case FACE:
                Direction.Axis axis = targetedFace.getAxis();
                if (axis == Direction.Axis.X) {
                    for (int yCoord = 0; yCoord < resolution; yCoord++) {
                        for (int zCoord = 0; zCoord < resolution; zCoord++) {
                            targets.add(new int[]{targetX, yCoord, zCoord});
                        }
                    }
                } else if (axis == Direction.Axis.Y) {
                    for (int xCoord = 0; xCoord < resolution; xCoord++) {
                        for (int zCoord = 0; zCoord < resolution; zCoord++) {
                            targets.add(new int[]{xCoord, targetY, zCoord});
                        }
                    }
                } else { // Z axis
                    for (int xCoord = 0; xCoord < resolution; xCoord++) {
                        for (int yCoord = 0; yCoord < resolution; yCoord++) {
                            targets.add(new int[]{xCoord, yCoord, targetZ});
                        }
                    }
                }
                break;

            case CONNECTED_FACE: {
                Direction.Axis faceAxis = targetedFace.getAxis();
                boolean[][] visited2D = new boolean[resolution][resolution];
                java.util.Queue<int[]> queue = new java.util.ArrayDeque<>();

                boolean isAddMode = (mode == CarvingMode.ADD);

                int clickedX = isAddMode ? (targetX - targetedFace.getStepX()) : targetX;
                int clickedY = isAddMode ? (targetY - targetedFace.getStepY()) : targetY;
                int clickedZ = isAddMode ? (targetZ - targetedFace.getStepZ()) : targetZ;

                int startU, startV;
                if (faceAxis == Direction.Axis.X) {
                    startU = clickedY;
                    startV = clickedZ;
                } else if (faceAxis == Direction.Axis.Y) {
                    startU = clickedX;
                    startV = clickedZ;
                } else { // Z
                    startU = clickedX;
                    startV = clickedY;
                }

                if (startU >= 0 && startU < resolution && startV >= 0 && startV < resolution) {
                    int startIdx = (faceAxis == Direction.Axis.X) ? (clickedX + startU * resolution + startV * resolution * resolution)
                                 : ((faceAxis == Direction.Axis.Y) ? (startU + clickedY * resolution + startV * resolution * resolution)
                                 : (startU + startV * resolution + clickedZ * resolution * resolution));

                    if (voxelMaterials.containsKey(startIdx)) {
                        visited2D[startU][startV] = true;
                        queue.add(new int[]{startU, startV});

                        int[][] neighborOffsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

                        while (!queue.isEmpty()) {
                            int[] current = queue.poll();
                            int cu = current[0];
                            int cv = current[1];

                            int targetVoxelX = (faceAxis == Direction.Axis.X) ? targetX : ((faceAxis == Direction.Axis.Y) ? cu : cu);
                            int targetVoxelY = (faceAxis == Direction.Axis.X) ? cu : ((faceAxis == Direction.Axis.Y) ? targetY : cv);
                            int targetVoxelZ = (faceAxis == Direction.Axis.X) ? cv : ((faceAxis == Direction.Axis.Y) ? cv : targetZ);

                            targets.add(new int[]{targetVoxelX, targetVoxelY, targetVoxelZ});

                            for (int[] offset : neighborOffsets) {
                                int nu = cu + offset[0];
                                int nv = cv + offset[1];

                                if (nu >= 0 && nu < resolution && nv >= 0 && nv < resolution && !visited2D[nu][nv]) {
                                    int nIdx = (faceAxis == Direction.Axis.X) ? (clickedX + nu * resolution + nv * resolution * resolution)
                                             : ((faceAxis == Direction.Axis.Y) ? (nu + clickedY * resolution + nv * resolution * resolution)
                                             : (nu + nv * resolution + clickedZ * resolution * resolution));

                                    if (voxelMaterials.containsKey(nIdx)) {
                                        visited2D[nu][nv] = true;
                                        queue.add(new int[]{nu, nv});
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }

        // 3. Apply action based on mode
        int modifiedCount = 0;
        BlockState activeMaterial = toolMaterial != null ? toolMaterial : data.getOriginalBlockState();

        if (mode == CarvingMode.ROTATE_Y_CW || mode == CarvingMode.ROTATE_Y_CCW || mode == CarvingMode.ROTATE_X_UP || mode == CarvingMode.ROTATE_X_DOWN) {
            Map<Integer, BlockState> rotated = new java.util.HashMap<>();
            for (Map.Entry<Integer, BlockState> entry : voxelMaterials.entrySet()) {
                int idx = entry.getKey();
                int vx = idx % resolution;
                int vy = (idx / resolution) % resolution;
                int vz = idx / (resolution * resolution);

                int nx, ny, nz;
                switch (mode) {
                    case ROTATE_Y_CW:
                        nx = resolution - 1 - vz;
                        ny = vy;
                        nz = vx;
                        break;
                    case ROTATE_Y_CCW:
                        nx = vz;
                        ny = vy;
                        nz = resolution - 1 - vx;
                        break;
                    case ROTATE_X_UP:
                        nx = vx;
                        ny = resolution - 1 - vz;
                        nz = vy;
                        break;
                    case ROTATE_X_DOWN:
                    default:
                        nx = vx;
                        ny = vz;
                        nz = resolution - 1 - vy;
                        break;
                }

                int newIdx = nx + (ny * resolution) + (nz * resolution * resolution);
                rotated.put(newIdx, entry.getValue());
            }
            voxelMaterials.clear();
            voxelMaterials.putAll(rotated);
            modifiedCount = 1;
        } else {
            for (int[] target : targets) {
                int tx = target[0];
                int ty = target[1];
                int tz = target[2];

                if (tx < 0 || tx >= resolution || ty < 0 || ty >= resolution || tz < 0 || tz >= resolution) {
                    continue;
                }

                int index = tx + (ty * resolution) + (tz * resolution * resolution);

                switch (mode) {
                    case REMOVE:
                        if (voxelMaterials.containsKey(index)) {
                            voxelMaterials.remove(index);
                            modifiedCount++;
                        }
                        break;

                    case ADD:
                        if (!voxelMaterials.containsKey(index)) {
                            voxelMaterials.put(index, activeMaterial);
                            data.addBlock(activeMaterial.getBlock());
                            modifiedCount++;
                        }
                        break;

                    case REPLACE:
                        if (voxelMaterials.containsKey(index)) {
                            BlockState previous = voxelMaterials.get(index);
                            if (previous != activeMaterial) {
                                voxelMaterials.put(index, activeMaterial);
                                data.addBlock(activeMaterial.getBlock());
                                modifiedCount++;
                            }
                        }
                        break;
                }
            }
        }

        // 4. Determine depleted materials and update data blocks
        Set<Block> blocksAfter = new HashSet<>();
        for (BlockState state : voxelMaterials.values()) {
            if (state != null) {
                blocksAfter.add(state.getBlock());
            }
        }

        List<Block> depletedMaterials = new ArrayList<>();
        for (Block block : blocksBefore) {
            if (!blocksAfter.contains(block)) {
                depletedMaterials.add(block);
                data.removeBlock(block);
            }
        }

        // Dynamically recompute authoritative collision shapes on the server
        if (modifiedCount > 0) {
            VoxelShape updatedShape = calculateCollisionShape(data);
            data.setCollisionShape(updatedShape);
            data.setVisualShape(updatedShape);
            data.setInteractionShape(updatedShape);
            data.incrementVersion();
        }

        return new CarvingResult(modifiedCount, depletedMaterials);
    }

    // --- CLIENT-SIDE MODEL BAKING ---

    /**
     * Determines whether client-side custom model baking and rendering should be bypassed.
     *
     * @param definitions The carved data definition for the block
     * @return True if custom rendering should be bypassed in favor of the vanilla model
     */
    public static boolean shouldBypassRendering(CarvedData definitions) {
        if (!renderingEnabled) {
            return true;
        }
        if (skipOtherPlayerCarvings) {
            try {
                net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && !player.getUUID().equals(definitions.getOwnerUuid())) {
                    return true;
                }
            } catch (Throwable throwable) {
                // Safeguard for non-client or early-loading contexts
            }
        }
        return false;
    }

    /**
     * Dynamically bakes a custom 3D greedy-meshed BlockStateModel from the carved data definitions.
     * Separates outer boundary quads (culled against neighbors) from internal cavity quads (unculled).
     *
     * @param definitions The carved data containing voxel occupancy and materials
     * @return A baked BlockStateModel representing the carved block
     */
    public static BlockStateModel bakeCustomModel(CarvedData definitions) {
        // Validation check
        if (shouldBypassRendering(definitions)) {
            return resolveOriginalBlockStateModel(definitions);
        }

        int resolution = definitions.getResolution();
        Map<Integer, BlockState> voxelMaterials = definitions.getVoxelMaterials();
        BlockStateModel originalModel = resolveOriginalBlockStateModel(definitions);

        // Dynamically compute physics, collision and interaction shapes
        VoxelShape computedShape = calculateCollisionShape(definitions);
        definitions.setVisualShape(computedShape);
        definitions.setCollisionShape(computedShape);
        definitions.setInteractionShape(computedShape);

        Map<Direction, List<BakedQuad>> culledQuadsMap = new EnumMap<>(Direction.class);
        List<BakedQuad> unculledQuadsList = new ArrayList<>();
        BakedQuad.MaterialInfo fallbackInfo = getOriginalMaterialInfo(originalModel);

        for (Direction direction : Direction.values()) {
            List<BakedQuad> directionBoundaryQuads = new ArrayList<>();
            compileQuadsForDirection(definitions, direction, fallbackInfo, directionBoundaryQuads, unculledQuadsList);
            culledQuadsMap.put(direction, directionBoundaryQuads);
        }

        // Copy layout parameters from the original model
        Material.Baked particleMaterial = originalModel.particleMaterial();
        int originalFlags = originalModel.materialFlags();
        boolean useAmbientOcclusion = true;
        List<BlockStateModelPart> originalParts = new ArrayList<>();
        originalModel.collectParts(RandomSource.create(), originalParts);
        if (!originalParts.isEmpty()) {
            useAmbientOcclusion = originalParts.get(0).useAmbientOcclusion();
        }

        return new CarvedBlockStateModel(culledQuadsMap, unculledQuadsList, particleMaterial, originalFlags, useAmbientOcclusion);
    }

    // NewStart Determine if a block state is solid opaque vs transparent
    public static boolean isSolidOpaque(BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        return !state.getOcclusionShape().isEmpty() && !state.propagatesSkylightDown();
    }

    public static boolean shouldRenderFace(BlockState current, BlockState adjacent, Direction direction) {
        if (current == null || current.isAir()) {
            return false;
        }
        if (adjacent == null || adjacent.isAir()) {
            return true;
        }
        if (current == adjacent) {
            return false;
        }

        boolean currentSolid = isSolidOpaque(current);
        boolean adjacentSolid = isSolidOpaque(adjacent);

        if (currentSolid && !adjacentSolid) {
            // Solid voxel touching a transparent voxel (e.g. Oak Wood touching Glass):
            // Render the solid face to eliminate x-ray holes
            return true;
        } else if (!currentSolid && adjacentSolid) {
            // Transparent voxel touching a solid voxel:
            // Culled against the solid backplate
            return false;
        } else if (!currentSolid && !adjacentSolid) {
            // Different transparent voxels (e.g. Red Glass next to Blue Glass):
            return true;
        } else {
            // Both are solid opaque voxels (e.g. Stone next to Wood):
            return false;
        }
    }
    // NewEnd

    private static void compileQuadsForDirection(
        CarvedData definitions,
        Direction direction,
        BakedQuad.MaterialInfo fallbackInfo,
        List<BakedQuad> culledQuadsList,
        List<BakedQuad> unculledQuadsList
    ) {
        int resolution = definitions.getResolution();
        Map<Integer, BlockState> voxelMaterials = definitions.getVoxelMaterials();

        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDir = direction.getAxisDirection();
        int normalStep = axisDir.getStep();

        for (int wVal = 0; wVal < resolution; wVal++) {
            BlockState[][] faceMaterials = new BlockState[resolution][resolution];

            // 1. Build a 2D slice grid representing only visible voxel faces for this direction
            for (int v = 0; v < resolution; v++) {
                for (int u = 0; u < resolution; u++) {
                    int cx = (axis == Direction.Axis.X) ? wVal : ((axis == Direction.Axis.Y) ? u : u);
                    int cy = (axis == Direction.Axis.X) ? v : ((axis == Direction.Axis.Y) ? wVal : v);
                    int cz = (axis == Direction.Axis.X) ? u : ((axis == Direction.Axis.Y) ? v : wVal);

                    BlockState current = getVoxelState(cx, cy, cz, resolution, voxelMaterials);
                    if (current != null) {
                        int ax = cx + (axis == Direction.Axis.X ? normalStep : 0);
                        int ay = cy + (axis == Direction.Axis.Y ? normalStep : 0);
                        int az = cz + (axis == Direction.Axis.Z ? normalStep : 0);
                        BlockState adj = getVoxelState(ax, ay, az, resolution, voxelMaterials);
                        if (shouldRenderFace(current, adj, direction)) {
                            faceMaterials[u][v] = current;
                        }
                    }
                }
            }

            // 2. Perform 2D greedy meshing
            boolean[][] visited = new boolean[resolution][resolution];
            for (int v = 0; v < resolution; v++) {
                for (int u = 0; u < resolution; u++) {
                    BlockState material = faceMaterials[u][v];
                    if (material != null && !visited[u][v]) {
                        // Expand U (width)
                        int w = 1;
                        while (u + w < resolution && faceMaterials[u + w][v] == material && !visited[u + w][v]) {
                            w++;
                        }

                        // Expand V (height)
                        int h = 1;
                        while (v + h < resolution) {
                            boolean ok = true;
                            for (int k = 0; k < w; k++) {
                                if (faceMaterials[u + k][v + h] != material || visited[u + k][v + h]) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (!ok) break;
                            h++;
                        }

                        // Mark rectangle cells as visited
                        for (int dy = 0; dy < h; dy++) {
                            for (int dx = 0; dx < w; dx++) {
                                visited[u + dx][v + dy] = true;
                            }
                        }

                        // Voxel coordinates to normalized coordinates [0.0, 1.0]
                        float minU = (float) u / resolution;
                        float maxU = (float) (u + w) / resolution;
                        float minV = (float) v / resolution;
                        float maxV = (float) (v + h) / resolution;
                        float normalW = (float) (axisDir == Direction.AxisDirection.POSITIVE ? wVal + 1 : wVal) / resolution;

                        // Calculate continuous UV texture coordinates aligned with world axes
                        org.joml.Vector3f p0, p1, p2, p3;
                        long uv0, uv1, uv2, uv3;

                        // Retrieve active material info
                        BakedQuad.MaterialInfo matInfo = getMaterialInfo(material, direction, fallbackInfo);
                        if (matInfo != null) {
                            TextureAtlasSprite sprite = matInfo.sprite();
                            float spriteMinU = sprite.getU0();
                            float spriteMaxU = sprite.getU1();
                            float spriteMinV = sprite.getV0();
                            float spriteMaxV = sprite.getV1();

                            float texMinU = spriteMinU + (spriteMaxU - spriteMinU) * minU;
                            float texMaxU = spriteMinU + (spriteMaxU - spriteMinU) * maxU;

                            // For vertical faces, invert V so higher Y aligns with the top of the sprite (spriteMinV)
                            float texTopV = spriteMinV + (spriteMaxV - spriteMinV) * (1.0f - maxV);
                            float texBottomV = spriteMinV + (spriteMaxV - spriteMinV) * (1.0f - minV);

                            // For horizontal faces, V maps directly along the Z axis
                            float texMinZ = spriteMinV + (spriteMaxV - spriteMinV) * minV;
                            float texMaxZ = spriteMinV + (spriteMaxV - spriteMinV) * maxV;

                            if (direction == Direction.DOWN) {
                                p0 = new org.joml.Vector3f(minU, normalW, maxV);
                                p1 = new org.joml.Vector3f(minU, normalW, minV);
                                p2 = new org.joml.Vector3f(maxU, normalW, minV);
                                p3 = new org.joml.Vector3f(maxU, normalW, maxV);

                                uv0 = packUV(texMinU, texMaxZ);
                                uv1 = packUV(texMinU, texMinZ);
                                uv2 = packUV(texMaxU, texMinZ);
                                uv3 = packUV(texMaxU, texMaxZ);
                            } else if (direction == Direction.UP) {
                                p0 = new org.joml.Vector3f(minU, normalW, minV);
                                p1 = new org.joml.Vector3f(minU, normalW, maxV);
                                p2 = new org.joml.Vector3f(maxU, normalW, maxV);
                                p3 = new org.joml.Vector3f(maxU, normalW, minV);

                                uv0 = packUV(texMinU, texMinZ);
                                uv1 = packUV(texMinU, texMaxZ);
                                uv2 = packUV(texMaxU, texMaxZ);
                                uv3 = packUV(texMaxU, texMinZ);
                            } else if (direction == Direction.NORTH) {
                                p0 = new org.joml.Vector3f(maxU, maxV, normalW);
                                p1 = new org.joml.Vector3f(maxU, minV, normalW);
                                p2 = new org.joml.Vector3f(minU, minV, normalW);
                                p3 = new org.joml.Vector3f(minU, maxV, normalW);

                                uv0 = packUV(texMaxU, texTopV);
                                uv1 = packUV(texMaxU, texBottomV);
                                uv2 = packUV(texMinU, texBottomV);
                                uv3 = packUV(texMinU, texTopV);
                            } else if (direction == Direction.SOUTH) {
                                p0 = new org.joml.Vector3f(minU, maxV, normalW);
                                p1 = new org.joml.Vector3f(minU, minV, normalW);
                                p2 = new org.joml.Vector3f(maxU, minV, normalW);
                                p3 = new org.joml.Vector3f(maxU, maxV, normalW);

                                uv0 = packUV(texMinU, texTopV);
                                uv1 = packUV(texMinU, texBottomV);
                                uv2 = packUV(texMaxU, texBottomV);
                                uv3 = packUV(texMaxU, texTopV);
                            } else if (direction == Direction.WEST) {
                                p0 = new org.joml.Vector3f(normalW, maxV, minU);
                                p1 = new org.joml.Vector3f(normalW, minV, minU);
                                p2 = new org.joml.Vector3f(normalW, minV, maxU);
                                p3 = new org.joml.Vector3f(normalW, maxV, maxU);

                                uv0 = packUV(texMinU, texTopV);
                                uv1 = packUV(texMinU, texBottomV);
                                uv2 = packUV(texMaxU, texBottomV);
                                uv3 = packUV(texMaxU, texTopV);
                            } else { // EAST
                                p0 = new org.joml.Vector3f(normalW, maxV, maxU);
                                p1 = new org.joml.Vector3f(normalW, minV, maxU);
                                p2 = new org.joml.Vector3f(normalW, minV, minU);
                                p3 = new org.joml.Vector3f(normalW, maxV, minU);

                                uv0 = packUV(texMaxU, texTopV);
                                uv1 = packUV(texMaxU, texBottomV);
                                uv2 = packUV(texMinU, texBottomV);
                                uv3 = packUV(texMinU, texTopV);
                            }

                            BakedQuad quad = new BakedQuad(
                                p0, p1, p2, p3,
                                uv0, uv1, uv2, uv3,
                                direction,
                                matInfo
                            );

                            boolean isOuterBoundary = (axisDir == Direction.AxisDirection.NEGATIVE && wVal == 0)
                                || (axisDir == Direction.AxisDirection.POSITIVE && wVal == resolution - 1);
                            if (isOuterBoundary) {
                                culledQuadsList.add(quad);
                            } else {
                                unculledQuadsList.add(quad);
                            }
                        }
                    }
                }
            }
        }
    }

    private static BlockState getVoxelState(int x, int y, int z, int resolution, Map<Integer, BlockState> voxelMaterials) {
        if (x < 0 || x >= resolution || y < 0 || y >= resolution || z < 0 || z >= resolution) {
            return null;
        }
        int index = x + (y * resolution) + (z * resolution * resolution);
        return voxelMaterials.get(index);
    }

    private static long packUV(float u, float v) {
        return ((long) Float.floatToIntBits(u) & 0xFFFFFFFFL) << 32 | ((long) Float.floatToIntBits(v) & 0xFFFFFFFFL);
    }

    private static BlockStateModel resolveOriginalBlockStateModel(CarvedData definitions) {
        return Minecraft.getInstance()
                        .getModelManager()
                        .getBlockStateModelSet()
                        .get(definitions.getOriginalBlockState());
    }

    /**
     * Looks up the material and sprite info for a given block state and direction.
     *
     * @param state The block state to sample texture info from
     * @param direction The face direction to query
     * @param fallback The fallback material info if none is discovered
     * @return The resolved MaterialInfo for quad baking
     */
    public static BakedQuad.MaterialInfo getMaterialInfo(BlockState state, Direction direction, BakedQuad.MaterialInfo fallback) {
        try {
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
            if (model != null) {
                List<BlockStateModelPart> parts = new ArrayList<>();
                model.collectParts(RandomSource.create(), parts);
                for (BlockStateModelPart part : parts) {
                    List<BakedQuad> quads = part.getQuads(direction);
                    if (quads != null && !quads.isEmpty()) {
                        return quads.get(0).materialInfo();
                    }
                }
                // Try other directions
                for (Direction otherDirection : Direction.values()) {
                    for (BlockStateModelPart part : parts) {
                        List<BakedQuad> quads = part.getQuads(otherDirection);
                        if (quads != null && !quads.isEmpty()) {
                            return quads.get(0).materialInfo();
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            // Safe fallback for server/non-client calls
        }
        return fallback;
    }

    /**
     * Extracts the primary material info from an original block state model.
     *
     * @param originalModel The original BlockStateModel
     * @return The extracted MaterialInfo or null if not found
     */
    public static BakedQuad.MaterialInfo getOriginalMaterialInfo(BlockStateModel originalModel) {
        if (originalModel != null) {
            List<BlockStateModelPart> parts = new ArrayList<>();
            originalModel.collectParts(RandomSource.create(), parts);
            for (BlockStateModelPart part : parts) {
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> quads = part.getQuads(direction);
                    if (quads != null && !quads.isEmpty()) {
                        return quads.get(0).materialInfo();
                    }
                }
            }
        }
        return null;
    }

    // --- PHYSICS COLLISION SHAPES ---

    /**
     * Computes the optimized VoxelShape bounding boxes from the 3D voxel grid.
     *
     * @param data The carved data holding the voxel matrix
     * @return An optimized compound VoxelShape
     */
    public static VoxelShape calculateCollisionShape(CarvedData data) {
        int resolution = data.getResolution();
        boolean[][][] occupied = new boolean[resolution][resolution][resolution];
        Map<Integer, BlockState> materials = data.getVoxelMaterials();
        boolean hasAny = false;

        for (int iterationIndex = 0; iterationIndex < data.getTotalVoxels(); iterationIndex++) {
            if (materials.containsKey(iterationIndex)) {
                int x = iterationIndex % resolution;
                int y = (iterationIndex / resolution) % resolution;
                int z = iterationIndex / (resolution * resolution);
                occupied[x][y][z] = true;
                hasAny = true;
            }
        }

        if (!hasAny) {
            return Shapes.empty();
        }

        VoxelShape shape = Shapes.empty();
        boolean[][][] visited = new boolean[resolution][resolution][resolution];
        double res = resolution;

        for (int y = 0; y < resolution; y++) {
            for (int z = 0; z < resolution; z++) {
                for (int x = 0; x < resolution; x++) {
                    if (occupied[x][y][z] && !visited[x][y][z]) {
                        // Expand along X
                        int dx = 1;
                        while (x + dx < resolution && occupied[x + dx][y][z] && !visited[x + dx][y][z]) {
                            dx++;
                        }

                        // Expand along Z
                        int dz = 1;
                        while (z + dz < resolution) {
                            boolean ok = true;
                            for (int k = 0; k < dx; k++) {
                                if (!occupied[x + k][y][z + dz] || visited[x + k][y][z + dz]) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (!ok) break;
                            dz++;
                        }

                        // Expand along Y
                        int dy = 1;
                        while (y + dy < resolution) {
                            boolean ok = true;
                            for (int k = 0; k < dx; k++) {
                                for (int m = 0; m < dz; m++) {
                                    if (!occupied[x + k][y + dy][z + m] || visited[x + k][y + dy][z + m]) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (!ok) break;
                            }
                            if (!ok) break;
                            dy++;
                        }

                        // Mark cuboid voxels as visited
                        for (int ny = y; ny < y + dy; ny++) {
                            for (int nz = z; nz < z + dz; nz++) {
                                for (int nx = x; nx < x + dx; nx++) {
                                    visited[nx][ny][nz] = true;
                                }
                            }
                        }

                        // Add shapes box
                        VoxelShape box = Shapes.box(
                            x / res, y / res, z / res,
                            (x + dx) / res, (y + dy) / res, (z + dz) / res
                        );
                        shape = Shapes.or(shape, box);
                    }
                }
            }
        }

        return shape.optimize();
    }

    // NewStart Populates a CarvedData instance from the initial bounding boxes of the original BlockState
    public static void populateFromShape(CarvedData carvedData, BlockState state, net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos) {
        int resolution = carvedData.getResolution();
        carvedData.getVoxelMaterials().clear();

        VoxelShape shape = null;
        try {
            if (level != null && pos != null) {
                shape = state.getShape(level, pos, net.minecraft.world.phys.shapes.CollisionContext.empty());
            }
        } catch (Throwable ignored) {}

        if (shape == null || shape.isEmpty()) {
            shape = Shapes.block();
        }

        double voxelSize = 1.0 / resolution;
        double halfVoxel = voxelSize / 2.0;

        for (int z = 0; z < resolution; z++) {
            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    double centerX = (x * voxelSize) + halfVoxel;
                    double centerY = (y * voxelSize) + halfVoxel;
                    double centerZ = (z * voxelSize) + halfVoxel;

                    boolean inside = false;
                    for (net.minecraft.world.phys.AABB aabb : shape.toAabbs()) {
                        if (centerX >= aabb.minX && centerX <= aabb.maxX &&
                            centerY >= aabb.minY && centerY <= aabb.maxY &&
                            centerZ >= aabb.minZ && centerZ <= aabb.maxZ) {
                            inside = true;
                            break;
                        }
                    }

                    if (inside) {
                        int index = x + (y * resolution) + (z * resolution * resolution);
                        carvedData.getVoxelMaterials().put(index, state);
                    }
                }
            }
        }

        carvedData.rebuildBlockPalette();
        VoxelShape computedShape = calculateCollisionShape(carvedData);
        carvedData.setCollisionShape(computedShape);
        carvedData.setVisualShape(computedShape);
        carvedData.setInteractionShape(computedShape);
    }
    // NewEnd

    // --- CUSTOM BLOCK STATE MODEL WRAPPER ---

    /**
     * BlockStateModel wrapper implementation providing baked quads for custom carved blocks.
     */
    public static class CarvedBlockStateModel implements BlockStateModel, BlockStateModelPart {
        private final Map<Direction, List<BakedQuad>> culledQuadsMap;
        private final List<BakedQuad> unculledQuads;
        private final Material.Baked particleMaterial;
        private final int materialFlags;
        private final boolean useAmbientOcclusion;

        public CarvedBlockStateModel(
            Map<Direction, List<BakedQuad>> culledQuadsMap,
            List<BakedQuad> unculledQuads,
            Material.Baked particleMaterial,
            int materialFlags,
            boolean useAmbientOcclusion
        ) {
            this.culledQuadsMap = culledQuadsMap;
            this.unculledQuads = unculledQuads;
            this.particleMaterial = particleMaterial;
            this.materialFlags = materialFlags;
            this.useAmbientOcclusion = useAmbientOcclusion;
        }

        @Override
        public int materialFlags() {
            return this.materialFlags;
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.particleMaterial;
        }

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
            parts.add(this);
        }

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            if (direction == null) {
                return this.unculledQuads;
            }
            return this.culledQuadsMap.getOrDefault(direction, Collections.emptyList());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.useAmbientOcclusion;
        }
    }
}
