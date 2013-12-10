/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Event;

import net.minecraft.world.World;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Entity.EntityMeteor;

public class ImpactEvent extends MeteorEvent {

	public final int x;
	public final int y;
	public final int z;
	public final World world;
	public final MeteorType type;
	public final float radius;

	public ImpactEvent(EntityMeteor e, int x, int y, int z, float radius) {
		super(e);
		this.x = x;
		this.y = y;
		this.z = z;
		world = e.worldObj;
		type = e.getType();
		this.radius = radius;
	}

}
