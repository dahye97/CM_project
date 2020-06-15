import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import java.io.*;
import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
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
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.info.CMInteractionInfo;
import kr.ac.konkuk.ccslab.cm.info.CMSNSInfo;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContent;
import kr.ac.konkuk.ccslab.cm.sns.CMSNSContentList;
import kr.ac.konkuk.ccslab.cm.stub.CMClientStub;

public class IngstaClientEventHandler implements CMAppEventHandler{
   private IngstaClient m_client;
   private CMClientStub m_clientStub;
   private long m_lStartTime;
   private String m_friendList;
   private ArrayList<contentList> m_content = new ArrayList<contentList>();
   private boolean isStarted = false;
   
   public IngstaClientEventHandler(CMClientStub clientStub, IngstaClient client) {
      m_client = client;
      m_clientStub = clientStub;
      m_lStartTime = 0;
      m_friendList = "";
   }
   

   public void setStartTime(long time) {
      m_lStartTime = time;
   }
   
   public long getStartTime() {
      return m_lStartTime;
   }
   
   public String getFriendList() {
      return m_friendList;
   }
   
   public ArrayList<contentList> getContentList() {
      return m_content;
   }
   
   
   
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
         contentList.removeAllSNSContents();   // clear the content list to which downloaded contents will be stored
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