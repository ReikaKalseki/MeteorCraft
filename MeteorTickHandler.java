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
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class MeteorTickHandler implements ITickHandler {

	public static final MeteorTickHandler instance = new MeteorTickHandler();

	private static final Random rand = new Random();

	private MeteorTickHandler() {

	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		World world = (World)tickData[0];
		if (world != null && ReikaRandomHelper.doWithChance(1D/MeteorOptions.CHANCE.getValue())) {
			if (world.playerEntities.size() > 0) {
				EntityPlayer ep = (EntityPlayer)world.playerEntities.get(rand.nextInt(world.playerEntities.size()));
				if (ep != null) {
					int x = MathHelper.floor_double(ep.posX);
					//int y = MathHelper.floor_double(ep.posY);
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
						e = new EntityMeteor(world, dx, world.provider.getHeight(), dz);
					if (!world.isRemote)
						world.spawnEntityInWorld(e);
				}
			}
		}
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
