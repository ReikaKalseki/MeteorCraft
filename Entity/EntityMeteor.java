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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.MeteorCraft.MeteorGenerator;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.MeteorImpact;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityMeteor extends Entity implements IEntityAdditionalSpawnData {

	private MeteorType type;
	private boolean crossed = false;
	private boolean impact = false;
	private boolean boom = false;

	public EntityMeteor(World world) {
		super(world);
	}

	public EntityMeteor(World world, int x, int y, int z) {
		super(world);
		this.setPosition(x, y, z);
		type = MeteorType.STONE;
		double vx = ReikaRandomHelper.getRandomPlusMinus(0.75, 0.25);
		double vz = ReikaRandomHelper.getRandomPlusMinus(0.75, 0.25);
		if (rand.nextBoolean())
			vx = -vx;
		if (rand.nextBoolean())
			vz = -vz;
		this.addVelocity(vx, -4, vz);
		velocityChanged = true;
		noClip = true;
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
			int id = world.getBlockId(x, y, z);
			if (MeteorGenerator.canStopMeteor(world, x, y, z)) {
				this.onImpact(world, x, y, z);
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
										b.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(dx, dy, dz), 0);
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
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:flyby", 1, 1);
				}
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+127 && !boom) {
				crossed = true;
				this.playSound("meteorcraft:boom", 1, 1);
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:boom", 1, 1);
				}
				boom = true;
			}
			if (posY <= worldObj.provider.getAverageGroundLevel()+12 && !impact) {
				if (worldObj.isRemote) {
					Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:impact", 1, 1);
				}
				this.playSound("meteorcraft:impact", 1, 1);
				impact = true;
			}
		}
		if (motionY > -4 && !worldObj.isRemote)
			this.setDead();
	}

	@Override
	protected void entityInit() {
		if (worldObj.isRemote) {
			Minecraft.getMinecraft().thePlayer.playSound("meteorcraft:entry", 1, 1);
		}
		this.playSound("meteorcraft:entry", 1, 1);
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

}
