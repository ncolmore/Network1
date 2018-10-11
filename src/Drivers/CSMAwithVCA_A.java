package Drivers;

import Components.Transmitter;

public class CSMAwithVCA_A {
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
            //a<=clock b<=clock
            if(t1.getTrafficSlots().get(t1index)<= globalClock && t2.getTrafficSlots().get(t2index)<=globalClock){
                if(t1.backoffPlusDIFS < t2.backoffPlusDIFS){
                   scenario=0;
                }
               else if(t1.backoffPlusDIFS > t2.backoffPlusDIFS){
                    scenario=1;
                }
                else if(t1.backoffPlusDIFS == t2.backoffPlusDIFS){
                    scenario=2;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=globalClock+t1.backoffPlusDIFS+t1.getTransmissionTotal()+handshake;   //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=globalClock+t2.backoffPlusDIFS+t2.getTransmissionTotal()+handshake; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        globalClock=globalClock+handshake+t2.backoffPlusDIFS; //inc global timer
                        t2.doubleCW();
                        t1.doubleCW();//double both cw
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
                if(globalClock+t1.backoffPlusDIFS < t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS){ //t1 transmits
                    scenario=0;
                }
                else  if(globalClock+t1.backoffPlusDIFS > t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS){ //t2 transmits
                    scenario=1;
                }
                else if(globalClock+t1.backoffPlusDIFS == t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS){ //collision
                    scenario=2;
                }
                switch(scenario) {
                    case (0):
                            //t1 transmits
                        globalClock=globalClock+t1.backoffPlusDIFS+t1.getTransmissionTotal()+handshake;   //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=t2.getTrafficSlots().get(t2index)+t2.backoffPlusDIFS+t2.getTransmissionTotal()+handshake; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                        case (2)://collision
                        globalClock=globalClock+handshake+t2.backoffPlusDIFS; //inc global timer
                        t2.doubleCW();
                        t1.doubleCW();//double both cw
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
                if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS < globalClock+t2.backoffPlusDIFS){ //t1 transmits
                    scenario=0;
                }
                if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS > globalClock+t2.backoffPlusDIFS){ //t2 transmits
                    scenario=1;
                }
                if(t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS == globalClock+t2.backoffPlusDIFS){ //collision
                    scenario=2;
                }
                switch(scenario) {
                    case (0):
                        //t1 transmits
                        globalClock=t1.getTrafficSlots().get(t1index)+t1.backoffPlusDIFS+t1.getTransmissionTotal()+handshake;   //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t1index++;   //inc t1Index (packet to be serviced next)
                        t1.resetCW(); //reset CW
                        t1.generateBackoffTime();       //generate new b/o
                        t1.setTransmissions(t1.getTransmissions()+1); //increment counter
                        continue;  //continue from loop


                    case (1):
                        //t2 transmits
                        globalClock=globalClock+t2.backoffPlusDIFS+t2.getTransmissionTotal()+handshake; //inc global counter to the packet slot + DIFS + B/O + SIFS +ACK
                        t2index++;   //inc t2Index (packet to be serviced next)
                        t2.resetCW(); //reset CW for t2
                        t2.generateBackoffTime();//generate new b/o for t2
                        t2.setTransmissions(t2.getTransmissions()+1);//continue from loop
                        continue;

                    case (2)://collision
                        globalClock=globalClock+handshake+t2.backoffPlusDIFS; //inc global timer
                        t2.doubleCW();
                        t1.doubleCW();//double both cw
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
       if(t1.getTrafficSlots().get(0)+t1.getDIFS()+t1.getBackoffTime()<t2.getTrafficSlots().get(0)+t2.getDIFS()+t2.getBackoffTime()){ //t1 transmits first
           return 0;
       }
        if(t1.getTrafficSlots().get(0)+t1.getDIFS()+t1.getBackoffTime()==t2.getTrafficSlots().get(0)+t2.getDIFS()+t2.getBackoffTime()){ //t1 transmits first
            return 0;
        }
        else{
           return 1;//t2 transmits first
        }
    }

    public static int comparePacket(Transmitter t1, Transmitter t2, int t1Index, int t2Index){
        t1.generateBackoffTime();
        t2.generateBackoffTime();
        if(t1.getTrafficSlots().get(0)+t1.getDIFS()+t1.getBackoffTime()<t2.getTrafficSlots().get(0)+t2.getDIFS()+t2.getBackoffTime()){ //t1 transmits first
            return 0;
        }
        if(t1.getTrafficSlots().get(0)+t1.getDIFS()+t1.getBackoffTime()==t2.getTrafficSlots().get(0)+t2.getDIFS()+t2.getBackoffTime()){ //t1 transmits first
            return 0;
        }
        else{
            return 1;//t2 transmits first
        }
    }
}
