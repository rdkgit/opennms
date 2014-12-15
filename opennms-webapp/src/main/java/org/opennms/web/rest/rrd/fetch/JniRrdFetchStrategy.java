package org.opennms.web.rest.rrd.fetch;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jrobin.core.RrdException;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.rest.rrd.QueryRequest;
import org.opennms.web.rest.rrd.QueryRequest.Source;
import org.springframework.util.FileCopyUtils;

public class JniRrdFetchStrategy implements RrdFetchStrategy {
	private final ResourceDao m_resourceDao;

	public JniRrdFetchStrategy(final ResourceDao resourceDao) {
		m_resourceDao = resourceDao;
	}

	@Override
	public SortedMap<Long, Map<String, Double>> fetch(long step, long start,
			long end, List<Source> sources) throws RrdException {
		String rrdBinary = System.getProperty("rrd.binary");

        if (rrdBinary == null) {
            throw new RrdException("rrd.binary property must be set either in opennms.properties or in iReport");
        }

        //construct the query string out of the requestedMetrics data
        final StringBuilder query = new StringBuilder();
        query.append("--step").append(" ")
                .append(step).append(" ");

        query.append("--start").append(" ")
                .append(start).append(" ");

        query.append("--end").append(" ")
                .append(end).append(" ");

        for (final QueryRequest.Source source : sources) {
            final OnmsResource resource = m_resourceDao.getResourceById(source.getResource());
            final RrdGraphAttribute rrdGraphAttribute = resource.getRrdGraphAttributes().get(source.getAttribute());

            final String rrdFile = System.getProperty("rrd.base.dir") + File.separator + rrdGraphAttribute.getRrdRelativePath();

            query.append("DEF:")
                    .append(source.getLabel())
                    .append("=")
                    .append(rrdFile)
                    .append(":")
                    .append(source.getAttribute())
                    .append(":")
                    .append(source.getAggregation())
                    .append(" ");
        }

        StringBuilder command = new StringBuilder();
        command.append(rrdBinary).append(" ");
        command.append("xport").append(" ");
        command.append(query
                .toString()
                .replaceAll("[\r\n]+", " ")
                .replaceAll("\\s+", " "));

        String[] commandArray = StringUtils.createCommandArray(command.toString(), '@');

        /**
         * TODO: create class for the Xml unmarshalling...
        Object data = null;
        */

        try {
            Process process = Runtime.getRuntime().exec(commandArray);
            byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());
            String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));

            if (errors.length() > 0) {
                return null;
            }

            BufferedReader reader = null;

            try {
                InputStream is = new ByteArrayInputStream(byteArray);
                reader = new BufferedReader(new InputStreamReader(is));
                /**
                 * TODO: ...and use the class here
                data = (Object) Unmarshaller.unmarshal(Object.class, reader);
                */
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            throw new RrdException("exportRrd: can't execute command '" + command + ": ", e);
        }

        SortedMap<Long, Map<String, Double>> results = new TreeMap<Long, Map<String, Double>>();

        /**
         * TODO: construct the response object out of the unmarshalled xml data
         */
        return results;
	}
}
