# Adapted from nobel prize winners

from BaseCollection import BaseCollection
from html import *

class Collection(BaseCollection):
    PAGE_TITLE = 'CLARIN Virtual Language Observatory - Tools'
    PAGE_HEADING = 'CLARIN Virtual Language Observatory - Tools'
    PAGE_SUBHEADING = 'Demonstrator with CLARIN tools data and the NLP Software Registry from DFKI (contact: vlw@clarin.eu)'

    def __init__(self, db):
        self.db = db

        # Always show all the facets.
        self.facetlist = db.facetlist

        # Always show all the attributes
        self.attrlist =  db.attrlist

    def itemdisplay(self, item, request):
        
        metadata = self.db.metadata(int(item))
        
        refurl = '<a href="' + metadata['a_ref_link'] + '">' + metadata['a_ref_link'] + '</a>'
        
        servurl = '<a href="' + metadata['a_web_service'] + '">' + metadata['a_web_service'] + '</a>'
            
        return [br,
                "<b>Name: </b>", metadata['a_name'], br,
                "<b>Organisation: </b>", metadata['a_organisation'], br,
                "<b>Contact: </b>", metadata['a_contact'], br,
                "<b>Reference: </b>", refurl, br,
                "<b>Web Service: </b>", servurl
                ]
   
    def itemlisting(self, item, index, link=None, query=None, **args):

        metadata = self.db.metadata(int(item))
        listing = [abbrev(metadata['a_name'], 20)]
        if link:
            listing = link(listing, query=query, index=int(index))
        return listing
        

