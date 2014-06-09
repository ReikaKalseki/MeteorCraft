/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.Data.BlockArray;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.InvertedWeightedRandom;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.MagicCropHandler;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.MeteorCraft.Registry.MeteorOptions;

public class MeteorGenerator {

	public static final MeteorGenerator instance = new MeteorGenerator();

	private final HashMap<MeteorType, InvertedWeightedRandom<ItemStack>> viableOres = new HashMap();

	private final HashMap<ModOreList, HashMap<MeteorType, ArrayList<Integer>>> metas = new HashMap();
	private final HashMap<ModOreList, HashMap<MeteorType, ArrayList<Integer>>> ids = new HashMap();

	private static final Random rand = new Random();

	private final Material[] mats;

	private MeteorGenerator() {

		this.addMetadata(ModOreList.PITCHBLENDE, MeteorType.STONE, 1);
		this.addMetadata(ModOreList.PITCHBLENDE, MeteorType.END, 5);
		this.addMetadata(ModOreList.FORCE, MeteorType.STONE, 0);
		this.addMetadata(ModOreList.FORCE, MeteorType.NETHERRACK, 1);

		this.addID(ModOreList.ESSENCE, MeteorType.STONE, MagicCropHandler.getInstance().oreID);
		this.addID(ModOreList.ESSENCE, MeteorType.NETHERRACK, MagicCropHandler.getInstance().netherOreID);

		this.addMetadata(ModOreList.MIMICHITE, MeteorType.STONE, 0);
		this.addMetadata(ModOreList.MIMICHITE, MeteorType.NETHERRACK, 1);
		this.addMetadata(ModOreList.MIMICHITE, MeteorType.END, 2);

		for (int k = 0; k < MeteorType.list.length; k++) {
			MeteorType type = MeteorType.list[k];
			int id = type.blockID;
			int meta = type.blockMeta;
			for (int i = 0; i < ReikaOreHelper.oreList.length; i++) {
				ReikaOreHelper ore = ReikaOreHelper.oreList[i];
				if (this.canGenerateOre(ore)) {
					if (this.isValidOreForType(type, ore)) {
						this.addOre(type, ore.getOreBlock(), MeteorCraft.config.getOreWeight(ore));
					}
				}
			}
			for (int i = 0; i < ModOreList.oreList.length; i++) {
				ModOreList ore = ModOreList.oreList[i];
				if (this.canGenerateOre(ore) && this.canGenOreIn(type.blockID, ore)) {
					ArrayList<ItemStack> li = ore.getAllOreBlocks();
					for (int j = 0; j < li.size(); j++) {
						ItemStack block = li.get(j);
						if (MeteorCraft.config.isItemStackGenerationPermitted(block)) {
							//ReikaJavaLibrary.pConsole(type.name()+" INIT:"+ore.name());
							if (this.isValidOreIDForType(type, ore, block.itemID)) {
								//ReikaJavaLibrary.pConsole(type.name()+" ID:"+ore.name());
								if (this.isValidOreMetaForType(type, ore, block.getItemDamage())) {
									//ReikaJavaLibrary.pConsole(type.name()+" META:"+ore.name());
									MeteorCraft.logger.log("Registering "+block+" ("+ore.displayName+" ore) to meteor type "+type.name());
									this.addOre(type, block, MeteorCraft.config.getOreWeight(ore));
								}
							}
						}
					}
				}
			}
		}

		ArrayList<Material> mats = new ArrayList();
		mats.add(Material.circuits);
		mats.add(Material.glass);
		mats.add(Material.snow);
		mats.add(Material.ice);
		mats.add(Material.cactus);
		mats.add(Material.craftedSnow);
		mats.add(Material.fire);
		mats.add(Material.leaves);
		mats.add(Material.plants);
		mats.add(Material.portal);
		mats.add(Material.pumpkin);
		mats.add(Material.redstoneLight);
		mats.add(Material.sponge);
		mats.add(Material.lava);
		mats.add(Material.water);
		mats.add(Material.vine);
		mats.add(Material.web);
		this.mats = new Material[mats.size()];
		for (int i = 0; i < mats.size(); i++) {
			this.mats[i] = mats.get(i);
		}
	}

	private void addMetadata(ModOreList ore, MeteorType type, int dmg) {
		HashMap<MeteorType, ArrayList<Integer>> map = metas.get(ore);
		if (map == null) {
			map = new HashMap();
			metas.put(ore, map);
		}
		ArrayList<Integer> li = map.get(type);
		if (li == null) {
			li = new ArrayList();
			map.put(type, li);
		}
		if (!li.contains(dmg))
			li.add(dmg);
	}

	private void addID(ModOreList ore, MeteorType type, int id) {
		HashMap<MeteorType, ArrayList<Integer>> map = ids.get(ore);
		if (map == null) {
			map = new HashMap();
			ids.put(ore, map);
		}
		ArrayList<Integer> li = map.get(type);
		if (li == null) {
			li = new ArrayList();
			map.put(type, li);
		}
		if (!li.contains(id))
			li.add(id);
	}

	public boolean hasListedMetadatasForOre(ModOreList ore) {
		return metas.get(ore) != null;
	}

	public boolean hasListedIDsForOre(ModOreList ore) {
		return ids.get(ore) != null;
	}

	public boolean isValidOreForType(MeteorType type, ReikaOreHelper ore) {
		return type.blockID == ore.getOreGenBlock().blockID;
	}

	public boolean isValidOreMetaForType(MeteorType type, ModOreList ore, int meta) {
		HashMap<MeteorType, ArrayList<Integer>> map = metas.get(ore);
		if (map != null) {
			ArrayList<Integer> li = map.get(type);
			if (li != null) {
				return li.contains(meta);
			}
			else
				return true;
		}
		else
			return true;
	}

	public boolean isValidOreIDForType(MeteorType type, ModOreList ore, int id) {
		HashMap<MeteorType, ArrayList<Integer>> map = ids.get(ore);
		if (map != null) {
			ArrayList<Integer> li = map.get(type);
			if (li != null) {
				return li.contains(id);
			}
			else
				return true;
		}
		else
			return true;
	}

	private void addOre(MeteorType type, ItemStack is, int weight) {
		InvertedWeightedRandom<ItemStack> dat = this.getOres(type);
		if (dat == null) {
			dat = new InvertedWeightedRandom();
		}
		dat.addEntry(weight, is);
		viableOres.put(type, dat);
	}

	private InvertedWeightedRandom<ItemStack> getOres(MeteorType type) {
		return viableOres.get(type);
	}

	public boolean hasOresForType(MeteorType type) {
		return this.getOres(type) != null && !this.getOres(type).isEmpty();
	}

	private ItemStack getRandomOre(MeteorType type) {
		return this.getOres(type).getRandomEntry();
	}

	public boolean canGenerateOre(ModOreList ore) {
		return MeteorCraft.config.shouldGenerateOre(ore) && ore.existsInGame();
	}

	public boolean canGenerateOre(ReikaOreHelper ore) {
		return MeteorCraft.config.shouldGenerateOre(ore);
	}

	public ItemStack getBlock(MeteorType type) {
		if (ReikaRandomHelper.doWithChance(MeteorOptions.ORE.getValue()) && this.hasOresForType(type)) {
			return this.getRandomOre(type);
		}
		else {
			return type.getBlock();
		}
	}

	public void generate(World world, int x, int y, int z, MeteorType type) {
		BlockArray blocks = this.getMeteorBlockArray(world, x, y, z);
		for (int i = 0; i < blocks.getSize(); i++) {
			int[] xyz = blocks.getNthBlock(i);
			int fx = xyz[0];
			int fy = xyz[1];
			int fz = xyz[2];
			ItemStack is = this.getBlock(type);
			if (fy > 0)
				world.setBlock(fx, fy, fz, is.itemID, is.getItemDamage(), 3);
		}
	}

	public BlockArray getMeteorBlockArray(World world, int x, int y, int z) {
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
		blocks.sink(world, mats);
		return blocks;
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

	public static boolean canGenOreIn(Block b, ReikaOreHelper ore) {
		return b == ore.getOreGenBlock();
	}

	//Very basic rules:
	public static boolean canGenOreIn(int blockID, ModOreList ore) {
		if (ore.isNetherOres())
			return blockID == Block.netherrack.blockID;
		if (ore == ModOreList.ARDITE || ore == ModOreList.COBALT)
			return blockID == Block.netherrack.blockID;
		if (ore == ModOreList.FIRESTONE)
			return blockID == Block.netherrack.blockID;
		if (ore == ModOreList.MAGMANITE)
			return blockID == Block.netherrack.blockID;
		if (ore == ModOreList.AMMONIUM)
			return blockID == Block.netherrack.blockID;
		if (ore == ModOreList.SODALITE)
			return blockID == Block.whiteStone.blockID;
		if (ore == ModOreList.ESSENCE)
			return blockID == Block.netherrack.blockID || blockID == Block.stone.blockID;
		if (blockID == Block.whiteStone.blockID)
			return ore == ModOreList.PITCHBLENDE;
		if (ore == ModOreList.MIMICHITE)
			return true;
		return blockID == Block.stone.blockID;
	}

	public static enum MeteorType {
		STONE(Block.stone),
		NETHERRACK(Block.netherrack),
		END(Block.whiteStone);

		public final int blockID;
		public final int blockMeta;

		public static final MeteorType[] list = values();

		private MeteorType(Block b) {
			blockID = b.blockID;
			blockMeta = 0;
		}

		private MeteorType(ItemStack is) {
			blockID = is.itemID;
			blockMeta = is.getItemDamage();
		}

		public ItemStack getBlock() {
			return new ItemStack(blockID, 1, blockMeta);
		}
	}

}
