import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.constants.OutputNames;
import com.hp.oo.sdk.content.constants.ResponseNames;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;
import org.graylog2.gelfclient.*;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GelfMessages {


    private String GetOOStats() throws Exception {
        List<OOResource> ooResources = new ArrayList<OOResource>();

        ooResources.add(new OOResource()); // gets heap.
        for (MemoryPoolMXBean memoryPoolMXBean : ManagementFactory.getMemoryPoolMXBeans()) {
            ooResources.add(new OOResource(memoryPoolMXBean));
        }

        final ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(ooResources);
    }

    private GelfMessageLevel GetDebugLevel(String level, GelfMessageLevel defaultLevel){

<<<<<<< HEAD
        if (level == null || level.isEmpty()) {
=======
        if (level.isEmpty()) {
>>>>>>> First working version.
            return  defaultLevel;
        } else {
            try {
                return  GelfMessageLevel.fromNumericLevel(
                        Integer.parseInt(level)
                );
            } catch (Exception e) {
                return GelfMessageLevel.valueOf(
                        level.toUpperCase()
                );
            }
        }

    }

    private Map<String, Object> ProcessKeyValuePair(String strings, Boolean addUnderscore) {
        Map<String,Object> result = new HashMap<>();
<<<<<<< HEAD
        if (strings != null && !strings.isEmpty()) {
            for (String line : strings.split("\\r?\\n")) {
                Pattern compile = Pattern.compile("^(?<variable>\\w+)=(?<value>.*)$");
                Matcher matcher = compile.matcher(line);
                matcher.find();
                if (addUnderscore) result.put("_" + matcher.group("variable"), matcher.group("value"));
                else result.put(matcher.group("variable"), matcher.group("value"));
            }
=======

        for (String line : strings.split("\\r?\\n")) {
            Pattern compile = Pattern.compile("^(?<variable>\\w+)=(?<value>.*)$");
            Matcher matcher = compile.matcher(line);
            matcher.find();
            if (addUnderscore) result.put("_" + matcher.group("variable"), matcher.group("value"));
            else result.put(matcher.group("variable"), matcher.group("value"));
>>>>>>> First working version.
        }
        return result;
    }

    private String ProcessKeysValuePairsIntoJSONString(String KeyValues) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(ProcessKeyValuePair(KeyValues, false));
    }

    @Action(
            name = "Send Gelf Message",
            description = "Publishes a Gelf message via UDP.",
            outputs = {
                    @Output(OutputNames.RETURN_RESULT),
                    @Output("result_message")
            },
            responses = {
                    @Response(
                            text = ResponseNames.SUCCESS,
                            field = OutputNames.RETURN_RESULT,
                            value = "0",
                            responseType = ResponseType.RESOLVED,
                            isDefault = true
                    ),
                    @Response(
                            text = ResponseNames.FAILURE,
                            field = OutputNames.RETURN_RESULT,
                            value = "0",
                            responseType = ResponseType.ERROR
                    )
            })
    public Map<String, String> newGelfMessageUDP(
            @Param("message") String message,
            @Param("detail") String detail,
            @Param("grayLogHost") String hostname,
            @Param("grayLogPort") int port,
            @Param("includeStats") boolean includeStats,
            @Param("priority") String priority,
            @Param("logAbove") String minimumPriority,
            @Param("additionalProperties") String additionalProperties,
            @Param("objectDetails") String objectDetails
    ){
        Map<String, String> resultMap = new HashMap<String, String>();

        GelfMessageLevel messageLevel = GetDebugLevel(priority, GelfMessageLevel.ALERT);
        GelfMessageLevel minimumLevel = GetDebugLevel(minimumPriority, GelfMessageLevel.INFO);
        if (messageLevel.getNumericLevel() <= minimumLevel.getNumericLevel() ) {

            //  try {
            final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress(hostname, port))
                    .transport(GelfTransports.UDP).queueSize(512).connectTimeout(5000).reconnectDelay(1000)
                    .tcpNoDelay(true).sendBufferSize(32768);


            final GelfTransport transport = GelfTransports.create(config);
            String hostName = "unknown";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {}

            final GelfMessageBuilder builder = new GelfMessageBuilder(message, hostName)
                    .level(messageLevel)
                    .fullMessage(detail)
                    .additionalFields(
                        ProcessKeyValuePair(additionalProperties, true));
<<<<<<< HEAD

            try {
                builder.additionalField("_data", ProcessKeysValuePairsIntoJSONString(objectDetails));
            } catch (Exception e) { }


            if (includeStats) {
                builder.additionalField(
                        "_OO_uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime()
                    ).additionalField(
                                "_OO_threads", java.lang.management.ManagementFactory.getThreadMXBean().getThreadCount()
                    );

=======

            try {
                builder.additionalField("_data", ProcessKeysValuePairsIntoJSONString(objectDetails));
            } catch (Exception e) { }

            builder.additionalField("_OO_uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());

            if (includeStats) {
                builder.additionalField("_OO_threads", java.lang.management.ManagementFactory.getThreadMXBean().getThreadCount());
>>>>>>> First working version.
                try {
                    builder.additionalField("_OO_resource", GetOOStats());
                } catch (Exception e) {
                }
            }

            final GelfMessage gelfMessage = builder.build();

            boolean enqueued = transport.trySend(gelfMessage);
            // } catch (Exception e) {
            //     resultMap.put("result_message", e.getMessage());
            //}

            // The "result" output
            resultMap.put(OutputNames.RETURN_RESULT, "done");
            resultMap.put("result_message", gelfMessage.toString());
        } else {
            resultMap.put("result_message", "Message level was not high enough.");
        }
        // The "result_message" output


        return resultMap;
    }

}
