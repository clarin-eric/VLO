<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes" indent="yes" method="xml"/>
	<xsl:template match="nationalProjects">
		<mappings>
			<xsl:attribute name="field">nationalProject</xsl:attribute>
			<xsl:for-each-group select="nationalProjectMapping" group-by="NationalProject">
				<mapping>
					<normalizedValue>
						<xsl:attribute name="value">
							<xsl:value-of select="current-grouping-key()"/>
						</xsl:attribute>
					</normalizedValue>					
					<xsl:for-each select="current-group()">
					<variant>
						<xsl:attribute name="value">
								<xsl:value-of select="MdCollectionDisplayName"/>
							</xsl:attribute>
						<xsl:if test="MdCollectionDisplayName/@isRegExp">
							<xsl:attribute name="isRegExp">true</xsl:attribute>
						</xsl:if>
					</variant>						
				</xsl:for-each>
				</mapping>
			</xsl:for-each-group>
		</mappings>
	</xsl:template>
</xsl:stylesheet>