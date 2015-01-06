/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Event;

import net.minecraft.world.World;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Entity.EntityMeteor;

public class EntryEvent extends MeteorEvent {

	public final double x;
	public final double y;
	public final double z;
	public final double vx;
	public final double vy;
	public final double vz;
	public final World world;
	public final MeteorType type;

	public EntryEvent(EntityMeteor e) {
		super(e);
		x = e.posX;
		y = e.posY;
		z = e.posZ;
		vx = e.motionX;
		vy = e.motionY;
		vz = e.motionZ;
		world = e.worldObj;
		type = e.getType();
	}
}
