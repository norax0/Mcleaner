package norax.dev.Mcleaner.anvil;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.InflaterInputStream;

public class Parser {
    static final int SECTOR_SIZE = 4096;
    static final int HEADER_SECTORS = 2;

    public static List<Chunk> processRegionFile(File file) throws IOException {
        List<Chunk> chunks = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int[] header = new int[1024];
            for (int i = 0; i < 1024; i++) {
                header[i] = raf.readInt();
            }
            int[] timestamps = new int[1024];
            for (int i = 0; i < 1024; i++) {
                timestamps[i] = raf.readInt();
            }
            for (int i = 0; i < 1024; i++) {
                if (header[i] == 0) continue;
                int chunkX = i % 32;
                int chunkZ = i / 32;
                int offset = header[i] >> 8;
                long pos = offset * (long) SECTOR_SIZE;
                raf.seek(pos);
                int length = raf.readInt();
                int totalBlockLength = 4 + length;
                byte[] data = new byte[totalBlockLength];
                raf.seek(pos);
                raf.readFully(data);
                long inhabitedTime = -1;
                try {
                    int compression = data[4] & 0xFF;
                    if (compression == 2) {
                        inhabitedTime = getInhabitedTime(data);
                    }
                } catch (Exception e) {
                    inhabitedTime = -1;
                }
                Chunk chunk = new Chunk(chunkX, chunkZ, i, inhabitedTime, data, timestamps[i]);
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public static void saveRegionFile(File file, List<Chunk> chunks) throws IOException {
        int[] newHeader = new int[1024];
        int[] newTimestamps = new int[1024];
        File tempFile = File.createTempFile("region", ".mca", file.getParentFile());
        try (RandomAccessFile out = new RandomAccessFile(tempFile, "rw")) {
            byte[] headerPlaceholder = new byte[HEADER_SECTORS * SECTOR_SIZE];
            out.write(headerPlaceholder);
            int nextFreeSector = HEADER_SECTORS;
            Chunk[] chunkArray = new Chunk[1024];
            for (Chunk c : chunks) {
                chunkArray[c.getHeaderIndex()] = c;
            }
            for (int i = 0; i < 1024; i++) {
                Chunk c = chunkArray[i];
                if(c == null || c.isDeleted()) {
                    newHeader[i] = 0;
                    newTimestamps[i] = 0;
                    continue;
                }
                byte[] data = c.getData();
                int totalBlockLength = data.length;
                int sectorsNeeded = (totalBlockLength + SECTOR_SIZE - 1) / SECTOR_SIZE;
                newHeader[i] = (nextFreeSector << 8) | sectorsNeeded;
                newTimestamps[i] = c.getTimestamp();
                out.seek((long) nextFreeSector * SECTOR_SIZE);
                out.write(data);
                int padding = sectorsNeeded * SECTOR_SIZE - totalBlockLength;
                if (padding > 0) {
                    out.write(new byte[padding]);
                }
                nextFreeSector += sectorsNeeded;
            }
            out.seek(0);
            for (int i = 0; i < 1024; i++) {
                out.writeInt(newHeader[i]);
            }
            for (int i = 0; i < 1024; i++) {
                out.writeInt(newTimestamps[i]);
            }
        }
        Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public static long getInhabitedTime(byte[] data) throws IOException {
        int length = ((data[0] & 0xFF) << 24) | ((data[1] & 0xFF) << 16)
                | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        int compression = data[4] & 0xFF;
        if (compression != 2) {
            throw new IOException("Unsupported compression type");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data, 5, length - 1);
        InflaterInputStream iis = new InflaterInputStream(bais);
        DataInputStream dis = new DataInputStream(new BufferedInputStream(iis));
        return readInhabitedTime(dis);
    }

    public static long readInhabitedTime(DataInputStream dis) throws IOException {
        byte rootType = dis.readByte();
        if (rootType != 10)
            throw new IOException("root is not compound");
        dis.readUTF();
        return findInhabitedTime(dis);
    }

    private static long findInhabitedTime(DataInputStream dis) throws IOException {
        while (true) {
            byte tagType = dis.readByte();
            if (tagType == 0)
                break;
            String name = dis.readUTF();
            if ((tagType == 3 || tagType == 4) && "InhabitedTime".equals(name)) {
                if (tagType == 3)
                    return dis.readInt();
                else
                    return dis.readLong();
            }
            if (tagType == 10) {
                long value = findInhabitedTime(dis);
                if (value != -1)
                    return value;
            } else if (tagType == 9) {
                byte listType = dis.readByte();
                int len = dis.readInt();
                for (int i = 0; i < len; i++) {
                    if (listType == 10) {
                        long value = findInhabitedTime(dis);
                        if (value != -1)
                            return value;
                    } else {
                        skip(dis, listType);
                    }
                }
            } else {
                skip(dis, tagType);
            }
        }
        return -1;
    }

    private static void skip(DataInputStream dis, byte tagType) throws IOException {
        switch (tagType) {
            case 1: dis.readByte(); break;
            case 2: dis.readShort(); break;
            case 3: dis.readInt(); break;
            case 4: dis.readLong(); break;
            case 5: dis.readFloat(); break;
            case 6: dis.readDouble(); break;
            case 7: int arrayLength = dis.readInt(); dis.skipBytes(arrayLength); break;
            case 8: dis.readUTF(); break;
            case 9:
                byte listType = dis.readByte();
                int len = dis.readInt();
                for (int i = 0; i < len; i++) {
                    skip(dis, listType);
                }
                break;
            case 10:
                while (true) {
                    byte t = dis.readByte();
                    if(t == 0) break;
                    dis.readUTF();
                    skip(dis, t);
                }
                break;
            case 11: int intArrLength = dis.readInt(); dis.skipBytes(intArrLength * 4); break;
            case 12: int longArrLength = dis.readInt(); dis.skipBytes(longArrLength * 8); break;
            default: throw new IOException("Unknown tag type: " + tagType);
        }
    }
}