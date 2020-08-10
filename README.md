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

# chunk

Here is a link to the [chunk writer source](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/share/jfr/recorder/repository/jfrChunkWriter.cpp).
[This decoder ring](https://github.com/openjdk/jdk11/blob/37115c8ea4aff13a8148ee2b8832b20888a5d880/src/hotspot/cpu/zero/bytes_zero.hpp#L31)
gives some hints about the various sizes of `u2`, `u4`, `u8`.  `u2` is an "unsigned short", or 16 bits (2 bytes).
Therefore `u4` is 32 bits (4 bytes), `u8` is 64 bits (8 bytes). 

Whenever a chunk is opened, this is automatically written:

| field | offset | length | example | notes               |
|-------|--------|------ -|---------|---------------------|
| magic | 0      | 4      | FLR\0   | 4-byte nagic string |
| major | 4      | 2      | 0002    | major version       |
| minor | 6      | 2      | 0000    | minor version       |
|  x    | x      | x      | x       | x                   |


