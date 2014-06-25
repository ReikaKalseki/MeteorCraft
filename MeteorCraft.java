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

import java.net.URL;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.CommandableUpdateChecker;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.MeteorCraft.Blocks.BlockMeteorMachine;
import Reika.MeteorCraft.Blocks.ItemBlockMeteorMachine;
import Reika.MeteorCraft.Blocks.TileEntityMeteorGun;
import Reika.MeteorCraft.Blocks.TileEntityMeteorRadar;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Entity.EntityTrail;
import Reika.MeteorCraft.Registry.MeteorOptions;
import Reika.RotaryCraft.Auxiliary.ItemStacks;
import Reika.RotaryCraft.Auxiliary.WorktableRecipes;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "MeteorCraft", name="MeteorCraft", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class MeteorCraft extends DragonAPIMod {

	@Instance("MeteorCraft")
	public static MeteorCraft instance = new MeteorCraft();

	public static final MeteorConfig config = new MeteorConfig(instance, MeteorOptions.optionList, null, null, null, 0);

	public static ModLogger logger;

	public static Block meteorMachines;

	@SidedProxy(clientSide="Reika.MeteorCraft.MeteorClient", serverSide="Reika.MeteorCraft.MeteorCommon")
	public static MeteorCommon proxy;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, MeteorOptions.LOGLOADING.getState(), MeteorOptions.DEBUGMODE.getState(), false);

		meteorMachines = new BlockMeteorMachine(MeteorOptions.BLOCKID.getValue()).setUnlocalizedName("meteormachine");
		GameRegistry.registerBlock(meteorMachines, ItemBlockMeteorMachine.class, "Meteor Machines");
		for (int i = 0; i < 3; i++) {
			ItemStack is = new ItemStack(meteorMachines.blockID, i, 0);
			LanguageRegistry.addName(is, "Meteor Defence Gun");
		}
		LanguageRegistry.addName(new ItemStack(meteorMachines.blockID, 3, 0), "Meteor Radar");
		this.basicSetup(evt);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		int id = EntityRegistry.instance().findGlobalUniqueEntityId();
		EntityRegistry.instance().registerModEntity(EntityMeteor.class, "Meteor", id, instance, 384, 20, true);
		EntityRegistry.instance().registerModEntity(EntityTrail.class, "MeteorTrail", id+1, instance, 384, 20, true);
		TickRegistry.registerTickHandler(MeteorSpawnController.instance, Side.SERVER);
		proxy.addRenders();
		proxy.addSounds();

		GameRegistry.registerTileEntity(TileEntityMeteorGun.class, "meteorgun");
		GameRegistry.registerTileEntity(TileEntityMeteorRadar.class, "meteorradar");

		if (MeteorOptions.OLDGEN.getState())
			GameRegistry.registerWorldGenerator(new OldMeteorGenerator());

		ItemStack is = new ItemStack(meteorMachines.blockID, 1, 0);
		ItemStack is1 = new ItemStack(meteorMachines.blockID, 1, 1);
		ItemStack is2 = new ItemStack(meteorMachines.blockID, 1, 2);
		if (ModList.ROTARYCRAFT.isLoaded()) {
			WorktableRecipes.getInstance().addRecipe(is, "SRS", "PCP", "ScS", 'S', ItemStacks.steelingot, 'R', Block.blockRedstone, 'P', ItemStacks.basepanel, 'C', ItemStacks.compressor, 'c', ItemStacks.pcb);
			WorktableRecipes.getInstance().addRecipe(is1, "SLS", "PcP", "SCS", 'L', is, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'C', ItemStacks.compressor, 'c', ItemStacks.pcb);
			WorktableRecipes.getInstance().addRecipe(is2, "SLS", "PcP", "SCS", 'L', is1, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'C', ItemStacks.turbine, 'c', ItemStacks.pcb);

			WorktableRecipes.getInstance().addRecipe(new ItemStack(meteorMachines.blockID, 1, 3), "SsS", "PLP", "ScS", 's', ItemStacks.screen, 'L', ItemStacks.radar, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'c', ItemStacks.pcb);
		}
		else {
			GameRegistry.addRecipe(is, "IRI", "BTB", "IDI", 'I', Item.ingotIron, 'B', Block.blockIron, 'D', Block.dispenser, 'R', Block.blockRedstone, 'T', Block.tnt);
			GameRegistry.addRecipe(is1, "IRI", "BTB", "IDI", 'I', Item.ingotGold, 'B', Block.obsidian, 'D', Block.pistonBase, 'R', Block.blockNetherQuartz, 'T', is);
			GameRegistry.addRecipe(is2, "IRI", "BTB", "IDI", 'I', Item.diamond, 'B', Block.obsidian, 'D', Item.eyeOfEnder, 'R', Block.whiteStone, 'T', is1);

			GameRegistry.addRecipe(new ItemStack(meteorMachines.blockID, 1, 3), "SgS", "SrS", "SrS", 'g', Item.ingotGold, 'S', Item.ingotIron, 'r', Item.redstone);
		}
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {
		config.initModExclusions();
		CustomOreLoader.instance.loadFile();
	}

	@EventHandler
	public void registerCommands(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new MeteorCommand());
	}

	@Override
	public String getDisplayName() {
		return "MeteorCraft";
	}

	@Override
	public String getModAuthorName() {
		return "Reika";
	}

	@Override
	public URL getDocumentationSite() {
		return DragonAPICore.getReikaForumPage();
	}

	@Override
	public String getWiki() {
		return null;
	}

	@Override
	public String getUpdateCheckURL() {
		return CommandableUpdateChecker.reikaURL;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

}
