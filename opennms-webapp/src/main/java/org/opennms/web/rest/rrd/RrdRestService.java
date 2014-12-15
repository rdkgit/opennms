package org.opennms.web.rest.rrd;

import com.sun.jersey.spi.resource.PerRequest;

import org.apache.commons.jexl2.*;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.web.rest.OnmsRestService;
import org.opennms.web.rest.rrd.fetch.RrdFetchStrategy;
import org.opennms.web.rest.rrd.fetch.RrdFetchStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.*;

@Component
@PerRequest
@Scope("prototype")
@Path("/rrd")
public class RrdRestService extends OnmsRestService {
    @Autowired
    private ResourceDao m_resourceDao;

    @Autowired
    private RrdFetchStrategyFactory m_fetchStrategyFactory;

    private final RrdFetchStrategy m_fetchStrategy;

    public RrdRestService() {
    	m_fetchStrategy = m_fetchStrategyFactory.getFetchStrategy();
    }

    @POST
    @Path("/")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Response query(final QueryRequest request) throws Exception {
        readLock();
        try {
            // Compile the expressions
            final JexlEngine jexl = new JexlEngine();
            final LinkedHashMap<String, Expression> expressions = new LinkedHashMap<String, Expression>();
            for (final QueryRequest.Expression e : request.getExpressions()) {
                expressions.put(e.getLabel(),
                                jexl.createExpression(e.getExpression()));
            }

            // Prepare the response
            final QueryResponse response = new QueryResponse();
            response.setStep(request.getStep());
            response.setStart(request.getStart());
            response.setEnd(request.getEnd());

            // Fetch the data
            final SortedMap<Long, Map<String, Double>> data = m_fetchStrategy.fetch(
            		request.getStep(),
                    request.getStart(),
                    request.getEnd(),
                    request.getSources());

            // Do the calculations and build the list of resulting metrics
            final List<QueryResponse.Metric> metrics = new ArrayList<QueryResponse.Metric>(data.size());
            for (final SortedMap.Entry<Long, Map<String, Double>> dataEntry : data.entrySet()) {
                Map<String, Double> values = dataEntry.getValue();

                for (final Map.Entry<String, Expression> expressionEntry : expressions.entrySet()) {
                    final JexlContext context = new MapContext(new HashMap<String, Object>(values));

                    values.put(expressionEntry.getKey(),
                               (Double) expressionEntry.getValue().evaluate(context));
                }

                final QueryResponse.Metric metric = new QueryResponse.Metric();
                metric.setTimestamp(dataEntry.getKey());
                metric.setValues(values);
                metrics.add(metric);
            }

            // Complete the response
            response.setMetrics(metrics);

            return Response
                    .ok(response)
                    .build();

        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("/{resourceId}")
    @Produces({MediaType.TEXT_PLAIN})
    @Consumes(MediaType.TEXT_PLAIN)
    public Response info(@PathParam("resourceId") final String resourceId) {
        readLock();
        try {

            OnmsResource resource = m_resourceDao.getResourceById(resourceId);

            String result = "";

            result += "<h1>" + m_fetchStrategy.getClass().getSimpleName() + "</h1>";

            result += resource.getId() + "<br>";
            result += resource.getLabel() + "<br>";
            result += resource.getLink() + "<br>";
            result += resource.getName() + "<br>";
            result += resource.getResourceType().getName() + "<br>";
            result += resource.getResourceType().getLabel() + "<br>";

            result += "<h1>Attributes</h1>";

            for (OnmsAttribute onmsAttribute : resource.getAttributes()) {
                result += onmsAttribute.getName() + " " + onmsAttribute.getResource() + "<br>";
            }

            result += "<h1>Child resources</h1>";

            for (OnmsResource onmsResource : resource.getChildResources()) {
                result += onmsResource + "<br>";
            }

            result += "<h1>External Values</h1>";

            for (Map.Entry<String, String> entry : resource.getExternalValueAttributes().entrySet()) {
                result += entry.getKey() + "=" + entry.getValue() + "<br>";
            }

            result += "<h1>Rrd Graph Attributes</h1>";

            for (Map.Entry<String, RrdGraphAttribute> entry : resource.getRrdGraphAttributes().entrySet()) {
                result += entry.getKey() + "=" + entry.getValue().getName() + "/" + entry.getValue().getRrdRelativePath() + "/" + entry.getValue().getResource().getName() + "<br>";
            }

            result += "<h1>String Property Attributes</h1>";

            for (Map.Entry<String, String> entry : resource.getStringPropertyAttributes().entrySet()) {
                result += entry.getKey() + "=" + entry.getValue() + "<br>";
            }

            return Response.ok(result, "text/html").build();
        } finally {
            readUnlock();
        }
    }
}
