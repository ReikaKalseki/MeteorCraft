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

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.FactorizationHandler;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Event.ImpactEvent;

public class MeteorImpact {

	public final int posX;
	public final int posY;
	public final int posZ;
	public final float radius;
	private World world;

	private static final Random rand = new Random();

	public MeteorImpact(World world, int x, int y, int z, float range) {
		this.world = world;
		posX = x;
		posY = Math.max(y, MathHelper.ceiling_float_int(range));
		posZ = z;
		radius = range;
	}

	public void impact(EntityMeteor e) {
		MinecraftForge.EVENT_BUS.post(new ImpactEvent(e, posX, posY, posZ, radius));
		if (!world.isRemote) {
			double d = 0.5;
			double vx = e.motionX*d;
			double vz = e.motionZ*d;
			double vy = Math.abs(e.motionY);
			for (float i = -radius*1.2F; i <= radius*1.2F; i++) {
				for (float j = -radius*1.2F; j <= radius*1.2F; j++) {
					for (float k = -radius*1.2F; k <= radius*1.2F; k++) {
						double dd = ReikaMathLibrary.py3d(i, j, k);
						double dx = posX+0.5+i;
						double dy = posY+0.5+j;
						double dz = posZ+0.5+k;
						int x2 = MathHelper.floor_double(dx);
						int y2 = MathHelper.floor_double(dy);
						int z2 = MathHelper.floor_double(dz);
						int id = world.getBlockId(x2, y2, z2);
						int meta = world.getBlockMetadata(x2, y2, z2);
						if (dd <= radius) {
							if (id != 0) {
								if (this.canEntitize(world, x2, y2, z2, id, meta)) {
									ItemStack toDrop = this.getDroppedBlock(id, meta);
									EntityFallingSand es = new EntityFallingSand(world, x2, y2+4, z2, toDrop.itemID, toDrop.getItemDamage());
									es.addVelocity(vx, vy*rand.nextDouble()*rand.nextDouble(), vz);
									es.velocityChanged = true;
									es.fallTime = -1000;
									world.setBlock(x2, y2, z2, 0);
									world.spawnEntityInWorld(es);
								}
								else if (this.canDestroy(world, x2, y2, z2, id, meta)) {
									if (y2 > 0) {
										world.setBlock(x2, y2, z2, 0);
									}
								}
								else {

								}
							}
						}
					}
				}
			}
		}

		AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(posX, posY, posZ).expand(9, 9, 9);
		List<EntityLivingBase> li = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (int i = 0; i < li.size(); i++) {
			EntityLivingBase el = li.get(i);
			el.attackEntityFrom(DamageSource.generic, Integer.MAX_VALUE);
		}
		ReikaSoundHelper.playSoundAtBlock(world, posX, posY, posZ, "meteorcraft:impact");
		//MeteorSounds.IMPACT.playSoundAtBlock(world, posX, posY, posZ);
		for (int i = 0; i < world.playerEntities.size(); i++) {
			EntityPlayer ep = (EntityPlayer)world.playerEntities.get(i);
			ep.playSound("random.explode", 1, 1);
		}
		ReikaParticleHelper.EXPLODE.spawnAroundBlock(world, posX, posY, posZ, 2);

		int r = 12;
		for (int i = -r; i <= r; i++) {
			for (int j = -r; j <= r; j++) {
				for (int k = -r; k <= r; k++) {
					int dx = posX+i;
					int dy = posY+j;
					int dz = posZ+k;
					double dd = ReikaMathLibrary.py3d(i, j, k)-2+rand.nextDouble()*2;
					if (dd < r) {
						Material mat = world.getBlockMaterial(dx, dy, dz);
						int id = world.getBlockId(dx, dy, dz);
						int meta = world.getBlockMetadata(dx, dy, dz);
						Block b = Block.blocksList[id];
						if (mat == Material.glass || mat == Material.ice) {
							b.dropBlockAsItem(world, dx, dy, dz, meta, 0);
							world.setBlock(dx, dy, dz, 0);
							ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Block.glass);
						}
						if (mat == Material.circuits || mat == Material.web) {
							b.dropBlockAsItem(world, dx, dy, dz, meta, 0);
							world.setBlock(dx, dy, dz, 0);
						}
					}
				}
			}
		}

		MeteorGenerator.instance.generate(world, posX, posY, posZ, e.getType());

		int num = 16+rand.nextInt(16);
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
			int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
			int dy = world.getTopSolidOrLiquidBlock(dx, dz)+1;
			ReikaItemHelper.dropItem(world, dx, dy, dz, new ItemStack(Item.glowstone));
		}

		num = 12+rand.nextInt(37);
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
			int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
			int dy = ReikaRandomHelper.getRandomPlusMinus(posY, (int)radius);
			ReikaWorldHelper.ignite(world, dx, dy, dz);
		}

		num = 9+rand.nextInt(9);
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
			int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
			int dy = world.getTopSolidOrLiquidBlock(dx, dz)+1;
			ReikaItemHelper.dropItem(world, dx, dy, dz, new ItemStack(Item.gunpowder));
		}
	}

	private boolean canDestroy(World world, int x, int y, int z, int id, int meta) {
		if (id == Block.bedrock.blockID)
			return false;
		if (id == FactorizationHandler.getInstance().bedrockID)
			return false;
		return true;
	}

	private ItemStack getDroppedBlock(int id, int meta) {
		int dropid = id;//Block.blocksList[id].idDropped(meta, rand, 0);
		int dropmeta = meta;//Block.blocksList[id].damageDropped(meta);
		if (id == Block.grass.blockID)
			return new ItemStack(Block.dirt);
		return new ItemStack(dropid, 1, dropmeta);
	}

	private boolean canEntitize(World world, int x, int y, int z, int id, int meta) {
		if (y <= 0)
			return false;
		if (id == 0)
			return false;
		if (id == Block.bedrock.blockID)
			return false;
		Block b = Block.blocksList[id];
		if (b.hasTileEntity(meta))
			return false;
		if (ReikaWorldHelper.softBlocks(world, x, y, z))
			return false;
		if (b.getRenderType() != 0) //To prevent weird looking flying sand entities
			return false;
		return true;
	}

}
