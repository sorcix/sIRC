package com.sorcix.sirc.cap;

import com.sorcix.sirc.IrcPacket;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author pfnguyen
 */
public class CompoundNegotiator implements CapNegotiator.Listener {
    private List<CapNegotiator.Listener> listeners =
            new CopyOnWriteArrayList<CapNegotiator.Listener>();

    public CompoundNegotiator(CapNegotiator.Listener... listeners) {
        for (CapNegotiator.Listener listener : listeners) {
            addListener(listener);
        }
    }

    public void addListener(CapNegotiator.Listener listener) {
        listeners.add(listener);
    }

    @Override
    public boolean onNegotiateFeature(CapNegotiator capNegotiator, String feature) {
        boolean more = false;
        for (CapNegotiator.Listener listener : listeners) {
            boolean keep = listener.onNegotiateFeature(capNegotiator, feature);
            more = more || keep;
            if (!keep)
                listeners.remove(listener);
        }
        return more;
    }

    @Override
    public boolean onNegotiateMissing(CapNegotiator capNegotiator, String feature) {
        boolean more = false;
        for (CapNegotiator.Listener listener : listeners) {
            boolean keep = listener.onNegotiateMissing(capNegotiator, feature);
            more = more || keep;
            if (!keep)
                listeners.remove(listener);
        }
        return more;
    }

    @Override
    public boolean onNegotiateList(CapNegotiator capNegotiator, String[] features) throws IOException {
        boolean more = false;
        for (CapNegotiator.Listener listener : listeners) {
            boolean keep = listener.onNegotiateList(capNegotiator, features);
            more = more || keep;
            if (!keep)
                listeners.remove(listener);
        }
        return more;
    }

    @Override
    public boolean onNegotiate(CapNegotiator capNegotiator, IrcPacket packet) throws IOException {
        boolean more = false;
        for (CapNegotiator.Listener listener : listeners) {
            boolean keep = listener.onNegotiate(capNegotiator, packet);
            more = more || keep;
            if (!keep)
                listeners.remove(listener);
        }
        return more;
    }
}
