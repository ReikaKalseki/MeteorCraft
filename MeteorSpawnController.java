/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickHandler;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry.TickType;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.ReikaTwilightHelper;
import Reika.DragonAPI.ModRegistry.InterfaceCache;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.API.Event.MeteorShowerEndEvent;
import Reika.MeteorCraft.API.Event.MeteorShowerStartEvent;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;

public class MeteorSpawnController implements TickHandler {

	public static final MeteorSpawnController instance = new MeteorSpawnController();

	private static final Random rand = new Random();

	private boolean isShowering;
	private int showerDuration;

	private static final HashSet<Integer> blacklist = new HashSet();
	private static final HashMap<Integer, Integer> chanceOverrides = new HashMap();

	private MeteorSpawnController() {

	}

	public static void blacklistDimension(int dim) {
		if (dim != 0)
			blacklist.add(dim);
	}

	public static void setDimensionChance(int dim, int chance) {
		if (dim != 0)
			chanceOverrides.put(dim, chance);
	}

	@Override
	public void tick(TickType type, Object... tickData) {
		World world = (World)tickData[0];
		if (world != null && this.canSpawnIn(world)) {
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
				double chance = this.getChanceFromDimension(world.provider.dimensionId);
				if (InterfaceCache.IGALACTICWORLD.instanceOf(world.provider)) {
					IGalacticraftWorldProvider ig = (IGalacticraftWorldProvider)world.provider;
					chance /= ig.getMeteorFrequency();
				}

				if (MeteorOptions.SHOWER.getState() && ReikaRandomHelper.doWithChance(0.01D/chance)) {
					this.startMeteorShower(world);
				}
				else if (ReikaRandomHelper.doWithChance(1D/chance)) {
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

	private boolean canSpawnIn(World world) {
		if (world.provider.isHellWorld)
			return false;
		if (world.provider.dimensionId == 1)
			return true;
		if (world.provider.hasNoSky)
			return false;
		if (blacklist.contains(world.provider.dimensionId))
			return false;
		return this.getChanceFromDimension(world.provider.dimensionId) > 0;
	}

	private int getChanceFromDimension(int dimID) {
		if (dimID == ReikaTwilightHelper.getDimensionID())
			return MeteorOptions.FORESTCHANCE.getValue();
		Integer get = chanceOverrides.get(dimID);
		if (get != null)
			return get.intValue();
		switch(dimID) {
			case 0:
				return MeteorOptions.CHANCE.getValue();
			case 1:
				return MeteorOptions.ENDCHANCE.getValue();
			default:
				return MeteorOptions.OTHERCHANCE.getValue();
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
		EntityMeteor e = new EntityMeteor(world, dx, 1024, dz, MeteorType.getWeightedType());
		//if (ReikaRandomHelper.doWithChance(10))
		//e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.END);
		//else if (ReikaRandomHelper.doWithChance(20))
		//e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.NETHERRACK);
		//else
		//	e = new EntityMeteor(world, dx, world.provider.getHeight(), dz, MeteorType.STONE);
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
		ChatComponentTranslation chat = new ChatComponentTranslation("A meteor shower is starting...");
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
	public EnumSet<TickType> getType() {
		return EnumSet.of(TickType.WORLD);
	}

	@Override
	public boolean canFire(Phase p) {
		return p == Phase.START;
	}

	@Override
	public String getLabel() {
		return "MeteorCraft";
	}
}
