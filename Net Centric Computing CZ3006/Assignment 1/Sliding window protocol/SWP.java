import java.util.Timer;
import java.util.TimerTask;

/*===============================================================*
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Author: Professor SUN Chengzheng                             *
 *          School of Computer Engineering                       *
 *          Nanyang Technological University                     *
 *          Singapore 639798                                     *
 *===============================================================*/

public class SWP {

/*========================================================================*
 the following are provided, do not change them!!
 *========================================================================*/
   //the following are protocol constants.
   public static final int MAX_SEQ = 7; 
   public static final int NR_BUFS = (MAX_SEQ + 1)/2;

   // the following are protocol variables
   private int oldest_frame = 0;
   private PEvent event = new PEvent();  
   private Packet out_buf[] = new Packet[NR_BUFS];

   //the following are used for simulation purpose only
   private SWE swe = null;
   private String sid = null;  

   //Constructor
   public SWP(SWE sw, String s){
      swe = sw;
      sid = s;
   }

   //the following methods are all protocol related
   private void init(){
      for (int i = 0; i < NR_BUFS; i++){
	     out_buf[i] = new Packet();
      }
   }

   private void wait_for_event(PEvent e){
      swe.wait_for_event(e); //may be blocked
      oldest_frame = e.seq;  //set timeout frame seq
   }

   private void enable_network_layer(int nr_of_bufs) {
   //network layer is permitted to send if credit is available
	     swe.grant_credit(nr_of_bufs);
   }

   private void from_network_layer(Packet p) {
      swe.from_network_layer(p);
   }

   private void to_network_layer(Packet packet) {
	     swe.to_network_layer(packet);
   }

   private void to_physical_layer(PFrame fm)  {
      System.out.println("SWP: Sending frame: seq = " + fm.seq + 
			    " ack = " + fm.ack + " kind = " + 
			    PFrame.KIND[fm.kind] + " info = " + fm.info.data );
      System.out.flush();
      swe.to_physical_layer(fm);
   }

   private void from_physical_layer(PFrame fm) {
      PFrame fm1 = swe.from_physical_layer(); 
	fm.kind = fm1.kind;
	fm.seq = fm1.seq; 
	fm.ack = fm1.ack;
	fm.info = fm1.info;
   }


/*===========================================================================*
 	implement your Protocol Variables and Methods below: 
 *==========================================================================*/

  private boolean no_nak = true;
  private Timer[] timer = new Timer[NR_BUFS];
  private Timer ack_timer = new Timer();
 
  public boolean between(int a, int b, int c){
    return ((a <= b) && (b < c)) || ((c < a) && (a <= b)) || ((b < c) && (c < a));
  }

  public void send_frame(int frame_kind, int frame_nr, int frame_exp, Packet buffer[]){
    // create a new frame for outbound frame
    PFrame s = new PFrame();
    // define the kind of this frame
    s.kind = frame_kind;
    if (frame_kind == PFrame.DATA){
      s.info = buffer[frame_nr % NR_BUFS];
    }
    s.seq = frame_nr;
    s.ack = (frame_exp + MAX_SEQ) % (MAX_SEQ + 1);
    if (frame_kind == PFrame.NAK)  {
      no_nak = false;
    }
    to_physical_layer(s);       // transmit the frame
    if (frame_kind == PFrame.DATA)  {
      start_timer(frame_nr);    // previous frame_nr % NR_BUFS!!!!!!
    }
    stop_ack_timer(); 
  }

  public void protocol6() {
    // outgoing frame's ack number from the inbound data
    int ack_exp = 0;                // lower edge of the sender's window
    // expected frame's seq from the inbound data
    int frame_exp = 0;              // lower edge of the receier's window
    int next_frame_to_send = 0;     // upper edge of the sender's window
    int too_far = NR_BUFS;          // upper edge of the receiver's window
    PFrame r = new PFrame();        // frame for receiving input
    Packet in_buf[] = new Packet[NR_BUFS];     // buffer for inbound data
    boolean arrived[] = new boolean[NR_BUFS];  // arrive or not
    int nbuffered = 0;

    enable_network_layer(NR_BUFS);

    for (int i = 0; i < NR_BUFS; i++){
      arrived[i] = false;           // nothing arrives at first
      in_buf[i] = new Packet();     // initialization of in_buff
    }

    init();                         // initialization of out_buff

  	while(true) {	

      wait_for_event(event);

	    switch (event.type) {

	      case (PEvent.NETWORK_LAYER_READY):
          nbuffered++;
          from_network_layer(out_buf[next_frame_to_send % NR_BUFS]);        // fetch data
          send_frame(PFrame.DATA, next_frame_to_send, frame_exp, out_buf);  // send data
          next_frame_to_send = (next_frame_to_send + 1) % (MAX_SEQ + 1);    // slide window
          break; 

	      case (PEvent.FRAME_ARRIVAL):
          from_physical_layer(r);    // fetch frame
          if (PFrame.KIND[r.kind].equals("DATA")){   
            // send NAK if it's not the expected frame 
            if ((r.seq != frame_exp) && no_nak)
              send_frame(PFrame.NAK, 0, frame_exp, out_buf); 
            // start the timer for acknowledgement, in case there is no outgoing frame that can be piggybacked
            else
              start_ack_timer();

            // store the incoming frame into the buffer if it's seq is btw the receiver's window
            if (between(frame_exp, r.seq, too_far) && (arrived[r.seq % NR_BUFS] == false)){
              // frames may be accepted in any order
              arrived[r.seq % NR_BUFS] = true;   // mark buffer as full 
              in_buf[r.seq % NR_BUFS] = r.info;  // insert data into buffer  

              // up to the next not received frame
              while (arrived[frame_exp % NR_BUFS]){
                // pass frames and advance window
                to_network_layer(in_buf[frame_exp % NR_BUFS]);
                no_nak = true;  // allow the protocol to receive NAK
                arrived[frame_exp % NR_BUFS] = false;
                frame_exp = (frame_exp + 1) % (MAX_SEQ + 1);  // advance lower edge of receiver's window
                too_far = (too_far + 1) % (MAX_SEQ + 1);      // advance upper edge of receiver's window
                start_ack_timer();
              }
            }

          }

          //  if receive a NAK signal
          if (PFrame.KIND[r.kind].equals("NAK") && between(ack_exp, (r.ack+1)%(MAX_SEQ+1), next_frame_to_send)){
            // resend the frame
            send_frame(PFrame.DATA, (r.ack + 1) % (MAX_SEQ + 1), frame_exp, out_buf);
          }
          while (between(ack_exp, r.ack, next_frame_to_send)){
            nbuffered --;     
            stop_timer(ack_exp % NR_BUFS);    // frame arrive intact so stop the timers
            ack_exp = (ack_exp + 1) % (MAX_SEQ + 1);  // advance the lower edge of the sender's window
            enable_network_layer(1);          // get credit
          }
		      break;	

        case (PEvent.CKSUM_ERR):
          if (no_nak)
            // damaged frame, so send NAK
            send_frame(PFrame.NAK, 0, frame_exp, out_buf); 
      	  break; 

        case (PEvent.TIMEOUT): 
          // the sender doesnt receive any ack for the data, so resend the data
          send_frame(PFrame.DATA, oldest_frame, frame_exp, out_buf);   
	        break; 

	      case (PEvent.ACK_TIMEOUT): 
          // ack timer expired, send ack again
          send_frame(PFrame.ACK, 0, frame_exp, out_buf);  
          break; 
        
        default: 
		      System.out.println("SWP: undefined event type = " + event.type); 
		      System.out.flush();
	    } // end of switch
    }      
  }

 /* Note: when start_timer() and stop_timer() are called, 
    the "seq" parameter must be the sequence number, rather 
    than the index of the timer array, 
    of the frame associated with this timer, 
   */

   private void start_timer(int seq) {
      // stop the previous indicated timer
      stop_timer(seq);
      // create new timer for sending frames
      timer[seq % NR_BUFS] = new Timer();
      // schedule the task for execution after 500 ms
      timer[seq % NR_BUFS].schedule(new FrameTask(seq), 500); 
   }

   private void stop_timer(int seq) {
      try{
        timer[seq % NR_BUFS].cancel();
      } catch(Exception e) {}
   }

   private void start_ack_timer( ) {
      stop_ack_timer();
      ack_timer = new Timer();
      ack_timer.schedule(new AckTask(), 300);
   }

   private void stop_ack_timer() {
      try{
        ack_timer.cancel();
      } catch(Exception e) {}
   }

   class AckTask extends TimerTask {
      @Override
      public void run(){
        swe.generate_acktimeout_event();
      }
   }

   class FrameTask extends TimerTask {
      private int seq;
      public FrameTask(int seq){
        super();
        this.seq = seq;
      }
      @Override
      public void run(){
        swe.generate_timeout_event(this.seq);
      }
   }
}//End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
     swe.generate_acktimeout_event(), or
     swe.generate_timeout_event(seqnr).
*/


