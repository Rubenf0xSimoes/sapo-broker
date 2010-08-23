use Time::HiRes qw(time);
use Net::Domain qw(hostfqdn);

use Test::More;

use strict;
use warnings;
use bytes;

my $rand_name = '/perl/tests/' . hostfqdn() . "_" . time();
my $N         = $ENV{'BROKER_N_TESTS'} || 100;
my $L         = $ENV{'BROKER_SIZE_TESTS'} || 1000;
my $host      = $ENV{'BROKER_HOST'} || 'broker.labs.sapo.pt';

sub write_broker_data {
    eval {
        open my $f, '>', '.broker_info' or die $!;
        local $, = "\n";
        print $f $rand_name, $host, $N,, $L;
        close($f) or die !$;;
    };
    if ($@) {
        warn $@;
        return 0;
    } else {
        return 1;
    }

}

sub rand_string($) {
    my ($n) = @_;

    my $ret = '';
    for ( 1 .. $n ) {
        $ret .= chr( int( rand(256) ) );
    }
    return $ret;
}

sub write_rand_data() {
    open my $f, '>:raw', '.broker_data' or die $!;
    for my $n ( 1 .. $N ) {
        my $payload = rand_string( rand($L) );
        my $length  = length($payload);
        ok( defined $payload, "Generate random payload $n" );
        print $f $length . "\n";
        print $f $payload;
        ok( defined $payload, "Wrote $length bytes." );
    }
    close $f or die !$;;
}

ok( $rand_name,          'Generate topic name' );
ok( write_broker_data(), 'Save broker info file' );
ok( write_rand_data(),   'Save broker random data' );

done_testing( 3 + 2 * $N );
