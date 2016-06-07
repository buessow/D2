package com.buessow.dex.g4;

import org.junit.Test;

import static org.junit.Assert.*;
/**
 * Created by buessow on 6/6/16.
 */
public class PackageFormatterTest {
  @Test
  public void format_simple() throws Exception {
    assertArrayEquals(
        new byte[] { 0x01, 0x06, 0x00,  0x0b, 0x7f, 0x75 },
        PackageFormatter.format(Command.READ_FIRMWARE_HEADER));
  }

}
