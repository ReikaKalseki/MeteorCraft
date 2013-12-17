/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.util.EnumSet;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Event.MeteorShowerEndEvent;
import Reika.MeteorCraft.Event.MeteorShowerStartEvent;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class MeteorSpawnController implements ITickHandler {

	public static final MeteorSpawnController instance = new MeteorSpawnController();

	private static final Random rand = new Random();

	private boolean isShowering;
	private int showerDuration;

	private MeteorSpawnController() {

	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		World world = (World)tickData[0];
		if (world != null && !world.provider.isHellWorld) {
			if (isShowering) {
				showerDuration++;
				if (ReikaRandomHelper.doWithChance(this.getSpawnChanceDuringShower())) {
					if (world.playerEntities.size() > 0) {
						EntityPlayer ep = (EntityPlayer)world.playerEntities.get(rand.nextInt(world.playerEntities.size()));
						if (ep != null) {
							EntityMeteor e = this.createMeteor(world, ep).setExploding();
							if (!world.isRemote)
								world.spawnEntityInWorld(e);
						}
					}
				}
				if (this.shouldEndShower())
					this.endMeteorShower(world);
			}
			else {
				if (MeteorOptions.SHOWER.getState() && ReikaRandomHelper.doWithChance(0.01D/MeteorOptions.CHANCE.getValue())) {
					this.startMeteorShower(world);
				}
				else if (ReikaRandomHelper.doWithChance(1D/MeteorOptions.CHANCE.getValue())) {
					if (world.playerEntities.size() > 0) {
						EntityPlayer ep = (EntityPlayer)world.playerEntities.get(rand.nextInt(world.playerEntities.size()));
						if (ep != null) {
							EntityMeteor e = this.createMeteor(world, ep);
							if (MeteorOptions.EXPLODE.getState())
								e.setExploding();
							if (!world.isRemote)
								world.spawnEntityInWorld(e);
						}
					}
				}
			}
		}
	}

	private double getSpawnChanceDuringShower() {
		return 0.02;
	}

	public EntityMeteor createMeteor(World world, EntityPlayer ep) {
		int x = MathHelper.floor_double(ep.posX);
		int z = MathHelper.floor_double(ep.posZ);
		int r = 64;
		int dx = ReikaRandomHelper.getRandomPlusMinus(x, r);
		int dz = ReikaRandomHelper.getRandomPlusMinus(z, r);
		EntityMeteor e;
		if (ReikaRandomHelper.doWithChance(10))
			e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.END);
		else if (ReikaRandomHelper.doWithChance(20))
			e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.NETHERRACK);
		else
			e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.STONE);
		return e;
	}

	private boolean shouldEndShower() {
		if (showerDuration < 600)
			return false;
		if (showerDuration >= 6000)
			return true;
		return rand.nextInt(6000-showerDuration) == 0;
	}

	private void startMeteorShower(World world) {
		ChatMessageComponent chat = new ChatMessageComponent();
		chat.addText("A meteor shower is starting...");
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(chat);
		isShowering = true;
		showerDuration = 0;
		MinecraftForge.EVENT_BUS.post(new MeteorShowerStartEvent(world));
	}

	private void endMeteorShower(World world) {
		isShowering = false;
		MinecraftForge.EVENT_BUS.post(new MeteorShowerEndEvent(world, showerDuration));
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public String getLabel() {
		return "MeteorCraft";
	}
}
