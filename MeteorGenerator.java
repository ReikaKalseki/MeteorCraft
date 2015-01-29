/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom;
import Reika.DragonAPI.Instantiable.Data.WeightedRandom.InvertedWeightedRandom;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BlockArray;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaOreHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.AppEngHandler;
import Reika.DragonAPI.ModInteract.MagicCropHandler;
import Reika.DragonAPI.ModRegistry.ModOreList;
import Reika.MeteorCraft.CustomOreLoader.CustomOreEntry;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Registry.MeteorOptions;

public class MeteorGenerator {

	public static final MeteorGenerator instance = new MeteorGenerator();

	private final HashMap<MeteorType, InvertedWeightedRandom<ItemStack>> viableOres = new HashMap();

	private final HashMap<ModOreList, HashMap<MeteorType, ArrayList<Integer>>> metas = new HashMap();
	private final HashMap<ModOreList, HashMap<MeteorType, ArrayList<Block>>> ids = new HashMap();

	private static final Random rand = new Random();

	private final Material[] mats;

	private MeteorGenerator() {

		this.addMetadata(ModOreList.PITCHBLENDE, MeteorType.STONE, 1);
		this.addMetadata(ModOreList.PITCHBLENDE, MeteorType.END, 5);
		this.addMetadata(ModOreList.FORCE, MeteorType.STONE, 0);
		this.addMetadata(ModOreList.FORCE, MeteorType.NETHERRACK, 1);

		this.addID(ModOreList.ESSENCE, MeteorType.STONE, MagicCropHandler.getInstance().oreID);
		this.addID(ModOreList.ESSENCE, MeteorType.NETHERRACK, MagicCropHandler.getInstance().netherOreID);
		this.addID(ModOreList.ESSENCE, MeteorType.END, MagicCropHandler.getInstance().endOreID);

		this.addMetadata(ModOreList.MIMICHITE, MeteorType.STONE, 0);
		this.addMetadata(ModOreList.MIMICHITE, MeteorType.NETHERRACK, 1);
		this.addMetadata(ModOreList.MIMICHITE, MeteorType.END, 2);

		for (int k = 0; k < MeteorType.list.length; k++) {
			MeteorType type = MeteorType.list[k];
			Block id = type.blockID;
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
					Collection<ItemStack> li = ore.getAllOreBlocks();
					for (ItemStack block : li) {
						if (MeteorCraft.config.isItemStackGenerationPermitted(block)) {
							//ReikaJavaLibrary.pConsole(type.name()+" INIT:"+ore.name());
							if (this.isValidOreIDForType(type, ore, Block.getBlockFromItem(block.getItem()))) {
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
		mats.add(Material.gourd);
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

		List<CustomOreEntry> li = CustomOreLoader.instance.getEntries();
		for (int i = 0; i < li.size(); i++) {
			CustomOreEntry e = li.get(i);
			int weight = e.spawnWeight;
			MeteorType type = MeteorType.list[e.meteorType];
			List<ItemStack> items = e.getItems();
			for (int k = 0; k < items.size(); k++) {
				ItemStack is = items.get(k);
				this.addOre(type, is, weight);
			}
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

	private void addID(ModOreList ore, MeteorType type, Block id) {
		HashMap<MeteorType, ArrayList<Block>> map = ids.get(ore);
		if (map == null) {
			map = new HashMap();
			ids.put(ore, map);
		}
		ArrayList<Block> li = map.get(type);
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
		return type.blockID == ore.getOreGenBlock();
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

	public boolean isValidOreIDForType(MeteorType type, ModOreList ore, Block id) {
		HashMap<MeteorType, ArrayList<Block>> map = ids.get(ore);
		if (map != null) {
			ArrayList<Block> li = map.get(type);
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

	public ItemStack getBlock(MeteorType type, boolean ore) {
		if (ore && ReikaRandomHelper.doWithChance(MeteorOptions.ORE.getValue()) && this.hasOresForType(type)) {
			return this.getRandomOre(type);
		}
		else {
			return type.getBlock();
		}
	}

	public void generate(World world, int x, int y, int z, EntityMeteor e) {
		this.generate(world, x, y, z, e.getType(), e.genOres());
	}

	public void generate(World world, int x, int y, int z) {
		this.generate(world, x, y, z, this.getTypeFor(world), true);
	}

	private MeteorType getTypeFor(World world) {
		if (world.provider.dimensionId == -1 || world.provider.isHellWorld)
			return MeteorType.NETHERRACK;
		if (world.provider.dimensionId == 1 || world.provider.hasNoSky)
			return MeteorType.END;
		return MeteorType.STONE;
	}

	private void generate(World world, int x, int y, int z, MeteorType type, boolean ore) {
		BlockArray blocks = this.getMeteorBlockArray(world, x, y, z);
		for (int i = 0; i < blocks.getSize(); i++) {
			int[] xyz = blocks.getNthBlock(i);
			int fx = xyz[0];
			int fy = xyz[1];
			int fz = xyz[2];
			ItemStack is = this.getBlock(type, ore);
			if (fy > 0 && world.getBlock(fx, fy, fz) != Blocks.bedrock)
				ReikaWorldHelper.setBlock(world, fx, fy, fz, is);
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
						//world.setBlock(dx, dy, dz, is.getItem(), is.getItemDamage(), 3);
						blocks.addBlockCoordinate(dx, dy, dz);
					}
				}
			}
		}
		blocks.sink(world, mats);
		return blocks;
	}

	public static boolean canStopMeteor(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		if (b == Blocks.air)
			return false;
		if (ReikaWorldHelper.softBlocks(world, x, y, z))
			return false;
		Material mat = ReikaWorldHelper.getMaterial(world, x, y, z);
		List<Material> mats = ReikaJavaLibrary.makeListFrom(Material.circuits, Material.glass, Material.snow, Material.ice, Material.cactus, Material.craftedSnow, Material.fire, Material.leaves, Material.plants, Material.portal, Material.gourd, Material.redstoneLight, Material.sponge, Material.lava, Material.water, Material.vine, Material.web);
		if (mats.contains(mat))
			return false;
		return true;
	}

	public static boolean canGenOreIn(Block b, ReikaOreHelper ore) {
		return b == ore.getOreGenBlock();
	}

	//Very basic rules:
	public static boolean canGenOreIn(Block blockID, ModOreList ore) {
		if (ore.isNetherOres())
			return blockID == Blocks.netherrack;
		if (ore == ModOreList.ARDITE || ore == ModOreList.COBALT)
			return blockID == Blocks.netherrack;
		if (ore == ModOreList.FIRESTONE)
			return blockID == Blocks.netherrack;
		if (ore == ModOreList.MAGMANITE)
			return blockID == Blocks.netherrack;
		if (ore == ModOreList.AMMONIUM)
			return blockID == Blocks.netherrack;
		if (ore == ModOreList.SODALITE)
			return blockID == Blocks.end_stone;
		if (ore == ModOreList.ESSENCE)
			return blockID == Blocks.netherrack || blockID == Blocks.stone;
		if (blockID == Blocks.end_stone)
			return ore == ModOreList.PITCHBLENDE;
		if (ore == ModOreList.MIMICHITE)
			return true;
		return blockID == Blocks.stone;
	}

	public static boolean canStopMeteorRayTrace(World world, int x, int y, int z, EntityMeteor e, int dist) {
		int r = 4;
		for (int i = 0; i < dist; i++) {
			for (int a = -r; a <= r; a++) {
				for (int b = -r; b <= r; b++) {
					for (int c = -r; c <= r; c++) {
						int dx = a+MathHelper.floor_double(x+e.motionX*i);
						int dy = b+MathHelper.floor_double(y+e.motionY*i);
						int dz = c+MathHelper.floor_double(z+e.motionZ*i);
						if (dy < world.provider.getActualHeight()) {
							if (canStopMeteor(world, dx, dy, dz))
								return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static enum MeteorType {
		STONE(Blocks.stone, 100),
		NETHERRACK(Blocks.netherrack, 20),
		END(Blocks.end_stone, 10),
		SKYSTONE(AppEngHandler.getInstance().skystone, 15);

		public final Block blockID;
		public final int blockMeta;
		private final int chance;
		private Collection<ItemDrop> drops = new ArrayList();

		private static WeightedRandom<MeteorType> rand = new WeightedRandom();

		public static final MeteorType[] list = values();

		private MeteorType(Block b, int c) {
			blockID = b;
			blockMeta = 0;
			chance = c;
		}

		public ItemStack getBlock() {
			return new ItemStack(blockID, 1, blockMeta);
		}

		public static MeteorType getWeightedType() {
			return rand.getRandomEntry();
		}

		private void addDrop(ItemStack is, int min, int max) {
			if (this.isValid())
				drops.add(new ItemDrop(is, min, max));
		}

		private void addDrop(Item is, int min, int max) {
			this.addDrop(new ItemStack(is), min, max);
		}

		static {
			for (int i = 0; i < list.length; i++) {
				MeteorType m = list[i];
				if (m.isValid())
					rand.addEntry(m, m.chance);
			}

			MeteorType.STONE.addDrop(Items.glowstone_dust, 16, 32);
			MeteorType.STONE.addDrop(Items.gunpowder, 9, 18);

			//MeteorType.NETHERRACK.addDrop(Items.glowstone_dust, 16, 32);
			//MeteorType.NETHERRACK.addDrop(Items.gunpowder, 9, 18);
			MeteorType.NETHERRACK.addDrop(Items.blaze_powder, 16, 32);
			MeteorType.NETHERRACK.addDrop(Items.nether_wart, 16, 32);

			//MeteorType.END.addDrop(Items.glowstone_dust, 16, 32);
			//MeteorType.END.addDrop(Items.gunpowder, 9, 18);
			MeteorType.END.addDrop(Items.ender_pearl, 4, 12);
			MeteorType.END.addDrop(Items.nether_star, 0, 1);

			//MeteorType.SKYSTONE.addDrop(Items.glowstone_dust, 16, 32);
			//MeteorType.SKYSTONE.addDrop(Items.gunpowder, 9, 18);
			MeteorType.SKYSTONE.addDrop(AppEngHandler.getInstance().getCertusQuartzDust(), 16, 32);
			Collection<ItemStack> c = AppEngHandler.getInstance().getPossibleMeteorChestLoot();
			for (ItemStack is : c) {
				MeteorType.SKYSTONE.addDrop(is, 0, 1);
			}
		}

		public Collection<ItemDrop> getDroppedItems() {
			return Collections.unmodifiableCollection(drops);
		}

		public boolean isValid() {
			return blockID != null;
		}
	}

}
