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

import Reika.MeteorCraft.Blocks.TileEntityMeteorGun;
import Reika.MeteorCraft.Entity.EntityMeteor;

public class MeteorDefenceEvent extends MeteorEvent {

	public final TileEntityMeteorGun gun;

	public MeteorDefenceEvent(TileEntityMeteorGun te, EntityMeteor e) {
		super(e);
		gun = te;
	}

}