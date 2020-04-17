import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Scanner;

import kr.ac.konkuk.ccslab.cm.entity.CMServer;
import kr.ac.konkuk.ccslab.cm.entity.CMUser;
import kr.ac.konkuk.ccslab.cm.event.CMDummyEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
import kr.ac.konkuk.ccslab.cm.event.CMUserEvent;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMConfigurator;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class CMClientApp {
	private CMClientStub m_clientStub;
	private CMClientEventHandler m_eventHandler;
	private boolean m_bRun;
	private Scanner m_scan = null;
	
	public CMClientApp()
	{
		m_clientStub = new CMClientStub();
		m_eventHandler = new CMClientEventHandler(m_clientStub);
		m_bRun = true;
	}
	
	public CMClientStub getClientStub()
	{
		return m_clientStub;
	}
	public CMClientEventHandler getClientEventHandler()
	{
		return m_eventHandler;
	}
	
	public void startCM()
	{
		// get current server info from the server configuration file
		String strCurServerAddress = null;
		int nCurServerPort = -1;
		String strNewServerAddress = null;
		String strNewServerPort = null;
		
		strCurServerAddress = m_clientStub.getServerAddress();
		nCurServerPort = m_clientStub.getServerPort();
		
		// ask the user if he/she would like to change the server info
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("========== start CM");
		System.out.println("current server address: "+strCurServerAddress);
		System.out.println("current server port: "+nCurServerPort);
		
		try {
			System.out.print("new server address (enter for current value): ");
			strNewServerAddress = br.readLine().trim();
			System.out.print("new server port (enter for current value): ");
			strNewServerPort = br.readLine().trim();

			// update the server info if the user would like to do
			if(!strNewServerAddress.isEmpty() && !strNewServerAddress.equals(strCurServerAddress))
				m_clientStub.setServerAddress(strNewServerAddress);
			if(!strNewServerPort.isEmpty() && Integer.parseInt(strNewServerPort) != nCurServerPort)
				m_clientStub.setServerPort(Integer.parseInt(strNewServerPort));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		boolean bRet = m_clientStub.startCM();
		if(!bRet)
		{
			System.err.println("CM initialization error!");
			return;
		}
		startTest();
	}
	
	
	public void startTest()
	{
		System.out.println("client application starts.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		m_scan = new Scanner(System.in);
		String strInput = null;
		int nCommand = -1;
		while(m_bRun)
		{
			System.out.println("Type \"0\" for menu.");
			System.out.print("> ");
			try {
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			
			try {
				nCommand = Integer.parseInt(strInput);
			} catch (NumberFormatException e) {
				System.out.println("Incorrect command number!");
				continue;
			}
			
			switch(nCommand)
			{
			case 0:
				printAllMenus();
				break;
			case 100:
				startCM();
				break;
			case 999:
				testTerminateCM();
				break;			
			case 1: // connect to default server
				testConnectionDS();
				break;
			case 2: // disconnect from default server
				testDisconnectionDS();
				break;
			case 3: // connect to a designated server
				testConnectToServer();
				break;
			case 4: // disconnect from a designated server
				testDisconnectFromServer();
				break;
			case 10: // asynchronous login to default server
				testLoginDS();
				break;
			case 11: // synchronously login to default server
				testSyncLoginDS();
				break;
			case 12: // logout from default server
				testLogoutDS();
				break;
			case 13: // log in to a designated server
				testLoginServer();
				break;
			case 14: // log out from a designated server
				testLogoutServer();
				break;
			case 22: // join a session
				testJoinSession();
				break;
			case 23: // synchronously join a session
				testSyncJoinSession();
				break;
			case 24: // leave the current session
				testLeaveSession();
				break;
			case 42: // test CMDummyEvent
				testDummyEvent();
				break;
			case 43: // test CMUserEvent
				testUserEvent();
				break;
			case 51: // print current information about the client
				testCurrentUserStatus();
				break;
			case 53: // request additional server info
				testRequestServerInfo();
				break;
			case 57: // print all configurations
				testPrintConfigurations();
				break;
			case 90: // register user
				testRegisterUser();
				break;
			case 91: // deregister user
				testDeregisterUser();
				break;
			case 92: // find user
				testFindRegisteredUser();
				break;
			default:
				System.err.println("Unknown command.");
				break;
			}
		}
		
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_scan.close();
	}
	
	public void printAllMenus()
	{
		System.out.println("---------------------------------- Help");
		System.out.println("0: show all menus");
		System.out.println("---------------------------------- Start/Stop");
		System.out.println("100: start CM, 999: terminate CM");
		System.out.println("---------------------------------- Connection");
		System.out.println("1: connect to default server, 2: disconnect from default server");
		System.out.println("3: connect to designated server, 4: disconnect from designated server");
		System.out.println("---------------------------------- Login");
		System.out.println("10: login to default server, 11: synchronously login to default server");
		System.out.println("12: logout from default server");
		System.out.println("13: login to designated server, 14: logout from designated server");
		System.out.println("---------------------------------- Session/Group");
		System.out.println("20: request session information from default server");
		System.out.println("21: synchronously request session information from default server");
		System.out.println("22: join session of default server, 23: synchronously join session of default server");
		System.out.println("24: leave session of default server, 25: change group of default server");
		System.out.println("26: print group members");
		System.out.println("27: request session information from designated server");
		System.out.println("28: join session of designated server, 29: leave session of designated server");
		System.out.println("---------------------------------- Event Transmission");
		System.out.println("40: chat, 41: multicast chat in current group");
		System.out.println("42: test CMDummyEvent, 43: test CMUserEvent, 44: test datagram event, 45: test user position");
		System.out.println("46: test sendrecv, 47: test castrecv");
		System.out.println("48: test asynchronous sendrecv, 49: test asynchronous castrecv");
		System.out.println("---------------------------------- Information");
		System.out.println("50: show group information of default server, 51: show current user status");
		System.out.println("52: show current channels, 53: show current server information");
		System.out.println("54: show group information of designated server");
		System.out.println("55: measure input network throughput, 56: measure output network throughput");
		System.out.println("57: show all configurations, 58: change configuration");
		System.out.println("---------------------------------- Channel");
		System.out.println("60: add channel, 61: remove channel, 62: test blocking channel");
		System.out.println("---------------------------------- File Transfer");
		System.out.println("70: set file path, 71: request file, 72: push file");
		System.out.println("73: cancel receiving file, 74: cancel sending file");
		System.out.println("75: print sending/receiving file info");
		System.out.println("---------------------------------- Social Network Service");
		System.out.println("80: request content list, 81: request next content list, 82: request previous content list");
		System.out.println("83: request attached file, 84: upload content");
		System.out.println("---------------------------------- User");
		System.out.println("90: register new user, 91: deregister user, 92: find registered user");
		System.out.println("93: add new friend, 94: remove friend, 95: show friends, 96: show friend requesters");
		System.out.println("97: show bi-directional friends");
		System.out.println("---------------------------------- MQTT");
		System.out.println("200: connect, 201: publish, 202: subscribe, 203: print session info");
		System.out.println("204: unsubscribe, 205: disconnect");
		System.out.println("---------------------------------- Other CM Tests");
		System.out.println("101: test forwarding scheme, 102: test delay of forwarding scheme");
		System.out.println("103: test repeated request of SNS content list");
		System.out.println("104: pull/push multiple files, 105: split file, 106: merge files, 107: distribute and merge file");
		System.out.println("108: send event with wrong # bytes, 109: send event with wrong type");
	}
	public void testTerminateCM()
	{
		m_clientStub.terminateCM();
		m_bRun = false;
	}
	
	// DS 연결
	public void testConnectionDS()
	{
		System.out.println("====== connect to default server");
		m_clientStub.connectToServer();
		System.out.println("======");
	}
	
	// DS 연결 끊음
	public void testDisconnectionDS()
	{
		System.out.println("====== disconnect from default server");
		m_clientStub.disconnectFromServer();
		System.out.println("======");
	}
	
	// 로그인
	public void testLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		boolean bRequestResult = false;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		bRequestResult = m_clientStub.loginCM(strUserName, strPassword);
		if(bRequestResult)
			System.out.println("successfully sent the login request.");
		else
			System.err.println("failed the login request!");
		System.out.println("======");
	}
	
	// DS 동시 로그인
	public void testSyncLoginDS()
	{
		String strUserName = null;
		String strPassword = null;
		CMSessionEvent loginAckEvent = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}
		
		System.out.println("====== login to default server");
		System.out.print("user name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strUserName = br.readLine();
			if(console == null)
			{
				System.out.print("password: ");
				strPassword = br.readLine();
			}
			else
				strPassword = new String(console.readPassword("password: "));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		loginAckEvent = m_clientStub.syncLoginCM(strUserName, strPassword);
		if(loginAckEvent != null)
		{
			// print login result
			if(loginAckEvent.isValidUser() == 0)
			{
				System.err.println("This client fails authentication by the default server!");
			}
			else if(loginAckEvent.isValidUser() == -1)
			{
				System.err.println("This client is already in the login-user list!");
			}
			else
			{
				System.out.println("This client successfully logs in to the default server.");
			}			
		}
		else
		{
			System.err.println("failed the login request!");
		}

		System.out.println("======");		
	}
	
	// DS 로그아웃
	public void testLogoutDS()
	{
		boolean bRequestResult = false;
		System.out.println("====== logout from default server");
		bRequestResult = m_clientStub.logoutCM();
		if(bRequestResult)
			System.out.println("successfully sent the logout request.");
		else
			System.err.println("failed the logout request!");
		System.out.println("======");
	}
	
	// 세션 연결
	public void testJoinSession()
	{
		String strSessionName = null;
		boolean bRequestResult = false;
		System.out.println("====== join a session");
		System.out.print("session name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bRequestResult = m_clientStub.joinSession(strSessionName);
		if(bRequestResult)
			System.out.println("successfully sent the session-join request.");
		else
			System.err.println("failed the session-join request!");
		System.out.println("======");
	}
	
	// 세션 동시 연결
	public void testSyncJoinSession()
	{
		CMSessionEvent se = null;
		String strSessionName = null;
		System.out.println("====== join a session");
		System.out.print("session name: ");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			strSessionName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		se = m_clientStub.syncJoinSession(strSessionName);
		if(se != null)
		{
			System.out.println("successfully joined a session that has ("+se.getGroupNum()+") groups.");
		}
		else
		{
			System.err.println("failed the session-join request!");
		}
		
		System.out.println("======");		
	}
	
	// 세션 나가기
	public void testLeaveSession()
	{
		boolean bRequestResult = false;
		System.out.println("====== leave the current session");
		bRequestResult = m_clientStub.leaveSession();
		if(bRequestResult)
			System.out.println("successfully sent the leave-session request.");
		else
			System.err.println("failed the leave-session request!");
		System.out.println("======");
	}
	
	// Dummy Event 생성
	public void testDummyEvent()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group!");
			return;
		}
		
		System.out.println("====== test CMDummyEvent in current group");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("input message: ");
		String strInput = null;
		try {
			strInput = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CMDummyEvent due = new CMDummyEvent();
		due.setHandlerSession(myself.getCurrentSession());
		due.setHandlerGroup(myself.getCurrentGroup());
		due.setDummyInfo(strInput);
		m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
		due = null;
		
		System.out.println("======");
	}
	
	
	// 유저 이벤트 생성
	public void testUserEvent()
	{
		String strInput = null;
		String strReceiver = null;
		boolean bEnd = false;
		String[] strTokens = null;
		int nValueByteNum = -1;
		CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
		
		if(myself.getState() != CMInfo.CM_SESSION_JOIN)
		{
			System.out.println("You should join a session and a group!");
			return;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== test CMUserEvent");
		System.out.println("data type: CM_INT(0) CM_LONG(1) CM_FLOAT(2) CM_DOUBLE(3) CM_CHAR(4) CM_STR(5) CM_BYTES(6)");
		System.out.println("Type \"end\" to stop.");
		
		CMUserEvent ue = new CMUserEvent();
		ue.setStringID("testID");
		ue.setHandlerSession(myself.getCurrentSession());
		ue.setHandlerGroup(myself.getCurrentGroup());
		while(!bEnd)
		{
			System.out.println("If the data type is CM_BYTES, the number of bytes must be given "
					+ "in the third parameter.");
			System.out.print("(data type, field name, value): ");
			try {
				strInput = br.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ue.removeAllEventFields();
				ue = null;
				return;
			}
			
			if(strInput.equals("end"))
			{
				bEnd = true;
			}
			else
			{
				strInput.trim();
				strTokens = strInput.split("\\s+");
				if(Integer.parseInt(strTokens[0]) == CMInfo.CM_BYTES)
				{
					nValueByteNum = Integer.parseInt(strTokens[2]);
					if(nValueByteNum < 0)
					{
						System.out.println("CMClientApp.testUserEvent(), Invalid nValueByteNum("
								+nValueByteNum+")");
						ue.removeAllEventFields();
						ue = null;
						return;
					}
					byte[] valueBytes = new byte[nValueByteNum];
					for(int i = 0; i < nValueByteNum; i++)
						valueBytes[i] = 1;	// dummy data
					ue.setEventBytesField(strTokens[1], nValueByteNum, valueBytes);	
				}
				else
					ue.setEventField(Integer.parseInt(strTokens[0]), strTokens[1], strTokens[2]);
			}
		}
		
		System.out.print("receiver: ");
		try {
			strReceiver = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.send(ue, strReceiver);

		System.out.println("======");
		
		ue.removeAllEventFields();
		ue = null;
		return;
	}
	
	// 유저 정보
	public void testCurrentUserStatus()
	{
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		System.out.println("------ for the default server");
		System.out.println("name("+myself.getName()+"), session("+myself.getCurrentSession()+"), group("
				+myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"), state("
				+myself.getState()+"), attachment download scheme("+confInfo.getAttachDownloadScheme()+").");
		
		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while(iter.hasNext())
		{
			CMServer tserver = iter.next();
			if(tserver.getNonBlockSocketChannelInfo().findChannel(0) != null)
			{
				System.out.println("------ for additional server["+tserver.getServerName()+"]");
				System.out.println("current session("+tserver.getCurrentSessionName()+
						"), current group("+tserver.getCurrentGroupName()+"), state("
						+tserver.getClientState()+").");
				
			}
		}
		
		return;
	}
	
	
	// 유저 등록
	public void testRegisterUser()
	{
		String strName = null;
		String strPasswd = null;
		String strRePasswd = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== register a user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
			if(console == null)
			{
				System.out.print("Input password: ");
				strPasswd = br.readLine();
				System.out.print("Retype password: ");
				strRePasswd = br.readLine();
			}
			else
			{
				strPasswd = new String(console.readPassword("Input password: "));
				strRePasswd = new String(console.readPassword("Retype password: "));
			}
			
			if(!strPasswd.equals(strRePasswd))
			{
				System.err.println("Password input error");
				return;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_clientStub.registerUser(strName, strPasswd);
		System.out.println("======");
		return;
	}
	
	// 유저 탈퇴
	public void testDeregisterUser()
	{
		String strName = null;
		String strPasswd = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== Deregister a user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
			if(console == null)
			{
				System.out.print("Input password: ");
				strPasswd = br.readLine();
			}
			else
			{
				strPasswd = new String(console.readPassword("Input password: "));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.deregisterUser(strName, strPasswd);
		System.out.println("======");
		return;
	}
	
	
	// 아이디 찾기
	public void testFindRegisteredUser()
	{
		String strName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== search for a registered user");
		try {
			System.out.print("Input user name: ");
			strName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_clientStub.findRegisteredUser(strName);
		System.out.println("======");
		return;
	}
	
	// 서버 정보 조회
	public void testRequestServerInfo()
	{
		System.out.println("====== request additional server information");
		m_clientStub.requestServerInfo();
	}
	
	// 추가 서버 연결
	public void testConnectToServer()
	{
		System.out.println("====== connect to a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.connectToServer(strServerName);
		return;
	}
	
	// 추가 서버 연결 끊음
	public void testDisconnectFromServer()
	{
		System.out.println("===== disconnect from a designated server");
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Input a server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		m_clientStub.disconnectFromServer(strServerName);
		return;
	}
	
	// 추가 서버 로그인
	public void testLoginServer()
	{
		String strServerName = null;
		String user = null;
		String password = null;
		Console console = System.console();
		if(console == null)
		{
			System.err.println("Unable to obtain console.");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("====== log in to a designated server");
		try {
			System.out.print("Input server name: ");
			strServerName = br.readLine();
			if( strServerName.equals(m_clientStub.getDefaultServerName()) )	// login to a default server
			{
				System.out.print("User name: ");
				user = br.readLine();
				if(console == null)
				{
					System.out.print("Password: ");
					password = br.readLine();
				}
				else
				{
					password = new String(console.readPassword("Password: "));
				}
				
				m_clientStub.loginCM(user, password);
			}
			else // use the login info for the default server
			{
				CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
				user = myself.getName();
				password = myself.getPasswd();
				m_clientStub.loginCM(strServerName, user, password);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("======");
		return;
	}
	
	
	// 추가 서버 로그아웃
	public void testLogoutServer()
	{
		String strServerName = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("====== log out from a designated server");
		System.out.print("Input server name: ");
		try {
			strServerName = br.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		m_clientStub.logoutCM(strServerName);
		System.out.println("======");
	}
	
	// 연결 정보 출력
	public void testPrintConfigurations()
	{
		String[] strConfigurations;
		System.out.print("========== print all current configurations\n");
		Path confPath = m_clientStub.getConfigurationHome().resolve("cm-client.conf");
		strConfigurations = CMConfigurator.getConfigurations(confPath.toString());
		
		System.out.print("configuration file path: "+confPath.toString()+"\n");
		for(String strConf : strConfigurations)
		{
			String[] strFieldValuePair;
			strFieldValuePair = strConf.split("\\s+");
			System.out.print(strFieldValuePair[0]+" = "+strFieldValuePair[1]+"\n");
		}
		
	}
	

	
	
	public static void main(String[] args)
	{
		CMClientApp client = new CMClientApp();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setAppEventHandler(client.getClientEventHandler());
		cmStub.startCM();
	}
	
}
