package com.buessow.dex.g4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by buessow on 6/6/16.
 */
public class Packet {
  private static final byte HEADER_INIT = 1;
  private static final int HEADER_LENGTH = 4;
  private static final int CRC16_LENGTH = 2;

  public final byte command;
  public final byte[] data;

  public Packet(Command command) {
    this(command, new byte[0]);
  }

  public Packet(Command command, byte[] data) {
    this.command = command.getCode();
    this.data = data;
  }

  public Packet(InputStream in) throws Exception {
    ByteBuffer headerBuffer = ByteBuffer.allocate(HEADER_LENGTH);
    headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
    Crc16 crc = new Crc16();
    read(in, headerBuffer.array());
    crc.add(headerBuffer.array());
    if (headerBuffer.get() != HEADER_INIT)
      throw new IOException("unrecognized response, got " + headerBuffer.get(0));
    int packetLength = headerBuffer.getChar();
    command = headerBuffer.get();
    data = new byte[packetLength - HEADER_LENGTH - CRC16_LENGTH];
    read(in, data);
    crc.add(data);
    ByteBuffer crcBuffer = ByteBuffer.allocate(CRC16_LENGTH);
    crcBuffer.order(ByteOrder.LITTLE_ENDIAN);
    read(in, crcBuffer.array());
    int actualCrc = crcBuffer.getShort();
    if (crc.get() != actualCrc) {
      throw new IOException("checksum error, expect: " + crc.get() + " got: " + actualCrc);
    }
  }

  private void read(InputStream in, byte[] buffer) throws IOException {
    int actualLength = in.read(buffer);
    if (actualLength != buffer.length) {
      throw new IOException("truncated response, got " + actualLength + " expect " + buffer.length);
    }
  }

  public byte getCommand() {
    return command;
  }

  public byte[] getData() {
    return data;
  }

  public String getDataAsString() {
    return new String(data);
  }

  public byte[] format() {
    int payloadLength = data.length + HEADER_LENGTH;
    int totalLength = payloadLength + CRC16_LENGTH;
    ByteBuffer output = ByteBuffer.allocate(totalLength);
    output.order(ByteOrder.LITTLE_ENDIAN);
    output.put(HEADER_INIT);
    output.putShort((short)totalLength);
    output.put(command);
    output.put(data);
    Crc16.appendCrc16(output);
    return output.array();
  }

  public void write(OutputStream out) throws IOException {
    out.write(format());
  }
}
