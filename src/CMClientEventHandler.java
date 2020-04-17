import java.util.Iterator;
import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientEventHandler implements CMAppEventHandler {
	private CMClientStub m_clientStub;
	private long m_lDelaySum;	// for forwarding simulation
	private long m_lStartTime;	// for delay of SNS content downloading, distributed file processing
	private int m_nMinNumWaitedEvents;  // for checking the completion of asynchronous castrecv service
	private int m_nRecvReplyEvents;		// for checking the completion of asynchronous castrecv service
	
	public CMClientEventHandler(CMClientStub stub) {
		m_clientStub = stub;
		m_clientStub = stub;
		m_lDelaySum = 0;
		m_lStartTime = 0;
		
	}
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		switch(cme.getType()) {
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_DUMMY_EVENT:
			processDummyEvent(cme);
			break;
		case CMInfo.CM_USER_EVENT:
			processUserEvent(cme);
			break;
		case CMInfo.CM_MULTI_SERVER_EVENT:
			processMultiServerEvent(cme);
			break;
		default:
			return;
		}
	}
	
	private void processMultiServerEvent(CMEvent cme)
	{
		CMMultiServerEvent mse = (CMMultiServerEvent) cme;
		switch(mse.getID())
		{
		case CMMultiServerEvent.NOTIFY_SERVER_INFO:
			System.out.println("New server info received: num servers: "+mse.getServerNum() );
			Iterator<CMServerInfo> iter = mse.getServerInfoList().iterator();
			System.out.format("%-20s %-20s %-10s %-10s%n", "name", "addr", "port", "udp port");
			System.out.println("--------------------------------------------------------------");
			while(iter.hasNext())
			{
				CMServerInfo si = iter.next();
				System.out.format("%-20s %-20s %-10d %-10d%n", si.getServerName(), 
						si.getServerAddress(), si.getServerPort(), si.getServerUDPPort());
			}
			break;
		case CMMultiServerEvent.NOTIFY_SERVER_LEAVE:
			System.out.println("An additional server["+mse.getServerName()+"] left the "
					+ "default server.");
			break;
		case CMMultiServerEvent.ADD_RESPONSE_SESSION_INFO:
			System.out.println("Session information of server["+mse.getServerName()+"]");
			System.out.format("%-60s%n", "------------------------------------------------------------");
			System.out.format("%-20s%-20s%-10s%-10s%n", "name", "address", "port", "user num");
			System.out.format("%-60s%n", "------------------------------------------------------------");
			Iterator<CMSessionInfo> iterSI = mse.getSessionInfoList().iterator();

			while(iterSI.hasNext())
			{
				CMSessionInfo tInfo = iterSI.next();
				System.out.format("%-20s%-20s%-10d%-10d%n", tInfo.getSessionName(), tInfo.getAddress(), 
						tInfo.getPort(), tInfo.getUserNum());
			}
			break;
		case CMMultiServerEvent.ADD_LOGIN_ACK:
			System.out.println("This client successfully logs in to server["+mse.getServerName()+"].");
			break;
		}
		
		return;
	}
	
	private void processSessionEvent(CMEvent cme)
	{
		CMSessionEvent se= (CMSessionEvent)cme;
		switch(se.getID()) {
		case CMSessionEvent.LOGIN_ACK:
			if(se.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
			}
			else if(se.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
			}
			else
			{
				System.out.println("This client successfully logs in to the default server.");
			}
			break;
		case CMSessionEvent.REGISTER_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				// user registration succeeded
				System.out.println("User["+se.getUserName()+"] successfully registered at time["
							+se.getCreationTime()+"].");
			}
			else
			{
				// user registration failed
				System.out.println("User["+se.getUserName()+"] failed to register!");
			}
			break;
		case CMSessionEvent.DEREGISTER_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				// user deregistration succeeded
				System.out.println("User["+se.getUserName()+"] successfully deregistered.");
			}
			else
			{
				// user registration failed
				System.out.println("User["+se.getUserName()+"] failed to deregister!");
			}
			break;
		case CMSessionEvent.FIND_REGISTERED_USER_ACK:
			if( se.getReturnCode() == 1 )
			{
				System.out.println("User profile search succeeded: user["+se.getUserName()
						+"], registration time["+se.getCreationTime()+"].");
			}
			else
			{
				System.out.println("User profile search failed: user["+se.getUserName()+"]!");
			}
			break;
		case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
			System.err.println("Unexpected disconnection from ["+se.getChannelName()
					+"] with key["+se.getChannelNum()+"]!");
			break;
		case CMSessionEvent.INTENTIONALLY_DISCONNECT:
			System.err.println("Intentionally disconnected all channels from ["
					+se.getChannelName()+"]!");
			break;
			default:
				return;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	{
		CMDummyEvent due = (CMDummyEvent) cme;
		System.out.println("session("+due.getHandlerSession()+"), group("+due.getHandlerGroup()+")");
		System.out.println("dummy msg: "+due.getDummyInfo());
		return;
	}
	
	private void processUserEvent(CMEvent cme)
	{
		int id = -1;
		long lSendTime = 0;
		int nSendNum = 0;
		
		CMUserEvent ue = (CMUserEvent) cme;

		if(ue.getStringID().equals("testForward"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			System.out.println("Received user event \'testForward\', id: "+id);
		}
		else if(ue.getStringID().equals("testNotForward"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			System.out.println("Received user event 'testNotForward', id("+id+")");
		}
		else if(ue.getStringID().equals("testForwardDelay"))
		{
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			lSendTime = Long.parseLong(ue.getEventField(CMInfo.CM_LONG, "stime"));
			long lDelay = System.currentTimeMillis() - lSendTime;
			m_lDelaySum += lDelay;
			System.out.println("Received user event 'testNotForward', id("+id+"), delay("+lDelay+"), delay_sum("+m_lDelaySum+")");
		}
		else if(ue.getStringID().equals("EndForwardDelay"))
		{
			nSendNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "sendnum"));
			System.out.println("Received user envet 'EndForwardDelay', avg delay("+m_lDelaySum/nSendNum+" ms)");
			m_lDelaySum = 0;
		}
		else if(ue.getStringID().equals("repRecv"))
		{
			String strReceiver = ue.getEventField(CMInfo.CM_STR, "receiver");
			int nBlockingChannelType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
			int nBlockingChannelKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
			int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
			int opt = -1;
			if(nBlockingChannelType == CMInfo.CM_SOCKET_CHANNEL)
				opt = CMInfo.CM_STREAM;
			else if(nBlockingChannelType == CMInfo.CM_DATAGRAM_CHANNEL)
				opt = CMInfo.CM_DATAGRAM;

			CMDummyEvent due = new CMDummyEvent();
			due.setDummyInfo("This is a test message to test a blocking channel");
			System.out.println("Sending a dummy event to ("+strReceiver+")..");
			
			if(opt == CMInfo.CM_STREAM)
				m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, true);
			else if(opt == CMInfo.CM_DATAGRAM)
				m_clientStub.send(due, strReceiver, opt, nBlockingChannelKey, nRecvPort, true);
			else
				System.err.println("invalid sending option!: "+opt);
		}
		else if(ue.getStringID().equals("testSendRecv"))
		{
			System.out.println("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
					"], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			
			if(!m_clientStub.getMyself().getName().equals(ue.getReceiver()))
				return;
			
			CMUserEvent rue = new CMUserEvent();
			rue.setID(222);
			rue.setStringID("testReplySendRecv");
			boolean ret = m_clientStub.send(rue, ue.getSender());
			if(ret)
				System.out.println("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")");
			else
				System.err.println("Failed to send the reply event!");			
		}
		else if(ue.getStringID().equals("testCastRecv"))
		{
			System.out.println("Received user event from ["+ue.getSender()+"], to session["+
					ue.getEventField(CMInfo.CM_STR, "Target Session")+"] and group["+
					ue.getEventField(CMInfo.CM_STR,  "Target Group")+"], (id, "+ue.getID()+
					"), (string id, "+ue.getStringID()+")");
			CMUserEvent rue = new CMUserEvent();
			rue.setID(223);
			rue.setStringID("testReplyCastRecv");
			boolean ret = m_clientStub.send(rue, ue.getSender());
			if(ret)
				System.out.println("Sent reply event: (id, "+rue.getID()+"), (sting id, "+rue.getStringID()+")");
			else
				System.err.println("Failed to send the reply event!");
		}
		else if(ue.getStringID().equals("testReplySendRecv")) // for testing asynchronous sendrecv service
		{
			long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
			System.out.println("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
					"), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			System.out.println("Server response delay: "+lServerResponseDelay+"ms.");

		}
		else if(ue.getStringID().equals("testReplyCastRecv")) // for testing asynchronous castrecv service
		{
			System.out.println("Asynchronously received reply event from ["+ue.getSender()+"]: (type, "+ue.getType()+
					"), (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")");
			m_nRecvReplyEvents++;
			
			if(m_nRecvReplyEvents == m_nMinNumWaitedEvents)
			{
				long lServerResponseDelay = System.currentTimeMillis() - m_lStartTime;
				System.out.println("Complete to receive requested number of reply events.");
				System.out.println("Number of received reply events: "+m_nRecvReplyEvents);
				System.out.println("Server response delay: "+lServerResponseDelay+"ms.");
				m_nRecvReplyEvents = 0;
			}
			
		}
		else
		{
			System.out.println("CMUserEvent received from ["+ue.getSender()+"], strID("+ue.getStringID()+")");
			System.out.format("%-5s%-20s%-10s%-20s%n", "Type", "Field", "Length", "Value");
			System.out.println("-----------------------------------------------------");
			Iterator<CMUserEventField> iter = ue.getAllEventFields().iterator();
			while(iter.hasNext())
			{
				CMUserEventField uef = iter.next();
				if(uef.nDataType == CMInfo.CM_BYTES)
				{
					System.out.format("%-5s%-20s%-10d", uef.nDataType, uef.strFieldName, 
										uef.nValueByteNum);
					for(int i = 0; i < uef.nValueByteNum; i++)
						System.out.print(uef.valueBytes[i]);
					System.out.println();
				}
				else
					System.out.format("%-5d%-20s%-10d%-20s%n", uef.nDataType, uef.strFieldName, 
							uef.strFieldValue.length(), uef.strFieldValue);
			}
		}
		return;
	}
	
}
