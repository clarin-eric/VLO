<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output omit-xml-declaration="yes" indent="yes" method="xml"/>
	<xsl:template match="LanguageNames">
		<mappings>		
			<xsl:attribute name="field">languageCode</xsl:attribute>
			<xsl:for-each select="Language">				
				<mapping>
					<normalizedValue>
						<xsl:attribute name="value">
							<xsl:value-of select="@name"/>
						</xsl:attribute>
					</normalizedValue>					
					<xsl:for-each select="Variation">
						<variant>
							<xsl:attribute name="value">
								<xsl:value-of select="."/>
							</xsl:attribute>
						</variant>						
					</xsl:for-each>				
				</mapping>				
			</xsl:for-each>
		</mappings>		
	</xsl:template>
</xsl:stylesheet>