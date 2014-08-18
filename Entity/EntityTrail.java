/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Entity;

import Reika.DragonAPI.Base.InertEntity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityTrail extends InertEntity {

	public static final int LIFE = 480;

	public EntityTrail(World par1World) {
		super(par1World);
	}

	public EntityTrail(World world, double x, double y, double z) {
		super(world);
		this.setPosition(x, y, z);
	}

	@Override
	protected void entityInit() {

	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {

	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (ticksExisted > LIFE)
			this.setDead();
	}

	@Override
	public boolean isInRangeToRenderDist(double par1)
	{
		return true;
	}

}
