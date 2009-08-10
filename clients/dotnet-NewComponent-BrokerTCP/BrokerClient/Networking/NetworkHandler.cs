
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Collections.Generic;
using System.Net.Security;
using System.Security.Cryptography.X509Certificates;

using SapoBrokerClient;
using SapoBrokerClient.Encoding;
using SapoBrokerClient.Utils;

namespace SapoBrokerClient.Networking
{
	/// <summary>
	/// NetworkHandler deals with network connectivity.
    /// It also encodes and decoded messages at the network level (processes network header - encoding type and version and payload length).
	/// </summary>
	
	public class NetworkHandler : IDisposable
	{
        public enum IoStatus { Unstarted, Ok, Failed, Closed};
        
        /// <summary>
        /// IoSyncStatus contains NetworkHandler status and provides an object were clients can synchronize to be notified of state transiotion.
        /// </summary>
        public class IoSyncStatus{

            private readonly NotifiableEvent<IoStatus> notifiableEvent;

            public NotifiableEvent<IoStatus> OnChange
            {
                get { return notifiableEvent; }  
            } 

            private volatile IoStatus status;

            public IoStatus Status
            {
              get { return status; }
              set { status = value; }
            }

            public IoSyncStatus(IoStatus status, NotifiableEvent<IoStatus> notifiableEvent)
            {
                this.status = status;
                this.notifiableEvent = notifiableEvent;
            }
        }

		public delegate void MessageReceivedHandler(byte[] messagePayload);
        public delegate void IoFailureHandler(IoSyncStatus syncStatus);
		
		public event MessageReceivedHandler MessageReceived;
        public event IoFailureHandler IoFailed;
		
		#region Private Members

        private readonly log4net.ILog log = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        volatile private int retriesCount = int.MaxValue;

        public int ReconnectionRetries
        {
            get { return retriesCount; }
            set { retriesCount = value; }
        }

        protected Socket socket;
		private volatile Stream communicationStream;
	    private CircularContainer<HostInfo> hosts;
        private HostInfo hostInfo;
        private int connectionVersion = 0;

        private bool closed = false;
				
		#endregion

		#region Auxiliary methods
		private void OnMessageReceived(short protovolType, short protocolVersion, byte[] messagePayload)
		{
            // Save the delegate field in a temporary field for thread safety
			MessageReceivedHandler currentMessageReceived = MessageReceived;

            if (currentMessageReceived != null)
            {
                currentMessageReceived.BeginInvoke(messagePayload, null, null);
			}
		}
		
		
		private void DecodeHeader(MessageAccumulator.DecodedMessageHeader decodedMessageHeader, byte[] encodedMessageHeader)
		{
			short protocolTypeNet =  BitConverter.ToInt16(encodedMessageHeader, 0);
			short protocolVerionNet =  BitConverter.ToInt16(encodedMessageHeader, 2);
			int messageLengthNet =  BitConverter.ToInt32(encodedMessageHeader, 4);

            decodedMessageHeader.EncodingType = IPAddress.NetworkToHostOrder(protocolTypeNet);
			decodedMessageHeader.EncodingVersion = IPAddress.NetworkToHostOrder(protocolVerionNet);
			decodedMessageHeader.Length = IPAddress.NetworkToHostOrder(messageLengthNet);
		}
		
		#endregion
		
		public NetworkHandler(IList<HostInfo> hosts)
		{
            this.hosts = new CircularContainer<HostInfo>(hosts);
        }

        protected void CreateAndConnect(HostInfo hostInfo)
        {
            this.socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            IAsyncResult asyncResult = socket.BeginConnect(hostInfo.Hostname, hostInfo.Port, null, null);
            if (!asyncResult.CompletedSynchronously)
            {
                bool signaled =  asyncResult.AsyncWaitHandle.WaitOne(15 * 1000, false);
                if (!signaled)
                    throw new TimeoutException(String.Format("Connection timeout while connectig to agent {0}:{1}", hostInfo.Hostname, hostInfo.Port));
            }
            this.communicationStream = GetCommunicationStream();
        }

        virtual protected Stream GetCommunicationStream()
        {
            return new NetworkStream(socket, true);
        }

        private void StartReading(HostInfo hostInfo)
        {
            // Initialize comunication
            CreateAndConnect(hostInfo);


            // Start receiving
            ReadContext readContext = new ReadContext(4 * 1024, this.connectionVersion);

            ReadStream.BeginRead(readContext.Data, 0, readContext.Data.Length, new AsyncCallback(ReadCallback), readContext);
        }

		public void Start()
		{
            try
            {
                hostInfo = hosts.Get();
                StartReading(hostInfo);
            }catch(Exception e) {
                // Connection failed
                if (!OnIoFailure(this.connectionVersion))
                {
                    log.Error("Re-Connection failed");
                    throw e;
                }
            }
		}


        private class ReadContext
        {
            private volatile MessageAccumulator msgAccumulator;

            public MessageAccumulator Accumulator
            {
                get { return msgAccumulator; }
                set { msgAccumulator = value; }
            }
            private byte[] rawData;

            public byte[] Data
            {
                get { return rawData; }
                set { rawData = value; }
            }

            private int connectionVersion;

            public int ConnectionVersion
            {
                get { return connectionVersion; }
            }

            public ReadContext(int dataBufferLength, int connectionVersion)
            {
                msgAccumulator = new MessageAccumulator();
                rawData = new byte[dataBufferLength];
                this.connectionVersion = connectionVersion;
            }
        }

        /// <summary>
        /// Method used by trying to reconnect.
        /// </summary>
        /// <param name="connectionVersion">A value indicating witch connection version is being used. Useful for ensuring that only one thread performs this action.</param>
        /// <returns>false if it failed to reconnect or false indicating that reconnection was sucessfull</returns>
        private bool OnIoFailure(int connectionVersion)
        {
            log.ErrorFormat("Connection failed. {0}", hosts.Peek());
            
            lock (this)
            {
                if (closed)
                    return false;

                // Check connection version
                if (connectionVersion < this.connectionVersion)
                {
                    // Another thread already sinalized the failure so return
                    return !closed; // If the reconnection was sucessful then it's not closed, otherwise it is.
                }


                // Fire event
                IoSyncStatus syncStatus = new IoSyncStatus(IoStatus.Failed, new NotifiableEvent<IoStatus>());
                IoFailureHandler handler = this.IoFailed;
                if (handler != null)
                    handler(syncStatus);

                // Close socket
                CloseCommunication();


                // Reconnect 

                bool retry = false;
                int tryCount = 0;
                do
                {
                    try
                    {
                        StartReading(hostInfo);
                        retry = false;
                    }
                    catch (Exception)
                    {
                        retry = (++tryCount) != retriesCount;
                        if( ! retry)
                        {
                            closed = true;
                            //Notify clients
                            syncStatus.Status = IoStatus.Closed;
                            syncStatus.OnChange.Fire(syncStatus.Status);

                            // Return
                            return false;
                        }
                        // wait for while, give the agent time to get back to life.
                        System.Threading.Thread.Sleep(tryCount * 500);
                        hostInfo = hosts.Get();
                    }
                    if(retry)
                        log.ErrorFormat("Re-connection failed. {0}", hosts.Peek());
                } while (retry);

                ++connectionVersion;

                syncStatus.Status = IoStatus.Ok;
                syncStatus.OnChange.Fire(syncStatus.Status);
            }
            return true;
        }

        private void ReadFailed(ReadContext readContext)
        {
            OnIoFailure(readContext.ConnectionVersion);
        }

        private void ReadCallback(IAsyncResult asyncResult)
        {
            try
            {
                ReadContext readContext = (ReadContext)asyncResult.AsyncState;
                byte[] rawData = readContext.Data;

                int bytesReceived = 0;
                try
                {
                    bytesReceived = ReadStream.EndRead(asyncResult);
                }
                catch (Exception)
                {
                    bytesReceived = 0;
                }

                if (bytesReceived == 0)
                {
                    // Read as failed (bytes received == 0 or exception in EndRead)
                    ReadFailed(readContext);
                    return;
                }

                MessageAccumulator msgAcc = readContext.Accumulator;
                ProcessReceivedData(ref msgAcc, rawData, bytesReceived);
                readContext.Accumulator = msgAcc;


                //continue to read if it's not closed.
                lock (this)
                {
                    if (closed)
                        return;
                }
                ReadStream.BeginRead(readContext.Data, 0, readContext.Data.Length, new AsyncCallback(ReadCallback), readContext);
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
        }

        private void ProcessReceivedData(ref MessageAccumulator msgAccumulator, byte[] rawData, int dataLength)
        {
            byte[] workingBuffer;
            int readIndex;
            //int newDataLength;
            bool hasMoreData;
            
            hasMoreData = false;
            readIndex = 0;
            
            Console.WriteLine("Received data length: " + dataLength);

            do
            {
                workingBuffer = (msgAccumulator.Stage == MessageAccumulator.AccumatingStage.HEADER) ? msgAccumulator.Header : msgAccumulator.Payload;
                //int bytesToCopy = (dataLength > msgAccumulator.DesiredBytes) ? (msgAccumulator.DesiredBytes - msgAccumulator.ReceivedBytes)  : dataLength;

                int neededBytes = msgAccumulator.DesiredBytes - msgAccumulator.ReceivedBytes;

                int bytesToCopy = (dataLength > neededBytes) ? neededBytes : dataLength;
                try
                {
                    Array.Copy(rawData, readIndex, workingBuffer, msgAccumulator.ReceivedBytes, bytesToCopy);
                }
                catch (ArgumentException ae)
                {
                    Console.WriteLine(String.Format("readIndex: ", readIndex)); 
                }

                msgAccumulator.ReceivedBytes += bytesToCopy;
                readIndex += bytesToCopy;
                dataLength -= bytesToCopy;

                hasMoreData = dataLength != 0;

                if (msgAccumulator.ReceivedBytes == msgAccumulator.DesiredBytes)
                {
                    if (msgAccumulator.Stage == MessageAccumulator.AccumatingStage.HEADER)
                    {
                        DecodeHeader(msgAccumulator.MessageHeader, msgAccumulator.Header);

                        msgAccumulator.Payload = new byte[msgAccumulator.MessageHeader.Length];
                        msgAccumulator.Stage = MessageAccumulator.AccumatingStage.BODY;
                        msgAccumulator.DesiredBytes = msgAccumulator.MessageHeader.Length;
                        msgAccumulator.ReceivedBytes = 0;
                    }
                    else
                    {
                        OnMessageReceived(msgAccumulator.MessageHeader.EncodingType, msgAccumulator.MessageHeader.EncodingVersion, msgAccumulator.Payload);
                        msgAccumulator = new MessageAccumulator();
                    }
                }
                

            } while (hasMoreData);

        }

		public bool SendMessage(byte[] messagePayload, IMessageSerializer encodingType)
		{
			Stream stream = WriteStream;
			short netProtocolType = IPAddress.HostToNetworkOrder(encodingType.ProtocolType);
			short netProtocolVersion = IPAddress.HostToNetworkOrder(encodingType.ProtocolVersion);
			int netMessageLength = IPAddress.HostToNetworkOrder(messagePayload.Length);
			
			byte[] netProtocolTypeData =  BitConverter.GetBytes(netProtocolType);
			byte[] netProtocolVersionData =  BitConverter.GetBytes(netProtocolVersion);
			byte[] netMessageLengthData = BitConverter.GetBytes(netMessageLength);

            int currentConVersion = 0;
            try
            {
                lock (this)
                {
                    currentConVersion = this.connectionVersion;
                    if (closed)
                        throw new InvalidOperationException("Network object is closed.");

                    // Write header
                    stream.Write(netProtocolTypeData, 0, netProtocolTypeData.Length);
                    stream.Write(netProtocolVersionData, 0, netProtocolVersionData.Length);
                    stream.Write(netMessageLengthData, 0, netMessageLengthData.Length);

                    // Write payload
                    stream.Write(messagePayload, 0, messagePayload.Length);
                    stream.Flush();
                }
            }
            catch (Exception)
            {
                return OnIoFailure(currentConVersion);
            }
            return true;
		}
		
		protected Stream WriteStream
		{
			get{ return communicationStream;}
		}
		
		protected Stream ReadStream
		{
			get{ return communicationStream;}
		}


        #region IDisposable and Close Members

        public void Close()
        {
            Dispose(true);
        }
        public void Dispose()
        {
            Dispose(true);
        }

        ~NetworkHandler()
        {
            Dispose(false);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (disposing)
            {
                lock (this)
                {
                    if (closed)
                        return;
                    closed = true;
                    CloseCommunication();
                    GC.SuppressFinalize(this);
                }
            }
        }
        protected void CloseCommunication()
        {
            CloseStreams();
            CloseSocket();
        }

        protected virtual void CloseStreams()
        {
            if (ReadStream != null) ReadStream.Close();
            if (WriteStream != null) WriteStream.Close();
        }

        protected virtual void CloseSocket()
        {
            if (socket.Connected)
            {
                socket.Shutdown(SocketShutdown.Both);
                socket.Close();
            }
        }

        

        #endregion
    }
}
