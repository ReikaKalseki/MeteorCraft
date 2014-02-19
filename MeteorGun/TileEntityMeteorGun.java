/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.MeteorGun;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Base.TileEntityBase;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.MeteorCraft.MeteorCraft;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Event.EntryEvent;
import Reika.MeteorCraft.Event.MeteorDefenceEvent;
import Reika.RotaryCraft.API.ShaftPowerReceiver;

public class TileEntityMeteorGun extends TileEntityBase implements ShaftPowerReceiver {

	private int torque;
	private int omega;
	private long power;

	private int iotick = 512;

	private int soundTimer = 0;

	public TileEntityMeteorGun() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	public int getProtectionRange() {
		return 16*(this.getTier()*2+3);
	}

	public int getTier() {
		return this.getBlockMetadata();
	}

	public int getMinPower() {
		return (1+this.getTier())*524288;
	}

	public boolean canStopMeteors() {
		return ModList.ROTARYCRAFT.isLoaded() ? power >= this.getMinPower() : true;
	}

	@ForgeSubscribe
	public void killMeteor(EntryEvent e) {
		EntityMeteor m = e.meteor;
		double dd = ReikaMathLibrary.py3d(e.x-xCoord, 0, e.z-zCoord);
		if (this.canStopMeteors() && dd <= this.getProtectionRange()) {
			this.killMeteor(m);
		}
	}

	private void killMeteor(EntityMeteor m) {
		MinecraftForge.EVENT_BUS.post(new MeteorDefenceEvent(this, m));
		m.setPosition(xCoord+0.5, m.posY, zCoord+0.5);
		m.destroy();
		if (worldObj.isRemote) {
			EntityTNTPrimed tnt = new EntityTNTPrimed(worldObj, xCoord+0.5, yCoord+0.5, zCoord+0.5, null);
			tnt.motionY = 5;
			tnt.fuse = 40;
			worldObj.spawnEntityInWorld(tnt);
		}
		ReikaSoundHelper.playSoundAtBlock(worldObj, xCoord, yCoord, zCoord, "random.explode");
		ReikaParticleHelper.EXPLODE.spawnAroundBlock(worldObj, xCoord, yCoord, zCoord, 1);

		if (worldObj.isRemote) {
			EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
			//ep.playSound("ambient.weather.thunder", 2, 2);
			for (float i = 0; i <= 2; i += 0.5F) {
				ep.playSound("meteorcraft:boom", 2, i);
			}
			soundTimer = 10;
		}
	}

	@Override
	public int getOmega() {
		return omega;
	}

	@Override
	public int getTorque() {
		return torque;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public String getName() {
		return this.getTEName();
	}

	@Override
	public int getIORenderAlpha() {
		return iotick;
	}

	@Override
	public void setIORenderAlpha(int io) {
		iotick = io;
	}

	@Override
	public int getMachineX() {
		return xCoord;
	}

	@Override
	public int getMachineY() {
		return yCoord;
	}

	@Override
	public int getMachineZ() {
		return zCoord;
	}

	@Override
	public void setOmega(int omega) {
		this.omega = omega;
	}

	@Override
	public void setTorque(int torque) {
		this.torque = torque;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public boolean canReadFromBlock(int x, int y, int z) {
		return y <= yCoord && (Math.abs(x-xCoord)+Math.abs(y-yCoord)+Math.abs(z-zCoord)) == 1;
	}

	@Override
	public boolean isReceiving() {
		return true;
	}

	@Override
	public void noInputMachine() {
		torque = omega = 0;
		power = 0;
	}

	@Override
	public int getTileEntityBlockID() {
		return MeteorCraft.meteorGun.blockID;
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (soundTimer == 1) {
			if (worldObj.isRemote) {
				EntityPlayer ep = Minecraft.getMinecraft().thePlayer;

				ep.playSound("meteorcraft:boom", 2, 2);
				for (int k = 0; k < 2; k++) {
					ep.playSound("ambient.weather.thunder", 2, 0.25F);
					ep.playSound("ambient.weather.thunder", 2, 1);
					ep.playSound("ambient.weather.thunder", 2, 2);

					ep.playSound("ambient.weather.thunder", 2, 0.25F);
					ep.playSound("ambient.weather.thunder", 2, 1);
					ep.playSound("ambient.weather.thunder", 2, 2);
				}
			}
		}
		if (soundTimer > 0)
			soundTimer--;
	}

	@Override
	public void animateWithTick(World world, int x, int y, int z) {
		if (iotick > 0)
			iotick -= 8;
	}

	@Override
	protected String getTEName() {
		return "Meteor Defence Gun";
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 0;
	}

}
