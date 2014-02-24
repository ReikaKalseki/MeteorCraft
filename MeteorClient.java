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

import Reika.DragonAPI.Auxiliary.CustomSoundHandler;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Entity.EntityTrail;
import Reika.MeteorCraft.Entity.RenderMeteor;
import Reika.MeteorCraft.Entity.RenderTrail;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class MeteorClient extends MeteorCommon {

	@Override
	public void addRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, new RenderMeteor());
		RenderingRegistry.registerEntityRenderingHandler(EntityTrail.class, new RenderTrail());
	}

	@Override
	public void addSounds() {
		CustomSoundHandler.instance.addSound("meteorcraft", "impact");
		CustomSoundHandler.instance.addSound("meteorcraft", "flyby");
		CustomSoundHandler.instance.addSound("meteorcraft", "entry");
		CustomSoundHandler.instance.addSound("meteorcraft", "boom");

		//MinecraftForge.EVENT_BUS.register(new SoundLoader(MeteorCraft.instance, MeteorSounds.soundList, MeteorSounds.SOUND_FOLDER));
	}

}
