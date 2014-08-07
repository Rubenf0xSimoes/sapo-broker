# Generated by the protocol buffer compiler.  DO NOT EDIT!

from google.protobuf import descriptor
from google.protobuf import message
from google.protobuf import reflection
from google.protobuf import service
from google.protobuf import service_reflection
from google.protobuf import descriptor_pb2


_ATOM_ACTION_ACTIONTYPE = descriptor.EnumDescriptor(
  name='ActionType',
  full_name='sapo_broker.Atom.Action.ActionType',
  filename='ActionType',
  values=[
    descriptor.EnumValueDescriptor(
      name='PUBLISH', index=0, number=0,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='POLL', index=1, number=1,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='ACCEPTED', index=2, number=2,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='ACKNOWLEDGE_MESSAGE', index=3, number=3,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='SUBSCRIBE', index=4, number=4,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='UNSUBSCRIBE', index=5, number=5,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='NOTIFICATION', index=6, number=6,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='FAULT', index=7, number=7,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='PING', index=8, number=8,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='PONG', index=9, number=9,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='AUTH', index=10, number=10,
      options=None,
      type=None),
  ],
  options=None,
)

_ATOM_DESTINATIONTYPE = descriptor.EnumDescriptor(
  name='DestinationType',
  full_name='sapo_broker.Atom.DestinationType',
  filename='DestinationType',
  values=[
    descriptor.EnumValueDescriptor(
      name='TOPIC', index=0, number=0,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='QUEUE', index=1, number=1,
      options=None,
      type=None),
    descriptor.EnumValueDescriptor(
      name='VIRTUAL_QUEUE', index=2, number=2,
      options=None,
      type=None),
  ],
  options=None,
)


_ATOM_PARAMETER = descriptor.Descriptor(
  name='Parameter',
  full_name='sapo_broker.Atom.Parameter',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='name', full_name='sapo_broker.Atom.Parameter.name', index=0,
      number=1, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='value', full_name='sapo_broker.Atom.Parameter.value', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_HEADER = descriptor.Descriptor(
  name='Header',
  full_name='sapo_broker.Atom.Header',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='parameter', full_name='sapo_broker.Atom.Header.parameter', index=0,
      number=1, type=11, cpp_type=10, label=3,
      default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_BROKERMESSAGE = descriptor.Descriptor(
  name='BrokerMessage',
  full_name='sapo_broker.Atom.BrokerMessage',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='message_id', full_name='sapo_broker.Atom.BrokerMessage.message_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='payload', full_name='sapo_broker.Atom.BrokerMessage.payload', index=1,
      number=2, type=12, cpp_type=9, label=2,
      default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='expiration', full_name='sapo_broker.Atom.BrokerMessage.expiration', index=2,
      number=3, type=3, cpp_type=2, label=1,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='timestamp', full_name='sapo_broker.Atom.BrokerMessage.timestamp', index=3,
      number=4, type=3, cpp_type=2, label=1,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_PUBLISH = descriptor.Descriptor(
  name='Publish',
  full_name='sapo_broker.Atom.Publish',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Publish.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination_type', full_name='sapo_broker.Atom.Publish.destination_type', index=1,
      number=2, type=14, cpp_type=8, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.Publish.destination', index=2,
      number=3, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='message', full_name='sapo_broker.Atom.Publish.message', index=3,
      number=4, type=11, cpp_type=10, label=2,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_POLL = descriptor.Descriptor(
  name='Poll',
  full_name='sapo_broker.Atom.Poll',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Poll.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.Poll.destination', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='timeout', full_name='sapo_broker.Atom.Poll.timeout', index=2,
      number=3, type=3, cpp_type=2, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_ACCEPTED = descriptor.Descriptor(
  name='Accepted',
  full_name='sapo_broker.Atom.Accepted',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Accepted.action_id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_ACKNOWLEDGEMESSAGE = descriptor.Descriptor(
  name='AcknowledgeMessage',
  full_name='sapo_broker.Atom.AcknowledgeMessage',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.AcknowledgeMessage.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='message_id', full_name='sapo_broker.Atom.AcknowledgeMessage.message_id', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.AcknowledgeMessage.destination', index=2,
      number=3, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_SUBSCRIBE = descriptor.Descriptor(
  name='Subscribe',
  full_name='sapo_broker.Atom.Subscribe',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Subscribe.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.Subscribe.destination', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination_type', full_name='sapo_broker.Atom.Subscribe.destination_type', index=2,
      number=3, type=14, cpp_type=8, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_UNSUBSCRIBE = descriptor.Descriptor(
  name='Unsubscribe',
  full_name='sapo_broker.Atom.Unsubscribe',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Unsubscribe.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.Unsubscribe.destination', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination_type', full_name='sapo_broker.Atom.Unsubscribe.destination_type', index=2,
      number=3, type=14, cpp_type=8, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_NOTIFICATION = descriptor.Descriptor(
  name='Notification',
  full_name='sapo_broker.Atom.Notification',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='destination', full_name='sapo_broker.Atom.Notification.destination', index=0,
      number=1, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='subscription', full_name='sapo_broker.Atom.Notification.subscription', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='destination_type', full_name='sapo_broker.Atom.Notification.destination_type', index=2,
      number=3, type=14, cpp_type=8, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='message', full_name='sapo_broker.Atom.Notification.message', index=3,
      number=4, type=11, cpp_type=10, label=2,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_FAULT = descriptor.Descriptor(
  name='Fault',
  full_name='sapo_broker.Atom.Fault',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Fault.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fault_code', full_name='sapo_broker.Atom.Fault.fault_code', index=1,
      number=2, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fault_message', full_name='sapo_broker.Atom.Fault.fault_message', index=2,
      number=3, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fault_detail', full_name='sapo_broker.Atom.Fault.fault_detail', index=3,
      number=4, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_PING = descriptor.Descriptor(
  name='Ping',
  full_name='sapo_broker.Atom.Ping',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Ping.action_id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_PONG = descriptor.Descriptor(
  name='Pong',
  full_name='sapo_broker.Atom.Pong',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Pong.action_id', index=0,
      number=1, type=9, cpp_type=9, label=2,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_AUTHENTICATION = descriptor.Descriptor(
  name='Authentication',
  full_name='sapo_broker.Atom.Authentication',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='action_id', full_name='sapo_broker.Atom.Authentication.action_id', index=0,
      number=1, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='authentication_type', full_name='sapo_broker.Atom.Authentication.authentication_type', index=1,
      number=2, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='token', full_name='sapo_broker.Atom.Authentication.token', index=2,
      number=3, type=12, cpp_type=9, label=2,
      default_value="",
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='user_id', full_name='sapo_broker.Atom.Authentication.user_id', index=3,
      number=4, type=9, cpp_type=9, label=1,
      default_value=unicode("", "utf-8"),
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='role', full_name='sapo_broker.Atom.Authentication.role', index=4,
      number=5, type=9, cpp_type=9, label=3,
      default_value=[],
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
  ],
  options=None)

_ATOM_ACTION = descriptor.Descriptor(
  name='Action',
  full_name='sapo_broker.Atom.Action',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='publish', full_name='sapo_broker.Atom.Action.publish', index=0,
      number=1, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='poll', full_name='sapo_broker.Atom.Action.poll', index=1,
      number=2, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='accepted', full_name='sapo_broker.Atom.Action.accepted', index=2,
      number=3, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='ack_message', full_name='sapo_broker.Atom.Action.ack_message', index=3,
      number=4, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='subscribe', full_name='sapo_broker.Atom.Action.subscribe', index=4,
      number=5, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='unsubscribe', full_name='sapo_broker.Atom.Action.unsubscribe', index=5,
      number=6, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='notification', full_name='sapo_broker.Atom.Action.notification', index=6,
      number=7, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='fault', full_name='sapo_broker.Atom.Action.fault', index=7,
      number=8, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='ping', full_name='sapo_broker.Atom.Action.ping', index=8,
      number=9, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='pong', full_name='sapo_broker.Atom.Action.pong', index=9,
      number=10, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='auth', full_name='sapo_broker.Atom.Action.auth', index=10,
      number=11, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='action_type', full_name='sapo_broker.Atom.Action.action_type', index=11,
      number=12, type=14, cpp_type=8, label=2,
      default_value=0,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
    _ATOM_ACTION_ACTIONTYPE,
  ],
  options=None)

_ATOM = descriptor.Descriptor(
  name='Atom',
  full_name='sapo_broker.Atom',
  filename='broker.proto',
  containing_type=None,
  fields=[
    descriptor.FieldDescriptor(
      name='header', full_name='sapo_broker.Atom.header', index=0,
      number=1, type=11, cpp_type=10, label=1,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
    descriptor.FieldDescriptor(
      name='action', full_name='sapo_broker.Atom.action', index=1,
      number=2, type=11, cpp_type=10, label=2,
      default_value=None,
      message_type=None, enum_type=None, containing_type=None,
      is_extension=False, extension_scope=None,
      options=None),
  ],
  extensions=[
  ],
  nested_types=[],  # TODO(robinson): Implement.
  enum_types=[
    _ATOM_DESTINATIONTYPE,
  ],
  options=None)


_ATOM_HEADER.fields_by_name['parameter'].message_type = _ATOM_PARAMETER
_ATOM_PUBLISH.fields_by_name['destination_type'].enum_type = _ATOM_DESTINATIONTYPE
_ATOM_PUBLISH.fields_by_name['message'].message_type = _ATOM_BROKERMESSAGE
_ATOM_SUBSCRIBE.fields_by_name['destination_type'].enum_type = _ATOM_DESTINATIONTYPE
_ATOM_UNSUBSCRIBE.fields_by_name['destination_type'].enum_type = _ATOM_DESTINATIONTYPE
_ATOM_NOTIFICATION.fields_by_name['destination_type'].enum_type = _ATOM_DESTINATIONTYPE
_ATOM_NOTIFICATION.fields_by_name['message'].message_type = _ATOM_BROKERMESSAGE
_ATOM_ACTION.fields_by_name['publish'].message_type = _ATOM_PUBLISH
_ATOM_ACTION.fields_by_name['poll'].message_type = _ATOM_POLL
_ATOM_ACTION.fields_by_name['accepted'].message_type = _ATOM_ACCEPTED
_ATOM_ACTION.fields_by_name['ack_message'].message_type = _ATOM_ACKNOWLEDGEMESSAGE
_ATOM_ACTION.fields_by_name['subscribe'].message_type = _ATOM_SUBSCRIBE
_ATOM_ACTION.fields_by_name['unsubscribe'].message_type = _ATOM_UNSUBSCRIBE
_ATOM_ACTION.fields_by_name['notification'].message_type = _ATOM_NOTIFICATION
_ATOM_ACTION.fields_by_name['fault'].message_type = _ATOM_FAULT
_ATOM_ACTION.fields_by_name['ping'].message_type = _ATOM_PING
_ATOM_ACTION.fields_by_name['pong'].message_type = _ATOM_PONG
_ATOM_ACTION.fields_by_name['auth'].message_type = _ATOM_AUTHENTICATION
_ATOM_ACTION.fields_by_name['action_type'].enum_type = _ATOM_ACTION_ACTIONTYPE
_ATOM.fields_by_name['header'].message_type = _ATOM_HEADER
_ATOM.fields_by_name['action'].message_type = _ATOM_ACTION

class Atom(message.Message):
  __metaclass__ = reflection.GeneratedProtocolMessageType
  
  class Parameter(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_PARAMETER
  
  class Header(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_HEADER
  
  class BrokerMessage(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_BROKERMESSAGE
  
  class Publish(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_PUBLISH
  
  class Poll(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_POLL
  
  class Accepted(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_ACCEPTED
  
  class AcknowledgeMessage(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_ACKNOWLEDGEMESSAGE
  
  class Subscribe(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_SUBSCRIBE
  
  class Unsubscribe(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_UNSUBSCRIBE
  
  class Notification(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_NOTIFICATION
  
  class Fault(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_FAULT
  
  class Ping(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_PING
  
  class Pong(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_PONG
  
  class Authentication(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_AUTHENTICATION
  
  class Action(message.Message):
    __metaclass__ = reflection.GeneratedProtocolMessageType
    DESCRIPTOR = _ATOM_ACTION
  DESCRIPTOR = _ATOM
