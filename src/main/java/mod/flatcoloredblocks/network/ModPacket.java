package mod.flatcoloredblocks.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;

/**
 * Base Packet to be implemented.
 */
@SuppressWarnings( "rawtypes" )
public abstract class ModPacket implements Packet
{

	ServerPlayer serverEntity = null;

	public void server(
			final ServerPlayer playerEntity ) throws IOException {
		throw new RuntimeException( getClass().getName() + " is not a server packet." );
	}

	public void client()
	{
		throw new RuntimeException( getClass().getName() + " is not a client packet." );
	}

	abstract public void getPayload(
			FriendlyByteBuf buffer );

	abstract public void readPayload(
			FriendlyByteBuf buffer );

	public ModPacket(
			final FriendlyByteBuf buf
	) throws IOException {
		read(buf);
	}

	public void read(
			final FriendlyByteBuf buf ) throws IOException
	{

	}

	@Override
	public void write(
			final FriendlyByteBuf buf ) {
		getPayload( buf );
	}

	@Override
	public void handle(
			final PacketListener handler )
	{
		if ( serverEntity == null )
		{
			client();
		}
		else
		{
			try {
				server( serverEntity );
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
