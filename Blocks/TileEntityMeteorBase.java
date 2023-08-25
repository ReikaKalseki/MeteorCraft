/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Interfaces.TileEntity.LocationCached;
import Reika.MeteorCraft.MeteorCraft;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.AirburstEvent;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.EntryEvent;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.ImpactEvent;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.MeteorDefenceEvent;
import Reika.RotaryCraft.API.Power.ShaftPowerReceiver;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public abstract class TileEntityMeteorBase extends TileEntityBase implements ShaftPowerReceiver, LocationCached {

	private int torque;
	private int omega;
	private long power;

	protected int iotick = 512;

	private static final Collection<WorldLocation> cache = new ArrayList();
	private static final EventWatcher instance = new EventWatcher();

	public static final class EventWatcher {

		private EventWatcher() {
			MinecraftForge.EVENT_BUS.register(this);
		}

		// What happened to catching all child events??
		@SubscribeEvent
		public final void entryEvent(EntryEvent e) {
			for (WorldLocation loc : cache) {
				if (loc.dimensionID == e.world.provider.dimensionId) {
					TileEntity te = loc.getTileEntity(e.world);
					if (te == null) {
						MeteorCraft.logger.logError("Null meteor tile @ "+loc);
						continue;
					}
					((TileEntityMeteorBase)te).onEvent(e);
				}
			}
		}

		@SubscribeEvent
		public final void entryEvent(AirburstEvent e) {
			for (WorldLocation loc : cache) {
				if (loc.dimensionID == e.world.provider.dimensionId) {
					TileEntity te = loc.getTileEntity(e.world);
					if (te == null) {
						MeteorCraft.logger.logError("Null meteor tile @ "+loc);
						continue;
					}
					((TileEntityMeteorBase)te).onEvent(e);
				}
			}
		}

		@SubscribeEvent
		public final void entryEvent(ImpactEvent e) {
			for (WorldLocation loc : cache) {
				if (loc.dimensionID == e.world.provider.dimensionId) {
					TileEntity te = loc.getTileEntity(e.world);
					if (te == null) {
						MeteorCraft.logger.logError("Null meteor tile @ "+loc);
						continue;
					}
					((TileEntityMeteorBase)te).onEvent(e);
				}
			}
		}

		@SubscribeEvent
		public final void entryEvent(MeteorDefenceEvent e) {
			for (WorldLocation loc : cache) {
				if (loc.dimensionID == e.world.provider.dimensionId) {
					TileEntity te = loc.getTileEntity(e.world);
					if (te == null) {
						MeteorCraft.logger.logError("Null meteor tile @ "+loc);
						continue;
					}
					((TileEntityMeteorBase)te).onEvent(e);
				}
			}
		}

	}

	@Override
	protected final void onFirstTick(World world, int x, int y, int z) {
		WorldLocation loc = new WorldLocation(this);
		if (!cache.contains(loc))
			cache.add(loc);
	}

	public final void breakBlock() {
		cache.remove(new WorldLocation(this));
	}

	protected abstract void onEvent(MeteorCraftEvent e);

	@Override
	public final Block getTileEntityBlockID() {
		return MeteorCraft.meteorMachines;
	}

	public boolean canSeeSky() {
		return worldObj.canBlockSeeTheSky(xCoord, yCoord+1, zCoord);
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {
		if (iotick > 0)
			iotick -= 8;
	}

	@Override
	public final int getOmega() {
		return omega;
	}

	@Override
	public final int getTorque() {
		return torque;
	}

	@Override
	public final long getPower() {
		return power;
	}

	@Override
	public final int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public final void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	public final void setOmega(int omega) {
		this.omega = omega;
	}

	@Override
	public final void setTorque(int torque) {
		this.torque = torque;
	}

	@Override
	public final void setPower(long power) {
		this.power = power;
	}

	@Override
	public final boolean canReadFrom(ForgeDirection dir) {
		return dir != ForgeDirection.UP;
	}

	@Override
	public final boolean isReceiving() {
		return true;
	}

	@Override
	public final void noInputMachine() {
		torque = omega = 0;
		power = 0;
	}

	public boolean canPerformActions() {
		if (!this.canSeeSky())
			return false;
		return ModList.ROTARYCRAFT.isLoaded() ? power >= this.getMinPower() : true;
	}

	public abstract long getMinPower();

	@Override
	public final boolean shouldRenderInPass(int pass) {
		return pass == 0;
	}

	@Override
	public final int getRedstoneOverride() {
		return this.canPerformActions() ? 0 : 15;
	}

	@Override
	public final int getMinTorque(int available) {
		return 1;
	}

}
