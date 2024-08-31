package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HostBlackListsValidator {
    private int checkedCount = 0;
    private int ocurrencesCount = 0;
    private static final int BLACK_LIST_ALARM_COUNT = 5;
    private final Object lock = new Object();

    public List<Integer> checkHost(String ipaddress, int N) {
        LinkedList<Integer> blackListOcurrences = new LinkedList<>();
        HostBlacklistsDataSourceFacade skds = HostBlacklistsDataSourceFacade.getInstance();
        int partir = skds.getRegisteredServersCount() / N;
        List<MaliciousHostCounter> maliciousHosts = new ArrayList<>();
        int start, end;

        for (int i = 0; i < N; i++) {
            start = i * partir;
            if (i == N - 1) {
                end = skds.getRegisteredServersCount();
            } else {
                end = (i + 1) * partir;
            }
            MaliciousHostCounter host = new MaliciousHostCounter(skds, ipaddress, start, end, this);
            maliciousHosts.add(host);
        }

        for (MaliciousHostCounter host : maliciousHosts) {
            host.start();
        }

        for (MaliciousHostCounter host : maliciousHosts) {
            try {
                host.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            blackListOcurrences.addAll(host.getOccurrences());
            checkedCount += host.getCheckedCount();
            ocurrencesCount += host.getFoundCount();
        }

        if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
            skds.reportAsNotTrustworthy(ipaddress);
        } else {
            skds.reportAsTrustworthy(ipaddress);
        }

        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedCount, skds.getRegisteredServersCount()});
        return blackListOcurrences;
    }

    public synchronized void reportOccurrence(int serverIndex) {
        synchronized (lock) {
            ocurrencesCount++;
            if (ocurrencesCount >= BLACK_LIST_ALARM_COUNT) {
                lock.notifyAll();
            }
        }
    }

    public synchronized boolean shouldStop() {
        synchronized (lock) {
            return ocurrencesCount >= BLACK_LIST_ALARM_COUNT;
        }
    }

    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
}
