/*
 * Copyright 2017 lolnet.co.nz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.co.lolnet.equity.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import nz.co.lolnet.equity.Equity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PacketUtil {
    
    public static ByteBuf getByteBuf(ByteBufAllocator byteBufAllocator, ByteBuf byteBuf, int length) {
        if (byteBuf.hasMemoryAddress()) {
            return byteBuf.readRetainedSlice(length);
        }
        
        return byteBufAllocator.directBuffer(length, length).writeBytes(byteBuf, length);
    }
    
    public static boolean safeRelease(ByteBuf byteBuf) {
        try {
            if (byteBuf.refCnt() > 0) {
                byteBuf.release(byteBuf.refCnt());
            }
            
            return true;
        } catch (RuntimeException ex) {
            Equity.getInstance().getLogger().error("Encountered an error processing PacketUtil::safeRelease", ex);
            return false;
        }
    }
    
    public static void writeString(ByteBuf byteBuf, String string) {
        if (string.length() > Short.MAX_VALUE) {
            throw new UnsupportedOperationException("Cannot send string longer than Short.MAX_VALUE (got " + string.length() + " characters)");
        }
        
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(byteBuf, bytes.length);
        byteBuf.writeBytes(bytes);
    }
    
    public static String readString(ByteBuf byteBuf) {
        int length = readVarInt(byteBuf);
        if (length > Short.MAX_VALUE) {
            throw new UnsupportedOperationException("Cannot receive string longer than Short.MAX_VALUE (got " + length + " characters)");
        }
        
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    public static void writeArray(ByteBuf byteBuf, byte[] bytes) {
        if (bytes.length > Short.MAX_VALUE) {
            throw new UnsupportedOperationException("Cannot send byte array longer than Short.MAX_VALUE (got " + bytes.length + " bytes)");
        }
        
        writeVarInt(byteBuf, bytes.length);
        byteBuf.writeBytes(bytes);
    }
    
    public static byte[] toArray(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
    
    public static byte[] readArray(ByteBuf byteBuf) {
        return readArray(byteBuf, byteBuf.readableBytes());
    }
    
    public static byte[] readArray(ByteBuf byteBuf, int limit) {
        int length = readVarInt(byteBuf);
        if (length > limit) {
            throw new UnsupportedOperationException("Cannot receive byte array longer than " + limit + " (got " + length + " bytes)");
        }
        
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }
    
    public static void writeStringArray(ByteBuf byteBuf, List<String> list) {
        writeVarInt(byteBuf, list.size());
        for (String string : list) {
            writeString(byteBuf, string);
        }
    }
    
    public static List<String> readStringArray(ByteBuf byteBuf) {
        int length = readVarInt(byteBuf);
        List<String> list = Toolbox.newArrayList();
        for (int index = 0; index < length; index++) {
            list.add(readString(byteBuf));
        }
        
        return list;
    }
    
    public static int getVarIntSize(ByteBuf byteBuf, int input) {
        for (int index = 1; index < 5; ++index) {
            if ((input & -1 << index * 7) == 0) {
                return index;
            }
        }
        
        return 5;
    }
    
    public static int readVarInt(ByteBuf byteBuf) {
        return readVarInt(byteBuf, 5);
    }
    
    public static int readVarInt(ByteBuf byteBuf, int maxBytes) {
        int result = 0;
        int bytesRead = 0;
        
        while (byteBuf.readableBytes() != 0) {
            byte read = byteBuf.readByte();
            result |= (read & 0x7F) << (bytesRead++ * 7);
            if (bytesRead > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }
            
            if ((read & 0x80) != 0x80) {
                break;
            }
        }
        
        return result;
    }
    
    public static void writeVarInt(ByteBuf byteBuf, int value) {
        while (byteBuf.isWritable()) {
            int part = value & 0x7F;
            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }
            
            byteBuf.writeByte(part);
            if (value == 0) {
                break;
            }
        }
    }
    
    public static int readVarShort(ByteBuf byteBuf) {
        int low = byteBuf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = byteBuf.readUnsignedByte();
        }
        
        return ((high & 0xFF) << 15) | low;
    }
    
    public static void writeVarShort(ByteBuf byteBuf, int value) {
        int low = value & 0x7FFF;
        int high = (value & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        
        byteBuf.writeShort(low);
        if (high != 0) {
            byteBuf.writeByte(high);
        }
    }
    
    public static UUID readUUID(ByteBuf byteBuf) {
        return new UUID(byteBuf.readLong(), byteBuf.readLong());
    }
    
    public static void writeUUID(ByteBuf byteBuf, UUID uuid) {
        byteBuf.writeLong(uuid.getMostSignificantBits());
        byteBuf.writeLong(uuid.getLeastSignificantBits());
    }
}