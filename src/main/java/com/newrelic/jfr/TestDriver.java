package com.newrelic.jfr;

import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingFile;
import jdk.management.jfr.RecordingInfo;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

// BEEP BEEP
public class TestDriver {

    final static String host = "localhost";
    final static int port = 1099;

    public static void main(String[] args) throws Exception {
        var conn = connect();
        System.out.println("Server connection established");
        var flightRecorder = new ObjectName("jdk.management.jfr:type=FlightRecorder");

        long recordingId = (long) conn.invoke(flightRecorder, "newRecording", new Object[]{}, new String[]{});
        System.out.println("New recording created: " + recordingId);
        conn.invoke(flightRecorder, "setPredefinedConfiguration",
                new Object[]{recordingId, "profile"}, new String[]{"long", "java.lang.String"});
        System.out.println("setPredefinedConfiguration complete...starting recording...");

//        conn.invoke(objectName, "setRecordingOptions", tbd, tbd);
        var tabularData = makeTabularData(Map.of(
                "name", "New Relic JFR Recording",
                "disk", "true"));
        conn.invoke(flightRecorder, "startRecording", new Object[]{recordingId}, new String[]{"long"});
        System.out.println("Recording started!");
        System.out.println("Sleeping a bit...");

        TimeUnit.SECONDS.sleep(10);
        conn.invoke(flightRecorder, "stopRecording", new Object[]{recordingId}, new String[]{"long"});

        var streamId = (long)conn.invoke(flightRecorder, "openStream",
                new Object[]{recordingId, tabularData}, new String[]{"long", "javax.management.openmbean.TabularData"});
        System.out.println("Stream opened: " + streamId);


        var out = new FileOutputStream("/tmp/testfile.jfr");
        byte[] chunk;
        while ((chunk = readBytes(conn, flightRecorder, streamId)) != null) {
            System.out.println("Read " + chunk.length + " bytes from fake stream");
            out.write(chunk);
        }
        out.close();

        var rec = new RecordingFile(new File("/tmp/testfile.jfr").toPath());
        var ev = rec.readEvent();

        System.out.println("All data streamed.");
        conn.invoke(flightRecorder, "closeStream", new Object[]{streamId}, new String[]{"long"});
        System.out.println("Stream closed");

    }

    private static byte[] readBytes(MBeanServerConnection conn, ObjectName flightRecorder, long streamId) throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
        return (byte[]) conn.invoke(flightRecorder, "readStream", new Object[]{streamId}, new String[]{"long"});
    }

    private static MBeanServerConnection connect() throws IOException {
        var urlPath = "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
        var url = new JMXServiceURL("rmi", "", 0, urlPath);
        System.out.println("JMXConnectorProviderImpl called");
        var connector = new RMIConnector(url, Map.of());
        connector.connect();
        System.out.println("JMX Connected!");
        return connector.getMBeanServerConnection();
    }

    private static TabularDataSupport makeTabularData(final Map<String, String> options)
            throws OpenDataException {
        var typeName = "java.util.Map<java.lang.String, java.lang.String>";
        var itemNames = new String[]{"key", "value"};
        var openTypes = new OpenType[]{SimpleType.STRING, SimpleType.STRING};
        var rowType = new CompositeType(typeName, typeName, itemNames, itemNames, openTypes);
        var tabularType = new TabularType(typeName, typeName, rowType, new String[]{"key"});
        var table = new TabularDataSupport(tabularType);

        for (var entry : options.entrySet()) {
            Object[] itemValues = {entry.getKey(), entry.getValue()};
            CompositeData element = new CompositeDataSupport(rowType, itemNames, itemValues);
            table.put(element);
        }

        return table;
    }

}
