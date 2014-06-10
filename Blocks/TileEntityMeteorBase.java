/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.MeteorCraft.MeteorCraft;
import Reika.MeteorCraft.Event.EntryEvent;
import Reika.MeteorCraft.Event.ImpactEvent;
import Reika.RotaryCraft.API.ShaftPowerReceiver;

public abstract class TileEntityMeteorBase extends TileEntityBase implements ShaftPowerReceiver {

	private int torque;
	private int omega;
	private long power;

	protected int iotick = 512;

	public TileEntityMeteorBase() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void destroy() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	public abstract void onMeteor(EntryEvent e);

	public abstract void onImpact(ImpactEvent e);

	@Override
	public final int getTileEntityBlockID() {
		return MeteorCraft.meteorMachines.blockID;
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
	public final String getName() {
		return this.getTEName();
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
	public final boolean canReadFromBlock(int x, int y, int z) {
		return y <= yCoord && (Math.abs(x-xCoord)+Math.abs(y-yCoord)+Math.abs(z-zCoord)) == 1;
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

	@Override
	public final boolean hasModel() {
		return false;
	}

}
