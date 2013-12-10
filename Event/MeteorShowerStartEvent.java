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

public class MeteorShowerStartEvent extends Event {

	public final World world;

	/** World time at start */
	public final long startTime;

	public MeteorShowerStartEvent(World world) {
		this.world = world;
		startTime = world.getWorldTime();
	}

}
