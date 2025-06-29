package norax.dev.Mcleaner.anvil;

import norax.dev.Mcleaner.anvil.Types.InhabitedTime;
import norax.dev.Mcleaner.anvil.Types.Status;

public class Chunk {
    private int x;
    private int z;
    private int headerIndex;
    private InhabitedTime inhabitedTime;
    private Status status = Status.EMPTY;
    private byte[] data;
    private int timestamp;
    private boolean deleted = false;

    public Chunk(int x, int z, int headerIndex, long inhabitedTime, Status status, byte[] data, int timestamp) {
        this.x = x;
        this.z = z;
        this.headerIndex = headerIndex;
        this.inhabitedTime = new InhabitedTime(inhabitedTime);
        this.status = status;
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

    public Status getStatus() {
        return status;
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