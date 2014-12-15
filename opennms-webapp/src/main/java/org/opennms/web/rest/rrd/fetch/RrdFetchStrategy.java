package org.opennms.web.rest.rrd.fetch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.jrobin.core.RrdException;
import org.opennms.web.rest.rrd.QueryRequest;

/**
 * Used to retrieve values from the underlying RRD storage.
 *
 * @author jesse
 */
public interface RrdFetchStrategy {
	public SortedMap<Long, Map<String, Double>> fetch(final long step, final long start,
            final long end, final List<QueryRequest.Source> sources) throws IOException, RrdException;
}
