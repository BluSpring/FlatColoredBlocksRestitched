package mod.flatcoloredblocks;

import com.google.common.base.Stopwatch;
import mod.flatcoloredblocks.block.BlockFlatColored;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.TimeUnit;

public class CreativeTab extends CreativeModeTab
{

	public CreativeTab()
	{
		super(6, "FlatColoredBlocks" );
	}

	private int listOffset = 0;
	private Stopwatch offsetTimer;
	private NonNullList<ItemStack> list;

	public ItemStack makeIcon()
	{
		if ( list == null )
		{
			initalizeList();
		}

		if ( list.size() == 0 )
		{
			return new ItemStack( Blocks.COBBLESTONE );
		}

		// check timer, and if 0.7 seconds has elapsed, step it forwards ( and
		// keep it under list.size )
		if ( offsetTimer.elapsed( TimeUnit.MILLISECONDS ) > 700 )
		{
			listOffset = ++listOffset % list.size();
			offsetTimer = Stopwatch.createStarted();
		}

		return list.get( listOffset );
	}

	// initialize cycling icon.
	private void initalizeList()
	{
		offsetTimer = Stopwatch.createStarted();
		list = NonNullList.create();
		fillItemList( list );

		for ( int x = 0; x < list.size(); ++x )
		{
			final ItemStack is = list.get( x );

			if ( !( is.getItem() instanceof BlockItem) )
			{
				list.remove( x );
				--x;
				continue;
			}

			final BlockItem ib = (BlockItem) is.getItem();
			final Block b = ib.getBlock();

			// Remove other peoples stuff list..
			if ( !( b instanceof BlockFlatColored ) )
			{
				list.remove( x );
				--x;
				continue;
			}

			final int out = ( (BlockFlatColored) b ).hsvFromState( ModUtil.getFlatColoredBlockState( ( (BlockFlatColored) b ), is ) );

			final int s = out >> 8 & 0xff;
			final int v = out & 0xff;

			if ( s < 200 || v < 140 || v > 170 )
			{
				list.remove( x );
				--x;
			}
		}

		if ( list.isEmpty() )
		{
			fillItemList( list );
		}
	}

}
