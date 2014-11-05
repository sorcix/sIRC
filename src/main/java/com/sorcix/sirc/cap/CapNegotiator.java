package com.sorcix.sirc.cap;

import com.sorcix.sirc.IrcOutput;
import com.sorcix.sirc.IrcPacket;
import com.sorcix.sirc.IrcPacketFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author pfnguyen
 */
public class CapNegotiator {
    public interface Listener {
        /**
         * successful feature request
         * @param feature
         * @return true if this listener needs to process packets
         */
        boolean onNegotiateFeature(CapNegotiator capNegotiator, String feature);

        /**
         * unsuccessful feature request
         * @param feature
         * @return true if this listener should not be ignored (used by CompoundNegotiator)
         */
        boolean onNegotiateMissing(CapNegotiator capNegotiator, String feature);

        /**
         * list of features available on the server
         * @param features
         * @return true if this listener will request feature(s)
         */
        boolean onNegotiateList(CapNegotiator capNegotiator, String[] features)
            throws IOException;

        /**
         * while negotiation is active, packets are forwarded to the listener(s)
         * @param packet
         * @return true while this listener needs to receive more packets
         */
        boolean onNegotiate(CapNegotiator capNegotiator, IrcPacket packet)
                throws IOException;
    }

    private final IrcOutput output;

    public CapNegotiator(IrcOutput output) {
        this.output = output;
    }

    public void cancel() {
        negotiating = false;
        listeners.clear();
    }

    public void process(IrcPacket packet) throws IOException {
        String[] args = packet.getArgumentsArray();
        boolean isCapCmd = "CAP".equals(packet.getCommand());
        if (isCapCmd && args.length > 1) {
            String command = args[1];
            if ("ACK".equals(command)) {
                for (Listener l : listeners) {
                    if (!l.onNegotiateFeature(this, packet.getMessage()))
                        listeners.remove(l);
                }
            } else if ("NAK".equals(command)) {
                for (Listener l : listeners) {
                    l.onNegotiateMissing(this, packet.getMessage());
                    listeners.remove(l);
                }
            } else if ("LS".equals(command)) {
                negotiating = true;
                String[] features = packet.getMessage().split(" ");
                for (Listener l : listeners) {
                    if (!l.onNegotiateList(this, features))
                        listeners.remove(l);
                }
            }
        } else {
            for (Listener l : listeners) {
                if (!l.onNegotiate(this, packet))
                    listeners.remove(l);
            }
        }
        if (listeners.size() == 0) {
            cancel();
            send(IrcPacketFactory.createCAPEND().getRaw());
        }
    }

    public void request(final String feature) throws IOException {
        send(IrcPacketFactory.createCAPREQ(feature).getRaw());
    }
    public void send(final String rawCommand) throws IOException {
        output.sendNowEx(rawCommand);
    }

    public void addListener(Listener l) {
        listeners.add(l);
    }

    public void removeListener(Listener l) {
        listeners.remove(l);
    }

    public boolean isNegotiating() {
        return negotiating;
    }

    private boolean negotiating = false;
    private List<Listener> listeners = new CopyOnWriteArrayList<Listener>();
}
