package com.buessow.dex.g4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by buessow on 6/6/16.
 */
public class PackageFormatter {
  public static byte[] format(Command command) {
    int crc16Size = Short.SIZE / Byte.SIZE;
    int payloadLength = 4;
    int totalLength = payloadLength + crc16Size;
    ByteBuffer output = ByteBuffer.allocate(totalLength);
    output.order(ByteOrder.LITTLE_ENDIAN);
    output.put((byte)1);
    output.put((byte)totalLength);
    output.put((byte)0);
    output.put(command.getCode());
    Crc16.appendCrc16(output);
    return output.array();
  }
}
