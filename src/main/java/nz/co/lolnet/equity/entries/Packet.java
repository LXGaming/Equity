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

package nz.co.lolnet.equity.entries;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.netty.buffer.ByteBuf;

public class Packet {
	
	private final ByteBuf byteBuf;
	
	public Packet(ByteBuf byteBuf) {
		this.byteBuf = byteBuf;
	}
	
	public void writeString(String string) {
		if (string.length() > Short.MAX_VALUE) {
			throw new UnsupportedOperationException("Cannot send string longer than Short.MAX_VALUE (got " + string.length() + " characters)");
		}
		
		byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
		writeVarInt(bytes.length);
		getByteBuf().writeBytes(bytes);
	}
	
	public String readString() {
		int length = readVarInt();
		if (length > Short.MAX_VALUE) {
			throw new UnsupportedOperationException("Cannot receive string longer than Short.MAX_VALUE (got " + length + " characters)");
		}
		
		byte[] bytes = new byte[length];
		getByteBuf().readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public void writeArray(byte[] bytes) {
		if (bytes.length > Short.MAX_VALUE) {
			throw new UnsupportedOperationException("Cannot send byte array longer than Short.MAX_VALUE (got " + bytes.length + " bytes)");
		}
		
		writeVarInt(bytes.length);
		getByteBuf().writeBytes(bytes);
	}
	
	public byte[] toArray() {
		byte[] bytes = new byte[getByteBuf().readableBytes()];
		getByteBuf().readBytes(bytes);
		return bytes;
	}
	
	public byte[] readArray() {
		return readArray(getByteBuf().readableBytes());
	}
	
	public byte[] readArray(int limit) {
		int length = readVarInt();
		if (length > limit) {
			throw new UnsupportedOperationException("Cannot receive byte array longer than " + limit + " (got " + length + " bytes)");
		}
		
		byte[] bytes = new byte[length];
		getByteBuf().readBytes(bytes);
		return bytes;
	}
	
	public void writeArrayLegacy(byte[] bytes, boolean allowExtended) {
		if (allowExtended) {
			if (bytes.length <= (Integer.MAX_VALUE & 2097050)) {
				throw new UnsupportedOperationException("Cannot send byte array longer than 2097050 (got " + bytes.length + " bytes)");
			}
		} else {
			if (bytes.length <= Short.MAX_VALUE) {
				throw new UnsupportedOperationException("Cannot send byte array longer than Short.MAX_VALUE (got " + bytes.length + " bytes)");
			}
		}
		
		writeVarShort(bytes.length);
		getByteBuf().readBytes(bytes);
	}
	
	public byte[] readArrayLegacy() {
		int length = readVarShort();
		if (length > 2097050) {
			throw new UnsupportedOperationException("Cannot receive byte array longer than 2097050 (got " + length + " bytes)");
		}
		
		byte[] bytes = new byte[length];
		getByteBuf().readBytes(bytes);
		return bytes;
	}
	
	public void writeStringArray(List<String> list) {
		writeVarInt(list.size());
		for (String string : list) {
			writeString(string);
		}
	}
	
	public List<String> readStringArray() {
		int length = readVarInt();
		List<String> list = new ArrayList<String>(length);
		for (int index = 0; index < length; index++) {
			list.add(readString());
		}
		
		return list;
	}
	
	public int getVarIntSize(int input)	{
		for (int index = 1; index < 5; ++index) {
			if ((input & -1 << index * 7) == 0) {
				return index;
			}
		}
		
		return 5;
	}
	
	public int readVarInt() {
		return readVarInt(5);
	}
	
	public int readVarInt(int maxBytes) {
		int result = 0;
		int bytesRead = 0;
		
		while (getByteBuf().readableBytes() != 0) {
			byte read = getByteBuf().readByte();
			result |= (read & 127) << (bytesRead++ * 7);
			
			if (bytesRead > maxBytes) {
				throw new RuntimeException("VarInt too big");
			}
			
			if ((read & 128) != 128) {
				break;
			}
		}
		return result;
	}
	
	public void writeVarInt(int value) {
		while (getByteBuf().isWritable()) {
			int part = value & 127;
			value >>>= 7;
			if (value != 0) {
				part |= 128;
			}
			
			getByteBuf().writeByte(part);
			if (value == 0) {
				break;
			}
		}
	}
	
	public int readVarShort() {
		int low = getByteBuf().readUnsignedShort();
		int high = 0;
		if ((low & 32768) != 0) {
			low = low & 32767;
			high = getByteBuf().readUnsignedByte();
		}
		
		return ((high & 255) << 15) | low;
	}
	
	public void writeVarShort(int value) {
		int low = value & 32767;
		int high = (value & 8355840) >> 15;
		if (high != 0) {
			low = low | 32768;
		}
		
		getByteBuf().writeShort(low);
		if (high != 0) {
			getByteBuf().writeByte(high);
		}
	}
	
	public UUID readUUID() {
		return new UUID(getByteBuf().readLong(), getByteBuf().readLong());
	}
	
	public void writeUUID(UUID uuid) {
		getByteBuf().writeLong(uuid.getMostSignificantBits());
		getByteBuf().writeLong(uuid.getLeastSignificantBits());
	}
	
	public ByteBuf getByteBuf() {
		return byteBuf;
	}
	
	public void clearByteBuf() {
		getByteBuf().clear();
		getByteBuf().setZero(0, getByteBuf().capacity());
		getByteBuf().nioBuffer().clear();
	}
	
	public enum PacketDirection {
		
		CLIENTBOUND, SERVERBOUND;
		
		@Override
		public String toString() {
			return name().toUpperCase();
		}
	}
}