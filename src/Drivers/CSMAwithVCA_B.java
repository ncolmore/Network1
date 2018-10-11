package Drivers;

import Components.Transmitter;

import java.util.SimpleTimeZone;

public class CSMAwithVCA_B {
    public static void main(String[] args)  {
        int globalClock=0;
        int simulationTime=500000;//slots
        int t1index=0;
        int t2index=0;
        int tempclock;
        final int SIFSDuration=1;//slot
        final int ACK=2; //dlots
        final int RTS=2;//slots
        final int CTS=2;//slots
         int dataFrameSize=100;//slots
        int transmissionTotal=ACK+SIFSDuration+dataFrameSize;//slots  total amount of slots used during successful transmission
        int handshake=RTS+SIFSDuration+CTS+SIFSDuration;
        Transmitter t1=new Transmitter();
        Transmitter t2=new Transmitter();
        t1.setLambda(Transmitter.getLambda1());//change lambdas here
        t2.setLambda(Transmitter.getLambda1()); //change lambdas here
        t1.generateUValues();
        t1.generateXValues();
        t2.generateUValues();
        t2.generateXValues();
        t1.generateTrafficSlots();
        t2.generateTrafficSlots();
        int first=getFirstPacket(t1,t2);
        if(first==0){
            globalClock=t1.getTrafficSlots().get(0);
        }
        else{
            globalClock=t2.getTrafficSlots().get(0);
        }
        while(globalClock<simulationTime){
            int scenario=0;
            int a,b,c,d;
            //a<=clock b<=clock
            if(t1.getTrafficSlots().get(t1index)<= globalClock && t2.getTrafficSlots().get(t2index)<=globalClock){
                if(t1.backoffPlusDIFS+RTS+SIFSDuration<=t2.backoffPlusDIFS){
                   scenario=0; //A reaches CTS before B tries to transmit anything
                }
                 else if(t2.backoffPlusDIFS+RTS+SIFSDuration<=t1.backoffPlusDIFS){
                    scenario=1;//B reaches CTS before A tries to transmit anything
                }
                else if(t1.backoffPlusDIFS+RTS<=t2.backoffPlusDIFS && t1.backoffPlusDIFS+RTS+SIFSDuration>t2.backoffPlusDIFS+RTS ){
                    //b may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=2;
                }
                else if(t2.backoffPlusDIFS+RTS<=t1.backoffPlusDIFS && t2.backoffPlusDIFS+RTS+SIFSDuration>t1.backoffPlusDIFS+RTS ){
                    //a may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=3;
                }
                else if (t2.backoffPlusDIFS==t1.backoffPlusDIFS){ //collision
                    scenario=4;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=t2.backoffPlusDIFS+handshake+transmissionTotal+globalClock; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        int collide;
                        t2.getTrafficSlots().set(t2index,globalClock+t2.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t2.doubleCW();
                        t2.generateBackoffTime();
                        if(t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS>globalClock+t1.backoffPlusDIFS+handshake && t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+1<globalClock+t1.backoffPlusDIFS+handshake+100){
                            collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t1 transmits
                                globalClock=globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t1index++;   //inc t1Index (packet to be serviced next)
                                t1.resetCW(); //reset CW
                                t1.generateBackoffTime();       //generate new b/o
                                t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t2.doubleCW(); //both before clock
                                t1.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal < t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                                }
                                else{
                                    globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t1.getTrafficSlots().set(t1index,tempclock+t1.backoffPlusDIFS+t1.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }


                        continue;

                    case (3)://collision
                        t1.getTrafficSlots().set(t1index,globalClock+t1.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t1.doubleCW();
                        t1.generateBackoffTime();
                        if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS>globalClock+t2.backoffPlusDIFS+handshake && t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+1<globalClock+t2.backoffPlusDIFS+handshake+100){
                            collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t2 transmits
                                globalClock=globalClock+t2.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t2index++;   //inc t1Index (packet to be serviced next)
                                t2.resetCW(); //reset CW
                                t2.generateBackoffTime();       //generate new b/o
                                t2.setTransmissions(t2.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t1.doubleCW(); //both before clock
                                t2.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(globalClock+t2.backoffPlusDIFS+handshake+transmissionTotal < t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal;
                                }
                                else{
                                    globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t2.getTrafficSlots().set(t2index,tempclock+t2.backoffPlusDIFS+t2.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }
                    case (4)://collision
                        t2.doubleCW(); //both before clock
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        globalClock=globalClock+t1.backoffPlusDIFS+SIFSDuration+CTS;
                        t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+RTS+CTS+SIFSDuration); //attempt to resend the packet after the missed ack
                        t1.getTrafficSlots().set(t1index,t2.getTrafficSlots().get(t2index)+RTS+SIFSDuration+CTS+t2.backoffPlusDIFS); //attempt to resend the packet after the missed ack
                        if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                            t2index++; //drop packet
                        }
                        if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                            t1index++;//drop packet
                        }
                        //generate new b/o for t1 and t2
                        t1.generateBackoffTime();
                        t2.generateBackoffTime();
                        t1.setCollisions(t1.getCollisions()+1);
                        t2.setCollisions(t2.getCollisions()+1);
                        //inc collision counter
                        continue;

                }
                continue;
            }
            //a<=clock b>clock
            else if(t1.getTrafficSlots().get(t1index)<= globalClock && t2.getTrafficSlots().get(t2index)>globalClock){
                if(t1.backoffPlusDIFS+RTS+SIFSDuration<=t2.backoffPlusDIFS+t2.getTrafficSlots().get(t2index)){
                    scenario=0; //A reaches CTS before B tries to transmit anything
                }
                else if(t2.backoffPlusDIFS+RTS+SIFSDuration+t2.getTrafficSlots().get(t2index)<=t1.backoffPlusDIFS){
                    scenario=1;//B reaches CTS before A tries to transmit anything
                }
                else if(t1.backoffPlusDIFS+RTS<=t2.backoffPlusDIFS+t2.getTrafficSlots().get(t2index) && t1.backoffPlusDIFS+RTS+SIFSDuration>t2.backoffPlusDIFS+RTS+t2.getTrafficSlots().get(t2index) ){
                    //b may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=2;
                }
                else if(t2.backoffPlusDIFS+RTS<=t1.backoffPlusDIFS+t1.getTrafficSlots().get(t1index) && t2.backoffPlusDIFS+RTS+SIFSDuration>t1.backoffPlusDIFS+RTS+t1.getTrafficSlots().get(t1index) ){
                    //a may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=3;
                }
                else if (t2.backoffPlusDIFS+t2.getTrafficSlots().get(t2index)==t1.backoffPlusDIFS){ //collision
                    scenario=4;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=t2.backoffPlusDIFS+handshake+transmissionTotal+t2.getTrafficSlots().get(t2index); //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        int collide;
                        t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t2.doubleCW();
                        t2.generateBackoffTime();
                        if(t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS>globalClock+t1.backoffPlusDIFS+handshake && t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+1<globalClock+t1.backoffPlusDIFS+handshake+100){
                            collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t1 transmits
                                globalClock=globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t1index++;   //inc t1Index (packet to be serviced next)
                                t1.resetCW(); //reset CW
                                t1.generateBackoffTime();       //generate new b/o
                                t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t2.doubleCW(); //both before clock
                                t1.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(globalClock+t1.backoffPlusDIFS+handshake+transmissionTotal < t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                                }
                                else{
                                    globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t1.getTrafficSlots().set(t1index,tempclock+t1.backoffPlusDIFS+t1.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }


                        continue;

                    case (3)://collision
                        t1.getTrafficSlots().set(t1index,globalClock+t1.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t1.doubleCW();
                        t1.generateBackoffTime();
                        if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS>t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+handshake && t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+1<t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+handshake+100){
                                collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t1 transmits
                                globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t2index++;   //inc t1Index (packet to be serviced next)
                                t2.resetCW(); //reset CW
                                t2.generateBackoffTime();       //generate new b/o
                                t2.setTransmissions(t2.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t1.doubleCW(); //both before clock
                                t2.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(t2.getTrafficSlots().get(2)+t2.backoffPlusDIFS+handshake+transmissionTotal < t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.transmissionTotal+handshake;
                                }
                                else{
                                    globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }
                    case (4)://collision
                        t2.doubleCW(); //both before clock
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        globalClock=globalClock+t1.backoffPlusDIFS+SIFSDuration+CTS;
                        t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+RTS+CTS+SIFSDuration); //attempt to resend the packet after the missed ack
                        t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+RTS+SIFSDuration+CTS+t1.backoffPlusDIFS); //attempt to resend the packet after the missed ack
                        if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                            t2index++; //drop packet
                        }
                        if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                            t1index++;//drop packet
                        }
                        //generate new b/o for t1 and t2
                        t1.generateBackoffTime();
                        t2.generateBackoffTime();
                        t1.setCollisions(t1.getCollisions()+1);
                        t2.setCollisions(t2.getCollisions()+1);
                        //inc collision counter
                        continue;

                }
                continue;
            }
            //a>clock b<=clock
            else if(t1.getTrafficSlots().get(t1index) > globalClock && t2.getTrafficSlots().get(t2index)<= globalClock){
                if(t1.backoffPlusDIFS+RTS+SIFSDuration+t1.getTrafficSlots().get(t1index)<=t2.backoffPlusDIFS){
                    scenario=0; //A reaches CTS before B tries to transmit anything
                }
                else if(t2.backoffPlusDIFS+RTS+SIFSDuration<=t1.backoffPlusDIFS+t1.getTrafficSlots().get(t1index)){
                    scenario=1;//B reaches CTS before A tries to transmit anything
                }
                else if(t1.backoffPlusDIFS+RTS+t1index<=t2.backoffPlusDIFS&& t1.backoffPlusDIFS+RTS+SIFSDuration+t1.getTrafficSlots().get(t1index)>t2.backoffPlusDIFS+RTS ){
                    //b may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=2;
                }
                else if(t2.backoffPlusDIFS+RTS<=t1.backoffPlusDIFS+t1.getTrafficSlots().get(t1index) && t2.backoffPlusDIFS+RTS+SIFSDuration>t1.backoffPlusDIFS+RTS+t1.getTrafficSlots().get(t1index) ){
                    //a may collide, it will not recieve an ACK and needs to reselect a new B/O. If the new RTS is sent within the data frame->collision
                    scenario=3;
                }
                else if (t2.backoffPlusDIFS==t1.backoffPlusDIFS+t1.getTrafficSlots().get(t1index)){ //collision
                    scenario=4;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=t2.backoffPlusDIFS+handshake+transmissionTotal+globalClock; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        int collide;
                        t2.getTrafficSlots().set(t2index,globalClock+t2.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t2.doubleCW();
                        t2.generateBackoffTime();
                        if(globalClock+t2.backoffPlusDIFS>t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+handshake && globalClock +t2.backoffPlusDIFS+1<t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+handshake+100){
                            collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t1 transmits
                                globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t1index++;   //inc t1Index (packet to be serviced next)
                                t1.resetCW(); //reset CW
                                t1.generateBackoffTime();       //generate new b/o
                                t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t2.doubleCW(); //both before clock
                                t1.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+handshake+transmissionTotal < globalClock+t2.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.transmissionTotal;
                                }
                                else{
                                    globalClock=globalClock+t2.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t2.getTrafficSlots().set(t2index,globalClock+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }


                        continue;

                    case (3)://collision
                        t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+RTS+SIFSDuration+CTS);
                        t1.doubleCW();
                        t1.generateBackoffTime();
                        if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS>globalClock+t2.backoffPlusDIFS+handshake && t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+1<globalClock+t2.backoffPlusDIFS+handshake+100){
                            collide=1;
                        }
                        else{
                            collide=0;
                        }
                        switch (collide){
                            case(0): //no collision and t1 transmits
                                globalClock=globalClock+t2.backoffPlusDIFS+handshake+transmissionTotal;  //inc global counter to
                                t2index++;   //inc t1Index (packet to be serviced next)
                                t2.resetCW(); //reset CW
                                t2.generateBackoffTime();       //generate new b/o
                                t2.setTransmissions(t2.getTransmissions()+1); //increment counter
                                continue;
                            case(1):
                                t1.doubleCW(); //both before clock
                                t2.doubleCW();//double both cw
                                tempclock=globalClock;
                                if(globalClock+t2.backoffPlusDIFS+handshake+transmissionTotal < t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+SIFSDuration+CTS){ //t1 occurs first-> put global clock there
                                    globalClock=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal+handshake;
                                }
                                else{
                                    globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+ SIFSDuration+CTS;
                                }
                                t2.getTrafficSlots().set(t2index,globalClock+t2.backoffPlusDIFS+t2.transmissionTotal+handshake); //attempt to resend the packet after the missed ack
                                t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+RTS+SIFSDuration+CTS); //attempt to resend the packet after the missed ack
                                if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                                    t2index++; //drop packet
                                }
                                if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                                    t1index++;//drop packet
                                }
                                t1.generateBackoffTime();
                                t2.generateBackoffTime();
                                t1.setCollisions(t1.getCollisions()+1);
                                t2.setCollisions(t2.getCollisions()+1);
                                continue;
                        }
                    case (4)://collision
                        t2.doubleCW(); //both before clock
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+SIFSDuration+CTS;
                        t2.getTrafficSlots().set(t2index,globalClock+t2.backoffPlusDIFS+RTS+CTS+SIFSDuration); //attempt to resend the packet after the missed ack
                        t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+RTS+SIFSDuration+CTS+t1.backoffPlusDIFS); //attempt to resend the packet after the missed ack
                        if(t2.getCWCurrent() > t2.getCWMax()){ //handle cwMax
                            t2index++; //drop packet
                        }
                        if(t1.getCWCurrent()>t1.getCWMax()){ //handle cwMax
                            t1index++;//drop packet
                        }
                        //generate new b/o for t1 and t2
                        t1.generateBackoffTime();
                        t2.generateBackoffTime();
                        t1.setCollisions(t1.getCollisions()+1);
                        t2.setCollisions(t2.getCollisions()+1);
                        //inc collision counter
                        continue;

                }
                continue;
            }
            //a>clock b>clock
          else if(t1.getTrafficSlots().get(t1index)> globalClock && t2.getTrafficSlots().get(t2index)>globalClock){ //both greater than clock
                //find lowest slot # and forward globalClock
                if(t1.getTrafficSlots().get(t1index)<t2.getTrafficSlots().get(t2index)){
                    globalClock=t1.getTrafficSlots().get(t1index);
                }
                else{
                    globalClock=t2.getTrafficSlots().get(t2index);
                }
                continue;
            }


        }
        System.out.println("t1 transmissions"+ t1.getTransmissions());
        System.out.println("t2 transmissions"+ t2.getTransmissions());
        System.out.println("Collisions"+ t2.getCollisions());

    }

    public static int getFirstPacket(Transmitter t1, Transmitter t2){
       t1.generateBackoffTime();
       t2.generateBackoffTime();
       if(t1.getTrafficSlots().get(0)+t1.backoffPlusDIFS<t2.getTrafficSlots().get(0)+t2.backoffPlusDIFS){ //t1 transmits first
           return 0;
       }
        if(t1.getTrafficSlots().get(0)+t1.backoffPlusDIFS==t2.getTrafficSlots().get(0)+t2.backoffPlusDIFS){ //t1 transmits first
            return 0;
        }
        else{
           return 1;//t2 transmits first
        }
    }

    public static int comparePacket(Transmitter t1, Transmitter t2, int t1Index, int t2Index){
        t1.generateBackoffTime();
        t2.generateBackoffTime();
        if(t1.getTrafficSlots().get(0)+t1.backoffPlusDIFS<t2.getTrafficSlots().get(0)+t2.backoffPlusDIFS){ //t1 transmits first
            return 0;
        }
        if(t1.getTrafficSlots().get(0)+t1.backoffPlusDIFS==t2.getTrafficSlots().get(0)+t2.backoffPlusDIFS){ //t1 transmits first
            return 0;
        }
        else{
            return 1;//t2 transmits first
        }
    }
}
