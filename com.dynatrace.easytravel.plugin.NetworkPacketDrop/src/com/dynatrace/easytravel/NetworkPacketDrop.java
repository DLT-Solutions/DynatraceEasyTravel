package com.dynatrace.easytravel;

import com.dynatrace.easytravel.spring.AbstractGenericPlugin;

public class NetworkPacketDrop extends AbstractGenericPlugin {

    @Override
    public Object doExecute(String location, Object... context) {
        /*  Pattern logic moved to uemload component com.dynatrace.diagnostics.uemload.networkpacketdrop.NetworkPacketDrop
            beceasue iptables rule should be applied only on Linux WebLauncher host.

            For now this is the only way to run problem pattern on WebLauncher host.
         */
        return null;
    }
}
