#!/usr/bin/perl
# Author: Claus Zinn
# Initiated: June 16, 2009
# modified: by Alexander Koenig, from 2009-12-15 on

use strict;
use warnings;
use File::Spec::Functions;

# retrieve all kind of information and store them into hashtables
my %hash_table_x0  = (); # topNode
my %hash_table_x5  = (); # continent
my %hash_table_x6  = (); # country
my %hash_table_x14 = (); # organisation
my %hash_table_x15 = (); # genre
my %hash_table_x18 = (); # modality
my %hash_table_x19 = (); # interactivity
my %hash_table_x20 = (); # planning type
my %hash_table_x21 = (); # involvement
my %hash_table_x22 = (); # social context
my %hash_table_x26 = (); # subject
my %hash_table_x28 = (); # language

my $HOME="/home/alekoe/projects/flamenco/fromperl"; 

# init a counter for generating item numbers
my $counter = 0;

# open source (imdi.csv) and create targets (text.tsv and items.tsv)
open(my $src_data, "<", catfile($HOME,"imdi.csv")) || die("Cannot open source file imdi.csv. $!");
open(my $items_tsv, ">", catfile($HOME,"items.tsv")) || die("Cannot open target items.tsv. $!");
open(my $text_tsv, ">", catfile($HOME,"text.tsv")) || die("Cannot open target text.tsv. $!");

print "Started creating items.tsv and text.tsv.\n";
while (my $imdiLine = <$src_data> )
{
    my @imdiFields=split(/\t/, $imdiLine); # split the database row into fields

    my $corpus = cleanString( $imdiFields[0] );

	# first print the index number into the tsv files
    $counter++;
    print $items_tsv "$counter";
    print $text_tsv  "$counter\t"; # id has to be separated by tab from all other fields

    my $cell = "";
    # print the first 27 columns for each entry 
    foreach my $element (@imdiFields) 
    {
		my $cell = cleanString($element);
		print $items_tsv "\t$cell"; # tab separated
		print $text_tsv " $cell"; # space separated
    }

	# get ready for the next line; insert line break
    print $items_tsv "\n";
    print $text_tsv "\n";

    analyseFill( $imdiFields[0],  \%hash_table_x0); # we write this ourselves; it can never be empty or unspecified!
    analyseFill( $imdiFields[5],  \%hash_table_x5);

    my $temp3 = cleanString( $imdiFields[28] );
    
    # country, language, and organisation have other separators in CLARIN data
    if ( $corpus eq "clarin" ) 
    {
		analyseFill_clarin( $imdiFields[6],  \%hash_table_x6 );
		analyseFill_clarin( $imdiFields[14], \%hash_table_x14 );
		analyseFill_clarin( $imdiFields[15], \%hash_table_x15 );
		analyseFill_clarin( $temp3,          \%hash_table_x28 );
    } 
    else 
    {
		analyseFill( $imdiFields[6],  \%hash_table_x6);
		analyseFillSep( $imdiFields[14], \%hash_table_x14);
		analyseFillSep( $imdiFields[15], \%hash_table_x15);
		analyseFill( $temp3, \%hash_table_x28);
    }

    analyseFillSepMult( $imdiFields[18], \%hash_table_x18);

    my $temp0 = lcfirst($imdiFields[19]);
    analyseFill( $temp0, \%hash_table_x19);

    my $temp1 = lcfirst($imdiFields[20]);
    analyseFill( $temp1, \%hash_table_x20);

    my $temp2 = lcfirst($imdiFields[21]);
    analyseFill( $temp2, \%hash_table_x21);

    analyseFill( $imdiFields[22], \%hash_table_x22);
    # analyseFill( $imdiFields[23], \%hash_table_x23);
    # analyseFill( $imdiFields[24], \%hash_table_x24);

    analyseFill_olac( $imdiFields[26], \%hash_table_x26);


}   

print "Finished creating items.tsv and text.tsv.\n";
close $src_data;
close $items_tsv;
close $text_tsv;

print "Started creating facet map files.\n";

fix_hash_values( \%hash_table_x0 );
fix_hash_values( \%hash_table_x5 );
fix_hash_values( \%hash_table_x6 );
fix_hash_values( \%hash_table_x14 );
fix_hash_values( \%hash_table_x15 );
fix_hash_values( \%hash_table_x18 );
fix_hash_values( \%hash_table_x19 );
fix_hash_values( \%hash_table_x20 );
fix_hash_values( \%hash_table_x21 );
fix_hash_values( \%hash_table_x22 );
# fix_hash_values( \%hash_table_x23 );
# fix_hash_values( \%hash_table_x24 );
fix_hash_values( \%hash_table_x26 );
fix_hash_values( \%hash_table_x28 );

# create all the facet_terms.tsv file
create_facet_tsv( "facetCorpus",         %hash_table_x0);
create_facet_tsv( "facetContinent",      %hash_table_x5);
create_facet_tsv( "facetCountry",        %hash_table_x6);
create_facet_tsv( "facetOrganisation",   %hash_table_x14);
create_facet_tsv( "facetGenre",          %hash_table_x15);
create_facet_tsv( "facetModality",       %hash_table_x18); # can/should be deleted too
create_facet_tsv( "facetInteractivity",  %hash_table_x19);
create_facet_tsv( "facetPlanningtype",   %hash_table_x20);
create_facet_tsv( "facetInvolvement",    %hash_table_x21);
create_facet_tsv( "facetSocialcontext",  %hash_table_x22);
# create_facet_tsv( "facetEventstructure", %hash_table_x23);
# create_facet_tsv( "facetChannel",        %hash_table_x24);
create_facet_tsv( "facetSubject",        %hash_table_x26);
create_facet_tsv( "facetLanguage",       %hash_table_x28);

print "Finished creating facet map files.\n";

# now, we need a second run, having established the $facet_terms.tsv files with the coding
# ------------------------------------------------------------------------------------------

print "Started creating facet term files.\n";

my $newCounter = 0;
open($src_data, "<", catfile($HOME,"imdi.csv")) || die("Cannot open IMDI source file.\n");

open(my $corpus_data,         ">$HOME/facetCorpus_map.tsv") || die("Cannot open facetCorpus_map target.\n");
open(my $continent_data,      ">$HOME/facetContinent_map.tsv") || die("Cannot open facetContinent_map target.\n");
open(my $country_data,        ">$HOME/facetCountry_map.tsv") || die("Cannot open facetCountry_map target.\n");
open(my $organisation_data,   ">$HOME/facetOrganisation_map.tsv") || die("Cannot open facetOrganisation_map target.\n");
open(my $genre_data,          ">$HOME/facetGenre_map.tsv") || die("Cannot open facetGenre_map target.\n");
open(my $language_data,       ">$HOME/facetLanguage_map.tsv") || die("Cannot open facetLanguage_map target.\n");
open(my $modality_data,       ">$HOME/facetModality_map.tsv") || die("Cannot open facetModality_map target.\n");
open(my $interactivity_data,   ">$HOME/facetInteractivity_map.tsv") || die("Cannot open facetInteractivity_map target.\n");
open(my $planningtype_data,   ">$HOME/facetPlanningtype_map.tsv") || die("Cannot open facetPlanningtype_map target.\n");
open(my $involvement_data,    ">$HOME/facetInvolvement_map.tsv") || die("Cannot open facetInvolvement_map target.\n");
open(my $socialcontext_data,  ">$HOME/facetSocialcontext_map.tsv") || die("Cannot open facetSocialcontext_map target.\n");
#open(my $eventstructure_data, ">$HOME/facetEventstructure_map.tsv") || die("Cannot open facetEventstructure_map target.\n");
#open(my $channel_data,        ">$HOME/facetChannel_map.tsv") || die("Cannot open facetChannel_map target.\n");
open(my $subject_data,        ">$HOME/facetSubject_map.tsv") || die("Cannot open facetSubject_map target.\n");

while (my $imdiLine = <$src_data> )
{
    my @imdiFields=split(/\t/, $imdiLine);

    $newCounter++;
    my $corpus = &cleanString( $imdiFields[0] );

    process_x( $imdiFields[0],  \%hash_table_x0,  $corpus_data, $newCounter );
    process_x( $imdiFields[5],  \%hash_table_x5,  $continent_data, $newCounter );

    my $temp3 = cleanString( $imdiFields[28] );

    if ( $corpus eq "clarin" ) 
    {
		process_x_clarin( $imdiFields[6],  \%hash_table_x6, $country_data, $newCounter );
		process_x_clarin( $imdiFields[14], \%hash_table_x14, $organisation_data, $newCounter );
		process_x_clarin( $imdiFields[15], \%hash_table_x15, $genre_data, $newCounter );
		process_x_clarin( $temp3,          \%hash_table_x28, $language_data, $newCounter );
    } 
    else 
    {
		process_x( $imdiFields[6],  \%hash_table_x6,  $country_data, $newCounter );
		process_x_sep( $imdiFields[14], \%hash_table_x14, $organisation_data, $newCounter );
		process_x_sep( $imdiFields[15], \%hash_table_x15, $genre_data, $newCounter );
		process_x( $temp3, \%hash_table_x28, $language_data, $newCounter );
    }

    process_x_sep_mult( $imdiFields[18], \%hash_table_x18, $modality_data, $newCounter );

    my $temp0 = lcfirst($imdiFields[19]);
    process_x( $temp0, \%hash_table_x19, $interactivity_data, $newCounter );

    my $temp1 = lcfirst($imdiFields[20]);
    process_x( $temp1, \%hash_table_x20, $planningtype_data, $newCounter );

    my $temp2 = lcfirst($imdiFields[21]);
    process_x( $temp2, \%hash_table_x21, $involvement_data, $newCounter );

    process_x( $imdiFields[22], \%hash_table_x22, $socialcontext_data, $newCounter );
#    process_x( $imdiFields[23], \%hash_table_x23, $eventstructure_data, $newCounter );
#    process_x( $imdiFields[24], \%hash_table_x24, $channel_data, $newCounter );

    # olac data different to IMDI data
    process_x_olac( $imdiFields[26], \%hash_table_x26, $subject_data, $newCounter );

}
                
close $src_data;
close $corpus_data;
close $continent_data;
close $country_data;
close $organisation_data;
close $genre_data;
close $language_data;
close $modality_data;
close $interactivity_data;
close $planningtype_data;
close $involvement_data;
close $socialcontext_data;
#close $eventstructure_data;
#close $channel_data;
close $subject_data;

print "Finished creating facet term files.\n";
        

#==================
# sub routines    #
#==================



#----------------------------(  cleanString )-------------------------------------#
#  FUNCTION:    cleanString                                                       #
#  PURPOSE:     nomalise a string by removing leading and trailing whitespaces,   #
#               double whitespaces, tabs and doublequotes          .              #
#  ARGS:        $string - the string to be cleaned                                #
#---------------------------------------------------------------------------------#

sub cleanString {
    my $string = shift;

    $string =~ s/"//g; # remove double quotes
    $string =~ s/^\s+//; # remove leading whitespace    
    $string =~ s/\s+$//; # remove trailing whitespace
    $string =~ s/\t//g; # remove tabs (as our src is a tab separated file, there shouldn't be any in the first place, but it can't hurt, I guess)
    $string =~ s/\n//g; # remove newlines (there shouldn't be any, but imdi specification probably allows them in freefrom text fields)
    $string =~ s/\s\s/ /g; # normalize whitespace (no two whitespaces after one another)

    return $string;
}

sub removeDoubleOccurrenceWord {
        my $arg = $_[0];
	$arg =~ s/(\w+)\s\1/$1/g;
	return $arg;
}


# now, ignore not only empty content but also the values "Unspecified" and "unspecified"
sub analyseFill{
    my( $currentContent, $currentHash ) = @_;

    if (not (exists $currentHash->{$currentContent} ) )
    {
	if ( ( $currentContent ne "" ) &&
	     ( $currentContent ne "Unspecified") &&
	     ( $currentContent ne "unspecified"))
	     {
	    		$currentHash->{$currentContent} = 1;
	    	 }
    }
}

# with taking into account a separator (multiple values treatment)
sub analyseFillSep{
    my( $currentContent, $currentHash ) = @_;

    my @currentContentFields = split(/;/, $currentContent);
    foreach my $val (@currentContentFields) 
    {
		$val = cleanString($val);
		if (not (exists $currentHash->{$val} ) )
		{
		    if ( ( $val ne "" ) &&
			 ( $val ne "Unspecified") &&
			 ( $val ne "unspecified"))
		    {
				$currentHash->{$val} = 1;
		    }
		}
    }
}

# for modality; first separate using ";", then ","
sub analyseFillSepMult{
    my( $currentContent, $currentHash ) = @_;

    my @currentContentFields = split(/;/, $currentContent);
    foreach my $val (@currentContentFields) {
	my @moreContentFields = split(/,/, $val);
	foreach my $moreVal (@moreContentFields) {	
	    $moreVal = &cleanString($moreVal);
	    $moreVal = lc($moreVal);
	    if (not (exists $currentHash->{$moreVal} ) )
	    {
		if ( ( $moreVal ne "" ) &&
		     ( $moreVal ne "Unspecified") &&
		     ( $moreVal ne "unspecified"))
		{
		    $currentHash->{$moreVal} = 1;
		}
	    }
	}
    }
}

# special routine for OLAC data.
sub analyseFill_olac{
    my( $currentContent, $currentHash ) = @_;
    my @currentContentFields = split(/<br>/, $currentContent);
    foreach my $val (@currentContentFields) {
	my @moreContentFields = split(/,/, $val);
	foreach my $moreVal (@moreContentFields) {	
	    $moreVal = cleanString($moreVal);
	    $moreVal = lc($moreVal);
	    $moreVal = removeDoubleOccurrenceWord($moreVal);
	    if (not (exists $currentHash->{$moreVal} ) )
	    {
		if ( ( $moreVal ne "" ) &&
		     ( $moreVal ne "Unspecified") &&
		     ( $moreVal ne "unspecified"))
		{
		    $currentHash->{$moreVal} = 1;
		}
	    }
	}
    }
}

# special routine for CLARIN DATA
sub analyseFill_clarin{
    my( $currentContent, $currentHash ) = @_;
    my @contentFields = split(/\|\|/, $currentContent);
    foreach my $ct (@contentFields)
    {
	$ct = cleanString($ct);
	if (not (exists $currentHash->{$ct} ) )
	{
                if ( $ct ne "" ) {
                    #print "added: $ct\n";
                    $currentHash->{$ct} = 1;
                }
	}
    }
}   

sub create_facet_tsv
{
    my( $filename, %currentHash ) = @_;
    
    open(my $terms_file, ">", catfile($HOME,"$filename"."_terms.tsv")) || die("Cannot open facet file: $filename. $!");
    my $counter = 0;
    foreach my $v (keys %currentHash){
        print $terms_file "$currentHash{$v}\t$v\n";
    } 
    close $terms_file;
}

sub fix_hash_values{
    my( $currentHash ) = @_;
    $counter = 0;
    foreach my $v (keys %$currentHash){
        $counter++;
        $currentHash->{$v} = $counter;
    }
    return $currentHash;
}

sub process_x{
    my( $currentContent, $currentHash, $fileHandle, $currentCounter) = @_;

    if (exists $currentHash->{$currentContent} )
    {
	print $fileHandle "$currentCounter\t $currentHash->{$currentContent}\n";
    }
}

# process_x with taking seperator into account 
sub process_x_sep{
    my( $currentContent, $currentHash, $fileHandle, $currentCounter) = @_;

    my @currentContentFields = split(/;/, $currentContent);
    foreach my $val (@currentContentFields) {
	$val = &cleanString($val);
	if (exists $currentHash->{$val} )
	{
	    print $fileHandle "$currentCounter\t $currentHash->{$val}\n";
	}
    }
}

# two seps: ';', then ','.
sub process_x_sep_mult{
    my( $currentContent, $currentHash, $fileHandle, $currentCounter) = @_;

    my @currentContentFields = split(/;/, $currentContent);
    foreach my $val (@currentContentFields) {
	my @moreContentFields = split(/,/, $val);
	foreach my $moreVal (@moreContentFields) {	
	    $moreVal = &cleanString($moreVal);
	    $moreVal = lc($moreVal);
	    if (exists $currentHash->{$moreVal} )
	    {
		print $fileHandle "$currentCounter\t $currentHash->{$moreVal}\n";
	    }
	}
    }
}

sub process_x_olac{
    my( $currentContent, $currentHash, $fileHandle, $currentCounter) = @_;

    my @currentContentFields = split(/<br>/, $currentContent);
    foreach my $val (@currentContentFields) {
	my @moreContentFields = split(/,/, $val);
	foreach my $moreVal (@moreContentFields) {	
	    $moreVal = &cleanString($moreVal);
	    $moreVal = lc($moreVal);
	    $moreVal = &removeDoubleOccurrenceWord($moreVal);
	    if (exists $currentHash->{$moreVal} )
	    {
		print $fileHandle "$currentCounter\t $currentHash->{$moreVal}\n";
	    }
	}
    }
}

sub process_x_clarin{
    my( $currentContent, $currentHash, $fileHandle, $currentCounter) = @_;

    my @currentContentFields = split(/\|\|/, $currentContent);
    foreach my $ct (@currentContentFields)
    {
	$ct = &cleanString($ct);
        if (exists $currentHash->{$ct} )
        {
            print $fileHandle "$newCounter\t $currentHash->{$ct}\n";
        }
    }
}
