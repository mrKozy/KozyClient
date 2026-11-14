package com.redstarclient.cosmetics.knife;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

import java.util.Arrays;

public final class KnifeMesh {
    private final float[] pos;
    private final float[] normal;
    private final byte[] material;
    private final float[] tone;
    private final int quads;
    private final float shineFrom;
    private final float shineTo;

    private KnifeMesh(float[] pos, float[] normal, byte[] material, float[] tone, int quads, float shineFrom, float shineTo) {
        this.pos = pos;
        this.normal = normal;
        this.material = material;
        this.tone = tone;
        this.quads = quads;
        this.shineFrom = shineFrom;
        this.shineTo = shineTo;
    }

    public void emit(PoseStack.Pose pose, VertexConsumer out, int[] palette, int light) {
        for (int q = 0; q < this.quads; q++) {
            int mat = this.material[q];
            int argb = palette[mat];
            float nx = this.normal[q * 3];
            float ny = this.normal[q * 3 + 1];
            float nz = this.normal[q * 3 + 2];

            for (int v = 0; v < 4; v++) {
                int p = q * 12 + v * 3;
                float x = this.pos[p];
                float y = this.pos[p + 1];
                float z = this.pos[p + 2];
                float bright = this.tone[q];

                out.addVertex(pose, x, y, z)
                    .setColor(tint(argb, bright))
                    .setUv((x + z) * 0.09F, y * 0.09F)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, nx, ny, nz);
            }
        }
    }

    private static int tint(int argb, float k) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        if (k <= 1.0F) {
            r = (int) (r * k);
            g = (int) (g * k);
            b = (int) (b * k);
        } else {
            float w = Math.min(1.0F, k - 1.0F);
            r += (int) ((255 - r) * w);
            g += (int) ((255 - g) * w);
            b += (int) ((255 - b) * w);
        }
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public static final class Voxels {
        private static final int[][] FACES = new int[][]{
            {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
        };
        private final byte[] cells = new byte[131072];
        private final float[] shade = new float[131072];

        public void set(int x, int y, int z, int material) {
            this.set(x, y, z, material, 1.0F);
        }

        public void set(int x, int y, int z, int material, float tone) {
            int i = index(x, y, z);
            this.cells[i] = (byte) (material + 1);
            this.shade[i] = tone;
        }

        public boolean filled(int x, int y, int z) {
            int ix = x + 64;
            int iy = y + 32;
            int iz = z + 8;
            return ix >= 0 && iy >= 0 && iz >= 0 && ix < 128 && iy < 64 && iz < 16 && this.cells[(ix * 64 + iy) * 16 + iz] != 0;
        }

        public KnifeMesh mesh(float shineFrom, float shineTo) {
            int capacity = 256;
            float[] pos = new float[capacity * 12];
            float[] normal = new float[capacity * 3];
            byte[] material = new byte[capacity];
            float[] tone = new float[capacity];
            int quads = 0;

            for (int ix = 0; ix < 128; ix++) {
                for (int iy = 0; iy < 64; iy++) {
                    for (int iz = 0; iz < 16; iz++) {
                        int cell = (ix * 64 + iy) * 16 + iz;
                        if (this.cells[cell] != 0) {
                            int x = ix - 64;
                            int y = iy - 32;
                            int z = iz - 8;

                            for (int[] f : FACES) {
                                if (!this.filled(x + f[0], y + f[1], z + f[2])) {
                                    if (quads == capacity) {
                                        capacity *= 2;
                                        pos = Arrays.copyOf(pos, capacity * 12);
                                        normal = Arrays.copyOf(normal, capacity * 3);
                                        material = Arrays.copyOf(material, capacity);
                                        tone = Arrays.copyOf(tone, capacity);
                                    }

                                    face(pos, quads * 12, x, y, z, f);
                                    normal[quads * 3]     = f[0];
                                    normal[quads * 3 + 1] = f[1];
                                    normal[quads * 3 + 2] = f[2];
                                    material[quads] = (byte) (this.cells[cell] - 1);
                                    tone[quads] = this.shade[cell];
                                    quads++;
                                }
                            }
                        }
                    }
                }
            }

            return new KnifeMesh(
                Arrays.copyOf(pos, quads * 12),
                Arrays.copyOf(normal, quads * 3),
                Arrays.copyOf(material, quads),
                Arrays.copyOf(tone, quads),
                quads,
                shineFrom,
                shineTo
            );
        }

        private static void face(float[] out, int at, int x, int y, int z, int[] f) {
            float x0 = x, x1 = x + 1;
            float y0 = y, y1 = y + 1;
            float z0 = z, z1 = z + 1;
            float[] c;
            if (f[0] == 1) {
                c = new float[]{x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1};
            } else if (f[0] == -1) {
                c = new float[]{x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0};
            } else if (f[1] == 1) {
                c = new float[]{x0, y1, z1, x1, y1, z1, x1, y1, z0, x0, y1, z0};
            } else if (f[1] == -1) {
                c = new float[]{x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1};
            } else if (f[2] == 1) {
                c = new float[]{x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1};
            } else {
                c = new float[]{x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0};
            }
            System.arraycopy(c, 0, out, at, 12);
        }

        private static int index(int x, int y, int z) {
            return ((x + 64) * 64 + y + 32) * 16 + z + 8;
        }
    }
}
