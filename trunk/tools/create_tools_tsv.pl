#!/usr/bin/perl
#
# Input:
# CLARIN tools metadata as downloaded from http://www.clarin.eu/documents/lrt-inventory
# DFKI registry data in XML format
# Output: tsv facets etc., we will need the following fields 
#0   contributor (clarin or dfki)
#1   name
#2   type
#3   creator/author/developer
#4   description
#5   contact person(s)
#6   organisation(s)
#7   language(s)
#8   reference_link
#9	 webservice_link
#10   license
#11  platform(s)


 
use strict;
use warnings;
use Cwd;
use File::Spec::Functions;
use Data::Dumper;
use Text::CSV_XS;
use XML::Simple;
use File::Slurp;
use utf8;
use Encode;
    
# declare base dir
my $basedir = "/home/alekoe/projects/flamenco/create_tools/";

# declare absolute file names for dfki input, clarin input, items.tvs and text.tsv
my $items_file = catfile($basedir,"items.tsv");
my $text_file = catfile($basedir,"text.tsv");
my $clarin_file = catfile($basedir,"view-export_tools.csv");
my $dfki_file = catfile($basedir,"registry.xml");


# declare facet hashes
my %contributor = ("clarin" => 1, "dfki" =>2);
my %tooltype;
my %language;
my %platform;
my %institution;
my %licence;


# open text.tsv and items.tsv for output
open (my $text_output, ">", $text_file) or die "Could not open $text_file: $!";
open (my $items_output, ">", $items_file) or die "Could not open $items_file: $!";

# initialize item count
my $item_count = 1;

# first handle CLARIN input
open (my $clarin_input, "<", $clarin_file) or die "Could not open $clarin_file: $!";

my $csv = Text::CSV_XS->new ({ binary => 1, eol => $/ });

while (my $clarin_line = $csv->getline ($clarin_input)) 
{
     my @clarin_fields = @$clarin_line;
	  
    # we only need a couple of fields, and they need to be sanitized
	my $name = clean_up($clarin_fields[0]); # 0 name 	
	my $tool_type = clean_up($clarin_fields[3]); # 3 tool_type
	my $creator = clean_up($clarin_fields[4]); # 4 creator/author/developer 
	my $description = clean_up($clarin_fields[5]); # 5 description    
	my $contact_person = clean_up($clarin_fields[7]); # 7 contact person
	my $organisation = clean_up($clarin_fields[8]); # 8 organisation
	my $languages = clean_up($clarin_fields[10]); # 10 input languages
	my $reference_link = clean_up($clarin_fields[16]); # 16 reference link
	my $webservice_link = clean_up($clarin_fields[17]); # 17 webservice link
	my $open_source = clean_up($clarin_fields[19]); # 19 open source (yes/no)
	my $platform = clean_up($clarin_fields[22]); # 22 platform        

	my $pricing = "closed source";
	$pricing = "open source" if $open_source eq 'yes';         

	# we don't want the first line
	next if $tool_type eq 'Type (field_tool_type)';

	# print entry to items.tsv and text.tsv
	print $items_output "$item_count\tclarin\t$name\t$tool_type\t$creator\t$description\t$contact_person\t$organisation\t$languages\t$reference_link\t$webservice_link\t$pricing\t$platform\n";
	print $text_output "$item_count\tclarin $name $tool_type $creator $description $contact_person $organisation $languages $reference_link $webservice_link $pricing $platform\n";
	$item_count++;
	
	# some facets can have more than one value
	my @language_array = disassemble_facet($languages);
	my @tooltype_array = disassemble_facet($tool_type);
	my @platform_array = disassemble_facet($platform);
	
	# collect facets
	$licence{$pricing} = 1 unless $licence{$pricing};
	$institution{$organisation} = 1 unless $institution{$organisation};
	foreach my $element (@tooltype_array)
	{
		$tooltype{$element} = 1 unless $tooltype{$element}; 
	}
	foreach my $element (@language_array)
	{
		$language{$element} = 1 unless $language{$element}; 
	}
	foreach my $element (@platform_array)
	{
		$platform{$element} = 1 unless $platform{$element}; 
	}

}
                
close $clarin_input;


# then handle dfki input

# close and reopen to account for different encoding
close $items_output;
close $text_output;
open (my $new_text_output, ">>", $text_file) or die "Could not open $text_file: $!";
open (my $new_items_output, ">>", $items_file) or die "Could not open $items_file: $!";

#open (my $new_xml_input, "<:utf8", $dfki_file) or die "Could not open $dfki_file: $!";
#my $xml_string = read_file($new_xml_input);
my $xml = new XML::Simple( suppressempty => '');
my $data = $xml->XMLin($dfki_file);

# loop through entries
foreach my $element (@{$data->{ID}})
{
	my ($name, $tool_type, $creator, $description, $contact_person, $organisation, $languages, $reference_link, $webservice_link, $pricing, $platform); 
	
	$webservice_link = ''; # dfki does not provide a webservice link
	
	$name = clean_up($element->{title}->{content});
	
	foreach my $creator_field (@{$element->{creator}})
	{
		if (not (UNIVERSAL::isa( $creator_field, "HASH" )))
		{
			$creator = clean_up($creator_field);
		}
		else
		{
			if ($creator_field->{refine} eq 'Institute')
			{
				$organisation = clean_up($creator_field->{content});
			}
			elsif ($creator_field->{refine} eq 'Mail')
			{
				$contact_person = clean_up($creator_field->{content});
			}
			
		}
	}
	
	$description = clean_up($element->{description}[0]);

	foreach my $relation (@{$element->{relation}})
	{
		$reference_link = clean_up($relation->{content}) if $relation->{refine} eq 'url';
		$pricing = clean_up($relation->{content}) if $relation->{refine} eq 'academic_pricing';
	}


	# some facets can have more than one value
	my @tooltype_array;
	my @language_array;
	my @platform_array;
	
	foreach my $relation (@{$element->{Relation}})
	{
		if ($relation->{refine} eq 'MainSection') 		# tool_type
		{
			push(@tooltype_array,clean_up($relation->{content}));
		}

		if ($relation->{refine} eq 'SupportedLanguage') 		# language
		{
			push(@language_array,clean_up($relation->{content}));
		}

		if ($relation->{refine} eq 'SupportedPlatform') 		# platform
		{
			push(@platform_array,clean_up($relation->{content}));
		}
		
		$tool_type = join(', ', @tooltype_array);
		$languages = join(', ', @language_array);
		$platform =  join(', ', @platform_array);
	}
	
	# set default values for missing tags
	for my $value ($languages,$platform,$pricing)
	{
		$value = " " unless $value;
	}
	
	# print entry to items.tsv and text.tsv
	print $new_items_output "$item_count\tdfki\t$name\t$tool_type\t$creator\t$description\t$contact_person\t$organisation\t$languages\t$reference_link\t$webservice_link\t$pricing\t$platform\n";
	print $new_text_output "$item_count\tdfki $name $tool_type $creator $description $contact_person $organisation $languages $reference_link $webservice_link $pricing $platform\n";
	$item_count++;
		
	# collect facets
	$licence{$pricing} = 1 unless $licence{$pricing};
	$institution{$organisation} = 1 unless $institution{$organisation};
	foreach my $element (@tooltype_array)
	{
		$tooltype{$element} = 1 unless $tooltype{$element}; 
	}
	foreach my $element (@language_array)
	{
		$language{$element} = 1 unless $language{$element}; 
	}
	foreach my $element (@platform_array)
	{
		$platform{$element} = 1 unless $platform{$element}; 
	}
	
}

close $new_items_output;
close $new_text_output;


# now write facet_terms.tsv and facet_map.tsv files 

itemize_facets(\%tooltype);
itemize_facets(\%language);
itemize_facets(\%platform);
itemize_facets(\%institution);
itemize_facets(\%licence);

# create all the facet_terms.tsv file
create_facet_tsv( "contributor", %contributor);
create_facet_tsv( "tooltype", %tooltype);
create_facet_tsv( "language", %language);
create_facet_tsv( "platform", %platform);
create_facet_tsv( "institution", %institution);
create_facet_tsv( "licence", %licence);

# open items.tsv as input
open (my $items_input, "<", $items_file) or die "Could not open $items_file: $!";

# open all six map files as output
open (my $contributor_map, ">", catfile($basedir,"contributor_map.tsv")) or die("Cannot open contributor_map.tsv. $!\n");
open (my $tooltype_map,    ">", catfile($basedir,"tooltype_map.tsv"))    or die("Cannot open tooltype_map.tsv. $!\n");
open (my $language_map,    ">", catfile($basedir,"language_map.tsv"))    or die("Cannot open language_map.tsv. $!\n");
open (my $platform_map,    ">", catfile($basedir,"platform_map.tsv"))    or die("Cannot open platform_map.tsv. $!\n");
open (my $institution_map, ">", catfile($basedir,"institution_map.tsv")) or die("Cannot open institution_map.tsv. $!\n");
open (my $licence_map,     ">", catfile($basedir,"licence_map.tsv"))     or die("Cannot open licence_map.tsv. $!\n");

while (my $line = <$items_input>)
{
	my @fields = split /\t/, $line;
	my $item_id = $fields[0];
	
	# contributor
	my $contributor = $fields[1];
	print $contributor_map "$item_id\t$contributor{$contributor}\n" if $contributor;
	
	# institution
	my $institution = $fields[7];
	print $institution_map "$item_id\t$institution{$institution}\n" if $institution;
	
	# licence
	my $licence = $fields[11];
	my @licences = disassemble_facet($licence);
	foreach my $element (@licences)
	{
		print $licence_map "$item_id\t$licence{$element}\n" if $element;
	}

	# tool type
	my $tooltype  = $fields[3];
	my @tooltypes = disassemble_facet($tooltype);
	foreach my $element (@tooltypes)
	{
		print $tooltype_map "$item_id\t$tooltype{$element}\n" if $element;
	}
	
	# language
	my $language = $fields[8];
	my @languages = disassemble_facet($language);
	foreach my $element (@languages)
	{
		print $language_map "$item_id\t$language{$element}\n" if $element;
	}
	
	# platform
	my $platform = $fields[12];
	my @platforms = disassemble_facet($platform);
	foreach my $element (@platforms)
	{
		print $platform_map "$item_id\t$platform{$element}\n" if $element;
	}

}

close $items_input;
close $contributor_map;
close $tooltype_map;
close $language_map;
close $platform_map;
close $institution_map;
close $licence_map;

sub itemize_facets
{
    my( $facet_hash ) = @_;
    my $counter = 0;
    foreach my $key (keys %$facet_hash)
    {
        $counter++;
        $facet_hash->{$key} = $counter;
    }
    return $facet_hash;
}


sub create_facet_tsv
{
    my( $filename, %current_hash ) = @_;
    my $full_filename = catfile($basedir,$filename."_terms.tsv");
    open (my $terms_output, ">", $full_filename) or die("Cannot open facet file $full_filename. $!\n");
    my $counter = 0;
    foreach my $element (keys %current_hash)
    {
        print $terms_output "$current_hash{$element}\t$element\n";
    } 
    close $terms_output;
}




#----------------------------(  align_categories )----------------------------------------#
#  FUNCTION:    align_categories                                                          #
#  PURPOSE:     nomalise a field name by correcting typical misspellings					  #
#  ARGS:        $arg - the string to be normalises                           		      #
#  RETURNS:		$arg - the normalised string												  #
#-----------------------------------------------------------------------------------------#

sub align_categories{
    my $arg = shift;

    # tool types
    $arg =~ s/annotation tool/Annotation Tools/;
    $arg =~ s/evaluation tool/Evaluation Tools/;
    $arg =~ s/written language/Written Language/;
    $arg =~ s/multimedia tool/Multimedia/;
    $arg =~ s/multimodal tool/Multimedia/;
    $arg =~ s/spoken language/Spoken Language/;
    $arg =~ s/NLP development aid/NLP Development Aid/;
    $arg =~ s/toolbox/Other/;
    $arg =~ s/web service/Other/;
    $arg =~ s/web application/Other/;
    $arg =~ s/single tool/Other/;
    $arg =~ s/other/Other/;

    # license
    $arg =~ s/^yes$/free/;
    $arg =~ s/^no$/commercial/;
    $arg =~ s/^\$[0-9]+\.[0-9]+/commercial/;
    $arg =~ s/^[0-9]+\.[0-9]+/commercial/;
    $arg =~ s/^[0-9]+\$/commercial/;
    $arg =~ s/^[0-9]*SFr/commercial/;
    $arg =~ s/^\$[0-9]+/commercial/;
    $arg =~ s/^[0-9]+\/student/commercial/;
    $arg =~ s/^[0-9][0-9]+/commercial/;
    $arg =~ s/to negotiate2499/to negotiate/;
    $arg =~ s/to negotiate89.00\$/to negotiate/;
    $arg =~ s/^\£+[0-9]+/commercial/;

    # organisations (even within databases)
    $arg =~ s/Conexor Oy/Connexor Oy/;
    $arg =~ s/Conexor oy/Connexor Oy/;
    $arg =~ s/Connexor oy/Connexor Oy/;

    $arg =~ s/^DFKI$/DFKI GmbH/;
    $arg =~ s/German Research Center for Artificial Intelligence (DFKI)/DFKI GmbH/;

    $arg =~ s/MITRE Corp\./The MITRE Corporation/;
    $arg =~ s/^MITRE Corporation/The MITRE Corporation/;

    $arg =~ s/MPI for Psycholinguistics/Max Planck Institute for Psycholinguistics/;
    $arg =~ s/none/None/;

    # platform
    $arg =~ s/platform independent/independent/;
    $arg =~ s/platform-independent/independent/;
    $arg =~ s/windows/Windows/;
    $arg =~ s/tested under MS Windows/Windows/;
    $arg =~ s/tested under MS Windows and Linux/Windows and Linux/;
    $arg =~ s/^All$/Any/;
    $arg =~ s/^All major platforms$/Any/;
    $arg =~ s/Any platform able to support the software requirements/Any/;
    $arg =~ s/^any$/Any/;

    return $arg;
}
	


#----------------------------(  clean_up )----------------------------------------#
#  FUNCTION:    clean_up                                                          #
#  PURPOSE:     nomalise a string by removing leading and trailing whitespaces,   #
#               double whitespaces, tabs and doublequotes          .              #
#				(and fix the encoding on the way)								  #
#  ARGS:        $string - the string to be cleaned                                #
#  RETURNS:		$string - the normalised string									  #
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

	return " " unless $string; # account for fields that only consist of whitespaces
	
	# fix encoding
	$string = encode("utf-8", $string);
	
	$string = align_categories($string); # normalise names
	
    return $string;
}


#----------------------------(  disassemble_facet )----------------------------------------#
#  FUNCTION:    disassemble_facet                                                          #
#  PURPOSE:     split a string with multiple values into an array of values                #
#  ARGS:        $input - the string to be split up		                                   #
#  RETURNS:		@output - an array of the values in the comma-separated string			   #
#------------------------------------------------------------------------------------------#

sub disassemble_facet 
{
	my $input = shift;	
	my @output;
	if ($input =~ /\|\|/)
	{
		@output = split /\|/,$input;
	}
	else
	{		
	 	@output = split /,/,$input;
	}
	foreach my $item (@output)
	{
		  $item =~ s/^\s+//; # remove leading whitespace
		  $item =~ s/\s+$//; # remove trailing whitespace
	}
	return @output;
}