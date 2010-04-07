package com.orabbix;



/* This file is part of Zapcat.
 *
 * Zapcat is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Zapcat is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Zapcat. If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.Ostermiller.util.Base64;

/**
 * A daemon thread that waits for and forwards data items to a Zabbix server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
final class Sender extends Thread {
    private static final Logger log = Logger.getLogger("Orabbix");

    private final BlockingQueue<Item> queue;

    private final InetAddress zabbixServer;

    private final int zabbixPort;
    
    private final String head;
    
    private final String host;
    
    private static final String middle = "</key><data>";

    private static final String tail = "</data></req>";

    private final byte[] response = new byte[1024];

    private boolean stopping = false;

    private static final int retryNumber = 10; 
    
    private static final int TIMEOUT = 30 * 1000;

    /**
     * Create a new background sender.
     * 
     * @param queue
     *            The queue to get data items from.
     * @param zabbixServer
     *            The name or IP of the machine to send the data to.
     * @param zabbixPort
     *            The port number on that machine.
     * @param host
     *            The host name, as defined in the host definition in Zabbix.
     *   
     */
    public Sender(final BlockingQueue<Item> queue,
            final InetAddress zabbixServer, final int zabbixPort,
            final String host) {
        super("Zabbix-sender");
        setDaemon(true);

        this.queue = queue;

        this.zabbixServer = zabbixServer;
        this.zabbixPort = zabbixPort;
        this.host = host;
        this.head = "<req><host>" + Base64.encode(host) + "</host><key>";
    }

    /**
     * Indicate that we are about to stop.
     */
    public void stopping() {
        stopping = true;
        interrupt();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        while (!stopping) {
            try {
                final Item item = queue.take();
                int retryCount = 0;
                while (retryCount<= retryNumber){
	                try{
	                	send(item.getKey(), item.getValue());
	                	break;
	                	}catch (Exception e){
	                		log.warn("Warning while sending item "+item.getKey()+" value "+item.getValue()+" on host "+host+" retry number "+retryCount+" error:"+ e);
	                		Thread.sleep(1000);
	                		retryCount++;
	                		continue ;
	                	}
                }
                if (retryCount==retryNumber){
                	log.error("Error i didn't sent item "+item.getKey()+"  on host "+host+" tried "+retryCount +" times");
                }         
            } catch (InterruptedException e) {
                if (!stopping) {
                    log.warn("ignoring exception", e);
                }
                       
            } catch (Exception e) {
                log.warn("ignoring exception", e);
            }
        }

        // drain the queue
        while (queue.size() > 0) {
            final Item item = queue.remove();
            int retryCount = 0;
            while (retryCount<= retryNumber){
            	try {
            		send(item.getKey(), item.getValue());
            		break;
            	} catch (Exception e) {
            		log.warn("Warning while sending item "+item.getKey()+"  on host "+host+" retry number "+retryCount+1+" error:"+ e);
            		retryCount++;
            		continue;
            		}
           	
            }
            if (retryCount==retryNumber){
            	log.error("Error i didn't sent item "+item.getKey()+"  on host "+host+" tried "+retryCount+1);
            }
        }
    }

    private void send(final String key, final String value) throws IOException {
        final StringBuilder message = new StringBuilder(head);
        message.append(Base64.encode(key));
        message.append(middle);
        message.append(Base64.encode(value == null ? "" : value));
        message.append(tail);

        if (log.isDebugEnabled()) {
            log.debug("sending " + message);
        }

        Socket zabbix = null;
        OutputStreamWriter out = null;
        InputStream in = null;
        try {
            zabbix = new Socket(zabbixServer, zabbixPort);
            zabbix.setSoTimeout(TIMEOUT);
            

            out = new OutputStreamWriter(zabbix.getOutputStream());
            out.write(message.toString());
            out.flush();

            in = zabbix.getInputStream();
            final int read = in.read(response);
            if (log.isDebugEnabled()) {
                log.debug("received " + new String(response));
            }
            if (read != 2 || response[0] != 'O' || response[1] != 'K') {
                log.warn("received unexpected response '"
                        + new String(response) + "' for key '" + key + "'");
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (zabbix != null) {
                zabbix.close();
            }
        }
    }
}
