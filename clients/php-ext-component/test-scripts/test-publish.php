#!/usr/bin/env php
<?php

// instantiate the broker handle
$broker = broker_init("127.0.0.1", SB_PORT, SB_TCP, SB_PROTOBUF);
$msg = "Hello, world!";

// publish
broker_publish($broker, "/test/foo", $msg);

// dealloc broker
broker_destroy($broker);
?>
