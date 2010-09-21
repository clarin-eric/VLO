# C. Zinn, Summer 2009
from BaseCollection import BaseCollection
from html import *

class Collection(BaseCollection):
    PAGE_TITLE = 'CLARIN Virtual Language Observatory - Resources'
    PAGE_HEADING = 'CLARIN Virtual Language Observatory - Resources'
    PAGE_SUBHEADING = 'Demonstrator with IMDI, OLAC, ELRA and CLARIN data (contact: vlw@clarin.eu)'

    def __init__(self, db):
        self.db = db

        # Always show all the facets.
        self.facetlist = db.facetlist

        # Always show all the attributes
        self.attrlist =  db.attrlist

    def itemdisplay(self, item, request):                                                                                                 
        metadata = self.db.metadata(item)

        # IMDI BROWSER URL (default)
        url = '<a href="' + 'http://catalog.clarin.eu/ds/imdi_browser/?openpath=MPI' + metadata['nodeId'].replace(' ','')+'%23">open IMDI Browser</a>'       

        corpus = metadata['topNode'].replace(' ','').replace(' ', '')
        if corpus =='clarin' :
            url='<a href="' + 'http://www.clarin.eu/node/' + metadata['nodeId'].replace(' ','') + '">open CLARIN Browser</a>'

        if corpus =='elra' :
            url=''

        return [ metadata['name'], br,
                 metadata['topNode'], br,                                                                                                
                 metadata['country'], br,                                                                                                
                 url,
                 ]
                 
    def itemlisting(self, item, index, link=None, query=None, **args):

        metadata = self.db.metadata(long(item))
        listing = [abbrev(metadata['name'], 20)]
        if link:
            listing = link(listing, query=query, index=long(index))
        return listing






