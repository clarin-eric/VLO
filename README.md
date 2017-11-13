# Table of Contents
- What is the VLO? 
- What should I read? 
- Setting up
	- Preparing for release
- Using the VLO
	- Running an import
	
# What is the VLO?

Using the VLO faceted browser, you can browse metadata by facet. It consists of three
software components: a Solr server with VLO specific configuration, an importer and a
web application front end.

For more information, see the [wiki page](https://trac.clarin.eu/wiki/CmdiVirtualLanguageObservatory).

# What should I read?

- [README](README.md)
	This file; a general introduction
- [DEVELOPMENT](DEVELOPMENT.md)
	Information for developers
- [DEPLOYMENT](DEPLOYMENT.md)
	Instructions on how to deploy a fresh VLO installation
- [UPGRADE](UPGRADE.txt)
	Instructions on how to upgrade an existing VLO installation
- [CHANGES](CHANGES.txt)
	A list of changes per release
- [COPYING](COPYING.txt)
	Licensing information

# Running the VLO 

After a successful [deployment](DEPLOYMENT.md) and configuration, you should be able to
browse to the VLO web app and browse the imported records. You can also run the VLO
locally using the docker image (see [compose_vlo](https://gitlab.com/CLARIN-ERIC/compose_vlo))

If the VLO is empty, you will need to run an import first. 

## Running an import 

To run an import, go the `bin` child directory of the VLO application directory 
and run

`./vlo_solr_importer.sh`
	
as the appropriate user (e.g. `vlouser`). 

It's advisable to run this in a detached background process (for example using 
`screen`) because an import can take quite a long time depending on the amount
of records to be imported. Also make sure that enough memory is available. 
Some VM parameters are configured inside the script.

Some progress information is logged to a file `log/vlo-importer.log`. It gets
rotated automatically by the import process.

For a __fresh import__, even when the VLO is not configured to delete all documents
from the index first, you can simply remove the contents of the Solr data 
directory (check server configuration documentation for the exact location).
