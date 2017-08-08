/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Entity;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidBase;
import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ReikaTwilightHelper;
import Reika.MeteorCraft.MeteorCraft;
import Reika.MeteorCraft.MeteorGenerator;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.MeteorImpact;
import Reika.MeteorCraft.API.MeteorEntity;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.AirburstEvent;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.EntryEvent;
import Reika.MeteorCraft.Registry.MeteorOptions;
import Reika.MeteorCraft.Registry.MeteorSounds;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityMeteor extends Entity implements IEntityAdditionalSpawnData, MeteorEntity {

	private MeteorType type;
	private boolean spawned = false;
	private boolean crossed = false;
	private boolean impact = false;
	private boolean boom = false;
	private int explodeY;
	private boolean genOres;

	private double spawnX;
	private double spawnY;
	private double spawnZ;

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
		genOres = ReikaRandomHelper.doWithChance(MeteorOptions.LOADCHANCE.getValue());

		spawnX = x;
		spawnY = y;
		spawnZ = z;
	}

	public EntityMeteor setExploding() {
		explodeY = this.getRandomYToExplodeAlways();
		return this;
	}

	public EntityMeteor setType(MeteorType type) {
		if (type.isValid())
			this.type = type;
		return this;
	}

	@Override
	public void onUpdate() {
		if (ticksExisted < 2)
			MinecraftForge.EVENT_BUS.post(new EntryEvent(this));
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
			if (y < 256 && !world.checkChunksExist(x, y, z, x, y, z)) {
				if (world.isRemote)
					this.playClientFullVolSound("random.explode", 1, 0.2F);
				this.setDead();
			}/*
			Block[] b = new Block[8];
			for (int i = 0; i < b.length; i++) {
				int dx = MathHelper.floor_double(x+motionX);
				int dy = MathHelper.floor_double(y+motionY);
				int dz = MathHelper.floor_double(z+motionZ);
				b[i] = world.getBlock(dx, dy, dz);
			}*/
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
				for (int c = 0; c < 16; c++) {
					double dd = ReikaMathLibrary.py3d(motionX, motionY, motionZ);
					int ox = MathHelper.floor_double(x+motionX*c/dd);
					int oy = MathHelper.floor_double(y+motionY*c/dd);
					int oz = MathHelper.floor_double(z+motionZ*c/dd);
					for (int i = -r; i <= r; i++) {
						for (int j = -r; j <= r; j++) {
							for (int k = -r; k <= r; k++) {
								int dx = x+i+ox;
								int dy = y+j+oy;
								int dz = z+k+oz;
								Block id2 = world.getBlock(dx, dy, dz);
								if (id2 != Blocks.air) {
									if (!(id2 instanceof BlockLiquid || id2 instanceof BlockFluidBase)) {
										id2.dropBlockAsItem(world, dx, dy, dz, world.getBlockMetadata(dx, dy, dz), 0);
										world.setBlockToAir(dx, dy, dz);
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
				//this.playSound("meteorcraft:flyby", 1, 1);
				this.playSound(MeteorSounds.FLYBY, this);
				if (worldObj.isRemote) {
					//Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:flyby", 1, 1);
					this.playClientFullVolSound(MeteorSounds.FLYBY);
				}
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+127 && !boom) {
				crossed = true;
				//this.playSound("meteorcraft:boom", 1, 1);
				this.playSound(MeteorSounds.BOOM, this);
				if (worldObj.isRemote) {
					//Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:boom", 1, 1);
					this.playClientFullVolSound(MeteorSounds.BOOM);
				}
				boom = true;
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+12 && !impact) {
				if (worldObj.isRemote) {
					//Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:impact", 1, 1);
					this.playClientFullVolSound(MeteorSounds.IMPACT);
				}
				//this.playSound("meteorcraft:impact", 1, 1);
				this.playSound(MeteorSounds.IMPACT, this);
				impact = true;
			}
		}
		if (motionY > -4 && !worldObj.isRemote)
			this.setDead();
	}

	@SideOnly(Side.CLIENT)
	private void playClientFullVolSound(MeteorSounds sound) {
		this.playSound(sound, Minecraft.getMinecraft().thePlayer);
	}

	@SideOnly(Side.CLIENT)
	private void playClientFullVolSound(String s, float v, float p) {
		Minecraft.getMinecraft().thePlayer.playSound(s, v, p);
	}

	private void playSound(MeteorSounds sound, Entity e) {
		double x = e.posX;
		double y = e.posY;
		double z = e.posZ;
		if (worldObj.isRemote)
			ReikaSoundHelper.playClientSound(sound, x, y, z, 1, 1);
	}

	private int getRandomYToExplodeAlways() {
		if (worldObj.getWorldInfo().getTerrainType() == WorldType.FLAT)
			return 32+rand.nextInt(32);
		if (worldObj.provider.dimensionId == ReikaTwilightHelper.getDimensionID()) {
			return ReikaRandomHelper.getRandomPlusMinus(128, 24);
		}
		switch(worldObj.provider.dimensionId) {
			case -1:
				return 160+rand.nextInt(32);
			case 1:
				return 96+rand.nextInt(20);
			default:
				return 140+rand.nextInt(32);
		}
	}

	@Override
	protected void entityInit() {
		if (!spawned) {
			if (worldObj.isRemote) {
				this.playClientFullVolSound(MeteorSounds.ENTRY);
				//MeteorSounds.ENTRY.playSoundAtEntity(worldObj, Minecraft.getMinecraft().thePlayer);
			}
			//MeteorSounds.ENTRY.playSoundAtEntity(worldObj, this);
			this.playSound(MeteorSounds.ENTRY, this);
			spawned = true;
		}

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
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(this.getType().ordinal());
		data.writeBoolean(genOres);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		this.setType(data.readInt());
		genOres = data.readBoolean();
	}

	protected void onImpact(World world, int x, int y, int z) {
		this.setDead();
		MeteorImpact imp = new MeteorImpact(world, x, world.getTopSolidOrLiquidBlock(x, z), z, 4);
		imp.impact(this);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound NBT) {
		spawned = NBT.getBoolean("spawned");
		crossed = NBT.getBoolean("crossed");
		impact = NBT.getBoolean("impact");
		boom = NBT.getBoolean("boom");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound NBT) {
		NBT.setBoolean("spawned", spawned);
		NBT.setBoolean("crossed", crossed);
		NBT.setBoolean("impact", impact);
		NBT.setBoolean("boom", boom);
	}

	@Override
	public boolean isInRangeToRenderDist(double par1)
	{
		return true;
	}

	public void destroy() {
		Collection<EntityFallingBlock> li = new ArrayList();
		Collection<EntityItem> li2 = new ArrayList();
		AirburstEvent evt = new AirburstEvent(this, li, li2);
		int n = 32+rand.nextInt(48); //135 is approx the max in a impact meteor
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(posX, 2);
			double ry = ReikaRandomHelper.getRandomPlusMinus(posY, 2);
			double rz = ReikaRandomHelper.getRandomPlusMinus(posZ, 2);
			ItemStack is = MeteorGenerator.instance.getBlock(this.getType(), genOres);
			Block b = Block.getBlockFromItem(is.getItem());
			if (b != null) { //because some mods are derps and register items as ores
				EntityFallingBlock e = new EntityFallingBlock(worldObj, rx, ry, rz, b, is.getItemDamage());
				e.field_145812_b = -10000;
				if (!worldObj.isRemote)
					worldObj.spawnEntityInWorld(e);
				li.add(e);
			}
			else {

			}
		}
		if (!worldObj.isRemote)
			worldObj.newExplosion(null, posX, posY, posZ, 3F, true, true);

		Collection<ItemDrop> drops = this.getType().getDroppedItems();
		for (ItemDrop drop : drops) {
			ItemStack is = ReikaItemHelper.getSizedItemStack(drop.getItemStack(), 1);
			int num = drop.getDropCount();
			for (int i = 0; i < num; i++) {
				double rx = ReikaRandomHelper.getRandomPlusMinus(posX, 8);
				double ry = ReikaRandomHelper.getRandomPlusMinus(posY, 8);
				double rz = ReikaRandomHelper.getRandomPlusMinus(posZ, 8);
				li2.add(ReikaItemHelper.dropItem(worldObj, rx, ry, rz, is));
			}
		}

		if (worldObj.isRemote) {
			this.playClientFullVolSound("random.explode", 3, 0.01F);
			this.playClientFullVolSound("random.explode", 3, 0.1F);
			this.playClientFullVolSound("random.explode", 3, 0.2F);
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
						Material mat = ReikaWorldHelper.getMaterial(world, dx, dy, dz);
						if (mat == Material.glass || mat == Material.ice) {
							world.setBlockToAir(dx, dy, dz);
							ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Blocks.glass);
						}
					}
				}
			}
		}
		MinecraftForge.EVENT_BUS.post(evt);
		this.setDead();
	}

	public boolean genOres() {
		return genOres;
	}

	@Override
	public Vec3 getVelocityVector() {
		return Vec3.createVectorHelper(motionX, motionY, motionZ);
	}

	@Override
	public Vec3 getProjectedIntercept(double yLevel) {
		double dy = (posY-yLevel)/motionY;
		return Vec3.createVectorHelper(posX+motionX*dy, yLevel, posZ+motionZ*dy);
	}

	@Override
	public Block getMeteorRockType() {
		return this.getType().blockID;
	}

	@Override
	public Vec3 getSpawnPosition() {
		return Vec3.createVectorHelper(spawnX, spawnY, spawnZ);
	}

}
