<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:cmd="http://www.clarin.eu/cmd/"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    exclude-result-prefixes="xs"
    version="2.0"
    xpath-default-namespace="http://www.clarin.eu/cmd/">
    <!--    
    <!DOCTYPE html>
    -->

    <xsl:output
        method="html"
        encoding="UTF-8"
        doctype-system="about:legacy-compat"
        indent="yes"
        cdata-section-elements="td"/>
    
    <xsl:param 
        name="prune_Components_branches_without_text_values" 
        as="xs:boolean" 
        select='false()'/>

    <xsl:template 
        name="Component_tree" 
        match="/CMD/Components">
        <xsl:param 
            name="nodeset"
            as="element()+" 
            select="/CMD/Components"/>

        <ul>
            <xsl:for-each 
                select="$nodeset/element()">
                <xsl:variable 
                    name="subnodes_text" 
                    select="fn:normalize-space(fn:string-join(descendant-or-self::element()/text(), ''))" as="xs:string+"/>
                <xsl:if 
                    test="not($subnodes_text = '' and $prune_Components_branches_without_text_values)">
                    <xsl:variable 
                        name="nchildren" 
                        select="fn:count(child::element())"/>
                    <li>
                     
                        <code class="node">
                            <xsl:value-of 
                                select="fn:concat(local-name(), ' ')"/>
                            <xsl:if 
                                test="count(@*) > 0">
                                <div class="attributes">
                                    <xsl:for-each 
                                        select="@*">
                                        <xsl:value-of 
                                            select="name()"/>="<xsl:value-of select="."/>"
                                    </xsl:for-each>
                                </div>
                            </xsl:if>
                        </code>
                    
                        <xsl:choose>
                            <xsl:when 
                                test="$nchildren = 0 and not(not(child::node()))">
                                <!--<br /><br />-->
                                <div class="Component_tree_node_content">
                                    <xsl:choose>
                                        <xsl:when
                                            test="self::element() castable as xs:string">
                                            <xsl:variable
                                                name="leaf_value" 
                                                select="self::element() cast as xs:string"
                                                as="xs:string"/>                                         
                                            <xsl:variable
                                                name="is_URL"
                                                select="starts-with($leaf_value, 'http://') or starts-with($leaf_value, 'https://')"
                                                as="xs:boolean"/> 
                                            <code class="leaf">
                                                <xsl:choose>
                                                    <xsl:when 
                                                        test="$is_URL">
                                                        <a href="{$leaf_value}">
                                                            <xsl:value-of 
                                                                select="$leaf_value"/>
                                                        </a>
                                                    </xsl:when>
                                                    <xsl:otherwise>                                    
                                                        <xsl:value-of 
                                                            select="$leaf_value"/>                                         
                                                    </xsl:otherwise>                                 
                                                </xsl:choose>
                                            </code>
                                        </xsl:when>
                                        <xsl:otherwise>                                    
                                            <xsl:variable
                                                name="leaf_value" 
                                                select="format-number(self::element(), '#') cast as xs:string"
                                                as="xs:string"/>
                                            <xsl:variable
                                                name="is_URL"
                                                select="starts-with($leaf_value, 'http://') or starts-with($leaf_value, 'https://')"
                                                as="xs:boolean"/> 
                                            <code class="leaf">
                                                <xsl:choose>
                                                    <xsl:when 
                                                        test="$is_URL">
                                                    <a href="{$leaf_value}"><xsl:value-of select="$leaf_value"/></a>
                                                    </xsl:when>
                                                    <xsl:otherwise>                                    
                                                        <xsl:value-of 
                                                            select="$leaf_value"/>                                         
                                                    </xsl:otherwise>                                 
                                                </xsl:choose>
                                            </code>                  
                                        </xsl:otherwise>                                 
                                    </xsl:choose>
                                </div>
                            </xsl:when>
                            <xsl:otherwise>
                                <ul>
                                    <xsl:call-template 
                                        name="Component_tree">
                                        <xsl:with-param 
                                            name="nodeset" 
                                            select="self::element()"/>
                                    </xsl:call-template>
                                </ul>
                            </xsl:otherwise>
                        </xsl:choose>
                                     
                    </li>                                
                </xsl:if>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="CMD">
        <article style="background-color:#EEEEEE">
            <div class="endgame">
                <xsl:if test="not(not(Resources/*[normalize-space()]))"> 
                    <p>                    
                        <h1>Resources</h1>
                        <table>
                            <caption>Resources</caption>
                            <thead>
                                <tr>
                                    <th class="attribute">Reference to resource</th>
                                    <th class="attribute">Resource description</th>
                                    <th class="attribute">Resource MIME type</th>
                                    <th class="attribute">Resource Proxy ID</th>
                                </tr>
                            </thead>
                            <tbody class="attributesTbody">
                                <xsl:for-each 
                                    select="Resources/ResourceProxyList/ResourceProxy">
                                    <xsl:variable 
                                        name="URI"
                                        select="ResourceRef/text()"
                                        as="xs:string"/>
                                    <tr> 
                                        <td class="attributeValue">                                                
                                            <xsl:variable 
                                                name="protocol"
                                                select="fn:substring-before($URI, ':')"
                                                as="xs:string"/>
                                            <xsl:choose>
                                                <xsl:when 
                                                    test="$protocol = 'hdl'">
                                                    <xsl:variable 
                                                        name="HANDLE_PREFIX"
                                                        select="'http://hdl.handle.net'"
                                                        as="xs:string"/>                                                
                                                    <xsl:variable 
                                                        name="Handle_reference"
                                                        select="fn:substring-after($URI, ':')"
                                                        as="xs:string"/>
                                                    <xsl:variable 
                                                        name="Handle_HTTP_URL"
                                                        select="fn:concat($HANDLE_PREFIX, '/', $Handle_reference)"
                                                        as="xs:string"/>
                                                    <a href="{$Handle_HTTP_URL}">
                                                        <xsl:value-of
                                                            select="$Handle_HTTP_URL"/>
                                                    </a>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <a href="{ResourceRef}">
                                                        <xsl:value-of 
                                                            select="ResourceRef"/>
                                                    </a>
                                                </xsl:otherwise>
                                            </xsl:choose>                                                
                                        </td>
                                        <td class="attributeValue">
                                            <xsl:value-of select="ResourceType"/>
                                        </td>
                                        <td class="attributeValue">
                                            <xsl:value-of select="ResourceType/@mimetype"/>
                                        </td>
                                        <td class="attributeValue">
                                            <xsl:value-of select="./@id"/>
                                        </td>
                                    </tr>
                                </xsl:for-each>
                            </tbody>
                        </table>
                    </p>
                </xsl:if>
            </div>
                    
            <p>
                <h1>Metadata content</h1>
                <xsl:call-template name="Component_tree"/>
            </p>
        </article>
    </xsl:template>
</xsl:stylesheet>
