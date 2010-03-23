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

import java.util.concurrent.TimeUnit;

/**
 * The interface of a trapper and sender. Trappers and senders take initiative
 * in sending data to the monitoring server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 */
public interface Trapper {

    /**
     * Send a value to the monitoring server immediately. This method will not
     * accept <code>null</code> values.
     * 
     * @param key
     *            The identifier of the data item.
     * @param value
     *            The value. Cannot be <code>null</code>.
     */
    void send(String key, Object value);

    /**
     * Stop the trapper and clean up.
     */
    void stop();

    /**
     * Send the output of a JMX query to the server immediately.
     * 
     * @param key
     *            The identifier of the data item.
     * @param objectName
     *            The JMX object to query.
     * @param attribute
     *            The attribute on that object.
     */
    void send(String key, String objectName, String attribute);

    /**
     * Schedule the sending of the output of a JMX query to the server. The
     * query is performed at a fixed rate and the first query is performed
     * immediately.
     * 
     * @param time
     *            The time duration in the given <code>unit</code>.
     * @param unit
     *            The unit of the <code>time</code> argument.
     * @param key
     *            The identifier of the data item.
     * @param objectName
     *            The JMX object to query.
     * @param attribute
     *            The attribute on that object.
     */
    void every(int time, TimeUnit unit, String key, String objectName,
            String attribute);
}
