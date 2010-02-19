#!/usr/bin/perl

use strict;
use warnings;
use File::Spec::Functions;
use Pg;

# define output dir, db params and corpora
my $working_dir = "/home/alekoe/projects/flamenco/fromperl/";

my $connection_string = "user=webuser host=catalog.clarin.eu dbname=corpusstructure password=start1a";

my %corpora = (
"imdi_ailla.csv" => "559236",
"imdi_andes.csv" => "499598",
"imdi_bas.csv" => "515264",
"imdi_bifo.csv" => "507986",
"imdi_cgn.csv" => "359354",
"imdi_coralRom.csv" => "556021",
"imdi_dbd.csv" => "354356",
"imdi_echo.csv" => "353737",
"imdi_endangeredLanguages.csv" => "496861",
"imdi_esf.csv" => "353736",
"imdi_grtp.csv" => "514541",
"imdi_ifa.csv" => "452354",
"imdi_iLspIntera.csv" => "501553",
"imdi_labLita.csv" => "553616",
"imdi_leidenArchive.csv" => "500252",
"imdi_lund.csv" => "530642",
"imdi_mpiCorpora.csv" => "143171",
"imdi_signLanguage.csv" => "476106",
"imdi_olac.csv" => "1668");

# do all the work
foreach my $corpus (keys %corpora)
{
	print "start processing corpus $corpus\n";
	my $filename = catfile($working_dir,$corpus);
	get_data_from_db($corpora{$corpus},$filename);
	print "finished\n";
}

# read in data from the database and print it into a tsv file
sub get_data_from_db
{
	my ($start_node,$output_file) = @_;
		
	# open output file
	open (my $out,'>',$output_file) or die "Could not open $output_file for output. $!";
	
	# Define the SELECT statement to determine the start node
	my $limit_select = "SELECT vpath FROM corpusstructure WHERE nodeid=$start_node";
	
	# connect to the db
	my $conn = Pg::connectdb($connection_string);

	# execute SELECT to get the vpath for the start node
	my $start_node_result = $conn->exec($limit_select);
	
	# die if something went wrong	
	die $conn->errorMessage unless PGRES_TUPLES_OK eq $start_node_result->resultStatus;
	
	my $number_of_vpaths = $start_node_result->ntuples;
	my $start_node_vpath;
	my $select_statement;
	
	# if only one vpath
	if ($number_of_vpaths == 1)
	{
		# get the only row
		$start_node_vpath = $start_node_result->fetchrow;
	
		# need to add the start node itself to the vpath, so that no siblings of it will be also checked
		$start_node_vpath .= "\/MPI$start_node#";
		# Define the SELECT statement specifying which archiveobjects should be checked
		$select_statement = "SELECT DISTINCT imdimd_session.nodeid, imdimd_session.name, title, date, continent, country, address, projectname, projecttitle, projectid, projectcontactname, projectcontactaddress, projectcontactemail, projectcontactorganisation, genre, subgenre, task, modalities, interactivity, planningtype, involvement, socialcontext, eventstructure, channel, references_, subject, id,  imdimd_contentlanguage.name FROM imdimd_contentlanguage, imdimd_session, corpusstructure WHERE imdimd_contentlanguage.nodeid = imdimd_session.nodeid AND corpusstructure.nodeid = imdimd_session.nodeid AND vpath LIKE '$start_node_vpath%'"; 
	}
	#if more than one vpath, the select needs to include them all
	else
	{
		print "There was more than one vpath for starnode $start_node\n";
		# start of the select statement
		$select_statement = "SELECT DISTINCT imdimd_session.nodeid, imdimd_session.name, title, date, continent, country, address, projectname, projecttitle, projectid, projectcontactname, projectcontactaddress, projectcontactemail, projectcontactorganisation, genre, subgenre, task, modalities, interactivity, planningtype, involvement, socialcontext, eventstructure, channel, references_, subject, id,  imdimd_contentlanguage.name FROM imdimd_contentlanguage, imdimd_session, corpusstructure WHERE imdimd_contentlanguage.nodeid = imdimd_session.nodeid AND corpusstructure.nodeid = imdimd_session.nodeid AND (";
		for (my $i=1;$i<=$number_of_vpaths;$i++)
		{
			# fetch the vpath
			$start_node_vpath = $start_node_result->fetchrow;
			# need to add the start node itself to the vpath, so that no siblings of it will be also checked
			$start_node_vpath .= "\/MPI$start_node#";
			# slap it at the end of the query
			$select_statement .="vpath LIKE '$start_node_vpath%' OR ";
		}
		# delete the final OR and put a closing bracket there instead
		$select_statement =~ s/ OR $/\)/;
	}
	# execute select
	my $result = $conn->exec($select_statement);


	# die if something went wrong
	die $conn->errorMessage unless PGRES_TUPLES_OK eq $result->resultStatus;
	
	while (my @row = $result->fetchrow) 
	{
		my $array_ref = process_row(\@row);
		my @array = @{$array_ref};
    		my $string = join("\t", @array);
    		print $out $string . "\n";
	}
}


sub process_row
{
	# todo: monitor whether it makes a difference in the output if there is ' ' in a field instead of nothing/undef
	my $input = shift;
	my @output;
	foreach my $element (@{$input})
	{
		if (!(defined($element)))
		{
			push(@output,' ');
		}
		else
		{
			$element =~ s/\n/ /g; # remove newlines
			$element =~ s/\t/ /g; # remove tabs
			push(@output,$element);
		}			
	}
	return \@output;
}


