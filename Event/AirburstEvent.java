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

public class AirburstEvent extends MeteorEvent {

	public final double x;
	public final double y;
	public final double z;
	public final World world;
	public final MeteorType type;

	public AirburstEvent(EntityMeteor e) {
		super(e);
		x = e.posX;
		y = e.posY;
		z = e.posZ;
		world = e.worldObj;
		type = e.getType();
	}
}
