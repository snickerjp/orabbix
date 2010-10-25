/*
 * This file is part of orabbix.
 *
 * orabbix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * orabbix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * orabbix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.orabbix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.Ostermiller.util.Base64;

/**
 * A daemon thread that waits for and forwards data items to a Zabbix server.
 * 
 * @author Kees Jan Koster Completely modified by
 * 		   Andrea Dalle Vacche
 */
public final class Sender implements Runnable {
    private static final Logger log = Logger.getLogger("Orabbix");

    private final BlockingQueue<ZabbixItem> queue;

    private final Hashtable <String , Integer >  zabbixServers;

    
    
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
    public Sender(final BlockingQueue<ZabbixItem> queue,
    		Hashtable <String , Integer > ZabbixServers,
            
            final String host) {
      /*  super("Zabbix-sender");
        setDaemon(true);
*/
        this.queue = queue;
        this.zabbixServers = ZabbixServers;
        this.host = host;
        this.head = "<req><host>" + Base64.encode(host) + "</host><key>";
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
            try {
                final ZabbixItem item = queue.take();
                int retryCount = 0;
                trysend1:
                while (retryCount<= retryNumber){
	                try{
	                	send(item.getKey(), item.getValue());
	                	break;
	                	}catch (Exception e){
	                		log.warn("Warning while sending item "+item.getKey()+" value "+item.getValue()+" on host "+host+" retry number "+retryCount+" error:"+ e);
	                		Thread.sleep(1000);
	                		retryCount++;
	                		if (retryCount==retryNumber){
	                        	log.error("Error i didn't sent item "+item.getKey()+"  on host "+host+" tried "+retryCount +" times");
	                        }         
	                		continue trysend1;
	                	}
               }
                
            } catch (InterruptedException e) {
                if (!stopping) {
                    log.warn("ignoring exception", e);
                }
                       
            } catch (Exception e) {
                log.warn("ignoring exception", e);
            }


        // drain the queue
        while (queue.size() > 0) {
            final ZabbixItem item = queue.remove();
            int retryCount = 0;
            trysend2:
            while (retryCount<= retryNumber){
            	try {
            		send(item.getKey(), item.getValue());
            		break;
            	} catch (Exception e) {
            		log.warn("Warning while sending item "+item.getKey()+"  on host "+host+" retry number "+retryCount+" error:"+ e);
            		retryCount++;
            		continue trysend2;
            		}
           	
            }
            if (retryCount==retryNumber){
            	log.error("Error i didn't sent item "+item.getKey()+"  on host "+host+" tried "+retryCount);
            }
        }
    }

    private void send(final String key, final String value) throws IOException {
        final StringBuilder message = new StringBuilder(head);
        message.append(Base64.encode(key));
        message.append(middle);
        message.append(Base64.encode(value == null ? "" : value));
        message.append(tail);

        /*if (log.isDebugEnabled()) {
            log.debug("sending " + message);
        }*/

        Socket zabbix = null;
        OutputStreamWriter out = null;
        InputStream in = null;
        Enumeration<String> serverlist  = zabbixServers.keys();

        while (serverlist.hasMoreElements()){
        	String zabbixServer = serverlist.nextElement();
	        try {
	            zabbix = new Socket(zabbixServer, zabbixServers.get(zabbixServer).intValue());
	            zabbix.setSoTimeout(TIMEOUT);
	            
	
	            out = new OutputStreamWriter(zabbix.getOutputStream());
	            out.write(message.toString());
	            out.flush();
	
	            in = zabbix.getInputStream();
	            final int read = in.read(response);
	            /*if (log.isDebugEnabled()) {
	                log.debug("received " + new String(response));
	            }*/
	            if (read != 2 || response[0] != 'O' || response[1] != 'K') {
	                log.warn("received unexpected response '"
	                        + new String(response) + "' for key '" + key + "'");
	            }
	        } catch (Exception ex){
	        	log.error("Error contacting Zabbix server "+zabbixServer+"  on port "+ zabbixServers.get(zabbixServer));
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

    /**
     * Indicate that we are about to stop.
     */
    public void stopping() {
        stopping = true;
        /*interrupt();*/
    }
}
