<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:cmd="http://www.clarin.eu/cmd/"
    xmlns:fn="http://www.w3.org/2005/xpath-functions" exclude-result-prefixes="xs" version="2.0"
    xpath-default-namespace="http://www.clarin.eu/cmd/">
    <!--    
    <!DOCTYPE html>
    -->

    <xsl:output method="html" encoding="UTF-8" doctype-system="about:legacy-compat" indent="yes"
        cdata-section-elements="td"/>

    <xsl:param name="prune_Components_branches_without_text_values" as="xs:boolean" select="false()"/>

    <xsl:template mode="Component_Child" match="*">
        <xsl:variable name="subnodes_text"
            select="fn:normalize-space(fn:string-join(descendant-or-self::element()/text(), ''))"
            as="xs:string+"/>
        <xsl:if test="not($subnodes_text = '' and $prune_Components_branches_without_text_values)">
            <xsl:variable name="nchildren" select="fn:count(child::element())"/>

            <div>
                <xsl:choose>
                    <xsl:when test="$nchildren = 0">
                        <!-- leaf -->
                        <xsl:attribute name="class">node leaf</xsl:attribute>
                        <span class="node_title">
                            <xsl:value-of select="local-name()"/>
                        </span>
                        <span class="node_content">
                            <xsl:apply-templates mode="attributes" select="."/>
                            <xsl:if test="not(not(child::node()))">
                                <xsl:apply-templates mode="leaf" select="."/>
                            </xsl:if>
                        </span>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- parent -->
                        <xsl:attribute name="class">node parent panel panel-default</xsl:attribute>
                        <div class="panel-heading node_title">
                            <xsl:apply-templates select="@ref"/>
                            <xsl:value-of select="local-name()"/>
                            <xsl:apply-templates mode="attributes" select="."/>
                        </div>
                        <div class="panel-body node_content">
                            <xsl:if test="child::node()">
                                <div class="node_children">
                                    <xsl:apply-templates mode="Component_Child" select="*"/>
                                </div>
                            </xsl:if>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@ref">
        <xsl:variable name="resourceRef" select="//ResourceProxy[@id=current()]/ResourceRef"/>
        <xsl:if test="$resourceRef">
            <a class="node_resource_ref">
                <xsl:attribute name="href" select="replace($resourceRef, '^hdl:', 'http://hdl.handle.net/')"/>
                <span class="glyphicon glyphicon-file">
                    <xsl:attribute name="title" select="$resourceRef"/>
                </span>
            </a>
        </xsl:if>
        <xsl:text> </xsl:text>
    </xsl:template>

    <xsl:template mode="attributes" match="*">
        <xsl:if test="count(@*[name() != 'ref']) > 0">
            <div class="node_attributes">
                <xsl:for-each select="@*">
                    <span class="node_attribute">
                        <xsl:value-of select="name()"/>="<xsl:value-of select="."/>" </span>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template mode="leaf" match="*">
        <xsl:choose>
            <xsl:when test="self::element() castable as xs:string">
                <xsl:variable name="leaf_value" select="self::element() cast as xs:string"
                    as="xs:string"/>
                <xsl:variable name="is_URL"
                    select="starts-with($leaf_value, 'http://') or starts-with($leaf_value, 'https://')"
                    as="xs:boolean"/>
                <span class="node_value">
                    <xsl:choose>
                        <xsl:when test="$is_URL">
                            <a href="{$leaf_value}">
                                <xsl:value-of select="$leaf_value"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$leaf_value"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="leaf_value"
                    select="format-number(self::element(), '#') cast as xs:string" as="xs:string"/>
                <xsl:variable name="is_URL"
                    select="starts-with($leaf_value, 'http://') or starts-with($leaf_value, 'https://')"
                    as="xs:boolean"/>
                <span class="node_value">
                    <xsl:choose>
                        <xsl:when test="$is_URL">
                            <a href="{$leaf_value}">
                                <xsl:value-of select="$leaf_value"/>
                            </a>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$leaf_value"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="CMD">
        <article class="cmdi-record">
            <xsl:apply-templates select="/CMD/Components/*" mode="Component_Child"/>
        </article>
    </xsl:template>
</xsl:stylesheet>
