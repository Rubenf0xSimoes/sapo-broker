use lib ('../lib');
use lib('../protobufxs/blib/lib');
use lib('../protobufxs/blib/arch');

use SAPO::Broker::Clients::Async;
use Data::Dumper;
use AnyEvent;

use strict;
use warnings;

my $host = "localhost";    #"broker.labs.sapo.pt"; #'broker.m3.bk.sapo.pt'
#my $port = 3390;
my $port = 3323;

my $broker = SAPO::Broker::Clients::Async->new(
    host  => $host,
    port  => $port,
    codec => 'thriftxs',
    #tls   => 1,
    rcb   => sub {
        my ($msg) = @_;
        my $payload = $msg->message->payload;
        print STDERR "[$payload]\n";
    } );

my %options = (

    #    'destination_type' => 'VIRTUAL_QUEUE', #'TOPIC',
    #    'destination'      => 'test@/tests/perl',
    'destination_type' => 'TOPIC',
    'destination'      => '/tests/perl',
    'auto_acknowledge' => 1,
);

$broker->subscribe(%options);

my $w = AnyEvent->condvar;
$w->recv;
