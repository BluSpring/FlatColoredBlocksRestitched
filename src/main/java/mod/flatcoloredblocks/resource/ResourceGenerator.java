
package mod.flatcoloredblocks.resource;

import com.mojang.blaze3d.platform.NativeImage;
import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.block.BlockHSVConfiguration;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.client.ClientSide;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.models.JModel;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ResourceGenerator
{
	public final RuntimeResourcePack resourcePack = RuntimeResourcePack.create(FlatColoredBlocks.MODID + ":" + "internal_generator");

	public void init()
	{
		// ARRP is a godsend
		RRPCallback.AFTER_VANILLA.register(a -> {
			populateResources();
			a.add(resourcePack);
		});
	}

	public void populateResources()
	{
		registerConfiguredResources( EnumFlatBlockType.NORMAL, FlatColoredBlocks.instance.normal );
		registerConfiguredResources( EnumFlatBlockType.GLOWING, FlatColoredBlocks.instance.glowing );
		registerConfiguredResources( EnumFlatBlockType.TRANSPARENT, FlatColoredBlocks.instance.transparent );
	}

	private void registerConfiguredResources(
			EnumFlatBlockType type,
			BlockHSVConfiguration config )
	{
		for ( int varient = 0; varient < config.shadeConvertVariant.length; varient++ )
		{
			String name = config.getBlockName( varient );

			JState blockState = JState.state(JState.multipart(JState.model("flatcoloredblocks:block/" + name)));

			resourcePack.addBlockState(blockState, new ResourceLocation(FlatColoredBlocks.MODID, name));

			if ( type == EnumFlatBlockType.TRANSPARENT )
			{
				final ResourceLocation sourceLoc = ClientSide.instance.getTextureResourceLocation( EnumFlatBlockType.TRANSPARENT );
				final ResourceLocation textureName = ClientSide.instance.getTextureName( EnumFlatBlockType.TRANSPARENT, varient );

				try
				{
					final URL iresource = ResourceGenerator.class.getResource("/assets/" + sourceLoc.getNamespace() + "/" + sourceLoc.getPath());
					final NativeImage bi = NativeImage.read( iresource.openStream() );

					final BufferedImage image = new BufferedImage( bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_4BYTE_ABGR );
					final int xx = bi.getWidth();
					final int yy = bi.getHeight();

					float alphaMultiplier = config.shadeConvertVariant[varient] / 255.0f;
					for ( int x = 0; x < xx; ++x )
					{
						for ( int y = 0; y < yy; ++y )
						{
							final int color = bi.getPixelRGBA( x, y );
							final int a = (int) ( ( color >> 24 & 0xff ) * alphaMultiplier );
							image.setRGB( x, y, color & 0xffffff | a << 24 );
						}
					}

					resourcePack.addTexture(new ResourceLocation(FlatColoredBlocks.MODID, "blocks/" + textureName.getPath()), image);
				}
				catch ( IOException e )
				{
					// fails the first time it runs.
				}

				 var model = JModel
						 .model("flatcoloredblocks:block/flatcoloredblock_" + config.textureStyle)
						 .textures(
								 JModel.textures()
										 .var("all", "flatcoloredblocks:blocks/" + textureName.getPath())
										 .particle("flatcoloredblocks:blocks/" + textureName.getPath())
						 );

				resourcePack.addModel(model, new ResourceLocation(FlatColoredBlocks.MODID, "block/" + name));
				resourcePack.addModel(model, new ResourceLocation(FlatColoredBlocks.MODID, "item/" + name));
			}
			else
			{
				var model = JModel
						.model("flatcoloredblocks:block/flatcoloredblock_" + config.textureStyle);

				resourcePack.addModel(model, new ResourceLocation(FlatColoredBlocks.MODID, "block/" + name));
				resourcePack.addModel(model, new ResourceLocation(FlatColoredBlocks.MODID, "item/" + name));
			}
		}
	}

}
