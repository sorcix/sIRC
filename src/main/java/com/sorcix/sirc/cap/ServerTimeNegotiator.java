package com.sorcix.sirc.cap;

import com.sorcix.sirc.IrcPacket;

import java.io.IOException;

/**
 * @author pfnguyen
 */
public class ServerTimeNegotiator implements CapNegotiator.Listener {
    @Override
    public boolean onNegotiateFeature(CapNegotiator capNegotiator, String feature) {
        return false;
    }

    @Override
    public boolean onNegotiateMissing(CapNegotiator capNegotiator, String feature) {
        return false;
    }

    @Override
    public boolean onNegotiateList(
            CapNegotiator capNegotiator, String[] features) throws IOException {
        for (String feature : features) {
            if (feature.contains("server-time")) {
                capNegotiator.request(feature);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onNegotiate(CapNegotiator capNegotiator, IrcPacket packet) throws IOException {
        return false;
    }
}
