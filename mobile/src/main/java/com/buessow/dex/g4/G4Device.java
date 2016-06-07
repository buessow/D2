package com.buessow.dex.g4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by buessow on 6/6/16.
 */
public class G4Device {

  private OutputStream out;
  private InputStream in;

  public G4Device(InputStream in, OutputStream out) {
    this.in = in;
    this.out = out;
  }

  public String getFirmwareHeader() throws Exception {
    new Packet(Command.READ_FIRMWARE_HEADER).write(out);
    return new Packet(in).getDataAsString();
  }
}
