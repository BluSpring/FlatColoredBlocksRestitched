package mod.flatcoloredblocks.duck;

import mod.flatcoloredblocks.datafixer.chiselsandbits.ChiselsAndBitsData;

import java.util.List;

public interface ExtendedChunkPalettedStorageFixSection {
    List<ChiselsAndBitsData> getChiselsAndBitsData();
    void setChiselsAndBitsData(List<ChiselsAndBitsData> chiselsAndBitsDataList);
}
