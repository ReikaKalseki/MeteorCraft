/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Registry;

import java.net.URL;

import net.minecraft.client.audio.SoundCategory;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import Reika.DragonAPI.Instantiable.Data.Immutable.WorldLocation;
import Reika.DragonAPI.Interfaces.Registry.SoundEnum;
import Reika.MeteorCraft.MeteorCraft;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public enum MeteorSounds implements SoundEnum {

	ENTRY("entry"),
	FLYBY("flyby"),
	BOOM("boom"),
	IMPACT("impact");

	public static final MeteorSounds[] soundList = values();

	public static final String PREFIX = "Reika/MeteorCraft/";
	public static final String SOUND_FOLDER = "Sounds/";
	private static final String SOUND_PREFIX = "Reika.MeteorCraft.Sounds.";
	private static final String SOUND_DIR = "Sounds/";
	private static final String SOUND_EXT = ".ogg";
	private static final String MUSIC_FOLDER = "music/";
	private static final String MUSIC_PREFIX = "music.";

	private final String path;
	private final String name;

	private boolean isVolumed = false;

	private MeteorSounds(String n) {
		if (n.startsWith("#")) {
			isVolumed = true;
			n = n.substring(1);
		}
		name = n;
		path = PREFIX+SOUND_FOLDER+name+SOUND_EXT;
	}

	public void playSound(Entity e) {
		this.playSound(e, 1, 1);
	}

	public void playSound(Entity e, float vol, float pitch) {
		this.playSound(e.worldObj, e.posX, e.posY, e.posZ, vol, pitch);
	}

	public void playSound(World world, double x, double y, double z, float vol, float pitch) {
		if (FMLCommonHandler.instance().getEffectiveSide() != Side.SERVER)
			return;
		vol *= MeteorOptions.VOLUME.getFloat();
		;//ReikaPacketHelper.sendSoundPacket(MeteorCraft.packetChannel, this, world, x, y, z, vol, pitch);
	}

	public void playSound(World world, double x, double y, double z, float vol, float pitch, boolean attenuate) {

	}

	public void playSoundAtBlock(World world, int x, int y, int z, float vol, float pitch) {
		this.playSound(world, x+0.5, y+0.5, z+0.5, vol, pitch);
	}

	public void playSoundAtBlock(World world, int x, int y, int z) {
		this.playSound(world, x+0.5, y+0.5, z+0.5, 1, 1);
	}

	public void playSoundAtBlock(TileEntity te) {
		this.playSoundAtBlock(te.worldObj, te.xCoord, te.yCoord, te.zCoord);
	}

	public void playSoundAtBlock(WorldLocation loc) {
		this.playSoundAtBlock(loc.getWorld(), loc.xCoord, loc.yCoord, loc.zCoord);
	}

	public void playSoundNoAttenuation(World world, double x, double y, double z, float vol, float pitch, int broadcast) {
		//no packet handler to do this
	}

	public String getName() {
		return this.name();
	}

	public String getPath() {
		return path;
	}

	public URL getURL() {
		return MeteorCraft.class.getResource(SOUND_DIR+name+SOUND_EXT);
	}

	public static MeteorSounds getSoundByName(String name) {
		for (int i = 0; i < soundList.length; i++) {
			if (soundList[i].name().equals(name))
				return soundList[i];
		}
		MeteorCraft.logger.logError("\""+name+"\" does not correspond to a registered sound!");
		return null;
	}

	@Override
	public SoundCategory getCategory() {
		return SoundCategory.MASTER;
	}

	@Override
	public boolean canOverlap() {
		return true;
	}

	@Override
	public boolean attenuate() {
		return false;
	}

	@Override
	public float getModulatedVolume() {
		return 1;
	}

	@Override
	public boolean preload() {
		return false;
	}
}
