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

import java.net.URL;

import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Base.DragonAPIMod;
import Reika.DragonAPI.Instantiable.IO.ModLogger;
import Reika.DragonAPI.Libraries.ReikaRegistryHelper;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Entity.EntityTrail;
import Reika.MeteorCraft.Registry.MeteorOptions;
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
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod( modid = "MeteorCraft", name="MeteorCraft", version="beta", certificateFingerprint = "@GET_FINGERPRINT@", dependencies="required-after:DragonAPI")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class MeteorCraft extends DragonAPIMod {

	@Instance("MeteorCraft")
	public static MeteorCraft instance = new MeteorCraft();

	public static final MeteorConfig config = new MeteorConfig(instance, MeteorOptions.optionList, null, null, null, 0);

	public static ModLogger logger;

	@SidedProxy(clientSide="Reika.MeteorCraft.MeteorClient", serverSide="Reika.MeteorCraft.MeteorCommon")
	public static MeteorCommon proxy;

	@Override
	@EventHandler
	public void preload(FMLPreInitializationEvent evt) {
		config.loadSubfolderedConfigFile(evt);
		config.initProps(evt);

		logger = new ModLogger(instance, MeteorOptions.LOGLOADING.getState(), MeteorOptions.DEBUGMODE.getState(), false);

		ReikaRegistryHelper.setupModData(instance, evt);
		ReikaRegistryHelper.setupVersionChecking(evt);
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
	}

	@Override
	@EventHandler
	public void postload(FMLPostInitializationEvent evt) {

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
		return DragonAPICore.getReikaForumPage(instance);
	}

	@Override
	public boolean hasWiki() {
		return false;
	}

	@Override
	public URL getWiki() {
		return null;
	}

	@Override
	public boolean hasVersion() {
		return false;
	}

	@Override
	public String getVersionName() {
		return null;
	}

	@Override
	public ModLogger getModLogger() {
		return logger;
	}

}
