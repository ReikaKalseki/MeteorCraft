/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFluid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidBase;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.ModInteract.ReikaTwilightHelper;
import Reika.MeteorCraft.MeteorCraft;
import Reika.MeteorCraft.MeteorGenerator;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.MeteorImpact;
import Reika.MeteorCraft.Event.AirburstEvent;
import Reika.MeteorCraft.Event.EntryEvent;
import Reika.MeteorCraft.Registry.MeteorOptions;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityMeteor extends Entity implements IEntityAdditionalSpawnData {

	private MeteorType type;
	private boolean crossed = false;
	private boolean impact = false;
	private boolean boom = false;
	private int explodeY;

	public EntityMeteor(World world) {
		super(world);
	}

	public EntityMeteor(World world, int x, int y, int z, MeteorType type) {
		super(world);
		this.setPosition(x, y, z);
		this.type = type;
		double vx = ReikaRandomHelper.getRandomPlusMinus(0.75, 0.25);
		double vz = ReikaRandomHelper.getRandomPlusMinus(0.75, 0.25);
		if (rand.nextBoolean())
			vx = -vx;
		if (rand.nextBoolean())
			vz = -vz;
		this.addVelocity(vx, -4, vz);
		velocityChanged = true;
		noClip = true;
		if (!worldObj.isRemote)
			MinecraftForge.EVENT_BUS.post(new EntryEvent(this));
	}

	public EntityMeteor setExploding() {
		explodeY = this.getRandomYToExplodeAlways();
		return this;
	}

	public EntityMeteor setType(MeteorType type) {
		this.type = type;
		return this;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (ticksExisted > 1200)
			this.setDead();
		if (posY < 0) {
			this.setDead();
		}
		if (!isDead) {
			this.moveEntity(motionX, motionY, motionZ);
			int x = MathHelper.floor_double(posX);
			int y = MathHelper.floor_double(posY);
			int z = MathHelper.floor_double(posZ);
			World world = worldObj;
			if (posY < explodeY) {
				this.destroy();
			}
			if (!world.checkChunksExist(x, y, z, x, y, z)) {
				if (world.isRemote)
					Minecraft.getMinecraft().thePlayer.playSound("random.explode", 1, 0.2F);
				this.setDead();
			}
			int id = world.getBlockId(x, y, z);
			if (MeteorGenerator.canStopMeteor(world, x, y, z)) {
				this.onImpact(world, x, y, z);
			}
			else if (posY < MeteorOptions.MINY.getValue()) {
				this.destroy();
			}
			else if (!MeteorCraft.config.canImpactInBiome(world.getBiomeGenForCoords(x, z))) {
				this.destroy();
			}
			else if (!world.isRemote) {
				int r = 4;
				for (int c = 0; c < 4; c++) {
					double dd = ReikaMathLibrary.py3d(motionX, motionY, motionZ);
					int ox = MathHelper.floor_double(x-motionX*c/dd);
					int oy = MathHelper.floor_double(y-motionY*c/dd);
					int oz = MathHelper.floor_double(z-motionZ*c/dd);
					for (int i = -r; i <= r; i++) {
						for (int j = -r; j <= r; j++) {
							for (int k = -r; k <= r; k++) {
								int dx = x+i+ox;
								int dy = y+j+oy;
								int dz = z+k+oz;
								int id2 = world.getBlockId(dx, dy, dz);
								if (id != 0) {
									Block b = Block.blocksList[id];
									if (!(b instanceof BlockFluid || b instanceof BlockFluidBase)) {
										b.dropBlockAsItem(world, dx, dy, dz, world.getBlockMetadata(dx, dy, dz), 0);
										world.setBlock(dx, dy, dz, 0);
									}
								}
							}
						}
					}
				}
			}
			if (!worldObj.isRemote)
				world.spawnEntityInWorld(new EntityTrail(world, posX-motionX, posY-motionY, posZ-motionZ));
			if (posY <= worldObj.provider.getAverageGroundLevel()+224 && !crossed) {
				crossed = true;
				this.playSound("meteorcraft:flyby", 1, 1);
				//MeteorSounds.FLYBY.playSoundAtEntity(world, this);
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:flyby", 1, 1);
					//MeteorSounds.FLYBY.playSoundAtEntity(world, Minecraft.getMinecraft().thePlayer);
				}
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+127 && !boom) {
				crossed = true;
				this.playSound("meteorcraft:boom", 1, 1);
				//MeteorSounds.BOOM.playSoundAtEntity(world, this);
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:boom", 1, 1);
					//MeteorSounds.BOOM.playSoundAtEntity(world, Minecraft.getMinecraft().thePlayer);
				}
				boom = true;
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+12 && !impact) {
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:impact", 1, 1);
					//MeteorSounds.IMPACT.playSoundAtEntity(world, Minecraft.getMinecraft().thePlayer);
				}
				this.playSound("meteorcraft:impact", 1, 1);
				//MeteorSounds.IMPACT.playSoundAtEntity(world, this);
				impact = true;
			}
		}
		if (motionY > -4 && !worldObj.isRemote)
			this.setDead();
	}

	private int getRandomYToExplodeAlways() {
		if (worldObj.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return 30;
		if (worldObj.provider.dimensionId == ReikaTwilightHelper.getDimensionID()) {
			return 128;
		}
		switch(worldObj.provider.dimensionId) {
		case -1:
			return 160;
		case 1:
			return 96;
		default:
			return 140;
		}
	}

	@Override
	protected void entityInit() {
		if (worldObj.isRemote) {
			Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:entry", 1, 1);
			//MeteorSounds.ENTRY.playSoundAtEntity(worldObj, Minecraft.getMinecraft().thePlayer);
		}
		//MeteorSounds.ENTRY.playSoundAtEntity(worldObj, this);
		this.playSound("meteorcraft:entry", 1, 1);

		explodeY = -1;

		if (!worldObj.isRemote) {
			if (worldObj.provider.dimensionId == ReikaTwilightHelper.getDimensionID() && MeteorOptions.DIM7BURST.getState())
				explodeY = this.getRandomYToExplodeAlways();
			switch(worldObj.provider.dimensionId) {
			case 0:
				if (MeteorOptions.DIM0BURST.getState())
					explodeY = this.getRandomYToExplodeAlways();
				break;
			case 1:
				if (MeteorOptions.ENDBURST.getState())
					explodeY = this.getRandomYToExplodeAlways();
				break;
			case -1:
				if (MeteorOptions.NETHERBURST.getState())
					explodeY = this.getRandomYToExplodeAlways();
				break;
			default:
				if (MeteorOptions.OTHER.getState())
					explodeY = this.getRandomYToExplodeAlways();
				break;
			}
		}
	}

	public MeteorType getType() {
		return type != null ? type : MeteorType.STONE;
	}

	private void setType(int o) {
		type = MeteorType.values()[o];
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput data) {
		data.writeInt(this.getType().ordinal());
	}

	@Override
	public void readSpawnData(ByteArrayDataInput data) {
		this.setType(data.readInt());
	}

	protected void onImpact(World world, int x, int y, int z) {
		this.setDead();
		MeteorImpact imp = new MeteorImpact(world, x, world.getTopSolidOrLiquidBlock(x, z), z, 4);
		imp.impact(this);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {

	}

	@Override
	public boolean isInRangeToRenderDist(double par1)
	{
		return true;
	}

	public void destroy() {
		MinecraftForge.EVENT_BUS.post(new AirburstEvent(this));
		int n = 32+rand.nextInt(48); //135 is approx the max in a impact meteor
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(posX, 2);
			double ry = ReikaRandomHelper.getRandomPlusMinus(posY, 2);
			double rz = ReikaRandomHelper.getRandomPlusMinus(posZ, 2);
			ItemStack is = MeteorGenerator.instance.getBlock(this);
			if (is.itemID < Block.blocksList.length) { //because some mods are derps and register items as ores
				EntityFallingSand e = new EntityFallingSand(worldObj, rx, ry, rz, is.itemID, is.getItemDamage());
				e.fallTime = -10000;
				if (!worldObj.isRemote)
					worldObj.spawnEntityInWorld(e);
			}
			else {

			}
		}
		if (!worldObj.isRemote)
			worldObj.newExplosion(null, posX, posY, posZ, 3F, true, true);
		n = 12+rand.nextInt(24);
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(posX, 8);
			double ry = ReikaRandomHelper.getRandomPlusMinus(posY, 8);
			double rz = ReikaRandomHelper.getRandomPlusMinus(posZ, 8);
			ReikaItemHelper.dropItem(worldObj, rx, ry, rz, new ItemStack(Item.glowstone));
		}
		n = 6+rand.nextInt(6);
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(posX, 8);
			double ry = ReikaRandomHelper.getRandomPlusMinus(posY, 8);
			double rz = ReikaRandomHelper.getRandomPlusMinus(posZ, 8);
			ReikaItemHelper.dropItem(worldObj, rx, ry, rz, new ItemStack(Item.gunpowder));
		}
		if (worldObj.isRemote) {
			Minecraft.getMinecraft().thePlayer.playSound("random.explode", 3, 0.01F);
			Minecraft.getMinecraft().thePlayer.playSound("random.explode", 3, 0.1F);
			Minecraft.getMinecraft().thePlayer.playSound("random.explode", 3, 0.2F);
		}

		int r = 8;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					int x = MathHelper.floor_double(posX);
					int y = MathHelper.floor_double(posY);
					int z = MathHelper.floor_double(posZ);
					World world = worldObj;
					int dx = x+i;
					int dy = y+j;
					int dz = z+k;
					double dd = ReikaMathLibrary.py3d(i, j, k)-2+rand.nextDouble()*2;
					if (dd < r) {
						Material mat = world.getBlockMaterial(dx, dy, dz);
						if (mat == Material.glass || mat == Material.ice) {
							world.setBlock(dx, dy, dz, 0);
							ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Block.glass);
						}
					}
				}
			}
		}
		this.setDead();
	}

}
