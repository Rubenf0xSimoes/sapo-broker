#define _XOPEN_SOURCE 600
#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include "libsapo-broker2.h"

#define HOST        "127.0.0.1"

#define QUEUE_NAME  "/my/test/queue"
#define PAYLOAD     "ping"

int main(int argc, char *argv[])
{
    int i = 0;
    int count = 0;
    sapo_broker_t *sb;
    broker_msg_t *m;
    broker_server_t server;
    broker_destination_t dest;

    server.hostname =   HOST;
    server.port =    SB_PORT;
    server.transport = TCP;
    server.protocol = PROTOBUF;

    sb = broker_init( server );
    if (!sb) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    if( argc >= 2 ) {
        if( !strncmp("-topic", argv[1], 4 ) ) {
            dest.type = SB_TOPIC;
        } else {
            dest.type = SB_QUEUE;
        }

        if( argc >= 3) {
            dest.name = argv[2];
        } else {
            dest.name = QUEUE_NAME;
        }
        if( argc >= 4) {
            count = atoi(argv[3]);
        }

    } else {
        dest.type = SB_QUEUE;
        dest.name = QUEUE_NAME;
    }
    dest.queue_autoack = FALSE;

    if( dest.type == SB_QUEUE)
        printf("QUEUE ");
    else
        printf("TOPIC ");
    printf("%s\n", dest.name);

    if (broker_subscribe(sb, dest) != SB_OK) {
        printf("%s", broker_error(sb));
        exit(-1);
    }

    /* by default: process 2^31-1 msgs, depends on int overflow */
    for(; i <= count; i++) {
        m = broker_receive(sb, NULL);
        if( !m ) {
            printf("NO MSG\n");
            count++;
            sleep(1);
            continue;
        }

        printf("%s\n", m->payload);

        if( dest.type == SB_QUEUE && dest.queue_autoack == FALSE ) {
            broker_msg_ack( sb, m);
        } else {
            broker_msg_free(m);
        }
	}

    printf("Exiting: %s\n", broker_error(sb));
    /* pedantic mode, good for the server */
    broker_destroy( sb );
    return 0;
}
