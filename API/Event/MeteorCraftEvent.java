/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.API.Event;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.World;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Blocks.TileEntityMeteorGun;
import Reika.MeteorCraft.Entity.EntityMeteor;
import cpw.mods.fml.common.eventhandler.Event;

public abstract class MeteorCraftEvent extends Event {

	public final EntityMeteor meteor;

	public final double x;
	public final double y;
	public final double z;
	public final World world;
	public final MeteorType type;

	private MeteorCraftEvent(EntityMeteor e) {
		meteor = e;
		x = e.posX;
		y = e.posY;
		z = e.posZ;
		world = e.worldObj;
		type = e.getType();
	}

	public static class AirburstEvent extends MeteorCraftEvent {

		private final Collection<EntityFallingBlock> blocks;
		private final Collection<EntityItem> items;

		public AirburstEvent(EntityMeteor e, Collection<EntityFallingBlock> cb, Collection<EntityItem> ci) {
			super(e);
			blocks = cb;
			items = ci;
		}

		public Collection<EntityFallingBlock> getBlocks() {
			return Collections.unmodifiableCollection(blocks);
		}

		public Collection<EntityItem> getItems() {
			return Collections.unmodifiableCollection(items);
		}
	}

	public static class EntryEvent extends MeteorCraftEvent {

		public final double vx;
		public final double vy;
		public final double vz;

		public EntryEvent(EntityMeteor e) {
			super(e);
			vx = e.motionX;
			vy = e.motionY;
			vz = e.motionZ;
		}
	}

	public static class ImpactEvent extends MeteorCraftEvent {

		public final int impactX;
		public final int impactY;
		public final int impactZ;
		public final float radius;

		public ImpactEvent(EntityMeteor e, int x, int y, int z, float radius) {
			super(e);
			impactX = x;
			impactY = y;
			impactZ = z;
			this.radius = radius;
		}

	}

	public static class MeteorDefenceEvent extends MeteorCraftEvent {

		public final TileEntityMeteorGun gun;

		public MeteorDefenceEvent(TileEntityMeteorGun te, EntityMeteor e) {
			super(e);
			gun = te;
		}

	}

}
