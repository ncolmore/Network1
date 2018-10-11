package Drivers;

import Components.Transmitter;

import java.util.ArrayList;

public class CSMAwithCollisionAvoidanceB {
    public static void main(String[] args)  {
        int globalClock=0;
        int simulationTime=500000;//slots
        int t1index=0;
        int t2index=0;
        int tempclock;
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
                a=globalClock+t1.backoffPlusDIFS;
                b=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                c=globalClock+t2.backoffPlusDIFS;
                d=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal;
                if(b<c){ //no overlap t1 transmits
                   scenario=0;
                }
                 else if(a>d){ //no overlap t2 tx
                    scenario=1;
                }
                else{ //collision
                    scenario=2;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=b;  //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=d; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        t2.doubleCW(); //both before clock
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        if(globalClock+t1.backoffPlusDIFS < globalClock+t2.backoffPlusDIFS){ //t1 occurs first-> put global clock there
                            globalClock=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                        }
                        else{
                            globalClock=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal;
                        }
                        t1.getTrafficSlots().set(t1index,tempclock+t1.backoffPlusDIFS+t1.transmissionTotal); //attempt to resend the packet after the missed ack
                        t2.getTrafficSlots().set(t2index,tempclock+t2.backoffPlusDIFS+t2.transmissionTotal); //attempt to resend the packet after the missed ack
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
                    a=globalClock+t1.backoffPlusDIFS;
                    b=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                    c=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS;
                    d=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.transmissionTotal;
                    if(b<c){ //no overlap t1 transmits
                        scenario=0;
                    }
                    else if(a>d){ //no overlap t2 tx
                        scenario=1;
                    }
                    else{ //collision
                        scenario=2;
                    }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=b;  //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=d; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision a<= b>
                        t2.doubleCW();
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        if(globalClock+t1.backoffPlusDIFS < t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS){ //t1 occurs first-> put global clock there
                            globalClock=globalClock+t1.backoffPlusDIFS+t1.transmissionTotal;
                        }
                        else{
                            globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.transmissionTotal;
                        }
                        t1.getTrafficSlots().set(t1index,tempclock+t1.backoffPlusDIFS+t1.transmissionTotal); //attempt to resend the packet after the missed ack
                        t2.getTrafficSlots().set(t2index,t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.transmissionTotal); //attempt to resend the packet after the missed ack
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
          else if(t1.getTrafficSlots().get(t1index)> globalClock && t2.getTrafficSlots().get(t2index)<=globalClock){
                    a=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS;
                    b=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.transmissionTotal;
                    c=globalClock+t2.backoffPlusDIFS;
                    d=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal;
                    if(b<c){ //no overlap t1 transmits
                        scenario=0;
                    }
                    else if(a>d){ //no overlap t2 tx
                        scenario=1;
                    }
                    else{ //collision
                        scenario=2;
                    }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=b;  //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=d; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision a> b<=
                        t2.doubleCW();
                        t1.doubleCW();//double both cw
                        tempclock=globalClock;
                        if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS < globalClock+t2.backoffPlusDIFS){ //t1 occurs first-> put global clock there
                            globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.transmissionTotal;
                        }
                        else{
                            globalClock=globalClock+t2.backoffPlusDIFS+t2.transmissionTotal;
                        }
                        t1.getTrafficSlots().set(t1index,t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.transmissionTotal); //attempt to resend the packet after the missed ack
                        t2.getTrafficSlots().set(t2index,tempclock+t2.backoffPlusDIFS+t2.transmissionTotal); //attempt to resend the packet after the missed ack
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
