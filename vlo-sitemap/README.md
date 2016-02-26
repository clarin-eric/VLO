# vlo-sitemap-gen
Google sitemap generator for CLARINs Virtual Language Observatory (https://vlo.clarin.eu/)

To generate sitemap call start.sh and pass the path to the configuration file (.properties). If argument is missing, the program will try to read the default one from the current folder.

For configuration options see config.properties

Sitemap will contain static URLs from config.properties and URLs for each record in form:
https://vlo.clarin.eu/record?docId=XXX

More about VLO project: https://github.com/clarin-eric/VLO
