package Reika.MeteorCraft;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import Reika.MeteorCraft.MeteorGenerator.MeteorType;
import Reika.MeteorCraft.Registry.MeteorOptions;
import cpw.mods.fml.common.IWorldGenerator;

public class OldMeteorGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		if (this.canGen(world)) {
			if (random.nextInt(200) == 0)
				this.generate(random, world, chunkX*16, chunkZ*16);
		}
	}

	private boolean canGen(World world) {
		if (!MeteorOptions.OLDGEN.getState())
			return false;
		int id = world.provider.dimensionId;
		if (id == -1 || id == 1 || world.provider.hasNoSky)
			return false;
		return true;
	}

	private void generate(Random rand, World world, int chunkX, int chunkZ) {
		int x = chunkX+rand.nextInt(16);
		int z = chunkZ+rand.nextInt(16);
		int y = this.getGenY(world, x, z);
		MeteorGenerator.instance.generate(world, x, y, z, this.getRandomType());
	}

	private MeteorType getRandomType() {
		return null;
	}

	private int getGenY(World world, int x, int z) {
		return 0;
	}

}
