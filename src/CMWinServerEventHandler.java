import java.util.Iterator;
import java.io.*;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMFileEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMMultiServerEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEventField;
import kr.ac.konkuk.ccslab.cm.event.handler.CMAppEventHandler;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEvent;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventDISCONNECT;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBSCRIBE;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class CMWinServerEventHandler implements CMAppEventHandler {
	private CMWinServer m_server;
	private CMServerStub m_serverStub;
	private int m_nCheckCount;	// for internal forwarding simulation
	private boolean m_bDistFileProc;	// for distributed file processing

	// information for csc_ftp
	private boolean m_bStartCSCFTPSession;
	private String m_strFileSender;
	private String m_strFileReceiver;
	private int m_nTotalNumFilesPerSession;
	private int m_nCurNumFilesPerSession;
	
	public CMWinServerEventHandler(CMServerStub serverStub, CMWinServer server)
	{
		m_server = server;
		m_serverStub = serverStub;
		m_nCheckCount = 0;
		m_bDistFileProc = false;
		
		m_bStartCSCFTPSession = false;
		m_strFileSender = null;
		m_strFileReceiver = null;
		m_nTotalNumFilesPerSession = 0;
		m_nCurNumFilesPerSession = 0;
	}
	
	@Override
	public void processEvent(CMEvent cme) {
		// TODO Auto-generated method stub
		switch(cme.getType())
		{
		case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_INTEREST_EVENT:
			processInterestEvent(cme);
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
	
	private void processSessionEvent(CMEvent cme)
	{
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMSessionEvent se = (CMSessionEvent) cme;
		switch(se.getID())
		{
		case CMSessionEvent.LOGIN:
			//System.out.println("["+se.getUserName()+"] requests login.");
			printMessage("["+se.getUserName()+"] requests login.\n");
			if(confInfo.isLoginScheme())
			{
				// user authentication...
				// CM DB must be used in the following authentication..
				boolean ret = CMDBManager.authenticateUser(se.getUserName(), se.getPassword(), 
						m_serverStub.getCMInfo());
				if(!ret)
				{
					printMessage("["+se.getUserName()+"] authentication fails!\n");
					m_serverStub.replyEvent(cme, 0);
				}
				else
				{
					printMessage("["+se.getUserName()+"] authentication succeeded.\n");
					m_serverStub.replyEvent(cme, 1);
				}
			}
			break;
		case CMSessionEvent.LOGOUT:
			//System.out.println("["+se.getUserName()+"] logs out.");
			printMessage("["+se.getUserName()+"] logs out.\n");
			break;
		case CMSessionEvent.REQUEST_SESSION_INFO:
			//System.out.println("["+se.getUserName()+"] requests session information.");
			printMessage("["+se.getUserName()+"] requests session information.\n");
			break;
		case CMSessionEvent.CHANGE_SESSION:
			//System.out.println("["+se.getUserName()+"] changes to session("+se.getSessionName()+").");
			printMessage("["+se.getUserName()+"] changes to session("+se.getSessionName()+").\n");
			break;
		case CMSessionEvent.JOIN_SESSION:
			//System.out.println("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").");
			printMessage("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").\n");
			break;
		case CMSessionEvent.LEAVE_SESSION:
			//System.out.println("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").");
			printMessage("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").\n");
			break;
		case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL:
			//System.out.println("["+se.getChannelName()+"] request to add SocketChannel with index("
			//		+se.getChannelNum()+").");
			printMessage("["+se.getChannelName()+"] request to add a nonblocking SocketChannel with key("
			+se.getChannelNum()+").\n");
			break;
		case CMSessionEvent.REGISTER_USER:
			//System.out.println("User registration requested by user["+se.getUserName()+"].");
			printMessage("User registration requested by user["+se.getUserName()+"].\n");
			break;
		case CMSessionEvent.DEREGISTER_USER:
			//System.out.println("User deregistration requested by user["+se.getUserName()+"].");
			printMessage("User deregistration requested by user["+se.getUserName()+"].\n");
			break;
		case CMSessionEvent.FIND_REGISTERED_USER:
			//System.out.println("User profile requested for user["+se.getUserName()+"].");
			printMessage("User profile requested for user["+se.getUserName()+"].\n");
			break;
		case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
			m_server.printStyledMessage("Unexpected disconnection from ["
					+se.getChannelName()+"] with key["+se.getChannelNum()+"]!\n", "bold");
			break;
		case CMSessionEvent.INTENTIONALLY_DISCONNECT:
			m_server.printStyledMessage("Intentionally disconnected all channels from ["
					+se.getChannelName()+"]!\n", "bold");
			break;
		default:
			return;
		}
	}
	
	private void processInterestEvent(CMEvent cme)
	{
		CMInterestEvent ie = (CMInterestEvent) cme;
		switch(ie.getID())
		{
		case CMInterestEvent.USER_ENTER:
			//System.out.println("["+ie.getUserName()+"] enters group("+ie.getCurrentGroup()+") in session("
			//		+ie.getHandlerSession()+").");
			printMessage("["+ie.getUserName()+"] enters group("+ie.getCurrentGroup()+") in session("
					+ie.getHandlerSession()+").\n");
			break;
		case CMInterestEvent.USER_LEAVE:
			//System.out.println("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
			//		+ie.getHandlerSession()+").");
			printMessage("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
					+ie.getHandlerSession()+").\n");
			break;
		case CMInterestEvent.USER_TALK:
			//System.out.println("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")");
			printMessage("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")\n");
			//System.out.println("<"+ie.getUserName()+">: "+ie.getTalk());
			printMessage("<"+ie.getUserName()+">: "+ie.getTalk()+"\n");
			break;
		default:
			return;
		}
	}
	
	private void processDummyEvent(CMEvent cme)
	{
		CMDummyEvent due = (CMDummyEvent) cme;
		//System.out.println("session("+due.getHandlerSession()+"), group("+due.getHandlerGroup()+")");
		printMessage("session("+due.getHandlerSession()+"), group("+due.getHandlerGroup()+")\n");
		//System.out.println("dummy msg: "+due.getDummyInfo());
		printMessage("dummy msg: "+due.getDummyInfo()+"\n");
		return;
	}
	
	private void processUserEvent(CMEvent cme)
	{
		int nForwardType = -1;
		int id = -1;
		String strUser = null;
		
		CMUserEvent ue = (CMUserEvent) cme;
		
		if(ue.getStringID().equals("testNotForward"))
		{
			m_nCheckCount++;
			id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
			//System.out.println("Received user event 'testNotForward', id("+id+"), checkCount("+m_nCheckCount+")");
			printMessage("Received user event 'testNotForward', id("+id+"), checkCount("+m_nCheckCount+")\n");
		}
		else if(ue.getStringID().equals("testForward"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)	// typical forwarding
			{
				m_nCheckCount++;
				id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
				//System.out.println("Received user evnet 'testForward', id("+id+"), checkCount("+m_nCheckCount+")");
				printMessage("Received user evnet 'testForward', id("+id+"), checkCount("+m_nCheckCount+")\n");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
		}
		else if(ue.getStringID().equals("EndSim"))
		{
			int nSimNum = 0;
			nSimNum = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "simnum"));
			//System.out.println("Received user event 'EndSim', simulation num("+nSimNum+")");
			printMessage("Received user event 'EndSim', simulation num("+nSimNum+")\n");
			if(nSimNum == 0)
			{
				//System.out.println("divided by 0 error.");
				printMessage("divided by 0 error.\n");
				return;
			}
			int nAvgCount = m_nCheckCount / nSimNum;
			//System.out.println("Total count("+m_nCheckCount+"), average count("+nAvgCount+").");
			printMessage("Total count("+m_nCheckCount+"), average count("+nAvgCount+").\n");
			m_nCheckCount = 0;
		}
		else if(ue.getStringID().equals("testForwardDelay"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)
			{
				id = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "id"));
				//System.out.println("Received user event 'testForwardDelay', id("+id+")");
				printMessage("Received user event 'testForwardDelay', id("+id+")\n");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
		}
		else if(ue.getStringID().equals("EndForwardDelay"))
		{
			nForwardType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "ftype"));
			if(nForwardType == 0)
			{
				//System.out.println("Received user event 'EndForwardDelay'");
				printMessage("Received user event 'EndForwardDelay'\n");
				strUser = ue.getEventField(CMInfo.CM_STR, "user");
				m_serverStub.send(cme, strUser);
			}
			
		}
		else if(ue.getStringID().equals("reqRecv"))
		{
			strUser = ue.getEventField(CMInfo.CM_STR, "user");
			int nChType = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chType"));
			int nChKey = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "chKey"));
			int nRecvPort = Integer.parseInt(ue.getEventField(CMInfo.CM_INT, "recvPort"));
			CMUserEvent userEvent = new CMUserEvent();
			userEvent.setStringID("repRecv");
			userEvent.setEventField(CMInfo.CM_STR, "receiver", m_serverStub.getMyself().getName());
			userEvent.setEventField(CMInfo.CM_INT, "chType", Integer.toString(nChType));
			userEvent.setEventField(CMInfo.CM_INT, "chKey", Integer.toString(nChKey));
			userEvent.setEventField(CMInfo.CM_INT, "recvPort", Integer.toString(nRecvPort));
			m_serverStub.send(userEvent, strUser);
			
			printMessage("["+strUser+"] requested to receive a dummy event ");
			
			SocketChannel sc = null;
			DatagramChannel dc = null;
			CMDummyEvent due = null;
			if(nChType == CMInfo.CM_SOCKET_CHANNEL)
			{
				printMessage("with the blocking socket channel ("+nChKey+").\n");
				sc = m_serverStub.getBlockSocketChannel(nChKey, strUser);
				if(sc == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, socket channel not found, key("
							+nChKey+"), user("+strUser+")!");
					return;
				}
				
				due = (CMDummyEvent) m_serverStub.receive(sc);
				if(due == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, failed to receive a dummy event!");
					return;
				}
				printMessage("received dummy info: "+due.getDummyInfo()+"\n");

			}
			else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL)
			{
				printMessage("with the blocking datagram channel port("+nRecvPort+").\n");
				dc = m_serverStub.getBlockDatagramChannel(nRecvPort);
				if(dc == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, datagram channel not found, recvPort("
							+nRecvPort+")!");
					return;
				}
				
				due = (CMDummyEvent) m_serverStub.receive(dc);
				if(due == null)
				{
					System.err.println("CMWinServerEventHandler.processUserEvent(): reqRecv, failed to receive a dummy event!");
					return;
				}
				printMessage("received dummy info: "+due.getDummyInfo()+"\n");				
			}
			
		}
		else if(ue.getStringID().equals("testSendRecv"))
		{
			printMessage("Received user event from ["+ue.getSender()+"] to ["+ue.getReceiver()+
					"], (id, "+ue.getID()+"), (string id, "+ue.getStringID()+")\n");

			if(!m_serverStub.getMyself().getName().equals(ue.getReceiver()))
				return;

			CMUserEvent rue = new CMUserEvent();
			rue.setID(222);
			rue.setStringID("testReplySendRecv");
			boolean ret = m_serverStub.send(rue, ue.getSender());
			if(ret)
				printMessage("Sent reply event: (id, "+rue.getID()+"), (string id, "+rue.getStringID()+")\n");
			else
				printMessage("Failed to send the reply event!\n");			
		}
		else if(ue.getStringID().contentEquals("start_csc_ftp_session"))
		{
			processUserEvent_start_csc_ftp_session(ue);
		}
		else
		{
			printMessage("CMUserEvent received from ["+ue.getSender()+"], strID("+ue.getStringID()+")\n");
			printMessage(String.format("%-5s%-20s%-10s%-20s%n", "Type", "Field", "Length", "Value"));
			printMessage("-----------------------------------------------------\n");
			Iterator<CMUserEventField> iter = ue.getAllEventFields().iterator();
			while(iter.hasNext())
			{
				CMUserEventField uef = iter.next();
				if(uef.nDataType == CMInfo.CM_BYTES)
				{
					printMessage(String.format("%-5s%-20s%-10d", uef.nDataType, uef.strFieldName, 
							uef.nValueByteNum));
					for(int i = 0; i < uef.nValueByteNum; i++)
					{
						// not yet
					}
					printMessage("\n");
				}
				else
				{
					printMessage(String.format("%-5d%-20s%-10d%-20s%n", uef.nDataType, uef.strFieldName, 
							uef.strFieldValue.length(), uef.strFieldValue));
				}
			}
		}
		return;
	}
	
	private void processUserEvent_start_csc_ftp_session(CMUserEvent ue)
	{
		// store relevant information for a csc-ftp session
		m_bStartCSCFTPSession = true;
		m_strFileSender = ue.getEventField(CMInfo.CM_STR, "strFileSender");
		m_strFileReceiver = ue.getEventField(CMInfo.CM_STR, "strFileReceiver");
		String strNumFiles = ue.getEventField(CMInfo.CM_INT, "nNumFilesPerSession");
		try {
			m_nTotalNumFilesPerSession = Integer.parseInt(strNumFiles);
		}catch(NumberFormatException e) {
			System.err.println("string : "+strNumFiles);
			e.printStackTrace();
		}
		m_nCurNumFilesPerSession = 0;

		if(CMInfo._CM_DEBUG)
		{
			System.out.println("CMWinServerEventHandler.processUserEvent_start_csc_ftp_session(): ");
			System.out.println("m_bStartCSCFTPSession: "+m_bStartCSCFTPSession);
			System.out.println("strFileSender: "+m_strFileSender);
			System.out.println("strFileReceiver: "+m_strFileReceiver);
			System.out.println("nNumFilesPerSession: "+m_nTotalNumFilesPerSession);
			System.out.println("m_nCurNumFilesPerSession: "+m_nCurNumFilesPerSession);
		}

		return;
	}

	
	private void processMultiServerEvent(CMEvent cme)
	{
		CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
		CMMultiServerEvent mse = (CMMultiServerEvent) cme;
		switch(mse.getID())
		{
		case CMMultiServerEvent.REQ_SERVER_REG:
			printMessage("server ("+mse.getServerName()+") requests registration: ip("
					+mse.getServerAddress()+"), port("+mse.getServerPort()+"), udpport("
					+mse.getServerUDPPort()+").\n");
			break;
		case CMMultiServerEvent.RES_SERVER_REG:
			if( mse.getReturnCode() == 1 )
			{
				m_server.updateTitle();
				printMessage("server["+mse.getServerName()+"] is successfully registered "
						+ "to the default server.\n");
			}
			else
			{
				printMessage("server["+mse.getServerName()+"] is not registered to the "
						+ "default server.\n");
			}
			break;
		case CMMultiServerEvent.REQ_SERVER_DEREG:
			printMessage("server["+mse.getServerName()+"] requests deregistration.\n");
			break;
		case CMMultiServerEvent.RES_SERVER_DEREG:
			if( mse.getReturnCode() == 1 )
			{
				printMessage("server["+mse.getServerName()+"] is successfully deregistered "
						+ "from the default server.\n");
			}
			else
			{
				printMessage("server["+mse.getServerName()+"] is not deregistered from the "
						+ "default server.\n");
			}
			break;
		case CMMultiServerEvent.ADD_LOGIN:
			if( confInfo.isLoginScheme() )
			{
				// user authentication omitted for the login to an additional server
				//CMInteractionManager.replyToADD_LOGIN(mse, true, m_serverStub.getCMInfo());
				m_serverStub.replyEvent(mse, 1);
			}
			printMessage("["+mse.getUserName()+"] requests login to this server("
								+mse.getServerName()+").\n");
			break;
		case CMMultiServerEvent.ADD_LOGOUT:
			printMessage("["+mse.getUserName()+"] log out this server("+mse.getServerName()
					+").\n");
			break;
		case CMMultiServerEvent.ADD_REQUEST_SESSION_INFO:
			printMessage("["+mse.getUserName()+"] requests session information.\n");
			break;
		}

		return;
	}
	
	private void printMessage(String strText)
	{
		/*
		m_outTextArea.append(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
		*/
		m_server.printMessage(strText);
	}
	
	/*
	private void setMessage(String strText)
	{
		m_outTextArea.setText(strText);
		m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
	}
	*/

}