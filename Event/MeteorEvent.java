/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Event;

import java.util.ArrayList;

import Reika.MeteorCraft.Entity.EntityMeteor;
import cpw.mods.fml.common.eventhandler.Event;

public class MeteorEvent extends Event {

	public final EntityMeteor meteor;

	private static final ArrayList<EntityMeteor> meteors = new ArrayList();

	public MeteorEvent(EntityMeteor e) {
		meteor = e;
	}

}
