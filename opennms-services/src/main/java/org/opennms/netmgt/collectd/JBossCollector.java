/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.protocols.jmx.connectors.JBossConnectionFactory;

/*
 * The JBossCollector class manages the querying and storage of data into RRD files.  The list of 
 * MBeans to be queried is read from the jmx-datacollection-config.xml file using the "jboss" service name.
 * The super class, JMXCollector, performs all the work.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
/**
 * <p>JBossCollector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JBossCollector  extends JMXCollector {

    /**
     * <p>Constructor for JBossCollector.</p>
     */
    public JBossCollector() {
        super();
        setServiceName("jboss");
    }

    /* Returns the CollectionWrapper object from the factory using the parameterMap and ip address.
     * 
     * @see org.opennms.netmgt.collectd.JMXCollector#getMBeanServer(java.util.Map)
     */
    /** {@inheritDoc} */
    @Override
    public ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address) {
        return  JBossConnectionFactory.getMBeanServerConnection(parameterMap, address);
    }
    
}
