#!/usr/bin/perl
# Author: Claus Zinn
# modified by: Alexander Koenig
#
use strict;
use warnings;
use File::Spec::Functions;

# MAPPING:  ELRA RESOURCES -----> IMDI METADATA SCHEME
#
# ELRA RESOURCES SCHEME
# -----------------------
#
#  0 resource type						-> 15 	(genre)
#  1 catalogue_item_reference 			-> 25	(refs)
#  2 resource_fullname 					-> 2 	(name)
#  3 resource_shortname 					IGNORED	
#  4 date_of_availability 				-> 4 	(date)
#  5 resource_short_description 			-> 3 	(title)	
#  6 languages_resource     				-> 28 (languageName)	
#  7 member academic research 			IGNORED
#  8 member commercial research 			IGNORED	
#  9 member academic commercial 			IGNORED	
# 10 member commercial commercial 		IGNORED
# 11 member academic evaluation 			IGNORED	
# 12 member commercial evaluation 		IGNORED
# 13 non_member academic research 		IGNORED
# 14 non_member commercial research 		IGNORED	
# 15 non_member academic commercial 		IGNORED	
# 16 non_member commercial commercial 	IGNORED
# 17 non_member academic evaluation 		IGNORED	
# 18 non_member commercial evaluation 	IGNORED
# 19 special_price						IGNORED 


my $basedir = "/home/alekoe/projects/flamenco/fromperl";

my $counter = 0;


# open source and target file
open (my $src_file, "<", catfile($basedir,"elraOrig.tsv")) or die("Cannot open source file elraOrig.tsv: $!");
open (my $target_file, ">", catfile($basedir,"imdi_elra.csv")) or die("Cannot open target file imdi_elra.csv: $!");



while (my $elraLine = <$src_file> )
{
    my @elraFields=split(/\t/, $elraLine);

	# ignore first line
    unless ($counter) { $counter++; next;} 
	my $genre = $elraFields[0]; # resource type	
	my $ref   = $elraFields[1]; # catalogue_item_reference
	my $name  = $elraFields[2]; # resource_fullname 
	my $date  = $elraFields[4]; # date_of_availability 
	my $title = $elraFields[5]; # resource_short_description
	my $lang  = $elraFields[6]; # languages_resource
	# $lang =~ s/-/;/g;
	# $lang =~ s/,/;/g;

	# there should be something in node id, so we use a counter for that purpose
	print $target_file "$counter"; # nodeId
	print $target_file "\t $name"; # name
	print $target_file "\t $title"; # title
	print $target_file "\t $date"; # date
	print $target_file "\t"; # continent
	print $target_file "\t"; # country
	print $target_file "\t"; # address
	print $target_file "\t"; # projectname
	print $target_file "\t"; # projecttitle
	print $target_file "\t"; # projectid
	print $target_file "\t"; # contactname
	print $target_file "\t"; # contactaddress
	print $target_file "\t"; # contactemail
	print $target_file "\t"; # contactorganisation
	print $target_file "\t $genre"; # genre
	print $target_file "\t"; #subgenre
	print $target_file "\t"; #task
	print $target_file "\t"; #modalities
	print $target_file "\t"; #interactivity
	print $target_file "\t"; #planningtype
	print $target_file "\t"; #involvement
	print $target_file "\t"; #socialcontext
	print $target_file "\t"; #eventstructure
	print $target_file "\t"; #channel
	print $target_file "\t $ref"; #refs
	print $target_file "\t"; #subject
	print $target_file "\t"; #languageCode
	print $target_file "\t $lang \n"; # languageName
    $counter++;
}
                
close $src_file;
close $target_file;
        
