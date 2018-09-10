import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.net.InetSocketAddress;

public class GelfPersistance {

        private static GelfPersistance instance;
        private GelfConfiguration config;
        private GelfTransport transport;

    private GelfPersistance(String hostname, int port){
            config = new GelfConfiguration(new InetSocketAddress(hostname, port))
                    .transport(GelfTransports.UDP)
                    .queueSize(512)
                    .connectTimeout(5000)
                    .reconnectDelay(1000)
                    .tcpNoDelay(true)
                    .sendBufferSize(32768);

            transport = GelfTransports.create(config);
        }

        public static synchronized GelfPersistance getInstance(String hostname, int port){
            if(instance == null){
                instance = new GelfPersistance(hostname, port);
            }
            return instance;
        }

        public  GelfConfiguration getConfiguration(){
            return  config;
        }

        public GelfTransport getTransport(){
            return  transport;
        }

        public boolean SendGelfMessage(GelfMessage message){
            return transport.trySend(message);
        }
}
