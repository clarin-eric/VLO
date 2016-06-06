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

        <div class="component">
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
                     
                    <div class="node">
                        <strong>
                            <xsl:value-of 
                                select="fn:concat(local-name(), ' ')"/>
                        </strong>
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
                    </div>
                    
                    <xsl:choose>
                        <xsl:when 
                            test="$nchildren = 0 and not(not(child::node()))">
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
                                    <div class="leaf">
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
                                    </div>
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
                                    <div class="leaf">
                                        <xsl:choose>
                                            <xsl:when 
                                                test="$is_URL">
                                                <a href="{$leaf_value}">
                                                    <xsl:value-of select="$leaf_value"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>                                    
                                                <xsl:value-of 
                                                    select="$leaf_value"/>                                         
                                            </xsl:otherwise>                                 
                                        </xsl:choose>
                                    </div>                  
                                </xsl:otherwise>                                 
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <div class="children">
                                <xsl:call-template 
                                    name="Component_tree">
                                    <xsl:with-param 
                                        name="nodeset" 
                                        select="self::element()"/>
                                </xsl:call-template>
                            </div>
                                
                        </xsl:otherwise>
                    </xsl:choose>                        
                </xsl:if>
            </xsl:for-each>
        </div>
    </xsl:template>

    <xsl:template match="CMD">
        <article> 
            <xsl:call-template name="Component_tree"/>
        </article>
    </xsl:template>
</xsl:stylesheet>
