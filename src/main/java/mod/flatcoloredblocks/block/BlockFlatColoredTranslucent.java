package mod.flatcoloredblocks.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockFlatColoredTranslucent extends BlockFlatColored
{
	public BlockFlatColoredTranslucent(
			BlockHSVConfiguration type,
			final int i,
			final int j,
			final int varientNum )
	{
		super( type, i, j, varientNum );

		// Its still a full block.. even if its not a opaque cube
		// C&B requires this.
		// fullBlock = true; -- cannot set this.
	}

	@Override
	@Environment( EnvType.CLIENT )
	public boolean skipRendering(
			BlockState state,
			BlockState adjacentBlockState,
			Direction side )
	{
		return adjacentBlockState.is(this) || super.skipRendering(state, adjacentBlockState, side);
	}

	/*@Override
	public boolean func_149686_d(
			final BlockState state )
	{
		return false;
	}

	@Override
	public boolean func_149721_r(
			BlockState state )
	{
		return false;
	}*/

	public float[] getBeaconColorMultiplier(BlockState state)
	{
		int o = ConversionHSV2RGB.toRGB( hsvFromState( state ) );
		return new float[] { byteToFloat( ( o >> 16 ) & 0xff ), byteToFloat( ( o >> 8 ) & 0xff ), byteToFloat( ( o ) & 0xff ) };
	}

	private float byteToFloat(
			int i )
	{
		return Math.max( 0.0f, Math.min( 1.0f, (float) i / 255.0f ) );
	}

	@Override
	public float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return 1.0F;
	}
}
