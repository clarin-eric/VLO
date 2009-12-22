#!/usr/bin/perl
# Author: Claus Zinn
# modified by: Alexander Koenig

use strict;
use warnings;    
use File::Spec::Functions;

# prefix each item with an item number
my $base_dir="/home/alekoe/projects/flamenco/fromperl"; 
my $target_file = catfile($base_dir,"imdi.csv");
open (my $merged_imdi, ">", $target_file) or die "Cannot open imdi.csv in $base_dir. $!";

# for all files with "imdi_" prefix
my @files = <$base_dir/imdi_*>;
foreach my $file (@files) 
{
    if ($file =~/imdi_(.*)\.csv/) 
    { 
		my $postfix = $1;
		print "Now handling $file i.e. corpus $postfix\n";
		open (my $src_file, "<", $file) or die "Cannot open IMDI source file $file. $!";
		while (my $imdiLine = <$src_file> )
		{
		    print $merged_imdi "$postfix\t$imdiLine";
		}  
		close $src_file;
    }
}

close $merged_imdi;
print "Successfully created target file $target_file.\n";
