package norax.dev.Mcleaner.anvil;

import norax.dev.Mcleaner.anvil.Types.InhabitedTime;

public class Chunk {
    private int x;
    private int z;
    private int headerIndex;
    private InhabitedTime inhabitedTime;
    private byte[] data;
    private int timestamp;
    private boolean deleted = false;

    public Chunk(int x, int z, int headerIndex, long inhabitedTime, byte[] data, int timestamp) {
        this.x = x;
        this.z = z;
        this.headerIndex = headerIndex;
        this.inhabitedTime = new InhabitedTime(inhabitedTime);
        this.data = data;
        this.timestamp = timestamp;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getHeaderIndex() {
        return headerIndex;
    }

    public InhabitedTime getInhabitedTime() {
        return inhabitedTime;
    }

    public byte[] getData() {
        return data;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void delete() {
        deleted = true;
    }
}