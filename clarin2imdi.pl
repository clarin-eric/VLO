#!/usr/bin/perl
# Author: Claus Zinn
# modified by: Alexander Koenig
#
# converting the CLARIN metadata as downloaded from http://www.clarin.eu/documents/lrt-inventory to flamenco input scheme
# input file should be named "view-export_resources.csv" )

use strict;
use warnings;
use File::Spec::Functions;
use Text::CSV_XS;

# MAPPING:  CLARIN RESOURCES -----> IMDI METADATA SCHEME
#
# CLARIN RESOURCES SCHEME
# -----------------------
#
# 0 name                      -> 2 	(name)
# 1 resource_type             -> 15 (genre)
# 2 res_languages             -> 28 (languageName)
# 3 res_languages_other       IGNORED
# 4 description               -> 9  (projecttitle)
# 5 res_country               -> 6 	(country)
# 6 institution               -> 14	(contactorganisation)
# 7 creator                   -> 11	(contactname)
# 8 year                      -> 4 	(date)
# 9 end_creation_date         IGNORED
# 10 res_format               IGNORED
# 11 metadata_link            IGNORED
# 12 publications             IGNORED
# 13 reference_link           -> 25	(refs)
# 14 ethical_reference        IGNORED
# 15 legal_reference          IGNORED
# 16 res_license              IGNORED 
# 17 description_0            IGNORED
# 18 contact_person           IGNORED
# 19 long_term_preservation   IGNORED
# 20 working_languages        IGNORED
# 21 location_0               -> 12	(contactaddress)     
# 22 content_type             IGNORED
# 23 format_detailed          IGNORED
# 24 quality                  IGNORED
# 25 applications             IGNORED
# 26 project                  -> 8	(projectname)
# 27 size                     IGNORED
# 28 distribution_form        IGNORED
# 29 access                   IGNORED
# 30 source_0                 IGNORED
# 31 date_0                   IGNORED
# 32 type                     IGNORED
# 33 format_detailed_1        IGNORED
# 34 schema_reference         IGNORED
# 35 size_0                   IGNORED
# 36 working_languages_0      IGNORED
# 37 access_1                 IGNORED
# 38 date_2                   IGNORED
# 39 readilyAvail             IGNORED
# 40 urlCheck                 IGNORED
# 41 nid                      -> 1	(nodeId)
# 42 org1				      IGNORED
# 43 org2  				      IGNORED
# 44 org3			          IGNORED
# 45 org4      				  IGNORED
# 46 org5    			      IGNORED


my $basedir = "/home/alekoe/projects/flamenco/fromperl";

# open source and target file
open (my $src_file, "<", catfile($basedir,"view-export_resources.csv")) or die("Cannot open source file view-export_resources.csv: $!");
open (my $target_file, ">", catfile($basedir,"imdi_clarin.csv")) or die("Cannot open target file imdi_clarin.csv: $!");


my $csv = Text::CSV_XS->new ({ binary => 1, eol => $/ });

 while (my $clarin_line = $csv->getline ($src_file)) 
 {
     my @clarin_fields = @$clarin_line;
 
    # we only need a couple of fields, and they need to be sanitized
	my $name = clean_up($clarin_fields[0]); # 0 name             -> 2 	(name)
	my $resource_type = clean_up($clarin_fields[1]); # 1 resource_type    -> 15 (genre)
	my $res_languages = clean_up($clarin_fields[2]); # 2 res_languages    -> 28 (languageName)
	my $description = clean_up($clarin_fields[4]); # 4 description      -> 9  (projecttitle)
	my $res_country = clean_up($clarin_fields[5]); # 5 res_country      -> 6 	(country)
	my $institution = clean_up($clarin_fields[6]); # 6 institution      -> 14	(contactorganisation)
	my $creator = clean_up($clarin_fields[7]); # 7 creator          -> 11	(contactname)
	my $year = clean_up($clarin_fields[8]); # 8 year             -> 4 	(date)
	my $reference_link = clean_up($clarin_fields[13]); # 13 reference_link	-> 25	(refs)
	my $location_0 = clean_up($clarin_fields[21]); # 21 location_0      -> 12	(contactaddress)     
	my $project = clean_up($clarin_fields[26]); # 26 project         -> 8	(projectname)
	my $nid = clean_up($clarin_fields[41]); # 41 nid             -> 1	(nodeId)

	# we don't want the first line
	next if $resource_type eq 'Resource Type (field_resource_type)';

    print $target_file "$nid\t";                     # nodeId
    print $target_file "$name\t";			         # name
    print $target_file "CLARIN: unknown title\t";    # title
    print $target_file "$year\t";     			     # date
    print $target_file "\t";                         # continent
    print $target_file "$res_country\t";             # country
    print $target_file "\t";                         # address
    print $target_file "$project\t";                 # projectname
    print $target_file "$description\t";             # projecttitle
    print $target_file "\t";                         # projectid
    print $target_file "$creator\t";                 # contactname
    print $target_file "$location_0\t";              # contactaddress
    print $target_file "\t";                         # contactemail
    print $target_file "$institution\t";             # contactorganisation
    print $target_file "$resource_type\t";           # genre
    print $target_file "\t";                         # subgenre
    print $target_file "\t";                         # task
    print $target_file "\t";                         # modalities
    print $target_file "\t";                         # interactivity
    print $target_file "\t";                         # planningtype
    print $target_file "\t";                         # involvement
    print $target_file "\t";                         # socialcontext
    print $target_file "\t";                         # eventstructure
    print $target_file "\t";                         # channel
    print $target_file "$reference_link\t";          # refs
    print $target_file "\t";                         # subject
    print $target_file "\t";                         # languageCode
    print $target_file "$res_languages";             # languageName
    print $target_file "\n";
    
    #exit;
}
                
close $src_file;
close $target_file;
        

#==================
# sub routines    #
#==================

#----------------------------(  cleanString )-------------------------------------#
#  FUNCTION:    cleanString                                                       #
#  PURPOSE:     nomalise a string by removing leading and trailing whitespaces,   #
#               double whitespaces, tabs and doublequotes          .              #
#  ARGS:        $string - the string to be cleaned                                #
#---------------------------------------------------------------------------------#

sub clean_up {
    my $string = shift;
    
    # turn empty fields into ' '
	return " " unless $string;
	
    $string =~ s/"//g; # remove double quotes
    $string =~ s/^\s+//; # remove leading whitespace    
    $string =~ s/\s+$//; # remove trailing whitespace
    $string =~ s/\t//g; # remove tabs (as our src is a tab separated file, there shouldn't be any in the first place, but it can't hurt, I guess)
    $string =~ s/\n//g; # remove newlines (there shouldn't be any, but imdi specification probably allows them in freefrom text fields)
    $string =~ s/\s\s/ /g; # normalize whitespace (no two whitespaces after one another)

    return $string;
}
