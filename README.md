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
* Almost everything in the jfr subsystem is concealed, internal, locked down, or otherwise 
made to be completely unusable by anyone doing anything other than the singular
expected use case.

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

The "chunk" is the basic high-level block of JFR data.  JFR data is always contained in chunks.
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
| constant pool offset | 16     | 8      | 0000 0000 000f 0147 | obtained from _chunkstate->previous_checkpoint_offset(), so maybe "initial" means previous?   | 
| metadata offset      | 24     | 8       | 0000 0000 000f 8dab | metadata is the last thing written in the chunk, this is the offset to it |
| chunk start nanos    | 32     | 8      | 162a 092e cfe0 6f00 | that's a big number eh |
| chunk duration nanos | 40     | 8      | 0000 0002 550f 4b00 | this chunk took 10.017 seconds |
| chunk start ticks    | 48     | 8      | 0000 0128 9024 2ec3 | another big number   |
| clock frequency      | 56     | 8      | 0000 0000 a0ee bb00 | this calls `os::elapsed_frequency()` which is platform dependent.  It's a billion on linux/bsd, 1 million on aix, etc. NOT CPU frequency. |  
| compressed ints      | 64     | 4      | 0000 0001 | ints are compressed, the parsing side calls this "features" (unused). [hard coded to always 1](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/share/jfr/recorder/service/jfrOptionSet.cpp#L151)   

# metadata

The offset to the metadata is stored in the chunk header, and the metadata exists at 
the bottom of a chunk.  The chunk can be found at `chunkStart + metadataOffset` (denoted `s` below).
Metadata MUST be read before any events, because it contains bits required for parsing
events.  Note that `v` for length below means "variable" because numeric types are in a
special 7-bit packed format, and therefore computing offsets is impossible.  Once you start
reading metadata, don't lose your place, or you will have to re-read everything.

It's all stateful!

`sid` is the "string index" in the constant pool.

| field                | offset | length | example    | notes               |
|----------------------|--------|--------|------------|---------------------|
| size                 | s      | v      | d7 c784 00 | Size of the metadata block. Who doesn't like a 3-byte data type?  ;) |
| type id              | ? n/a  | 1      |            | Must be zero, so the size must be 1 byte |
| start time           | ? n/a  | v      | b5e6 93a7 e401 | this case had 6 bytes, which translted to the decimal value 61285397301. Parser ignores this. |
| duration             | ? n/a  | v (1?) | 00         | this was just zero in this example?  it is ignored by the parser |
| metadata id          | ? n/a  | v (1?) | 00         | just zero in this example. just logged and ignored |
| const pool size      | ? n/a  | v      | b00c       | in this example, the pool is 1584 bytes long 
| cp 1                 | ? n/a  | v      | ?          | an encoded string.  there are `const pool size` consecutive entries of this same type. |
| cp 2                 | ? n/a  | v      | ?          | an encoded string.  there are `const pool size` consecutive entries of this same type. |
| ...                  |        |        |            | an encoded string.  there are `const pool size` consecutive entries of this same type. |
| cp n                 | ? n/a  | v      | ?          | an encoded string.  there are `const pool size` consecutive entries of this same type. |
| root name sid        | ? n/a  | v      | 70         | the name of the root element          |
| attribute count      | ? n/a  | v      | 00         | the number of attributes in the root element. In this example it was 0.  |
| <key1> sid           | ? n/a  | v      | 12         | index to key in constant pool         |
| <val1> sid           | ? n/a  | v      | 4A         | index to value in constant pool       |
| <key2> sid           | ? n/a  | v      | xx         | index to key in constant pool         |
| <val3> sid           | ? n/a  | v      | xx         | index to value in constant pool       |
| ...                  | ? n/a  | v      | xx         | index to value in constant pool       |
| <keyn> sid           | ? n/a  | v      | xx         | index to key in constant pool         |
| <valn> sid           | ? n/a  | v      | xx         | index to value in constant pool       |
| child count          | ? n/a  | v      | 02         | the number of elements for the root element   |

This is now a recursive structure, where for each child we parse its name and attributes and
child count and then its children.

The root element of the metadata descriptor had (in this particular case) 2 children: `metadata` and
`region`.  The metadata is the interesting bit.  It contains the mappings of id to type descriptor,
which is later used in parsing.  It also says what each type consists of.  All of this is dynamically
generated from the huge xml file descriptor in the JDK source repo.  The JDKs parser puts this metadata
into maps for faster lookup.  The end result is a big map of parsers indexed by ID.  Most of the
`EventParser` instances are composed of an array of other parsers.


## How to read a string:     

Look in RecordingInput.java `readUTF()` for how to read/decode a string.
In later JVMs this has moved to anther class (StringParser I think).

When it comes time to read a string, read the first byte.

| field                | offset | length | example    | notes               |
|----------------------|--------|--------|------------|---------------------|
| encoding             | 0      | 1      | 04         | if 0, null string all done. if 1, empty string, all done. 3 = UTF8 byte array, 4 = char array, 5 = ISO-8859-1 byte array.  NOTE that the javadoc is wrong in `RecordingInput.java`. |
| size                 | 1      | v      | 10         | the size of the following array.  you might think that chars are 16 bits or that the encoding would matter, but nope, it's more complicated.  If char encoding, read chars as a variable width long (described above/elsewhere) and just cast down to `char`.  Otherwise, just read the byte array and pass it to `String()` with encoding. |
| <content bytes>      | ? n/a  | v      |            | this is the actual content of the constant pool string |


# Events

Hope you got all your metadata sorted in advance, because all of this is super specific
and depends on having good metadata and descriptors.

| field                | offset | length | example   | notes               |
|----------------------|--------|--------|-----------|----------------------
| size                 | 68     | 4      | 9f9b 8000 | might be a strange 7-bit packing sequence.  See RecordingInput.readLong().  |    
| typeId               | 72     | 8      | 01f3 83ed a3e4 0100 | 7-bit packed long. if typeId == 1, then it's the constant pool.  Otherwise, the type id is used to determine which parser to use.|
 
then a ton of type-specific parsing based on the metadata.

The process is basically look up the descriptor for that type ID, and then
recursively execute the parsing tree in depth-first order.  You'll eventually be
reading primitives and strings IDs.
 

