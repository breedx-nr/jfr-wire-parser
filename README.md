# jfr-wire-parser
An experiment to parse JFR data

# wat?

Using the [`FlightRecorderMXBean`](https://docs.oracle.com/en/java/javase/11/docs/api/jdk.management.jfr/jdk/management/jfr/FlightRecorderMXBean.html), you can begin "streaming" flight recorder
data out of a running process.  **Note:** this is not the same as the Java 14
"streaming" APIs which invoke registered callbacks.  This is a byte-for-byte
stream of what would normally be placed into a JFR file.

Normally, to parse this data, you would write it to a file and then
use the `RecordingFile` to read it in as objects.  That's pretty inefficient.
What if we could just parse this data as we see it?  I heard somewhere
that the format is simple enough...

# challenges

Initial impressions:
* The JDK internal parsers are complicated/convoluted.  
* A bunch of what would normally be simple parsing is hidden by work being done in constructors, a
very unforunately anitpattern.
* A notable lack of file format definition?  Well, I haven't found one yet.

# sample data
Here is a slice of the top of a JFR file.  It is used as a reference in the 
below chunk decoding.
```
00000000: 464c 5200 0002 0000 0000 0000 0010 b182  FLR.............
00000010: 0000 0000 000f 0147 0000 0000 000f 8dab  .......G........
00000020: 162a 092e cfe0 6f00 0000 0002 550f 4b00  .*....o.....U.K.
00000030: 0000 0128 9024 2ec3 0000 0000 a0ee bb00  ...(.$..........
00000040: 0000 0001 a280 8000 c216 968d b483 8925  ...............%
00000050: 0032 00c2 1604 0974 6872 6573 686f 6c64  .2.....threshold
00000060: 0404 3020 6e73 a480 8000 c216 83f5 b883  ..0 ns..........
00000070: 8925 0032 00c2 1604 0a73 7461 636b 5472  .%.2.....stackTr
00000080: 6163 6504 0566 616c 7365 a080 8000 c216  ace..false......
00000090: b2a9 b983 8925 0032 00c2 1604 0765 6e61  .....%.2.....ena
000000a0: 626c 6564 0404 7472 7565 a280 8000 c216  bled..true......
000000b0: bcbd ba83 8925 0032 00c0 1604 0974 6872  .....%.2.....thr
000000c0: 6573 686f 6c64 0404 3020 6e73 a380 8000  eshold..0 ns....
000000d0: c216 ea86 bc83 8925 0032 00c0 1604 0a73  .......%.2.....s
000000e0: 7461 636b 5472 6163 6504 0474 7275 65a0  tackTrace..true.
000000f0: 8080 00c2 16d4 b1bf 8389 2500 3200 c016  ..........%.2...
00000100: 0407 656e 6162 6c65 6404 0474 7275 65a2  ..enabled..true.
00000110: 8080 00c2 16ad ffc2 8389 2500 3200 be16  ..........%.2...
00000120: 0406 7065 7269 6f64 0407 3130 3030 206d  ..period..1000 m
00000130: 73a0 8080 00c2 16ff e5c3 8389 2500 3200  s...........%.2.
00000140: be16 0407 656e 6162 6c65 6404 0474 7275  ....enabled..tru
00000150: 65a2 8080 00c2 169e d1c5 8389 2500 3200  e...........%.2.
00000160: bc16 0409 7468 7265 7368 6f6c 6404 0430  ....threshold..0
00000170: 206e 73a3 8080 00c2 16c6 96c6 8389 2500   ns...........%.
00000180: 3200 bc16 040a 7374 6163 6b54 7261 6365  2.....stackTrace
```

# chunk

Here is a link to the [chunk writer source](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/share/jfr/recorder/repository/jfrChunkWriter.cpp).
[This decoder ring](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/cpu/zero/bytes_zero.hpp#L31)
gives some hints about the various sizes of `u2`, `u4`, `u8`.  `u2` is an "unsigned short", or 16 bits (2 bytes).
Therefore `u4` is 32 bits (4 bytes), `u8` is 64 bits (8 bytes). jlong is 64 bits.

Whenever a chunk is opened, the skeleton of this is automatically written, and then 
when the chunk is finished, the "chunk header" starting at offset 8 is written.

//TODO: Maybe pay more attention to endian because it might not all be big endian?

| field                | offset | length | example | notes               |
|----------------------|--------|--------|---------|---------------------|
| magic                | 0      | 4      | FLR\0   | 4-byte magic string |
| major                | 4      | 2      | 0002    | major version       |
| minor                | 6      | 2      | 0000    | minor version       |
| chunk size           | 8      | 8      | 0000 0000 0010 b182 | the total size of the chunk, in this example 1094018 bytes decimal | 
| checkpoint offset    | 16     | 8      | 0000 0000 000f 0147 | obtained from _chunkstate->previous_checkpoint_offset(), so maybe "initial" means previous?   | 
| metadata event offset| 24     | 8      | 0000 0000 000f 8dab | metadata is the last thing written in the chunk, this is the offset to it |
| chunk start nanos    | 32     | 8      | 162a 092e cfe0 6f00 | that's a big number eh |
| chunk duration nanos | 40     | 8      | 0000 0002 550f 4b00 | this chunk took 10.017 seconds |
| chunk start ticks    | 48     | 8      | 0000 0128 9024 2ec3 | another big number   |
| clock frequency      | 56     | 8      | 0000 0000 a0ee bb00 | this calls `os::elapsed_frequency()` which is platform dependent.  It's a billion on linux/bsd, 1 million on aix, etc. NOT CPU frequency. |  
| compressed ints      | 64     | 4      | 0000 0001 | ints are compressed, [hard coded to always 1](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/share/jfr/recorder/service/jfrOptionSet.cpp#L151)   

# then...

There's a section in `JfrRecorderService.cpp` in the `write` method that
has 
```
  pre_safepoint_write();
  invoke_safepoint_write();
  post_safepoint_write();
``` 
and `-re_safepoint_write()` has:

```
  _checkpoint_manager.write_types();
  _checkpoint_manager.write_epoch_transition_mspace();
  write_stacktrace_checkpoint(_stack_trace_repository, _chunkwriter, false);
  write_stringpool_checkpoint(_string_pool, _chunkwriter);
```

so let's look at `write_types()` first:

## write_types()

`JfrCheckpointManager::write_types()` which calls `JfrTypeManager::write_types(writer)`.
This implementation iterates over a list of `JfrSerializerRegistration`, which I suppose
is one per type that has been registered.
