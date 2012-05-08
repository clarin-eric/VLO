#!/usr/bin/perl -w
use strict;

my $solrcontext = "config/vlo_solr.xml";
my $vlocontext  = "config/contextfragment.xml";
my @filelist = qw(
    bin/application.properties
    war/vlo/WEB-INF/classes/applicationContext.xml
);
push @filelist, $solrcontext;
push @filelist, $vlocontext;

# ASK VALUES
my $PDIR = `pwd`;
chomp($PDIR);
my $SPATH = "";
my $VPATH = "";

# Derived from above.
my $SURL = "";

print "Please enter desired solr context path, e.g., ds/vlosolr\n";
$SPATH = <STDIN>;
chomp $SPATH;
print "Please enter desired vlo context path, e.g., ds/vlo\n";
$VPATH = <STDIN>;
chomp $VPATH;
print "\n";
print "Current package directory is [$PDIR] enter an empty line to accept or a new value to change it.\n";
my $TMP = <>;
chomp $TMP;
if($TMP ne ""){
    $PDIR = $TMP;
}
print "\n";
print "Current configuration is as follows:\n";
print "\tThe package directory is [[$PDIR]]\n";
print "\tThe desired solr context path is [[$SPATH]]\n";
print "\tThe desired vlo context path is [[$VPATH]]\n";
print "\n";
print "enter y to accept, any other value to abort.\n";
$TMP = <>;
chomp $TMP;
if($TMP ne "y"){
    die;
}

$SURL = "http://localhost:8080/$SPATH";

# UNPACK WAR FILES
system("cd war/vlo && unzip *.war");
system("cd war/solr && unzip *.war");

# SET VALUES
foreach(@filelist)
{
    local $/ = undef; #NOTE: disable /n spliting on file read. Only in this scope. Hacky, but perl best practises.
    open F, "<$_" or die $!.":: $_";
    my $in = <F>;
    close F;

    $in = join($SURL,split(/\[\[SOLRURL\]\]/,$in));
    $in = join($PDIR,split(/\[\[PACKAGEDIR\]\]/,$in));
    $in = join($SPATH,split(/\[\[SOLRPATH\]\]/,$in));
    $in = join($VPATH,split(/\[\[VLOPATH\]\]/,$in));

    open F, ">$_" or die;
    print F $in;
    close F;
}

# MOVE CONTEXT FILES
sub convertPathToFileName{
    my $in = shift;
    $in =~ s/\//#/g;
    $in = "config/$in.xml";
    return $in;
}

system("mv $solrcontext ".&convertPathToFileName($SPATH));
system("mv $vlocontext ".&convertPathToFileName($VPATH));
