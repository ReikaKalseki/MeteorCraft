/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import Reika.DragonAPI.ModList;

public class BlockMeteorMachine extends Block {

	private final Icon[] topIcon = new Icon[4];
	private final Icon[] sideIcon = new Icon[4];
	private final Icon[] bottomIcon = new Icon[4];

	public BlockMeteorMachine(int par1) {
		super(par1, ModList.ROTARYCRAFT.isLoaded() ? Material.iron : Material.rock);
		this.setHardness(ModList.ROTARYCRAFT.isLoaded() ? 4 : 2.5F);
		this.setResistance(ModList.ROTARYCRAFT.isLoaded() ? 15 : 10);
		this.setCreativeTab(CreativeTabs.tabMisc);
	}

	@Override
	public boolean hasTileEntity(int meta) {
		return meta < 4;
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		if (meta < 3)
			return new TileEntityMeteorGun();
		if (meta == 3)
			return new TileEntityMeteorRadar();
		return null;
	}

	@Override
	public int damageDropped(int dmg) {
		return dmg;
	}

	@Override
	public Icon getIcon(int s, int meta) {
		return s == 0 ? bottomIcon[meta] : s == 1 ? topIcon[meta] : sideIcon[meta];
	}

	@Override
	public void registerIcons(IconRegister ico) {
		String s = ModList.ROTARYCRAFT.isLoaded() ? "_rc" : "";
		for (int i = 0; i < topIcon.length; i++) {
			topIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_top"+s);
			sideIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_side"+s);
			bottomIcon[i] = ico.registerIcon("meteorcraft:machine_"+i+"_bottom"+s);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int oldid, int oldmeta) {
		TileEntityMeteorBase te = (TileEntityMeteorBase)world.getBlockTileEntity(x, y, z);
		if (te != null) {
			te.destroy();
		}
		super.breakBlock(world, x, y, z, oldid, oldmeta);
	}

}
