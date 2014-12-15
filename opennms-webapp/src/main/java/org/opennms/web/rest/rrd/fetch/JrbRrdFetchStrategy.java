package org.opennms.web.rest.rrd.fetch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jrobin.core.RrdException;
import org.jrobin.data.DataProcessor;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.rest.rrd.QueryRequest;
import org.opennms.web.rest.rrd.QueryRequest.Source;

public class JrbRrdFetchStrategy implements RrdFetchStrategy {
	private final ResourceDao m_resourceDao;

	public JrbRrdFetchStrategy(final ResourceDao resourceDao) {
		m_resourceDao = resourceDao;
	}

	@Override
	public SortedMap<Long, Map<String, Double>> fetch(long step, long start,
			long end, List<Source> sources) throws IOException, RrdException {
		final DataProcessor dproc = new DataProcessor(start, end);
        dproc.setStep(step);
        dproc.setFetchRequestResolution(300);

        for (final QueryRequest.Source source : sources) {
            OnmsResource resource = m_resourceDao.getResourceById(source.getResource());
            RrdGraphAttribute rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());

            final String file = System.getProperty("rrd.base.dir") + File.separator + rrdGraphAttribute.getRrdRelativePath();

            dproc.addDatasource(source.getLabel(),
                                file,
                                source.getAttribute(),
                                source.getAggregation());
        }

        SortedMap<Long, Map<String, Double>> results = new TreeMap<Long, Map<String, Double>>();

        dproc.processData();

        long[] timestamps = dproc.getTimestamps();

        for (int i = 0; i < timestamps.length; i++) {
            final long timestamp = timestamps[i] - dproc.getStep();

            Map<String, Double> data = new HashMap<String, Double>();
            for (QueryRequest.Source source : sources) {
                data.put(source.getLabel(), dproc.getValues(source.getLabel())[i]);
            }

            results.put(timestamp, data);
        }

        return results;
	}
}
