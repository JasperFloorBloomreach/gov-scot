package scot.gov.www.components;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.util.HstResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Arrays;

import static java.util.stream.Collectors.joining;

public class ArchiveUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ArchiveUtils.class);

    private ArchiveUtils() {
        // prevent instantiation
    }

    /**
     * Redirect to the same path on the old site.
     */
    public static void redirectToOldSite(HstRequest request, HstResponse response) {
        String path = request.getPathInfo();
        redirectToOldSite(path, request, response);
    }

    /**
     * Redirect to a given path on the old site.
     *
     * While beta still exists pages will be sent to the live gov.scot site.  When the new site goes live the redirects
     * will all go to www2.gov.scot.
     */
    public static void redirectToOldSite(String path, HstRequest request, HstResponse response) {
        String url = String.format("https://www2.gov.scot%s", path);
        LOG.info("Redirecting to archive {} -> {}", path, url);
        HstResponseUtils.sendRedirect(request, response, url);
    }

    public static boolean isArchivedUrl(HstRequest request)  {
        try {
            Session session = request.getRequestContext().getSession();
            String path = String.format("/content/redirects/HistoricalUrls%s", escapeJcrPath(request.getPathInfo()));
            if (path.endsWith("/")) {
                path = StringUtils.substringBeforeLast(path, "/");
            }
            return session.nodeExists(path);
        } catch (RepositoryException e) {
            LOG.error("Failed to find historical url redirect {}", request.getPathInfo(), e);
            return false;
        }
    }

    public static String escapeJcrPath(String path) {
        return Arrays.stream(path.split("/"))
                .filter(segment -> !StringUtils.equals(segment, ".."))
                .map(Text::escapeIllegalJcrChars)
                .collect(joining("/"));
    }

}
