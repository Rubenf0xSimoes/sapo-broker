﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Security.Cryptography.X509Certificates;

using SapoBrokerClient;
using SapoBrokerClient.Authentication;
using SapoBrokerClient.Authentication.BrokerDb;
using Samples.Utils;
using RJH.CommandLineHelper;

using log4net;
using log4net.Config;

namespace Samples.Consumers
{

    /**
     * Run using :
     * -hn:127.0.0.1 -sslPort:3390 -dt:TOPIC -cer:c:\agent.cer -dn:/secret/foo     
     */

    class StsAuthConsumer
    {
        public static void Main(string[] args)
        {
            Console.WriteLine("BrokerDB authenticated Consumer test");

            if (args.Length == 0)
            {
                System.Console.WriteLine(CommandLineArguments.Usage());
                return;
            }

            CommandLineArguments cliArgs = new CommandLineArguments();
            Parser parser = new Parser(System.Environment.CommandLine, cliArgs);
            parser.Parse();

            BasicConfigurator.Configure();

            X509CertificateCollection certCollection = null;
            if (cliArgs.CertificatePath != null)
            {
                X509Certificate cert = X509Certificate.CreateFromCertFile(cliArgs.CertificatePath);

                certCollection = new X509CertificateCollection();
                certCollection.Add(cert);
            }

            List<HostInfo> hosts = new List<HostInfo>();
            hosts.Add(new HostInfo(cliArgs.Hostname, cliArgs.SslPortNumber));


            SslBrokerClient brokerClient = new SslBrokerClient(hosts, certCollection);

            brokerClient.OnFault += (f) =>
            {
                Console.WriteLine("Error");
                Console.WriteLine(String.Format("Code: {0}, Message: {1}, Detail: {2}", f.Code, f.Message, f.Detail));
            };

            ICredentialsProvider provider = new BrokerDbProvider("luis", "luis");

            Console.WriteLine("Authenticating");
            if (!brokerClient.Authenticate(provider))
            {
                Console.WriteLine("Authentication failed");
                return;
            }

            Subscription subscription = new Subscription(cliArgs.DestinationName, cliArgs.DestinationType);
            subscription.OnMessage += delegate(NetNotification notification)
            {
                System.Console.WriteLine("Message received: {0}",
                                         System.Text.Encoding.UTF8.GetString(notification.Message.Payload));
                if (notification.DestinationType != NetAction.DestinationType.TOPIC)
                    brokerClient.Acknowledge(notification.Subscription, notification.Message.MessageId);
            };

            brokerClient.Subscribe(subscription);

            Console.WriteLine("Write X to unsbscribe and exit");
            while (!System.Console.Read().Equals('X'))
                ;
            Console.WriteLine();
            Console.WriteLine("Unsubscribe...");


            // Note Subscription instance could other than the one used for subscription as long as it was equivelent (same destination type and subscription pattern). Since the application is ending and therefor the socket will be closed agent's will discard the previous subscription. 
            brokerClient.Unsubscribe(subscription);

            Console.WriteLine("Good bye");
        }
    }
}
