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
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.Auxiliary.Trackers.CommandableUpdateChecker;
import Reika.DragonAPI.Auxiliary.Trackers.TickRegistry;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.CustomStringDamageSource;
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
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "MeteorCraft", name="MeteorCraft", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")

public class MeteorCraft extends DragonAPIMod {

	@Instance("MeteorCraft")
	public static MeteorCraft instance = new MeteorCraft();

	public static final MeteorConfig config = new MeteorConfig(instance, MeteorOptions.optionList, null, 0);

	public static ModLogger logger;

	public static Block meteorMachines;

	public static final CustomStringDamageSource meteorDamage = (CustomStringDamageSource)new CustomStringDamageSource("was hit by a meteor").setDamageBypassesArmor();

	@SidedProxy(clientSide="Reika.MeteorCraft.MeteorClient", serverSide="Reika.MeteorCraft.MeteorCommon")
	public static MeteorCommon proxy;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		this.verifyVersions();
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, false);

		meteorMachines = new BlockMeteorMachine().setBlockName("meteormachine");
		GameRegistry.registerBlock(meteorMachines, ItemBlockMeteorMachine.class, "Meteor Machines");
		this.basicSetup(evt);
	}

	@Override
	@EventHandler
	public void load(FMLInitializationEvent event) {
		int id = EntityRegistry.instance().findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityMeteor.class, "Meteor", id);
		EntityRegistry.instance().registerModEntity(EntityMeteor.class, "Meteor", EntityRegistry.findGlobalUniqueEntityId(), instance, 384, 20, true);

		id = EntityRegistry.instance().findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityTrail.class, "MeteorTrail", id);
		EntityRegistry.instance().registerModEntity(EntityTrail.class, "MeteorTrail", EntityRegistry.findGlobalUniqueEntityId(), instance, 384, 20, true);

		TickRegistry.instance.registerTickHandler(MeteorSpawnController.instance, Side.SERVER);
		proxy.addRenders();
		proxy.addSounds();

		GameRegistry.registerTileEntity(TileEntityMeteorGun.class, "meteorgun");
		GameRegistry.registerTileEntity(TileEntityMeteorRadar.class, "meteorradar");

		if (MeteorOptions.OLDGEN.getState())
			GameRegistry.registerWorldGenerator(new OldMeteorGenerator(), 100);

		ItemStack is = new ItemStack(meteorMachines, 1, 0);
		ItemStack is1 = new ItemStack(meteorMachines, 1, 1);
		ItemStack is2 = new ItemStack(meteorMachines, 1, 2);
		if (ModList.ROTARYCRAFT.isLoaded()) {
			WorktableRecipes.getInstance().addRecipe(is, "SRS", "PCP", "ScS", 'S', ItemStacks.steelingot, 'R', Blocks.redstone_block, 'P', ItemStacks.basepanel, 'C', ItemStacks.compressor, 'c', ItemStacks.pcb);
			WorktableRecipes.getInstance().addRecipe(is1, "SLS", "PcP", "SCS", 'L', is, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'C', ItemStacks.compressor, 'c', ItemStacks.pcb);
			WorktableRecipes.getInstance().addRecipe(is2, "SLS", "PcP", "SCS", 'L', is1, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'C', ItemStacks.turbine, 'c', ItemStacks.pcb);

			WorktableRecipes.getInstance().addRecipe(new ItemStack(meteorMachines, 1, 3), "SsS", "PLP", "ScS", 's', ItemStacks.screen, 'L', ItemStacks.radar, 'S', ItemStacks.steelingot, 'P', ItemStacks.basepanel, 'c', ItemStacks.pcb);
		}
		else {
			GameRegistry.addRecipe(is, "IRI", "BTB", "IDI", 'I', Items.iron_ingot, 'B', Blocks.iron_block, 'D', Blocks.dispenser, 'R', Blocks.redstone_block, 'T', Blocks.tnt);
			GameRegistry.addRecipe(is1, "IRI", "BTB", "IDI", 'I', Items.gold_ingot, 'B', Blocks.obsidian, 'D', Blocks.piston, 'R', Blocks.quartz_block, 'T', is);
			GameRegistry.addRecipe(is2, "IRI", "BTB", "IDI", 'I', Items.diamond, 'B', Blocks.obsidian, 'D', Items.ender_eye, 'R', Blocks.end_stone, 'T', is1);

			GameRegistry.addRecipe(new ItemStack(meteorMachines, 1, 3), "SgS", "SrS", "SrS", 'g', Items.gold_ingot, 'S', Items.iron_ingot, 'r', Items.redstone);
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
