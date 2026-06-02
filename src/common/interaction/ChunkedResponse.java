package common.interaction;

import java.io.Serializable;

public class ChunkedResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int totalChunks;
    private final int chunkIndex;
    private final byte[] data;

    public ChunkedResponse(int totalChunks, int chunkIndex, byte[] data) {
        this.totalChunks = totalChunks;
        this.chunkIndex = chunkIndex;
        this.data = data;
    }

    public int getTotalChunks() { return totalChunks; }
    public int getChunkIndex() { return chunkIndex; }
    public byte[] getData() { return data; }
}