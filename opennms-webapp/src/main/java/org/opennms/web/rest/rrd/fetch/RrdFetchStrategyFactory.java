package org.opennms.web.rest.rrd.fetch;

import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.rrd.MultiOutputRrdStrategy;
import org.opennms.netmgt.rrd.QueuingRrdStrategy;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.rrd.rrdtool.JniRrdStrategy;
import org.springframework.beans.factory.annotation.Autowired;

public class RrdFetchStrategyFactory {
    @Autowired
    private ResourceDao m_resourceDao;

	public RrdFetchStrategy getFetchStrategy() {
		RrdStrategy<?, ?> strategy = findRrdStrategy();

		if (strategy instanceof JniRrdStrategy) {
			return new JniRrdFetchStrategy(m_resourceDao);
        } else if (findRrdStrategy() instanceof JRobinRrdStrategy) {
        	return new JrbRrdFetchStrategy(m_resourceDao);
        } else {
            throw new RuntimeException("No appropriate RRD strategy found");
        }
	}

	private static RrdStrategy<?, ?> findRrdStrategy() {
        return findRrdStrategy(RrdUtils.getStrategy());
    }

    private static RrdStrategy<?, ?> findRrdStrategy(final RrdStrategy<?, ?> rrdStrategy) {
        if (rrdStrategy instanceof JniRrdStrategy || rrdStrategy instanceof JRobinRrdStrategy) {
            return rrdStrategy;
        }

        if (rrdStrategy instanceof QueuingRrdStrategy) {
            return findRrdStrategy(((QueuingRrdStrategy) rrdStrategy).getDelegate());
        }

        if (rrdStrategy instanceof MultiOutputRrdStrategy) {
            for (final RrdStrategy<?, ?> delegate : ((MultiOutputRrdStrategy) rrdStrategy).getDelegates()) {
                RrdStrategy<?, ?> x = findRrdStrategy(delegate);

                if (x instanceof JniRrdStrategy || x instanceof JRobinRrdStrategy) {
                    return x;
                }
            }
        }

        return rrdStrategy;
    }
}
