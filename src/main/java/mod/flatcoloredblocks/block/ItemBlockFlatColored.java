package mod.flatcoloredblocks.block;

import mod.flatcoloredblocks.FlatColoredBlocks;
import mod.flatcoloredblocks.ModUtil;
import mod.flatcoloredblocks.RegistryItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Set;

public class ItemBlockFlatColored extends BlockItem implements RegistryItem
{

	public BlockFlatColored getColoredBlock()
	{
		return (BlockFlatColored) getBlock();
	}

	private String getColorPrefix(
			final Set<EnumFlatColorAttributes> which )
	{
		if ( which.contains( EnumFlatColorAttributes.dark ) )
		{
			return "flatcoloredblocks.dark";
		}

		if ( which.contains( EnumFlatColorAttributes.light ) )
		{
			return "flatcoloredblocks.light";
		}

		return "flatcoloredblocks.";
	}

	private String getColorHueName(
			final Set<EnumFlatColorAttributes> characteristics )
	{
		for ( final EnumFlatColorAttributes c : characteristics )
		{
			if ( !c.isModifier )
			{
				return c.name();
			}
		}

		return EnumFlatColorAttributes.black.name();
	}

	public ItemBlockFlatColored(
			final Block block )
	{
		super( block, ( new Item.Properties() ).tab( FlatColoredBlocks.instance.creativeTab ) );
		setRegistryName( ((RegistryItem) block).getRegistryName() );
	}

	@Override
	public Component getName(
			ItemStack stack )
	{
		final BlockState state = ModUtil.getFlatColoredBlockState( getColoredBlock(), stack );
		final int shadeNum = getColoredBlock().getShadeNumber( state );

		final Set<EnumFlatColorAttributes> colorChars = getColoredBlock().getFlatColorAttributes( state );

		final String type = getTypeLocalization();
		final String prefix = getColorPrefix( colorChars );
		final String hue = getColorHueName( colorChars );

		return Component.literal( type + ModUtil.translateToLocal( prefix + hue + ".name" ) + " " + ModUtil.translateToLocal( "flatcoloredblocks.Shade.name" ) + shadeNum );
	}

	private String getTypeLocalization()
	{
		switch ( getColoredBlock().getType() )
		{
			case GLOWING:
				return ModUtil.translateToLocal( "flatcoloredblocks.Glowing.name" ) + " ";
			case TRANSPARENT:
				return ModUtil.translateToLocal( "flatcoloredblocks.Transparent.name" ) + " ";
			default:
				return "";
		}
	}

	@Override
	public void appendHoverText(
			ItemStack stack,
			Level worldIn,
			List<Component> tooltip,
			TooltipFlag flagIn )
	{
		final BlockState state = ModUtil.getFlatColoredBlockState( getColoredBlock(), stack );
		final BlockFlatColored blk = getColoredBlock();

		final int hsv = blk.hsvFromState( state );
		final int rgb = ConversionHSV2RGB.toRGB( hsv );

		if ( FlatColoredBlocks.instance.config.showRGB )
		{
			addColor( ColorFormat.RGB, rgb, tooltip );
		}

		if ( FlatColoredBlocks.instance.config.showHEX )
		{
			addColor( ColorFormat.HEX, rgb, tooltip );
		}

		if ( FlatColoredBlocks.instance.config.showHSV )
		{
			addColor( ColorFormat.HSV, hsv, tooltip );
		}

		if ( FlatColoredBlocks.instance.config.showLight && blk.lightValue > 0 )
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.lightvalue" ) ).append( ' ' );
			sb.append( blk.lightValue ).append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.lightValueUnit" ) );
			tooltip.add( Component.literal( sb.toString() ) );
		}

		if ( FlatColoredBlocks.instance.config.showOpacity && blk.opacity < 100 )
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.opacity" ) ).append( ' ' );
			sb.append( blk.opacity ).append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.percent" ) );
			tooltip.add( Component.literal( sb.toString() ) );
		}

		super.appendHoverText( stack, worldIn, tooltip, flagIn );
	}

	public static enum ColorFormat
	{
		HEX, RGB, HSV
	};

	private void addColor(
			final ColorFormat Format,
			final int value,
			final List<Component> tooltip )
	{
		final int r_h = value >> 16 & 0xff;
		final int g_s = value >> 8 & 0xff;
		final int b_v = value & 0xff;

		final StringBuilder sb = new StringBuilder();

		if ( Format == ColorFormat.HEX )
		{
			sb.append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.hex" ) ).append( ' ' );
			sb.append( "#" ).append( hexPad( Integer.toString( r_h, 16 ) ) ).append( hexPad( Integer.toString( g_s, 16 ) ) ).append( hexPad( Integer.toString( b_v, 16 ) ) );
		}
		else if ( Format == ColorFormat.RGB )
		{
			sb.append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.rgb" ) ).append( ' ' );
			sb.append( ChatFormatting.RED ).append( r_h ).append( ' ' );
			sb.append( ChatFormatting.GREEN ).append( g_s ).append( ' ' );
			sb.append( ChatFormatting.BLUE ).append( b_v );
		}
		else
		{
			sb.append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.hsv" ) ).append( ' ' );
			sb.append( 360 * r_h / 255 ).append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.deg" ) + ' ' );
			sb.append( 100 * g_s / 255 ).append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.percent" ) + ' ' );
			sb.append( 100 * b_v / 255 ).append( ModUtil.translateToLocal( "flatcoloredblocks.tooltips.percent" ) );
		}

		tooltip.add( Component.literal( sb.toString() ) );
	}

	public static String hexPad(
			String string )
	{
		if ( string.length() == 0 )
			return "00";
		if ( string.length() == 1 )
			return "0" + string;
		return string;
	}

	public int getColorFromItemStack(
			final ItemStack stack,
			final int renderPass )
	{
		final BlockState state = ModUtil.getFlatColoredBlockState( getColoredBlock(), stack );
		return getColoredBlock().colorFromState( state );
	}

}
