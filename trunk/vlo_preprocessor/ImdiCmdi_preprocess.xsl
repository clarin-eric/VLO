<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="xml" indent="yes"/>

	<xsl:param name="inputDir"></xsl:param><!-- e.g. /lat/tools/vlo_importer/data/imdi/imdi-20110106/corpora -->
	<xsl:param name="organisationsFile"></xsl:param> <!-- e.g. /lat/tools/vlo_importer/data/imdi/OrganisationControlledVocabulary.xml -->

    <xsl:variable name="organisations" select="doc($organisationsFile)"/>
    <xsl:variable name="newLine" select="system-property('line.separator')"/>

    <xsl:template match="Organisation">
        <xsl:variable name="org" select="normalize-space(.)"/>
        <xsl:variable name="correctOrganisation"
            select="$organisations/Organisations/Organisation[Variation=$org]/@name"/>
        <Organisation>
            <xsl:choose>
                <xsl:when test="string-length($correctOrganisation)>1">
                    <xsl:value-of select="$correctOrganisation"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
        </Organisation>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="main">
        <xsl:variable name="inputImdis" select="concat('file://', $inputDir, '?select=*.cmdi;recurse=yes;on-error=ignore')" />
        <xsl:for-each
            select="collection($inputImdis)">
            <xsl:result-document href="{document-uri(.)}">
                <xsl:comment>
                    <xsl:text>Preprocessed by version 0.1 of the VLO preprocessor</xsl:text>
                </xsl:comment>
                <xsl:value-of select="$newLine"/>
                <xsl:apply-templates select="."/>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
