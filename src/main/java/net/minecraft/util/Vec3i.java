package net.minecraft.util;

import com.google.common.base.MoreObjects;

public class Vec3i implements Comparable<Vec3i> {
    public static final Vec3i NULL_VECTOR = new Vec3i(0, 0, 0);
    private final int x;
    private final int y;
    private final int z;

    public Vec3i(int xIn, int yIn, int zIn) {
        this.x = xIn;
        this.y = yIn;
        this.z = zIn;
    }

    public Vec3i(double xIn, double yIn, double zIn) {
        this(MathHelper.floor(xIn), MathHelper.floor(yIn), MathHelper.floor(zIn));
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof Vec3i vec3i)) {
            return false;
        } else {
            return this.getX() == vec3i.getX() && (this.getY() == vec3i.getY() && this.getZ() == vec3i.getZ());
        }
    }

    public int hashCode() {
        return (this.getY() + this.getZ() * 31) * 31 + this.getX();
    }

    public int compareTo(Vec3i p_compareTo_1_) {
        return this.getY() == p_compareTo_1_.getY() ? (this.getZ() == p_compareTo_1_.getZ() ? this.getX() - p_compareTo_1_.getX() : this.getZ() - p_compareTo_1_.getZ()) : this.getY() - p_compareTo_1_.getY();
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public Vec3i crossProduct(Vec3i vec) {
        return new Vec3i(this.getY() * vec.getZ() - this.getZ() * vec.getY(), this.getZ() * vec.getX() - this.getX() * vec.getZ(), this.getX() * vec.getY() - this.getY() * vec.getX());
    }

    public double distanceSq(double toX, double toY, double toZ) {
        double d0 = this.getX() - toX;
        double d1 = this.getY() - toY;
        double d2 = this.getZ() - toZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceSqToCenter(double xIn, double yIn, double zIn) {
        double d0 = this.getX() + 0.5D - xIn;
        double d1 = this.getY() + 0.5D - yIn;
        double d2 = this.getZ() + 0.5D - zIn;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double distanceSq(Vec3i to) {
        return this.distanceSq(to.getX(), to.getY(), to.getZ());
    }

    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).add("z", this.getZ()).toString();
    }
}
