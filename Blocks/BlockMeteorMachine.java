/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2017
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import Reika.DragonAPI.ModList;

public class BlockMeteorMachine extends Block {

	private final IIcon[] topIcon = new IIcon[5];
	private final IIcon[] sideIcon = new IIcon[5];
	private final IIcon[] bottomIcon = new IIcon[5];

	public BlockMeteorMachine() {
		super(ModList.ROTARYCRAFT.isLoaded() ? Material.iron : Material.rock);
		this.setHardness(ModList.ROTARYCRAFT.isLoaded() ? 4 : 2.5F);
		this.setResistance(ModList.ROTARYCRAFT.isLoaded() ? 15 : 10);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta < 5;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		if (meta < 3)
			return new TileEntityMeteorGun();
		if (meta == 3)
			return new TileEntityMeteorRadar();
		if (meta == 4)
			return new TileEntityMeteorMagnet();
		return null;
	}

	@Override
	public int damageDropped(int dmg) {
		return dmg;
	}

	@Override
	public IIcon getIcon(int s, int meta) {
		return s == 0 ? bottomIcon[meta] : s == 1 ? topIcon[meta] : sideIcon[meta];
	}

	@Override
	public void registerBlockIcons(IIconRegister ico) {
		String s = ModList.ROTARYCRAFT.isLoaded() ? "_rc" : "";
		for (int i = 0; i < topIcon.length; i++) {
			topIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_top"+s);
			sideIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_side"+s);
			bottomIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_bottom"+s);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block oldid, int oldmeta) {
		TileEntityMeteorBase te = (TileEntityMeteorBase)world.getTileEntity(x, y, z);
		if (te != null) {
			te.breakBlock();
		}
		super.breakBlock(world, x, y, z, oldid, oldmeta);
	}

}
