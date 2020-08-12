package com.newrelic.jfr.parser;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ByteSupport {

    public long readVarLen(ByteBuffer buff) {
        var b = buff.get();
        return readVarLenRecursive(buff, b, 0, 0);
    }

    private long readVarLenRecursive(ByteBuffer buff, byte b, int bytepos, long acc) {
        if (bytepos == 8) {
            return acc + (((long) (b & 0XFF)) << 56);
        }
        long result = acc + ((b & 0x7FL) << (7 * bytepos));
        if (b >= 0) {
            return result;
        }
        return readVarLenRecursive(buff, buff.get(), bytepos + 1, result);
    }

    public String poolString(ByteBuffer buff, List<String> poolStrings) {
        var index = (int) readVarLen(buff);
        return poolStrings.get(index);
    }

    public String readString(ByteBuffer buff) {
        byte enc = buff.get();
        switch (enc) {
            case 0:
                return null;
            case 1:
                return "";
            case 3: {
                var size = (int) readVarLen(buff);
                var bytes = readBytes(buff, size);
                return new String(bytes, UTF_8);
            }
            case 4: {
                var size = (int) readVarLen(buff);
                var chars = new char[size];
                for (int i = 0; i < size; i++) {
                    chars[i] = (char) readVarLen(buff);
                }
                return new String(chars);
            }
            case 5: {
                var size = (int) readVarLen(buff);
                var bytes = readBytes(buff, size);
                return new String(bytes, ISO_8859_1);
            }
        }
        if (enc == 0) {
            return null;
        }

        return null;
    }

    @NotNull
    private byte[] readBytes(ByteBuffer buff, int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = buff.get();
        }
        return bytes;
    }
}
