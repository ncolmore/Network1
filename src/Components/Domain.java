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
    private int CWCurrent;
    public static final int simulationTime=10000000;//microseconds
    private static final int lambda1=50000000;//frames/microsecond
    private static final int lambda2=100000000;//frames/microsecond
    private static final int lambda3=200000000;//frames/microsecond
    private static final int lambda4=300000000;//frames/microsecond
    private int lambda;
    private ArrayList<Transmitter> transmitters;
    private ArrayList<Receiver> receivers;
    private boolean isChannelBusy;
    private int globalFrameClock;
    private int aIndex;
    private int bIndex;

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

    public boolean isChannelBusy() {
        return isChannelBusy;
    }

    public void setChannelBusy(boolean channelBusy) {
        isChannelBusy = channelBusy;
    }

    public int getGlobalFrameClock() {
        return globalFrameClock;
    }

    public void setGlobalFrameClock(int globalFrameClock) {
        this.globalFrameClock = globalFrameClock;
    }

    public int getaIndex() {
        return aIndex;
    }

    public void setaIndex(int aIndex) {
        this.aIndex = aIndex;
    }

    public int getbIndex() {
        return bIndex;
    }

    public void setbIndex(int bIndex) {
        this.bIndex = bIndex;
    }

    public int getCWCurrent() {
        return CWCurrent;
    }

    public void setCWCurrent(int CWCurrent) {
        this.CWCurrent = CWCurrent;
    }

    public void doubleCW(){
        CWCurrent=CWCurrent*2;
    }
    public void resetCW(){
        CWCurrent=CW0;
    }
}
