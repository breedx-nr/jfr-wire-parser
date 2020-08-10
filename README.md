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



