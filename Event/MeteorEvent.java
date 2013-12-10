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

import net.minecraftforge.event.Event;
import Reika.MeteorCraft.Entity.EntityMeteor;

public class MeteorEvent extends Event {

	public final EntityMeteor meteor;

	public MeteorEvent(EntityMeteor e) {
		meteor = e;
	}

}
