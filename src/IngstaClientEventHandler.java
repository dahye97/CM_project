import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JOptionPane;

import com.mysql.fabric.xmlrpc.Client;

import java.io.*;
import java.awt.*;

import kr.ac.konkuk.ccslab.cm.entity.CMServerInfo;
import kr.ac.konkuk.ccslab.cm.entity.CMSessionInfo;
import kr.ac.konkuk.ccslab.cm.event.CMDataEvent;
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
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventCONNACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBCOMP;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBLISH;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREC;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventPUBREL;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventSUBACK;
import kr.ac.konkuk.ccslab.cm.event.mqttevent.CMMqttEventUNSUBACK;
import kr.ac.konkuk.ccslab.cm.info.CMConfigurationInfo;
import kr.ac.konkuk.ccslab.cm.info.CMFileTransferInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMFileTransferManager;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;
import kr.ac.konkuk.ccslab.cm.util.CMUtil;

public class IngstaClientEventHandler implements CMAppEventHandler{
   //private JTextArea m_outTextArea;
   private IngstaClient m_client;
   private CMClientStub m_clientStub;
   private long m_lDelaySum;   // for forwarding simulation
   // for delay of SNS content downloading, distributed file processing, server response,
   // csc-ftp, c2c-ftp
   private long m_lStartTime;
   private int m_nEstDelaySum;   // for SNS downloading simulation
   private int m_nSimNum;      // for simulation of multiple sns content downloading
   private FileOutputStream m_fos;   // for storing downloading delay of multiple SNS content
   private PrintWriter m_pw;      //
   private int m_nCurrentServerNum;   // for distributed file processing
   private int m_nRecvPieceNum;      // for distributed file processing
   private boolean m_bDistFileProc;   // for distributed file processing
   private String m_strExt;         // for distributed file processing
   private String[] m_filePieces;      // for distributed file processing
   private boolean m_bReqAttachedFile;   // for storing the fact that the client requests an attachment
   private int m_nMinNumWaitedEvents;  // for checking the completion of asynchronous castrecv service
   private int m_nRecvReplyEvents;      // for checking the completion of asynchronous castrecv service
   private String m_friendList;
   private String m_contentList;
   private ArrayList<contentList> m_content = new ArrayList<contentList>();
   private boolean isStarted = false;
   
   // information for csc-ftp and c2c-ftp experiments
   private String m_strFileSender;
   private String m_strFileReceiver;
   private File[] m_arraySendFiles;
   private int m_nTotalNumFTPSessions;
   private int m_nCurNumFTPSessions;
   // information for c2c-ftp experiments
   private boolean m_bStartC2CFTPSession;
   private int m_nTotalNumFilesPerSession;
   private int m_nCurNumFilesPerSession;
   
   
   public IngstaClientEventHandler(CMClientStub clientStub, IngstaClient client)
   {
      m_client = client;
      //m_outTextArea = textArea;
      m_clientStub = clientStub;
      m_lDelaySum = 0;
      m_lStartTime = 0;
      m_nEstDelaySum = 0;
      m_nSimNum = 0;
      m_fos = null;
      m_pw = null;
      m_nCurrentServerNum = 0;
      m_nRecvPieceNum = 0;
      m_bDistFileProc = false;
      m_strExt = null;
      m_filePieces = null;
      m_bReqAttachedFile = false;
      m_nMinNumWaitedEvents = 0;
      m_nRecvReplyEvents = 0;
      m_friendList = "";
      
      m_strFileSender = null;
      m_strFileReceiver = null;
      m_arraySendFiles = null;
      m_nTotalNumFTPSessions = 0;
      m_nCurNumFTPSessions = 0;
      m_bStartC2CFTPSession = false;
      m_nTotalNumFilesPerSession = 0;
      m_nCurNumFilesPerSession = 0;
   }
   
   //////////////////////////////////////////////////////////////////////////////
   // get/set methods
   
   public void setStartTime(long time)
   {
      m_lStartTime = time;
   }
   
   public long getStartTime()
   {
      return m_lStartTime;
   }
   
   public void setFileOutputStream(FileOutputStream fos)
   {
      m_fos = fos;
   }
   
   public FileOutputStream getFileOutputStream()
   {
      return m_fos;
   }
   
   public void setPrintWriter(PrintWriter pw)
   {
      m_pw = pw;
   }
   
   public PrintWriter getPrintWriter()
   {
      return m_pw;
   }
   
   public void setSimNum(int num)
   {
      m_nSimNum = num;
   }
   
   public int getSimNum()
   {
      return m_nSimNum;
   }
   
   public void setCurrentServerNum(int num)
   {
      m_nCurrentServerNum = num;
   }
   
   public int getCurrentServerNum()
   {
      return m_nCurrentServerNum;
   }
   
   public void setRecvPieceNum(int num)
   {
      m_nRecvPieceNum = num;
   }
   
   public int getRecvPieceNum()
   {
      return m_nRecvPieceNum;
   }
   
   public void setDistFileProc(boolean b)
   {
      m_bDistFileProc = b;
   }
   
   public boolean isDistFileProc()
   {
      return m_bDistFileProc;
   }
   
   public void setFileExtension(String ext)
   {
      m_strExt = ext;
   }
   
   public String getFileExtension()
   {
      return m_strExt;
   }
   
   public void setFilePieces(String[] pieces)
   {
      m_filePieces = pieces;
   }
   
   public String[] getFilePieces()
   {
      return m_filePieces;
   }
   
   public void setReqAttachedFile(boolean bReq)
   {
      m_bReqAttachedFile = bReq;
   }
   
   public boolean isReqAttachedFile()
   {
      return m_bReqAttachedFile;
   }

   public void setMinNumWaitedEvents(int num)
   {
      m_nMinNumWaitedEvents = num;
   }
   
   public int getMinNumWaitedEvents()
   {
      return m_nMinNumWaitedEvents;
   }
   
   public void setRecvReplyEvents(int num)
   {
      m_nRecvReplyEvents = num;
   }
   
   public int getRecvReplyEvents()
   {
      return m_nRecvReplyEvents;
   }
   
   public void setFileSender(String strFileSender)
   {
      m_strFileSender = strFileSender;
   }
   
   public String getFileSender()
   {
      return m_strFileSender;
   }
   
   public void setFileReceiver(String strFileReceiver)
   {
      m_strFileReceiver = strFileReceiver;
   }
   
   public String getFileReceiver()
   {
      return m_strFileReceiver;
   }
   
   public void setSendFileArray(File[] arraySendFiles)
   {
      m_arraySendFiles = arraySendFiles;
   }
   
   public File[] getSendFileArray()
   {
      return m_arraySendFiles;
   }
   
   public void setTotalNumFTPSessions(int nNum)
   {
      m_nTotalNumFTPSessions = nNum;
   }
   
   public int getTotalNumFTPSessions()
   {
      return m_nTotalNumFTPSessions;
   }
   
   public void setCurNumFTPSessions(int nNum)
   {
      m_nCurNumFTPSessions = nNum;
   }
   
   public int getCurNumFTPSessions()
   {
      return m_nCurNumFTPSessions;
   }
   
   public void setIsStartC2CFTPSession(boolean bStart)
   {
      m_bStartC2CFTPSession = bStart;
   }
   
   public boolean isStartC2CFTPSession()
   {
      return m_bStartC2CFTPSession;
   }
   
   public void setTotalNumFilesPerSession(int nNum)
   {
      m_nTotalNumFilesPerSession = nNum;
   }
   
   public int getTotalNumFilesPerSession()
   {
      return m_nTotalNumFilesPerSession;
   }
   
   public void setCurNumFilesPerSession(int nNum)
   {
      m_nCurNumFilesPerSession = nNum;
   }
   
   public int getCurNumFilesPerSession()
   {
      return m_nCurNumFilesPerSession;
   }
   
   public String getFriendList() {
	   return m_friendList;
   }
   
   /*
   public String getContentList() {
	   return m_contentList;
   }
   */
   
   public ArrayList<contentList> getContentList() {
	   return m_content;
   }
      
   //////////////////////////////////////////////////////////////////////////////
   
   @Override
   public void processEvent(CMEvent cme) {
      switch(cme.getType())
      {
      	case CMInfo.CM_SESSION_EVENT:
			processSessionEvent(cme);
			break;
		case CMInfo.CM_SNS_EVENT:
			processSNSEvent(cme);
			break;
		case CMInfo.CM_MQTT_EVENT:
			processMqttEvent(cme);
			break;
		default:
			return;
      }   
   }
   
   
   //CM_SNS_EVENT
   private void processSNSEvent(CMEvent cme)
	{
		CMSNSInfo snsInfo = m_clientStub.getCMInfo().getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		CMSNSEvent se = (CMSNSEvent) cme;
		int i = 0;
		
		switch(se.getID())
		{
		case CMSNSEvent.CONTENT_UPLOAD_RESPONSE:
			if( se.getReturnCode() == 1 )
			{
				System.out.println("Content upload succeeded.");
				m_client.mqttPublish(m_clientStub.getMyself().getName());
			}
			else
			{
				System.out.println("Content upload failed.");
			}
			System.out.println("user("+se.getUserName()+"), seqNum("+se.getContentID()+"), time("
					+se.getDate()+").");
			
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_RESPONSE:
			contentList.removeAllSNSContents();	// clear the content list to which downloaded contents will be stored
			m_nEstDelaySum = 0;
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD_END:
			processCONTENT_DOWNLOAD_END(se);
			if(!isStarted) {
				isStarted = true;
				m_client.CMmainFrame();
			}
			break;
		case CMSNSEvent.CONTENT_DOWNLOAD:
			ArrayList<String> fNameList = null;
			if(se.getNumAttachedFiles() > 0)
			{
				ArrayList<String> rcvList = se.getFileNameList();
				fNameList = new ArrayList<String>();
				for(i = 0; i < rcvList.size(); i++)
				{
					fNameList.add(rcvList.get(i));
				}
			}
			contentList.addSNSContent(se.getContentID(), se.getDate(), se.getWriterName(), se.getMessage(),
					se.getNumAttachedFiles(), se.getReplyOf(), se.getLevelOfDisclosure(), fNameList);
			//System.out.println("transmitted delay: "+se.getEstDelay());
			m_nEstDelaySum += se.getEstDelay();
			break;
		
		case CMSNSEvent.RESPONSE_ATTACHED_FILE:
			if(se.getReturnCode() == 1)
			{
				System.out.println("The request for an attached file ["+se.getFileName()
						+"] of content ID ["+se.getContentID()+"] written by ["+se.getWriterName()
						+"] is succeeded.");
			}
			else
			{
				System.out.println("The request for an attached file ["+se.getFileName()
						+"] of content ID ["+se.getContentID()+"] written by ["+se.getWriterName()
						+"] is failed!");
			}
			break;
		case CMSNSEvent.ADD_NEW_FRIEND_ACK:
			if(se.getReturnCode() == 1)
			{
				System.out.println("["+se.getUserName()+"] succeeds to add a friend["
						+se.getFriendName()+"].");
			}
			else
			{
				System.out.println("["+se.getUserName()+"] fails to add a friend["
						+se.getFriendName()+"].");
			}
			break;
		case CMSNSEvent.REMOVE_FRIEND_ACK:
			if(se.getReturnCode() == 1)
			{
				System.out.println("["+se.getUserName()+"] succeeds to remove a friend["
						+se.getFriendName()+"].");
			}
			else
			{
				System.out.println("["+se.getUserName()+"] fails to remove a friend["
						+se.getFriendName()+"].");
			}
			break;
		case CMSNSEvent.RESPONSE_FRIEND_LIST:
			int friendNum = se.getFriendList().size();
			String[] friend = new String[friendNum];
			m_friendList = "";
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" friends "
					+"of total "+se.getTotalNumFriends()+" friends.");
			System.out.print("Friends");
			for(i = 0; i < friendNum; i++)
			{
				friend[i] = se.getFriendList().get(i);
				m_friendList += ("\t "+(i+1)+"\t      |");
				m_friendList += ("  \t"+se.getFriendList().get(i)+"\n");
			}
			m_client.setFriendList(m_friendList);
			m_client.setFriendName(friend);
			System.out.println();
			break;
		case CMSNSEvent.RESPONSE_FRIEND_REQUESTER_LIST:
			m_friendList = "";
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" requesters "
					+"of total "+se.getTotalNumFriends()+" requesters.");
			System.out.print("Requesters");
			for(i = 0; i < se.getFriendList().size(); i++)
			{
				m_friendList += ("\t "+(i+1)+"\t      |");
				m_friendList += ("  \t"+se.getFriendList().get(i)+"\n");
			}
			m_client.setFriendList(m_friendList);
			System.out.println();
			break;
		case CMSNSEvent.RESPONSE_BI_FRIEND_LIST:
			m_friendList = "";
			System.out.println("["+se.getUserName()+"] receives "+se.getNumFriends()+" bi-friends "
					+"of total "+se.getTotalNumFriends()+" bi-friends.");
			System.out.print("Bi-friends");
			for(i = 0; i < se.getFriendList().size(); i++)
			{
				m_friendList += ("\t "+(i+1)+"\t      |");
				m_friendList += ("  \t"+se.getFriendList().get(i)+"\n");
			}
			m_client.setFriendList(m_friendList);
			System.out.println();
			break;
		case CMSNSEvent.CHANGE_ATTACH_DOWNLOAD_SCHEME:
			String[] attachScheme = {"Full", "Thumbnail", "Prefetching", "None"};
			System.out.println("Server changes the scheme for attachment download of SNS content to ["
					+attachScheme[se.getAttachDownloadScheme()]+"].");
			break;
		case CMSNSEvent.PREFETCH_COMPLETED:
			processPREFETCH_COMPLETED(se);
			break;
		}
		return;
	}
   
   
   //컨텐츠 보기
   private void processCONTENT_DOWNLOAD_END(CMSNSEvent se) {
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMSNSInfo snsInfo = m_clientStub.getCMInfo().getSNSInfo();
		CMSNSContentList contentList = snsInfo.getSNSContentList();
		Iterator<CMSNSContent> iter = null;
		int index = 0;
		m_content = new ArrayList<contentList>();
		
		
		iter = contentList.getContentList().iterator();
		while(iter.hasNext() && index < 3) {
			CMSNSContent cont = iter.next();
			ArrayList<File> filePath = new ArrayList<File>();
			
			if(cont.getNumAttachedFiles() > 0) {
				ArrayList<String> fNameList = cont.getFileNameList();
				for(int j = 0; j < fNameList.size(); j++) {
					m_clientStub.requestAttachedFileOfSNSContent(fNameList.get(j));
					String fPath = confInfo.getTransferedFileHome().toString() + File.separator + fNameList.get(j);
					filePath.add(new File(fPath));
				}	
			}
			contentList loadContent = new contentList(cont.getDate(), cont.getWriterName(), filePath, cont.getMessage());
			m_content.add(loadContent);
			index++;
		}
		
		return;
   }
      
   
   //CM_SESSION_EVENT
   private void processSessionEvent(CMEvent cme)
   {
      long lDelay = 0;
      CMSessionEvent se = (CMSessionEvent)cme;
      switch(se.getID())
      {
      case CMSessionEvent.LOGIN_ACK:
         lDelay = System.currentTimeMillis() - m_lStartTime;
         System.out.println("LOGIN_ACK delay: "+lDelay+" ms.\n");
         if(se.isValidUser() == 0)
         {
            System.out.println("This client fails authentication by the default server!\n");
         }
         else if(se.isValidUser() == -1)
         {
            System.out.println("This client is already in the login-user list!\n");
         }
         else
         {
            System.out.println("This client successfully logs in to the default server.\n");
            CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
            m_client.getLoginFrame().dispose();
            m_client.mqttConnect();
            m_client.init();
            m_client.setTitle("CM Client ["+interInfo.getMyself().getName()+"]");
         }
         break;
      case CMSessionEvent.REGISTER_USER_ACK:
         if( se.getReturnCode() == 1 )
         {
            System.out.println("User["+se.getUserName()+"] successfully registered at time["
                     +se.getCreationTime()+"].\n");
            JOptionPane.showMessageDialog(null, "회원가입 성공");
         }
         else
         {
            System.out.println("User["+se.getUserName()+"] failed to register!\n");
            JOptionPane.showMessageDialog(null, "회원가입 실패");
         }
         break;
      case CMSessionEvent.DEREGISTER_USER_ACK:
         if( se.getReturnCode() == 1 )
         {
            System.out.println("User["+se.getUserName()+"] successfully deregistered.\n");
         }
         else
         {
            System.out.println("User["+se.getUserName()+"] failed to deregister!\n");
         }
         break;
      case CMSessionEvent.FIND_REGISTERED_USER_ACK:
         if( se.getReturnCode() == 1 )
         {
            System.out.println("User profile search succeeded: user["+se.getUserName()
                  +"], registration time["+se.getCreationTime()+"].\n");
         }
         else
         {
            System.out.println("User profile search failed: user["+se.getUserName()+"]!\n");
         }
         break;
      case CMSessionEvent.UNEXPECTED_SERVER_DISCONNECTION:
         m_client.setTitle("CM Client");
         break;
      case CMSessionEvent.INTENTIONALLY_DISCONNECT:
         m_client.setTitle("CM Client");
         break;
      default:
         return;
      }
   }
   
   
   //SNS Reach Time?
   private void processPREFETCH_COMPLETED(CMSNSEvent se)
   {
      String strUserName = se.getUserName();
      int nDelay = (int)(System.currentTimeMillis()-m_lStartTime);
      System.out.println("["+strUserName+"] prefetching attachments completed, total delay: "+nDelay+" ms\n");
      return;
   }
   
   
   //CM_MQTT_EVENT
   private void processMqttEvent(CMEvent cme)
	{
		switch(cme.getID())
		{
		case CMMqttEvent.CONNACK:
			CMMqttEventCONNACK conackEvent = (CMMqttEventCONNACK)cme;
			System.out.println("["+conackEvent.getSender()+"] sent CMMqttEvent.CONNACK, "
					+ "[return code: "+conackEvent.getReturnCode()+"]");
			break;
		case CMMqttEvent.PUBLISH:
			CMMqttEventPUBLISH pubEvent = (CMMqttEventPUBLISH)cme;
			System.out.println("["+pubEvent.getSender()+"] sent CMMqttEvent.PUBLISH, "
					+ "[packet ID: "+pubEvent.getPacketID()+"], [topic: "
					+pubEvent.getTopicName()+"], [msg: "+pubEvent.getAppMessage()
					+"], [QoS: "+pubEvent.getQoS()+"]");
			break;
		case CMMqttEvent.PUBACK:
			CMMqttEventPUBACK pubackEvent = (CMMqttEventPUBACK)cme;
			System.out.println("["+pubackEvent.getSender()+"] sent CMMqttEvent.PUBACK, "
					+ "[packet ID: "+pubackEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.PUBREC:
			CMMqttEventPUBREC pubrecEvent = (CMMqttEventPUBREC)cme;
			System.out.println("["+pubrecEvent.getSender()+"] sent CMMqttEvent.PUBREC, "
					+ "[packet ID: "+pubrecEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.PUBREL:
			CMMqttEventPUBREL pubrelEvent = (CMMqttEventPUBREL)cme;
			System.out.println("["+pubrelEvent.getSender()+"] sent CMMqttEventPUBREL, "
					+ "[packet ID: "+pubrelEvent.getPacketID()+"]");
			m_client.DownloadNewSNSContent();
			break;
		case CMMqttEvent.PUBCOMP:
			CMMqttEventPUBCOMP pubcompEvent = (CMMqttEventPUBCOMP)cme;
			System.out.println("["+pubcompEvent.getSender()+"] sent CMMqttEvent.PUBCOMP, "
					+ "[packet ID: "+pubcompEvent.getPacketID()+"]");
			break;
		case CMMqttEvent.SUBACK:
			CMMqttEventSUBACK subackEvent = (CMMqttEventSUBACK)cme;
			System.out.println("["+subackEvent.getSender()+"] sent CMMqttEvent.SUBACK, "
					+subackEvent.getReturnCodeList());
			break;
		case CMMqttEvent.UNSUBACK:
			CMMqttEventUNSUBACK unsubackEvent = (CMMqttEventUNSUBACK)cme;
			System.out.println("["+unsubackEvent.getSender()+"] sent CMMqttEvent.UNSUBACK");
			break;
		}
		return;
	}
   
   
   public void sleep(int sec) {
	   try {
		   Thread.sleep(sec);
	   }
	   catch (InterruptedException e){
		   e.printStackTrace();
	   }
   }
   
   
}



class contentList {
	String date;
	String writer;
	ArrayList<File> file = new ArrayList<File>();
	String content;
	
	contentList() {
		date = "";
		writer = "";
		file = null;
		content = "No Content";
	}
	
	public contentList(String date, String writer, ArrayList<File> file, String content) {
		this.date = date;
		this.writer = writer;
		this.file = file;
		this.content = content;
	}
	
}