package com.tdtycoon.plugin.model;

/**
 * One block captured by /copywand + /copydisplay, stored as an offset
 * from the selection's bottom-center-front point, plus its BlockData string.
 * Used to spawn a group of BlockDisplay entities that make up a tower model.
 */
public class RelativeBlock {

    private final double offsetX;
    private final double offsetY;
    private final double offsetZ;
    private final String blockDataString; // e.g. "minecraft:oak_log[axis=y]"

    public RelativeBlock(double offsetX, double offsetY, double offsetZ, String blockDataString) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.blockDataString = blockDataString;
    }

    public double getOffsetX() { return offsetX; }
    public double getOffsetY() { return offsetY; }
    public double getOffsetZ() { return offsetZ; }
    public String getBlockDataString() { return blockDataString; }

    /** Serializes to a single config line: "x,y,z,blockdata" */
    public String serialize() {
        return offsetX + "," + offsetY + "," + offsetZ + "," + blockDataString;
    }

    public static RelativeBlock deserialize(String line) {
        // split only on the first 3 commas since blockDataString may contain commas
        String[] parts = line.split(",", 4);
        double x = Double.parseDouble(parts[0]);
        double y = Double.parseDouble(parts[1]);
        double z = Double.parseDouble(parts[2]);
        String data = parts[3];
        return new RelativeBlock(x, y, z, data);
    }
}
