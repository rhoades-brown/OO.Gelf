import com.hp.oo.sdk.content.annotations.Action;
import com.hp.oo.sdk.content.annotations.Output;
import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.annotations.Response;
import com.hp.oo.sdk.content.constants.OutputNames;
import com.hp.oo.sdk.content.constants.ResponseNames;
import com.hp.oo.sdk.content.plugin.ActionMetadata.MatchType;
import com.hp.oo.sdk.content.plugin.ActionMetadata.ResponseType;

import java.util.HashMap;
import java.util.Map;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

public class GelfMessages {

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
            @Param("flowName") String flowName
    ) {
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            final GelfConfiguration config = new GelfConfiguration(new InetSocketAddress(hostname, port))
                    .transport(GelfTransports.UDP).queueSize(512).connectTimeout(5000).reconnectDelay(1000)
                    .tcpNoDelay(true).sendBufferSize(32768);

            GelfMessageLevel messageLevel;

            if (priority.isEmpty()) {
                messageLevel = GelfMessageLevel.ALERT;
            } else {
                try {
                    Integer priorityLevel = Integer.parseInt(priority);
                    messageLevel = GelfMessageLevel.fromNumericLevel(priorityLevel);
                } catch (Exception e) {
                    messageLevel = GelfMessageLevel.valueOf(priority);
                }
            }

            final GelfTransport transport = GelfTransports.create(config);

            final GelfMessageBuilder builder = new GelfMessageBuilder(message, InetAddress.getLocalHost().getHostName())
                    .level(messageLevel)
                    .fullMessage(detail)
                    .additionalField("_OO_flowName", flowName);

            final GelfMessage gelfMessage = builder.build();

            boolean enqueued = transport.trySend(gelfMessage);
        } catch (Exception e) {
            resultMap.put("result_message", e.getMessage());
        }

        // The "result" output
        resultMap.put(OutputNames.RETURN_RESULT, "done");

        // The "result_message" output
        resultMap.put("result_message", "This is a bad sum");

        return resultMap;
    }


    /**
     * This Action sums two numbers and results in success only if the sum is positive.
     * The description "Adds two numbers" will be imported into the Description field when this action is imported into Studio.
     * The action has two outputs - One containing the actual result, and the other a message.
     * The action has two responses
     *  Success - A success response. Will be chosen when the field "result" (From the return map) will be greater than or equal to "0"
     *  Failure - An error response. Will be chosen when the field "result" (From the return map) will be less than "0"
     *
     * @param x The first number. @Param("op1") will make x's name in OO (Studio and central) be "op1"
     * @param y The second number. @Param("op2") will make y's name in OO (Studio and central) be "op2"
     */
    @Action(name = "checkPositiveSum",
            description = "Adds two numbers",
            outputs = {
                    @Output(OutputNames.RETURN_RESULT),
                    @Output("result_message")
            },
            responses = {
                    @Response(text = ResponseNames.SUCCESS, field = OutputNames.RETURN_RESULT, value = "0", matchType = MatchType.COMPARE_GREATER_OR_EQUAL, responseType = ResponseType.RESOLVED),
                    @Response(text = ResponseNames.FAILURE, field = OutputNames.RETURN_RESULT, value = "0", matchType = MatchType.COMPARE_LESS, responseType = ResponseType.ERROR)
            })
    public Map<String, String> checkPositiveSum(@Param("op1") int x, @Param("op2") int y) {
        Map<String, String> resultMap = new HashMap<String, String>();

        //Calculate sum
        int sum = x + y;

        //The "result" output
        resultMap.put(OutputNames.RETURN_RESULT, String.valueOf(sum));

        //The "result_message" output
        if (sum >= 0) {
            resultMap.put("result_message", "This is a good sum");
        } else {
            resultMap.put("result_message", "This is a bad sum");
        }

        return resultMap;
    }
}
