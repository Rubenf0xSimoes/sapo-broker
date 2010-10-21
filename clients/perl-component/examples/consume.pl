use lib ('../lib');

use SAPO::Broker::Clients::Simple;
use Data::Dumper;

use strict;
use warnings;

my $broker = SAPO::Broker::Clients::Simple->new('host' => 'localhost');
my %options = (
	'destination_type' => 'QUEUE',
	'destination' => '/tests/perl',
	#'destination' => '/janus/sms/received/vivo',
	'auto_acknowledge' => 1
);

$broker->subscribe(%options);

my $N = $ARGV[0] || 100;

for my $n (1..$N){
    my $notification = $broker->receive;
    my $payload = $notification->message->payload;
    print $payload."\n";
    if($n == 10){
        #$broker->unsubscribe(%options);
    }
}
