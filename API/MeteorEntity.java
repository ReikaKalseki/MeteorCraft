package Reika.MeteorCraft.API;

import net.minecraft.block.Block;
import net.minecraft.util.Vec3;

public interface MeteorEntity {

	public Vec3 getVelocityVector();

	public Vec3 getProjectedIntercept(double yLevel);

	public Vec3 getSpawnPosition();

	public void destroy();

	public Block getMeteorRockType();

}
