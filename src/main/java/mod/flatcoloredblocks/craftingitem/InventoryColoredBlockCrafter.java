package mod.flatcoloredblocks.craftingitem;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.ModUtil;
import mod.flatcoloredblocks.RegistryHelper;
import mod.flatcoloredblocks.block.BlockFlatColored;
import mod.flatcoloredblocks.block.EnumFlatBlockType;
import mod.flatcoloredblocks.block.EnumFlatColorAttributes;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Generates and Crafts items that are seen in the crafting item's gui.
 */
public class InventoryColoredBlockCrafter implements Container
{

	private final Player thePlayer;
	private final ContainerColoredBlockCrafter craftingContainer;

	private final ArrayList<ItemStack> options = new ArrayList<>();
	public int offset = 0;

	public InventoryColoredBlockCrafter(
			final Player thePlayer,
			final ContainerColoredBlockCrafter coloredCrafterContainer )
	{
		this.thePlayer = thePlayer;
		craftingContainer = coloredCrafterContainer;
	}

	public static HashMap<Object, Collection<Item>> getDyeList()
	{
		final HashMap<Object, Collection<Item>> dyeList = new HashMap<>();

		tagIntoList( dyeList, DyeColor.BLACK, getTagKey("black_dyes"));
		tagIntoList( dyeList, DyeColor.RED, getTagKey("red_dyes") );
		tagIntoList( dyeList, DyeColor.GREEN, getTagKey("green_dyes") );
		tagIntoList( dyeList, DyeColor.BROWN, getTagKey("brown_dyes") );
		tagIntoList( dyeList, DyeColor.BLUE, getTagKey("blue_dyes") );
		tagIntoList( dyeList, DyeColor.PURPLE, getTagKey("purple_dyes") );
		tagIntoList( dyeList, DyeColor.CYAN, getTagKey("cyan_dyes") );
		tagIntoList( dyeList, DyeColor.LIGHT_GRAY, getTagKey("light_gray_dyes") );
		tagIntoList( dyeList, DyeColor.GRAY, getTagKey("gray_dyes") );
		tagIntoList( dyeList, DyeColor.PINK, getTagKey("pink_dyes") );
		tagIntoList( dyeList, DyeColor.LIME, getTagKey("lime_dyes") );
		tagIntoList( dyeList, DyeColor.YELLOW, getTagKey("yellow_dyes") );
		tagIntoList( dyeList, DyeColor.LIGHT_BLUE, getTagKey("light_blue_dyes") );
		tagIntoList( dyeList, DyeColor.MAGENTA, getTagKey("magenta_dyes") );
		tagIntoList( dyeList, DyeColor.ORANGE, getTagKey("orange_dyes") );
		tagIntoList( dyeList, DyeColor.WHITE, getTagKey("white_dyes") );
		tagIntoList( dyeList, EnumFlatBlockType.NORMAL, getItems( FlatColoredBlocks.instance.config.solidCraftingBlock ) );
		tagIntoList( dyeList, EnumFlatBlockType.GLOWING, getItems( FlatColoredBlocks.instance.config.glowingCraftingBlock ) );
		tagIntoList( dyeList, EnumFlatBlockType.TRANSPARENT, getItems( FlatColoredBlocks.instance.config.transparentCraftingBlock ) );

		return dyeList;
	}

	private static TagKey<Item> getTagKey(String name) {
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("c", name));
	}

	private static void tagIntoList(
			HashMap<Object, Collection<Item>> dyeList,
			Enum<?> e,
			TagKey<Item> itemList )
	{
		dyeList.put( e, RegistryHelper.getItemsFromTag(itemList));
	}

	private InventorySummary scanPlayerInventory()
	{
		final EnumSet<DyeColor> dyes = EnumSet.noneOf( DyeColor.class );
		final Inventory ip = thePlayer.getInventory();

		final HashMap<Object, Collection<Item>> dyeList = getDyeList();
		final HashMap<Object, HashSet<ItemCraftingSource>> stacks = new HashMap<Object, HashSet<ItemCraftingSource>>();

		boolean hasCobblestone = false;
		boolean hasGlowstone = false;
		boolean hasGlass = false;

		for ( final Entry<Object, Collection<Item>> items : dyeList.entrySet() )
		{
			stacks.put( items.getKey(), new HashSet<ItemCraftingSource>() );
		}

		stacks.put( null, new HashSet<ItemCraftingSource>() );

		for ( int x = 0; x < ip.getContainerSize(); ++x )
		{
			final ItemStack is = ip.getItem( x );

			if ( is != null )
			{
				for ( final Entry<Object, Collection<Item>> items : dyeList.entrySet() )
				{
					for ( final Item ore : items.getValue() )
					{
						if ( is.getItem() == ore )
						{

							if ( items.getKey() instanceof DyeColor )
							{
								dyes.add( (DyeColor) items.getKey() );
							}
							else
							{
								if ( items.getKey() == EnumFlatBlockType.NORMAL )
								{
									hasCobblestone = true;
								}

								if ( items.getKey() == EnumFlatBlockType.TRANSPARENT )
								{
									hasGlass = true;
								}

								if ( items.getKey() == EnumFlatBlockType.GLOWING )
								{
									hasGlowstone = true;
								}
							}

							stacks.get( items.getKey() ).add( new ItemCraftingSource( ip, x ) );
						}
					}
				}
			}
		}

		return new InventorySummary( hasCobblestone, hasGlowstone, hasGlass, stacks, dyes );
	}

	private static TagKey<Item> getItems(
			final String name )
	{
		return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation( name ) );
	}

	/**
	 * recalculate the entire container.
	 */
	public void updateContents() throws IOException {
		options.clear();
		BlockFlatColored.getAllShades( options );

		final InventorySummary da = scanPlayerInventory();
		final EnumSet<DyeColor> dyes = da.dyes;

		final Iterator<ItemStack> i = options.iterator();
		while ( i.hasNext() )
		{
			final ItemStack is = i.next();
			final Block blk = Block.byItem( is.getItem() );
			final BlockState state = ModUtil.getFlatColoredBlockState( (BlockFlatColored) blk, is );

			final Set<EnumFlatColorAttributes> charistics = ( (BlockFlatColored) blk ).getFlatColorAttributes( state );
			boolean isGood = true;

			for ( final EnumFlatColorAttributes cc : charistics )
			{
				if ( !dyes.contains( cc.primaryDye ) || !dyes.contains( cc.secondaryDye ) )
				{
					isGood = false;
				}
			}

			final DyeColor alternateDye = EnumFlatColorAttributes.getAlternateDye( charistics );
			if ( alternateDye != null && dyes.contains( alternateDye ) )
			{
				isGood = true;
			}

			if ( !isGood || !da.has( ( (BlockFlatColored) blk ).getCraftable() ) )
			{
				i.remove();
			}
		}

		craftingContainer.setScroll( craftingContainer.scrollPercent );
	}

	@Override
	public int getContainerSize()
	{
		return options.size();
	}

	@Override
	public ItemStack getItem(
			int index )
	{

		index += offset;
		if ( index < options.size() )
		{
			return options.get( index ).copy();
		}

		return ModUtil.getEmptyStack();
	}

	@Override
	public ItemStack removeItem(
			int index,
			final int count )
	{

		index += offset;
		if ( index < options.size() )
		{
			ItemStack out = options.get( index );

			if ( ModUtil.getStackSize( out ) <= 0 )
			{
				out = null;
			}

			return out;
		}

		return ModUtil.getEmptyStack();
	}

	public ItemStack craftItem(
			final ItemStack reqItem,
			final int count,
			final boolean simulate )
	{
		if ( ModUtil.isEmpty( reqItem ) )
		{
			return ModUtil.getEmptyStack();
		}

		int outAmount = 0;

		final InventorySummary da = scanPlayerInventory();
		final Block blk = Block.byItem( reqItem.getItem() );
		final BlockState state = ModUtil.getFlatColoredBlockState( (BlockFlatColored) blk, reqItem );

		final Set<EnumFlatColorAttributes> charistics = ( (BlockFlatColored) blk ).getFlatColorAttributes( state );
		final Object Craftable = ( (BlockFlatColored) blk ).getCraftable();
		final HashSet<DyeColor> requiredDyes = new HashSet<DyeColor>();

		final int craftAmount = Craftable instanceof EnumFlatBlockType ? ( (EnumFlatBlockType) Craftable ).getOutputCount() : 1;

		final DyeColor alternateDye = EnumFlatColorAttributes.getAlternateDye( charistics );
		final HashSet<DyeColor> alternateSet = new HashSet<DyeColor>();

		if ( alternateDye != null )
		{
			alternateSet.add( alternateDye );
		}

		for ( final EnumFlatColorAttributes cc : charistics )
		{
			requiredDyes.add( cc.primaryDye );
			requiredDyes.add( cc.secondaryDye );
		}

		for ( int x = 0; x < count && outAmount + craftAmount <= 64; ++x )
		{
			boolean isGood = true;

			final ItemCraftingSource isx = findItem( da.stacks.get( Craftable ), simulate );
			if ( isx == null )
			{
				isGood = false;
			}

			HashSet<DyeColor> usedSet = alternateSet;
			availableDyeTest:
			{

				// test to see if there is an alternate, and if there is, see if
				// the player has one...
				if ( alternateDye != null )
				{
					final ItemCraftingSource is = findItem( da.stacks.get( alternateDye ), simulate );
					if ( is != null )
					{
						break availableDyeTest;
					}
				}

				// no alternate, try standard set.
				usedSet = requiredDyes;
				for ( final DyeColor dye : requiredDyes )
				{
					final ItemCraftingSource is = findItem( da.stacks.get( dye ), simulate );
					if ( is == null )
					{
						isGood = false;
					}
				}
			}

			if ( isGood && isx != null )
			{
				for ( final DyeColor dye : usedSet )
				{
					final ItemCraftingSource is = findItem( da.stacks.get( dye ), simulate );

					is.consume( 1 );
				}

				isx.consume( 1 );
				outAmount += craftAmount;
			}
			else
			{
				break;
			}
		}

		try {
			updateContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if ( outAmount <= 0 )
		{
			return ModUtil.getEmptyStack();
		}

		final ItemStack out = reqItem.copy();
		ModUtil.setStackSize( out, outAmount );

		return out;
	}

	private ItemCraftingSource findItem(
			final HashSet<ItemCraftingSource> hashSet,
			final boolean simulate )
	{
		for ( final ItemCraftingSource src : hashSet )
		{
			src.simulate = simulate;
			final ItemStack is = src.getStack();
			if ( is != null && ModUtil.getStackSize( is ) > 0 )
			{
				return src;
			}
		}

		return null;
	}

	@Override
	public ItemStack removeItemNoUpdate(
			final int index )
	{
		return ModUtil.getEmptyStack();
	}

	@Override
	public void setItem(
			final int index,
			final ItemStack stack )
	{

	}

	@Override
	public int getMaxStackSize()
	{
		return 0;
	}

	@Override
	public void setChanged()
	{

	}

	@Override
	public boolean stillValid(
			final Player player )
	{
		return true;
	}

	@Override
	public void startOpen(
			final Player player )
	{

	}

	@Override
	public void stopOpen(
			final Player player )
	{

	}

	@Override
	public boolean canPlaceItem(
			final int index,
			final ItemStack stack )
	{
		return false;
	}

	@Override
	public void clearContent()
	{
		options.clear();
	}

	@Override
	public boolean isEmpty() // whatever this is...
	{
		for ( final ItemStack itemstack : options )
		{
			if ( !itemstack.isEmpty() )
			{
				return false;
			}
		}

		return true;
	}

}
