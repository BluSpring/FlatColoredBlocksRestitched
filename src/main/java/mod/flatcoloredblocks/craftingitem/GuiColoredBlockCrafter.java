package mod.flatcoloredblocks.craftingitem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.flatcoloredblocks.FlatColoredBlocks;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * GuiContainer for crafting item's gui manages render, scroll bar and defers
 * scroll position to Container.
 */
public class GuiColoredBlockCrafter extends AbstractContainerScreen<ContainerColoredBlockCrafter>
{

	private static final ResourceLocation CRAFTER_GUI_TEXTURE = new ResourceLocation( FlatColoredBlocks.MODID, "textures/gui/container/coloredcrafting.png" );

	private final ContainerColoredBlockCrafter myContainer;
	private boolean isScrolling = false;
	private float currentScroll = 0;

	public GuiColoredBlockCrafter(
			final int id,
			final Player player,
			final Level world,
			final int x,
			final int y,
			final int z )
	{
		super( new ContainerColoredBlockCrafter( id, player, world, x, y, z ), player.getInventory(), Component.empty() );

		myContainer = menu;
		passEvents = false;
		imageHeight = 239;
		imageWidth = 195;
	}

	@Override
	protected void renderLabels(
			final PoseStack poseStack,
			final int mouseX,
			final int mouseY )
	{
		font.draw(poseStack, FlatColoredBlocks.instance.itemColoredBlockCrafting.getName( null ).getContents().toString(), 8F, 6F, 0x404040 );
		font.draw(poseStack, I18n.get( "container.inventory", new Object[0] ), 8, imageHeight - 93, 0x404040 );
	}

	@Override
	protected void renderBg(
			final PoseStack poseStack,
			final float partialTicks,
			final int mouseX,
			final int mouseY )
	{
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.setShaderTexture(0, CRAFTER_GUI_TEXTURE);

		final int xOffset = ( width - imageWidth ) / 2;
		final int yOffset = ( height - imageHeight ) / 2;

		this.blit( poseStack, xOffset, yOffset, 0, 0, imageWidth, imageHeight );

		final int scrollBarLeft = leftPos + 175;
		final int scrollBarTop = topPos + 18;
		final int scrollBarBottom = scrollBarTop + 112 + 14;

		final int scrollNobOffsetX = 232;
		final int scrollNobOffsetY = 0;

		RenderSystem.setShaderTexture(0, CRAFTER_GUI_TEXTURE);

		RenderSystem.setShaderColor( 1.0F, 1.0F, 1.0F, 1.0F );
		GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );

		final int rowsOfScrolling = Math.max( ( myContainer.getItemCount() + 8 ) / 9 - 7, 0 );
		if ( rowsOfScrolling <= 0 )
		{
			try {
				myContainer.setScroll( currentScroll = 0 );
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		/*( scrollBarLeft, scrollBarTop + (int) ( ( scrollBarBottom - scrollBarTop - 17 ) * currentScroll ), scrollNobOffsetX + ( rowsOfScrolling > 0 ? 0 : 12 ),
				scrollNobOffsetY, 12, 15 );*/

	}

	@Override
	public boolean mouseScrolled(
			double d, double e, double f )
	{
		if ( e != 0 )
		{
			final int rowsToScroll = myContainer.getItemCount() / 9 - 7;

			if ( e > 0 )
			{
				e = 1;
			}
			if ( e < 0 )
			{
				e = -1;
			}

			currentScroll = (float) ( currentScroll - (double) e / (double) rowsToScroll );
			currentScroll = Mth.clamp( currentScroll, 0.0F, 1.0F );
			try {
				myContainer.setScroll( currentScroll );
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			return true;
		}
		return false;
	}

	protected boolean insideScrollbar(
			double x,
			double y )
	{
		final int scrollBarLeft = leftPos + 175;
		final int scrollBarTop = topPos + 18;
		final int scrollBarRight = scrollBarLeft + 14;
		final int scrollBarBottom = scrollBarTop + 112 + 14;

		return x >= (double) scrollBarLeft && y >= (double) scrollBarTop && x < (double) scrollBarRight && y < (double) scrollBarBottom;
	}

	public boolean mouseClicked(
			double x,
			double y,
			int button )
	{
		if ( button == 0 && this.insideScrollbar( x, y ) )
		{
			this.isScrolling = true;
			return true;
		}

		return super.mouseClicked( x, y, button );
	}

	public boolean mouseReleased(
			double x,
			double y,
			int button )
	{
		if ( button == 0 )
		{
			this.isScrolling = false;
		}

		return super.mouseReleased( x, y, button );
	}

	public boolean mouseDragged(
			double x,
			double y,
			int button,
			double sx,
			double sy )
	{
		if ( this.isScrolling )
		{
			final int scrollBarTop = topPos + 18;
			final int scrollBarBottom = scrollBarTop + 112 + 14;
			currentScroll = (float) ( y - scrollBarTop - 7.5F ) / ( scrollBarBottom - scrollBarTop - 15.0F );
			currentScroll = Mth.clamp( currentScroll, 0.0F, 1.0F );
			try {
				myContainer.setScroll( this.currentScroll );
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		else
		{
			return super.mouseDragged( x, y, button, sx, sy );
		}
	}

	@Override
	public void render(
			PoseStack poseStack,
			int mouseX,
			int mouseY,
			float partialTicks )
	{
		this.init();
		super.render( poseStack, mouseX, mouseY, partialTicks );
		this.renderTooltip( poseStack, mouseX, mouseY );
	}

}
