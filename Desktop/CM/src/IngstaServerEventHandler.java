import kr.ac.konkuk.ccslab.cm.event.CMEvent;
import kr.ac.konkuk.ccslab.cm.event.CMInterestEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSNSEvent;
import kr.ac.konkuk.ccslab.cm.event.CMSessionEvent;
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
import kr.ac.konkuk.ccslab.cm.info.CMInfo;
import kr.ac.konkuk.ccslab.cm.manager.CMDBManager;
import kr.ac.konkuk.ccslab.cm.stub.CMServerStub;

public class IngstaServerEventHandler implements CMAppEventHandler {
   private IngstaServer m_server;
   private CMServerStub m_serverStub;
   
   public IngstaServerEventHandler(CMServerStub serverStub, IngstaServer server)
   {
      m_server = server;
      m_serverStub = serverStub;
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
   
   private void processSessionEvent(CMEvent cme)
   {
      CMConfigurationInfo confInfo = m_serverStub.getCMInfo().getConfigurationInfo();
      CMSessionEvent se = (CMSessionEvent) cme;
      switch(se.getID())
      {
      case CMSessionEvent.LOGIN:
         printMessage("["+se.getUserName()+"] requests login.\n");
         if(confInfo.isLoginScheme())
         {
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
         printMessage("["+se.getUserName()+"] logs out.\n");
         break;
      case CMSessionEvent.REQUEST_SESSION_INFO:
         printMessage("["+se.getUserName()+"] requests session information.\n");
         break;
      case CMSessionEvent.CHANGE_SESSION:
         printMessage("["+se.getUserName()+"] changes to session("+se.getSessionName()+").\n");
         break;
      case CMSessionEvent.JOIN_SESSION:
         printMessage("["+se.getUserName()+"] requests to join session("+se.getSessionName()+").\n");
         break;
      case CMSessionEvent.LEAVE_SESSION:
         printMessage("["+se.getUserName()+"] leaves a session("+se.getSessionName()+").\n");
         break;
      case CMSessionEvent.ADD_NONBLOCK_SOCKET_CHANNEL:
         printMessage("["+se.getChannelName()+"] request to add a nonblocking SocketChannel with key("
         +se.getChannelNum()+").\n");
         break;
      case CMSessionEvent.REGISTER_USER:
         printMessage("User registration requested by user["+se.getUserName()+"].\n");
         break;
      case CMSessionEvent.DEREGISTER_USER:
         printMessage("User deregistration requested by user["+se.getUserName()+"].\n");
         break;
      case CMSessionEvent.FIND_REGISTERED_USER:
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
         printMessage("["+ie.getUserName()+"] enters group("+ie.getCurrentGroup()+") in session("
               +ie.getHandlerSession()+").\n");
         break;
      case CMInterestEvent.USER_LEAVE:
         printMessage("["+ie.getUserName()+"] leaves group("+ie.getHandlerGroup()+") in session("
               +ie.getHandlerSession()+").\n");
         break;
      case CMInterestEvent.USER_TALK:
         printMessage("("+ie.getHandlerSession()+", "+ie.getHandlerGroup()+")\n");
         printMessage("<"+ie.getUserName()+">: "+ie.getTalk()+"\n");
         break;
      default:
         return;
      }
   }

   
   private void processSNSEvent(CMEvent cme)
   {
      CMSNSEvent se = (CMSNSEvent) cme;
      switch(se.getID())
      {
      case CMSNSEvent.CONTENT_DOWNLOAD_REQUEST:
         printMessage("["+se.getUserName()+"] requests SNS contents starting at: offset("
               +se.getContentOffset()+").\n");
         break;
      case CMSNSEvent.CONTENT_DOWNLOAD_END_RESPONSE:
         if(se.getReturnCode() == 1)
         {
            printMessage("["+se.getUserName()+"] has received SNS contents starting at "
                  +se.getContentOffset()+" successfully.\n");
         }
         else
         {
            printMessage("!! ["+se.getUserName()+" had a problem while receiving SNS "
                  + "contents starting at "+se.getContentOffset()+".\n");
         }
         break;
      case CMSNSEvent.CONTENT_UPLOAD_REQUEST:
         printMessage("content upload requested by ("+se.getUserName()+"), message("+se.getMessage()
               +"), #attachement("+se.getNumAttachedFiles()+"), replyID("+se.getReplyOf()
               +"), lod("+se.getLevelOfDisclosure()+")\n");
         break;
      case CMSNSEvent.REQUEST_ATTACHED_FILE:
         printMessage("["+se.getUserName()+"] requests an attached file ["
               +se.getFileName()+"] of SNS content ID["+se.getContentID()+"] written by ["
               +se.getWriterName()+"].\n");
         break;
      }
      return;
   }
   
   private void processMqttEvent(CMEvent cme)
   {
      switch(cme.getID())
      {
      case CMMqttEvent.CONNECT:
         CMMqttEventCONNECT conEvent = (CMMqttEventCONNECT)cme;
         printMessage("["+conEvent.getUserName()
            +"] requests to connect MQTT service.\n");
         break;
      case CMMqttEvent.PUBLISH:
         CMMqttEventPUBLISH pubEvent = (CMMqttEventPUBLISH)cme;
         printMessage("["+pubEvent.getSender()+"] requests to publish, ");
         printMessage("[packet ID: "+pubEvent.getPacketID()
               +"], [topic: "+pubEvent.getTopicName()+"], [msg: "
               +pubEvent.getAppMessage()+"], [qos: "+pubEvent.getQoS()+"]\n");
         break;
      case CMMqttEvent.PUBACK:
         CMMqttEventPUBACK pubackEvent = (CMMqttEventPUBACK)cme;
         printMessage("["+pubackEvent.getSender()+"] sent CMMqttEvent.PUBACK, "
               + "[packet ID: "+pubackEvent.getPacketID()+"]\n");
         break;
      case CMMqttEvent.PUBREC:
         CMMqttEventPUBREC pubrecEvent = (CMMqttEventPUBREC)cme;
         printMessage("["+pubrecEvent.getSender()+"] sent CMMqttEvent.PUBREC, "
               + "[packet ID: "+pubrecEvent.getPacketID()+"]\n");
         break;
      case CMMqttEvent.PUBREL:
         CMMqttEventPUBREL pubrelEvent = (CMMqttEventPUBREL)cme;
         printMessage("["+pubrelEvent.getSender()+"] sent CMMqttEventPUBREL, "
               + "[packet ID: "+pubrelEvent.getPacketID()+"]\n");
         break;
      case CMMqttEvent.PUBCOMP:
         CMMqttEventPUBCOMP pubcompEvent = (CMMqttEventPUBCOMP)cme;
         printMessage("["+pubcompEvent.getSender()+"] sent CMMqttEvent.PUBCOMP, "
               + "[packet ID: "+pubcompEvent.getPacketID()+"]\n");
         break;
      case CMMqttEvent.SUBSCRIBE:
         CMMqttEventSUBSCRIBE subEvent = (CMMqttEventSUBSCRIBE)cme;
         printMessage("["+subEvent.getSender()+"] requests to subscribe, "
               + subEvent.getTopicQoSList()+"\n");
         break;
      case CMMqttEvent.UNSUBSCRIBE:
         CMMqttEventUNSUBSCRIBE unsubEvent = (CMMqttEventUNSUBSCRIBE)cme;
         printMessage("["+unsubEvent.getSender()+"] requests to unsubscribe, "
               + unsubEvent.getTopicList()+"\n");
         break;
      case CMMqttEvent.DISCONNECT:
         CMMqttEventDISCONNECT disconEvent = (CMMqttEventDISCONNECT)cme;
         printMessage("["+disconEvent.getSender()
            +"] requests to disconnect MQTT service.\n");
         break;
      }
      return;
   }
   
   
   private void printMessage(String strText)
   {
      m_server.printMessage(strText);
   }

}