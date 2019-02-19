package scot.gov.www.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

import java.util.Calendar;

@HippoEssentialsGenerated(internalName = "govscot:FOI")
@Node(jcrType = "govscot:FOI")
public class FOI extends AttributableContent {
    @HippoEssentialsGenerated(internalName = "govscot:foiNumber")
    public String getFoiNumber() {
        return getProperty("govscot:foiNumber");
    }

    @HippoEssentialsGenerated(internalName = "govscot:dateReceived")
    public Calendar getDateReceived() {
        return getProperty("govscot:dateReceived");
    }

    @HippoEssentialsGenerated(internalName = "govscot:dateResponded")
    public Calendar getDateResponded() {
        return getProperty("govscot:dateResponded");
    }

    @HippoEssentialsGenerated(internalName = "govscot:title")
    public String getTitle() {
        return getProperty("govscot:title");
    }

    @HippoEssentialsGenerated(internalName = "govscot:summary")
    public String getSummary() {
        return getProperty("govscot:summary");
    }

    @HippoEssentialsGenerated(internalName = "govscot:seoTitle")
    public String getSeoTitle() {
        return getProperty("govscot:seoTitle");
    }

    @HippoEssentialsGenerated(internalName = "govscot:metaDescription")
    public String getMetaDescription() {
        return getProperty("govscot:metaDescription");
    }

    @HippoEssentialsGenerated(internalName = "hippostd:tags")
    public String[] getTags() {
        return getProperty("hippostd:tags");
    }

    @HippoEssentialsGenerated(internalName = "govscot:isbn")
    public String getIsbn() {
        return getProperty("govscot:isbn");
    }

    @HippoEssentialsGenerated(internalName = "govscot:publicationDate")
    public Calendar getPublicationDate() {
        return getProperty("govscot:publicationDate");
    }

    @HippoEssentialsGenerated(internalName = "govscot:publicationType")
    public String getPublicationType() {
        return getProperty("govscot:publicationType");
    }

    @HippoEssentialsGenerated(internalName = "govscot:sme")
    public String getSme() {
        return getProperty("govscot:sme");
    }

    @HippoEssentialsGenerated(internalName = "govscot:request")
    public HippoHtml getRequest() {
        return getHippoHtml("govscot:request");
    }

    @HippoEssentialsGenerated(internalName = "govscot:response")
    public HippoHtml getResponse() {
        return getHippoHtml("govscot:response");
    }

    @HippoEssentialsGenerated(internalName = "govscot:notes")
    public HippoHtml getNotes() {
        return getHippoHtml("govscot:notes");
    }

    @HippoEssentialsGenerated(internalName = "govscot:contact")
    public HippoHtml getContact() {
        return getHippoHtml("govscot:contact");
    }

    public String getLabel() { return "FOI/EIR Release"; }
}
