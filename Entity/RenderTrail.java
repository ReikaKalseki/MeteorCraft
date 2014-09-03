/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2014
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.MeteorCraft.Entity;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;

public class RenderTrail extends Render {

	private static final ResourceLocation texture = new ResourceLocation("meteorcraft", "textures/entity/smoke.png");

	public void renderEntity(EntityTrail er, double par2, double par4, double par6, float par8, float par9)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef((float)par2, (float)par4, (float)par6);
		Tessellator v5 = Tessellator.instance;
		float var16 = 1.0F;
		float var17 = 0.5F;
		float var18 = 0.25F;
		int var19 = er.getBrightnessForRender(par9);
		int var20 = var19 % 65536;
		int var21 = var19 / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var20 / 1.0F, var21 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float var26 = 255.0F;
		int var22 = (int)var26;
		GL11.glRotatef(180.0F - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		float size = 10;//-er.ticksExisted/(float)er.LIFE*10;
		//ReikaTextureHelper.bindFinalTexture(MeteorCraft.class, "/Reika/MeteorCraft/Textures/smoke.png");
		this.bindTexture(texture);
		GL11.glScaled(size, size, 1);
		GL11.glTranslated(-0.5, -0.5, 0);
		//GL11.glDepthFunc(GL11.GL_ALWAYS);
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.DEFAULT.apply();
		//GL11.glColor4f(1-er.ticksExisted/(float)er.LIFE, 1-er.ticksExisted/(float)er.LIFE, 1-er.ticksExisted/(float)er.LIFE, 1);
		GL11.glColor4f(1, 1, 1, 1-er.ticksExisted/(float)er.LIFE);
		v5.startDrawingQuads();
		v5.setNormal(0.0F, 1.0F, 0.0F);
		v5.addVertexWithUV(0, 0, 0, 0, 0);
		v5.addVertexWithUV(1, 0, 0, 1, 0);
		v5.addVertexWithUV(1, 1, 0, 1, 1);
		v5.addVertexWithUV(0, 1, 0, 0, 1);
		v5.draw();
		//ReikaJavaLibrary.pConsole(er.getRange());
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glTranslated(0.5, 0.5, 0);
		GL11.glScaled(1D/size, 1D/size, 1);
		//GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glPopMatrix();
	}

	@Override
	public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par10) {
		this.renderEntity((EntityTrail)entity, par2, par4, par6, par8, par10);
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

}
