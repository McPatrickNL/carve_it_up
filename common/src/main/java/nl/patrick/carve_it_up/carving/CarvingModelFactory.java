package nl.patrick.carve_it_up.carving;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

// File Location from project root:
// common/src/main/java/nl/patrick/carve_it_up/carving/CarvingModelFactory.java

public class CarvingModelFactory {

    // Client rendering configuration settings
    public static boolean renderingEnabled = true;
    public static boolean skipOtherPlayerCarvings = false;

    // --- ENUMS & RESULT CONTAINER ---

    public enum CarvingMode {
        REMOVE("Remove"),
        ADD("Add"),
        REPLACE("Replace");
        
        private final String name;
        
        CarvingMode(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return this.name;
        }
    }

    public enum CarvingPattern {
        VOXEL("One voxel"),
        MULTI_VOXEL("Multi voxel"),
        LINE("Line"),
        FACE("Face");
        
        private final String name;
        
        CarvingPattern(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return this.name;
        }
    }

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
        Direction dir = direction != null ? direction : Direction.UP;
        Direction f = face != null ? face : Direction.UP;

        switch (pattern) {
            case VOXEL:
                targets.add(new int[]{targetX, targetY, targetZ});
                break;

            case MULTI_VOXEL:
                int halfLower = (width - 1) / 2;
                int halfUpper = width / 2;
                for (int dx = -halfLower; dx <= halfUpper; dx++) {
                    for (int dy = -halfLower; dy <= halfUpper; dy++) {
                        for (int dz = -halfLower; dz <= halfUpper; dz++) {
                            targets.add(new int[]{targetX + dx, targetY + dy, targetZ + dz});
                        }
                    }
                }
                break;

            case LINE:
                int stepX = dir.getStepX();
                int stepY = dir.getStepY();
                int stepZ = dir.getStepZ();
                for (int step = 0; step < resolution; step++) {
                    int cx = targetX + stepX * step;
                    int cy = targetY + stepY * step;
                    int cz = targetZ + stepZ * step;
                    if (cx < 0 || cx >= resolution || cy < 0 || cy >= resolution || cz < 0 || cz >= resolution) {
                        break;
                    }
                    targets.add(new int[]{cx, cy, cz});
                }
                break;

            case FACE:
                Direction.Axis axis = f.getAxis();
                if (axis == Direction.Axis.X) {
                    for (int y = 0; y < resolution; y++) {
                        for (int z = 0; z < resolution; z++) {
                            targets.add(new int[]{targetX, y, z});
                        }
                    }
                } else if (axis == Direction.Axis.Y) {
                    for (int x = 0; x < resolution; x++) {
                        for (int z = 0; z < resolution; z++) {
                            targets.add(new int[]{x, targetY, z});
                        }
                    }
                } else { // Z axis
                    for (int x = 0; x < resolution; x++) {
                        for (int y = 0; y < resolution; y++) {
                            targets.add(new int[]{x, y, targetZ});
                        }
                    }
                }
                break;
        }

        // 3. Apply action based on mode
        int modifiedCount = 0;
        BlockState activeMaterial = toolMaterial != null ? toolMaterial : data.getOriginalBlockState();

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
                        BlockState previous = voxelMaterials.put(index, activeMaterial);
                        if (previous != activeMaterial) {
                            data.addBlock(activeMaterial.getBlock());
                            modifiedCount++;
                        }
                    }
                    break;
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
        for (Block b : blocksBefore) {
            if (!blocksAfter.contains(b)) {
                depletedMaterials.add(b);
                data.removeBlock(b);
            }
        }

        // Increment data version to invalidate client caches
        if (modifiedCount > 0) {
            data.incrementVersion();
        }

        return new CarvingResult(modifiedCount, depletedMaterials);
    }

    // --- CLIENT-SIDE MODEL BAKING ---

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
            } catch (Throwable t) {
                // Safeguard for non-client or early-loading contexts
            }
        }
        return false;
    }

    public static BlockStateModel bakeCustomModel(CarvedData definitions) {
        // Validation check
        if (shouldBypassRendering(definitions)) {
            return definitions.getOriginalBlockStateModel();
        }

        int resolution = definitions.getResolution();
        Map<Integer, BlockState> voxelMaterials = definitions.getVoxelMaterials();
        BlockStateModel originalModel = definitions.getOriginalBlockStateModel();

        // Dynamically compute physics, collision and interaction shapes
        VoxelShape computedShape = calculateCollisionShape(definitions);
        definitions.setVisualShape(computedShape);
        definitions.setCollisionShape(computedShape);
        definitions.setInteractionShape(computedShape);

        // Compile greedy-meshed quads for all 6 directions
        Map<Direction, List<BakedQuad>> quadsMap = new EnumMap<>(Direction.class);
        BakedQuad.MaterialInfo fallbackInfo = getOriginalMaterialInfo(originalModel);

        for (Direction direction : Direction.values()) {
            List<BakedQuad> directionQuads = new ArrayList<>();
            compileQuadsForDirection(definitions, direction, fallbackInfo, directionQuads);
            quadsMap.put(direction, directionQuads);
        }

        // Copy layout parameters from the original model
        Material.Baked particleMaterial = originalModel.particleMaterial();
        int originalFlags = originalModel.materialFlags();
        boolean useAO = true;
        List<BlockStateModelPart> originalParts = new ArrayList<>();
        originalModel.collectParts(RandomSource.create(), originalParts);
        if (!originalParts.isEmpty()) {
            useAO = originalParts.get(0).useAmbientOcclusion();
        }

        return new CarvedBlockStateModel(quadsMap, particleMaterial, originalFlags, useAO);
    }

    private static void compileQuadsForDirection(
        CarvedData definitions,
        Direction direction,
        BakedQuad.MaterialInfo fallbackInfo,
        List<BakedQuad> quadsList
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
                        if (adj == null) {
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

                        // Winding order-dependent corners
                        org.joml.Vector3f p0, p1, p2, p3;
                        if (direction == Direction.DOWN) {
                            p0 = new org.joml.Vector3f(minU, normalW, maxV);
                            p1 = new org.joml.Vector3f(minU, normalW, minV);
                            p2 = new org.joml.Vector3f(maxU, normalW, minV);
                            p3 = new org.joml.Vector3f(maxU, normalW, maxV);
                        } else if (direction == Direction.UP) {
                            p0 = new org.joml.Vector3f(minU, normalW, minV);
                            p1 = new org.joml.Vector3f(minU, normalW, maxV);
                            p2 = new org.joml.Vector3f(maxU, normalW, maxV);
                            p3 = new org.joml.Vector3f(maxU, normalW, minV);
                        } else if (direction == Direction.NORTH) {
                            p0 = new org.joml.Vector3f(maxU, minV, normalW);
                            p1 = new org.joml.Vector3f(maxU, maxV, normalW);
                            p2 = new org.joml.Vector3f(minU, maxV, normalW);
                            p3 = new org.joml.Vector3f(minU, minV, normalW);
                        } else if (direction == Direction.SOUTH) {
                            p0 = new org.joml.Vector3f(minU, minV, normalW);
                            p1 = new org.joml.Vector3f(minU, maxV, normalW);
                            p2 = new org.joml.Vector3f(maxU, maxV, normalW);
                            p3 = new org.joml.Vector3f(maxU, minV, normalW);
                        } else if (direction == Direction.WEST) {
                            p0 = new org.joml.Vector3f(normalW, minV, minU);
                            p1 = new org.joml.Vector3f(normalW, maxV, minU);
                            p2 = new org.joml.Vector3f(normalW, maxV, maxU);
                            p3 = new org.joml.Vector3f(normalW, minV, maxU);
                        } else { // EAST
                            p0 = new org.joml.Vector3f(normalW, minV, maxU);
                            p1 = new org.joml.Vector3f(normalW, maxV, maxU);
                            p2 = new org.joml.Vector3f(normalW, maxV, minU);
                            p3 = new org.joml.Vector3f(normalW, minV, minU);
                        }

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
                            float texMinV = spriteMinV + (spriteMaxV - spriteMinV) * minV;
                            float texMaxV = spriteMinV + (spriteMaxV - spriteMinV) * maxV;

                            long uv0, uv1, uv2, uv3;
                            if (direction == Direction.DOWN) {
                                uv0 = packUV(texMinU, texMaxV);
                                uv1 = packUV(texMinU, texMinV);
                                uv2 = packUV(texMaxU, texMinV);
                                uv3 = packUV(texMaxU, texMaxV);
                            } else if (direction == Direction.UP) {
                                uv0 = packUV(texMinU, texMinV);
                                uv1 = packUV(texMinU, texMaxV);
                                uv2 = packUV(texMaxU, texMaxV);
                                uv3 = packUV(texMaxU, texMinV);
                            } else if (direction == Direction.NORTH) {
                                uv0 = packUV(texMaxU, texMinV);
                                uv1 = packUV(texMaxU, texMaxV);
                                uv2 = packUV(texMinU, texMaxV);
                                uv3 = packUV(texMinU, texMinV);
                            } else if (direction == Direction.SOUTH) {
                                uv0 = packUV(texMinU, texMinV);
                                uv1 = packUV(texMinU, texMaxV);
                                uv2 = packUV(texMaxU, texMaxV);
                                uv3 = packUV(texMaxU, texMinV);
                            } else if (direction == Direction.WEST) {
                                uv0 = packUV(texMinU, texMinV);
                                uv1 = packUV(texMinU, texMaxV);
                                uv2 = packUV(texMaxU, texMaxV);
                                uv3 = packUV(texMaxU, texMinV);
                            } else { // EAST
                                uv0 = packUV(texMaxU, texMinV);
                                uv1 = packUV(texMaxU, texMaxV);
                                uv2 = packUV(texMinU, texMaxV);
                                uv3 = packUV(texMinU, texMinV);
                            }

                            BakedQuad quad = new BakedQuad(
                                p0, p1, p2, p3,
                                uv0, uv1, uv2, uv3,
                                direction,
                                matInfo
                            );
                            quadsList.add(quad);
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
                for (Direction d : Direction.values()) {
                    for (BlockStateModelPart part : parts) {
                        List<BakedQuad> quads = part.getQuads(d);
                        if (quads != null && !quads.isEmpty()) {
                            return quads.get(0).materialInfo();
                        }
                    }
                }
            }
        } catch (Throwable t) {
            // Safe fallback for server/non-client calls
        }
        return fallback;
    }

    public static BakedQuad.MaterialInfo getOriginalMaterialInfo(BlockStateModel originalModel) {
        if (originalModel != null) {
            List<BlockStateModelPart> parts = new ArrayList<>();
            originalModel.collectParts(RandomSource.create(), parts);
            for (BlockStateModelPart part : parts) {
                for (Direction d : Direction.values()) {
                    List<BakedQuad> quads = part.getQuads(d);
                    if (quads != null && !quads.isEmpty()) {
                        return quads.get(0).materialInfo();
                    }
                }
            }
        }
        return null;
    }

    // --- PHYSICS COLLISION SHAPES ---

    public static VoxelShape calculateCollisionShape(CarvedData data) {
        int resolution = data.getResolution();
        boolean[][][] occupied = new boolean[resolution][resolution][resolution];
        Map<Integer, BlockState> materials = data.getVoxelMaterials();
        boolean hasAny = false;

        for (int i = 0; i < data.getTotalVoxels(); i++) {
            if (materials.containsKey(i)) {
                int x = i % resolution;
                int y = (i / resolution) % resolution;
                int z = i / (resolution * resolution);
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

    // --- CUSTOM BLOCK STATE MODEL WRAPPER ---

    public static class CarvedBlockStateModel implements BlockStateModel, BlockStateModelPart {
        private final Map<Direction, List<BakedQuad>> quadsMap;
        private final Material.Baked particleMaterial;
        private final int materialFlags;
        private final boolean useAmbientOcclusion;

        public CarvedBlockStateModel(
            Map<Direction, List<BakedQuad>> quadsMap,
            Material.Baked particleMaterial,
            int materialFlags,
            boolean useAmbientOcclusion
        ) {
            this.quadsMap = quadsMap;
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
            return this.quadsMap.getOrDefault(direction, Collections.emptyList());
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.useAmbientOcclusion;
        }
    }
}
