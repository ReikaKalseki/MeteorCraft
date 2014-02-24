/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Event.EntryEvent;
import Reika.MeteorCraft.Event.ImpactEvent;
import Reika.MeteorCraft.Event.MeteorDefenceEvent;

public class TileEntityMeteorGun extends TileEntityMeteorBase {

	private int soundTimer = 0;

	public int getProtectionRange() {
		return 16*(this.getTier()*2+3);
	}

	public int getTier() {
		return this.getBlockMetadata();
	}

	@Override
	public long getMinPower() {
		return (1+this.getTier())*524288;
	}

	@Override
	@ForgeSubscribe
	public void onMeteor(EntryEvent e) {
		EntityMeteor m = e.meteor;
		double dd = ReikaMathLibrary.py3d(e.x-xCoord, 0, e.z-zCoord);
		if (this.canPerformActions() && dd <= this.getProtectionRange()) {
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
				//MeteorSounds.BOOM.playSoundAtEntity(worldObj, ep, 2, i);
			}
			soundTimer = 10;
		}
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (soundTimer == 1) {
			if (worldObj.isRemote) {
				EntityPlayer ep = Minecraft.getMinecraft().thePlayer;

				ep.playSound("meteorcraft:boom", 2, 2);
				//MeteorSounds.BOOM.playSoundAtEntity(worldObj, ep, 2, 2);
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
	protected String getTEName() {
		return "Meteor Defence Gun";
	}

	@Override
	@ForgeSubscribe
	public void onImpact(ImpactEvent e) {

	}

}
