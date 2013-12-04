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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.Data.BlockArray;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.MeteorCraft.Registry.MeteorOptions;

public class MeteorGenerator {

	public static final MeteorGenerator instance = new MeteorGenerator();

	private ArrayList<ItemStack> viableOres = new ArrayList();

	private static final Random rand = new Random();

	private MeteorGenerator() {
		for (int i = 0; i < ReikaOreHelper.oreList.length; i++) {
			ReikaOreHelper ore = ReikaOreHelper.oreList[i];
			viableOres.add(ore.getOreBlock());
		}
		for (int i = 0; i < ModOreList.oreList.length; i++) {
			ModOreList ore = ModOreList.oreList[i];
			if (this.canGenerateOre(ore)) {
				viableOres.addAll(ore.getAllOreBlocks());
			}
		}
	}

	private ItemStack getRandomOre() {
		return viableOres.get(rand.nextInt(viableOres.size()));
	}

	public boolean canGenerateOre(ModOreList ore) {
		return MeteorCraft.config.shouldGenerateOre(ore) && ore.existsInGame();
	}

	public ItemStack getBlock(EntityMeteor e) {
		if (ReikaRandomHelper.doWithChance(MeteorOptions.ORE.getValue())) {
			return this.getRandomOre();
		}
		else {
			return e.getType().getBlock();
		}
	}

	public void generate(World world, int x, int y, int z, EntityMeteor e) {
		BlockArray blocks = new BlockArray();
		int radius = 2+rand.nextInt(3);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				for (int k = -radius; k <= radius; k++) {
					double dd = ReikaMathLibrary.py3d(i, j, k);
					int dx = x+i;
					int dy = y+j;
					int dz = z+k;
					//dy = world.getTopSolidOrLiquidBlock(dx, dz);
					if (dd <= radius || (j < 0 && ReikaMathLibrary.py3d(i, 0, k) <= radius)) {
						//ItemStack is = this.getBlock(e);
						//world.setBlock(dx, dy, dz, is.itemID, is.getItemDamage(), 3);
						blocks.addBlockCoordinate(dx, dy, dz);
					}
				}
			}
		}
		blocks.sink(world, Material.circuits, Material.glass, Material.snow, Material.ice, Material.cactus, Material.craftedSnow, Material.fire, Material.leaves, Material.plants, Material.portal, Material.pumpkin, Material.redstoneLight, Material.sponge, Material.lava, Material.water, Material.vine, Material.web);
		for (int i = 0; i < blocks.getSize(); i++) {
			int[] xyz = blocks.getNthBlock(i);
			int fx = xyz[0];
			int fy = xyz[1];
			int fz = xyz[2];
			ItemStack is = this.getBlock(e);
			world.setBlock(fx, fy, fz, is.itemID, is.getItemDamage(), 3);
		}
	}

	public static boolean canStopMeteor(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y, z);
		if (id == 0)
			return false;
		if (ReikaWorldHelper.softBlocks(world, x, y, z))
			return false;
		Material mat = world.getBlockMaterial(x, y, z);
		List<Material> mats = ReikaJavaLibrary.makeListFrom(Material.circuits, Material.glass, Material.snow, Material.ice, Material.cactus, Material.craftedSnow, Material.fire, Material.leaves, Material.plants, Material.portal, Material.pumpkin, Material.redstoneLight, Material.sponge, Material.lava, Material.water, Material.vine, Material.web);
		if (mats.contains(mat))
			return false;
		return true;
	}

	public static enum MeteorType {
		STONE(Block.stone),
		NETHERRACK(Block.netherrack),
		END(Block.whiteStone);

		private final Block block;

		private MeteorType(Block b) {
			block = b;
		}

		public ItemStack getBlock() {
			return new ItemStack(block);
		}
	}

}
