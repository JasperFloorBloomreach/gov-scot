package scot.gov.www.components;

import org.hippoecm.hst.component.support.bean.BaseHstComponent;

import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.query.filter.Filter;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.content.beans.standard.HippoBeanIterator;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.util.ContentBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class TopicComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(TopicComponent.class);

    @Override
    public void doBeforeRender(final HstRequest request,
                               final HstResponse response) {
        HstRequestContext context = request.getRequestContext();
        HippoBean base = context.getSiteContentBaseBean();
        Topic topic = context.getContentBean(Topic.class);
        request.setAttribute("document", topic);

        populatePoliciesAndDirectorates(base, topic, request);
        populateNews(base, topic, request);
        populateConsultations(base, topic, request);
        populatePublications(base, topic, request);
    }

    private void populatePoliciesAndDirectorates(HippoBean base, Topic topic, HstRequest request) {
        try {
            HstQuery query = ContentBeanUtils.createIncomingBeansQuery(
                    topic,
                    base,
                    "*/@hippo:docbase",
                    Policy.class,
                    false);
            query.addOrderByAscending("govscot:title");
            HippoBeanIterator policies = executeQueryLoggingException(query, request, "policies");
            populatePolicies(policies, request);
        } catch (QueryException e) {
            LOG.warn("Unable to get policies for topic {}", topic.getPath(), e);
        }
    }

    private void populatePolicies(HippoBeanIterator policies, HstRequest request) {
        // populate the directorates responsible for policies into a map - this will remove any duplicates
        Map<String, HippoBean> directoratesById = new HashMap<>();
        while (policies.hasNext()) {
            Policy policy = (Policy) policies.nextHippoBean();
            directoratesById.put(policy.getIdentifier(), policy.getResponsibleDirectorate());
            for (HippoBean directorate : policy.getSecondaryResponsibleDirectorate()) {
                directoratesById.put(directorate.getIdentifier(), directorate);
            }
        }

        // now add them to a list and sort them by name
        List<HippoBean> directorates = directoratesById.values()
                .stream()
                .sorted(comparing(HippoBean::getName))
                .collect(toList());
        request.setAttribute("directorates", directorates);
    }

    private void populateNews(HippoBean base, Topic topic, HstRequest request) {
        HstQuery query = topicLinkedBeansQuery(topic, base, News.class);
        query.addOrderByDescending("govscot:publishedDate");
        query.setLimit(3);

        try {
            executeQueryLoggingException(query, request, "news");
        } catch (QueryException e) {
            LOG.warn("Unable to get News for topic {}", topic.getPath(), e);
        }
    }

    private void populateConsultations(HippoBean base, Topic topic, HstRequest request) {
        HstQuery query = topicLinkedBeansQuery(topic, base, Publication.class);
        query.addOrderByDescending("govscot:publicationDate");
        query.setLimit(3);

        try {
            Filter filter = query.createFilter();
            filter.addContains("govscot:publicationType", "consultation-paper");
            executeQueryLoggingException(query, request, "consultations");
            ((Filter) query.getFilter()).addAndFilter(filter);
        } catch (QueryException e) {
            LOG.error("Unable to get Consultations for topic {}", topic.getPath(), e);
        }
    }

    private void populatePublications(HippoBean base, Topic topic, HstRequest request) {
        HstQuery query = topicLinkedBeansQuery(topic, base, Publication.class);
        query.addOrderByDescending("govscot:publicationDate");
        query.setLimit(3);

        try {
            Filter filter = query.createFilter();
            filter.addNotContains("govscot:publicationType", "consultation-paper");
            ((Filter) query.getFilter()).addAndFilter(filter);
            executeQueryLoggingException(query, request, "publications");
        } catch (QueryException e) {
            LOG.error("Unable to get Publications for topic {}", topic.getPath(), e);
        }
    }

    private HstQuery topicLinkedBeansQuery(Topic topic, HippoBean base, Class linkedClass) {
        try {
            return ContentBeanUtils.createIncomingBeansQuery(
                    topic,
                    base,
                    "*/@hippo:docbase",
                    linkedClass,
                    false);
        } catch (QueryException e) {
            LOG.warn("Unable to get linked beans for topic {}", topic.getPath(), e);
            return null;
        }
    }

    private HippoBeanIterator executeQueryLoggingException(
            HstQuery query,
            HstRequest request,
            String name)
            throws QueryException {
        HstQueryResult result = query.execute();
        request.setAttribute(name, result.getHippoBeans());
        return result.getHippoBeans();
    }

}