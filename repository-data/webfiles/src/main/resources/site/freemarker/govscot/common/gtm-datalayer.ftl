<#ftl output_format="HTML">

<#if document??>
<@hst.headContribution category="dataLayer">
<script id="datalayerPush">

    <#if !dateCreated??><#assign dateCreated = document.getProperty('hippostdpubwf:creationDate')/></#if>
    <#if !lastUpdated??><#assign lastUpdated = document.getProperty('hippostdpubwf:lastModificationDate')/></#if>
    <#if !uuid??><#assign uuid = document.getProperty('jcr:uuid')/></#if>
    <#if !reportingTags?? && document.getProperty('govscot:reportingTags')??>
      <#assign reportingTags = document.getProperty('govscot:reportingTags')/>
    </#if>

    window.dataLayer = window.dataLayer || [];
    window.dataLayer.push({
        <#if document.responsibleRole??>
            'responsibleRole': '${document.responsibleRole.title?json_string}',
        </#if>
        <#if document.secondaryResponsibleRole?has_content>
            'secondaryResponsibleRole': [<#list document.secondaryResponsibleRole as role>'${role.title?json_string}'<#sep>, </#sep></#list>],
        </#if>
        <#if document.responsibleDirectorate??>
            'responsibleDirectorate': '${document.responsibleDirectorate.title?json_string}',
        </#if>
        <#if document.secondaryResponsibleDirectorate?has_content>
            'secondaryResponsibleDirectorate': [<#list document.secondaryResponsibleDirectorate as dir>'${dir.title}'<#sep>, </#sep></#list>],
        </#if>
        <#if document.topics?has_content>
            'topics': [<#list document.topics as topic>'${topic.title?json_string}'<#sep>, </#sep></#list>],
        </#if>
        <#if document.publicationDate??>
            'publicationDate': '<@fmt.formatDate value=document.publicationDate.time type="Date" pattern="dd/MM/yyyy" />',
        </#if>
        <#if reportingTags?has_content>
            'reportingTags': [<#list reportingTags as tag>'${tag?json_string}'<#sep>, </#sep></#list>],
        </#if>
        <#if policies?has_content>
            'policies': [<#list policies as policy>'${policy?json_string}'<#sep>, </#sep></#list>],
        </#if>
        <#if collections?has_content>
            'collections': [<#list collections as collection>'${collection.title?json_string}'<#sep>, </#sep></#list>],
        </#if>
        'lastUpdated': '<@fmt.formatDate value=lastUpdated.time type="Date" pattern="dd/MM/yyyy" />',
        'dateCreated': '<@fmt.formatDate value=dateCreated.time type="Date" pattern="dd/MM/yyyy" />',
        'uuid': '${uuid}'
    })
</script>
</@hst.headContribution>
</#if>