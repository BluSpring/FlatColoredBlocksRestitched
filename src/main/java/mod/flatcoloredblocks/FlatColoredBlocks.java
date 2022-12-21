package mod.flatcoloredblocks;

import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.BlockHSVConfiguration;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.ItemBlockFlatColored;
import mod.flatcoloredblocks.block.cnbconvert.LegacyChiseledBlock;
import mod.flatcoloredblocks.block.cnbconvert.LegacyChiseledBlockEntity;
import mod.flatcoloredblocks.config.ModConfig;
import mod.flatcoloredblocks.craftingitem.ContainerColoredBlockCrafter;
import mod.flatcoloredblocks.craftingitem.ItemColoredBlockCrafter;
import mod.flatcoloredblocks.datafixer.forge.LegacyForgeBlockParser;
import mod.flatcoloredblocks.network.NetworkRouter;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FlatColoredBlocks implements ModInitializer
{
	// create creative tab...
	public static FlatColoredBlocks instance;

	public static LegacyForgeBlockParser legacyForgeBlockParser;

	public static final String MODID = "flatcoloredblocks";

	public CreativeTab creativeTab;
	public ModConfig config;

	// @ObjectHolder( "flatcoloredblocks:coloredcraftingitem" )
	public ItemColoredBlockCrafter itemColoredBlockCrafting;

	public BlockHSVConfiguration normal;
	public BlockHSVConfiguration transparent;
	public BlockHSVConfiguration glowing;

	public MenuType<ContainerColoredBlockCrafter> containerType;

	public final RuntimeResourcePack resourcePack = RuntimeResourcePack.create(new ResourceLocation(MODID, "internal"));

	public FlatColoredBlocks()
	{
		instance = this;

		containerType = ScreenHandlerRegistry.registerSimple(new ResourceLocation(MODID, "colored_block_crafter"), (syncId, container) ->
				new ContainerColoredBlockCrafter(syncId, container.player, container.player.getLevel(), 0, 0, 0)
		);

		// configure creative tab.
		creativeTab = new CreativeTab();

		// configure networking and gui.
		NetworkRouter.instance = new NetworkRouter();

		config = new ModConfig( new File( FabricLoader.getInstance().getConfigDir().toFile(), MODID ) );
		initHSVFromConfiguration( config );
	}

	@Override
	public void onInitialize() {
		RegistryEvents.onBlocksRegistry(Registry.BLOCK);
		RegistryEvents.onItemsRegistry(Registry.ITEM);

		registerDyeTags();
		RRPCallback.BEFORE_VANILLA.register(a -> a.add(resourcePack));

		ServerWorldEvents.UNLOAD.register((ignored, ignored2) -> {
			legacyForgeBlockParser = null;
		});
	}

	private void registerDyeTags() {
		for (DyeColor dyeColor : DyeColor.values()) {
			resourcePack.addTag(new ResourceLocation("c", dyeColor.getName() + "_dyes"), JTag.tag().add(new ResourceLocation("minecraft", dyeColor.getName() + "_dye")));
		}
	}

	public int getFullNumberOfShades()
	{
		return normal.getNumberOfShades()
				+ transparent.getNumberOfShades() * FlatColoredBlocks.instance.config.TRANSPARENCY_SHADES
				+ glowing.getNumberOfShades() * FlatColoredBlocks.instance.config.GLOWING_SHADES;
	}

	public int getFullNumberOfBlocks()
	{
		return normal.getNumberOfShades()
				+ transparent.getNumberOfShades()
				+ glowing.getNumberOfShades();
	}

	public void initHSVFromConfiguration(
			final ModConfig config )
	{
		normal = new BlockHSVConfiguration( EnumFlatBlockType.NORMAL, config );
		transparent = new BlockHSVConfiguration( EnumFlatBlockType.TRANSPARENT, config );
		glowing = new BlockHSVConfiguration( EnumFlatBlockType.GLOWING, config );
	}

	public static Block LEGACY_CHISELED_BLOCK = null;
	public static BlockEntityType<LegacyChiseledBlockEntity> LEGACY_CHISELED_BLOCK_ENTITY = null;

	public static class RegistryEvents
	{
		public static void onBlocksRegistry(Registry<Block> registry)
		{
			Log.debug( "registering blocks : " + registry.key().registry().toString() );
			FlatColoredBlocks.instance.blocks(registry);

			if (FabricLoader.getInstance().isModLoaded("chiselsandbits")) {
				LEGACY_CHISELED_BLOCK = Registry.register(
						Registry.BLOCK,
						new ResourceLocation("flatcoloredblocks", "legacy_chiseled_block"),
						new LegacyChiseledBlock(FabricBlockSettings.copy(Blocks.STONE))
				);

				LEGACY_CHISELED_BLOCK_ENTITY = Registry.register(
						Registry.BLOCK_ENTITY_TYPE,
						new ResourceLocation("flatcoloredblocks", "legacy_chiseled_block_entity"),
						FabricBlockEntityTypeBuilder.create(LegacyChiseledBlockEntity::new, LEGACY_CHISELED_BLOCK).build()
				);
			}
		}

		public static void onItemsRegistry(Registry<Item> registry)
		{
			Log.debug( "registering items : " + registry.key().registry().toString() );
			FlatColoredBlocks.instance.items(registry);
		}

	}

	public void items(
			Registry<Item> registry )
	{
		//ItemColoredBlockCrafter icbc = FlatColoredBlocks.instance.itemColoredBlockCrafting = new ItemColoredBlockCrafter();

		//Registry.register(registry, icbc.getRegistryName(), icbc);

		for ( BlockItem ib : itemBlocks )
		{
			Registry.register(registry, ((RegistryItem) ib).getRegistryName(), ib);
		}
	}

	private static List<BlockItem> itemBlocks = new LinkedList<>();

	public void blocks(
			Registry<Block> registry )
	{
		final BlockHSVConfiguration configs[] = new BlockHSVConfiguration[] { FlatColoredBlocks.instance.normal, FlatColoredBlocks.instance.transparent, FlatColoredBlocks.instance.glowing };

		// any time we regenerate blocks we regenerate this.
		itemBlocks.clear();

		// create and configure all blocks.
		for ( final BlockHSVConfiguration hsvconfig : configs )
		{
			for ( int v = 0; v < hsvconfig.MAX_SHADE_VARIANT; ++v )
			{
				final BlockFlatColored cb = BlockFlatColored.construct( hsvconfig, v );
				Registry.register(registry, cb.getRegistryName(), cb );

				final ItemBlockFlatColored cbi = new ItemBlockFlatColored( cb );
				itemBlocks.add( cbi );
			}
		}
	}

}
