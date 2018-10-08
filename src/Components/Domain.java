package Components;

import java.util.ArrayList;

public class Domain {
    public static final int dataFrameSize=1500;//expressed in bytes
    public static final int slotDuration=20;//expressed in microseconds
    public static final int SIFSDuration=10;//microseconds
    public static final int ACK=30; //bytes
    public static final int RTS=30;//bytes
    public static final int CTS=30;//bytes
    public static final int  transmitRate=750000; //bytes/sec
    public static final int CWMax=1024;//slots
    public static final int CW0=4;
    public static final int simulationTime=10000000;//microseconds
    private static final int lambda1=50000000;//frames/microsecond
    private static final int lambda2=100000000;//frames/microsecond
    private static final int lambda3=200000000;//frames/microsecond
    private static final int lambda4=300000000;//frames/microsecond
    private int lambda;
    private ArrayList<Transmitter> transmitters;
    private ArrayList<Receiver> receivers;

    public Domain(){
        setLambda(0);
        transmitters=new ArrayList<>();
        receivers=new ArrayList<>();
    }

    public ArrayList<Transmitter> getTransmitters() {
        return transmitters;
    }

    public ArrayList<Receiver> getReceivers() {
        return receivers;
    }



    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public void addTransmitter(Transmitter t1){
        transmitters.add(t1);
    }
    public void addReceiver(Receiver r1){
        receivers.add(r1);
    }

}
