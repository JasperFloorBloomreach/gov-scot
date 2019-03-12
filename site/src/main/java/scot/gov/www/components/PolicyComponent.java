package scot.gov.www.components;

import org.apache.commons.lang.time.StopWatch;
import org.hippoecm.hst.component.support.bean.BaseHstComponent;
import org.hippoecm.hst.content.beans.query.HstQuery;
import org.hippoecm.hst.content.beans.query.HstQueryResult;
import org.hippoecm.hst.content.beans.query.builder.Constraint;
import org.hippoecm.hst.content.beans.query.builder.HstQueryBuilder;
import org.hippoecm.hst.content.beans.query.exceptions.QueryException;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scot.gov.www.beans.News;
import scot.gov.www.beans.Policy;
import scot.gov.www.beans.PolicyInDetail;
import scot.gov.www.beans.PolicyLatest;

import java.io.IOException;
import java.util.*;

import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.constraint;
import static org.hippoecm.hst.content.beans.query.builder.ConstraintBuilder.or;

/**
 * Get the information required to render a Policy or Policy in Detail page.
 */
public class PolicyComponent extends BaseHstComponent {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyComponent.class);

    @Override
    public void doBeforeRender(final HstRequest request, final HstResponse response) {
        HippoBean document;

        try {
            document = request.getRequestContext().getContentBean();
            if(document == null) {
                response.setStatus(404);
                response.forward("/pagenotfound");
                return;
            }
        }  catch (IOException e) {
            throw new HstComponentException("forward failed", e);
        }

        Policy policy;
        try {
            policy = getPolicy(document);

            if(policy == null) {
                response.setStatus(404);
                response.forward("/pagenotfound");
                return;
            }

            LOG.info("policy {}", policy.getPath());
        }  catch (IOException e) {
            throw new HstComponentException("forward failed", e);
        }

        List<PolicyInDetail> details = document.getParentBean().getChildBeans(PolicyInDetail.class);
        HippoBean prev = prevBean(policy, document, details);
        HippoBean next = nextBean(policy, document, details);
        request.setAttribute("document", document);
        request.setAttribute("index", policy);
        request.setAttribute("policyDetails", details);
        request.setAttribute("prev", prev);
        request.setAttribute("next", next);

        // if this is the patest page then also include latest info
        if (request.getPathInfo().endsWith("/latest/")) {
            List<HippoBean> all = getLatestNews(request, policy);
            PolicyLatest latest = (PolicyLatest) document;
            all.addAll(latest.getRelatedItems());
            Collections.sort(all, this::compareDateIfNoNull);
            Collections.reverse(all);
            LOG.info("{} related items, total size is {}", latest.getRelatedItems().size(), all.size());
            request.setAttribute("latest", all);
        }
    }

    private int compareDateIfNoNull(HippoBean left, HippoBean right) {
        return dateToCompare(left).compareTo(dateToCompare(right));
    }

    Calendar dateToCompare(HippoBean bean) {
        Calendar publicationDate = bean.getProperty("govscot:publicationDate");
        if (publicationDate != null) {
            return publicationDate;
        }

        // this bean has no publication date, default to the hippostdpubwf:publicationDate
        return bean.getProperty("hippostdpubwf:publicationDate");
    }

    private List<HippoBean> getLatestNews(HstRequest request, Policy policy) {
        if (policy.getPolicyTags().length == 0) {
            return new ArrayList<>();
        }

        HippoBean scope = request.getRequestContext().getSiteContentBaseBean();
        HstQuery query = HstQueryBuilder.create(scope)
                .ofTypes(News.class)
                .where(or(tagConstraints(policy)))
                .orderByDescending("govscot:publicationDate").build();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            HstQueryResult result = query.execute();
            stopWatch.stop();

            List<HippoBean> all = new ArrayList<>();
            result.getHippoBeans().forEachRemaining(all::add);
            return all;
        } catch (QueryException e) {
            throw new HstComponentException(e);
        }
    }

    private Constraint[] tagConstraints(Policy policy) {
        ArrayList<Constraint> tagConstraints = new ArrayList<>();
        for (String tag : policy.getPolicyTags()) {
            LOG.info("policy tag: {}", tag);
            tagConstraints.add(constraint("govscot:policyTags").equalTo(tag));
        }
        return tagConstraints.toArray(new Constraint[tagConstraints.size()]);
    }

    private Policy getPolicy(HippoBean document) {
        HippoBean parent = document.getParentBean();
        List<Policy> policies = parent.getChildBeans(Policy.class);
        if (policies.isEmpty()) {
            LOG.info("No policy found under {}", document.getPath());
        }
        if (policies.size() > 1) {
            LOG.info("More than one policy found under {}, will use first", document.getPath());
        }
        return policies.get(0);
    }

    private HippoBean prevBean(HippoBean policy, HippoBean document, List<PolicyInDetail> details) {

        if ("latest".equals(document.getName())) {
            return policy;
        }

        // if the document being rendered is the policy, then there is no previous bean
        if (document.isSelf(policy)) {
            return null;
        }

        int index = details.indexOf(document);
        if (index == 0) {
            // for the first details page the prev is the policy
            return policy;
        }

        return details.get(index - 1);
    }

    private HippoBean nextBean(HippoBean policy, HippoBean document, List<PolicyInDetail> details) {

        if ("latest".equals(document.getName())) {
            return details.isEmpty() ? null : details.get(0);
        }

        // if the document being rendered is the policy, return the first details page (if there is one)
        if (document.isSelf(policy)) {
            return nextBeanForPolicy(details);
        }

        // if this is the last details page then next is null
        int index = details.indexOf(document);
        if (index == details.size() - 1) {
            return null;
        }

        return details.get(index + 1);
    }

    private HippoBean nextBeanForPolicy(List<PolicyInDetail> details) {
        if (details.isEmpty()) {
            return null;
        }
        return details.get(0);
    }

}