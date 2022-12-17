package mod.flatcoloredblocks.datafixer.chiselsandbits;

import net.minecraft.network.FriendlyByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.zip.InflaterInputStream;

// Mostly from https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/1d7309e06dec869d12e97e648133b57f6e91f067/src/main/java/mod/chiselsandbits/chiseledblock/serialization/BlobSerializer.java
// but modified to only use the reads
public class CB2BCConverter {
    public static CompactBitsFormat loadCBLegacy(FriendlyByteBuf byteBuf) {
        var version = byteBuf.readVarInt();

        CompactBitsFormat bitsFormat;

        if (version == 0) { // Compact format
            bitsFormat = loadCompact(byteBuf);
        } else {
            throw new RuntimeException("Unsupported version " + version + "!");
        }

        bitsFormat.loadBlocks(byteBuf);

        return bitsFormat;
    }

    public static ByteBuffer inflate(ByteBuffer buffer) {
        var inflater = new InflaterInputStream(new ByteArrayInputStream(buffer.array()));
        var inflatedBuffer = ByteBuffer.allocate(3145728);

        int usedBytes = 0;
        int rv = 0;

        do {
            usedBytes += rv;
            try {
                rv = inflater.read(inflatedBuffer.array(), usedBytes, inflatedBuffer.limit() - usedBytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } while (rv > 0);

        return inflatedBuffer;
    }

    public static CompactBitsFormat loadCompact(FriendlyByteBuf byteBuf) {
        var types = byteBuf.readVarInt();
        var palette = new int[types];

        for (int x = 0; x < types; x++) {
            palette[x] = byteBuf.readVarInt();
        }

        var bitsPerInt = Math.max(Integer.SIZE - Integer.numberOfLeadingZeros(types - 1), 1);

        return new CompactBitsFormat(types, palette, bitsPerInt);
    }

    // Direct copy of
    // https://github.com/ChiselsAndBits/Chisels-and-Bits/blob/1d7309e06dec869d12e97e648133b57f6e91f067/src/main/java/mod/chiselsandbits/chiseledblock/serialization/BitStream.java
    static class BitStream {
        int offset = 0;
        int bit = 0;
        int firstLiveInt = -1;
        int lastLiveInt = -1;

        int currentInt = 0;
        int intOffset = 0;

        IntBuffer output;
        ByteBuffer bytes;

        public BitStream()
        {
            bytes = ByteBuffer.allocate( 250 );
            output = bytes.asIntBuffer();
        }

        private BitStream(
                final int byteOffset,
                final ByteBuffer wrap )
        {
            intOffset = byteOffset / 4;
            bytes = wrap;
            output = bytes.asIntBuffer();
            currentInt = hasInt() ? output.get( 0 ) : 0;
        }

        public static BitStream valueOf(
                final int byteOffset,
                final ByteBuffer wrap )
        {
            return new BitStream(byteOffset, wrap);
        }

        public void reset()
        {
            offset = 0;
            bit = 0;
            firstLiveInt = -1;
            lastLiveInt = -1;
            output.put( 0, 0 );
            currentInt = 0;
            intOffset = 0;
        }

        public byte[] toByteArray()
        {
            final ByteArrayOutputStream o = new ByteArrayOutputStream();
            output.put( offset, currentInt );
            o.write( bytes.array(), 0, ( lastLiveInt + 1 ) * 4 );
            return o.toByteArray();
        }

        public boolean get()
        {
            final boolean result = ( currentInt & 1 << bit ) != 0;

            if ( ++bit >= 32 )
            {
                ++offset;
                bit = 0;
                currentInt = hasInt() ? output.get( offset - intOffset ) : 0;
            }

            return result;
        }

        private boolean hasInt()
        {
            return output.capacity() > offset - intOffset && offset - intOffset >= 0;
        }

        public void add(
                final boolean b )
        {
            if ( b )
            {
                currentInt = currentInt | 1 << bit;
                lastLiveInt = offset;

                if ( firstLiveInt == -1 )
                {
                    firstLiveInt = offset;
                }
            }

            if ( ++bit >= 32 )
            {
                output.put( offset, currentInt );
                ++offset;
                bit = 0;
                currentInt = 0;

                // reset?
                if ( output.capacity() <= offset )
                {
                    final ByteBuffer ibytes = ByteBuffer.allocate( bytes.limit() + 248 );
                    final IntBuffer ioutput = ibytes.asIntBuffer();

                    // copy...
                    System.arraycopy( bytes.array(), 0, ibytes.array(), 0, bytes.capacity() );

                    bytes = ibytes;
                    output = ioutput;
                }

                output.put( offset, 0 );
            }
        }

        public int byteOffset() {
            return Math.max( firstLiveInt * 4, 0 );
        }
    }

    public static class CompactBitsFormat {
        public int types;
        public int[] palette;
        public int bitsPerInt;

        public int[] blocks;

        public CompactBitsFormat(
                int types,
                int[] palette,
                int bitsPerInt
        ) {
            this.types = types;
            this.palette = palette;
            this.bitsPerInt = bitsPerInt;
        }

        public void loadBlocks(FriendlyByteBuf byteBuf) {
            var offset = byteBuf.readVarInt();
            var interestBytes = byteBuf.readVarInt();

            var bits = BitStream.valueOf(offset, ByteBuffer.wrap(byteBuf.array(), byteBuf.readerIndex(), interestBytes));

            var values = new int[16 * 16 * 16];

            for (var x = 0; x < (16 * 16 * 16); x++) {
                var index = 0;

                for (var y = (bitsPerInt - 1); y >= 0; --y) {
                    index |= bits.get() ? 1 << y : 0;
                }

                values[x] = palette[index];
            }

            this.blocks = values;
        }

        public int getLegacyIdFromStateId(int id) {
            return id & 4095;
        }

        public int getMetadataFromStateId(int id) {
            return id >> 12 & 15;
        }
    }
}
