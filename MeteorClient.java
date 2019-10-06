/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import Reika.DragonAPI.Instantiable.IO.SoundLoader;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Entity.EntityTrail;
import Reika.MeteorCraft.Entity.RenderMeteor;
import Reika.MeteorCraft.Entity.RenderTrail;
import Reika.MeteorCraft.Registry.MeteorSounds;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class MeteorClient extends MeteorCommon {

	@Override
	public void addRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, new RenderMeteor());
		RenderingRegistry.registerEntityRenderingHandler(EntityTrail.class, new RenderTrail());
	}

	@Override
	public void addSounds() {
		//CustomSoundHandler.instance.addSound("meteorcraft", "impact", SoundCategory.MASTER);
		//CustomSoundHandler.instance.addSound("meteorcraft", "flyby", SoundCategory.MASTER);
		//CustomSoundHandler.instance.addSound("meteorcraft", "entry", SoundCategory.MASTER);
		//CustomSoundHandler.instance.addSound("meteorcraft", "boom", SoundCategory.MASTER);

		new SoundLoader(MeteorSounds.soundList).register();
	}

}
