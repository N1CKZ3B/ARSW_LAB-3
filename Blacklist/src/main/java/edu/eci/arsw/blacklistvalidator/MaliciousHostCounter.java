package edu.eci.arsw.blacklistvalidator;

import java.util.LinkedList;
import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;

public class MaliciousHostCounter extends Thread {
    private final String ip;
    private final int startIdx, endIdx;
    private int foundCount;
    private int checkedCount;
    private final HostBlacklistsDataSourceFacade dataSource;
    private final LinkedList<Integer> occurrences;
    private final HostBlackListsValidator validator;

    public MaliciousHostCounter(HostBlacklistsDataSourceFacade dataSource, String ip, int startIdx, int endIdx, HostBlackListsValidator validator) {
        this.dataSource = dataSource;
        this.ip = ip;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.validator = validator;
        this.occurrences = new LinkedList<>();
        foundCount = 0;
        checkedCount = 0;
    }

    @Override
    public void run() {
        for (int i = startIdx; i < endIdx && !validator.shouldStop(); i++) {
            checkedCount++;
            if (dataSource.isInBlackListServer(i, ip)) {
                occurrences.add(i);
                foundCount++;
                validator.reportOccurrence(i);

                if (validator.shouldStop()) {
                    break;
                }
            }
        }
    }

    public int getFoundCount() {
        return foundCount;
    }

    public int getCheckedCount() {
        return checkedCount;
    }

    public LinkedList<Integer> getOccurrences() {
        return occurrences;
    }
}
