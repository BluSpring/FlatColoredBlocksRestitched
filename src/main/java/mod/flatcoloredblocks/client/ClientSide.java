package mod.flatcoloredblocks.client;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.ModUtil;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.BlockFlatColoredTranslucent;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.craftingitem.GuiColoredBlockCrafter;
import mod.flatcoloredblocks.mixin.MinecraftAccessor;
import mod.flatcoloredblocks.resource.ResourceGenerator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ClientSide implements ClientModInitializer
{

	public static final ClientSide instance = new ClientSide();

	public ResourceGenerator resourceGenerator = new ResourceGenerator();

	public void preinit()
	{
		resourceGenerator.init();

		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		if ( manager instanceof ReloadableResourceManager)
		{
			( (ReloadableResourceManager) manager ).registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
				if ( FlatColoredBlocks.instance.itemColoredBlockCrafting != null )
				{
					FlatColoredBlocks.instance.itemColoredBlockCrafting.scrollIndex = -1;
				}
			});
		}
	}

	@Override
	public void onInitializeClient()
	{
		preinit();
		//ModLoadingContext.get().registerExtensionPoint( ExtensionPoint.GUIFACTORY, new ModGuiRouter() );

		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			clientItems();
			clientBlocks();
		});
	}

	public void clientItems()
	{
		Block[] flatColoredBlocks = BlockFlatColored.getAllBlocks().toArray( new Block[BlockFlatColored.getAllBlocks().size()] );
		ItemColors colors = ((MinecraftAccessor) Minecraft.getInstance()).getItemColors();

		colors.register( new ItemColor() {

			@Override
			public int getColor(
					final ItemStack stack,
					final int tintIndex )
			{
				final Block blk = Block.byItem( stack.getItem() );
				return ( (BlockFlatColored) blk ).colorFromState( ModUtil.getFlatColoredBlockState( ( (BlockFlatColored) blk ), stack ) );
			}
		}, flatColoredBlocks );
	}

	public void clientBlocks()
	{
		Block[] flatColoredBlocks = BlockFlatColored.getAllBlocks().toArray( new Block[BlockFlatColored.getAllBlocks().size()] );
		BlockColors colors = Minecraft.getInstance().getBlockColors();

		colors.register((state, p_186720_2_, pos, tintIndex) -> ( (BlockFlatColored) state.getBlock() ).colorFromState( state ), flatColoredBlocks );

		for (Block block : flatColoredBlocks) {
			if (block instanceof BlockFlatColoredTranslucent) {
				BlockRenderLayerMap.INSTANCE.putBlock(block, RenderType.translucent());
			}
		}
	}

	public ResourceLocation getTextureName(
			final EnumFlatBlockType type,
			final int varient )
	{
		return new ResourceLocation( FlatColoredBlocks.MODID, getBaseTextureName( type ) + "_" + varient );
	}

	public String getBaseTextureName(
			final EnumFlatBlockType type )
	{
		return "flatcoloredblock" + getTextureFor( type );
	}

	public String getBaseTextureNameWithBlocks(
			final EnumFlatBlockType type )
	{
		return "blocks/flatcoloredblock" + getTextureFor( type );
	}

	public ResourceLocation getTextureResourceLocation(
			final EnumFlatBlockType type )
	{
		return new ResourceLocation( FlatColoredBlocks.MODID, "blocks/flatcoloredblock_" + getTextureFileFor( type ) );
	}

	private String getTextureFileFor(
			final EnumFlatBlockType type )
	{
		switch ( type )
		{
			case GLOWING:
				return FlatColoredBlocks.instance.config.DISPLAY_TEXTURE_GLOWING.resourceName();
			case TRANSPARENT:
				return FlatColoredBlocks.instance.config.DISPLAY_TEXTURE_TRANSPARENT.resourceName();
			default:
				return FlatColoredBlocks.instance.config.DISPLAY_TEXTURE.resourceName();
		}
	}

	private String getTextureFor(
			final EnumFlatBlockType type )
	{
		switch ( type )
		{
			case GLOWING:
				return "_glowing";
			case TRANSPARENT:
				return "_transparent";
			default:
				return "";
		}
	}

}
