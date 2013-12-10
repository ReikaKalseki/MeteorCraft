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
import net.minecraftforge.event.Event;

public class MeteorShowerEndEvent extends Event {

	public final World world;

	public final long duration;

	public MeteorShowerEndEvent(World world, long dura) {
		this.world = world;
		duration = dura;
	}

}
