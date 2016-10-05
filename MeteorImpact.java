/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2016
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import Reika.DragonAPI.Instantiable.ItemDrop;
import Reika.DragonAPI.Interfaces.Block.SemiUnbreakable;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.FactorizationHandler;
import Reika.MeteorCraft.API.Event.MeteorCraftEvent.ImpactEvent;
import Reika.MeteorCraft.Entity.EntityMeteor;
import Reika.MeteorCraft.Registry.MeteorSounds;

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
						Block id = world.getBlock(x2, y2, z2);
						int meta = world.getBlockMetadata(x2, y2, z2);
						if (dd <= radius) {
							if (id != Blocks.air) {
								if (this.canEntitize(world, x2, y2, z2, id, meta)) {
									ItemStack toDrop = this.getDroppedBlock(id, meta);
									EntityFallingBlock es = new EntityFallingBlock(world, x2, y2+4, z2, Block.getBlockFromItem(toDrop.getItem()), toDrop.getItemDamage());
									es.addVelocity(vx, vy*rand.nextDouble()*rand.nextDouble(), vz);
									es.velocityChanged = true;
									es.field_145812_b = -1000;
									world.setBlockToAir(x2, y2, z2);
									world.spawnEntityInWorld(es);
								}
								else if (this.canDestroy(world, x2, y2, z2, id, meta)) {
									if (y2 > 0) {
										world.setBlockToAir(x2, y2, z2);
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

		AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(posX, posY, posZ).expand(16, 16, 16);
		List<EntityLivingBase> li = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (EntityLivingBase el : li) {
			el.attackEntityFrom(MeteorCraft.meteorDamage, this.getDamageFor(el));
		}
		//ReikaSoundHelper.playSoundAtBlock(world, posX, posY, posZ, "meteorcraft:impact");
		if (world.isRemote)
			ReikaSoundHelper.playClientSound(MeteorSounds.IMPACT, posX+0.5, posY+0.5, posZ+0.5, 1, 1);
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
						Material mat = ReikaWorldHelper.getMaterial(world, dx, dy, dz);
						Block b = world.getBlock(dx, dy, dz);
						int meta = world.getBlockMetadata(dx, dy, dz);
						if (mat == Material.glass || mat == Material.ice) {
							if (!world.isRemote) {
								b.dropBlockAsItem(world, dx, dy, dz, meta, 0);
								world.setBlockToAir(dx, dy, dz);
							}
							ReikaSoundHelper.playBreakSound(world, dx, dy, dz, Blocks.glass);
						}
						if (mat == Material.circuits || mat == Material.web) {
							b.dropBlockAsItem(world, dx, dy, dz, meta, 0);
							if (!world.isRemote)
								world.setBlockToAir(dx, dy, dz);
						}
					}
				}
			}
		}

		if (!world.isRemote)
			MeteorGenerator.instance.generate(world, posX, posY, posZ, e);
		/*
		int num = 16+rand.nextInt(16);
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
			int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
			int dy = world.getTopSolidOrLiquidBlock(dx, dz)+1;
			ReikaItemHelper.dropItem(world, dx, dy, dz, new ItemStack(Items.glowstone_dust));
		}

		num = 9+rand.nextInt(9);
		for (int i = 0; i < num; i++) {
			int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
			int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
			int dy = world.getTopSolidOrLiquidBlock(dx, dz)+1;
			ReikaItemHelper.dropItem(world, dx, dy, dz, new ItemStack(Items.gunpowder));
		}*/
		if (!world.isRemote) {
			Collection<ItemDrop> drops = e.getType().getDroppedItems();
			for (ItemDrop drop : drops) {
				ItemStack is = ReikaItemHelper.getSizedItemStack(drop.getItem(), 1);
				int n = drop.getDropCount();
				for (int i = 0; i < n; i++) {
					int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
					int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
					int dy = world.getTopSolidOrLiquidBlock(dx, dz)+1;
					ReikaItemHelper.dropItem(world, dx, dy, dz, is);
				}
			}

			int num = 12+rand.nextInt(37);
			for (int i = 0; i < num; i++) {
				int dx = ReikaRandomHelper.getRandomPlusMinus(posX, (int)radius);
				int dz = ReikaRandomHelper.getRandomPlusMinus(posZ, (int)radius);
				int dy = ReikaRandomHelper.getRandomPlusMinus(posY, (int)radius);
				ReikaWorldHelper.ignite(world, dx, dy, dz);
			}
		}
	}

	private float getDamageFor(EntityLivingBase el) {
		int dd = (int)ReikaMathLibrary.py3d(el.posX-posX, el.posY-posY, el.posZ-posZ);
		if (dd <= 9)
			return Integer.MAX_VALUE;
		else
			return 20-2*(dd-9);
	}

	private boolean canDestroy(World world, int x, int y, int z, Block id, int meta) {
		if (id == Blocks.bedrock)
			return false;
		if (id == FactorizationHandler.getInstance().bedrockID)
			return false;
		if (id instanceof SemiUnbreakable) {
			return !((SemiUnbreakable)id).isUnbreakable(world, x, y, z, meta);
		}
		return true;
	}

	private ItemStack getDroppedBlock(Block id, int meta) {
		Block dropid = id;//Blocks.blocksList[id].getItemDropped(meta, rand, 0);
		int dropmeta = meta;//Blocks.blocksList[id].damageDropped(meta);
		if (id == Blocks.grass)
			return new ItemStack(Blocks.dirt);
		return new ItemStack(dropid, 1, dropmeta);
	}

	private boolean canEntitize(World world, int x, int y, int z, Block id, int meta) {
		if (!this.canDestroy(world, x, y, z, id, meta))
			return false;
		if (y <= 0)
			return false;
		if (id == Blocks.air)
			return false;
		if (id == Blocks.bedrock)
			return false;
		if (id.hasTileEntity(meta))
			return false;
		if (ReikaWorldHelper.softBlocks(world, x, y, z))
			return false;
		if (id.getRenderType() != 0) //To prevent weird looking flying sand entities
			return false;
		return true;
	}

}
