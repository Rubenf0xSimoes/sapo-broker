use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new();
my %options = (
	'destination_type' => 'QUEUE',
	'destination' => '/perl/tests',
	'auto_acknowledge' => 1
);

$broker->subscribe(%options);

my $N = $ARGV[0] || 100;
my $prefix = $ARGV[1] || ("time=".time);

for my $n (1..$N){
	my $message = $broker->receive();
	print Dumper($message);
}
