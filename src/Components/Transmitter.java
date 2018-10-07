package Components;

public class Transmitter {
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
    private int backoffTime;//expressed in microseconds
    private static final int DIFS=40;//expressed in microseconds
    private boolean transmitFrame;
    private int collisions;


    public Transmitter(){
    setBackoffTime(0);
    setCollisions(0);
    setTransmitFrame(false);
    }
    public int getBackoffTime() {
        return backoffTime;
    }

    public void setBackoffTime(int backoffTime) {
        this.backoffTime = backoffTime;
    }

    public int getDIFS() {
        return DIFS;
    }

    public boolean isTransmitFrame() {
        return transmitFrame;
    }

    public void setTransmitFrame(boolean transmitFrame) {
        this.transmitFrame = transmitFrame;
    }

    public int getCollisions() {
        return collisions;
    }

    public void setCollisions(int collisions) {
        this.collisions = collisions;
    }





}
